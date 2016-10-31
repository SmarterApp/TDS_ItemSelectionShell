package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.sets.Cset1;
import tds.itemselection.impl.sets.Cset1Factory2013;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.TestSegment;

import java.util.*;

/**
 * This class handles the logic behind the selection of Multi-Stage Braille (MSB) assessments. MSB assessments
 * (currently) consist of an adaptive segment followed by a fixed form segment. The fixed segment is not known at
 * the start of the assessment - it must be calculated based on the student's performance in the initial adaptive
 * section. This class accomplishes this selection by creating an artificial segment made up of item groups representing
 * the entire pool of each of the segments. It passes these item groups into the existing adaptive selection algorithm
 * and uses the result to determine which fixed form test is appropriate for the MSB student being assessed.
 */
@Service
public class MsbAssessmentSelectionServiceImpl implements MsbAssessmentSelectionService {

    public MsbAssessmentSelectionServiceImpl() {}

    public MsbAssessmentSelectionServiceImpl(IItemSelectionDBLoader itemSelectionDbLoader) {
        this.itemSelectionDbLoader = itemSelectionDbLoader;
    }

    @Autowired
    @Qualifier("itemDBLoader")
    private IItemSelectionDBLoader itemSelectionDbLoader;

    public String getAdaptiveSegmentKey() {
        return adaptiveSegmentKey;
    }

    public void setAdaptiveSegmentKey(String adaptiveSegmentKey) {
        this.adaptiveSegmentKey = adaptiveSegmentKey;
    }

    private String adaptiveSegmentKey;

    @Override
    public ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID opportunityKey) throws Exception {

        // This section returns segment metadata for all unsatisfied segments in the exam
        ArrayList<ItemCandidatesData> itemCandidates = itemSelectionDbLoader.getAllItemCandidates(connection, opportunityKey, true);
        if(itemCandidates.isEmpty()) return null;
        if(itemCandidates.get(0).getSegmentPosition() == 1) return itemCandidates.get(0);

        // This applies a case-insensitive filter on the itemcandidates and returns only those that contain the filter sequence
        List<ItemCandidatesData> filteredItemCandidates = filterItemCandidates(itemCandidates, "fixedform");

        // Here we're getting the fully actualized Segment objects from the previously obtained metadata
        List<TestSegment> testSegments = getTestSegmentsForItemCandidates(filteredItemCandidates, connection);

//        Cset1 fixedItemGroup = buildCsetFromEligibleSegments(testSegments, buildCombinedBlueprint(testSegments), opportunityKey);

        // We need a cleanup method here. All of the segments in the opportunity that were NOT selected need to be disqualified from
        // being presented to the student. The effect of this segregation needs to be tested downstream to determine the effects
        // on downstream systems and ensure that the functionality has not changed.

        return null;
    }

    @Override
    public List<ItemCandidatesData> filterItemCandidates(List<ItemCandidatesData> itemCandidates, String filter) {
        ArrayList<ItemCandidatesData> fixedFormItemCandidates = new ArrayList<>();
        for(int i = 0; i < itemCandidates.size(); i++) {
            if(itemCandidates.get(i).getAlgorithm().toLowerCase().contains(filter.toLowerCase())) {
                fixedFormItemCandidates.add(itemCandidates.get(i));
            }
        }
        return fixedFormItemCandidates;
    }

    @Override
    public List<TestSegment> getTestSegmentsForItemCandidates(List<ItemCandidatesData> itemCandidates, SQLConnection connection) throws Exception {
        // SegmentCollection2 is a static singleton containing segment information/methods to go get it
        SegmentCollection2 segmentCollection = SegmentCollection2.getInstance ();
        ArrayList<TestSegment> testSegments = new ArrayList<>();
        // Here we're getting the fully actualized Segment objects from the previously obtained metadata
        for(int i = 0; i < itemCandidates.size(); i++) {
            TestSegment segment = segmentCollection.getSegment(connection, null,
                    itemCandidates.get(i).getSegmentKey(), itemSelectionDbLoader);
            testSegments.add(segment);
        }
        return testSegments;
    }

    @Override
    public Cset1 buildCombinedCset(TestSegment testSegment, Blueprint blueprint, UUID opportunityKey) {
        Cset1 fixedItemGroup = new Cset1(blueprint);
        int numberOfItemsRequired = 0;
        int maximumItems = 0;
        // This is where the construction of the artificial item groups happens. Each fixed form segment's item pool will become
        // its own item group. These item groups will become the basis for a newly created item pool that will be passed into
        // the existing adaptive selection algorithm. The "winner" will become segment #2.

        Cset1Factory2013 cset1Factory2013 = new Cset1Factory2013(opportunityKey, itemSelectionDbLoader, testSegment);

//        for(int i = 0; i < testSegments.size(); i++) {
//            Cset1Factory2013 csetFactory = new Cset1Factory2013(opportunityKey, itemSelectionDbLoader, testSegments.get(i));
//            Collection<ItemGroup> itemGroupConstructor = testSegments.get(i).getPool().getItemGroups();
//            ItemGroup artificialItemGroup = new ItemGroup();
//            for(ItemGroup itemGroup : itemGroupConstructor) {
//                for(TestItem testItem : itemGroup.getItems()) {
//                    artificialItemGroup.addItem(testItem);
//                }
//                numberOfItemsRequired += itemGroup.getNumberOfItemsRequired();
//                maximumItems += itemGroup.getMaximumNumberOfItems();
//            }
//            CsetGroup itemGroup = new CsetGroup(testSegments.get(1).getSegmentKey(), numberOfItemsRequired, maximumItems);
//            fixedItemGroup.addItemgroup(itemGroup);
//        }
//        return fixedItemGroup;
        return fixedItemGroup;
    }

    @Override
    public TestSegment buildCombinedTestSegment(List<TestSegment> testSegments, Blueprint blueprint, ItemPool itemPool) {
        TestSegment combinedTestSegment = new TestSegment("Artificial Adaptive Segment");
        if(testSegments.isEmpty()) return combinedTestSegment;
        combinedTestSegment.setBp(blueprint);
        combinedTestSegment.parentTest = testSegments.get(0).parentTest;
        combinedTestSegment.refreshMinutes = testSegments.get(0).refreshMinutes;
        combinedTestSegment.setPool(itemPool);
        return combinedTestSegment;
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
    public Blueprint buildCombinedBlueprint(List<TestSegment> testSegments) {
        List<Blueprint> blueprints = extractBlueprints(testSegments);
        if(blueprints.isEmpty()) return new Blueprint();
        Blueprint combinedBlueprint = blueprints.get(0).copy(true);
        combinedBlueprint.segmentKey = "Artificial Adaptive Segment";
        combinedBlueprint.segmentID = "Fixed Form";
        combinedBlueprint.segmentPosition = 2;

        for (int i = 1; i < blueprints.size(); i++) {
            combinedBlueprint.minOpItems += blueprints.get(i).minOpItems;
            combinedBlueprint.maxOpItems += blueprints.get(i).maxOpItems;
            for(BpElement bpElement : blueprints.get(i).elements.getValues()) {
                combinedBlueprint.elements.addBpElement(bpElement);
            }
        }
        return combinedBlueprint;
    }

    private List<Blueprint> extractBlueprints(List<TestSegment> testSegments) {
        List<Blueprint> blueprints = new ArrayList<>();
        for(int i = 0; i < testSegments.size(); i++) {
            blueprints.add(testSegments.get(i).getBp());
        }
        return blueprints;
    }
}
