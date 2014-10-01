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

//for AdaptiveSelector2013
public abstract class TerminationCondition {
	
	private static Logger  _logger  = LoggerFactory.getLogger (TerminationCondition.class);

    protected Blueprint bp;
    private Boolean satisfied;

    public TerminationCondition(Blueprint bp)
    {
        this.bp = bp;
        satisfied = null;
    }

    public boolean IsSatisfied()
    {
        {
            if (satisfied == null)
                satisfied = isConditionSatisfied();
            return satisfied;
        }
    }

    protected abstract boolean isConditionSatisfied();
    
    public String Description;

	public Blueprint getBp() {
		return bp;
	}

	public void setBp(Blueprint bp) {
		this.bp = bp;
	}

	public String getDescription() {
		return Description;
	}

}
