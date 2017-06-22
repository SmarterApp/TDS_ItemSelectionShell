/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * @author akulakov
 * Interface of the Item Selection DB Loader
 *
 */
public interface IItemSelectionDBLoader
{
  /**
   * 
   * @param oppkey
   * @return
   * @throws ReturnStatusException
   * @throws SQLException
   */
  public ItemCandidatesData getItemCandidates (SQLConnection connection, UUID oppkey) throws ReturnStatusException, SQLException;

  /**
   *
   * @param oppkey The opportunity key for the call
   * @param connection
   * @return The metadata for all remaining active segments in the assessment
   * @throws ReturnStatusException
   * @throws SQLException
   */
  List<ItemCandidatesData> getAllItemCandidates(SQLConnection connection, UUID oppkey)
          throws ReturnStatusException, SQLException;

  /**
   *
   * @param connection
   * @param selectedSegmentPosition
   * @param oppKey
   */
  void cleanupDismissedItemCandidates(SQLConnection connection, Long selectedSegmentPosition, UUID oppKey) throws ReturnStatusException;
  
  /**
   * 
   * @param oppkey
   * @param segmentKey
   * @param groupID
   * @param blockID
   * @param isFieldTest
   * @return
   * @throws ReturnStatusException
   */
  public ItemGroup getItemGroup (SQLConnection connection, UUID oppkey, String segmentKey, String groupID,
      String blockID, Boolean isFieldTest) throws ReturnStatusException;
   /**
   * @param segmentKey
   * @param seg
   * @param sessionKey
   * @throws ItemSelectionException 
   * @throws ReturnStatusException 
   */
  public void loadSegment (SQLConnection connection, String segmentKey, TestSegment seg, UUID sessionKey) throws ReturnStatusException, ItemSelectionException;

  /**
   * @param oppkey
   * @param segmentKey
   * @return
   * @throws ItemSelectionException 
   */
  public StudentHistory2013 loadOppHistory (SQLConnection connection, UUID oppkey, String segmentKey) throws ItemSelectionException;
  /**
   * 
   * @param oppkey
   * @param segmentPosition
   * @param reason
   * @return
   * @throws ReturnStatusException 
   */
  public boolean setSegmentSatisfied(SQLConnection connection, UUID oppkey, Integer segmentPosition, String reason) throws ReturnStatusException;
  /**
   * @param connection
   * @param oppkey
   * @param designation
   * @param segmentKey
   * @param reason
   * @return
   * @throws ReturnStatusException
   */
  public String addOffGradeItems(SQLConnection connection, UUID oppkey,	String designation, String segmentKey, _Ref<String> reason) throws ReturnStatusException;
}
