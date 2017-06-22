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

package tds.itemselection.services;

import java.util.UUID;

import tds.itemselection.base.ItemCandidatesData;

public interface MsbAssessmentSelectionService {
  /**
   * Convenience method for returning the selected item candidate using the existing AdaptiveSelector2013 code. Calls
   * many of the methods below in proper sequence and includes some validation checks.
   *
   * @param opportunityKey
   * @param segmentCollection The cached test segments
   * @return Returns the ItemCandidatesData object that represents the fixed form selected
   * @throws Exception
   */
  ItemCandidatesData selectFixedMsbSegment(UUID opportunityKey) throws Exception;
}
