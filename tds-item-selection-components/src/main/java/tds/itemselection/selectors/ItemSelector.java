package tds.itemselection.selectors;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;

public interface ItemSelector {
  /**
   * Gets the next item group to serve to the user
   *
   * @param itemData the {@link tds.itemselection.base.ItemCandidatesData} to act upon
   * @return ItemGroup the {@link tds.itemselection.base.ItemGroup}
   */
  ItemGroup getNextItemGroup(ItemCandidatesData itemData) throws ItemSelectionException;

  /**
   * @return if there is an error when selecting an item group
   */
  String getItemSelectorError();

  /**
   * {@code true} if the segment is completed
   */
  boolean isSegmentCompleted();
}
