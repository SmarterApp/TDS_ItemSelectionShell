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

package msb;

import AIR.Common.DB.SQLConnection;
import builders.*;
import org.junit.*;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.msb.MsbAssessmentSelectionServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MsbAssessmentSelectionServiceTest {

    private MsbAssessmentSelectionServiceImpl msbAssessmentSelectionService;
    private IItemSelectionDBLoader itemSelectionDbLoader;

    @Before
    public void setup() {
        itemSelectionDbLoader = mock(IItemSelectionDBLoader.class);
        msbAssessmentSelectionService = new MsbAssessmentSelectionServiceImpl(itemSelectionDbLoader);
    }

    @After
    public void tearDown() {}

    private List<ItemCandidatesData> retrieveItemCandidatesData() {
        ArrayList<ItemCandidatesData> itemCandidatesData = new ArrayList<>();
        ItemCandidatesDataBuilder itemCandidateDataBuilder = new ItemCandidatesDataBuilder();

        ItemCandidatesData candidate1 = itemCandidateDataBuilder
                .build();
        itemCandidatesData.add(candidate1);

        ItemCandidatesData candidate2 = itemCandidateDataBuilder
                .withSegmentKey("(SBAC_PT)SBAC-MSB-IRP-CAT-NoCalc-MATH-7-Summer-2015-2016")
                .withSegmentID("SBAC-MSB-IRP-CAT-NoCalc-MATH-7")
                .withSegmentPosition(2)
                .build();
        itemCandidatesData.add(candidate2);

        ItemCandidatesData candidate3 = itemCandidateDataBuilder
                .withAlgorithm("fixedform")
                .withSegmentKey("(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-7-Summer-2015-2016")
                .withSegmentID("SBAC-MSB-IRP-Perf-MATH-7")
                .withSegmentPosition(3)
                .build();
        itemCandidatesData.add(candidate3);

        ItemCandidatesData candidate4 = itemCandidateDataBuilder
                .withAlgorithm("fixedform")
                .withSegmentKey("(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-11-Summer-2015-2016")
                .withSegmentID("SBAC-MSB-IRP-Perf-MATH-11")
                .withSegmentPosition(4)
                .build();
        itemCandidatesData.add(candidate4);

        return itemCandidatesData;
    }

    private List<TestSegment> retrieveTestSegmentData () {
        ArrayList<TestSegment> testSegments = new ArrayList<>();

        TestSegmentBuilder testSegmentBuilder = new TestSegmentBuilder();
        BlueprintBuilder blueprintBuilder = new BlueprintBuilder();
        ItemPoolBuilder itemPoolBuilder = new ItemPoolBuilder();
        ItemGroupBuilder itemGroupBuilder = new ItemGroupBuilder();
        TestItemBuilder testItemBuilder = new TestItemBuilder();

        ArrayList<TestItem> testItems1 = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            testItems1.add(testItemBuilder.build());
        }
        ItemGroup itemGroup1 = itemGroupBuilder
                .withItems(testItems1)
                .build();
        List<ItemGroup> itemGroups1 = new ArrayList<>();
        itemGroups1.add(itemGroup1);
        ItemPool itemPool1 = itemPoolBuilder
                .withItemGroups(itemGroups1)
                .withItems(testItems1)
                .withSiblingItems(testItems1)
                .build();

        TestSegment testSegment1 = testSegmentBuilder.build();

        return testSegments;
    }

    //region filterItemCandidates tests

    @Test
    public void filterItemCandidatesWithFixedFormFilterShouldReturnTwoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidatesByAlgorithm(itemCandidatesData, "fixedform");

        // Assert
        Assert.assertTrue(filterredItemCandidates.size() == 2);
        Assert.assertTrue(filterredItemCandidates.get(0).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(1).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(0).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-7-Summer-2015-2016");
        Assert.assertTrue(filterredItemCandidates.get(1).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-11-Summer-2015-2016");
    }

    @Test
    public void filterItemCandidatesWithFixedFilterShouldReturnNoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidatesByAlgorithm(itemCandidatesData, "fixed");

        // Assert
        Assert.assertTrue(filterredItemCandidates.isEmpty());
    }

    @Test
    public void filterItemCandidatesWithFixedFormUpperCaseFilterShouldReturnTwoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidatesByAlgorithm(itemCandidatesData, "FiXedFoRM");

        // Assert
        Assert.assertTrue(filterredItemCandidates.size() == 2);
        Assert.assertTrue(filterredItemCandidates.get(0).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(1).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(0).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-7-Summer-2015-2016");
        Assert.assertTrue(filterredItemCandidates.get(1).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-11-Summer-2015-2016");
    }

    @Test
    public void filterItemCandidatesWithExcludedAlgorithmTypeFilterShouldReturnNoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidatesByAlgorithm(itemCandidatesData, "nonsense");

        // Assert
        Assert.assertTrue(filterredItemCandidates.isEmpty());
    }

    @Test
    public void filterItemCandidatesWithNoCandidatesShouldReturnNoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = new ArrayList<>();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidatesByAlgorithm(itemCandidatesData, "nonsense");

        // Assert
        Assert.assertTrue(filterredItemCandidates.isEmpty());
    }

    //endregion

    //region getTestSegmentsForItemCandidates Tests

    @Test
    public void getTestSegmentsForItemCandidatesFourCandidatesReturnsFourSegments() throws Exception {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();
        SQLConnection connection = mock(SQLConnection.class);
        SegmentCollection2 segmentCollection = SegmentCollection2.getInstance();

        // Act
        List<TestSegment> testSegments = msbAssessmentSelectionService.getTestSegmentsForItemCandidates(itemCandidatesData, segmentCollection, connection);

        // Assert
        Assert.assertNotNull(testSegments);
        Assert.assertTrue(testSegments.size() == itemCandidatesData.size());
        for(ItemCandidatesData itemCandidate : itemCandidatesData) {
            boolean segmentMatch = false;
            for(TestSegment testSegment : testSegments) {
                if(testSegment.getSegmentKey().compareToIgnoreCase(itemCandidate.getSegmentKey()) == 0) {
                    segmentMatch = true;
                    break;
                }
            }
            Assert.assertTrue(segmentMatch);
        }
    }

    // endregion

    // region buildCombinedItemGroups tests

    @Test
    @Ignore
    public void buildCombinedItemGroupsValidTestSegmentsProducesOneGroupPerSegmentContainingAllSegmentQuestions() {
        // Arrange
        List<TestSegment> testSegments = retrieveTestSegmentData();

        // Act
        List<ItemGroup> result = msbAssessmentSelectionService.buildCombinedItemGroups(testSegments);

        // Assert
    }

    // endregion

    // region selectFixedMsbSegment tests

    @Test
    public void selectFixedMsbSegmentFirstSegmentPresentReturnsFirstSegmentAndSetsDataTest() throws Exception {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();
        SegmentCollection2 segmentCollection = SegmentCollection2.getInstance();
        when(itemSelectionDbLoader.getAllItemCandidates(any(SQLConnection.class), any(UUID.class))).thenReturn((ArrayList<ItemCandidatesData>) itemCandidatesData);
        UUID opportunityKey = UUID.fromString("86b0ee41-01d9-4a95-bd56-0544c2d5e8cd");

        // Act
        ItemCandidatesData result = msbAssessmentSelectionService.selectFixedMsbSegment(mock(SQLConnection.class), opportunityKey, segmentCollection);

        // Assert
        Assert.assertTrue(msbAssessmentSelectionService.getAdaptiveSegmentData().getSegmentKey().equals(itemCandidatesData.get(0).getSegmentKey()));
        Assert.assertTrue(result.getSegmentKey().equals(itemCandidatesData.get(0).getSegmentKey()));
    }

    @Test
    @Ignore
    public void selectFixedMsbSegmentSecondSegmentPerformsAdaptiveSelection() throws Exception {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();
        itemCandidatesData.remove(0);
        SegmentCollection2 segmentCollection = SegmentCollection2.getInstance();
        when(itemSelectionDbLoader.getAllItemCandidates(any(SQLConnection.class), any(UUID.class))).thenReturn((ArrayList<ItemCandidatesData>) itemCandidatesData);
        UUID opportunityKey = UUID.fromString("86b0ee41-01d9-4a95-bd56-0544c2d5e8cd");

        // Act
        ItemCandidatesData result = msbAssessmentSelectionService.selectFixedMsbSegment(mock(SQLConnection.class), opportunityKey, segmentCollection);

        // Assert
        Assert.assertTrue(msbAssessmentSelectionService.getAdaptiveSegmentData().getSegmentKey().equals(itemCandidatesData.get(0).getSegmentKey()));
        Assert.assertTrue(result.getSegmentKey().equals(itemCandidatesData.get(0).getSegmentKey()));
    }

    // endregion
}
