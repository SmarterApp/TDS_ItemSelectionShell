/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

import tds.itemselection.api.IItemSelection;

public abstract class AbstractItemSelector implements IItemSelection {

	  protected String     _error         = null;
	  
	  protected Boolean isSegmentCompleted = false;
	  
	  @Override
	  public String getItemSelectorError() {
	  	return _error;
	  }

	  public Boolean isSegmentCompleted()
	  {
		  return isSegmentCompleted;
	  }

}
