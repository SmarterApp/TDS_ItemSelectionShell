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

import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.List;
import java.util.UUID;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.model.OffGradeResponse;

/**
 * Handles data retrieval used for item candidates.  {@link tds.itemselection.base.ItemCandidatesData} are used to determine
 * the next group of items to presented to the user.
 * <p>
 * Interface pulled from {@link tds.itemselection.loader.IItemSelectionDBLoader} without the SQLConnections
 */
public interface ItemCandidatesService {
  /**
   * Retrieves the next {@link tds.itemselection.base.ItemCandidatesData} for an exam
   * If an exam is complete, an ItemCandidatesData with algorithm {@link tds.dll.api.IItemSelectionDLL#SATISFIED}
   * is returned.
   *
   * @param examId the examId
   * @return {@link tds.itemselection.base.ItemCandidatesData}
   * @throws ReturnStatusException if there are any issues
   */
  ItemCandidatesData getItemCandidates(UUID examId) throws ReturnStatusException;

  /**
   * Retrieves all the available item candidates
   * If an exam is complete, the list contains a single ItemCandidatesData with algorithm
   * {@link tds.dll.api.IItemSelectionDLL#SATISFIED}.
   *
   * @param examId the exam item
   * @return the entire list of available {@link tds.itemselection.base.ItemCandidatesData}
   * @throws ReturnStatusException if there are any issues
   */
  List<ItemCandidatesData> getAllItemCandidates(UUID examId) throws ReturnStatusException;

  /**
   * Cleans up the dismissed item candidates.  The system keeps track of which segments have been satisfied.  For certain
   * type of Assessments there are multiple ways to satisfy a segment (for example, MSB).
   *
   * @param selectedSegmentPosition the segment position that has satisfied item candidates
   * @param examId                  the associated exam id
   * @throws ReturnStatusException if there are any issues
   */
  void cleanupDismissedItemCandidates(Long selectedSegmentPosition, UUID examId) throws ReturnStatusException;

  /**
   * Gets the {@link tds.itemselection.base.ItemGroup}
   *
   * @param examId      exam id
   * @param segmentKey  the segment key
   * @param groupID     the group id associated with the segment
   * @param blockID     the block id associeated with the segment
   * @param isFieldTest the field test
   * @return {@link tds.itemselection.base.ItemGroup}
   * @throws ReturnStatusException if there are any issues
   */
  ItemGroup getItemGroup(UUID examId, String segmentKey, String groupID, String blockID, Boolean isFieldTest) throws ReturnStatusException;

  /**
   * Loads the student history for an given exam and segment key
   *
   * @param examId     exam id
   * @param segmentKey segment key
   * @return {@link tds.itemselection.loader.StudentHistory2013} populated with historical student data
   * @throws ItemSelectionException If there are any issues fetching the data
   */
  StudentHistory2013 loadOppHistory(UUID examId, String segmentKey) throws ItemSelectionException;

  /**
   * Determines if the segment is satisfied
   *
   * @param examId          the exam id
   * @param segmentPosition the segment position
   * @param reason          the reason to set the segment to be satisfied
   * @return {@code true} if the segment could be satisfied
   * @throws ReturnStatusException if there are any issues
   */
  boolean setSegmentSatisfied(UUID examId, Integer segmentPosition, String reason) throws ReturnStatusException;

  /**
   * Adds off grade items
   * @param examId exam id
   * @param designation designation
   * @param segmentKey the segment key
   * @return
   * @throws ReturnStatusException
   */
  OffGradeResponse addOffGradeItems(UUID examId, String designation, String segmentKey) throws ReturnStatusException;
}
