/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.selectors.impl;

import org.apache.commons.lang.NotImplementedException;

import java.util.List;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.selectors.ItemSelector;

public abstract class AbstractItemSelector implements ItemSelector {
  String _error = null;

  // have termination condition(s) been met.
  boolean isSegmentCompleted = false;

  @Override
  public String getItemSelectorError() {
    return _error;
  }

  @Override
  public boolean isSegmentCompleted() {
    return isSegmentCompleted;
  }

  @Override
  public ItemGroup getNextItemGroup(ItemCandidatesData itemData, List<ItemGroup> itemGroups) throws ItemSelectionException {
    throw new NotImplementedException("This method is for Multi-Stage Braille use only");
  }
}
