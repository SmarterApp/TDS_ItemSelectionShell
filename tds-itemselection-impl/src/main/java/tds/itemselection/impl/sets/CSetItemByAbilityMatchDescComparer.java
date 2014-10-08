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

public class CSetItemByAbilityMatchDescComparer implements Comparator<CSetItem> {

	@Override
	public int compare(CSetItem o1, CSetItem o2) {
        int result = (new Double (o2.abilityMatch)).compareTo(o1.abilityMatch);
        if (result == 0)
            result = (new Double (o2.rcAbilityMatch)).compareTo(o1.rcAbilityMatch);
        return result;
	}

}
