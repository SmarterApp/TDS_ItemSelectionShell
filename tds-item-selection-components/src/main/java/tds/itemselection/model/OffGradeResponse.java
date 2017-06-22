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

package tds.itemselection.model;

/**
 * Response when adding off grades
 */
public class OffGradeResponse {
  public static final String SUCCESS = "success";
  public static final String FAILED = "failed";

  private final String status;
  private final String reason;

  public OffGradeResponse(final String status, final String reason) {
    this.status = status;
    this.reason = reason;
  }

  /**
   * @return either the constant SUCCESS or FAILED
   */
  public String getStatus() {
    return status;
  }

  /**
   * @return the reason it was successful or failed
   */
  public String getReason() {
    return reason;
  }
}
