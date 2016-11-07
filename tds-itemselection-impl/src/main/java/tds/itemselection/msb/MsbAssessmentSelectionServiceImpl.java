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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.TestSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MsbAssessmentSelectionServiceImpl implements MsbAssessmentSelectionService {

    public MsbAssessmentSelectionServiceImpl() {
    }

    public MsbAssessmentSelectionServiceImpl(IItemSelectionDBLoader itemSelectionDbLoader) {
        this.itemSelectionDbLoader = itemSelectionDbLoader;
    }

    private final String FIXED_ALGORITHM = "fixedform";

    @Autowired
    @Qualifier("itemDBLoader")
    private IItemSelectionDBLoader itemSelectionDbLoader;

    @Autowired
    @Qualifier("aa2013Selector")
    private IItemSelection adaptiveSelector;

    /* TODO: Pass in all of the segments (including inactive ones) and execute the second portion based on whether or
     * not the first segment is active, not whether or not it is present
     */
    @Override
    public ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID opportunityKey,
                                                    SegmentCollection2 segmentCollection) throws Exception {
        List<ItemCandidatesData> itemCandidates =
                itemSelectionDbLoader.getAllItemCandidates(connection, opportunityKey);
        if (itemCandidates.isEmpty()) return null;
        if (itemCandidates.get(0).getSegmentPosition() == 1) { //TODO: replace this logical statement to check whether it is active
            return itemCandidates.get(0);
        }

        // TODO REMOVE
        ItemCandidatesData adaptiveSegmentData = new ItemCandidatesData();

        List<ItemCandidatesData> filteredItemCandidates = filterItemCandidatesByAlgorithm(itemCandidates, FIXED_ALGORITHM);
        List<TestSegment> testSegments = getTestSegmentsForItemCandidates(filteredItemCandidates,
                segmentCollection, connection);
        ItemGroup itemGroup = adaptiveSelector.getNextItemGroup(connection,
                adaptiveSegmentData, buildCombinedItemGroups(testSegments));

        String segmentId = itemGroup.getGroupID();

        int index = 0;
        for (int i = 0; i < filteredItemCandidates.size(); i++) {
            if (segmentId.compareTo(filteredItemCandidates.get(i).getSegmentKey()) == 0) {
                index = i;
                break;
            }
        }
        ItemCandidatesData calculatedFixedForm = filteredItemCandidates.get(index);

        cleanupUnusedSegments(connection, calculatedFixedForm.getSegmentPosition(), opportunityKey);

        return calculatedFixedForm;
    }

    @Override
    public List<ItemCandidatesData> filterItemCandidatesByAlgorithm(List<ItemCandidatesData> itemCandidates, String filter) {
        ArrayList<ItemCandidatesData> itemCandidatesData = new ArrayList<>();
        for (int i = 0; i < itemCandidates.size(); i++) {
            if (itemCandidates.get(i).getAlgorithm().compareToIgnoreCase(filter) == 0) {
                itemCandidatesData.add(itemCandidates.get(i));
            }
        }
        return itemCandidatesData;
    }

    @Override
    public List<TestSegment> getTestSegmentsForItemCandidates(List<ItemCandidatesData> itemCandidates,
                                                              SegmentCollection2 segmentCollection,
                                                              SQLConnection connection) throws Exception {
        ArrayList<TestSegment> testSegments = new ArrayList<>();
        for (int i = 0; i < itemCandidates.size(); i++) {
            TestSegment segment = segmentCollection.getSegment(connection, null,
                    itemCandidates.get(i).getSegmentKey(), itemSelectionDbLoader);
            testSegments.add(segment);
        }
        return testSegments;
    }

    @Override
    public List<ItemGroup> buildCombinedItemGroups(List<TestSegment> testSegments) {
        List<ItemGroup> itemGroups = new ArrayList<>();
        for (int i = 0; i < testSegments.size(); i++) {
            ItemGroup itemGroup = new ItemGroup();
            // The group ID is being set to the segment key because we need to know what segment was selected later
            itemGroup.setGroupID(testSegments.get(i).getSegmentKey());
            ArrayList<TestItem> groupItems = new ArrayList<>();
            ArrayList<TestItem> segmentItems = new ArrayList(testSegments.get(i).getPool().getItems());
            for (int j = 0; j < segmentItems.size(); j++) {
                TestItem testItem = segmentItems.get(j);
                testItem.setGroupID(testSegments.get(i).getSegmentKey());
                groupItems.add(testItem);
            }
            itemGroup.setItems(groupItems);
            itemGroup.setMaximumNumberOfItems(groupItems.size());
            itemGroup.setNumberOfItemsRequired(groupItems.size());
            itemGroup.setNumRequired(groupItems.size());
            itemGroups.add(itemGroup);
        }
        return itemGroups;
    }

    @Override
    public void cleanupUnusedSegments(SQLConnection connection, Long selectedSegmentPosition, UUID opportunityKey) {
        itemSelectionDbLoader.cleanupDismissedItemCandidates(connection, selectedSegmentPosition, opportunityKey);
    }
}
