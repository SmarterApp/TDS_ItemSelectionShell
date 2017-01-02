/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.base.ItemGroup;

/**
 * @author akulakov 
 * 		This is auxilary, supporting class /// <summary> ///
 *         Convenient collection of cset item groups encapsulating annoyances of
 *         Map /// </summary>
 */
public class CsetGroupCollection
{
  private static Logger  _logger  = LoggerFactory.getLogger (CsetGroupCollection.class);
  
  private Map<String, CsetGroup> _groups = new HashMap<String, CsetGroup> ();

  // / <summary>
  // / Get the cset group of same ID as ItemGroup, adds it if not exists
  // / </summary>
  // / <param name="group"></param>
  // / <returns></returns>
  public CsetGroup setAndGet (ItemGroup group)
  {
    CsetGroup cgrp;
    if (!_groups.containsKey (group.groupID))
    {
      cgrp = new CsetGroup (group.groupID, group.getNumberOfItemsRequired (), group.getMaxItems ());
      _groups.put (group.groupID, cgrp);
      return cgrp;
    }
    return  _groups.get (group.groupID);
  }

  // / <summary>
  // / Gets the cset group of groupID if exists, null otherwise
  // / </summary>
  // / <param name="groupID"></param>
  // / <returns></returns>
  public CsetGroup get (String groupID)
  {
    if (_groups.containsKey (groupID))
      return  _groups.get (groupID);
    else
      return null;
  }

  // / <summary>
  // / Removes an itemgroup from the collection
  // / </summary>
  // / <param name="groupID"></param>
  public void remove (String groupID)
  {
    if (_groups.containsKey (groupID))
      _groups.remove (groupID);
  }

  // / <summary>
  // / Removed all itemgroups with no active items from the collection
  // / </summary>
  public void removeUsed ()
  {
    removeUsed(false);
  }

  public void removeUsed(boolean ignoreParent) {
    List<CsetGroup> remove = new ArrayList<CsetGroup> ();
    for (CsetGroup grp : _groups.values ())
    {
      if (grp.getActiveCount(ignoreParent) == 0)
        remove.add (grp);
    }
    for (CsetGroup grp : (Collection<CsetGroup>) remove)
      _groups.remove (grp.groupID);
  }

  public Collection<CsetGroup> getValues ()
  {
    return _groups.values ();
  }
  // AK: I would like to redefine the function above
  public List<CsetGroup> getOrderedValues ()
  {
    List<String> keys = new ArrayList<String> (_groups.keySet());
    Collections.sort (keys);
    List<CsetGroup> res = new ArrayList<CsetGroup>();
    for(String id: keys)
    {
    	res.add(_groups.get(id));
    }
    return res;
  }

}
