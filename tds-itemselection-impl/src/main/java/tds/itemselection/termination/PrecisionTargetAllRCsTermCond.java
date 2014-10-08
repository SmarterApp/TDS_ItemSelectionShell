/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.termination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.ReportingCategory;

//for AdaptiveSelector2013
public class PrecisionTargetAllRCsTermCond extends TerminationCondition {
	
	private static Logger  _logger  = LoggerFactory.getLogger (PrecisionTargetAllRCsTermCond.class);

	public PrecisionTargetAllRCsTermCond(Blueprint bp) {
		super(bp);
	}

	@Override
	protected boolean isConditionSatisfied() {
		boolean met = true;
			for(ReportingCategory rc: bp.getReportingCategories())
			{
				met &= rc.getPrecisionTargetMet();
			}

		return met;
	}
	@Override
	public String getDescription() {
		return "RC PRECISION";
	}
}
