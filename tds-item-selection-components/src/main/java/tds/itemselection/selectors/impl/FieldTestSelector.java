/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.selectors.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.selectors.ItemSelector;
import tds.itemselection.services.ItemCandidatesService;

/**
 * Selects field test items
 */
@Component
public class FieldTestSelector extends AbstractItemSelector implements ItemSelector {
  private static Logger _logger = LoggerFactory.getLogger(FieldTestSelector.class);

  private final ItemCandidatesService itemCandidatesService;

  @Autowired
  public FieldTestSelector(ItemCandidatesService itemCandidatesService) {
    this.itemCandidatesService = itemCandidatesService;
  }

  @Override
  public ItemGroup getNextItemGroup(ItemCandidatesData itemCandidates) throws ItemSelectionException {
    try {
      return itemCandidatesService.getItemGroup(itemCandidates.getOppkey(),
        itemCandidates.getSegmentKey(),
        itemCandidates.getGroupID(),
        itemCandidates.getBlockID(),
        true);
    } catch (Exception e) {
      String error = String.format("Exception %1$s executing field test selection algorithm. Exception error: %2$s", "Exception", e.getMessage());
      _logger.error(error);
      throw new ItemSelectionException(error);
    }
  }
}
