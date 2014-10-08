/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.api;

/**
 * @author akulakov
 * 
 */
public class ItemSelectionException extends Exception
{

  /**
	 * 
	 */
  private static final long serialVersionUID = 6089317144003548557L;

  public ItemSelectionException () {
    // TODO Auto-generated constructor stub. (AK) make specific
    super ("Exception accured in ItemSeleciton pacage");
  }

  public ItemSelectionException (String message) {
    super (message);
  }

  public ItemSelectionException (Throwable cause) {
    super (cause);
  }

  public ItemSelectionException (String message, Throwable cause) {
    super (message, cause);
  }

  public ItemSelectionException (String message, Throwable cause,
      boolean enableSuppression, boolean writableStackTrace) {
    super (message, cause, enableSuppression, writableStackTrace);
    // TODO Auto-generated constructor stub
  }

}
