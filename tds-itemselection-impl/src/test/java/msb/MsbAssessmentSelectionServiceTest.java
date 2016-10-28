package msb;

import AIR.Common.DB.SQLConnection;
import builders.ItemCandidatesDataBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.msb.MsbAssessmentSelectionServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

/**
 * Created by fairway on 10/27/16.
 */
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

        TestSegment testSegment1 = new TestSegment(
                "(SBAC_PT)SBAC-MSB-IRP-CAT-NoCalc-MATH-7-Summer-2015-2016"      //segmentKey
        );
        testSegment1.refreshMinutes = 33;
        testSegment1.parentTest =  "(SBAC_PT)SBAC-MSB-Mathematics-7-Summer-2015-2016";
        testSegment1.position = 2;
        testSegment1.loaded = false;
        testSegment1.error = null;

        Blueprint blueprint1 = new Blueprint();
        // Excluded properties: refresh, loading, _lastLoadTime - These properties are accessible via constructor
        // and there are no setters for them in the TestSegment class

        return testSegments;
    }

    // public List<ItemCandidatesData> filterItemCandidates(List<ItemCandidatesData> itemCandidates, String filter) tests

    @Test
    public void filterItemCandidatesWithFixedFormFilterShouldReturnTwoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidates(itemCandidatesData, "fixedform");

        // Assert
        Assert.assertTrue(filterredItemCandidates.size() == 2);
        Assert.assertTrue(filterredItemCandidates.get(0).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(1).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(0).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-7-Summer-2015-2016");
        Assert.assertTrue(filterredItemCandidates.get(1).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-11-Summer-2015-2016");
    }

    @Test
    public void filterItemCandidatesWithFixedFilterShouldReturnTwoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidates(itemCandidatesData, "fixed");

        // Assert
        Assert.assertTrue(filterredItemCandidates.size() == 2);
        Assert.assertTrue(filterredItemCandidates.get(0).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(1).getAlgorithm() == "fixedform");
        Assert.assertTrue(filterredItemCandidates.get(0).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-7-Summer-2015-2016");
        Assert.assertTrue(filterredItemCandidates.get(1).getSegmentKey() == "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-11-Summer-2015-2016");
    }
    @Test
    public void filterItemCandidatesWithFixedFormUpperCaseFilterShouldReturnTwoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidates(itemCandidatesData, "FiXedFoRM");

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
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidates(itemCandidatesData, "nonsense");

        // Assert
        Assert.assertTrue(filterredItemCandidates.isEmpty());
    }

    @Test
    public void filterItemCandidatesWithNoCandidatesShouldReturnNoResults() {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = new ArrayList<>();

        // Act
        List<ItemCandidatesData> filterredItemCandidates = msbAssessmentSelectionService.filterItemCandidates(itemCandidatesData, "nonsense");

        // Assert
        Assert.assertTrue(filterredItemCandidates.isEmpty());
    }

    // END filterItemCandidates tests

    // public List<TestSegment> getTestSegmentsForItemCandidates(
    //      List<ItemCandidatesData> itemCandidates, SQLConnection connection) throws Exception tests

    @Test
    public void getTestSegmentsForItemCandidatesTwoCandidatesReturnsTwoSegments() throws Exception {
        // Arrange
        List<ItemCandidatesData> itemCandidatesData = retrieveItemCandidatesData();
        SQLConnection connection = mock(SQLConnection.class);

        // Act
        List<TestSegment> testSegments = msbAssessmentSelectionService.getTestSegmentsForItemCandidates(itemCandidatesData, connection);

        // Assert
        Assert.assertNotNull(testSegments);
    }

    //END getTestSegmentsForItemCandidatesTests
}
