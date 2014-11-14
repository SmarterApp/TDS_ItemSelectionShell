/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import AIR.Common.DB.SQLConnection;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.Strand;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.impl.math.AAMath;
import tds.itemselection.impl.math.BStarEquation;
import tds.itemselection.impl.math.PhistarDiff;
import tds.itemselection.impl.sets.Cset1;
import tds.itemselection.impl.sets.Cset1Factory;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.SegmentCollection;
import tds.itemselection.loader.TestSegment;

/**
 * @author akulakov
 * 
 */
public class AdaptiveSelector  extends AbstractAdaptiveSelector implements IItemSelection
{
  @Autowired
  @Qualifier("itemDBLoader")
  private IItemSelectionDBLoader loader = null;
  
  // a combination of test-constant and examinee-variable data
  Blueprint          blueprint;

  Cset1              cset1;

  ItemCandidatesData itemCandidates;
  // itemCandidates contains oppkey, segmentKey, itempool;
  TestSegment        segment;
  Cset1Factory       csetFactory;

  // min/max ability are used to determine final range of ability matches
  // computed and normalize all ability matches to range [0,1]
  double             minItemAbility = 9999.0;
  double             maxItemAbility = -9999.0;
  int                itemsRequired  = -1;
  int                maxItems       = -1;
  private boolean 	_debug = false;
  
  private static Logger  _logger  = LoggerFactory.getLogger (AdaptiveSelector.class);
  
  public ItemGroup getNextItemGroup (SQLConnection connection,
			ItemCandidatesData itemData) throws ItemSelectionException {
	  
    final String messageTemplate = "Exception %1$s executing adaptive algorithm. Exception error: %2$s";

    ItemGroup result = null;
    String error = "";

    try {
      
      itemCandidates = itemData;

      SegmentCollection segs = SegmentCollection.getInstance ();
      segment = segs.getSegment (connection, itemCandidates.getSession (), itemCandidates.getSegmentKey (), loader);
      if (segment == null)
      {
        error = "Unable to load blueprint";
        _logger.error (String.format (messageTemplate, "AdaptiveSelection", error));
        throw new ItemSelectionException (error);
      }

      result = selectNext (connection);

      if (result == null) {
        error = "Adaptive item selection failed: Unknown error";
      }
      if (error != null && !error.isEmpty()) {
        _logger.error (String.format (messageTemplate, "AdaptiveSelector", error));
      }

    } catch (ItemSelectionException ie)
    {
      _logger.error (String.format (messageTemplate, "ItemSelectionException", ie.getMessage()));
      throw new ItemSelectionException (error);
    } catch (Exception e)
    {
      _logger.error (String.format (messageTemplate, "Exception", e.getMessage()));
      throw new ItemSelectionException (error);
    }
    return result;
  }


  /* *
   *  1. Compute initial candidate itemgroup set (CSET1)
   * (moved outside of adaptive selector)
   *  2. Compute second candidate itemgroup set CSET2
   *  3. Return best itemgroup within CSET2
   */
  public ItemGroup selectNext (SQLConnection connection) throws ItemSelectionException {

    csetFactory = new  Cset1Factory(itemCandidates.getOppkey (), loader, segment);
    cset1 = csetFactory.MakeCset1 (connection);
    this.blueprint = cset1.getBlueprint ();

    if (this.cset1.itemGroups.size () < 1)
      return null;

    int minitems = Math.max (1, this.blueprint.randomizerIndex);
    int minfirstitems = Math.max (1, this.blueprint.randomizerInitialIndex);

    double wt;

    CsetGroup cg = null;

    try
    {
      wt = OverallWeight (blueprint.info);
      // we randomly select from the top x groups
      // 5/2011: For math tests of only strands in blueprint, there is too much
      // sameness of the 2nd to ~5 items across tests.
      // This is due to the tendency to cycle through the strands, one for each
      // item, and of students to maintain
      // the same initial ability until the first cycle of strands is completed.
      if (blueprint.numAdministered < blueprint.getNumStrands ())
      {
        minitems = minfirstitems;
      }
      Random rand = new Random ();
      int n = Math.min (minitems, cset1.itemGroups.size ());

      ComputeStrandParms (wt);
      for (CsetGroup group : this.cset1.itemGroups)
      {
        ComputeAbilityMatch (group);
      }

      // Once the ability match for each item is computed, call cset1 to
      // finalize the selection metrics
      // by normalizing ability metrics and combining with blueprint metrics.
      // SetSelectionMetrics also orders the csetgroups from best (index 0) to
      // worst (index n -1)
      cset1.setSelectionMetrics (blueprint.bpWeight, minItemAbility, maxItemAbility);

      // Idea: we don't return the best group. We return random group between n first.
      // Parameter n = 4---6 ???
      int index = rand.nextInt (n);
      cg = cset1.itemGroups.get (index);

      cg.sort (blueprint.itemWeight);
      this.PruneItemgroup (cg);

      ItemGroup result = new ItemGroup (cg.groupID, blueprint.segmentKey,
          blueprint.segmentPosition, cg.getNumRequired (), cg.getMaxItems ());
      for (TestItem item : cg.getActive ())
      {
        result.addItem (item);
      }
      // result.items.Sort();

      return result;

    } catch (Exception e)
    {
      _error = e.getMessage ();
      throw new ItemSelectionException("Error occurs in selectNext () method: " + e.getMessage ());
    }

  }

  private double OverallWeight (double I)
  {
    if (I < 0.01)
      return 100.0;
    else
      return Math.min (1.0 / I, 100.0);
  }

  // / <summary>
  // / Compute the distance between the difficulty of an itemgroup and the ideal
  // examinee difficulty (bstar)
  // / </summary>
  public void ComputeAbilityMatch (CsetGroup group)
  {
    double sum = 0.0; // this is only needed if
    double term, bsquar; // term is the ability metric for each item, bsquar is
                         // goodness of fit to strand ideal b

    Strand strand;

    for (TestItem itm : group.items)
    {
      // TODO: check casting
      CsetItem item = (CsetItem) itm;
      strand = this.blueprint.getStrand (item.strandName);
      if (strand != null)
      {
        bsquar = (item.b - strand.bstar) * (item.b - strand.bstar);
        term = strand.phistar + strand.phidiff * bsquar;
        item.abilityMatch = term;

        // the following for normalization

        minItemAbility = Math.min (minItemAbility, term);
        maxItemAbility = Math.max (maxItemAbility, term);

        // for the group average ability match
        sum += term;

        // for debugging purposes only
        item.b_bstar = bsquar;
        item.phiDiff = strand.phidiff;
        item.phiStar = strand.phistar;
      }
      else // strand == null
      {
        // TODO: (AK) What I need to do?
      }
    }
    group.abilityMatch = sum / group.getActiveCount();
    // this is merely an intermediate term which is obsolete as of 2012
    // Note that this is not the final step as final selection metrics require
    // the following 2 steps:
    // 1. Normalize all ability matches to the same scale as blueprint match:
    // [0,1]
    // 2. Linearly combine normalized ability metric and blueprint metric to
    // obtain the final selection metric
  }

  // / <summary>
  // / Compute the adaptive algorithm parameters for each strand and store in
  // each respective strand object.
  // / </summary>
  private void ComputeStrandParms (double overallWeight)
  {
    // Process:
    // -- (Previously done: Compute student estimated theta, lambda, and info on
    // each strand. See Strand.UpdateAbility)
    // 2. Compute strand weights w.r.t. adaptive ability match
    // 3. Compute gammas
    // 4. Compute b* values
    // 5. Compute phi* values
    // 6. Compute phi diff for each strand

    BStarEquation bstar = new BStarEquation (); // reuse with unique
                                                // initialization for each
                                                // strand
    PhistarDiff phiDiff = new PhistarDiff (); // use evaluate function that
                                              // takes ALL parameters and
                                              // performs a single computation

    for (Strand strand : blueprint.strands)
    {
      strand.adaptiveWeight = strand.weight * (Math.exp (-strand.lambda / 2) * Math.sqrt (2.0 / (strand.lambda * Math.PI))) / 2.0;
      strand.gamma = strand.adaptiveWeight / overallWeight;

      bstar.initialize (blueprint.theta, strand.theta, strand.adaptiveCut, strand.gamma);
      strand.bstar = blueprint.abilityOffset + bstar.solveBisection (-10.0, +20.0);
      strand.phistar = ComputePhistar (strand.theta, strand.bstar, overallWeight, strand.adaptiveWeight, strand.adaptiveCut);
      strand.phidiff = phiDiff.Evaluate (strand.bstar, strand.adaptiveCut, blueprint.theta, strand.theta, strand.lambda, overallWeight, strand.adaptiveWeight, strand.info);
    }
  }

  private double ComputePhistar (double theta, double bstar, double overallWt, double strandWt, double cutscore)
  {
    double btheta, lambda, u;
    btheta = AAMath.probCorrect (theta, bstar);
    lambda = lambdaK (bstar, theta, cutscore);
    u = btheta * (1 - btheta);
    return overallWt * u + strandWt * lambda;
  }

  // / <summary>
  // / Helper function to ComputePhistar
  // / </summary>
  // / <param name="b"></param>
  // / <param name="thetaK"></param>
  // / <param name="cutK"></param>
  // / <returns></returns>
  private double lambdaK (double b, double thetaK, double cutK)
  {
    double ptheta = AAMath.probCorrect (thetaK, b);
    double pcut = AAMath.probCorrect (cutK, b);
    double term1 = ptheta * Math.log (ptheta) + (1 - ptheta) * Math.log (1 - ptheta);
    double term2 = ptheta * Math.log (pcut) + (1 - ptheta) * Math.log (1 - pcut);
    return 2 * term1 - 2 * term2;
  }

  // / <summary>
  // / Prunes unwanted items from a group
  // / </summary>
  // / <param name="group"></param>
  private void PruneItemgroup (CsetGroup group)
  {
    if (group.getActiveCount() <= 1)
      return; // must have more than one item to prune

    // First prune for strict maxes
    // Then prune for items over the group max
    // Then prune for items over the test max length
    // In no case should the group be pruned down to zero.

    PruneStrictMaxes (group);
    if (group.getActiveCount() <= 1)
      return;

    // prune for items over the max allowed for the group
    int overage = group.getActiveCount() - group.getMaxItems();
    if (overage > 0)
      group.prune (overage);
    if (group.getActiveCount() <= 1)
      return;

    // Prune for items over max test length
    overage = (group.getActiveCount() + blueprint.numAdministered) - blueprint.maxOpItems;
    if (overage > 0)
      group.prune (overage);

  }

  // / <summary>
  // / Flags items in the group that violate strict max
  // / </summary>
  // / <param name="group"></param>
  private void PruneStrictMaxes (CsetGroup group)
  {

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

    BpElement[] maxes = blueprint.getStrictmaxVector ();
    int[] bpCounts = group.getBpCounts (maxes); // the (initial) count of items
                                                // on each strict max element
    boolean more = true;
    int n = maxes.length;

    //
    while (more)
    {
      more = false;
      for (int i = 0; i < n; ++i)
      {
        if (group.getActiveCount() > 1 && bpCounts[i] + maxes[i].numAdministered > maxes[i].maxRequired)
        {
          // prunes exactly one item at a time
          more = group.prune (maxes[i].ID); // it may not be possible to prune
                                            // ANY item
          // because an item is likely on more than one content level, refresh
          // the counts vector to prevent over-pruning
          if (more) // only need to refresh bpcounts if an item was successfully
                    // pruned
            // TODO: (AK) needed setBpCounts here!
            bpCounts = group.getBpCounts (maxes);
        }
      }
    }
  }
}
