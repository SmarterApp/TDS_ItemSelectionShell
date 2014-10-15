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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import AIR.Common.DB.SQLConnection;

public class SegmentCollection2 {
	/**
	 * Singleton instance
	 */
 volatile private static SegmentCollection2 instance;

 /**
  * Lock to serialize access to singleton
  */
 private static Object _syncRoot = new Object();

 /**
  * Set of segments for non-simulation work 
  */
 private Map<String, TestSegment> _segments = new HashMap<String, TestSegment>();

 /**
  * For simulation work where blueprints and item pools change from session to session
  * Each element of sessions is a hash table of segments. The key to the element is the
  * database session key
  */
 private HashMap<UUID, HashMap<String, TestSegment>> _sessions = new HashMap<UUID, HashMap<String, TestSegment>>();

 /**
  * Another lock
  */
 private Object _segmentCollectionLock = new Object();

 /**
  * Singleton access method
  * @return
  */
 public static SegmentCollection2 getInstance()
 {
     if (instance == null)
     {
         synchronized(_syncRoot) 
         {
             if (instance == null)   // Double Checked Locking 
             {
                 instance = new SegmentCollection2();
             }
         }
     }
     return instance;
 }

 /**
  * Return the segment object from the collection, lazy-loaded
  * @param segmentKey
  * @param loader
  * @return
  */
 public TestSegment getSegment(SQLConnection connection, String segmentKey, IItemSelectionDBLoader loader) throws SQLException, Exception
 {
     return _getSegment(connection, null, segmentKey, _segments, loader);
 }

 /**
  * Return the segment object from the collection, lazy-loaded
  * @param sessionKey
  * @param segmentKey
  * @param loader
  * @return
  */
 public TestSegment getSegment(SQLConnection connection, UUID sessionKey, String segmentKey, IItemSelectionDBLoader loader)  throws SQLException, Exception
 {
     if (sessionKey == null) // permits general use of this method, even for non-simulation environments
         return getSegment(connection, segmentKey, loader);
     if (!_sessions.containsKey(sessionKey))
     {
         synchronized (_segmentCollectionLock)
         {
             if (!_sessions.containsKey(sessionKey))
             {
                 _sessions.put(sessionKey, new HashMap<String, TestSegment>());
             }
         }
     }
     Map<String, TestSegment> session = _sessions.get(sessionKey);
     return _getSegment(connection, sessionKey, segmentKey, session, loader);
 }

 /**
  * For the simulator only, remove a session's collection of segments
  * @param sessionKey
  */
 public void RemoveSession(String sessionKey)
 {
     if (_sessions.containsKey(sessionKey))
     {
         synchronized (_segmentCollectionLock)
         {
             if (_sessions.containsKey(sessionKey))
             {
                 _sessions.remove(sessionKey);
             }
         }
     }
 }

 /**
  * Internal method to get test segment
  * @param sessionKey
  * @param segmentKey
  * @param segments
  * @param loader
  * @return
  */
 private TestSegment _getSegment(SQLConnection connection, UUID sessionKey, String segmentKey, 
 		Map<String, TestSegment> segments, IItemSelectionDBLoader loader) throws SQLException, Exception
 {
     if (!segments.containsKey(segmentKey))
     {   
         synchronized (_segmentCollectionLock)   
         {
             if (!segments.containsKey(segmentKey))
             {
                 // Add a new segment and load it
                 TestSegment segment = new TestSegment(segmentKey);
                 segment.Load(connection, sessionKey, loader);
                 segments.put(segmentKey, segment);
             }
             else
             {
                 // Check if the existing segment requires refresh, if so, create and load a new segment
                 // Assign it to the dictionary for later use (The current segment in use is not disturbed)
                 TestSegment testSegment = segments.get(segmentKey);
                 if (testSegment.RequireRefresh())
                 {
                     TestSegment segment = new TestSegment(segmentKey);
                     segment.Load(connection, sessionKey, loader);
                     segments.put(segmentKey, segment);
                 }
             }
         }
     }
     return segments.get(segmentKey);
 }


}
