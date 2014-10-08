/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.sets;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akulakov This is auxilary, supporting class
 * 
 *         /// <summary> /// Holds delimited string of itemgroups administered
 *         previously to examinee /// </summary>
 * 
 * 
 */
public class CsetGroupString
{
  private static Logger  _logger  = LoggerFactory.getLogger (CsetGroupString.class);
  // chronological order amongst all elements in a collection
  public int                           sequence;
  // the opportunity key from which the itemgroups came
  public UUID                        oppkey;
  // delimited string of itemgroups administered on the previous test
  public String                        groupString;

  public Map<String, String> itemgroups = new HashMap<String, String> (64);

  public boolean exists (String groupID)
  {
    return itemgroups.containsKey (groupID);
  }

  public CsetGroupString (UUID oppkey, int sequence, String groups)
  {
    this.oppkey = oppkey;
    this.sequence = sequence;
    this.groupString = groups;

    for (String gID : groupString.split (","))
    {
      itemgroups.put (gID, gID);
    }
  }

}
