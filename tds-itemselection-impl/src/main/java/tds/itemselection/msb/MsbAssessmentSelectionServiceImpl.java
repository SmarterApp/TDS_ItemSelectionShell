package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.sets.Cset1;
import tds.itemselection.impl.sets.Cset1Factory2013;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.TestSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    @Autowired
    @Qualifier("itemDBLoader")
    private IItemSelectionDBLoader itemSelectionDbLoader;

    @Override
    public ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID oppkey) throws Exception {

        // This section returns segment metadata for all unsatisfied segments in the exam
        ArrayList<ItemCandidatesData> itemCandidates = itemSelectionDbLoader.getAllItemCandidates(connection, oppkey, true);
        if(itemCandidates.isEmpty()) return null;
        if(itemCandidates.get(0).getSegmentPosition() == 1) return itemCandidates.get(0);
        ArrayList<ItemCandidatesData> fixedFormItemCandidates = new ArrayList<>();
        for(int i = 0; i < itemCandidates.size(); i++) {
            // Filter out all the remaining non-fixed segments
            if(itemCandidates.get(i).getAlgorithm().toLowerCase().contains("fixedform")) {
                fixedFormItemCandidates.add(itemCandidates.get(i));
            }
        }

        // SegmentCollection2 is a static singleton containing segment information/methods to go get it
        SegmentCollection2 segmentCollection = SegmentCollection2.getInstance ();
        ArrayList<TestSegment> testSegments = new ArrayList<>();
        // Here we're getting the fully actualized Segment objects from the previously obtained metadata
        for(int i = 0; i < fixedFormItemCandidates.size(); i++) {
            TestSegment segment = segmentCollection.getSegment(connection, null,
                    itemCandidates.get(i).getSegmentKey(), itemSelectionDbLoader);
            testSegments.add(segment);
        }

        ArrayList<Cset1> fixedFormItemGroups = new ArrayList<>();
        int numberOfItemsRequired = 0;
        int maximumItems = 0;
        // This is where the construction of the artificial item groups happens. Each fixed form segment's item pool will become
        // its own item group. These item groups will become the basis for a newly created item pool that will be passed into
        // the existing adaptive selection algorithm. The "winner" will become segment #2.
        for(int i = 0; i < testSegments.size(); i++) {
            Cset1Factory2013 csetFactory = new Cset1Factory2013(oppkey, itemSelectionDbLoader, testSegments.get(i));
            Collection<ItemGroup> itemGroupConstructor = testSegments.get(i).getPool().getItemGroups();
            Cset1 fixedItemGroup = new Cset1(testSegments.get(1).getBp());
            ItemGroup artificialItemGroup = new ItemGroup();
            for(ItemGroup itemGroup : itemGroupConstructor) {
                for(TestItem testItem : itemGroup.getItems()) {
                    artificialItemGroup.addItem(testItem);
                }
                numberOfItemsRequired += itemGroup.getNumberOfItemsRequired();
                maximumItems += itemGroup.getMaximumNumberOfItems();
            }
            CsetGroup itemGroup = new CsetGroup(testSegments.get(1).getSegmentKey(), numberOfItemsRequired, maximumItems);
        }

        // We need a cleanup method here. All of the segments in the opportunity that were NOT selected need to be disqualified from
        // being presented to the student. The effect of this segregation needs to be tested downstream to determine the effects
        // on downstream systems and ensure that the functionality has not changed.

        return null;
    }
}
