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
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.ItemPool;

/**
 * @author akulakov
 * 
 */
/**
 * All test segment information for adaptive item selection
 */
public class TestSegment
{
  private static Logger  _logger  = LoggerFactory.getLogger (TestSegment.class);

  public TestSegment (String segKey, boolean shallow)
  {
    segmentKey = segKey;
    if (!shallow)
    {
      segmentBlueprint = new Blueprint ();
      segmentItemPool = new ItemPool ();
    }
  }

  public TestSegment(String segmentKey) {
	this.segmentKey = segmentKey;
}

  //===========Common members for all Algorithms==============================
  /**
   * Key for the segment 
   */
  public String segmentKey;
  /**
   * How often this segment need to be refreshed
   */
  public Integer refreshMinutes = 1000000;     
  // how often should this object be reloaded from the database? (AK) it
  // is equal near 2 years!
	/**
	 * Last time this segment was loaded
	 */
  private Date _lastLoadTime = null;
  /**
   * Is same as segmentKey for unsegmented tests
   */
  public String parentTest;
  /**
   * Position of this segment
   */
  public Integer position;
  
  /**
   * Blue print for this segment
   */
  public Blueprint segmentBlueprint = new Blueprint();

  /**
   * Item pool for this segment
   */
  public ItemPool segmentItemPool = new ItemPool();
 

//========================================================================

  public volatile boolean  loaded         = false;        // make sure all
                                                           // threads have
                                                           // up-to-date on this
                                                           // variable
  private volatile boolean loading        = false;
  public String            error          = null;
  private boolean          refresh        = false;

  // TODO: (AK)

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
   * @return the _bp
   */
  public Blueprint getBp () {
    return segmentBlueprint;
  }

  /**
   * @param _bp
   *          the _bp to set
   */
  public void setBp (Blueprint bp) {
    this.segmentBlueprint = bp;
  }

  /**
   * @return the _pool
   */
  public ItemPool getPool () {
    return segmentItemPool;
  }

  /**
   * @param _pool
   *          the _pool to set
   */
  public void setPool (ItemPool pool) {
    this.segmentItemPool = pool;
  }

  // / <summary>
  // / Is it time for the segment data to be refreshed?
  // / </summary>
  public boolean getRefresh ()
  {
    //TODO
    //return loaded && lastLoaded.getMinutes () + (refreshMinutes) < (new Date ()).getMinutes ();
    return true;
  }

  // / <summary>
  // / In situation Lazy loader for shared segment. Locks against race
  // conditions.
  // / </summary>
  // / <param name="loader"></param>
  // / <returns></returns>
  public String load (SQLConnection connection, IItemSelectionDBLoader loader, UUID sessionKey)
  {
    // This object is shared so the method is subject to race conditions. Ensure
    // that at most one thread is loading this at a time
    if (loaded)
      return null;
    synchronized (this)
    {
      if (!loaded && !loading)
        try
        {
          error = null; // this may have been a retry from a previous error
          loading = true;
          TestSegment seg = new TestSegment (segmentKey, false); // temporary
                                                                 // segment,
                                                                 // instantiate
                                                                 // deep
          // Load the segment here
          // In situation means that data in this object will be altered by the
          // loader. (Reload, below, loads an independent object so is not in
          // situ)
          loader.loadSegment (connection, segmentKey, seg, sessionKey);
          this.segmentBlueprint = seg.segmentBlueprint;
          this.segmentItemPool = seg.segmentItemPool;
          this.refreshMinutes = seg.refreshMinutes;
          this.parentTest = seg.parentTest;
          this.position = seg.position;
          loaded = true;
          loading = false;
          _lastLoadTime = new Date ();
        } catch (Exception e)
        {
          // TODO: (AK) Do I need to add throwing of ItemSelectExeption?
          loading = false; // this permits repeated attempts at loading
          error = e.getMessage (); // signals to all that loading failed, until
                                   // someone succeeds
          return e.getMessage ();
        }
    }
    return null;
  }

  // / <summary>
  // / Reload the segment data without disturbing threads using the existing
  // data
  // / </summary>
  // / <param name="loader"></param>
  // / <returns></returns>
  public TestSegment reload (SQLConnection connection, IItemSelectionDBLoader loader, UUID sessionKey, String error)
  {
    // unlike the first-time loader, return the object loaded so it can be
    // replace this object in the collection
    // That will allow threads that are working with the original object to
    // complete their task without disruption
    // The new object will be used by threads that come along after.
    if (loading)
      return null; // another thread got here first
    synchronized (this)
    {
      if (refresh) // else another thread got here first, so exit without
                   // loading
        try
        {
          loading = true;
          // potential new segment, instantiate deep because loader needs the
          // objects
          TestSegment seg = new TestSegment (segmentKey, false);
          // Load the segment here // this is main  command!
          loader.loadSegment (connection, segmentKey, seg, sessionKey);
          seg.loaded = true;
          loading = false;
          seg._lastLoadTime = _lastLoadTime = new Date ();
          // important to set my lastLoaded timestamp to prevent threads that
          // were waiting at the lock from reloading again
          return seg; // this will replace my object in the collection
        } catch (Exception e)
        {
          // TODO: (AK) Do I need to add throwing of ItemSelectExeption?
          loading = false;
          error = e.getMessage (); // not fatal since we still have the original
                                   // segment data. Return error but don't log
                                   // in 'this'
          return null;
        }
    }
    return null;
  }
  //====================================================================
	
 
  /**
   * Checks whether this segment need to be refreshed or not
   * @return
   */
  public Boolean RequireRefresh()
  {
  	if (_lastLoadTime == null)
  		return true;    	
  	Date sumWithRefreshMinutes=new Date(_lastLoadTime.getTime() + 
  			((refreshMinutes == null ? 1000000 : refreshMinutes) * 60000));    	
      return (sumWithRefreshMinutes.compareTo(new Date()) < 0) ? true : false ;
  }

  /**
   * Load the segment - session key is null for real data 
   * @param sessionKey
   * @param loader
   */
  public synchronized void Load(SQLConnection connection, UUID sessionKey, IItemSelectionDBLoader loader) throws SQLException, Exception
  {
		loader.loadSegment(connection, this.segmentKey, this, sessionKey);
		_lastLoadTime = new Date();
  }
  
  /**
   * Initialize the segment with data from database
   * @param rs
   * @throws SQLException
   */
  public void initializeOverallBluePrint(SingleDataResultSet res) throws SQLException
  {
	// Only one record for the segment
	  DbResultRecord record;
      record = res.getCount () > 0 ? res.getRecords ().next () : null;
      if (record != null) {
    	Long tmp = record.<Long> get ("refreshMinutes");
        refreshMinutes = new Integer(tmp.toString());
        parentTest = record.<String> get ("ParentTest");
        tmp = record.<Long> get ("segmentPosition");
        position = new Integer(tmp.toString());
        segmentBlueprint.initializeOverallBluePrint(res);
      }
  }   

  }
