package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.impl.sets.Cset1Factory;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.loader.TestSegment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Some stuff about the class
 */
@Service
public class MsbAssessmentSelectionServiceImpl implements MsbAssessmentSelectionService {

    @Autowired
    @Qualifier("itemDBLoader")
    private IItemSelectionDBLoader itemSelectionDbLoader;

    @Override
    public ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID oppkey) throws Exception {
        ArrayList<ItemCandidatesData> itemCandidates = itemSelectionDbLoader.getAllItemCandidates(connection, oppkey, true);
        if(itemCandidates.isEmpty()) return null;
        if(itemCandidates.get(0).getSegmentPosition() == 1) return itemCandidates.get(0);
        ArrayList<ItemCandidatesData> fixedFormItemCandidates = new ArrayList<>();
        for(int i = 0; i < itemCandidates.size(); i++) {
            if(itemCandidates.get(i).getAlgorithm().contains("fixedform")) { // We only want to include fixed form tests as second segment options
                fixedFormItemCandidates.add(itemCandidates.get(i));
            }
        }

        SegmentCollection2 segmentCollection = SegmentCollection2.getInstance ();
        for(int i = 0; i < fixedFormItemCandidates.size(); i++) {
            segmentCollection.getSegment(connection, itemCandidates.get(i).getSession(),
                    itemCandidates.get(i).getSegmentKey(), itemSelectionDbLoader);
        }
        return null;
    }

    private CsetGroup groupFixedFormTests(UUID opportunityKey, TestSegment segment) {
        // Create artificial item groups to pass into the AdaptiveItemSelector2013 and retrieve the group that matches best
        // Cset1Factory itemFactory = new Cset1Factory(opportunityKey, itemSelectionDbLoader, segment);
        // Item factory has a "Compute Satisfaction" method that is designed to select the appropriate item group from among artificially created groups
        return null;
    }

    private List<TestSegment> retrieveMsbFixedFormSegmentCandidates(UUID opportunityKey) {
        return null;
    }

    private List<ItemCandidatesData> retrieveAllItemCandidates() {
        // Modify the existing SP to return the information for all segments
        // itemSelectionDbLoader.getItemCandidates(connection, oppkey);
        return null;
    }

}
