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
 * Abstract interface to represent IRT Models 
 * @author aphilip
 * @author akulakov 
 * 
 */
public abstract class IRTModel 
{
	// Note valid values are checked in the TestCollection constructor.
	// TODO
    //private int numQuadraturePoints = String.IsNullOrEmpty(ConfigurationManager.AppSettings["NumQuadPointsForExpectedInfo"]) ? 5 : Int32.Parse(ConfigurationManager.AppSettings["NumQuadPointsForExpectedInfo"]);
	/**
	 * Number of the Quadrature Points For ExpectedInfo
	 * All models have this value of the quadrature points!
	 */
    private int numQuadraturePoints = 5;

	/**
	 * Possible IRT models 
	 * @author aphilip
	 */
	public enum ModelType { Unknown, IRT3PL, IRTPCL, RAW, IRT3PLN, IRTGPC};

	/**
	 * Measurement model name
	 */
	public ModelType measurementModel;

	/**
	 * Factory method to create an IRT Model object
	 * @param type
	 * @param paramA
	 * @param bVector
	 * @param paramC
	 * @return
	 * @throws Exception
	 */
	public static IRTModel CreateModel(ModelType type, Double paramA, List<Double> bVector, Double paramC) throws Exception
	{
		switch (type)
		{
			case IRT3PL:
				return new IRTModel3pl(paramA, bVector, paramC);
			case IRT3PLN:
				return new IRTModel3pln(paramA, bVector, paramC);
			case IRTPCL:
				return new IRTModelPCL(paramA, bVector, paramC);
			case IRTGPC:
				return new IRTModelGPC(paramA, bVector, paramC);
			case RAW:
				return new IRTModelRaw(paramA, bVector, paramC);
			default:
				throw new Exception("Undefined model type");
		}
	}
	
	/**
	 * 
	 * @param modelName
	 * @param paramA
	 * @param bVector
	 * @param paramC
	 * @return
	 * @throws Exception
	 */
	public static IRTModel CreateModel(String modelName, Double paramA, List<Double> bVector, Double paramC) throws Exception
	{
		return CreateModel(getTypeByName(modelName), paramA, bVector, paramC);
	}

	/**
	 * Constructor
	 */
	protected IRTModel()
	{            
	}

	/**
	 * Constructor taking model name 
	 * @param eModel
	 */
	protected IRTModel(ModelType eModel)
	{
		measurementModel = eModel;
	}

	/**
	 * Probability of getting the score based on specified theta
	 * @param score
	 * @param theta
	 * @return
	 */
	abstract public double Probability(double score, double theta);

	/**
	 * First derivative of log probability of score given theta 
	 * @param score
	 * @param theta
	 * @return
	 */
	abstract public double D1LnlWrtTheta(double score, double theta);

	/**
	 * Second derivative of log probability of score given theta 
	 * @param score
	 * @param theta
	 * @return
	 */
	abstract public double D2LnlWrtTheta(double score, double theta);

	/**
	 * Information based on theta 
	 * @param score
	 * @param theta
	 * @return
	 */
	abstract public double Information(double theta);

	/**
	 * Item difficulty
	 * @return
	 */
	abstract public double Difficulty();

	/**
	 * Expected score
	 * @param theta
	 * @return
	 */
	abstract public double ExpectedScore(double theta);
	/**
	 * 
	 * @param modelName
	 * @return
	 */
	protected static ModelType getTypeByName(String modelName)
	{
		if(modelName.equalsIgnoreCase("IRT3PL"))
			return ModelType.IRT3PL;
		else if(modelName.equalsIgnoreCase("IRT3PLN"))
			return ModelType.IRT3PLN;		
		else if(modelName.equalsIgnoreCase("IRTGPC"))
			return ModelType.IRTGPC;
		else if(modelName.equalsIgnoreCase("IRTPCL"))
			return ModelType.IRTPCL;
		else if(modelName.equalsIgnoreCase("RAW"))
			return ModelType.RAW;
		else 
			return ModelType.Unknown;
	}
	/**
	 *  Compute the information if the student's theta isn't known exactly, but is assumed normaly distributed with mean theta and standard deviation se.
	 * @param theta
	 * @param se
	 * @return
	 */
    public double ExpectedInformation(double theta, double se)
    {
    	GaussianFunctionIRT gauss = new GaussianFunctionIRT(this, theta, se);
    	return GaussHermiteQuadrature.Integrate(gauss);
    }

    // For debug	
	public void dumpAA2()
	{
		System.out.println("ModelType " + this.measurementModel.toString());
	}
}
