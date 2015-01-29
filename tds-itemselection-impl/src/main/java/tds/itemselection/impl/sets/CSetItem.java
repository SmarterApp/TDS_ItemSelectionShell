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
package tds.itemselection.impl.sets;

import java.util.Iterator;

import tds.itemgroupselection.measurementmodels.GaussHermiteQuadrature;
import tds.itemgroupselection.measurementmodels.GaussianFunctionIRT;
import tds.itemgroupselection.measurementmodels.IRTModel;
import tds.itemselection.base.Dimension;
import tds.itemselection.base.IRTMeasures;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.BpMetric;
import tds.itemselection.impl.blueprint.ReportingCategory;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.loader.StudentAbility;


/**
 * A wrapper around TestItem which is considered as a candidate for selection.
 * This class also includes all logic involving such selection
 * @author aphilip
 */
public class CSetItem extends CsetItem
{
//	/**
//	 * Base item which this class is wrapping around
//	 */
//    public TestItem Item;

    /**
     * Content value associated with this item (unnormalized)
     */
    public double ContentValue = 0.0; // Default value

    /**
     * Weighted reporting category information value associated with this item (unnormalized) 
     */
    public double ReportingCategoryInformationValue = 0.0; // Default value;

    /**
     * Weighted overall information value associated with this item (unnormalized) 
     */
    public double OverallInformationValue = 0.0; // Default value;
    
    public double rcAbilityMetric = 0.0; //added for 2013; normalized ability match value WRT RCs
    public double selectionMetric;
    public double BpJitter;

	public boolean abilityMatchCalculated = false;
    /**
     * Constructor
     * @param item
     */
    public CSetItem(TestItem item)
    {
    	super(item);
    }

    public CSetItem(TestItem item2, CsetGroup csetGrp) {
    	super(item2, csetGrp);
	}

	/**
     * Calculate selection metric for this item
     * @param bp
     * @param ability
     */
    public void CalculateSelectionMetric(Blueprint bp, StudentAbility ability)
    {
        // Overall item content value 
        ContentValue = 0D;        
    	for(int i=0, nContentLevels = contentLevels.size() ; i < nContentLevels; ++i)
    		ContentValue += GetContentValueForFeature(contentLevels.get(i), bp, ability);
    	ContentValue += GetContentValueForFeature(strandName, bp, ability);

		// Set the overall information value
        IRTMeasures overallMeasures = ability.overallAbility;
        double fItemInformation = GetInformation(overallMeasures);
		OverallInformationValue = ComputeH0(bp.hweightBeforeOverallTargetMet, 
        		bp.hweightAfterOverallTargetMet, fItemInformation, overallMeasures.Information, bp.overallTargetInformation);

		// Set the reporting category information value // TODO check calculations
        ReportingCategoryInformationValue = 0F;
        for(int i=0, nContentLevels = contentLevels.size() ; i < nContentLevels; ++i)
        	ReportingCategoryInformationValue += GetInformationForFeature(contentLevels.get(i), bp, ability);
        ReportingCategoryInformationValue += GetInformationForFeature(strandName, bp, ability);        
    }
    
    private double GetContentValueForFeature(String sFeature, Blueprint bp, StudentAbility ability)
    {
    	// Content Value
    	double fContentValue = 0D;
    	
		// Get the number of this blue print feature administered so far
		int nAdministered = ability.getAdministeredBPFeatureCount(sFeature);
		
		// Get the blueprint feature from blueprint
		BpElement bpElement = bp.getBPElement(sFeature); 		
		if (bpElement == null)
			return fContentValue;
		
        // Minimum and Maximum required items for this blueprint feature 
        int nMinRequired = bpElement.minRequired; 
        int nMaxRequired = bpElement.maxRequired;

        // Get the priority weight for this feature 
        double priorityWeight = bpElement.weight;

        // Calculate the m function which rewards depending on time left
        double T = (double) bp.maxOpItems;
        double t = (double) ability.overallAbility.count;
        double m = T/(T - t);

        // Calculate the s function
        double s = ComputeS(m, nAdministered, nMinRequired, nMaxRequired);

        // Update the content value of this item
        return priorityWeight * s;            
    }

    private double GetInformationForFeature(String sFeature, Blueprint bp, StudentAbility ability)
    {
    	double fInformation = 0F;    	
    	IRTMeasures rcMeasures = ability.getRCAbility(sFeature);
    	if (rcMeasures == null)
    		return fInformation;
		ReportingCategory rc = bp.getReportingCategory(sFeature);     		
		if (rc == null)
    		return fInformation;
    	fInformation = GetInformation(rcMeasures);
    	return rc.weight *
    				ComputeHK(rc.hweightBeforeTargetMet, rc.hweightAfterTargetMet, 
    							fInformation, rcMeasures.Information, rc.precisionTarget);
    }
 	
 	private double GetInformation(IRTMeasures irtMeasures)
    {    		
        double fInformation = 0F;        
        Iterator<Dimension> itDimension = dimensions.iterator(); 
    	while(itDimension.hasNext())
    	{
    		Dimension dim = itDimension.next();
        	if (hasDimensions && dim.isOverall) // TODO check that hasDimension - false???
	    		continue ;
        	
        	if (dim.irtModelInstance.measurementModel == IRTModel.ModelType.IRTPCL || 
        			dim.irtModelInstance.measurementModel == IRTModel.ModelType.IRTGPC)
        	{
        		fInformation += GaussHermiteQuadrature.Integrate(
        			new GaussianFunctionIRT(dim.irtModelInstance, irtMeasures.Theta, irtMeasures.StandardError));
        	}
        	else 
        	{
        		fInformation += dim.irtModelInstance.Information(irtMeasures.Theta);
        	}
        }        
    	return fInformation;    		
    }

    /**
     * S function for adaptive control
     * @param m
     * @param nAdministered
     * @param nMinRequired
     * @param nMaxRequired
     * @return
     */
    private double ComputeS(double m, int nAdministered, int nMinRequired, int nMaxRequired)
    {
        return (nAdministered <= nMinRequired && nMinRequired > 0) ?
                    m * (2F - (nAdministered / (double)nMinRequired)) :
                   ((nAdministered < nMaxRequired) ?
                        (1F - (nAdministered - nMinRequired) / (double)(nMaxRequired - nMinRequired)) :
                              (double)(nMaxRequired - nAdministered) - 1F);   
    }

    /**
     * Hk function for reporting category weighting
     * @param ck
     * @param dk
     * @param fCategoryInformation
     * @param fCategoryOverallInformation
     * @param fCategoryRequiredScore
     * @return
     */
    private double ComputeHK(double ck, double dk, double fCategoryInformation, double fCategoryOverallInformation, double fCategoryRequiredScore)
    {
        return fCategoryOverallInformation < fCategoryRequiredScore ? ck * fCategoryInformation : dk * fCategoryInformation;
    }

    /**
     * H0 function for overall information weighting
     * @param a
     * @param b
     * @param fItemInformation
     * @param fOverallInformation
     * @param fRequiredScore
     * @return
     */
    private double ComputeH0(double a, double b, double fItemInformation, double fOverallInformation, double fRequiredScore)
    {
        return fOverallInformation < fRequiredScore ? a*fItemInformation : b*fItemInformation;
    }
    /// <summary>
    /// Compute the blueprint satisfaction metric for this itemgroup
    /// </summary>
    /// <param name="bp"></param>
    public double computeBPMetric(Blueprint bp)
    {
        // FUNCTION CASES
        // Met or exceeded maximum allowed
        // Below minimum required
        // Between min and max required

        BpMetric metric = contentLevelCollection.computeBpMetric(bp, true);
        //NOTE: this is different from how the group's selectionMetric is set.  For the group,
        //  the selectionMetric is always set and raw is only set if there are no CLs.
        //  For the item, it's the opposite.  Leaving as-is pending further investigation.
        rawBpMetric = metric.Metric;

        if (metric.hasContentLevels)
            selectionMetric = rawBpMetric;
        
        return rawBpMetric;
    }
    /// <summary>
    /// Normalize the ability match WRT RC's.  Only used with new alg. Blueprint metric still undefined
    /// </summary>
    /// <param name="abilityMatch"></param>
    /// <param name="bpWeight"></param>
    public void SetRCAbilityMetric(double minAbility, double maxAbility)
    {
        // where maxability effectively == minability, all item abilities = 1
        // just as where bpmax == bpmin all item bpmetrics = 1
        if (Math.abs(maxAbility - minAbility) < .001)
            rcAbilityMetric = 1.0;
        else
            rcAbilityMetric = (rcAbilityMatch - minAbility) / (maxAbility - minAbility);
    }
    /// <summary>
    /// Normalize bp metric and set linear combination of final selection metric
    /// This is used to sort items within the final selected group for the purposes of
    /// pruning for strict maxes.
    /// </summary>
    /// <param name="bpWeight"></param>
    /// <param name="minBpMetric"></param>
    /// <param name="maxBpMetric"></param>
    public void setSelectionMetric(double bpWeight, double minBpMetric, double maxBpMetric, double abilityWeight, Double rcAbilityWeight)
    {
        if (Math.abs(maxBpMetric - minBpMetric) < .001)
            bpMetric = 1.0;
        else
            bpMetric = (rawBpMetric - minBpMetric) / (maxBpMetric - minBpMetric);
        
        selectionMetric = (bpWeight * bpMetric) + (abilityWeight * abilityMetric) + (rcAbilityWeight * rcAbilityMetric);
    }
    // New variant for AIROnline2013: on 1.0 less then for AIROnline2012
    // / <summary>
    // / Normalize the ability match. Blueprint metric still undefined
    // / </summary>
    // / <param name="abilityMatch"></param>
    // / <param name="bpWeight"></param>
    @Override
    public void setAbilityMetric (double minAbility, double maxAbility)
    {
      if (Math.abs (maxAbility - minAbility) < .001)
        abilityMetric = 1.0;
      else
        abilityMetric = (abilityMatch - minAbility) / (maxAbility - minAbility);
    }


}
