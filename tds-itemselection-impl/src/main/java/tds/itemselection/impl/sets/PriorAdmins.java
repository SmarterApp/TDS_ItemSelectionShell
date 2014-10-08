/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.sets;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.base.TestItem;
import tds.itemselection.impl.item.CsetItem;

/**
 * @author akulakov
 * 
 */
// / <summary>
// / The collection of items administered on tests previous to this one
// / </summary>

// Nobody uses this class now!
// TODO: (AK)
public class PriorAdmins
{
  private static Logger  _logger  = LoggerFactory.getLogger (PriorAdmins.class);
  // key is the chronological sequence number 1 to N, where 1 is most distant in
  // past
  private Map<Integer, UsedSequence> _admins = null;
  int                                          _min    = 9999;
  int                                          _max    = 0;
  private Map<String, TestItem>      _items  = null;

  public int getFirst () {
    return _min;
  }

  public int getLast () {
    return _max;
  }

  // / <summary>
  // / Add an item to a prior admin with the given chronology
  // / </summary>
  // / <param name="chronology"></param>
  // / <param name="item"></param>
  private void addItem (CsetItem item)
  {
    int chronology = item.getChronology ();
    UsedSequence seq;
    if (_admins == null)
      _admins = new HashMap<Integer, UsedSequence> ();
    if (!_admins.containsKey (chronology))
    {
      seq = new UsedSequence (chronology);
      _admins.put (chronology, seq);
      seq.addItem (item);
    }
    else
    {
      seq =  _admins.get (chronology);
      seq.addItem (item);
    }

  }

  // / <summary>
  // / Set an item's most recent chronology and add to prior admins
  // / Call Sort when all items have been added
  // / </summary>
  // / <param name="chronology"></param>
  // / <param name="item"></param>
  public void addItem (int chronology, CsetItem item)
  {
    // cannot sort while adding because an item's chronology may change as it is
    // discovered to have been administered more than once previously
    // The CSETGroup parent is the 'owner' of the chronology
    item.getParentGroup ().setChronology (chronology);
    _min = Math.min (chronology, _min);
    _max = Math.max (chronology, _max);
    if (_items == null)
      _items = new HashMap<String, TestItem> ();
    // This guarantees every item is added exactly once, no matter how many
    // times administered
    if(!_items.containsKey(item.itemID))
    {
    _items.put (item.itemID, item);
    }
  }

  // / <summary>
  // / Once all items have been added for every chronology, sort them into their
  // respective slots
  // / IMPORTANT: The object cannot be used until this is done.
  // / </summary>
  public void sortItems ()
  {
    if (_items == null)
      return;
    for (Object oitem : _items.values ())
    {
      CsetItem item = (CsetItem) oitem;
      addItem (item);
    }
  }

  // / <summary>
  // / Get the array of items from a prior admin
  // / </summary>
  // / <param name="chronology"></param>
  // / <returns></returns>
  public UsedSequence get (int chronology)
  {
    if (_admins.containsKey (chronology))
      return _admins.get (chronology);
    else
      return null;
  }

}
