package tds.itemselection.selectors;

import java.util.List;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;

public interface MsbItemSelector extends ItemSelector {
  /**
   * @param itemData   Metadata about the segment
   * @param itemGroups These item groups will only be present if the test is a Multi-Stage Braille (MSB) test. In that
   *                   case, each group will represent the entire contents of a fixed-form segment.
   * @return ItemGroup if this is a MSB test, this item group will be the next fixed-form segment to execute
   * @throws tds.itemselection.api.ItemSelectionException when there is an issue getting the next group
   */
  ItemGroup getNextItemGroup(ItemCandidatesData itemData, List<ItemGroup> itemGroups) throws ItemSelectionException;
}
