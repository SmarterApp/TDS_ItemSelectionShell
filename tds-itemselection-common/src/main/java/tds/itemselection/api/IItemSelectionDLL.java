/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.api;

import java.util.UUID;

import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import AIR.Common.DB.SQLConnection;

/**
 * @author akulakov
 *
 */
public interface IItemSelectionDLL
{
  /**
   * 
   * @param connection
   * @param oppkey
   * @param algorithmValue
   * @param segmentValue
   * @param segmentKeyValue
   * @param segmentIDValue
   * @param groupIDValue
   * @param blockIDValue
   * @param itempoolValue
   * @param sessionValue
   * @param isSimulationValue
   * @throws ReturnStatusException
   */
  public void AA_GetNextItemCandidates_SP (SQLConnection connection, UUID oppkey,
      _Ref<String> algorithmValue,
      _Ref<Integer> segmentValue, // = segmentPosition
      _Ref<String> segmentKeyValue,
      _Ref<String> segmentIDValue,
      _Ref<String> groupIDValue,
      _Ref<String> blockIDValue,
      _Ref<String> itempoolValue,
      _Ref<UUID> sessionValue,
      _Ref<Boolean> isSimulationValue
      ) throws ReturnStatusException;
 
  /**
   * 
   * @param connection
   * @param oppkey
   * @param segmentKey
   * @param language
   * @param groupIDValue
   * @param blockIDValue
   * @throws ReturnStatusException
   */
  public void _AA_NextFixedformGroup_SP (SQLConnection connection, UUID oppkey,
      String segmentKey, String language,
      _Ref<String> groupIDValue,
      _Ref<String> blockIDValue) throws ReturnStatusException;

  /**
   * 
   * @param connection
   * @param oppkey
   * @param segmentPosition
   * @param segmentKey
   * @param segmentID
   * @param language
   * @param groupID
   * @param blockID
   * @param debug
   * @throws ReturnStatusException
   */
  public void _AA_NextFieldtestGroup_SP (SQLConnection connection, UUID oppkey,
	      int segmentPosition,
	      String segmentKey,
	      String segmentID,
	      String language,
	      _Ref<String> groupID,
	      _Ref<String> blockID,
	      boolean debug) throws ReturnStatusException;
  /**
   * 
   * @param connection
   * @param oppkey
   * @return
   * @throws ReturnStatusException
   */
  public boolean IsSimulation_FN (SQLConnection connection, UUID oppkey) throws ReturnStatusException;
  
  /**
   * 
   * @param connection
   * @param oppkey
   * @param segment
   * @return
   * @throws ReturnStatusException
   */
  public boolean _AA_IsSegmentSatisfied_FN (SQLConnection connection, UUID oppkey, 
      Integer segment) throws ReturnStatusException;
  
  /**
   * 
   * @param connection
   * @param oppkey
   * @param segmentKey
   * @param segmentID
   * @param groupID
   * @param blockID
   * @return
   * @throws ReturnStatusException
   */
  public SingleDataResultSet AF_GetItempool_FN (SQLConnection connection, UUID oppkey,
      String segmentKey, 
      String segmentID,
      Boolean fieldTest,
      String groupID,
      String blockID) throws ReturnStatusException; 

}
