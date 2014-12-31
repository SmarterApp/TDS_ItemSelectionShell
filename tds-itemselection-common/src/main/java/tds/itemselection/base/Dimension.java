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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemgroupselection.measurementmodels.IRTModel;
import tds.itemgroupselection.measurementmodels.IRTModel3pl;
import tds.itemgroupselection.measurementmodels.IRTModelGPC;

/**
 * Class representing IRT parameters associated with item measurement dimension
 * @author aphilip
 */
public class Dimension 
{
	public static Double MIN_B_VALUE = -9999.0; 
	 
	private static Logger  _logger  = LoggerFactory.getLogger (Dimension.class);

	/**
	 * IRTModel for this dimension
	 */
    public IRTModel irtModelInstance = null;
    
    private IRTModel expectedInfoIRTModel;
    
    public IRTModel getExpectedInfoIRTModel() {
		return expectedInfoIRTModel;
	}

	public void setExpectedInfoIRTModel(IRTModel expectedInfoIRTModel) {
		this.expectedInfoIRTModel = expectedInfoIRTModel;
	}

	public String name = null;
	
	/**
	 * IRT model name  
	 */
    public String IRTModelName;
    
    /**
     * IRT a parameter 
     */
    public Double ParamA;
    
    /**
     * List of item difficulty step values
     */
    public List<Double> bVector = new ArrayList<Double>();
    
    /**
     * Parameter c if applicable
     */
    public Double ParamC;
    
    /**
     * Flag to indicate whether this dimension is overall
     */
    public boolean isOverall = true;
    
    public int getScorePoints()
    {
    	return bVector.size();
    }
    
	public double averageB;

    public double getAverageB() {
		return averageB;
	}

    public void setAverageB(double averageB) {
		this.averageB = averageB;   
    }
    
	public void updateAverageB() {
        double sum = 0.0;
        if(this.bVector != null)
        {
	        for(int i = 0; i < this.bVector.size(); i++)
	        {
	        	sum += this.bVector.get(i);
	        }
	        sum /= this.bVector.size();
        }       
        averageB = ((this.bVector == null || this.bVector.isEmpty())? MIN_B_VALUE : (this.bVector.size() == 1 ? this.bVector.get(0) : sum));
	}
   
    /**
     * Add dimension entry collected from the table
     * @param irtModel
     * @param paramNum
     * @param sParamName
     * @param fParamValue
     */
    public void InitializeDimensionEntry(String irtModel, int paramNum, String sParamName, Double fParamValue)
    {   
    	IRTModelName = irtModel;
    	ParamArray[paramNum] = fParamValue;
    }
    
    /**
     * Initialize the IRT model for this dimension
     * @throws Exception
     */
    public void initializeIRT() throws Exception
    {    	
        IRTModel.ModelType enmModel = IRTModel.ModelType.valueOf(IRTModelName.toUpperCase());
        
        bVector = new ArrayList<Double>();
        if (enmModel == IRTModel.ModelType.IRT3PL || enmModel == IRTModel.ModelType.IRT3PLN)
        {
        	ParamA = ParamArray[0];
        	bVector.add(ParamArray[1]);
        	ParamC = ParamArray[2];        	
        }
        else if (enmModel == IRTModel.ModelType.IRTPCL || enmModel == IRTModel.ModelType.IRTGPC)
        {
        	int k = 0;
        	if (enmModel == IRTModel.ModelType.IRTGPC)
        	{
        		ParamA = ParamArray[0];
        		k = 1;
        	}
        	while (k < 10 && ParamArray[k] != -1)
            {
            	bVector.add(ParamArray[k]);
            	k++;
            }
        }
        irtModelInstance = IRTModel.CreateModel(enmModel, ParamA, bVector, ParamC);
        updateAverageB();        
 		setExpectedInfoIRTModel(IRTModel.CreateModel(enmModel, 1., bVector, ParamC));
     }
    
    /*
     * This is used to parse and get the dimension data from database - (Maximum 10 values)
     */
    private double[] ParamArray = new double[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

	public double CalculateExpectedInformation(double theta, double se)
    {
        if (this.bVector.size() == 1)
            return this.expectedInfoIRTModel.Information(theta);
        else
        {
        	return this.expectedInfoIRTModel.ExpectedInformation(theta, se);       
        }
    }
    //
    public void dumpAA2()
    {
    	this.irtModelInstance.dumpAA2();
    	System.out.println (String.format ("ParamA %s", this.ParamA));
     	int i = 0;
    	for(Double d: this.bVector)
    	{
    		System.out.println ("bVector[" + i + " ] = "  + d);
    		i++;
    	}
       	System.out.println (String.format ("ParamC %s", this.ParamC));
    	if(this.irtModelInstance instanceof IRTModel3pl)
    	{
    		IRTModel3pl model = (IRTModel3pl)this.irtModelInstance;
    		model.dumpAA2();
    	}
    	if(this.irtModelInstance instanceof IRTModelGPC)//IRTGPC
    	{
    		IRTModelGPC model = (IRTModelGPC)this.irtModelInstance;
    		model.dumpAA2();
    	}
    }

}
