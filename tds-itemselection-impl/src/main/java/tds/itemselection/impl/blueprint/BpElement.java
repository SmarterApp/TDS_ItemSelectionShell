/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.Helpers._Ref;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.CsetGroup;

/**
 * @author akulakov
 * 
 */
public class BpElement
{
	
	public BpElement()
	{}

  private static Logger  _logger  = LoggerFactory.getLogger (BpElement.class);

  /*
   * Common for 2 algorithm
   */
	// independent of examinee
	// the name of the blueprint/ element
	public String ID;
	// minimum number of items required to/ satisfy the element
	public Integer minRequired;
	// maximum number of items allowed
	public Integer maxRequired;
	// if 'true', then it is not allowed to administer more than the maxRequired
	// items on this element
	public Boolean isStrictMax;// if 'true', then it is not allowed to administer more than the maxRequired items on this element
	// weight relative to other blueprint elements
	// Weight contributed by this element for blueprint match
	public Double weight = 1.0D; // = bpweight

	public Boolean isStrand = false;
	public boolean isReportingCategory = false;
	
	public boolean isReportingCategory() {
		return isReportingCategory;
	}

	public void setReportingCategory(boolean isReportingCategory) {
		this.isReportingCategory = isReportingCategory;
	}


	/*
	 * Only for Adaptive Algorithm
	 */
  // specific to examinee
  // number items administered this examinee at this/ point in time
  public Integer                              numAdministered = 0;
  // Array of CSetItem that have this content level property
  private Map<String, CSetItem> _items          = new HashMap<String, CSetItem> ();
  
  public Map<String, CSetItem> getItems() {
	return _items;
}

public void setItems(Map<String, CSetItem> _items) {
	this._items = _items;
}

//==================Adaptive2=================================

  public enum BpElementType
  {
      Strand,
      ContentLevel,
      AffinityGroup
  }

  public BpElementType bpElementType;

  public BpElementType getBpElementType() {
	return bpElementType;
}

public void setBpElementType(BpElementType bpElementType) {
	this.bpElementType = bpElementType;
}

// new for 2014 to support off-grade items
// TODO: this is temporary solution because I don't know field (or property as is in C#)
// with names TierBPriorCut, TierCPriorCut, TierBSPQCut, TierCSPQCut, SmallCut, BigCut
// But all of them are Integer
Map<String, Integer> itemSelectionParams = new HashMap<String, Integer>();

public void putItemSelectionParam(String name, String value)
{
	
	_Ref<Integer> valueRef = new _Ref<Integer>();
	if(isValueIntegerParsed(value, valueRef))
	{
		itemSelectionParams.put(name, valueRef.get());
	}
	else
		_logger.warn("Cannot parse value " + value + " to int");
}

// / <summary>
  // / Has this element met or exceeded its strict max?
  // / </summary>
  public boolean hasOverMax ()
  {
    return isStrictMax && numAdministered >= maxRequired;
  }

  // the following are flags to identify elements that are needed for adaptive
  // ability match computations

  // / <summary>
  // / Use this constructor for test blueprint independent of examinee
  // / </summary>
  // / <param name="name"></param>
  // / <param name="minrequired"></param>
  // / <param name="maxrequired"></param>
  // / <param name="isStrict"></param>
  // / <param name="weight"></param>
  public BpElement (String name,
      int minrequired,
      int maxrequired,
      boolean isStrict,
      double weight)
  {
    this.ID = name;
    this.minRequired = minrequired;
    this.maxRequired = maxrequired;
    this.isStrictMax = isStrict;
    this.weight = weight;
    this.isStrand = false;
  }

  // / <summary>
  // / Use this constructor for examinee-specific blueprint satisfaction
  // (poolcount must be set separately and can fluctuate via recycling)
  // / </summary>
  // / <param name="name"></param>
  // / <param name="minrequired"></param>
  // / <param name="maxrequired"></param>
  // / <param name="isStrict"></param>
  // / <param name="weight"></param>
  // / <param name="numAdministered"></param>
	public BpElement(String name, int minrequired, int maxrequired,
			boolean isStrict, double weight, int numAdministered) {
		this(name, minrequired, maxrequired, isStrict, weight);
		this.numAdministered = numAdministered;
	}

	public BpElement(String name, int minrequired, int maxrequired,
			boolean isStrict, double weight, BpElementType type) {
		this(name, minrequired, maxrequired, isStrict, weight);
		this.bpElementType = type;
	}

	public BpElement(String name, int minrequired, int maxrequired,
			boolean isStrict, double weight, int numAdministered,
			BpElementType type) {
		this(name, minrequired, maxrequired, isStrict, weight);
		this.numAdministered = numAdministered;
		this.bpElementType = type;
	}

  public BpElement copy()
  {
      return copy(false);
  }

  public BpElement copy (boolean preserveStatistics)
  {
    BpElement elem;
    if((isStrand && isReportingCategory))
    {
    	_logger.info("BpElement is a strand and ReportingCategory element");
    }
    if (isStrand)
    {
      Strand s = (Strand) this;
      elem = s.copy (preserveStatistics);
    }
    else if (isReportingCategory) {
		ReportingCategory rc = (ReportingCategory) this;
		elem = rc.Copy(preserveStatistics);
	} else
      elem = new BpElement (ID, minRequired, maxRequired, isStrictMax, weight, 0);
    return elem;
  }
  
  public void addItem (CSetItem item)
  {
    if (_items == null)
      _items = new HashMap<String, CSetItem> ();

    if(!_items.containsKey(item.getItemID()))
    {
    	_items.put (item.itemID, item);
    }
  }

  // / <summary>
  // / Number of items on this content level that are active
  // / </summary>
  public int getPoolCount ()
  {
    if (_items == null)
      return 0;
    int cnt = 0;
    for (CsetItem item : _items.values ())
      if (item.isActive ())
        ++cnt;
    return cnt;
  }

  // / <summary>
  // / Prune all items if numAdministered >= maxRequired
  // / </summary>
  // / <param name="elements"></param>
  public boolean prune ()
  {
    // IMPORTANT: These items are shared with other BpElements, so they, too,
    // will be pruned by these items
    // This can cause some content levels to fall short of their ability to meet
    // minitem requirements
    // So they will need to 'Unprune' (below)
    if (isStrictMax && numAdministered >= maxRequired && _items != null)
    {
      boolean pruned = false;
      for (CsetItem item : _items.values ())
      {
        CsetGroup grp = item.getParentGroup ();
        if (item.isActive () && !item.isRequired ()
            && (grp.getMaxItems () == -1 || grp.getActiveCount () > grp.getMaxItems ()))
        { // pruning can only apply to items currently in the pool
          item.pruned = true;
          pruned = true;
        }
      }
      return pruned;

    }
    return false;
  }

  // / <summary>
  // / Return pruned items to pool if pool insufficient for minitems
  // / </summary>
  // / <param name="bpElements">The collection of blueprint elements</param>
  public void unprune (boolean releaseAll)
  {
    // Use the blueprint elements to decide which items to leave pruned
    if (_items == null || minRequired < 1)
      return; // no items this element
    int pcnt = getPoolCount ();
    int itemcnt = _items.size ();
    if (pcnt + numAdministered >= minRequired || pcnt == itemcnt)
      return;
    for (CsetItem item : _items.values ())
    {
      if (item.pruned)
      {
        item.pruned = false;
        ++pcnt;
        if (!releaseAll && (pcnt + numAdministered >= minRequired))
          return;
      }

    }
  }

  // / <summary>
  // / Recycle items from previous admin to meet minimum requirement
  // / </summary>
  // / <param name="chronology"></param>
  // / <param name="releaseAll"></param>
  // / <returns></returns>
  public boolean recycle (int chronology, boolean releaseAll)
  {
    int pcnt = getPoolCount ();
    boolean recycled = false;
    int itemcnt = (_items == null) ? 0 : _items.size ();
    if (pcnt + numAdministered >= minRequired || pcnt == itemcnt)
      return false;
    for (CsetItem item : _items.values ())
    {
      if (item.getUsed () && item.getChronology () == chronology)
      {
        item.getParentGroup ().setUsed (false); // this has the side effect of
                                                // releasing ALL items : the
                                                // parent group
        recycled = true;
        // Update the poolcount by number of items on this contentlevel in the
        // Parentgroup
        pcnt = getPoolCount (); // brute force poolcount of ALL items this
                                // element, but it is accurate
        if (!releaseAll && (pcnt + numAdministered >= minRequired))
          return recycled;
      }
    }
    return recycled;
  }

  public void initialize(DbResultRecord record) throws SQLException
  {
  	ID = record.<String>get("contentLevel"); // TODO - Check Conflict with GroupID
  	minRequired = record.<Integer>get("minItems");
  	maxRequired = record.<Integer>get("maxItems");
  	isStrictMax = record.<Boolean>get("isStrictMax");
  	weight = float2Double(record, "bpweight");
  	Long intType = record.<Long>get("elementType");
  	Integer indx = (intType != null)? intType.intValue(): 0;
  	bpElementType = BpElementType.values()[indx];
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

	private Boolean isValueIntegerParsed(String value, _Ref<Integer> valueRef)
	{
		Boolean isSuccess = false;
		if(value == null || value.isEmpty())
			return false;
		
		try{
			Integer out = new Integer(Integer.parseInt(value));
			valueRef.set(out);
			isSuccess = true;
		} catch(Exception e)
		{
			_logger.error("Don't parse Integer value " + value + ": " + e.getMessage());
			isSuccess = false;
		}
		return isSuccess;
	}

}
