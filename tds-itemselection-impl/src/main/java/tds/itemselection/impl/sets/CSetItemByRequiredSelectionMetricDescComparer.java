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
	  
	  Boolean bO1 = new Boolean(o1.isRequired);
	  Boolean bO2 = new Boolean(o2.isRequired);
	  int result = bO2.compareTo(bO1);
	  if(result == 0)
		{
			Double O1 = new Double(o1.selectionMetric);
			Double O2 = new Double(o2.selectionMetric);
			result = O2.compareTo(O1);
		}
		return result;
	}
    
}
