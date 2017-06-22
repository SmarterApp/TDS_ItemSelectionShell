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

package tds.itemselection.services;

import java.util.UUID;

import tds.itemselection.base.ItemGroup;
import tds.itemselection.model.ItemResponse;

/**
 * Handles selecting items
 */
public interface ItemSelectionService {
  /**
   * Selects the next item group
   *
   * @param examId the exam id
   * @param isMsb  {@code true} if it is an MSB assessment
   * @return {@link tds.itemselection.model.ItemResponse} containing an {@link tds.itemselection.base.ItemGroup}
   */
  ItemResponse<ItemGroup> getNextItemGroup(UUID examId, boolean isMsb);
}
