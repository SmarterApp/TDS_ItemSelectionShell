/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.sets;

import java.util.Comparator;

public class CSetItemByRequiredSelectionMetricDescComparer implements Comparator<CSetItem>{
	
 
	@Override
	public int compare(CSetItem o1, CSetItem o2) {
		int result = 1;
		if(o1.isRequired && o2.isRequired())
		{
			Double O1 = new Double(o1.selectionMetric);
			Double O2 = new Double(o2.selectionMetric);
			result = O1.compareTo(O2);
		}
		return result;
	}
    
}
