/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemgroupselection.measurementmodels;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class GaussianFunctionIRTTest
{
  @Test
  public void testApply () throws Exception {
    double fMean = 0D;
    double fStdDev = 0.2D;

    double paramA = 0.2;
    double paramB = 0.05;
    double paramC = 0.2;
    ArrayList<Double> paramBList = new ArrayList<Double> ();
    paramBList.add (paramB);
    IRTModel model = IRTModel.CreateModel ("IRT3PL", paramA, paramBList, paramC);
    GaussianFunctionIRT gfIRT = new GaussianFunctionIRT (model, fMean, fStdDev);
    double fValue = 0D;
    double fExpectedValue = 0.00665535210267177;
    double fErrorMargin = .00001;
    assertEquals (fExpectedValue, gfIRT.Apply (fValue), fErrorMargin);
  }
}
