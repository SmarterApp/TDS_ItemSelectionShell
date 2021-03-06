/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/

package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
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
     * @param segmentCollection The cached test segments
     * @return Returns the ItemCandidatesData object that represents the fixed form selected
     * @throws Exception
     */
    ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID opportunityKey,
                                             SegmentCollection2 segmentCollection) throws Exception;

    /**
     * Generally this filter parameter will be "fixedform"
     *
     * @param itemCandidates Metadata about all remaining segments in the assessment
     * @param filter The algorithm type to filter on
     * @return Filtered segment metadata (ItemCandidatesData) based on algorithm type
     */
    List<ItemCandidatesData> filterItemCandidatesByAlgorithm(List<ItemCandidatesData> itemCandidates, String filter);

    /**
     * This will weed out any inactive segments
     *
     * @param itemCandidates Metadata about all remaining segments in the assessment
     * @param isActive Whether we want the active segments, or the inactive ones
     * @return Filtered segment metadata (ItemCandidatesData) based on isActive
     */
    List<ItemCandidatesData> filterItemCandidatesByActive(List<ItemCandidatesData> itemCandidates, boolean isActive);

    /**
     *
     * @param itemCandidates Metadata about all active remaining segments in the assessment
     * @param segmentCollection The cached test segments
     * @param connection
     * @return Retrieves the fully actualized test segment objects from their metadata
     * @throws Exception
     */
    List<TestSegment> getTestSegmentsForItemCandidates(List<ItemCandidatesData> itemCandidates,
                                                       SegmentCollection2 segmentCollection,
                                                       SQLConnection connection) throws Exception;

    /**
     * Produces a list of item groups representing each of the distinct segment candidates. Each group will contain all
     * questions belonging to the segment.
     *
     * @param testSegments
     * @return A list of item groups
     */
    List<ItemGroup> buildCombinedItemGroups(List<TestSegment> testSegments);

}
