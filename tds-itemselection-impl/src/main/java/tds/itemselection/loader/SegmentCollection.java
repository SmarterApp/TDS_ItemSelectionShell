/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

//import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;
//import tds.dll.api.LogDBErrorArgs;
import tds.itemselection.api.ItemSelectionException;

/**
 * @author akulakov
 */
/**
 * This class must be "above". And where it must be dependents on how common is
 * this class.
 *  
 */
public class SegmentCollection
{
  private static Logger  _logger  = LoggerFactory.getLogger (SegmentCollection.class);

  static SegmentCollection              instance  = null;
  private static Object                 _syncRoot = new Object ();
  // for non-simulation work
  private Map<String, TestSegment>      _segments;
  //
  // for simulation work where blueprints and itempools change from session to
  // session
  // Each element of sessions is a Map of segments. The key to the
  // element is the database session key
  private Map<UUID, Map<String, TestSegment>> _sessions;
  
//  /**
//   * Another lock
//   */
//  private Object _segmentCollectionLock = new Object();


  private SegmentCollection ()
  {
    this._segments = new HashMap<String, TestSegment> (32);
    this._sessions = new HashMap<UUID, Map<String, TestSegment>> (32);
  }

  public static SegmentCollection getInstance ()
  {
    if (instance == null)
    {
      synchronized (_syncRoot)// needed to lock
      {
        if (instance == null) // else another thread beat me into the lock
        {
          instance = new SegmentCollection ();
        }
      }
    }
    return instance;
  }

  // / <summary>
  // / For the simulator only, remove a session's collection of segments
  // / </summary>
  // / <param name="sessionKey"></param>
  public void RemoveSession (String sessionKey)
  {
    if (_sessions.containsKey (sessionKey))
    {
      _sessions.remove (sessionKey);
    }
  }

  // / <summary>
  // / Return the segment object from the collection, lazy-loaded
  // / </summary>
  // / <param name="sessionKey">Null if not a simulation</param>
  // / <param name="segmentKey">Database key to the test</param>
  // / <param name="loader">The loader object</param>
  // / <returns></returns>
  public TestSegment getSegment (UUID sessionKey, String segmentKey, IItemSelectionDBLoader loader ) throws ItemSelectionException, InterruptedException, ReturnStatusException
  {
    if (sessionKey == null) // permits general use of this method, even for
                            // non-simulation environments
      return getSegment (this._segments, segmentKey, loader, null);

    Map<String, TestSegment> session;
    if (!_sessions.containsKey (sessionKey))
    {
      session = new HashMap<String, TestSegment> ();
      if(!_sessions.containsKey(sessionKey))
      {
    	  _sessions.put (sessionKey, session);
      }
    }

    session = _sessions.get (sessionKey);
    return getSegment (session, segmentKey, loader, sessionKey);

  }

  // / <summary>
  // / Get a test segment. Must provide the loader object for lazy loading.
  // / </summary>
  // / <param name="segmentKey"></param>
  // / <param name="loader"></param>
  // / <returns></returns>
  //@SuppressWarnings ("unused")
  private TestSegment getSegment (Map<String, TestSegment> segments, String segmentKey, IItemSelectionDBLoader loader, UUID sessionKey)
      throws ItemSelectionException, InterruptedException, ReturnStatusException
  {
    TestSegment segment;
    String error = null;
    int waitCnt = 0; // busy waiting for segment to load
    int waitInterval = 100; // milliseconds to wait before checking again
    int waitMax = 600; // maximum number of intervals to wait
    // TODO: (AK) If segmentKey == null !segments.containsKey (segmentKey) throws NullPointerException !
    // What I need todo?
    if (!segments.containsKey (segmentKey))
    {
      // No such segment, so create it and attempt to load it, recognizing that
      // multiple threads may need it simultaneously

      // Another thread may have beat me into the lock, so check again
      if (!segments.containsKey (segmentKey))
      {
        segment = new TestSegment (segmentKey, true); // first time load
                                                      // instantiate shallow
        segments.put (segmentKey, segment);
      }

      segment = segments.get (segmentKey);
      if (!segment.loaded)
      {
        error = segment.load (loader, sessionKey); // the segment enforces its
                                                   // own mutual exclusion on
                                                   // loading
        if (error != null) // only the thread that actually loads the segment
                           // will receive this error
          throw new ItemSelectionException (segment.error);
      }
    }
    
    segment = segments.get (segmentKey);
    if (segment.getRefresh ()) // segments may be refreshed (reloaded)
                               // periodically.
    { // load into a new segment object. The existing segment controls mutual
      // exclusion.
      TestSegment newseg = segment.reload (loader, sessionKey, error);
      // Log the error, but this is not fatal as we still have the old segment
      // data
//      if (error != null)
//      {
//        String message = String.format ("Method: %s with segmentKey: %s failed with error: %s", "getSegment", segmentKey, error);
//        _logger.error (message);
//        LogDBErrorArgs args = new  LogDBErrorArgs(loader.getConnection ()) ;        
//        args.setSessionKey (sessionKey);
//        args.setMsg (message);
//        args.setProcName ("getSegment");
//        // iCommon._LogDBError(LogDBErrorArgs); TODO???
//        _logger.info (args.getMsg ());
//      }
//      else 
    	  if (newseg != null)
      { // NOTE: if newseg == null, then either the new segment failed to load
        // or another thread beat me to it.
        segments.put (segmentKey, newseg);
        return newseg; // can skip the extra checks below since I am the thread
                       // that loaded the new segment
      }
    }
    segment = segments.get (segmentKey);
    // the segment object may exist but may 1) be loading, 2) have experienced
    // an error loading
    while (!segment.loaded && segment.error == null && waitCnt < waitMax)
    {
      ++waitCnt;
      Thread.sleep (waitInterval);
    }
    if (segment.loaded)
      return segment;
    else
      return null; // leave it to the caller to deal with this
  }

}
