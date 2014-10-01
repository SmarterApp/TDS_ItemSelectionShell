/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

import java.util.HashMap;





import tds.itemselection.base.IRTMeasures;
//import tds.itemgroupselection.itemgroupselection.Blueprint;
//import tds.itemgroupselection.itemgroupselection.IRTMeasures;
//import tds.itemgroupselection.itemgroupselection.ItemResponse;
//import tds.itemgroupselection.itemgroupselection.ReportingCategory;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.ReportingCategory;

public class StudentAbility {
	   /**
     * Count of matching blue print features for this student
     */
    private HashMap<String, Integer> _bpCount = new HashMap<String, Integer>(); 

	/**
	 * Initial ability of the student 
	 */
	private IRTMeasures _initialAbility = new IRTMeasures();
	
	/**
	 * Ability of the student in different reporting categories
	 */
    private HashMap<String, IRTMeasures> _rcAbility = new HashMap<String, IRTMeasures>();
    
	/**
	 * Overall theta (ability) of the student
	 */
	public IRTMeasures overallAbility = new IRTMeasures(); 
	
	/**
	 * Constructor 
	 */
    public StudentAbility()
    {
    }

    /**
     * Initialize the student ability
     * @param fTheta
     * @param fSE
     * @param fInformation
     */
    public void initialize(double fTheta, double fSE, double fInformation)
    {
    	_initialAbility = new IRTMeasures(fTheta, fSE, fInformation, 0D, 0);
    	overallAbility = new IRTMeasures(_initialAbility);
    }

    /**
     * Get the count of the specified blue print feature administered so far
     * @param bpName
     * @return
     */
    public int getAdministeredBPFeatureCount(String bpName)
    {
    	return (_bpCount.containsKey(bpName)) ? _bpCount.get(bpName) : 0;
    }

    /**
     * Get student ability in measures for the specified reporting category
     * @param rc
     * @return
     */
    public IRTMeasures getRCAbility(String rc)
    {            
        if (!_rcAbility.containsKey(rc))
        	_rcAbility.put(rc, new IRTMeasures(_initialAbility));  
        return _rcAbility.get(rc);
    }
    
    /**
     * Update student measures based on the student response for the item  
     * @param item
     * @param r
     */
    public void updateStudentMeasures(Blueprint bp, TestItem item, ItemResponse r)
    {
    	double fScore = (r.score == null || r.score < 0 )? 0D : r.score;
    	overallAbility.UpdateMeasures(item, fScore);    	
    	for(int i=0, nContentLevels = item.contentLevels.size() ; i < nContentLevels; ++i)
    		updateMeasure(bp, item.contentLevels.get(i), item, fScore);
		updateMeasure(bp, item.strandName, item, fScore);
    }
    
    /**
     * Update measures of a particular feature for this student
     * @param bp
     * @param sFeature
     * @param item
     * @param fScore
     */
    private void updateMeasure(Blueprint bp, String sFeature, TestItem item, double fScore)
    {
		ReportingCategory rc = bp.getReportingCategory(sFeature);
		if (rc != null)
		{
			if (!_rcAbility.containsKey(sFeature))
	            _rcAbility.put(sFeature, new IRTMeasures(_initialAbility));
	         _rcAbility.get(sFeature).UpdateMeasures(item, fScore);
		}
		// As per spec. everything is a blueprint feature  
		if (!_bpCount.containsKey(sFeature))
        	_bpCount.put(sFeature, 0);
        int nCurrentValue = _bpCount.get(sFeature);
        _bpCount.put(sFeature,  nCurrentValue + 1) ; // Increment the count
    }

}
