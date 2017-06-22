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
 * The available algorithm types that are supported by item selection
 */
public enum AlgorithmType {
  FIXED_FORM("fixedform"),
  FIELD_TEST("fieldtest"),
  ADAPTIVE2("adaptive2"),
  ADAPTIVE("adaptive"),
  SATISFIED("satisfied");

  private final String type;

  AlgorithmType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static AlgorithmType fromType(String algorithmType) {
    if (algorithmType == null) throw new IllegalArgumentException("The algorithm type cannot be null");

    for (AlgorithmType algorithm : values()) {
      if (algorithmType.equalsIgnoreCase(algorithm.getType())) {
        return algorithm;
      }
    }
    // No Algorithm found for algorithm type
    throw new IllegalArgumentException(String.format("No Algorithm found with the name %s", algorithmType));
  }
}
