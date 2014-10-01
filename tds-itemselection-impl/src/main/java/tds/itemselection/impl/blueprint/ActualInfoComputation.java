/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import tds.itemselection.api.IBpInfoContainer;
import tds.itemselection.base.Dimension;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.math.AAMath;

public class ActualInfoComputation  {
    public void compute(IBpInfoContainer bpComponent, ItemResponse response)
    {
        for (Dimension dim : response.getBaseItem().dimensions)
        {
            // if this is a multi-dimensional item, don't count the overall dim, just the components
            if (dim.isOverall && response.getBaseItem().hasDimensions)
                continue;
            double info = bpComponent.getInfo();
            double theta = bpComponent.getTheta();
            double se = bpComponent.getStandartError();
            double d1 = dim.irtModelInstance.D1LnlWrtTheta(response.getDimensionScores().get(dim.IRTModelName), theta);
            bpComponent.setInfo(info + (-1 * dim.irtModelInstance.D2LnlWrtTheta(response.getDimensionScores().get(dim.IRTModelName), theta)));
            bpComponent.setTheta(theta - (d1 / (-1 *info)));
        }
        bpComponent.setStandartError( AAMath.SEfromInfo(bpComponent.getInfo()));
    }
}
