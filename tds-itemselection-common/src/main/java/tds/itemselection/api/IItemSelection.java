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
   * @param itemData
   *
   * @return ItemGroup
   */
  ItemGroup getNextItemGroup (SQLConnection connection, ItemCandidatesData itemData)  throws ItemSelectionException;

  /**
   *
   * @param connection
   * @param itemData Metadata about the segment
   * @param itemGroups These item groups will only be present if the test is a Multi-Stage Braille (MSB) test. In that
   *                   case, each group will represent the entire contents of a fixed-form segment.
   * @return ItemGroup if this is a MSB test, this item group will be the next fixed-form segment to execute
   * @throws ItemSelectionException
   */
  ItemGroup getNextItemGroup (SQLConnection connection, ItemCandidatesData itemData, List<ItemGroup> itemGroups)  throws ItemSelectionException;

  /**
   * 
   * @return
   */
  String getItemSelectorError();

  /**
   * 
   */
  Boolean isSegmentCompleted();
}
