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

import java.util.List;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;

public interface MsbItemSelector extends ItemSelector {
  /**
   * @param itemData   Metadata about the segment
   * @param itemGroups These item groups will only be present if the test is a Multi-Stage Braille (MSB) test. In that
   *                   case, each group will represent the entire contents of a fixed-form segment.
   * @return ItemGroup if this is a MSB test, this item group will be the next fixed-form segment to execute
   * @throws tds.itemselection.api.ItemSelectionException when there is an issue getting the next group
   */
  ItemGroup getNextItemGroup(ItemCandidatesData itemData, List<ItemGroup> itemGroups) throws ItemSelectionException;
}
