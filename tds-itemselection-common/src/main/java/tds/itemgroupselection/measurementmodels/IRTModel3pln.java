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
 * IRT 3 Parameter normalized(?) model
 * @author aphilip
 *
 */
public class IRTModel3pln extends IRTModel3pl 
{
	/**
	 * Constructor
	 * @param paramA
	 * @param bVector
	 * @param paramC
	 * @throws Exception
	 */
    public IRTModel3pln(Double paramA, List<Double> bVector, Double paramC) throws Exception
    {
        super(paramA, bVector, paramC);
        measurementModel = ModelType.IRT3PLN;
        _paramDA = 1.7*_paramA; // D = 1.7
    }
}
