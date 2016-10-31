package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.Cset1;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.TestSegment;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public interface MsbAssessmentSelectionService {

    ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID opportunityKey) throws Exception;

    List<ItemCandidatesData> filterItemCandidates(List<ItemCandidatesData> itemCandidates, String filter);

    List<TestSegment> getTestSegmentsForItemCandidates(List<ItemCandidatesData> itemCandidates, SQLConnection connection) throws Exception;

    Cset1 buildCombinedCset(TestSegment testSegment, Blueprint blueprint, UUID opportunityKey);

    Blueprint buildCombinedBlueprint(List<TestSegment> testSegments);

    TestSegment buildCombinedTestSegment(List<TestSegment> testSegments, Blueprint blueprint, ItemPool itemPool);

    ItemPool buildCombinedItemPool(List<TestSegment> testSegments);

}
