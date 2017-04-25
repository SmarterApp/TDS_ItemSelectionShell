/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.sets;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.blueprint.ActualInfoComputation;
import tds.itemselection.impl.bpmatchcomputation.BlueprintMatchComputation;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.impl.item.PruningStrategy;
import tds.itemselection.impl.sets.BlueprintEnabledCsetFactory;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.Cset1;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.impl.sets.CsetGroupCollection;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.impl.sets.PriorAdmins;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.services.ItemCandidatesService;

/**
 * Updated implementation with minimal fixes of {@link tds.itemselection.impl.sets.Cset1Factory2013}
 */
public class Cset1Factory2016 extends BlueprintEnabledCsetFactory {
  private static Logger _logger = LoggerFactory.getLogger(Cset1Factory2016.class);

  private Map<String, CsetItem> items = new HashMap<String, CsetItem>();
  private CsetGroupCollection itemGroups = new CsetGroupCollection();

  private Cset1 cset1 = null;
  private UUID oppkey = null;
  private List<String> customPool = new ArrayList<String>();
  // blueprint and itempool for segment
  private TestSegment segment;

  // collection of CsetGroupString each represents a previous test opportunity
  // in the same subject.
  // Mask for item selection.
  private ArrayList<HashSet<String>> previousGroups = new ArrayList<HashSet<String>>();
  // collection of group IDs for field test items pre-selected FOR THIS TEST.
  // Exclude from pool.
  private HashSet<String> excludeGroups = new HashSet<String>();
  // collection of ItemResponse objects administered THIS TEST.
  // Exclude from pool.
  private List<ItemResponse> responses = new ArrayList<ItemResponse>();
  // collection of items used, each element is a sequence.
  // Indexed by sequence.
  private PriorAdmins priorAdmins = new PriorAdmins();

  // the bp-match routine, actual theta and info calcs, and the pruning strategy
  //  vary between the current algorithm and the new one.  The Cset1Factory is configured accordingly.
  private final BlueprintMatchComputation bpSatisfactionCalc;
  private final ActualInfoComputation actualInfoCalc;
  private final PruningStrategy pruningStrategy;
  private final ItemCandidatesService itemCandidatesService;

  //TODO - Passing these services into an object that is holding state is a bit odd.  Carrying forward
  public Cset1Factory2016(final UUID opportunityKey,
                          final BlueprintMatchComputation bpMatchComp,
                          final ActualInfoComputation actualInfoComp,
                          final PruningStrategy pruningStrategy,
                          final ItemCandidatesService itemCandidatesService,
                          final TestSegment testSegment) {
    this.oppkey = opportunityKey;
    this.bpSatisfactionCalc = bpMatchComp;
    this.actualInfoCalc = actualInfoComp;
    this.pruningStrategy = pruningStrategy;
    this.itemCandidatesService = itemCandidatesService;
    this.segment = testSegment;
  }

  public Cset1 MakeCset1() throws ItemSelectionException, ReturnStatusException {
    return MakeCset1(false);
  }

  public Cset1 MakeCset1(boolean ignoreParent) throws ItemSelectionException, ReturnStatusException {
    // initialized Pool and created itemGroups!
    initializePool();

    // Preemptively filter items over strict maxes
    pruningStrategy.PruneStrictMaxes(this.getBp(), false);

    // Recycle items if necessary and remove from consideration any previously
    // used that are not recycled
    if (previousGroups.size() > 0) {
      bp.recycleItems(priorAdmins);
      // Any itemgroups remaining flagged as used, remove from the collection

    }
    // remove all item groups that are completely pruned or remain in 'used'
    // status
    itemGroups.removeUsed(ignoreParent);

    ComputeSatisfaction();

    return this.cset1;
  }

  /**
   * Load the student's previous responses into a copy of the BP that can be
   * modified for this student/opp.
   *
   * @throws tds.itemselection.api.ItemSelectionException
   * @throws TDS.Shared.Exceptions.ReturnStatusException
   */
  public void LoadHistory() throws ItemSelectionException, ReturnStatusException {
    StudentHistory2013 oppHData = itemCandidatesService.loadOppHistory(oppkey, segment.getSegmentKey());

    customPool = oppHData.get_itemPool();
    previousGroups = oppHData.get_previousTestItemGroups();
    excludeGroups = oppHData.get_previousFieldTestItemGroups();
    responses = oppHData.get_previousResponses();
    double startAbility = oppHData.getStartAbility();

    bp = segment.getBp().copy();
    bp.setStartAbilityRC(startAbility);
    bp.setActualInfoComputation(actualInfoCalc);
    bp.offGradePoolFilter = oppHData.getOffgradeFilter();

    ProcessResponses();

    // if there are previous responses, calculate the standard error at the BP and RC levels
    // This can only be done after info values have been tallied for all responses so far.
    if (responses.size() > 0)
      bp.updateStandardError();
  }


  /**
   * Add response item groups to excludeGroups, and update blueprint
   * satisfaction and ability estimates
   *
   * @throws TDS.Shared.Exceptions.ReturnStatusException
   */
  private void ProcessResponses() throws ReturnStatusException {
    ItemPool pool = segment.getPool();
    TestItem item;
    List<ItemResponse> res = new ArrayList<ItemResponse>(responses);
    int currentSegment = 1;
    Collections.sort(res);
    // update my local blueprint with this examinee's items and _responses for
    // blueprint satisfaction and ability estimation
    for (ItemResponse r : res) // Is the adaptive ability estimate computation
    // commutative? If not, then we need to compute
    // in the same order every time
    {
      item = pool.getItem(r.itemID);
      if (item == null) // then this response is to a sibling item not in this
      {
        item = pool.getSiblingItem(r.itemID);
      }
      // Why do we care about sibling items? Because the segments may include
      // common item groups with non-intersecting partitions of items
      if (item != null) {
        r.setBaseItem(item);
      }
      if (!excludeGroups.contains(r.groupID)) {
        excludeGroups.add(r.groupID);
      }

      // if this is a segmented test, then overall ability estimates need to
      // percolate down the segments
      // This is solely for the integrity of strands
      if (r.segmentPosition > currentSegment) {
        currentSegment = r.segmentPosition;
        bp.SetStartAbility(bp.theta);
      }
      bp.ProcessResponse(r, segment.position);
    }
    //Old variant
    double panicWeight;
    if (bp.numAdministered == bp.minOpItems) {
      panicWeight = bp.minOpItems;
    } else {
      panicWeight = bp.minOpItems / (double) (bp.minOpItems - bp.numAdministered);
    }
  }

  /**
   * Create the examinee-specific item pool from his custom pool excluding
   * field test items preselected and itemgroups already selected
   * Also flag items administered previously on other tests
   */
  private void initializePool() throws ItemSelectionException {
    TestItem item;
    CSetItem cSetItem;
    ItemGroup grp;
    CsetGroup csetGrp;
    ItemPool pool = segment.getPool();

    // create the customized itempool from the segment's pool and the custom
    // pool received from the database
    // exclude all item groups that were selected for this test or have been
    // pre-selected for field test administration
    for (String itemID : customPool) {
      item = pool.getItem(itemID);
      // this may result in null if the custom pool is over all segments
      if (item == null)
        continue;
      // If all items are notActive or FieldTest we will have empty itemGroups !
      if (!excludeGroups.contains(item.groupID) && item.isActive && !item.isFieldTest
        && !items.containsKey(item.itemID)) // added in version from 2015-03-13
      {

        grp = pool.getItemGroup(item.groupID);
        // Here is the only point in code where we load ItemGroup from pool in itemGroups member
        csetGrp = itemGroups.setAndGet(grp); // makes the csetgroup if it does not
        // exist and adds to the collection

        cSetItem = new CSetItem(item, csetGrp);
        csetGrp.addItem(cSetItem);

        items.put(cSetItem.itemID, cSetItem);

        bp.addCsItem(cSetItem); // add the item to the examinee's blueprint to
        // permit computation of poolsize per element
        // do not add the item to the CSET group until we have processed the
        // items for previous use and recycling

        // record whether or not this item was used for this examinee in the
        // past
        for (HashSet<String> gs : this.previousGroups) {
          if (gs.contains(item.groupID)) {
            priorAdmins.addItem(gs.size(), cSetItem);
          }
        }
      }
    }
    // QUESTION: Should we 'index' the used items by chronology? Or just run
    // through them all when recycling.
    priorAdmins.sortItems();
  }

  /**
   * Computes blueprint satisfaction
   * on all remaining itemgroups (not pruned or used)
   *
   * @throws TDS.Shared.Exceptions.ReturnStatusException
   */
  private void ComputeSatisfaction() throws ItemSelectionException, ReturnStatusException {
    // Compute blueprint metric on every itemgroup (CSETGroup) (add jitter?)
    // Sort itemgroups by blueprint metric
    // Remove itemgroups below threshold
    // Unit-Normalize itemgroup blueprint metrics
    // double epsilon = 0.001; // two metrics this close are considered equal
    double minMetric = 99999.0;
    double maxMetric = -99999.0;
    int cset1Size;
    int last;
    int i;
    CsetGroup group;
    CsetGroup firstGroup;
    if (cset1 == null)
      cset1 = new Cset1(bp);

    this.bpSatisfactionCalc.execute(this, itemGroups.getValues());
    List<CsetGroup> groups = new ArrayList<CsetGroup>(itemGroups.getValues());
    //Remove the groups which has not active items
    Collections.sort(groups);
    if (groups == null || groups.isEmpty()) {
      // Cannot initialize Item Pool: item groups are empty
      return;
    } else {
      firstGroup = groups.get(0);  // for debugging
    }

    if (bp.numAdministered == 0)
      cset1Size = Math.max(bp.cSet1Size, bp.randomizerInitialIndex);
    else
      cset1Size = Math.max(bp.cSet1Size, bp.randomizerIndex);

    maxMetric = firstGroup.selectionMetric;
    // find the 'last' group index
    last = Math.min(cset1Size, groups.size()) - 1;

    if (!firstGroup.metricComputed) { // then no metrics were computed. Set all group bpmetrics = 1 and return
      // top cset1size groups
      for (i = 0; i <= last; ++i) // preserve sorted ordering
      {
        group = groups.get(i);
        group.normalizeBPMetric2013(1.0, 1.0); // there is other cleanup to do
        cset1.addItemgroup(group);
      }
      return;
    }
    // first, make sure we have a legitimate metric (work backwards from
    // cset1size)
    for (i = last; i >= 0; --i) {
      group = groups.get(i);
      if (group.metricComputed) {
        last = i;
        minMetric = group.selectionMetric;
        break;
      }
    }
    // now proceed forward from 'last' to the outer boundary of the metric
    if (bp.cset1Order.equalsIgnoreCase("ALL")) {
      for (i = last + 1; i < groups.size(); ++i) {
        group = groups.get(i);
        if (!group.metricComputed || group.selectionMetric < minMetric) {
          break;
        }
        last = i;
      }
    }

    // normalize the cset groups' metrics and add each to the result set
    for (i = 0; i <= last; ++i) {
      group = groups.get(i);
      group.normalizeBPMetric2013(minMetric, maxMetric);
      cset1.addItemgroup(group);
    }
  }
}
