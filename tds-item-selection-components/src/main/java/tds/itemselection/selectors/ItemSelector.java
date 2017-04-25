package tds.itemselection.selectors;

import java.util.List;

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
   * @param itemData   Metadata about the segment
   * @param itemGroups These item groups will only be present if the test is a Multi-Stage Braille (MSB) test. In that
   *                   case, each group will represent the entire contents of a fixed-form segment.
   * @return ItemGroup if this is a MSB test, this item group will be the next fixed-form segment to execute
   * @throws ItemSelectionException when there is an issue getting the next group
   */
  ItemGroup getNextItemGroup(ItemCandidatesData itemData, List<ItemGroup> itemGroups) throws ItemSelectionException;

  /**
   * @return if there is an error when selecting an item group
   */
  String getItemSelectorError();

  /**
   * {@code true} if the segment is completed
   */
  boolean isSegmentCompleted();
}
