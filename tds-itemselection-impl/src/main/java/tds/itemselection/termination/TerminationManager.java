/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.termination;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.impl.blueprint.Blueprint;

// for AdaptiveSelector2013
public class TerminationManager {
	
	private static Logger  _logger  = LoggerFactory.getLogger (TerminationManager.class);

    private Blueprint bp;
    
    public boolean TerminationFlagsSet;
    public String SegmentCompleteReason;
    private List<TerminationCondition> termConditionsFlagged;
    private MinCountTermCond minCountTermCond;

    public TerminationManager(Blueprint bp)
    {
        this.bp = bp;
        SegmentCompleteReason = null;

        // we'll need to execute this up front, because if any flags are set, this
        //  is required to be satisfied as well.  Keep a reference so that if the min count flag is set, 
        //  the condition is not evaluated more than once when we iterate through the list below.
        minCountTermCond = new MinCountTermCond(bp);

        termConditionsFlagged = new ArrayList<TerminationCondition>();
        if (bp.terminateBasedOnCount)
            termConditionsFlagged.add(minCountTermCond);
        if (bp.terminateBasedOnOverallInformation)
            termConditionsFlagged.add(new PrecisionTargetOverallTermCond(bp));
        if (bp.terminateBasedOnReportingCategoryInformation)
            termConditionsFlagged.add(new PrecisionTargetAllRCsTermCond(bp));
        if(bp.terminateBasedOnScoreTooClose)
            termConditionsFlagged.add(new TooCloseTermCond(bp));

        TerminationFlagsSet = termConditionsFlagged.size() > 0;
    }
    
    /// <summary>
    /// Evaluates configured termination conditions.
    /// MIN must be met before terminating, even if a precision targets have been met.
    /// Note that it's possible to terminate before administering min FT items, but
    /// we'll leave it up the content creators to not set the MIN low enough for
    /// that to happen.  We can add a validation check when loading the config
    /// if necessary to make sure that this doesn't occur.
    /// </summary>
    /// <returns>true if the segment should be terminated</returns>
    public boolean IsSegmentComplete()
    {
        SegmentCompleteReason = null;

        // no termination flags have been set; don't need to go any further
        if (!TerminationFlagsSet)
            return false;

        // at least 1 flag is set; evaluate...
        // if any flags are set, then we must always meet min; if not, segment is not satisfied
        if(!minCountTermCond.isConditionSatisfied())
            return false;

        for (TerminationCondition cond : termConditionsFlagged)
        {
            if (bp.terminateBaseOnFlagsAnd && !cond.isConditionSatisfied())
                return false;
            else if (!bp.terminateBaseOnFlagsAnd && cond.isConditionSatisfied())
            {
                SegmentCompleteReason = cond.Description;
                return true;
            }
        }

        // If AND, we did not short circuit
        // If OR, we did not satisfy any single condition
        // So the segment is either not satisfied, or all conditions are satisfied with an AND.
        boolean satisfied = true;
        for(TerminationCondition tc: termConditionsFlagged)
        {
        	satisfied &= tc.isConditionSatisfied();
        }
        satisfied &= bp.terminateBaseOnFlagsAnd;
        if (satisfied)
            SegmentCompleteReason = "CONFIG";

        return satisfied;
    }
}

