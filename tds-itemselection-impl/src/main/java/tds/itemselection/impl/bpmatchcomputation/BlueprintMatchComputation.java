/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.bpmatchcomputation;

import java.util.Collection;
import java.util.Random;

import TDS.Shared.Exceptions.ReturnStatusException;

import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.impl.sets.BlueprintEnabledCsetFactory;

public abstract class BlueprintMatchComputation {

    protected Random rnd ;

    public BlueprintMatchComputation(Random rand)
    {
        this.rnd = rand;
    }

    /// <summary>
    /// For all groups in the collection, calculates bp-match for each item in the group 
    /// and sets the tie-breaker value
    /// </summary>
    /// <param name="csetFactory"></param>
    /// <param name="groups"></param>
    /// <param name="rand"></param>
    public void execute(BlueprintEnabledCsetFactory csetFactory, Collection<CsetGroup> groups) throws ReturnStatusException
    {
        for (CsetGroup group : groups)
            execute(csetFactory, group);
    }

    /// <summary>
    /// Calculates bp-match for each item in the group and sets the tie-breaker value
    /// </summary>
    /// <param name="csetFactory"></param>
    /// <param name="group"></param>
    public void execute(BlueprintEnabledCsetFactory csetFactory, CsetGroup group) throws ReturnStatusException
    {
        CalculateBpMatchForGroup(csetFactory, group);
        SetBpTieBreakForGroup(csetFactory, group);
    }

    /// <summary>
    /// Performs bp-match computation on the CSETGroup given the Blueprint.
    /// Will operate on the CSETGroup that's passed in, modifying it's data.
    /// </summary>
    /// <param name="bp">The blueprint to match against</param>
    /// <param name="group">The CSETGroup to calculate values for.  Will update this group's internal members.</param>
    protected abstract void CalculateBpMatchForGroup(BlueprintEnabledCsetFactory csetFactory, CsetGroup group) throws ReturnStatusException;

    /// <summary>
    /// Sets the tie break for groups that have the same bp match value.
    /// </summary>
    /// <param name="csetFactory"></param>
    /// <param name="group"></param>
    protected void SetBpTieBreakForGroup(BlueprintEnabledCsetFactory csetFactory, CsetGroup group)
    {
        if (csetFactory.getBp().cset1Order.equalsIgnoreCase("ABILITY"))
        {
            group.computeAbilityDiff(csetFactory.getBp().theta);
            group.setBpJitter(group.irtDiff);
        }
        else
        	group.setBpJitter(rnd.nextDouble());
    }
}

