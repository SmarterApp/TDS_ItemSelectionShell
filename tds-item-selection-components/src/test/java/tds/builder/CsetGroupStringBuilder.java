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

package tds.builder;

import java.util.UUID;

import tds.itemselection.impl.sets.CsetGroupString;

public final class CsetGroupStringBuilder {
  // chronological order amongst all elements in a collection
  private int                           sequence;
  // the opportunity key from which the itemgroups came
  private UUID oppkey;
  private String groupString;

  private CsetGroupStringBuilder() {
  }

  public static CsetGroupStringBuilder aCsetGroupString() {
    return new CsetGroupStringBuilder();
  }

  public CsetGroupStringBuilder withSequence(int sequence) {
    this.sequence = sequence;
    return this;
  }

  public CsetGroupStringBuilder withOppkey(UUID oppkey) {
    this.oppkey = oppkey;
    return this;
  }

  public CsetGroupStringBuilder withGroupString(String groupString) {
    this.groupString = groupString;
    return this;
  }

  public CsetGroupString build() {
    return new CsetGroupString(oppkey, sequence, groupString);
  }
}
