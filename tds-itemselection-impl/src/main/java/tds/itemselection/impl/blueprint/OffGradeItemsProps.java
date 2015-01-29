/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.Helpers._Ref;

/**
 * @author akulakov
 * 
 */

public class OffGradeItemsProps {
	
	private static Logger _logger = LoggerFactory.getLogger(OffGradeItemsProps.class);
	
	final String offGradeMinItemsAdministeredName = "offGradeMinItemsAdministered" ;
	final String proficientThetaName 	= "proficientTheta";
	final String proficientPLevelName 	= "proficientPLevel";
	final String offGradeProbAffectProficiencyName 	= "offGradeProbAffectProficiency";

    // new for 2014-2015 to support off-grade items
    // This Map can be empty, has either one(1) or two(2) records 
    public Map<String, Integer> countByDesignator = new HashMap<String, Integer>(); // ex: { { "OFFGRADE ABOVE", 50 }, { "OFFGRADE BELOW", 35 } }
    public Integer 	minItemsAdministered; 
    public Double 	proficientTheta 	= null; // the cut score representing proficient, can be null
    public Integer 	proficientPLevel 	= null; // the integer level associated with the previous value, can be null
    public Double 	probAffectProficiency = 0.0001; // default value
    
    public OffGradeItemsProps copy()
    {
    	OffGradeItemsProps out 		= new OffGradeItemsProps();
    	out.countByDesignator 		= new HashMap<String, Integer>(this.countByDesignator);
    	out.minItemsAdministered 	= this.minItemsAdministered;
    	out.proficientTheta 		= this.proficientTheta;
    	out.proficientPLevel 		= this.proficientPLevel;
    	out.probAffectProficiency 	= this.probAffectProficiency;
    	
    	return out;    	
    }
    
	public void populateBluePrintOffGradeItemsDesignator(String name, String value)
	{
		if(name.equalsIgnoreCase(offGradeMinItemsAdministeredName))
		{
			_Ref<Integer> vlue = new _Ref<Integer>();
			if(isValueIntegerParsed(value, vlue))
			{
				this.minItemsAdministered = vlue.get();
			}
		}
		else if(name.equalsIgnoreCase(proficientThetaName))
		{
			_Ref<Double> vlue = new _Ref<Double>();
			if(isValueDoubleParsed(value, vlue))
			{
				this.proficientTheta = vlue.get();
			}
		}
		else if(name.equalsIgnoreCase(proficientPLevelName))
		{
			_Ref<Integer> vlue = new _Ref<Integer>();
			if(isValueIntegerParsed(value, vlue))
			{
				this.proficientPLevel = vlue.get();
			}
		}
		else if(name.equalsIgnoreCase(offGradeProbAffectProficiencyName))
		{
			_Ref<Double> vlue = new _Ref<Double>();
			if(isValueDoubleParsed(value, vlue))
			{
				this.probAffectProficiency = vlue.get();
			}
		}
	}

	private Boolean isValueDoubleParsed(String value, _Ref<Double> vlue)
	{
		Boolean isSuccess = false;
		if(value == null || value.isEmpty())
			return false;
		
		try{
			Double out = new Double(Double.parseDouble(value));
			vlue.set(out);
			isSuccess = true;
		} catch(Exception e)
		{
			_logger.error("Don't parse Double value " + value + ": " +  e.getMessage());
			isSuccess = false;
		}
		return isSuccess;
	}
	
	private Boolean isValueIntegerParsed(String value, _Ref<Integer> vlue)
	{
		Boolean isSuccess = false;
		if(value == null || value.isEmpty())
			return false;
		
		try{
			Integer out = new Integer(Integer.parseInt(value));
			vlue.set(out);
			isSuccess = true;
		} catch(Exception e)
		{
			_logger.error("Don't parse Integer value " + value + ": " + e.getMessage());
			isSuccess = false;
		}
		return isSuccess;
	}
	


}
