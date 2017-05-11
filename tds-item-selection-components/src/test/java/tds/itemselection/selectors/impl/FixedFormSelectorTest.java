package tds.itemselection.selectors.impl;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.model.AlgorithmType;
import tds.itemselection.services.ItemCandidatesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FixedFormSelectorTest {
  private FixedFormSelector fixedFormSelector;

  @Mock
  private ItemCandidatesService mockItemCandidatesService;

  @Before
  public void setUp() {
    fixedFormSelector = new FixedFormSelector(mockItemCandidatesService);
  }

  @Test
  public void shouldReturnNextGroup() throws ItemSelectionException, ReturnStatusException {
    UUID examId = UUID.randomUUID();
    ItemCandidatesData data = new ItemCandidatesData(examId, AlgorithmType.FIXED_FORM.getType());
    data.setSegmentKey("segmentKey");
    data.setGroupID("groupId");
    data.setBlockID("blockId");

    ItemGroup itemGroup = mock(ItemGroup.class);

    when(mockItemCandidatesService.getItemGroup(examId, "segmentKey", "groupId", "blockId", false)).thenReturn(itemGroup);

    assertThat(fixedFormSelector.getNextItemGroup(data)).isEqualTo(itemGroup);

    verify(mockItemCandidatesService).getItemGroup(examId, "segmentKey", "groupId", "blockId", false);
  }

  @Test(expected = ItemSelectionException.class)
  public void shouldThrowIfThereIsError() throws ReturnStatusException, ItemSelectionException {
    UUID examId = UUID.randomUUID();
    ItemCandidatesData data = new ItemCandidatesData(examId, AlgorithmType.FIXED_FORM.getType());
    data.setSegmentKey("segmentKey");
    data.setGroupID("groupId");
    data.setBlockID("blockId");

    when(mockItemCandidatesService.getItemGroup(examId, "segmentKey", "groupId", "blockId", false)).thenThrow(new ReturnStatusException("Message"));

    fixedFormSelector.getNextItemGroup(data);
  }
}