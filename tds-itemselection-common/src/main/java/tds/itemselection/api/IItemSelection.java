/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.api;

import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;

import java.util.List;

/**
 * @author akulakov
 *
 */
public interface IItemSelection
{
  /**
   * this is one interface function yet
   * 
   * @param connection
   * @param oppkey
   *
   * @return ItemGroup
   */
  public ItemGroup getNextItemGroup (SQLConnection connection, ItemCandidatesData itemData)  throws ItemSelectionException;

  ItemGroup getNextItemGroup (SQLConnection connection, ItemCandidatesData itemData, List<ItemGroup> itemGroups)  throws ItemSelectionException;
  /**
   * 
   * @return
   */
  public String getItemSelectorError();
  /**
   * 
   */
  public Boolean isSegmentCompleted();
}
