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

public class BP1_Experimental extends BlueprintMatchComputation {

	public BP1_Experimental(Random rand) {
		super(rand);
	}

	@Override
	protected void CalculateBpMatchForGroup(BlueprintEnabledCsetFactory csetFactory,
			CsetGroup group) {
		// populate content levels
        group.populateContentLevels();
        BpMetric metric = group.contentLevels.computeBpMetric(csetFactory.getBp(), false);

        // Use selectionMetric to store value to leverage the CompareTo method
        if (metric.hasContentLevels ())
        {
            group.metricComputed = true;
            group.rawBpMetric = group.selectionMetric = metric.Sum; //note: sum only; not metric
        }
        else
        {
            // no content levels found, this group can contribute nothing to blueprint satisfaction
            group.metricComputed = false;
            group.selectionMetric = metric.Metric;
        }

		
	}

}
