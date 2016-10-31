/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.sets;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import tds.itemselection.base.Dimension;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;

/**
 * @author akulakov
 * 
 */
public class ItemPool
{
	// ================Common for Adaptive Algorithms========================
	
	/**
	 * Items in the item pool
	 */
  private Map<String, TestItem> _items = new HashMap<String, TestItem>();

  /**
   * Item groups in the item pool
   */
  private Map<String, ItemGroup> _itemGroups = new HashMap<String, ItemGroup>();

  /**
   * Sibling items in the item pool
   * // items that exist in other segments
   */
  private Map<String, TestItem> _siblingItems = new HashMap<String, TestItem>();

	
  private static Logger  _logger  = LoggerFactory.getLogger (ItemPool.class);

  //===========================only old Adaptive Algorithms====================================
  // / <summary>
  // / Add an item to the itempool; creates an itemgroup for singleton items
  // / </summary>
  // / <param name="item"></param>
  public void addItem (TestItem item)
  {
    // Do not add items to groups, as this is customizable on an examinee by
    // examinee basis
    if (!_items.containsKey (item.itemID))
    {
      _items.put (item.itemID, item);
      if (item.groupID.getBytes ()[0] == 'I') // indicates an item without an
                                              // explicit group. Create the
                                              // group
      {
        ItemGroup grp = new ItemGroup (item.groupID, 0, 1);
        addItemgroup (grp);
      }
    }
  }

  // / <summary>
  // / Add an item to the segment's pool of sibling items
  // / Is empty when the test is not segmented
  // / </summary>
  // / <param name="item"></param>
  public void addSiblingItem (TestItem item)
  {
    if (!_siblingItems.containsKey (item.itemID))
    {
      _siblingItems.put (item.itemID, item);
    }
    // We do not need the sibling item groups
  }

  /**
   * Get the candidate item in the item pool
   * @param itemID
   * @return
   */
  public TestItem getItem(String itemID)
  {
      return _items.containsKey(itemID) ? _items.get(itemID) : null;
  }
  /**
   * Get the item group in the item pool
   * @param groupID
   * @return
   */
  public ItemGroup getItemGroup(String groupID)
  {
      return _itemGroups.containsKey(groupID) ? _itemGroups.get(groupID) : null;
  }

  /**
   * Get the sibling item in the pool
   * @param itemID
   * @return
   */
  public TestItem getSiblingItem(String itemID)
  {
      return _siblingItems.containsKey(itemID) ? _siblingItems.get(itemID) : null;
  }

  // / <summary>
  // / Add an itemgroup to the itempool
  // / </summary>
  // / <param name="group"></param>
  public void addItemgroup (ItemGroup group)
  {
    if (!_itemGroups.containsKey (group.groupID)) {
      _itemGroups.put (group.groupID, group);
    }
  }
  public void dumpItemPool()
  {
	  String shift = "         ";
		_logger.info(shift + "    ItemPool:");    
		_logger.info(shift + "_items.size : " + _items.size());  
		_logger.info(shift + "_groups.size: " + _itemGroups.size());  
		_logger.info(shift + "_siblingItems.size: " + _siblingItems.size());  
  }

  ///============================new Adaptive Algorithms2======================================
  /**
   * Constructor
   */
  public ItemPool()
  {            
  }

  /**
   * Initialize test items of the segments item pool
   * @param res
   * @throws SQLException
   */
	public void InitializeTestItems(SingleDataResultSet res)
			throws SQLException {
		_items.clear();
		Iterator<DbResultRecord> recItr = res.getRecords();
		DbResultRecord record;
		if(_itemGroups == null)		{
			_itemGroups = new HashMap<String, ItemGroup>();
		}

		while (recItr.hasNext()) {
			record = recItr.next();
			TestItem testItem = new TestItem();
			testItem.initialize(record);
			_items.put(testItem.itemID, testItem);
			if (!_itemGroups.containsKey(testItem.groupID)) {
				if (testItem.groupID != null && !testItem.groupID.isEmpty()
//						&& (testItem.groupID.charAt(0) == 'I' 
//							|| testItem.groupID.charAt(0) == 'G')
							) {
					ItemGroup group = new ItemGroup(testItem.groupID, 0, 1);
					_itemGroups.put(testItem.groupID, group);
				}
				else // testItem.groupID == null || testItem.groupID.isEmpty() || !("I-200-123" OR "G-200-91") Group contains only one item
				{
					String groupId = "I-".concat(testItem.getItemID());
					testItem.setGroupID(groupId);
					ItemGroup group = new ItemGroup(groupId, 0, 1);
					_itemGroups.put(groupId, group);
				}
				// first item in the group
				ItemGroup group = _itemGroups.get(testItem.groupID);
				group.getItems().add(testItem);
			}
			else {
				ItemGroup group = _itemGroups.get(testItem.groupID);
				group.getItems().add(testItem);
				group.setMaximumNumberOfItems(group.getMaximumNumberOfItems() + 1);
			}
		}
	}
	/**
  	 * Initialize test items of the segments item pool + segmentPosition from blueprint
  	 * @param res
  	 * @param segmentPosition
  	 * @throws SQLException
  	 */
  	public void InitializeTestItems(SingleDataResultSet res, Integer segmentPosition)
			throws SQLException {
		_items.clear();
		Iterator<DbResultRecord> recItr = res.getRecords();
		DbResultRecord record;
		if(_itemGroups == null)		{
			_itemGroups = new HashMap<String, ItemGroup>();
		}

		while (recItr.hasNext()) {
			record = recItr.next();
			TestItem testItem = new TestItem();
			testItem.initialize(record);
			testItem.setSegmentPosition(segmentPosition);
			_items.put(testItem.itemID, testItem);
			if (!_itemGroups.containsKey(testItem.groupID)) {
				if (testItem.groupID != null && !testItem.groupID.isEmpty()) {
					ItemGroup group = new ItemGroup(testItem.groupID, 0, 1);
					_itemGroups.put(testItem.groupID, group);
				}
				else // testItem.groupID == null || testItem.groupID.isEmpty() // !("I-200-123" OR "G-200-91") Group contains only one item
				{
					String groupId = "I-".concat(testItem.getItemID());
					testItem.setGroupID(groupId);
					ItemGroup group = new ItemGroup(groupId, 0, 1);
					_itemGroups.put(groupId, group);
				}
				// first item in the group
				ItemGroup group = _itemGroups.get(testItem.groupID);
				group.getItems().add(testItem);
			}
			else {
				ItemGroup group = _itemGroups.get(testItem.groupID);
				group.getItems().add(testItem);
				group.setMaximumNumberOfItems(group.getMaximumNumberOfItems() + 1);
			}
		}		
	}
  /**
   * Initialize item groups of the segments item pool
   * @param groupTable
   * @throws SQLException
   */
  public void initializeItemGroups(SingleDataResultSet res) throws SQLException
  {   
      _itemGroups.clear();
      Iterator<DbResultRecord> recItr = res.getRecords ();
      DbResultRecord record;
      while(recItr.hasNext())
      {
    	  record = recItr.next();
          ItemGroup itemGroup = new ItemGroup();
          itemGroup.initialize(record);
          _itemGroups.put(itemGroup.getGroupID(), itemGroup);
      }
  }
    
  /**
   * Initialize the sibling items of the segments item pool
   * @param siblingItemTable
   * @throws SQLException
   */
  public void InitializeSiblingItems(SingleDataResultSet res) throws SQLException, Exception
  {
      // Get all the sibling items, if available
      _siblingItems.clear();
      if (res != null)
      {
          Iterator<DbResultRecord> recItr = res.getRecords ();
          DbResultRecord record;
          while(recItr.hasNext())
          {
        	record = recItr.next();
          	TestItem item = new TestItem();
          	item.initialize(record);
          	_siblingItems.put(item.itemID, item);
          }
      }
  }
  

  /**
   * Initialize the item dimension parameters from database and then construct the IRT model
   * @param itemDimensionsTable
   * @throws SQLException
   * @throws Exception
   */
	public void InitializeItemDimensions(SingleDataResultSet res)
			throws SQLException, Exception {
		if (res != null) {
			Iterator<DbResultRecord> recItr = res.getRecords();
			DbResultRecord record;
			while (recItr.hasNext()) {
				record = recItr.next();
				String sItemKey = record.<String> get("itemkey");
				String sDimensionName = record.<String> get("Dimension");
				String irtModel = record.<String> get("irtModel");
				int paramNum = record.<Integer> get("parmnum");
				String sParamName = record.<String> get("parmname");
				Double fParamValue = float2Double(record, "parmvalue");
				TestItem item = _items.get(sItemKey);
				if(item != null)
				{
					item.initializeDimensionEntry(sDimensionName, irtModel,
						paramNum, sParamName, fParamValue);
				}
			}

			// Now construct the IRT model objects for dimensions
			Iterator<TestItem> itTI = _items.values().iterator();
			while (itTI.hasNext()) {
				TestItem item = itTI.next();
				Iterator<Dimension> itDimension = item.dimensions.iterator();
				while (itDimension.hasNext()) {
					Dimension dim = itDimension.next();
					dim.initializeIRT();
				}
				item.hasDimensions = item.dimensions.size() > 1;
			}
		}
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
    // for debug only
    public Collection<ItemGroup> getItemGroups()
    {
    	return this._itemGroups.values();
    }
    public Collection<TestItem> getItems()
    {
    	return this._items.values();
    }
    public Collection<TestItem> getSiblingItems() { return this._siblingItems.values(); }


}
