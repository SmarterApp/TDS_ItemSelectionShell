package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.TestSegment;

import java.util.List;
import java.util.UUID;

/**
 * This class handles the logic behind the selection of Multi-Stage Braille (MSB) assessments. MSB assessments
 * (currently) consist of an adaptive segment followed by a fixed form segment. The fixed segment is not known at
 * the start of the assessment - it must be calculated based on the student's performance in the initial adaptive
 * section. This class accomplishes this selection by creating an artificial segment made up of item groups representing
 * the entire pool of each of the segments. It passes these item groups into the existing adaptive selection algorithm
 * and uses the result to determine which fixed form test is appropriate for the MSB student being assessed.
 *
 * @see TestSegment
 * @see ItemGroup
 * @see ItemCandidatesData
 */
public interface MsbAssessmentSelectionService {

    /**
     * Convenience method for returning the selected item candidate using the existing AdaptiveSelector2013 code. Calls
     * many of the methods below in proper sequence and includes some validation checks.
     *
     * @param connection
     * @param opportunityKey
     * @param segmentCollection
     * @return Returns the ItemCandidatesData object that represents the fixed form selected
     * @throws Exception
     */
    ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID opportunityKey,
                                             SegmentCollection2 segmentCollection) throws Exception;

    /**
     * Generally this filter parameter will be "fixedform"
     *
     * @param itemCandidates
     * @param filter
     * @return Filtered segment metadata (ItemCandidatesData) based on algorithm type
     */
    List<ItemCandidatesData> filterItemCandidatesByAlgorithm(List<ItemCandidatesData> itemCandidates, String filter);

    /**
     *
     * @param itemCandidates
     * @param segmentCollection
     * @param connection
     * @return Retrieves the fully actualized test segment objects from their metadata
     * @throws Exception
     */
    List<TestSegment> getTestSegmentsForItemCandidates(List<ItemCandidatesData> itemCandidates,
                                                       SegmentCollection2 segmentCollection,
                                                       SQLConnection connection) throws Exception;

    /**
     * //TODO: GREG - determine if we need a full test pool object, or only a subset of this functionality
     *
     * @param testSegments
     * @return A full test item pool for msb segments
     */
    ItemPool buildCombinedItemPool(List<TestSegment> testSegments);

    /**
     * Produces a list of item groups representing each of the distinct segment candidates. Each group will contain all
     * questions belonging to the segment.
     *
     * @param testSegments
     * @param itemPool
     * @return A list of item groups
     */
    List<ItemGroup> buildCombinedItemGroups(List<TestSegment> testSegments, ItemPool itemPool);

    /**
     * Puts test into a state where it will end after the selected fixed form completes
     * @param testSegments
     */
    void cleanupUnusedSegments(List<ItemCandidatesData> testSegments);

    // TODO: GREG - Write repository method to go to the db and query for opportunity test segment open and position 2.

}
