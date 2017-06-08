/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TDS.Shared.Exceptions.ReturnStatusException;
import AIR.Common.DB.SQLConnection;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.blueprint.ActualInfoComputation;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.bpmatchcomputation.BlueprintMatchComputation;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.impl.item.PruningStrategy;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.loader.TestSegment;

/**
 * @author akulakov
 * 
 */
public class Cset1Factory extends BlueprintEnabledCsetFactory
{
  private static Logger  _logger  = LoggerFactory.getLogger (Cset1Factory.class);
  /*
   * What can we expect the db to provide? The items in the student's customized
   * pool
   * 
   * 
   * What should permanently reside in memory (a singleton) ALL items in the
   * pool together with the strand/contentlevel/affinity-group designations on
   * each. Optional: The test blueprint
   * 
   * What we don't need to permanently reside in memory Item properties (the
   * only use is in computing customized itempool, which is provided by the db)
   * Blueprint (for now, provided by the db on each item selection request)
   */

  private Map<String, CsetItem>     		  items          = new HashMap<String, CsetItem> ();
  private CsetGroupCollection                 itemGroups     = new CsetGroupCollection ();

  private IItemSelectionDBLoader              loader         = null;
  private Cset1                               cset1          = null;
  private UUID                                oppkey         = null;
  private String                              customPool     = null;
  // ArrayList _groupLists;
  private double                              startAbility;
  // as we near the end of the test, panic over contentlevels that have not
  // reached min required
  private double                              panicWeight;
  // blueprint and itempool for segment
  private TestSegment                         segment;
  // collection of CsetGroupString each represents a previous test opportunity
  // in the same subject.
  // Mask for item selection.
  private List<CsetGroupString>               previousGroups = new ArrayList<CsetGroupString> ();
  // collection of group IDs for field test items pre-selected FOR THIS TEST.
  // Exclude from pool.
  private Map<String, String>       excludeGroups   = new HashMap<String, String> ();
  // collection of ItemResponse objects administered THIS TEST.
  // Exclude from pool.
  private Map<String, ItemResponse> responses      = new HashMap<String, ItemResponse> ();
  // collection of items used, each element is a sequence.
  // Indexed by sequence.
  private PriorAdmins                         priorAdmins    = new PriorAdmins ();

  public Cset1Factory (UUID opportunityKey, IItemSelectionDBLoader loader, TestSegment segment)
  {
    this.loader = loader;
    this.oppkey = opportunityKey;
    this.segment = segment;
    // What now?
  }

public Cset1 MakeCset1 (SQLConnection connection) throws ItemSelectionException, ReturnStatusException
  {
    StudentHistory2013 oppHData =  loader.loadOppHistory (connection, oppkey, segment.getSegmentKey ());
    
    customPool = oppHData.getCustomPool ();
    previousGroups = oppHData.getGroups ();// ArrayList<HashSet<String>> _previousTestItemGroups
    excludeGroups = oppHData.getExcludeGroups ();
    responses = oppHData.getResponses ();
    startAbility = oppHData.getStartAbility ();
    // make a copy
    bp = segment.getBp ();
    bp.startAbility = startAbility;
    
    ProcessResponses ();
    // initialized Pool and created itemGroups!
    initializePool ();

    // Preemptively filter items over strict maxes
    bp.pruneStrictMaxes ();

    // Recycle items if necessary and remove from consideration any previously
    // used that are not recycled
    if (previousGroups.size () > 0)
    {
      bp.recycleItems (priorAdmins);
      // Any itemgroups remaining flagged as used, remove from the collection

    }
    // remove all item groups that are completely pruned or remain in 'used'
    // status
    itemGroups.removeUsed ();

    ComputeSatisfaction ();

    return this.cset1;
  }

 

  /**
   * Add response item groups to excludeGroups, and update blueprin
   * satisfaction and ability estimates
 * @throws ReturnStatusException 
   */
  private void ProcessResponses () throws ReturnStatusException
  {
    ItemPool pool = segment.getPool ();
    TestItem item;
    List<ItemResponse> res = new ArrayList<ItemResponse> (responses.values ());
    int currentSegment = 1;
    Collections.sort (res);
    // update my local blueprint with this examinee's items and _responses for
    // blueprint satisfaction and ability estimation
    for (ItemResponse r : res) // Is the adaptive ability estimate computation
                               // commutative? If not, then we need to compute
                               // in the same order every time
    {
      item = pool.getItem (r.itemID);
      if (item == null) // then this response is to a sibling item not in this
                        // segment
      {
        item = pool.getSiblingItem (r.itemID);
      }
      // Why do we care about sibling items? Because the segments may include
      // common item groups with non-intersecting partitions of items
      if (item != null)
      {
        r.setBaseItem (item);
      }
      if (!excludeGroups.containsKey (r.groupID))
      {
        excludeGroups.put (r.groupID, r.groupID);
      }

      // if this is a segmented test, then overall ability estimates need to
      // percolate down the segments
      // This is solely for the integrity of strands
      if (r.segmentPosition > currentSegment)
      {
        currentSegment = r.segmentPosition;
        bp.SetStartAbility (bp.theta);
      }
      bp.ProcessResponse (r, segment.position);
    }
    if (bp.numAdministered == bp.minOpItems) {
      panicWeight = bp.minOpItems;
    }
    else {
      panicWeight = bp.minOpItems / (double) (bp.minOpItems - bp.numAdministered);
    }
  }

  /**
   * Create the examinee-specific item pool from his custom pool excluding
   * field test items preselected and itemgroups already selected
   * Also flag items administered previously on other tests
   */
  private void initializePool () throws ItemSelectionException
  {
	    String[] customItems = customPool.split (",");
	    TestItem item;
	    CSetItem cSetItem;
	    ItemGroup grp;
	    CsetGroup csetGrp;
	    ItemPool pool = segment.getPool ();

	    // create the customized itempool from the segment's pool and the custom
	    // pool received from the database
	    // exclude all item groups that were selected for this test or have been
	    // pre-selected for field test administration
	    for (String itemID : customItems)
	    {
	      item = pool.getItem (itemID);
	      // this may result in null if the custom pool is over all segments
	      if (item == null)
	        continue;
	     // If all items are notActive or FieldTest we will have empty itemGroups !
	      if (!excludeGroups.containsKey (item.groupID) && item.isActive && !item.isFieldTest)
	      {

	        grp = pool.getItemGroup (item.groupID);
	      // Here is the only point in code where we load ItemGroup from pool in itemGroups member
	        csetGrp = itemGroups.setAndGet (grp); // makes the csetgroup if it does not
	                                        // exist and adds to the collection
	        
	        cSetItem = new CSetItem (item, csetGrp);
	        csetGrp.addItem (cSetItem);

	        items.put (cSetItem.itemID, cSetItem);

	        bp.addCsItem (cSetItem); // add the item to the examinee's blueprint to
	                                // permit computation of poolsize per element
	        // do not add the item to the CSET group until we have processed the
	        // items for previous use and recycling

	        // record whether or not this item was used for this examinee in the
	        // past
	        for (CsetGroupString gs : this.previousGroups)
	        {
	          if (gs.exists (item.groupID))
	          {
	            priorAdmins.addItem (gs.sequence, cSetItem);
	          }
	        }
	      }
	    }
	    // QUESTION: Should we 'index' the used items by chronology? Or just run
	    // through them all when recycling.
	    priorAdmins.sortItems ();
  }
  
  /**
   * Computes blueprint satisfaction 
   * on all remaining itemgroups (not pruned or used)
   */
  private void ComputeSatisfaction () throws ItemSelectionException
  {
    // Compute blueprint metric on every itemgroup (CSETGroup) (add jitter?)
    // Sort itemgroups by blueprint metric
    // Remove itemgroups below threshold
    // Unit-Normalize itemgroup blueprint metrics
    // double epsilon = 0.001; // two metrics this close are considered equal
    double minMetric = 99999.0;
    double maxMetric = -99999.0;
    int cset1Size;
    Random rand = new Random ();
    int last;
    int i;
    CsetGroup group;
    CsetGroup firstGroup;
    if (cset1 == null)
      cset1 = new Cset1 (bp);
    for (CsetGroup grp : itemGroups.getValues ())
    {
      if (this.bp.adaptiveVersion == "bp2")
        grp.computeMetricByItem (bp, panicWeight); // grp.ComputeMetricByGroup(_bp,
                                                     // _panicWeight);
      else
        // assume adaptiveVersion = "bp1", the only other value at this point
        grp.computeMetricByGroup (bp, panicWeight);

      if (bp.cset1Order.equalsIgnoreCase("ABILITY"))
      {
        grp.computeAbilityDiff (bp.theta);
        grp.setBpJitter (grp.irtDiff);
      }
      else
        grp.setBpJitter (rand.nextDouble ());
    }

    List<CsetGroup> groups = new ArrayList<CsetGroup> (itemGroups.getValues ());
    Collections.sort (groups);

    if(groups == null || groups.isEmpty())
    {
    	// Cannot initialize Item Pool: item groups are empty
    	return;
    } else
    {
        firstGroup =  groups.get (0);    	
    }

    if (bp.numAdministered == 0)
      cset1Size = Math.max (bp.cSet1Size, bp.randomizerInitialIndex);
    else
      cset1Size = Math.max (bp.cSet1Size, bp.randomizerIndex);

    maxMetric = firstGroup.selectionMetric;
    // find the 'last' group index
    last = Math.min (cset1Size, groups.size ()) - 1;

    if (!firstGroup.metricComputed)
    { // then no metrics were computed. Set all group bpmetrics = 1 and return
      // top cset1size groups
      for (i = 0; i <= last; ++i) // preserve sorted ordering
      {
        group =  groups.get (i);
        group.normalizeBPMetric (1.0, 1.0); // there is other cleanup to do
        cset1.addItemgroup (group);
      }
      return;
    }
    // first, make sure we have a legitimate metric (work backwards from
    // cset1size)
    for (i = last; i >= 0; --i)
    {
      group =  groups.get (i);
      if (group.metricComputed)
      {
        last = i;
        minMetric = group.selectionMetric;
        break;
      }
    }
    // now proceed forward from 'last' to the outer boundary of the metric
    if (bp.cset1Order == "ALL")
    {
      for (i = last + 1; i < groups.size (); ++i)
      {
        group =  groups.get (i);
        if (!group.metricComputed || group.selectionMetric < minMetric)
        {
          last = i;
          break;
        }
      }
    }

    // normalize the cset groups' metrics and add each to the result set
    for (i = 0; i <= last; ++i)
    {
      group =  groups.get (i);
      group.normalizeBPMetric (minMetric, maxMetric);
      cset1.addItemgroup (group);
    }
    return;

  }

}
