/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

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
public class FieldTestSelectorTest {
  private FieldTestSelector fieldTestSelector;

  @Mock
  private ItemCandidatesService mockItemCandidatesService;

  @Before
  public void setUp() {
    fieldTestSelector = new FieldTestSelector(mockItemCandidatesService);
  }

  @Test
  public void shouldReturnNextGroup() throws ItemSelectionException, ReturnStatusException {
    UUID examId = UUID.randomUUID();
    ItemCandidatesData data = new ItemCandidatesData(examId, AlgorithmType.FIXED_FORM.getType());
    data.setSegmentKey("segmentKey");
    data.setGroupID("groupId");
    data.setBlockID("blockId");

    ItemGroup itemGroup = mock(ItemGroup.class);

    when(mockItemCandidatesService.getItemGroup(examId, "segmentKey", "groupId", "blockId", true)).thenReturn(itemGroup);

    assertThat(fieldTestSelector.getNextItemGroup(data)).isEqualTo(itemGroup);

    verify(mockItemCandidatesService).getItemGroup(examId, "segmentKey", "groupId", "blockId", true);
  }

  @Test(expected = ItemSelectionException.class)
  public void shouldThrowIfThereIsError() throws ReturnStatusException, ItemSelectionException {
    UUID examId = UUID.randomUUID();
    ItemCandidatesData data = new ItemCandidatesData(examId, AlgorithmType.FIXED_FORM.getType());
    data.setSegmentKey("segmentKey");
    data.setGroupID("groupId");
    data.setBlockID("blockId");

    when(mockItemCandidatesService.getItemGroup(examId, "segmentKey", "groupId", "blockId", true)).thenThrow(new ReturnStatusException("Message"));

    fieldTestSelector.getNextItemGroup(data);
  }
}