package tds.itemselection.msb;

import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.base.ItemCandidatesData;

import java.sql.SQLException;
import java.util.UUID;

/**
 *
 */
public interface MsbAssessmentSelectionService {

    ItemCandidatesData selectFixedMsbSegment(SQLConnection connection, UUID oppkey) throws Exception;

}
