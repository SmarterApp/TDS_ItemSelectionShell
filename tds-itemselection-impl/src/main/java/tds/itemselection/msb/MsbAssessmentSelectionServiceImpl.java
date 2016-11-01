package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import AIR.Common.Utilities.SpringApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tds.itemselection.api.IItemSelection;
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

    public ItemCandidatesData getAdaptiveSegmentKey() {
        return adaptiveSegmentData;
    }

    public void setAdaptiveSegmentKey(ItemCandidatesData adaptiveSegmentKey) {
        this.adaptiveSegmentData = adaptiveSegmentKey;
    }

    private ItemCandidatesData adaptiveSegmentData;

    @Override
    public ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID opportunityKey) throws Exception {

        // This section returns segment metadata for all unsatisfied segments in the exam
        ArrayList<ItemCandidatesData> itemCandidates = itemSelectionDbLoader.getAllItemCandidates(connection, opportunityKey, true);
        if(itemCandidates.isEmpty()) return null;
        if(itemCandidates.get(0).getSegmentPosition() == 1) {
            setAdaptiveSegmentKey(itemCandidates.get(0));
            return itemCandidates.get(0);
        }

        List<ItemCandidatesData> filteredItemCandidates = filterItemCandidates(itemCandidates, "fixedform");
        List<TestSegment> testSegments = getTestSegmentsForItemCandidates(filteredItemCandidates, connection);
        ItemPool itemPool = buildCombinedItemPool(testSegments);
        IItemSelection selector = SpringApplicationContext.getBean ("aa2013Selector",IItemSelection.class);
        ItemGroup itemGroup = selector.getNextItemGroup(connection, adaptiveSegmentData, buildCombinedItemGroups(testSegments, itemPool));

        String segmentId = itemGroup.getGroupID();

        for(int i = 0; i < filteredItemCandidates.size(); i++) {
            if(segmentId.compareTo(filteredItemCandidates.get(i).getSegmentKey()) == 0) {
                return filteredItemCandidates.get(i);
            }
        }

        // We need a cleanup method here. All of the segments in the opportunity that were NOT selected need to be disqualified from
        // being presented to the student. The effect of this segregation needs to be tested downstream to determine the effects
        // on downstream systems and ensure that the functionality has not changed.

        return itemSelectionDbLoader.getItemCandidates(connection, opportunityKey);
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
            itemGroup.setGroupID(testSegments.get(i).getSegmentKey());
            itemGroup.setItems(new ArrayList<> (itemPool.getItems()));
            itemGroup.setMaximumNumberOfItems(itemPool.getItems().size());
            itemGroup.setNumberOfItemsRequired(itemPool.getItems().size());
            itemGroup.setNumRequired(itemPool.getItems().size());
            itemGroups.add(itemGroup);
        }
        return itemGroups;
    }
}
