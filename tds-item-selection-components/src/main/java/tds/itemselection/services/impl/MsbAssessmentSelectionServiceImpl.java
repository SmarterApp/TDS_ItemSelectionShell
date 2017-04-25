package tds.itemselection.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.model.AlgorithmType;
import tds.itemselection.selectors.ItemSelector;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.MsbAssessmentSelectionService;
import tds.itemselection.services.SegmentService;

@Service
public class MsbAssessmentSelectionServiceImpl implements MsbAssessmentSelectionService {
  private final ItemSelector adaptiveSelector;
  private final ItemCandidatesService itemCandidatesService;
  private final SegmentService segmentService;

  @Autowired
  public MsbAssessmentSelectionServiceImpl(ItemSelector adaptiveSelector, ItemCandidatesService itemCandidatesService, SegmentService segmentService) {
    this.adaptiveSelector = adaptiveSelector;
    this.itemCandidatesService = itemCandidatesService;
    this.segmentService = segmentService;
  }

  @Override
  public ItemCandidatesData selectFixedMsbSegment(UUID opportunityKey) throws Exception {
    List<ItemCandidatesData> itemCandidates =
      itemCandidatesService.getAllItemCandidates(opportunityKey);
    if (itemCandidates.isEmpty()) return null;
    ItemCandidatesData adaptiveSegmentData = itemCandidates.get(0);
    if (itemCandidates.get(0).getSegmentPosition() == 1 && itemCandidates.get(0).isActive()) {
      return itemCandidates.get(0);
    }
    int activeCount = 0;
    int indexLastSeen = -1;
    for (int i = 0; i < itemCandidates.size(); i++) {
      if (itemCandidates.get(i).isActive()) {
        activeCount++;
        indexLastSeen = i;
      }
    }

    if (activeCount == 1) {
      return itemCandidates.get(indexLastSeen);
    } else if (activeCount == 0) {
      ItemCandidatesData itemCandidatesData = new ItemCandidatesData();
      itemCandidatesData.setAlgorithm("SATISFIED");
      return itemCandidatesData;
    }

    List<ItemCandidatesData> filteredItemCandidates = filterItemCandidatesByAlgorithm(
      filterItemCandidatesByActive(itemCandidates, true), AlgorithmType.FIXED_FORM.getType());

    List<TestSegment> testSegments = getTestSegmentsForItemCandidates(filteredItemCandidates);
    ItemGroup itemGroup = adaptiveSelector.getNextItemGroup(adaptiveSegmentData, buildCombinedItemGroups(testSegments));

    String segmentId = itemGroup.getGroupID();

    int index = 0;
    for (int i = 0; i < filteredItemCandidates.size(); i++) {
      if (segmentId.compareTo(filteredItemCandidates.get(i).getSegmentKey()) == 0) {
        index = i;
        break;
      }
    }
    ItemCandidatesData calculatedFixedForm = filteredItemCandidates.get(index);

    itemCandidatesService.cleanupDismissedItemCandidates(calculatedFixedForm.getSegmentPosition(), opportunityKey);

    return calculatedFixedForm;
  }

  private List<ItemCandidatesData> filterItemCandidatesByAlgorithm(List<ItemCandidatesData> itemCandidates, String filter) {
    ArrayList<ItemCandidatesData> itemCandidatesData = new ArrayList<>();
    for (ItemCandidatesData itemCandidate : itemCandidates) {
      if (itemCandidate.getAlgorithm().compareToIgnoreCase(filter) == 0) {
        itemCandidatesData.add(itemCandidate);
      }
    }
    return itemCandidatesData;
  }

  private List<ItemCandidatesData> filterItemCandidatesByActive(List<ItemCandidatesData> itemCandidates, boolean isActive) {
    ArrayList<ItemCandidatesData> itemCandidatesData = new ArrayList<>();
    for (ItemCandidatesData itemCandidate : itemCandidates) {
      if (itemCandidate.isActive() == isActive) {
        itemCandidatesData.add(itemCandidate);
      }
    }
    return itemCandidatesData;
  }

  private List<TestSegment> getTestSegmentsForItemCandidates(List<ItemCandidatesData> itemCandidates) throws Exception {
    List<TestSegment> testSegments = new ArrayList<>();
    for (ItemCandidatesData itemCandidate : itemCandidates) {
      TestSegment segment = segmentService.getSegment(null, itemCandidate.getSegmentKey());
      testSegments.add(segment);
    }
    return testSegments;
  }

  private List<ItemGroup> buildCombinedItemGroups(List<TestSegment> testSegments) {
    List<ItemGroup> itemGroups = new ArrayList<>();
    for (TestSegment testSegment : testSegments) {
      ItemGroup itemGroup = new ItemGroup();
      // The group ID is being set to the segment key because we need to know what segment was selected later
      itemGroup.setGroupID(testSegment.getSegmentKey());
      List<TestItem> groupItems = new ArrayList<>();
      Collection<TestItem> segmentItems = testSegment.getPool().getItems();
      for (TestItem testItem : segmentItems) {
        testItem.setGroupID(testSegment.getSegmentKey());
        groupItems.add(testItem);
      }
      itemGroup.setItems(groupItems);
      itemGroup.setMaximumNumberOfItems(groupItems.size());
      itemGroup.setNumberOfItemsRequired(groupItems.size());
      itemGroup.setNumRequired(groupItems.size());
      itemGroups.add(itemGroup);
    }
    return itemGroups;
  }
}
