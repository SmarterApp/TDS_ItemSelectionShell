/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.base;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.results.DbResultRecord;

/**
 * @author temp_rreddy
 * @author akulakov
 */
// Comment from C#:
// I need a structure that holds item and group information for passing to the
// database procedure T_InsertItems:
// @oppkey uniqueidentifier, @session uniqueidentifier, @browserID
// uniqueidentifier,
// @segment int, @segmentID varchar(100), @page int, @groupID varchar(50),
// @bankkey bigint,
// @itemkeys varchar(max), @delimiter char = ',', @groupItemsRequired int = -1

public class ItemGroup
{

  private static Logger  _logger  = LoggerFactory.getLogger (ItemGroup.class);

  /*
   * Common members for Adaptive Algorithm and Adaptive2 Algorithm
   */
  // Comment from C#: array of pointers to CSETItem that have this groupID
  public List<TestItem> 		items  = new ArrayList<TestItem> ();
  public String         		groupID;
  // number of items flagged as 'required'
  protected Integer           	numberOfItemsRequired;
// the maximum number of items to administer from the group
  protected Integer           	maximumNumberOfItems;
  
  /*
   * Members for Adaptive Algorithm only
   */    
  private int           _bankkey;
  private int           _groupKey;
  private String        _segmentKey;
  private String        _segmentID;
  private int           _segmentPosition;
  // the average of IRT_b values of items in the group. Needed for simulations and Item Selection
  private double        _groupB;
  private int           _itemCount;
  
  /**
   * @return the _bankkey
   */
  public int getBankkey () {
    return _bankkey;
  }

  /**
   * @param _bankkey
   *          the _bankkey to set
   */
  public void setBankkey (int _bankkey) {
    this._bankkey = _bankkey;
  }

  /**
   * @return the _groupID
   */
  public String getGroupID () {
    return groupID;
  }

  /**
   * @param _groupID
   *          the _groupID to set
   */
  public void setGroupID (String _groupID) {
    this.groupID = _groupID;
  }

  /**
   * @return the _segmentPosition
   */
  public int getSegmentPosition () {
    return _segmentPosition;
  }

  /**
   * @param _segmentPosition
   *          the _segmentPosition to set
   */
  public void setSegmentPosition (int _segmentPosition) {
	    this._segmentPosition = _segmentPosition;
	  }

  /**
   * 
   * @param _segmentPosition
   */
  public void setSegmentPosition (long _segmentPosition) {
	    this._segmentPosition = (int)_segmentPosition;
	  }

  /**
   * @return the _numRequired
   */
  public int getNumRequired () {
    return numberOfItemsRequired;
  }

  /**
   * @param _numRequired
   *          the _numRequired to set
   */
	public void setNumRequired(int _numRequired) {
		this.numberOfItemsRequired = _numRequired;
	}

	public Integer getNumberOfItemsRequired() {
		return numberOfItemsRequired;
	}

	public void setNumberOfItemsRequired(Integer numberOfItemsRequired) {
		this.numberOfItemsRequired = numberOfItemsRequired;
	}

	public Integer getMaximumNumberOfItems() {
		return maximumNumberOfItems;
	}

	public void setMaximumNumberOfItems(Integer maximumNumberOfItems) {
		this.maximumNumberOfItems = maximumNumberOfItems;
	}

  /**
   * @return the _segmentID
   */
  public String getSegmentID () {
    return _segmentID;
  }

  /**
   * @param _segmentID
   *          the _segmentID to set
   */
  public void setSegmentID (String _segmentID) {
    this._segmentID = _segmentID;
  }

  /**
   * @return the _segmentKey
   */
  public String getSegmentKey () {
    return _segmentKey;
  }

  /**
   * @param _segmentKey
   *          the _segmentKey to set
   */
  public void setSegmentKey (String _segmentKey) {
    this._segmentKey = _segmentKey;
  }

  /**
   * @return the _maxItems
   */
  public int getMaxItems () {
    if (maximumNumberOfItems == -1)
      return this.getActiveCount ();
    else
      return maximumNumberOfItems;
  }

  /**
   * @return the _activeCount
   */
  public int getActiveCount () {
    int cnt = 0;
    for (TestItem item : getItems ())
      if (item.isActive)
        ++cnt;
    return cnt;
  }

  public ItemGroup()
  {            
      items = new ArrayList<TestItem>();
  }

  /**
   * Constructor taking parameters 
   * @param ID
   * @param itemsRequired
   * @param maxItems
   */
  public ItemGroup(String ID, Integer itemsRequired, Integer maxItems)
  {
      groupID = ID;
      numberOfItemsRequired = itemsRequired;
      maximumNumberOfItems = maxItems;
      items = new ArrayList<TestItem>();
  }

  public ItemGroup (String groupID, int itemsRequired, int maxItems)
  {
    this.groupID = groupID;
    numberOfItemsRequired = itemsRequired;
    this.maximumNumberOfItems = maxItems;

    _segmentKey = null;
    _segmentPosition = -1;
  }

  public ItemGroup (String ID, String segmentID, int segmentPos, int itemsRequired, int maxItems)
  {
    this.groupID = ID;
    this._segmentID = segmentID;
    this._segmentPosition = segmentPos;
    this.numberOfItemsRequired = itemsRequired;
    this.maximumNumberOfItems = maxItems;
  }

public ItemGroup(String groupID2, String segmentKey, String segmentID,
		Integer segmentPosition, Integer numberOfItemsRequired2,
		Integer maximumNumberOfItems2) {
	this(groupID2, segmentID, segmentPosition, numberOfItemsRequired2, maximumNumberOfItems2);
	this._segmentKey = segmentKey;
}

/**
 * Load the item group from a database record
 * @param rs
 * @throws SQLException 
 */
  public void initialize(DbResultRecord record) throws SQLException
  {
	  	groupID = record.<String> get("itemGroup"); // why itemGroup? TODO: check it
	  	Long l  = record.<Long> get("itemsRequired");
		numberOfItemsRequired = (Integer)1 ;
		maximumNumberOfItems = record.<Integer> get("maxItems");
		// TODO AK: what does do with bpweight?
  }

  public void addItem (TestItem item)
  {
    this.getItems ().add (item);

  }

  public TestItem getItem (String ID)
  {
    for (TestItem item : getItems ())
    {
      if (item.itemID.equals(ID))
        return item;
    }
    return null;
  }

  public int compareTo (Object rhs)
  {
    return 0; // TODO: (AK) must return _groupB (?)
  }

  public String getItemIDString (String delim)
  {
    TestItem itm = getItems ().get (0);
    String result = itm.itemID;
    for (int i = 1; i < getItems ().size (); ++i)
    {
      itm = getItems ().get (i);
      result += delim + itm.itemID;
    }
    return result;

  }

  /**
   * @return the _groupB
   */
  public double getGroupB () {
    double sum = 0.0;
    for (TestItem itm : getItems ())
      sum += itm.getAverageB();
    return sum / getItems ().size ();
  }

  /**
   * @return the _itemCount
   */
  public int getItemCount () {
    return getItems ().size ();
  }

  /**
   * @return the _items
   */
  public List<TestItem> getItems () {
    return items;
  }

  /**
   * @param _items
   *          the _items to set
   */
  public void setItems (List<TestItem> _items) {
    this.items = _items;
  }

}

