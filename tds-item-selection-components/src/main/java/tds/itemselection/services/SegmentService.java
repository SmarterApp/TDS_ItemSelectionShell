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

import tds.itemselection.loader.TestSegment;

/**
 * Replaces the need to use {@link tds.itemselection.loader.SegmentCollection2}
 */
public interface SegmentService {

  /**
   * Gets a {@link tds.itemselection.loader.TestSegment}
   * <p>
   * Implementation exists in {@link tds.itemselection.loader.SegmentCollection2}
   *
   * @param sessionKey the session key
   * @param segmentKey the segment key
   * @return a {@link tds.itemselection.loader.TestSegment}
   * @throws Exception if there are issues building the test segment
   * @throws Exception
   */
  TestSegment getSegment(UUID sessionKey, String segmentKey) throws Exception;

  /**
   * Gets a {@link tds.itemselection.loader.TestSegment}
   * <p>
   * Implementation exists in {@link tds.itemselection.loader.SegmentCollection2}
   *
   * @param segmentKey the segment key
   * @return a {@link tds.itemselection.loader.TestSegment}
   * @throws Exception if there are issues building the test segment
   */
  TestSegment getSegment(String segmentKey) throws Exception;
}
