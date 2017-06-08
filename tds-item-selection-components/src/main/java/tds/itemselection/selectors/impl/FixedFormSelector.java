/*
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
 * Handles Fixed Form Selector logic
 */
@Component
public class FixedFormSelector extends AbstractItemSelector implements ItemSelector {
  private static Logger logger = LoggerFactory.getLogger(FixedFormSelector.class);
  private static final String messageTemplate = "Exception %1$s executing fixed form selection algorithm. Exception error: %2$s";
  private final ItemCandidatesService itemCandidatesService;

  @Autowired
  public FixedFormSelector(final ItemCandidatesService itemCandidatesService) {
    this.itemCandidatesService = itemCandidatesService;
  }

  @Override
  public ItemGroup getNextItemGroup(final ItemCandidatesData itemCandidates) throws ItemSelectionException {
    try {
      return itemCandidatesService.getItemGroup(itemCandidates.getOppkey(),
        itemCandidates.getSegmentKey(),
        itemCandidates.getGroupID(),
        itemCandidates.getBlockID(),
        false);
    } catch (Exception e) {
      String error = String.format(messageTemplate, "Error: ", e.getMessage());
      logger.error(error);
      throw new ItemSelectionException(error);
    }
  }
}
