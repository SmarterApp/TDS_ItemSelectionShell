/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.bpmatchcomputation;

import java.util.Random;

import tds.itemselection.impl.blueprint.BpMetric;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.impl.sets.BlueprintEnabledCsetFactory;

public class BP1 extends BlueprintMatchComputation{

	public BP1(Random rand) {
		super(rand);
	}

	@Override
	protected void CalculateBpMatchForGroup(BlueprintEnabledCsetFactory csetFactory,
			CsetGroup group) {
        // FUNCTION CASES
        // Met or exceeded maximum allowed
        // Below minimum required
        // Between min and max required

        // NOTE that this computation DOES NOT use the number of items in each contentlevel, only that it can satisfy to some degree
        // That is because some groups may be overpopulated and are pruned at the end

		// populate content levels
		group.populateContentLevels();
        BpMetric metric = group.contentLevels.computeBpMetric(csetFactory.getBp(), true);
       
        // Use selectionMetric to store value to leverage the CompareTo method
        if (metric.hasContentLevels ())
        {
            group.metricComputed = true;
            group.rawBpMetric = group.selectionMetric = metric.Metric;
        }
        else
        {
            // no content levels found, this group can contribute nothing to blueprint satisfaction
            group.metricComputed = false;
            group.selectionMetric = metric.Metric;
        }		
	}
}
