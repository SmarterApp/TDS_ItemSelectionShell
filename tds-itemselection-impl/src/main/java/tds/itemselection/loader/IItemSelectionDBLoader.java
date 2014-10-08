/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

import java.sql.SQLException;
import java.util.UUID;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;

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
  public ItemCandidatesData getItemCandidates (UUID oppkey) throws ReturnStatusException, SQLException;
  
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
  public ItemGroup getItemGroup (UUID oppkey, String segmentKey, String groupID,
      String blockID, Boolean isFieldTest) throws ReturnStatusException;
   /**
   * 
   * @param connection
   * @throws ItemSelectionException
   */
  public void setConnection(SQLConnection connection);

  /**
   * @return
   */
  public SQLConnection getConnection ();

  /**
   * @param segmentKey
   * @param seg
   * @param sessionKey
   * @throws ItemSelectionException 
   * @throws ReturnStatusException 
   */
  public void loadSegment (String segmentKey, TestSegment seg, UUID sessionKey) throws ReturnStatusException, ItemSelectionException;

  /**
   * @param oppkey
   * @param segmentKey
   * @return
   * @throws ItemSelectionException 
   */
  public StudentHistory2013 loadOppHistory (UUID oppkey, String segmentKey) throws ItemSelectionException;
  /**
   * 
   * @param oppkey
   * @param segmentPosition
   * @param reason
   * @return
 * @throws ReturnStatusException 
   */
  public boolean SetSegmentSatisfied(UUID oppkey, Integer segmentPosition, String reason) throws ReturnStatusException;
}
