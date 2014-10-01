/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.expectedability;

import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.CsetGroup;

public abstract class ExpectedInfoComputation {
	
    public Double MinItemAbility;
    public Double MaxItemAbility;
    // these are only used for the new algorithm.  Will stay null for the current alg.
    public Double MinRCItemAbility;
    public Double MaxRCItemAbility;
    
    public Blueprint bp;

    protected ExpectedInfoComputation()
    {
        MinItemAbility = Double.POSITIVE_INFINITY;
        MaxItemAbility = Double.NEGATIVE_INFINITY;
        MinRCItemAbility = Double.POSITIVE_INFINITY;
        MaxRCItemAbility = Double.NEGATIVE_INFINITY;
    }

    protected void SetMinMaxItemAbility(double itemAbility)
    {
        MinItemAbility = Math.min(MinItemAbility, itemAbility);
        MaxItemAbility = Math.max(MaxItemAbility, itemAbility);
    }

    protected void SetMinMaxRCItemAbility(double itemAbility)
    {
        this.MinRCItemAbility = Math.min(this.MinRCItemAbility, itemAbility);
        this.MaxRCItemAbility = Math.max(this.MaxRCItemAbility, itemAbility);
    }

    /// <summary>
    /// calculate the expected ability for the group that's passed in
    /// </summary>
    /// <param name="bp"></param>
    /// <param name="group"></param>
    public abstract void ComputeExpectedInfo(Blueprint bp, CsetGroup group);
}
