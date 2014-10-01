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

package tds.itemselection.impl.blueprint;

import java.sql.SQLException;

import tds.itemselection.api.IReportingCategory;
import tds.itemselection.impl.math.AAMath;
import AIR.Common.DB.results.DbResultRecord;


public class ReportingCategory extends BpElement implements IReportingCategory
{
	/**
	 * Ability weight for this reporting category (qk)
	 */
	public Double abilityWeight = 1.0;	// DEFAUL value
	
	/**
	 * H function weight before target is met (ck)
	 */
	public Double hweightBeforeTargetMet = 1.0;	// DEFAUL value
	
	/**
	 * H function weight after target is met (dk)
	 */
	public Double hweightAfterTargetMet = 1.0;	// DEFAUL value;
	
	/**
	 * Target information for this reporting category (tk) 
	 */
	public Double targetInformation = 0.0;	// DEFAUL value;
	
	// specific to examinee
	public double precisionTarget = Double.MAX_VALUE;
	
    // independent of examinee
    public double adaptiveCut ;  // the cut point for strands in adaptive ability match
    //TODO compare with targetInformation
    public double startInfo = 0.2;        // for adaptive ability match
    public double adaptiveWeight = 5.0;     // 
    public double startAbility;
    // specific to examinee
    public double standardError = AAMath.SEfromInfo(this.startInfo);
    public double theta;
    public double lambda = 0.00632;    // lambda is an intermediate value used for computing Phi*_kt
    public double minLambda = 0.00632;
    public double info;
    // The following are computed by the adaptive algorithm in determining best item to administer
    public double gamma;
    public double bstar;
    public double phistar;
    public double phidiff;

    // new for 2013
    //TODO compare with hweightBeforeTargetMet and hweightAfterTargetMet
    public double precisionTargetMetWeight;
    public double precisionTargetNotMetWeight;

    public int vectorIndex = -1;      // to be set by Blueprint when added to strands vector

	public double getStartAbility() {
		return startAbility;
	}

	public void setStartAbility(double startAbility) {
		this.startAbility = startAbility;
	}

	public Double getStandardError() {
		return standardError;
	}

	public void setStandardError(Double standardError) {
		this.standardError = standardError;
	}
	
	public Double getAdaptiveCut() {
		return adaptiveCut;
	}
	
	public void setAdaptiveCut(Double adaptiveCut) {
		this.adaptiveCut = adaptiveCut;
	}
	
	public Double getTheta() {
		return theta;
	}
	
	public void setTheta(Double theta) {
		this.theta = theta;
	}
	
	public Double getLambda() {
		return lambda;
	}
	
	public void setLambda(Double lambda) {
		this.lambda = lambda;
	}

	public Double getMinLambda() {
		return minLambda;
	}

	public void setMinLambda(Double minLambda) {
		this.minLambda = minLambda;
	}

	public Double getInfo() {
		return info;
	}

	public void setInfo(Double info) {
		this.info = info;
	}

	@Override
	public Double getStandartError() {
		return this.standardError; 
	}

	@Override
	public void setStandartError(Double standartError) {
		this.standardError = standartError;		
	}
	// TODO all changes of standardError
	public boolean getPrecisionTargetMet() {
		return precisionTarget <= standardError;
	}
	
    /**
     * Constructor
     */
     public ReportingCategory(String name, int minrequired, int maxrequired, boolean isStrict, double bpweight,
            Double cut, double info, double startAbility, Double abilityWeight, Double scalar, 
            Double precisionTarget, Double precisionTargetMetWeight, Double precisionTargetNotMetWeight, BpElementType type)
        {
    	super(name, minrequired, maxrequired, isStrict, bpweight, type);
            this.isStrand = false;
            this.adaptiveCut = (cut != null)? cut:-9999.0;
            this.startInfo = this.info = info;
            this.standardError = AAMath.SEfromInfo(this.startInfo);  // initialize SE based on start info
            this.startAbility = startAbility;
            this.abilityWeight = (abilityWeight != null && abilityWeight != 0.)? abilityWeight: 1.0;
            this.adaptiveWeight = (scalar != null)?scalar: 5.0; // previous default
            this.precisionTarget = (precisionTarget  != null)?precisionTarget: Double.MAX_VALUE;
            this.precisionTargetMetWeight = (precisionTargetMetWeight != null)?precisionTargetMetWeight: 1.0;
            this.precisionTargetNotMetWeight = (precisionTargetNotMetWeight != null)?precisionTargetNotMetWeight: 1.0;

            minLambda = lambda = 0.00632; // previous default
    }
    
    public ReportingCategory() {
	super();
	}

	/**
     * Load the reporting category element from a database results set
     * @param rs
     * @throws SQLException
     */
    @Override
    public void initialize(DbResultRecord record) throws SQLException
    {
    	super.initialize(record);    
    	abilityWeight 			= float2Double(record, "abilityWeight");
    	hweightBeforeTargetMet 	= float2Double(record, "precisionTargetMetWeight");
    	hweightAfterTargetMet 	= float2Double(record, "precisionTargetNotMetWeight");
    	targetInformation 		= float2Double(record, "precisionTarget");
    	
    	isStrand = false;
    	isReportingCategory = true;
    	
    	abilityWeight = (abilityWeight != null && abilityWeight != 0.)? abilityWeight: 1.0;
    }
    private Double float2Double(DbResultRecord record, String columnName)
    {
  	  try
  	  {
  		 return record.<Double> get(columnName); 
  	  } catch(Exception e)
  	  {
  		 return new Double(record.<Float> get(columnName)); 
  	  } 	  
    }
    /// <summary>
    /// Make a copy to use in examinee-specific thread context
    /// </summary>
    /// <returns></returns>
    public ReportingCategory Copy(boolean preserveStatistics)
    {
        ReportingCategory rc = new ReportingCategory(ID, minRequired, maxRequired, isStrictMax, weight, adaptiveCut, startInfo,
            startAbility, abilityWeight, adaptiveWeight, precisionTarget, precisionTargetMetWeight, precisionTargetNotMetWeight, bpElementType);
        rc.vectorIndex = this.vectorIndex;
        if (preserveStatistics)
        {
            rc.info = this.info;
            rc.theta = this.theta;
            rc.standardError = this.standardError;
            rc.numAdministered = this.numAdministered;
        }
        return rc;
    }
    
}
