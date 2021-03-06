/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
/**
 * (c) Copyright American Institutes for Research, unpublished work created 2008-2013
 *  All use, disclosure, and/or reproduction of this material is
 *  prohibited unless authorized in writing. All rights reserved.
 *
 *  Rights in this program belong to:
 *  American Institutes for Research.
 *  
 *  Code based on ScoringEngine C# project - Re-factored here 
*/
package tds.itemgroupselection.measurementmodels;

import java.util.ArrayList;
import java.util.List;

/**
 * Generalized Partial Credit IRT model 
 * @author aphilip
 *
 */
public class IRTModelGPC extends IRTModelPCL 
{
	/**
	 * Constructor
	 * @param paramA
	 * @param bVector
	 * @param paramC
	 */
    public IRTModelGPC(Double paramA, List<Double> bVector, Double paramC)
    {
        super(paramA, bVector, paramC);
        measurementModel = ModelType.IRTGPC;
        _paramA = paramA;
        _paramDA = 1.7 * _paramA;
    }
}
