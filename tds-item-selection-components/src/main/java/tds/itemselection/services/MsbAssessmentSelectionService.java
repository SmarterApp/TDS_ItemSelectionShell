package tds.itemselection.services;

import java.util.UUID;

import tds.itemselection.base.ItemCandidatesData;

public interface MsbAssessmentSelectionService {
  /**
   * Convenience method for returning the selected item candidate using the existing AdaptiveSelector2013 code. Calls
   * many of the methods below in proper sequence and includes some validation checks.
   *
   * @param opportunityKey
   * @param segmentCollection The cached test segments
   * @return Returns the ItemCandidatesData object that represents the fixed form selected
   * @throws Exception
   */
  ItemCandidatesData selectFixedMsbSegment(UUID opportunityKey) throws Exception;
}
