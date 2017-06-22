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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import tds.builder.BlueprintBuilder;
import tds.builder.StudentHistory2013Builder;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.model.AlgorithmType;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.SegmentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdaptiveSelector2013Test {
  @Mock
  private SegmentService mockSegmentService;

  @Mock
  private ItemCandidatesService mockItemCandidatesService;

  private AdaptiveSelector2013 selector;

  @Before
  public void setUp() {
    selector = new AdaptiveSelector2013(mockSegmentService, mockItemCandidatesService);
  }

  @Test
  public void shouldReturnNullIfSegmentCannotBeFound() throws ItemSelectionException {
    UUID examId = UUID.randomUUID();
    ItemCandidatesData data = new ItemCandidatesData(
      examId,
      AlgorithmType.ADAPTIVE2.getType(),
      "segmentKey",
      "segmentId",
      1,
      "groupId",
      "blockId",
      UUID.randomUUID(),
      false,
      true);

    ItemGroup itemGroup = selector.getNextItemGroup(data, new ArrayList<ItemGroup>());

    assertThat(itemGroup).isNull();
  }

  @Test
  public void shouldReturnNextItemGroup() throws Exception {
    UUID examId = UUID.randomUUID();

    ItemCandidatesData data = new ItemCandidatesData(
      examId,
      AlgorithmType.ADAPTIVE2.getType(),
      "segmentKey",
      "segmentId",
      1,
      "groupId",
      "blockId",
      UUID.randomUUID(),
      false,
      true);

    Blueprint blueprint = new BlueprintBuilder()
      .withSegmentId("segmentId")
      .withSegmentPosition(1)
      .withSegmentKey("segmentKey")
      .build();

    StudentHistory2013 history = new StudentHistory2013Builder()
      .withItemPool(Collections.singletonList("187-1234"))
      .build();

    ItemGroup itemGroup1 = new ItemGroup("G-176-1123", 1, 2);
    ItemGroup itemGroup2 = new ItemGroup("G-222-1123", 1, 2);

    ItemPool itemPool = new ItemPool();
    TestItem testItem1 = new TestItem("187-1234", itemGroup1.getGroupID(), 1, false, "strand", true);
    itemPool.addItem(testItem1);
    itemPool.addItemgroup(itemGroup1);
    itemPool.addItemgroup(itemGroup2);

    TestSegment segment = new TestSegment("segmentKey", false);
    segment.setBp(blueprint);
    segment.setPool(itemPool);

    when(mockSegmentService.getSegment(null,"segmentKey")).thenReturn(segment);
    when(mockItemCandidatesService.loadOppHistory(examId, "segmentKey")).thenReturn(history);
    when(mockItemCandidatesService.setSegmentSatisfied(examId, segment.position, "POOL EMPTY")).thenReturn(true);

    ItemGroup itemGroup = selector.getNextItemGroup(data, Arrays.asList(itemGroup1, itemGroup2));

    assertThat(itemGroup.getGroupID()).isEqualTo(itemGroup1.getGroupID());
  }
}