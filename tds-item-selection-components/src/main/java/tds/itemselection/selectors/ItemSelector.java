/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.itemselection.selectors;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;

public interface ItemSelector {
  /**
   * Gets the next item group to serve to the user
   *
   * @param itemData the {@link tds.itemselection.base.ItemCandidatesData} to act upon
   * @return ItemGroup the {@link tds.itemselection.base.ItemGroup}
   */
  ItemGroup getNextItemGroup(ItemCandidatesData itemData) throws ItemSelectionException;

  /**
   * @return if there is an error when selecting an item group
   */
  String getItemSelectorError();

  /**
   * {@code true} if the segment is completed
   */
  boolean isSegmentCompleted();
}
