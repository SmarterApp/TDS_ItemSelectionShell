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
 * IRT Partial credit model
 * @author aphilip
 *
 */
public class IRTModelPCL extends IRTModel 
{
	/**
	 * A Parameter
	 */
	protected Double _paramA;
	
	/**
	 * List of B parameters 
	 */
    protected List<Double> _paramB;
    
    /**
     * D Times A
     */
    protected Double _paramDA;

    /**
	 * Constructor
	 * @param paramA
	 * @param bVector
	 * @param paramC
	 */
    public IRTModelPCL(Double paramA, List<Double> bVector, Double paramC )
    {
       super(ModelType.IRTPCL);
        _paramA = 1D;
        _paramB = bVector;
        _paramDA = 1F * _paramA;
        // paramC is 0 for IRTPCL and IRTGPC models
    }

    /**
     * Probability
     */
    public double Probability(double score, double theta)
    {
    	double[] parameterSums = new double[_paramB.size() + 1];
        parameterSums[0] = 0D;
        for (int i = 1; i <= _paramB.size(); i++)
            parameterSums[i] = parameterSums[i - 1] + _paramB.get(i - 1);

        int s = (int)Math.floor(score);
        double den = 1D;
        for (int i = 1; i <= _paramB.size(); i++)
            den += Math.exp(_paramDA * (i * theta - parameterSums[i]));
        return Math.exp(_paramDA * (score * theta - parameterSums[s])) / den;
    }

    /**
     * First derivative
     */
    public double D1LnlWrtTheta(double score, double theta)
    {
    	double bsum = 0D;
    	double eSum = 0D;
    	double emSum = 0D;
    	double e;
        for (int m = 1; m <= _paramB.size(); m++)
        {
            bsum += (theta - _paramB.get(m - 1));
            e = Math.exp(_paramDA * bsum);
            eSum += e;
            emSum += _paramDA * e * m;
        }
        return (_paramDA * score * (1 + eSum) - emSum) / (1 + eSum);
    }

    /**
     * Second derivative
     */
    public double D2LnlWrtTheta(double score, double theta)
    {
    	double bsum = 0D;
    	double eSum = 0D;
    	double emSum = 0D;
    	double emmSum = 0D;
        for (int m = 1; m <= _paramB.size(); m++)
        {
            bsum += theta - _paramB.get(m - 1);
            double e = Math.exp(_paramDA * bsum);
            eSum += e;
            emSum += _paramDA * m * e;
            emmSum += _paramDA * _paramDA * m * m * e;
        }
        return (emSum * emSum - (1 + eSum) * emmSum) / Math.pow(1 + eSum, 2.0);
    }

    /**
     * Information 
     */
    public double Information(double theta)
    {
        double[] parameterSums = new double[_paramB.size() + 1];
        parameterSums[0] = 0D;
        for (int i = 1; i <= _paramB.size(); i++)
            parameterSums[i] = parameterSums[i - 1] + _paramB.get(i - 1);
        double den = 1D;
        double sum1 = 0.0;
        double sum2 = 0.0;
        for (int i = 1; i <= _paramB.size(); i++)
        {
        	double exp = Math.exp(_paramDA * (i * theta - parameterSums[i]));
            den += exp;
            sum1 += i * exp;
            sum2 += i * i * exp;
        }

        sum1 = sum1 / den;
        return _paramDA * _paramDA * (sum2 / den - sum1 * sum1);
    }
    
    /**
     * Difficulty
     */
    public double Difficulty()
    {
    	int nCount = _paramB.size();
    	double fSum = 0D;
    	for(int i=0; i < nCount; ++i)
    		fSum += _paramB.get(i);
        return fSum / nCount;        
    }

    /**
     * Expected score
     */
    public double ExpectedScore(double theta)
    {
    	double expectedScore = 0.0;
        for (int score = 1; score <= _paramB.size(); score++)
            expectedScore += score * Probability((double)score, theta);
        return expectedScore;
    }
}
