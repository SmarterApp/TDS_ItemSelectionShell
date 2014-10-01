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
 *  Code based on AIROnline2012 C# project - Re-factored here 
 */
package tds.itemselection.base;

import java.util.Iterator;

/**
 * Helper class to accumulate certain relevant information for the opportunity
 * @author aphilip
 */
public class IRTMeasures 
{
	/**
	 * Theta value 
	 */
	public double Theta;
	
	/**
	 * Information 
	 */
	public double Information ;
	
	/**
	 * Standard error in theta measurement
	 */
	public double StandardError ;
	
	/**
	 * Second derivative sum so far
	 */
	public double D2LnlWrtThetaSum ;
	
	/**
	 * Number of items contributed to this measure
	 */
	public int count ;
	
	/**
	 * Constructor
	 */
	public IRTMeasures()
	{		
	}
	
	/**
	 * Constructor taking all parameters
	 * @param fTheta
	 * @param fInformation
	 * @param fStdError
	 * @param fD2
	 * @param nCount
	 */
	public IRTMeasures(double fTheta, double fInformation, double fStdError, double fD2, int nCount)
	{
		Theta = fTheta;
		Information = fInformation;
		StandardError = fStdError;
		D2LnlWrtThetaSum = fD2;
		count = nCount;
	}

	/**
	 * Copy the parameter from other IRTmeasures object
	 * @param other
	 */
	public IRTMeasures(IRTMeasures other)
	{
		Theta = other.Theta;
		Information = other.Information;
		StandardError = other.StandardError;
		D2LnlWrtThetaSum = other.D2LnlWrtThetaSum;
		count = other.count;
	}
	
	/**
	 * Update the measure using a new item and the score obtained by the student
	 * @param item
	 * @param fScore
	 */
	public void UpdateMeasures(TestItem item, double fScore)
	{
	    double fItemD1 = 0D;
	    double fItemD2 = 0D;
	    Iterator<Dimension> itDimension = item.dimensions.iterator(); 
		while(itDimension.hasNext())
		{
			Dimension dim = (Dimension) itDimension.next();
			// TODO hasDimensions is outer for Dimension?? 
	    	if (item.hasDimensions && dim.isOverall)
	    		continue ;
	    	fItemD1 += dim.irtModelInstance.D1LnlWrtTheta(fScore, Theta);
	    	fItemD2 += dim.irtModelInstance.D2LnlWrtTheta(fScore, Theta);
    	}
	    D2LnlWrtThetaSum += fItemD2;
	    Theta -= (fItemD1 / D2LnlWrtThetaSum);
	    Information = -1D * D2LnlWrtThetaSum;
	    if(Information > 0.)
	    	StandardError = 1D / Math.sqrt(Information);
	    
	    ++count;
	}
}
