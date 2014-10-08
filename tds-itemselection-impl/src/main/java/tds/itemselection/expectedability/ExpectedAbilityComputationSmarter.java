/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.expectedability;

import java.util.List;

import tds.itemselection.base.Dimension;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.ReportingCategory;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.CsetGroup;

public class ExpectedAbilityComputationSmarter extends ExpectedInfoComputation {

	public ExpectedAbilityComputationSmarter() {
		super();
	}

	// / <summary>
	// / Computes expected info for a single group using the new algorithm, both
	// overall
	// / and at the RC-level.
	// / </summary>
	// / <param name="bp"></param>
	// / <param name="group"></param>
	@Override
	public void ComputeExpectedInfo(Blueprint bp, CsetGroup group) {
		double sumOverall = 0.0;
		double sumRc = 0.0;

		List<TestItem> activeIncluded = group.getActive();
		for (TestItem itm : activeIncluded) {
			if (itm instanceof CSetItem) {
				CSetItem item = (CSetItem) itm;
				// sets abilityMatch and rcAbilityMatch on the item CSetItem
				ComputeExpectedInfo(bp, item);

				// sum over item group; will divide by # items in the group to
				// get group-level ability match
				sumOverall += item.abilityMatch;
				sumRc += item.rcAbilityMatch;

				// the following for normalization
				SetMinMaxItemAbility(item.abilityMatch);
				SetMinMaxRCItemAbility(item.rcAbilityMatch);
			}
		}

		group.abilityMatch = sumOverall / activeIncluded.size();
		group.rcAbilityMatch = sumRc / activeIncluded.size();
	}

	// / <summary>
	// / Computes expected info for a single item. Factored out for use in the
	// bp-match routine
	// / to break ties.
	// / </summary>
	// / <param name="bp"></param>
	// / <param name="item"></param>
	public void ComputeExpectedInfo(Blueprint bp, CSetItem item) {
		// already calc'd. This is something that doesn't change for a cset
		// item,
		// so don't need to calculate it again. May have been used to break a
		// tie
		// in the bp match.
		// TODO
		// if (item.abilityMatchCalculated)
		// return;

		double overallInfo = 0.0;
		double sumRcInfo = 0.0;

		if (bp.abilityWeight != 0) // no need to calc if we're just going to
									// multiply by 0
		{
			for (Dimension dim : item.dimensions) {
				overallInfo += dim.CalculateExpectedInformation(bp.theta,
						bp.standardError);
			}
			// TODO
			// overallInfo *= bp.PrecisionTargetMet ?
			// bp.precisionTargetMetWeight : bp.precisionTargetNotMetWeight; //
			// h_0
		}

		if (bp.rcAbilityWeight != 0) {
			for (String cl : item.contentLevels) {
				ReportingCategory rc = bp.getReportingCategory(cl);
				if (rc == null)
					continue;

				for (Dimension dim : item.dimensions) {
					sumRcInfo += dim.CalculateExpectedInformation(
							rc.getTheta(), rc.standardError);
				}
				// TODO
				// sumRcInfo *= rc.PrecisionTargetMet ?
				// rc.precisionTargetMetWeight : rc.precisionTargetNotMetWeight;
				// // h_k
				sumRcInfo *= rc.abilityWeight; // q_k
			}
		}

		item.abilityMatch = overallInfo; // overall abilityWeight will be
											// applied when normalizing.
		item.rcAbilityMatch = sumRcInfo; // rcAbilityWeight will be applied when
											// normalizing.
		// TODO
		// item.abilityMatchCalculated = true;
	}
}
