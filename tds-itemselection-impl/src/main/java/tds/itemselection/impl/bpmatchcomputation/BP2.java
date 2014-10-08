/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.bpmatchcomputation;

import java.util.Collections;
import java.util.Random;

import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.ContentLevelCollection;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.Cset1Factory2013;
import tds.itemselection.impl.sets.CsetGroup;

public class BP2  extends BlueprintMatchComputation {

	public BP2(Random rand) {
		super(rand);
		}

	@Override
	protected void CalculateBpMatchForGroup(Cset1Factory2013 csetFactory,
			CsetGroup group) {
		
        double sum = 0;
        int maxitms = group.getMaxItems();
        int cnt = 0;
        int active = group.getActiveCount();

        // if this group makes the cut, then it will need the blueprint and panic weight to compute metrics on its items
        if (maxitms < active)
            sum = 0;        // this useless statement for debugging breakpoint

		for (TestItem itm : group.getItems()) {
			if (itm instanceof CSetItem) {
				CSetItem item = (CSetItem) itm;
				if (item.isActive) {
					item.computeBPMetric(csetFactory.getBp());
				}
			}
		}

        //  NOTE: commented-out Prune call below!
        if (maxitms < active)      // this sort needed only if we are selecting by maxitems
        {
        	Collections.sort (group.items);
            // Prune(active - maxitems)?;
        }
		for (TestItem itm : group.getItems()) {
			if (itm instanceof CsetItem) {
				CsetItem item = (CsetItem) itm;
				if (itm.isActive) {
					sum += item.rawBpMetric;
					++cnt;
					if (cnt >= maxitms)
						break;
				}
			}
		}
        // Use selectionMetric to store value to leverage the CompareTo method
        if (cnt > 0)
        {
            group.metricComputed = true;
            group.rawBpMetric = group.selectionMetric = sum / cnt;
        }
        else
        {
            // no content levels found, this group can contribute nothing to blueprint satisfaction
            group.metricComputed = false;
            group.selectionMetric = ContentLevelCollection.NO_CLs;
        }
        return;		
	}

}
