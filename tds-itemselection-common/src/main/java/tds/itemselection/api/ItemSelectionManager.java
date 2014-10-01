/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author akulakov
 *
 */
@Component
public class ItemSelectionManager implements IItemSelectionFactory
{
  private static Logger  _logger  = LoggerFactory.getLogger (ItemSelectionManager.class);

  @Autowired 
  private IItemSelection instanceSelection = null;
  
  public ItemSelectionManager() {}
  
  /* (non-Javadoc)
   * @see tds.itemselection.IItemSelectionFactory#createSelector(java.lang.String)
   */
  public IItemSelection createSelector () {
    // TODO Auto-generated method stub
    return instanceSelection;
  }

}
