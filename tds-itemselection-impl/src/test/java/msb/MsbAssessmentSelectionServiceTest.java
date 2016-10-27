package msb;

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

    private final UUID opportunityKey = UUID.fromString("86b0ee41-01d9-4a95-bd56-0544c2d5e8cd");
    private final UUID sessionKey = UUID.fromString("ad1350c7-0747-45d2-8879-7289dee2566f");

    @Before
    public void setup() {
        msbAssessmentSelectionService = new MsbAssessmentSelectionServiceImpl();
        itemSelectionDbLoader = mock(IItemSelectionDBLoader.class);
    }

    private List<ItemCandidatesData> retrieveItemCandidatesData() {
        ArrayList<ItemCandidatesData> itemCandidatesData = new ArrayList<>();

        ItemCandidatesData candidate1 = new ItemCandidatesData(
                opportunityKey                                              // oppkey
                , "adaptive2"                                               // algorithm
                , "(SBAC_PT)SBAC-MSB-IRP-CAT-Calc-MATH-7-Summer-2015-2016"  // segmentKey
                , "SBAC-MSB-IRP-CAT-Calc-MATH-7"                            // segmentID
                , 1                                                         // segmentPosition
                , ""                                                        // groupID
                , ""                                                        // blockID
                , sessionKey                                                // session
                , false                                                     // isSimulation
        );
        itemCandidatesData.add(candidate1);

        ItemCandidatesData candidate2 = new ItemCandidatesData(
                opportunityKey
                , "adaptive2"
                , "(SBAC_PT)SBAC-MSB-IRP-CAT-NoCalc-MATH-7-Summer-2015-2016"
                , "SBAC-MSB-IRP-CAT-NoCalc-MATH-7"
                , 2
                , ""
                , ""
                , sessionKey
                , false
        );
        itemCandidatesData.add(candidate2);

        ItemCandidatesData candidate3 = new ItemCandidatesData(
                opportunityKey
                , "fixedform"
                , "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-7-Summer-2015-2016"
                , "SBAC-MSB-IRP-Perf-MATH-7"
                , 3
                , ""
                , ""
                , sessionKey
                , false
        );
        itemCandidatesData.add(candidate3);

        ItemCandidatesData candidate4 = new ItemCandidatesData(
                opportunityKey
                , "fixedform"
                , "(SBAC_PT)SBAC-MSB-IRP-Perf-MATH-11-Summer-2015-2016"
                , "SBAC-MSB-IRP-Perf-MATH-11"
                , 4
                , ""
                , ""
                , sessionKey
                , false
        );
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



    //END getTestSegmentsForItemCandidatesTests
}
