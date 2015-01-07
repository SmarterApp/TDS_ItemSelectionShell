/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemgroupselection.measurementmodels;

import java.util.ArrayList;

import org.junit.Test;

public class IRTModelTest
{

  @Test (expected = Exception.class)
  public void test_CreateModel_UnknownModel_ThrowsException () throws Exception {
    double paramA = 0.02;
    double paramB = 0.05;
    double paramC = 0.2;
    ArrayList<Double> paramBList = new ArrayList<Double> ();
    paramBList.add (paramB);
    IRTModel.CreateModel ("IRTL", paramA, paramBList, paramC);
  }

  @Test
  public void testCreateModel () throws Exception {
    double paramA = 0.02;
    double paramB = 0.05;
    double paramC = 0.2;
    ArrayList<Double> paramBList = new ArrayList<Double> ();
    paramBList.add (paramB);
    assert (IRTModel.CreateModel ("IRT3PL", paramA, paramBList, paramC) instanceof IRTModel3pl);
    assert (IRTModel.CreateModel ("IRT3PLN", paramA, paramBList, paramC) instanceof IRTModel3pln);
    assert (IRTModel.CreateModel ("IRTGPC", paramA, paramBList, paramC) instanceof IRTModelGPC);
    assert (IRTModel.CreateModel ("IRTPCL", paramA, paramBList, paramC) instanceof IRTModelPCL);
  }
}
