/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
/**
 * (c) Copyright American Institutes for Research, unpublished work created 2008-2013
 *  All use, disclosure, and/or reproduction of this material is
 *  prohibited unless authorized in writing. All rights reserved.
 *
 *  Rights in this program belong to:
 *  American Institutes for Research.
 *  
 *  Code based on AIROnline2012 C# project - Re-factored here 
 */

package tds.itemselection.algorithms;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import AIR.Common.DB.SQLConnection;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.IRTMeasures;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.ReportingCategory;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.impl.sets.CSetItemGroup;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.StudentAbility;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.loader.TestSegment;

/**
 * Class to adaptively select next set of items to administer for the given
 * student opportunity
 * @author aphilip
 * @author akulakov
 * 
 */

public class AdaptiveSelector2   extends AbstractAdaptiveSelector  implements IItemSelection{
	  private static Logger  _logger  = LoggerFactory.getLogger (AdaptiveSelector2.class);

	/**
	 * Random item picker from CSet2
	 */
    private static final Random rand = new Random();
    
    /**
     * Database interface layer
     */
    //private DatabaseAccess _db;
    // instead of DatabaseAccess
    @Autowired
    @Qualifier("aa2DBLoader")
    private IItemSelectionDBLoader loader;
        
//    /**
//     * Student opportunity
//     */
//    private StudentTestOpportunity _opportunity;
    
    private Boolean _debug = false;
    
    /**
     * Past test history of the student 
     */
    private StudentHistory2013 _studentHistory = new StudentHistory2013();

    /**
     * Select the next item group to be administered
     * @return
     */
    public ItemGroup getNextItemGroup(SQLConnection connection,
			ItemCandidatesData itemData) throws ItemSelectionException 
    {
        ItemGroup candidateItemGroup = null;
 
        try
        {
            // We have a collection of segments for our use 
            SegmentCollection2 segs = SegmentCollection2.getInstance();

            // Get the segment for this opportunity
            TestSegment segment = segs.getSegment(connection, itemData.getSession(), itemData.getSegmentKey(), loader);
            if (segment == null)
                throw new ItemSelectionException("Unable to load segment");

            // Load the opportunities past history
            _studentHistory = loader.loadOppHistory(connection, itemData.getOppkey(), itemData.getSegmentKey());

            // Estimate/Update theta for the student based on past history
            // theta is student ability
            StudentAbility studentAbility = new StudentAbility();
            _studentHistory.updateStudentAbility(segment.getBp(), segment.getPool(), studentAbility);

            // Check if we have already satisfied terminating conditions if so terminate
            String Reason = "";
            // now that the blueprint has been updated to reflect all previous responses, 
            // check that we haven't satisfied configured termination
            //  conditions.
            // function below is the same as all C# space "TerminationManager"
            if (CanTerminate(segment.getBp(), studentAbility, Reason))
            {
            	this.isSegmentCompleted = true; 
            	return null;
            }
            
            // Get list of candidate item groups based on current segment and past history
            // TODO - Implement strict max checks and item pool recycling
            ArrayList<CSetItemGroup> candidateGroups = _studentHistory.getCandidateItemGroups(segment);

            // Calculate the selection metric for candidate groups
            int nNumberOfCandidates = candidateGroups.size();
            for(int i=0; i < nNumberOfCandidates; ++i)
            {      
            	CSetItemGroup csetItemGroup = candidateGroups.get(i);
            	csetItemGroup.CalculateSelectionMetric(segment.getBp(), studentAbility);
            }
            
            // Normalize the computed measures for the candidate set
            NormalizeCandidateMeasures(candidateGroups);

            // Get the weights from blueprint/config.
            double w0 = segment.getBp().bpWeight;
            double w1 = segment.getBp().rcAbilityWeight;
            double w2 = segment.getBp().overallInformationMatchWeight;
            
            // (Descending) Sort the candidateGroups setting w1 & w0 0 and picking the top CSet1Size as CSet1
            // TODO why I need to check it? Maybe check candidateGroups.size() >= 1 ?
            if (candidateGroups.size() < segment.getBp().cSet1Size)
            	throw new Exception ("Insuffient number of candidate items for selection");
            
            int pickingSize = Math.min(candidateGroups.size() , segment.getBp().cSet1Size);
            Collections.sort(candidateGroups, new CSetItemGroup.ObjectiveFunction(0D, 0D, w2));            
            ArrayList<CSetItemGroup> cSet1 = new ArrayList<CSetItemGroup>(candidateGroups.subList(0, pickingSize));

            // Now use all the weighted measures to get the f values pick the top CSet2Size as CSet2 - (Descending) Sort
            Collections.sort(cSet1, new CSetItemGroup.ObjectiveFunction(w0, w1, w2));
            ArrayList<CSetItemGroup> CSet2 = new ArrayList<CSetItemGroup>(cSet1.subList(0, segment.getBp().cSet2Size));
            
            // Pick a random CSetItemGroup as next candidate - Create and set a new ItemGroup
            int candidateIndex = rand.nextInt(segment.getBp().cSet2Size);
            CSetItemGroup candidateCSetItemGroup = CSet2.get(candidateIndex);
            
            ItemGroup baseItemGroup = candidateCSetItemGroup.baseGroup;
            candidateItemGroup = new ItemGroup(baseItemGroup.getGroupID(), baseItemGroup.getNumberOfItemsRequired(),
                                               baseItemGroup.getMaximumNumberOfItems());
            int nItems = candidateCSetItemGroup.items.size();
            for (int j=0 ; j < nItems ; ++j)
            {
            	candidateItemGroup.getItems().add(candidateCSetItemGroup.items.get(j));
            }
        }
        catch (Exception ex)
        {
        	_logger.error(ex.getMessage());
            throw new ItemSelectionException (ex.getMessage());
        }
        return candidateItemGroup; 
    }
 
    /**
     * Normalize the measures of the candidate groups in the set 
     * @param candidateGroups
     */
    private void NormalizeCandidateMeasures(ArrayList<CSetItemGroup> candidateGroups)
    {
    	int nNumberOfCandidates = candidateGroups.size();
    	if (nNumberOfCandidates <= 0)
    		return ;
    	
        Collections.sort(candidateGroups, CSetItemGroup.ContentValueComparatorDesc);
        double contentValueMin = candidateGroups.get(0).contentValue;
        double contentValueMax = candidateGroups.get(nNumberOfCandidates-1).contentValue;

        Collections.sort(candidateGroups, CSetItemGroup.RCInformationComparatorDesc);
        double RCInformationMin = candidateGroups.get(0).reportingCategoryInformationValue;
        double RCInformationMax = candidateGroups.get(nNumberOfCandidates-1).reportingCategoryInformationValue;
        
        Collections.sort(candidateGroups, CSetItemGroup.OverallInformationComparatorDesc);
        double overallInformationMin = candidateGroups.get(0).overallInformationValue;
        double overallInformationMax = candidateGroups.get(nNumberOfCandidates-1).overallInformationValue;

        for(CSetItemGroup csetItemGroup: candidateGroups)
        {      
        	csetItemGroup.NormalizeContentValue(contentValueMin, contentValueMax);
        	csetItemGroup.NormalizeReportingCategoryInformationValue(RCInformationMin, RCInformationMax);
        	csetItemGroup.NormalizeOverallInformationValue(overallInformationMin, overallInformationMax);
        }
    }
    
    /**
     * Check whether we can stop the test based on the conditions and items
     * administered so far
     * @param bluePrint
     * @param studentAbility
     * @param Reason
     * @return
     */
    private boolean CanTerminate(Blueprint bluePrint, StudentAbility studentAbility, String Reason)
    {
    	boolean bTerminate = false ;
    	Reason = "";
    	if (bluePrint.terminateBasedOnCount)
    	{
    		bTerminate = CanTerminateBasedOnCount(bluePrint, studentAbility);
    		if (bTerminate)
    			Reason = "TermCount";
    	}
    	if (bluePrint.terminateBasedOnOverallInformation && 
    			(!bTerminate || bluePrint.terminateBaseOnFlagsAnd))
    	{
    		bTerminate = CanTerminateBasedOnOverallInformation(bluePrint, studentAbility);
    		if (bTerminate)
    		{
	    		if (!Reason.isEmpty())
	    			Reason.concat(" & ");
	    		Reason.concat("TermOverall");
    		}    			
    	}
    	if (bluePrint.terminateBasedOnReportingCategoryInformation&& 
    			(!bTerminate || bluePrint.terminateBaseOnFlagsAnd))
    	{
    		bTerminate = CanTerminateBasedOnReportingCategoryInformation(bluePrint, studentAbility);    		
    		if (bTerminate)
    		{
	    		if (!Reason.isEmpty())
	    			Reason.concat(" & ");
	    		Reason.concat("TermReporting");
    		}    			
    	}
    	if (bluePrint.terminateBasedOnScoreTooClose&& 
    			(!bTerminate || bluePrint.terminateBaseOnFlagsAnd))
    	{
    		bTerminate = CanTerminateBasedOnScoreTooClose(bluePrint, studentAbility);    		
    		if (bTerminate)
    		{
	    		if (!Reason.isEmpty())
	    			Reason.concat(" & ");
	    		Reason.concat("TermTooClose");
    		}    			
    	}
    	return !Reason.isEmpty();
    }
    
    /**
     * Can we terminate now based on count of items administered
     * @param bp
     * @param studentAbility
     * @return
     */
    private boolean CanTerminateBasedOnCount(Blueprint bluePrint, StudentAbility studentAbility)
    {    	
    	boolean bCanTerminate = (studentAbility.overallAbility.count >= bluePrint.minOpItems);
    	ArrayList<BpElement> bpElements = bluePrint.getBPElements();
		int nCount = bpElements.size();
		int i = 0; 
		while(bCanTerminate && i < nCount)
		{
			BpElement bp = bpElements.get(i);
			int nAdministeredCount = studentAbility.getAdministeredBPFeatureCount(bp.ID);
			bCanTerminate = (nAdministeredCount >= bp.minRequired);
		}
		return bCanTerminate;
    }
    
    /**
     * Test can be terminated based on student achieving required information
     * @param bluePrint
     * @param studentAbility
     * @return
     */
    private boolean CanTerminateBasedOnOverallInformation(Blueprint bluePrint, StudentAbility studentAbility)
    {
    	return (studentAbility.overallAbility.Information >= bluePrint.overallTargetInformation);
    }

    /**
     * Test can be terminated based on student achieving required information in all reporting categories
     * @param bluePrint
     * @param studentAbility
     * @return
     */
    private boolean CanTerminateBasedOnReportingCategoryInformation(Blueprint bluePrint, StudentAbility studentAbility)
    {    	
    	ArrayList<ReportingCategory> reportingCategories = bluePrint.getReportingCategories();
		int nCount = reportingCategories.size();
		int i = 0; 
		boolean bCanTerminate = true ;
		while(bCanTerminate && i < nCount)
		{
			ReportingCategory rc = reportingCategories.get(i);
			IRTMeasures irtMeasure = studentAbility.getRCAbility(rc.ID);
			bCanTerminate = irtMeasure != null && ((irtMeasure.Information >= rc.targetInformation));
		}
		return bCanTerminate;
    }

    /**
     * Test can be terminated theta difference is not significant
     * @param bluePrint
     * @param studentAbility
     * @return
     */
    private boolean CanTerminateBasedOnScoreTooClose(Blueprint bluePrint, StudentAbility studentAbility)
    {    	
    	double fThetaDifference = studentAbility.overallAbility.Theta - bluePrint.adaptiveCut.doubleValue();
    	double fTimesSigma = studentAbility.overallAbility.StandardError * bluePrint.tooCloseSEs.doubleValue();
    	return Math.abs(fThetaDifference) < fTimesSigma ;
    }
    
	@Override
	public String getItemSelectorError() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
