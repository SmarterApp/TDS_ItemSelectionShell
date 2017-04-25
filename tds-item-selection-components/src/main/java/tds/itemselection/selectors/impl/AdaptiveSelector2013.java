/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.selectors.impl;

import AIR.Common.DB.SQLConnection;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.expectedability.ExpectedAbilityComputationSmarter;
import tds.itemselection.impl.blueprint.ActualInfoComputation;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.bpmatchcomputation.BPMatchByItemWithIterativeGroupItemSelection;
import tds.itemselection.impl.item.PruningStrategySmarter;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.Cset1;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.impl.sets.CsetGroupCollection;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.model.OffGradeResponse;
import tds.itemselection.selectors.ItemSelector;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.SegmentService;
import tds.itemselection.sets.Cset1Factory2016;
import tds.itemselection.termination.TerminationManager;

@Component
@Qualifier("adaptiveSelector")
public class AdaptiveSelector2013 extends AbstractAdaptiveSelector implements ItemSelector {
  private static final String messageTemplate = "Exception %1$s executing adaptive algorithm. Exception error: %2$s.";
  private static Logger _logger = LoggerFactory.getLogger(AdaptiveSelector2013.class);
  private static final String csvSeparator = ", ";

  // a combination of test-constant and examinee-variable data
  private Blueprint blueprint;
  private Cset1 cset1;
  private ItemCandidatesData itemCandidates;
  private TestSegment segment;
  private Random rand = new Random();

  private String ls = System.getProperty("line.separator");
  private final SegmentService segmentService;
  private final ItemCandidatesService itemCandidatesService;

  @Autowired
  public AdaptiveSelector2013(final SegmentService segmentService, final ItemCandidatesService itemCandidatesService) {
    this.segmentService = segmentService;
    this.itemCandidatesService = itemCandidatesService;
  }

  @Override
  public ItemGroup getNextItemGroup(ItemCandidatesData itemData) throws ItemSelectionException {
    return getNextItemGroup(itemData, null);
  }

  public ItemGroup getNextItemGroup(SQLConnection connection, ItemCandidatesData itemData, List<ItemGroup> itemGroups) throws ItemSelectionException {
    ItemGroup result = null;
    String error = "";

    try {
      itemCandidates = itemData;

      // We have a collection of segments for our use
//      SegmentCollection2 segs = SegmentCollection2.getInstance();
      // Get the segment for this opportunity
//      segment = itemData.getIsSimulation() ?
//        segs.getSegment(connection, itemCandidates.getSession(), itemCandidates.getSegmentKey(), loader) :
//        segs.getSegment(connection, null, itemCandidates.getSegmentKey(), loader);
      segment = segmentService.getSegment(null, itemCandidates.getSegmentKey());
      if (segment == null) {
        error = "Unable to load blueprint";
        _logger.error(String.format(messageTemplate, "AdaptiveSelection", error));
        throw new ItemSelectionException(error);
      }

      result = selectNext(itemGroups);

      if (result == null) {
        error = "Adaptive item selection failed: Try to find next segment";
      }

      if (StringUtils.isNotEmpty(error)) {
        _logger.error(String.format(messageTemplate, "AdaptiveSelector", error));
      }

    } catch (ItemSelectionException ie) {
      _logger.error(String.format(messageTemplate, "ItemSelectionException", ie.getMessage()), ie);
    } catch (Exception e) {
      _logger.error(String.format(messageTemplate, "Exception", e.getMessage()), e);
    }
    return result;
  }

  public ItemGroup selectNext() throws ItemSelectionException, ReturnStatusException {
    return selectNext(null);
  }


  /* *
   *  1. Compute initial candidate itemgroup set (Cset1)
   * (moved outside of adaptive selector)
   *  2. Compute second candidate itemgroup set Cset2
   *  3. Return best itemgroup within Cset2
   */
  public ItemGroup selectNext(List<ItemGroup> itemGroups) throws ItemSelectionException, ReturnStatusException {

    this._error = null;
    Cset1Factory2016 csetFactory = new Cset1Factory2016(itemCandidates.getOppkey(),
      new BPMatchByItemWithIterativeGroupItemSelection(rand),
      new ActualInfoComputation(),
      new PruningStrategySmarter(rand),
      itemCandidatesService,
      segment);


    try {
      // load all previous responses and calculate working actuals
      csetFactory.LoadHistory();
      // the blueprint has been updated to reflect all previous responses,
      // check that we haven't satisfied configured termination conditions.
      // now check to see if we've satisfied configured termination conditions for this segment.
      TerminationManager termMgr = new TerminationManager(csetFactory.getBp());
      if (termMgr.IsSegmentComplete() && (itemGroups == null || itemGroups.isEmpty())) {
        String reason = termMgr.SegmentCompleteReason;
        terminateSegment(itemCandidates, reason);
        return null;
      }

      // now that we have a working bp and theta estimate, and the test is not terminated,
      //  check to see if we need to append any off-grade items to the pool
      if (csetFactory.getBp().offGradeItemsProps.countByDesignator.size() > 0  // 1 or more off-grade designators are configured for this test
        && (csetFactory.getBp().offGradePoolFilter == null
        || csetFactory.getBp().offGradePoolFilter.isEmpty()))  // have not already added off-grade items to the pool !!!
      {
        String filter = csetFactory.getBp().getOffGradeFilter();
        if (filter != null && !filter.isEmpty()) {
          OffGradeResponse response = itemCandidatesService.addOffGradeItems(itemCandidates.getOppkey(),
            filter, null); // filter = designator = poolfilterProperty = ("OFFGRADE ABOVE"/"OFFGRADE BELOW"/null)
          if (!response.getStatus().equalsIgnoreCase("success")) {
            if (response.getReason().equalsIgnoreCase("offgrade accommodation not exists")) {
              csetFactory.getBp().offGradePoolFilter = "No Accommodation";
              _logger.info("Status = " + response.getStatus() + ", reason = " + response.getReason());
            } else {
              String exceptionMessage = String.format("Attempt to include off-grade items: %s returned a status of:  %s, reason:  %s", filter, response.getStatus(), response.getReason());
              throw new ReturnStatusException(exceptionMessage);
            }
          }
          if (StringUtils.isEmpty(response.getReason())) {
            // the student's custom item pool has been updated with off-grade items; reload history to include
            //  the updated itempool
            csetFactory.LoadHistory();
          }
        }
      }
            /*
             *	If item groups are passed into this method, the method is being leveraged as a selector for
             *	Multi-Stage Braille (MSB) assessments. Parent group exclusions for adaptive selection are not
             *	a valid criteria for excluding retrieved item groups. Ignoring exclusion allows tests paused along
             *	segment barriers to be restarted properly.
             */
      cset1 = csetFactory.MakeCset1(itemGroups != null);
      this.blueprint = cset1.getBlueprint();

      // Record current ability and information approximations (if there is a AdaptiveThetas listener)
      //AIROnlineCommon.AALogger.ThetaLogger.WriteLine("," + _oppkey + "," + blueprint.lastAbilityPosition.ToString() + "," + blueprint.theta.ToString() + "," + blueprint.info.ToString());

      // Per Jon, if we're out of groups, terminate the segment.
      if (this.cset1.itemGroups.size() < 1) {
        terminateSegment(itemCandidates, "POOL EMPTY");
        return null;
      }
      int minitems = Math.max(1, this.blueprint.randomizerIndex);
      int minfirstitems = Math.max(1, this.blueprint.randomizerInitialIndex);

      CsetGroup cg = null;

      if (blueprint.numAdministered < blueprint.getReportingCategories().size()) {
        minitems = minfirstitems;
      }

      int n = Math.min(minitems, cset1.itemGroups.size());

			/*
       *   This section of code that occupies the following IF block was written to support Multi-Stage Braille
			 *   assessments. The code in the MsbAssessmentSelectionService calls the adaptive algorithm and passes it a
			 *   list of ItemGroup objects. Each of these item groups represents the entire contents of a fixed form
			 *   segment (one segment per group). The groups replace the item pool remaining to the initial adaptive
			 *   segment and force the algorithm to select a next "question" from the remaining item groups - in this
			 *   case, the fixed form segment that best matches the student's current ability at the end of their
			 *   adaptive section.
			 */
      if (itemGroups != null) {
        CsetGroupCollection collection = new CsetGroupCollection();
        List<CsetGroup> csetGroups = new ArrayList<>();
        for (ItemGroup itemGroup : itemGroups) {
          CsetGroup group = collection.setAndGet(itemGroup);
          for (int j = 0; j < itemGroup.getItems().size(); j++) {
            CSetItem item = new CSetItem(itemGroup.getItems().get(j), group);
            group.addItem(item);
          }
          csetGroups.add(group);
        }
        cset1.itemGroups = csetGroups;
        n = Math.min(minitems, cset1.itemGroups.size());
      }

      // compute the ability match for each group in cset1
      ComputeAbilityMatch();

      // Once the ability match for each item is computed, call cset1 to finalize the selection metrics
      // by normalizing ability metrics and combining with blueprint metrics.
      // SetSelectionMetrics also orders the csetgroups from best (index 0) to worst (index n -1)
      cset1.setSelectionMetrics();

      int index = rand.nextInt(n);

      cg = cset1.itemGroups.get(index);
      SortSelectedGroup(cg);
      PruneSelectedGroup(cg);
      ItemGroup result = new ItemGroup(cg.groupID, blueprint.segmentKey, blueprint.segmentID, blueprint.segmentPosition,
        cg.getNumberOfItemsRequired(), cg.getMaximumNumberOfItems());

      for (TestItem item : cg.getActiveIncluded()) {
        result.addItem(item);
      }

      return result;

    } catch (Exception e) {

      _error = e.getMessage();
      _logger.error("Error occurs in selectNext () method: "
        + e.getMessage(), e);

      throw new ItemSelectionException(e);
    }
  }


  // / <summary>
  // / Prunes unwanted items from a group
  // / </summary>
  // / <param name="group"></param>
  private void PruneItemgroup(CsetGroup group, boolean pruneForStrictMax) {
    if (group.getActiveIncludedCount() <= 1)
      return; // must have more than one item to prune

    // First prune for strict maxes
    // Then prune for items over the group max
    // Then prune for items over the test max length
    // In no case should the group be pruned down to zero.
    if (pruneForStrictMax)
      PruneStrictMaxes(group);

    if (group.getActiveCount() <= 1)
      return;

    // prune for items over the max allowed for the group
    int overage = group.getActiveIncludedCount() - group.getMaxItems();
    if (overage > 0)
      group.prune(overage);
    if (group.getActiveIncludedCount() <= 1)
      return;

    // Prune for items over max test length
    overage = (group.getActiveIncludedCount() + blueprint.numAdministered)
      - blueprint.maxOpItems;
    if (overage > 0)
      group.prune(overage);

  }

  // / <summary>
  // / Flags items in the group that violate strict max
  // / </summary>
  // / <param name="group"></param>
  private void PruneStrictMaxes(CsetGroup group) {

    // even though no individual item violates a strict max, the combined
    // administration of all group items may do so
    // determine which strict maxes are violated and delete just enough items to
    // satisfy
    // some items may be classified on more than one strict max so optimally
    // from preserving the group, they would be the ones to remove
    // However, we also prefer to remove the items which least match the
    // testee's ability
    // These constraints are difficult if not impossible to satisfy
    // simultaneously in all cases

    // Process:
    // Get the vector of bp elements with strict maxes
    // Get a corresponding vector of item counts
    // While the group has items that would put the element over its max
    // Prune a single item from the group that is classified on that element
    // (see CsetGroup.Prune)
    // Refresh the vector of item counts
    //
    // NOTE: Refreshing the itemcount vector with each prune has the side-effect
    // of updating counts on
    // bp elements not immediately targeted, which is desired.

    int[] bpCounts = group.BpCounts(blueprint.strictMaxes, false, true); // the (initial) count of items on each strict max element

    boolean more = true;

    //
    while (more) {
      more = false;
      for (int i = 0; i < blueprint.strictMaxes.size(); ++i) {
        if (group.getActiveIncludedCount() > 1 && bpCounts[i] + blueprint.strictMaxes.get(i).numAdministered > blueprint.strictMaxes.get(i).maxRequired) {
          // prunes exactly one item at a time
          more = group.prune(blueprint.strictMaxes.get(i).ID);    // it may not be possible to prune ANY item
          // because an item is likely on more than one content level, refresh the counts vector to prevent over-pruning
          if (more)   // only need to refresh bpcounts if an item was successfully pruned
            group.BpCounts(blueprint.strictMaxes, false, true, bpCounts);
        }
      }
    }
  }

  protected void ComputeAbilityMatch() throws ReturnStatusException {
    cset1.ComputeExpectedAbility(new ExpectedAbilityComputationSmarter());
  }

  // / <summary>
  // / new alg will break ties randomly when sorting items in the selected
  // item group
  // / prior to the final pruning stage.
  // / </summary>
  // / <param name="cg"></param>
  protected void SortSelectedGroup(CsetGroup cg) {
    cg.sort(blueprint, true, rand);
  }

  // / <summary>
  // / Prune the selected item group only for test length. Won't prune for
  // strict maxes
  // / </summary>
  // / <param name="group"></param>
  protected void PruneSelectedGroup(CsetGroup group) {
    PruneItemgroup(group, false);
  }

  /// <summary>
  /// Terminates the current segment for the reason provided.
  /// </summary>
  /// <param name="reason"></param>
  private void terminateSegment(ItemCandidatesData itemCandidates, String reason) throws ReturnStatusException {
    if (itemCandidatesService.setSegmentSatisfied(itemCandidates.getOppkey(), segment.position, reason)) {
      this.isSegmentCompleted = true;
      //TODO - This is public in the other class but does not seem to be used
      //this.terminationReason = reason;
    } else
      _error = String.format("Could not mark segment: %s as satisfied for reason: %s.", segment.position.toString(), reason);
  }

  //
  //=======================DEBUGGING=========================================
  //
  private void toString2File(String path, String res) {
    try {
      File csvFile = new File(path);
      if (!csvFile.getParentFile().exists()) {
        _logger.info("Creating directory: " + csvFile.getParentFile());

        boolean result = csvFile.getParentFile().mkdirs();
        if (result) {
          _logger.info("DIR: " + csvFile.getParentFile()
            + "  created");
        }
      }
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(
        csvFile))) {
        writer.write(res);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  //
  private void cset1ToCSVFile(Cset1 cset1, String path) {
    StringBuilder strBuilder = new StringBuilder();

    double bpWeight = cset1.getBlueprint().bpWeight; // w0
    double abilityWeight = (cset1.getBlueprint().cset1Order
      .equalsIgnoreCase("DISTRIBUTION")) ? 0.0
      : cset1.getBlueprint().abilityWeight;  // w2
    double rcAbilityWeight = (cset1.getBlueprint().cset1Order
      .equalsIgnoreCase("DISTRIBUTION")) ? 0.0
      : cset1.getBlueprint().rcAbilityWeight; // w1

    // (blueprintWeight * bpMetric) + (abilityWeight * abilityMetric) + (rcAbilityWeight * rcAbilityMetric)
    strBuilder.append("groupID")
      .append(csvSeparator).append("irtModel")
      .append(csvSeparator).append("selectionMetric")
      .append(csvSeparator).append("blueprintWeight(w2)")
      .append(csvSeparator).append("bpMetric")
      .append(csvSeparator).append("abilityWeight(w0)")
      .append(csvSeparator).append("abilityMetric")
      .append(csvSeparator).append("rcAbilityWeight(w1)")
      .append(csvSeparator).append("rcAbilityMetric")
      .append(csvSeparator).append("BpJitter")
      .append(ls);

    for (CsetGroup gr : cset1.itemGroups) {
      strBuilder.append(gr.groupID)
        .append(csvSeparator).append(gr.items.get(0).dimensions.get(0).IRTModelName)
        .append(csvSeparator).append(gr.selectionMetric)
        .append(csvSeparator).append(bpWeight)
        .append(csvSeparator).append(gr.bpMetric)
        .append(csvSeparator).append(abilityWeight)
        .append(csvSeparator).append(gr.abilityMetric)
        .append(csvSeparator).append(rcAbilityWeight)
        .append(csvSeparator).append(gr.rcAbilityMetric)
        .append(csvSeparator).append(gr.getBpJitter())
        .append(ls);
    }
    toString2File(path, strBuilder.toString());
  }

  protected CsetGroup getSelectedGroup(CsetGroup group, List<String> sGroups) {
    CsetGroup out = null;
    for (String sGr : sGroups) {
      if (sGr.equals(group.groupID)) {
        out = group;
        break;
      }
    }
    return out;
  }

}
