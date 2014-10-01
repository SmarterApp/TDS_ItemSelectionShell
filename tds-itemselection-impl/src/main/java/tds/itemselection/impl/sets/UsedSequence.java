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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.impl.item.CsetItem;

/**
 * @author akulakov
 * 
 */
// / <summary>
// / Contains all cset items used previously on tests in a given sequence
// / Not for items used on THIS test opportunity
// / </summary>

// Nobody uses this class now!
public class UsedSequence
{
  private static Logger  _logger  = LoggerFactory.getLogger (UsedSequence.class);
  
  //Lower sequence numbers are further in the past. Higher are more recent.
  private int    sequence;                                            
  List<CsetItem> _items  = new ArrayList<CsetItem> ();

  public Collection<CsetItem> getItems ()
  {
    return _items;
  }

  public UsedSequence (int sequence) {
    this.sequence = sequence;
  }

  // / <summary>
  // / Add an item to this collection of previoiusly used items
  // / </summary>
  // / <param name="item"></param>
  public void addItem (CsetItem item)
  {
    // item.SetUsedChronology(sequence); Moved this to public version of
    // PriorAdmins.AddItem
    _items.add (item);
  }

  /**
   * @return the sequence only readable
   */
  public int getSequence () {
    return sequence;
  }

}
