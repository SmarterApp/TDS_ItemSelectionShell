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
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.TestSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MsbAssessmentSelectionServiceImpl implements MsbAssessmentSelectionService {

    public MsbAssessmentSelectionServiceImpl() {}

    public MsbAssessmentSelectionServiceImpl(IItemSelectionDBLoader itemSelectionDbLoader) {
        this.itemSelectionDbLoader = itemSelectionDbLoader;
    }

    @Autowired
    @Qualifier("itemDBLoader")
    private IItemSelectionDBLoader itemSelectionDbLoader;

    @Autowired
    @Qualifier ("aa2013Selector")
    private IItemSelection adaptiveSelector;

    public ItemCandidatesData getAdaptiveSegmentKey() {
        return adaptiveSegmentData;
    }

    public void setAdaptiveSegmentKey(ItemCandidatesData adaptiveSegmentKey) {
        this.adaptiveSegmentData = adaptiveSegmentKey;
    }

    private ItemCandidatesData adaptiveSegmentData;

    @Override
    public ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID opportunityKey,
                                                    SegmentCollection2 segmentCollection) throws Exception {
        ArrayList<ItemCandidatesData> itemCandidates = itemSelectionDbLoader.getAllItemCandidates(connection, opportunityKey);
        if(itemCandidates.isEmpty()) return null;
        if(itemCandidates.get(0).getSegmentPosition() == 1) {
            setAdaptiveSegmentKey(itemCandidates.get(0));
            return itemCandidates.get(0);
        }

        List<ItemCandidatesData> filteredItemCandidates = filterItemCandidatesByAlgorithm(itemCandidates, "fixedform");
        List<TestSegment> testSegments = getTestSegmentsForItemCandidates(filteredItemCandidates,
                segmentCollection, connection);
        ItemPool itemPool = buildCombinedItemPool(testSegments);
        ItemGroup itemGroup = adaptiveSelector.getNextItemGroup(connection,
                adaptiveSegmentData, buildCombinedItemGroups(testSegments, itemPool));

        String segmentId = itemGroup.getGroupID();

        ItemCandidatesData calculatedFixedForm = null;
        ArrayList<ItemCandidatesData> rejectedFixedForms = new ArrayList<>();
        for(int i = 0; i < filteredItemCandidates.size(); i++) {
            if(segmentId.compareTo(filteredItemCandidates.get(i).getSegmentKey()) == 0) {
                calculatedFixedForm =  filteredItemCandidates.get(i);
                calculatedFixedForm.setSegmentPosition((long) 2);
            } else {
                rejectedFixedForms.add(filteredItemCandidates.get(i));
            }
        }

        cleanupUnusedSegments(rejectedFixedForms);

        return calculatedFixedForm;
    }

    @Override
    public List<ItemCandidatesData> filterItemCandidatesByAlgorithm(List<ItemCandidatesData> itemCandidates, String filter) {
        ArrayList<ItemCandidatesData> itemCandidatesData = new ArrayList<>();
        for(int i = 0; i < itemCandidates.size(); i++) {
            if(itemCandidates.get(i).getAlgorithm().compareToIgnoreCase(filter) == 0) {
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
        for(int i = 0; i < itemCandidates.size(); i++) {
            TestSegment segment = segmentCollection.getSegment(connection, null,
                    itemCandidates.get(i).getSegmentKey(), itemSelectionDbLoader);
            testSegments.add(segment);
        }
        return testSegments;
    }

    @Override
    public ItemPool buildCombinedItemPool(List<TestSegment> testSegments) {
        ItemPool itemPool = new ItemPool();
        for(int i = 0; i < testSegments.size(); i++) {
            itemPool.addItemgroup(new ItemGroup(testSegments.get(i).getSegmentKey(), -1, -1));
            ArrayList<TestItem> testItems = new ArrayList(testSegments.get(i).getPool().getItems());
            for(int j = 0; j < testItems.size(); j++) {
                TestItem testItem = testItems.get(j);
                testItem.setGroupID(testSegments.get(i).getSegmentKey());
                itemPool.addItem(testItem);
            }
            ArrayList<TestItem> siblingItems = new ArrayList(testSegments.get(i).getPool().getSiblingItems());
            for(int k = 0; k < siblingItems.size(); k++) {
                TestItem siblingItem = siblingItems.get(k);
                siblingItem.setGroupID(testSegments.get(i).getSegmentKey());
                itemPool.addSiblingItem(siblingItem);
            }
        }
        return itemPool;
    }

    @Override
    public List<ItemGroup> buildCombinedItemGroups(List<TestSegment> testSegments, ItemPool itemPool) {
        ArrayList<ItemGroup> itemGroups = new ArrayList<>();
        for(int i = 0; i < testSegments.size(); i++) {
            ItemGroup itemGroup = new ItemGroup();
            // This is the segment key because we need to know what segment was selected
            itemGroup.setGroupID(testSegments.get(i).getSegmentKey());
            itemGroup.setItems(new ArrayList<> (itemPool.getItems()));
            itemGroup.setMaximumNumberOfItems(itemPool.getItems().size());
            itemGroup.setNumberOfItemsRequired(itemPool.getItems().size());
            itemGroup.setNumRequired(itemPool.getItems().size());
            itemGroups.add(itemGroup);
        }
        return itemGroups;
    }

    @Override
    public void cleanupUnusedSegments(List<ItemCandidatesData> testSegments) {

    }
}
