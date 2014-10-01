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

import java.util.ArrayList;
import java.util.Comparator;

import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.loader.StudentAbility;

/**
 * Class wrapping around a (possibly) pruned item group. Contains logic required for selection
 * and pruning. 
 * @author aphilip
 */
public class CSetItemGroup 
{
	/**
	 * Comparator for comparison based on content value (ascending)
	 */
	public static final Comparator<CSetItemGroup> ContentValueComparatorDesc 
    		= new Comparator<CSetItemGroup>() { public int compare(CSetItemGroup o1, CSetItemGroup o2) 
					 {return o1.contentValue.compareTo(o2.contentValue);}};

	/**
	 * Comparator for comparison based on reporting category information value (ascending)
	 */
	public static final Comparator<CSetItemGroup> RCInformationComparatorDesc 
	= new Comparator<CSetItemGroup>() { public int compare(CSetItemGroup o1, CSetItemGroup o2) 
			 {return o1.reportingCategoryInformationValue.compareTo(o2.reportingCategoryInformationValue);}};

	/**
	 * Comparator for comparison based on overall information value (ascending)
	 */
	public static final Comparator<CSetItemGroup> OverallInformationComparatorDesc 
	= new Comparator<CSetItemGroup>() { public int compare(CSetItemGroup o1, CSetItemGroup o2) 
			 {return o1.overallInformationValue.compareTo(o2.overallInformationValue);}};


    /**
     * Objective function allowing sorting based on weighted measures 
     * (Descending sort) - Highest valued measure on top
     * @author aphilip
     */			 
    public static class ObjectiveFunction implements Comparator<CSetItemGroup> 
    {
    	/**
    	 * Weight applied to Content Value (Blue print match)
    	 */
    	private double w2;
    	
    	/**
    	 * Weight applied to reporting category information measure 
    	 */
    	private double w1;
    	
    	/**
    	 * Weight applied to overall information measure
    	 */
    	private double w0;
    	
    	/**
    	 * Constructor takes the weights to be applied
    	 * @param p0
    	 * @param p1
    	 * @param p2
    	 */
    	public ObjectiveFunction(double p0, double p1, double p2)
    	{    		
    		w0 = p0;
    		w1 = p1;
    		w2 = p2;
    	}
    	
    	/**
    	 * Comparison method
    	 */
    	@Override
    	public int compare(CSetItemGroup o1, CSetItemGroup o2) 
    	{
    		// TODO AK: I think it is error. Needed w0*contentValue + w1*reportingCategoryInformationValue+w2*overallInformationValue
    		// and w2=1 for CSET1
    		Double d1 = w2 * o1.contentValue + w1 * o1.reportingCategoryInformationValue + w0 * o1.overallInformationValue;
    		Double d2 = w2 * o2.contentValue + w1 * o2.reportingCategoryInformationValue + w0 * o2.overallInformationValue;
    		return d2.compareTo(d1); // Descending sort
    	}    	
    }
    
	/**
	 * Base item group which this class is representing
	 */
    public ItemGroup baseGroup;

    /**
     * List of member items of this class
     */
    public ArrayList<CSetItem> items = new ArrayList<CSetItem>();

    /**
     * Content value associated with this group // TODO is it good default value?
     */
    public Double contentValue = 0.0; // Default value

    /**
     * Weighted information value associated with report category matches
     */
    public Double reportingCategoryInformationValue = 0.0; // Default value;

    /**
     * Weighted information for overall information 
     */
    public Double overallInformationValue = 0.0; // Default value;

    /**
     * Constructor 
     * @param itemGroup
     */
    public CSetItemGroup(ItemGroup itemGroup)
    {
        baseGroup = itemGroup;
        items.clear();
    }
    
    /**
     * Add the item to the item group if it is a candidate
     * @param item
     */
    public void AddItem(TestItem item)
    {
    	if (!item.isActive || item.isFieldTest)
    		return;
    	items.add(new CSetItem(item));
    }

    /**
     * Calculate the selection metric for the group as avarage of selection 
     * metrics for contained items
     * @param bp
     * @param ability
     */
    public void CalculateSelectionMetric(Blueprint bp, StudentAbility ability)
    {
        contentValue = 0D;
        reportingCategoryInformationValue = 0D;
        overallInformationValue = 0D;
        int nItems = items.size();
        for(int i=0; i < nItems; ++i)
        {
        	CSetItem cSetItem = items.get(i);
        	cSetItem.CalculateSelectionMetric(bp, ability);
            contentValue += cSetItem.ContentValue;
            reportingCategoryInformationValue += cSetItem.ReportingCategoryInformationValue;
            overallInformationValue += cSetItem.OverallInformationValue;
        }
        contentValue /= (double)nItems;
        reportingCategoryInformationValue /= (double)nItems;
        overallInformationValue /= (double)nItems;
    }

    /**
     * Normalize content value using given min and max
     * @param Min
     * @param Max
     */
    public void NormalizeContentValue(Double Min, Double Max)
    {
    	contentValue = (Min.compareTo(Max) == 0) ? 1D : (contentValue - Min) / (Max - Min);
    }

    /**
     * Normalize reporting category information value using given min and max
     * @param Min
     * @param Max
     */
    public void NormalizeReportingCategoryInformationValue(Double Min, Double Max)
    {
    	reportingCategoryInformationValue = (Min.compareTo(Max) == 0) ? 1D : (reportingCategoryInformationValue - Min) / (Max - Min);
    }

    /**
     * Normalize overall information value using given min and max
     * @param Min
     * @param Max
     */
    public void NormalizeOverallInformationValue(Double Min, Double Max)
    {
    	overallInformationValue = (Min.compareTo(Max) == 0) ? 1D : (overallInformationValue - Min) / (Max - Min);
    }
}
