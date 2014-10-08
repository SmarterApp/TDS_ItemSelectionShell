/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author akulakov
 * 
 */
public class BpElements
{
  private static Logger  _logger  = LoggerFactory.getLogger (BpElements.class);
  // each blueprint element is a combination of general and examinee-specific/
  // data
  private Map<String, BpElement> _elements = new HashMap<String, BpElement> ();

  // / <summary>
  // / Add a BpElement to the collection
  // / </summary>
  // / <param name="elem"></param>
  public void addBpElement (BpElement elem)
  {
    if (!_elements.containsKey (elem.ID))
      _elements.put (elem.ID, elem);
  }

  // / <summary>
  // / Retrieve a BpElement from the collection
  // / </summary>
  // / <param name="ID"></param>
  // / <returns></returns>
  public BpElement getElementByID (String ID)
  {
    if (_elements.containsKey (ID))
      return _elements.get (ID);
    else
      return null;
  }

  public Collection<BpElement> getValues ()
  {
    return _elements.values ();
  }
}
