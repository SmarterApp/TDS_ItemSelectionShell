/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import tds.itemselection.base.TestItem;

/**
 * @author akulakov
 * 
 */
public class ItemResponse implements Comparable<Object>
{
  private static Logger  _logger  = LoggerFactory.getLogger (ItemResponse.class);
// Common members
	/**
	 * Segment position
	 */
public Integer segmentPosition;

/**
 * Item for which this response is for
 */
public String itemID;

/**
 * Group id of the item
 */    
public String groupID;

/**
 * Students score value for this item
 */
public Integer score;

public Integer getScore() {
	return score;
}

public void setScore(Integer score) {
	this.score = score;
}

/**
 * Item position
 */
public Integer itemPosition;

/**
 * Flag to indicate whether this response is for a field test
 */
public Boolean isFieldTest;

/**
 * Constructor
 */
public ItemResponse()
{            
}
// Only Adaptive Algorithm old

  private double _b;
  private String _strand;
  TestItem       _baseItem = null;
  private String scoreDimensions;
  private Map<String, Integer> DimensionScores =new HashMap<String, Integer>();

  public Map<String, Integer> getDimensionScores() {
	return DimensionScores;
}

public void setDimensionScores(Map<String, Integer> dimensionScores) {
	DimensionScores = dimensionScores;
}

/**
   * @return the _baseItem
   */
  public TestItem getBaseItem () {
    return _baseItem;
  }

  /**
   * @param _baseItem
   *          the _baseItem to set
   */
  public void setBaseItem (TestItem _baseItem) {
    this._baseItem = _baseItem;
  }

  /**
   * @return the _b
   */
  public double get_b () {
    if (_baseItem != null)
      return _baseItem.b;
    else
      return _b;
  }

  /**
   * @param _b
   *          the _b to set
   */
  public void set_b (double _b) {
    this._b = _b;
  }

  /**
   * @return the _strand
   */
  public String get_strand () {
    if (_baseItem != null)
      return _baseItem.strandName;
    else
      return _strand;
  }

  /**
   * @param _strand
   *          the _strand to set
   */
  public void setStrand (String _strand) {
    this._strand = _strand;
  }

  public ItemResponse (int segment, String ID, String groupID, int position, String strand, double IRTb, int score, boolean isFieldTest)
  {
    this.groupID = groupID;
    this.segmentPosition = segment;
    this.itemID = ID;
    this.itemPosition = position;
    this._b = IRTb;
    this._strand = strand;
    this.score = score;
    this.isFieldTest = isFieldTest;
  }

  public ItemResponse (int segment, String ID, String groupID, int position, int score, boolean isFieldTest)
  {
    this.groupID = groupID;
    this.segmentPosition = segment;
    this.itemID = ID;
    this.itemPosition = position;

    this.score = score;
    this.isFieldTest = isFieldTest;
  }

  public int compareTo (Object rhs)
  {
    ItemResponse resp = (ItemResponse) rhs;
    return (new Integer (this.itemPosition)).compareTo (resp.itemPosition);
  }
  

  /**
   * Load the item response from database record
   * @param rs
   * @throws SQLException
   */
  public void initialize(DbResultRecord record) throws SQLException
  {
  
      	if (record != null) {
      	  segmentPosition = record.<Integer> get("segmentPosition");
        	itemID = record.<String> get ("itemID");
        	groupID = record.<String> get ("groupID");
            score = record.<Integer> get("score");
            itemPosition = record.<Integer> get("position");
            isFieldTest = record.<Boolean> get ("isFieldTest");
      	}
    
  }

}
