/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.base;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author akulakov
 * 
 */
public class ItemCandidatesData
{
  
	private static Logger _logger = LoggerFactory.getLogger (ItemCandidatesData.class);

  UUID                  oppkey;
  String                algorithm;
  String                segmentKey;
  String                segmentID;
  Long                  segmentPosition;
  String                groupID;
  String                blockID;
  UUID                  session;
  Boolean               isSimulation = false;

  public String getAlgorithm() {
	return algorithm;
}

public void setAlgorithm(String algorithm) {
	this.algorithm = algorithm;
}

public Boolean getIsSimulation() {
	return isSimulation;
}

public void setIsSimulation(Boolean isSimulation) {
	this.isSimulation = isSimulation;
}

/**
   * @return the oppkey
   */
  public UUID getOppkey () {
    return oppkey;
  }

  /**
   * @param oppkey
   *          the oppkey to set
   */
  public void setOppkey (UUID oppkey) {
    this.oppkey = oppkey;
  }

  /**
   * @return the groupID
   */
  public String getGroupID () {
    return groupID;
  }

  /**
   * @param groupID
   *          the groupID to set
   */
  public void setGroupID (String groupID) {
    this.groupID = groupID;
  }

  /**
   * @return the blockID
   */
  public String getBlockID () {
    return blockID;
  }

  /**
   * @param blockID
   *          the blockID to set
   */
  public void setBlockID (String blockID) {
    this.blockID = blockID;
  }

  /**
   * @return the segmentKey
   */
  public String getSegmentKey () {
    return segmentKey;
  }

  /**
   * @param segmentKey
   *          the segmentKey to set
   */
  public void setSegmentKey (String segmentKey) {
    this.segmentKey = segmentKey;
  }

  /**
   * @return the session
   */
  public UUID getSession () {
    return session;
  }

  /**
   * @param session
   *          the session to set
   */
  public void setSession (UUID session) {
    this.session = session;
  }

  public String getSegmentID() {
		return segmentID;
	}

	public void setSegmentID(String segmentID) {
		this.segmentID = segmentID;
	}

	public Long getSegmentPosition() {
		return segmentPosition;
	}

	public void setSegmentPosition(Long segmentPosition) {
		this.segmentPosition = segmentPosition;
	}


  /**
   * @param oppkey
   * @param algorithm
   * @param segmentKey
   * @param segmentID
   * @param segmentPosition
   * @param groupID
   * @param blockID
   * @param itempool
   * @param session
   * @param isSimulation
   */
  public ItemCandidatesData (UUID oppkey,
      String algorithm,
      String segmentKey,
      String segmentID,
      Integer segmentPosition,
      String groupID,
      String blockID,
      UUID session,
      Boolean isSimulation) {
    super ();
    this.oppkey = oppkey;
    this.algorithm = algorithm;
    this.segmentKey = segmentKey;
    this.segmentID = segmentID;
    this.segmentPosition = new Long (segmentPosition);
    this.groupID = groupID;
    this.blockID = blockID;
    this.session = session;
    this.isSimulation = isSimulation;
  }

  public ItemCandidatesData (UUID oppkey,
      String algorithm,
      String segmentKey,
      String segmentID,
      Long segmentPosition,
      String groupID,
      String blockID,
      UUID session,
      Boolean isSimulation) {
    super ();
    this.oppkey = oppkey;
    this.algorithm = algorithm;
    this.segmentKey = segmentKey;
    this.segmentID = segmentID;
    this.segmentPosition = segmentPosition;
    this.groupID = groupID;
    this.blockID = blockID;
    this.session = session;
    this.isSimulation = isSimulation;
  }

  public ItemCandidatesData (UUID oppkey,
      String algorithm)
  {
    super ();
    this.oppkey = oppkey;
    this.algorithm = algorithm;
  }
  /**
   * 
   */
  public void dumpItemCandidatesData () throws ReturnStatusException {
	    System.out.println ();
	    _logger.info (String.format ("oppkey: %s", (this.oppkey != null)?this.oppkey.toString (): null));
	    _logger.info (String.format ("algorithm: %s", this.algorithm));
	    _logger.info (String.format ("segmentKey: %s", this.segmentKey));
	    _logger.info (String.format ("segmentID: %s", this.segmentID));
	    _logger.info (String.format ("segmentPosition: %d", this.segmentPosition));
	    _logger.info (String.format ("groupID: %s", this.groupID));
	    _logger.info (String.format ("blockID: %s", this.blockID));
	    _logger.info (String.format ("session: %s", (this.session != null)?this.session.toString (): null));
	    _logger.info (String.format ("isSimulation: %b", (this.isSimulation != null)?this.isSimulation: null));
	    System.out.println ();
	  }
  public void dumpDebugItemCandidatesData () throws ReturnStatusException {
	    System.out.println ();
	    System.out.println (String.format ("oppkey: %s", (this.oppkey != null)?this.oppkey.toString (): null));
	    System.out.println (String.format ("algorithm: %s", this.algorithm));
	    System.out.println (String.format ("segmentKey: %s", this.segmentKey));
	    System.out.println (String.format ("segmentID: %s", this.segmentID));
	    System.out.println (String.format ("segmentPosition: %d", this.segmentPosition));
	    System.out.println (String.format ("groupID: %s", this.groupID));
	    System.out.println (String.format ("blockID: %s", this.blockID));
	    System.out.println (String.format ("session: %s", (this.session != null)?this.session.toString (): null));
	    System.out.println (String.format ("isSimulation: %b", (this.isSimulation != null)?this.isSimulation: null));
	    System.out.println ();
	  }

  public ItemCandidatesData () {
    // TODO Auto-generated constructor stub
  }
}
