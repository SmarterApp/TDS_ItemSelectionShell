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

import com.google.common.base.Optional;

/**
 * Represents a response when finding items
 * @param <T>
 */
public class ItemResponse<T> {
  private final T responseData;
  private final String errorMessage;
  private final Status responseStatus;

  /**
   * @param responseData the data to be included in the response when there are no errors
   */
  public ItemResponse(final T responseData) {
    this.responseData = responseData;
    this.errorMessage = responseData == null ? "Null response data" : null;
    this.responseStatus = responseData == null ? Status.FAILURE : Status.SUCCESS;
  }

  /**
   * @param errorMessage the error message to explain why there is not data
   */
  public ItemResponse(final String errorMessage) {
    this.responseData = null;
    this.errorMessage = errorMessage;
    this.responseStatus = Status.FAILURE;
  }

  /**
   * @param responseStatus the response status
   */
  public ItemResponse(final Status responseStatus) {
    this.responseData = null;
    this.errorMessage = null;
    this.responseStatus = responseStatus;
  }

  /**
   * @return With contiain T if response doesn't have an error
   */
  public Optional<T> getResponseData() {
    return Optional.fromNullable(responseData);
  }

  /**
   * @return the error message if action could not be processed
   */
  public Optional<String> getErrorMessage() {
    return Optional.fromNullable(errorMessage);
  }

  /**
   * @return the response status
   */
  public Status getResponseStatus() {
    return responseStatus;
  }

  /**
   * Enum of response status.
   */
  public enum Status {
    SUCCESS,  //Indicates a successful response
    SATISFIED, //Indicates there are no additional responses
    FAILURE   //Indicates a failure retrieving the response
  }
}
