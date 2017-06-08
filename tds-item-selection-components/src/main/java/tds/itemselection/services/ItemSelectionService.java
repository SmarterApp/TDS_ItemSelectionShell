package tds.itemselection.services;

import java.util.UUID;

import tds.itemselection.base.ItemGroup;
import tds.itemselection.model.ItemResponse;

/**
 * Handles selecting items
 */
public interface ItemSelectionService {
  /**
   * Selects the next item group
   *
   * @param examId the exam id
   * @param isMsb  {@code true} if it is an MSB assessment
   * @return {@link tds.itemselection.model.ItemResponse} containing an {@link tds.itemselection.base.ItemGroup}
   */
  ItemResponse<ItemGroup> getNextItemGroup(UUID examId, boolean isMsb);
}
