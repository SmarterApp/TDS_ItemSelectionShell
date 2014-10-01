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

import java.util.List;

/**
 * Place holder class for no name model
 * @author aphilip
 *
 */
public class IRTModelRaw extends IRTModel 
{
	/**
	 * Constructor
	 * @param paramA
	 * @param bVector
	 * @param paramC
	 */
    public IRTModelRaw(Double paramA, List<Double> bVector, Double paramC) 
    {
    	super(ModelType.RAW);
    }

    /**
     * Probability
     */
    public double Probability(double score, double theta)
    {
        return 0D;
    }

    /**
     * First derivative
     */
    public double D1LnlWrtTheta(double score, double theta)
    {
        return 0D;
    }

    /**
     * Second derivative
     */
    public double D2LnlWrtTheta(double score, double theta)
    {
        return 0D;
    }

    /**
     * Information 
     */
    public double Information(double theta)
    {
        return 0D;
    }

    /**
     * Difficulty
     */
    public double Difficulty()
    {
        return 0D;
    }

    public double ExpectedScore(double theta)
    {
        return 0D;
    }
}
