/*
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.selectors.impl;

import tds.itemselection.selectors.ItemSelector;

public abstract class AbstractItemSelector implements ItemSelector {
  private String error = null;

  private boolean isSegmentCompleted = false;

  @Override
  public String getItemSelectorError() {
    return error;
  }

  @Override
  public boolean isSegmentCompleted() {
    return isSegmentCompleted;
  }

  void setError(final String error) {
    this.error = error;
  }

  void setSegmentCompleted(final boolean segmentCompleted) {
    isSegmentCompleted = segmentCompleted;
  }
}
