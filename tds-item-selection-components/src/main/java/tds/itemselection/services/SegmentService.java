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
