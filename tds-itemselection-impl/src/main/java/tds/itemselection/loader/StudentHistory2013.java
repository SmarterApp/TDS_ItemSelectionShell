/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.CSetItemGroup;
import tds.itemselection.impl.sets.CsetGroupString;
import tds.itemselection.impl.sets.ItemPool;

/**
 * @author akulakov
 *
 */
public class StudentHistory2013
{
  private String 					customPool 	= null;
  private List<CsetGroupString> 	groups; 
  private Map<String, String> 		excludeGroups; 
  private Map<String, ItemResponse> responses;
	/**
	 * Start ability of the student 
	 */
  private Float startAbility = 0F;
  /**
   *  Item Pool Delimiter. Why is it ","?
   */
  private final String ITEM_POOL_DELIMITER = ",";
  
  /**
   * @return the customPool
   */
  public String getCustomPool () {
    return customPool;
  }
  /**
   * @param customPool the customPool to set
   */
  public void setCustomPool (String customPool) {
    this.customPool = customPool;
  }
  /**
   * @return the groups
   */
  public List<CsetGroupString> getGroups () {
    return groups;
  }
  /**
   * @param groups the groups to set
   */
  public void setGroups (List<CsetGroupString> groups) {
    this.groups = groups;
  }
  /**
   * @return the excludeGroups
   */
  public Map<String, String> getExcludeGroups () {
    return excludeGroups;
  }
  /**
   * @param excludeGroups the excludeGroups to set
   */
  public void setExcludeGroups (Map<String, String> excludeGroups) {
    this.excludeGroups = excludeGroups;
  }
  /**
   * @return the responses
   */
  public Map<String, ItemResponse> getResponses () {
    return responses;
  }
  /**
   * @param responses the responses to set
   */
  public void setResponses (Map<String, ItemResponse> responses) {
    this.responses = responses;
  }
  /**
   * @return the startAbility
   */
  public Float getStartAbility () {
    return startAbility;
  }
  /**
   * @param startAbility the startAbility to set
   */
  public void setStartAbility (Float startAbility) {
    this.startAbility = startAbility;
  }
  
//===============Adaptive2 Algorithm==========================================  
  /**
   * Standard error in start ability measurement
   */
  private Double _startSE = 0D;
      
  /**
   * Information associated with initial ability measurement 
   */
  private Double _startInformation = 0D;


/**
   * Sequence of item groups administered for this student previously
   */
  private ArrayList<HashSet<String>> _previousTestItemGroups = new ArrayList<HashSet<String>>();

  /**
   * Previous field test item groups administered to this student 
   */
  private HashSet<String> _previousFieldTestItemGroups = new HashSet<String>();

  /**
   * Past responses of this student 
   */
  private ArrayList<ItemResponse> _previousResponses = new ArrayList<ItemResponse>();

  /**
   * Custom item pool for this student
   */
  private ArrayList<String> _itemPool = new ArrayList<String>();
	
  /**
   * //AM: added in 2014; 
   * will be "OFFGRADE ABOVE" or "OFFGRADE BELOW" or null 
   * if the student doesn't have the relevant accommodation
   */
  private String offgrade = null;
  
  public Double get_startSE() {
	return _startSE;
}
public void set_startSE(Double _startSE) {
	this._startSE = _startSE;
}
public Double get_startInformation() {
	return _startInformation;
}
public void set_startInformation(Double _startInformation) {
	this._startInformation = _startInformation;
}
public ArrayList<HashSet<String>> get_previousTestItemGroups() {
	return _previousTestItemGroups;
}
public void set_previousTestItemGroups(
		ArrayList<HashSet<String>> _previousTestItemGroups) {
	this._previousTestItemGroups = _previousTestItemGroups;
}
public HashSet<String> get_previousFieldTestItemGroups() {
	return _previousFieldTestItemGroups;
}
public void set_previousFieldTestItemGroups(
		HashSet<String> _previousFieldTestItemGroups) {
	this._previousFieldTestItemGroups = _previousFieldTestItemGroups;
}
public ArrayList<ItemResponse> get_previousResponses() {
	return _previousResponses;
}
public void set_previousResponses(ArrayList<ItemResponse> _previousResponses) {
	this._previousResponses = _previousResponses;
}
public ArrayList<String> get_itemPool() {
	return _itemPool;
}
public void set_itemPool(ArrayList<String> _itemPool) {
	this._itemPool = _itemPool;
}


    /**
     * Get list of candidate groups based on current segment and past history
     * @param testSegment
     * @return
     */
    public ArrayList<CSetItemGroup> getCandidateItemGroups(TestSegment testSegment)
    {
        // List of potential candidates
        HashMap<String, CSetItemGroup> candidateGroups = new HashMap<String, CSetItemGroup>();

        // Item pool of segment
        ItemPool segmentItemPool = testSegment.getPool();

        // For all items given by student history
        int nCustomItems = _itemPool.size();
        for(int i=0; i < nCustomItems; ++i)
        {
            String itemID =  _itemPool.get(i);
            TestItem item = segmentItemPool.getItem(itemID);
            if (item == null)
                continue;
            
            // Check if the item is previously administered as field test item or it is not active or it is a field test item
            if (_previousFieldTestItemGroups.contains(item.groupID) || 
            		!(item.isActive) ||
            		(item.isFieldTest))
                continue;
            
            Boolean skip = false;            
            for(ItemResponse r : _previousResponses)
            {
            	if (r.groupID.equalsIgnoreCase (item.groupID))
            	{
            		skip = true;
            		break;
            	}
            }            
            if (skip)
              continue;
            
            // SegmentItemPool is guaranteed to have an item group for an item - get it
            ItemGroup itemGroup = segmentItemPool.getItemGroup(item.groupID);
            
            // Add the group, if it does not exist already
            if (!candidateGroups.containsKey(item.groupID))
            	candidateGroups.put(item.groupID, new CSetItemGroup(itemGroup));
            
            CSetItemGroup grp = candidateGroups.get(item.groupID);  
            grp.AddItem(item); // Note: Item will be added to the group - only if it is potential 
        }
        return new ArrayList<CSetItemGroup>(candidateGroups.values());
    } 
    
    public String getOffgrade() {
		return offgrade;
	}
	public void setOffgrade(String offgrade) {
		this.offgrade = offgrade;
	}
	/**
     * Constructor 
     */
    public StudentHistory2013()
    {        
    }
    
    /**
     * Initialize item pool for this student from the database
     * @param rs 
     * @throws SQLException
     */
	public void initializeItemPool(SingleDataResultSet res) throws SQLException {
		// Only one record for the pool
		_itemPool.clear();
		DbResultRecord record = res.getCount() > 0 ? res.getRecords().next()
				: null;
		if (record != null) {
			startAbility = record.<Float> get("initialAbility");
			// TODO - Get the s.e also
			// Get the item pool for the student
			String itemPool = record.<String> get("itempool");
			String[] items = itemPool.split(ITEM_POOL_DELIMITER);
			for (int i = 0; i < items.length; ++i)
				_itemPool.add(items[i].trim());
			//AM: added in 2014; will be "OFFGRADE ABOVE" or "OFFGRADE BELOW" or null if the student doesn't have the relevant accommodation
			if (res.hasColumn("offgrade"))
			offgrade = record.<String> get("offgrade");			
		}
	}
    
    /**
     * Initialize previous item groups administered for this student 
     * @param rs
     * @throws SQLException ;
     */
	public void initializePreviousItemGroups(SingleDataResultSet res)
			throws SQLException {
		// Get the past item groups administered for this student in
		// chronological order i
		// Index is the order in which they were administered
		_previousTestItemGroups.clear();
		Iterator<DbResultRecord> recItr = res.getRecords();
		while (recItr.hasNext()) {
			DbResultRecord record = recItr.next();
			if (record != null) {
				// TODO: why not initialize: oppk = record.<UUID> get
				// ("oppkey");
				String itemGroupString = record.<String> get("itemgroupString");
				String[] itemGroups = itemGroupString.split(",");
				if (itemGroups.length < 1)
					continue;
				HashSet<String> itemGroupSet = new HashSet<String>();
				for (int i = 0; i < itemGroups.length; ++i)
					itemGroupSet.add(itemGroups[i].trim());
				_previousTestItemGroups.add(itemGroupSet);
			}
		}
	}

    /**
     * Initialize field test item groups selected for this student
     * @param rs
     * @throws SQLException
     */
	public void initializeFieldTestItemGroups(SingleDataResultSet res)
			throws SQLException {
		// Get all the field test item groups selected for this segment
		_previousFieldTestItemGroups.clear();
		Iterator<DbResultRecord> recItr = res.getRecords();
		while (recItr.hasNext()) {
			DbResultRecord record = recItr.next();
			if (record != null) {
				String fieldTestItemGroup = record.<String> get("FTGroupID");
				_previousFieldTestItemGroups.add(fieldTestItemGroup);
			}
		}
	}

    /**
     * Initialize past responses for this student
     * @param rs
     * @throws SQLException
     */
	public void initializePreviousResponses(SingleDataResultSet res)
			throws SQLException {
		// Get all the previous responses
		_previousResponses.clear();
		Iterator<DbResultRecord> recItr = res.getRecords();
		while (recItr.hasNext()) {
		  DbResultRecord record = recItr.next();
			ItemResponse itemResponse = new ItemResponse();
			itemResponse.initialize(record);
			_previousResponses.add(itemResponse);
		}
	}
    
    /**
     * Update/estimate student ability using past responses 
     * @param segmentItemPool
     * @param studentAbility
     */
	public void updateStudentAbility(Blueprint bp, ItemPool pool,
			StudentAbility studentAbility) {
		studentAbility.initialize(startAbility, _startSE, _startInformation);
		int nNumberOfResponses = _previousResponses.size();
		for (int i = 0; i < nNumberOfResponses; ++i) {
			ItemResponse r = _previousResponses.get(i);
			TestItem item = pool.getItem(r.itemID);
			if (item == null)
				item = pool.getSiblingItem(r.itemID);
			if (item == null)
				continue; // Nothing better to do
			studentAbility.updateStudentMeasures(bp, item, r);
		}
	}
}
