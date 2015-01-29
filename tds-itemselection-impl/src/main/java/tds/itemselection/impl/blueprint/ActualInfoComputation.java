/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.api.IBpInfoContainer;
import tds.itemselection.base.Dimension;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.math.AAMath;

public class ActualInfoComputation  {
    public void compute(IBpInfoContainer bpComponent, ItemResponse response) throws ReturnStatusException
    {
        for (Dimension dim : response.getBaseItem().dimensions)
        {
            // if this is a multi-dimensional item, don't count the overall dim, just the components
            if (dim.isOverall && response.getBaseItem().hasDimensions)
                continue;

            double d1 = dim.irtModelInstance.D1LnlWrtTheta(response.getDimensionScores().get((dim.name).toLowerCase()), bpComponent.getTheta());
            bpComponent.setInfo(bpComponent.getInfo() + (-1.0 * dim.irtModelInstance.D2LnlWrtTheta(response.getDimensionScores().get(dim.name), bpComponent.getTheta())));
            if (Double.isNaN(bpComponent.getInfo()))
                throw new ReturnStatusException(String.format("NaN encountered while"
                		+ " calculating actual info for: %s."
                		+ " Theta: %d", bpComponent.getName(), bpComponent.getTheta()));
            if(bpComponent.getInfo() < 0.25) // Bound the SE by 2.0
            	bpComponent.setInfo(0.25);
           
            bpComponent.setTheta(bpComponent.getTheta() - (d1 / (-1 * bpComponent.getInfo())));
            if(bpComponent.getTheta() < -4.0) // Keep theta between -4.0 and 4.0
            	bpComponent.setInfo(-4.0);
            	
            if(bpComponent.getTheta() > 4.0)
            	bpComponent.setInfo(4.0);
            	
       }
        
        //AM: this can't be calculated 1 response at a time, because info can't be expected to be > 0
        //  until at least a group has been processed.  Best to calculate this after all responses so far
        //  have been processed.
        // TODO
        // bpComponent.setStandartError( AAMath.SEfromInfo(bpComponent.getInfo()));
    }
     
    public void calculateSE(IBpInfoContainer bpComponent)
    {
        bpComponent.setStandartError(AAMath.SEfromInfo(bpComponent.getInfo()));
    }

}
