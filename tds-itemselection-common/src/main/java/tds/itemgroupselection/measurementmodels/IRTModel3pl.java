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
 * IRT 3 parameter model
 * @author aphilip
 *
 */
public class IRTModel3pl extends IRTModel 
{
	/**
	 * A parameter
	 */
    protected Double _paramA;

    /**
     * B Parameter
     */
    protected Double _paramB;

    /**
     * C Parameter
     */
    protected Double _paramC;

    /**
     * D times A 
     */
    protected Double _paramDA; 

	/**
	 * Constructor
	 */
    protected IRTModel3pl()
    {            
    }

    /**
     * Constructor taking parameter
     * @param paramA
     * @param bVector
     * @param paramC
     * @throws Exception
     */
    public IRTModel3pl(Double paramA, List<Double> bVector, Double paramC) throws Exception
    {
        super(ModelType.IRT3PL);
        
        if (bVector.size() != 1)
            throw new Exception("Parameter b should be single valued");

        _paramA = paramA;
        _paramB = bVector.get(0);
        _paramC = paramC;
        _paramDA = 1F *_paramA; // D = 1
    }

    /**
     * Probability
     */
    public double Probability(double score, double theta)
    {
        double p = _paramC + (1.0 - _paramC) / (1 + Math.exp(-_paramDA * (theta - _paramB)));
        return Math.pow(p, score)*Math.pow(1.0 - p, 1.0 - score);
    }

    /**
     * First derivative
     */
    public double D1LnlWrtTheta(double score, double theta)
    {
        double kern = Math.exp(_paramDA * (_paramB - theta));
        return -((_paramDA*(1 + _paramC*kern - score - score * kern)) / ((1 + kern)*(1 + _paramC*kern)));        
    }

    /**
     * Second derivative
     */
    public double D2LnlWrtTheta(double score, double theta)
    {
        double kern = Math.exp(_paramDA * (_paramB - theta));
        return
            -((_paramDA * _paramDA * kern *
               (1 + _paramC *_paramC * kern * kern - _paramC * (2.0 * kern * (-1.0 + score) + score +  kern * kern *score)))/
                    (Math.pow(1 + kern, 2.0)*Math.pow(1 + _paramC*kern, 2.0)));
    }

    /**
     * Information value
     */
    public double Information(double theta)
    {
        double p, q, t;
        p = _paramC + (1.0 - _paramC) / (1 + Math.exp(-_paramDA * (theta - _paramB))); 
        q = 1.0 - p;
        t = (p - _paramC) / (1 - _paramC);
        return _paramDA * _paramDA * q * t * t / p;
    }

    /**
     * Difficulty
     */
    public double Difficulty()
    {
        return _paramB;
    }

    /**
     * Expected score
     */
    public double ExpectedScore(double theta)
    {
        return Probability(1.0, theta);
    }
 }
