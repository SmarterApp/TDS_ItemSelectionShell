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

/**
 * Wrapper around IRTModel, Mean and Std. Error so that integration 
 * can be performed using these measures 
 * @author aphilip
 *
 */
public class GaussianFunctionIRT implements IUnaryFunction
{
	/**
	 * IRT model which will be used to get information measure 
	 */
	private IRTModel Model;
	
	/**
	 * Mean value of Gaussian
	 */
	private double Mean ;
	
	/**
	 * Std. Error of Gaussian
	 */
	private double StandardDeviation ;
	
	/**
	 * Constructor expects all parameters
	 * @param irtModel
	 * @param fMean
	 * @param fStdDeviation
	 */
	public GaussianFunctionIRT(IRTModel irtModel, double fMean, double fStdDeviation)
	{
		Model = irtModel;
		Mean = fMean;
		StandardDeviation = fStdDeviation;
	}
	
	/**
	 * Client calls this function to calculate information around the mean 
	 * and the specified value of standard deviation
	 * @param fValue
	 * @return
	 */
	public double Apply(double fValue)
	{
		return Model.Information(Mean + fValue * StandardDeviation);
	}
}
