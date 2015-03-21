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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.ContentLevelCollection;
import tds.itemselection.impl.item.CsetItem;

/**
 * @author akulakov
 * 
 */
public class CsetGroup extends ItemGroup implements Comparable<Object>
{
  private static Logger  _logger  = LoggerFactory.getLogger (CsetGroup.class);
	
  // ordinal priority relative to other cset1 groups based on sort by bpmetric
  // (descending) then irtDiff (ascending)
  public int                             priority;
  // ability of this itemgroup to contribute to satisfying blueprint,
  // higher values are better
  public double                          bpMetric;
  // the non-normalized version
  public double                          rawBpMetric;
  // average over IRT b of group's operational items using last computed
  // overall theta (not official adaptive algorithm metric)
  public double                          irtMetric;
  // distance of this group's IRT/ metric/ from the/ 'ideal' based on/ most
  // recent/ past ability estimate (lower values are better: 0 is a perfect
  // match)
  public double                          irtDiff;

  // IMPORTANT: irtMetric and irtDiff are based on the PREVIOUS ability estimate
  // and is used to sort by ability match AFTER bp satisfaction is determined

  // the number of items/ that, if administered, would put some bp element over
  // its maximum
  public int                             numOverMax;
  // number of items in group that are on a bp element that has not yet reached
  // its minimum
  public int                             numUnderMin;

  // official adaptive algorithm ability match metric
  public double                          abilityMatch    = 99999.0;
  public double                          rcAbilityMatch  = 0.0;

  public double                          abilityMetric   = -1.0;
  public double 						 rcAbilityMetric = -1.0; //added for 2013

  // a weighted linear combination of bpMetric and abilityMetric
  public double                          selectionMetric = 99999.0;

  // For old algorithm
  private Map<String, Integer> 			_contentLevels   = null;

  // 9/2012 added for cset1 computation: collection of {CLID, numItems}
  // AA2013
  public ContentLevelCollection 		contentLevels = null;

  private boolean                        _used           = false;
  private int                            _chronology     = 0;

  public boolean                         metricComputed  = false;
  private int                            ActiveCount;
  
  private Blueprint                      _bp             = null;
  private double                         _panicWeight    = 1.0;
  private double                         _jitter         = 0.0;
  
  private boolean _debug = false;

  private boolean pruned;

  public boolean isPruned() {
	return pruned;
}

public void setPruned(boolean pruned) {
	this.pruned = pruned;
}

public double getBpJitter ()
  {
    return _jitter;

  }

  public void 
  setBpJitter (double value)
  {
    _jitter = value;
  }

  // / <summary>
  // / If ANY item in the group was used, then the whole group is considered
  // used
  // / </summary>
  public int getChronology ()
  {
    return _chronology;
  }

  public void setChronology (int value)
  {
    _used = true;
    _chronology = Math.max (_chronology, value);
  }

  // / <summary>
  // / Has the itemgroup been used previously, excluding it from consideration
  // / </summary>
  public boolean getUsed ()
  {
    return _used;
  }

  public void setUsed (boolean value)
  {
    if (_chronology > 0)
    { // cannot set a group used if there is no history of usage
      // but you can unset usage, which returns the itemgroup to the pool
      _used = value;
    }
  }
  

  /// <summary>
  /// Returns the count of items that we'd like to administer with the group.
  /// May not end up being the number of items we actually administer.
  /// If maxItems == -1, then we want to administer all active items ideally.
  /// </summary>
  public int getIntendedSize()
  {
          if (maximumNumberOfItems != -1)
              return maximumNumberOfItems;
          int c = 0;
          for (TestItem item : items)
          {
              // still making sure that the item is active in the item bank.
              //  Not checking pruned though.  Also not checking used, but an item is
              //  used only if the group was used, so we don't have to worry about counting
              //  used items in an active group.
              if (item.isActive)
                  c++;
          }
          return c;
  }


  // / <summary>
  // / Current constructor
  // / </summary>
  // / <param name="groupID"></param>
  // / <param name="numRequired"></param>
  // / <param name="maxItems"></param>
  public CsetGroup (String groupID, int numRequired, int maxItems)
  {
    super (groupID, numRequired, maxItems);
  }

  // / <summary>
  public int compareTo (Object rhs)
  {
    CsetGroup r = (CsetGroup) rhs;
    int c;
    if (rhs == this)
      return 0;
    if (r.selectionMetric == this.selectionMetric)
    {
      c = Double.compare (this.selectionMetric + _jitter, r.selectionMetric + r._jitter);
      return c;
    }
    /*if (r.selectionMetric > this.selectionMetric)
      return 1;
    if (r.selectionMetric < this.selectionMetric)
      return -1;*/
    return Double.compare (r.selectionMetric, this.selectionMetric);
  }

  public void removeInactive ()
  {
    List<TestItem> removedItems = new ArrayList<TestItem> ();
    CSetItem csetItem = null;
    for (TestItem item : items)
    {
      if(item instanceof CSetItem) {
        csetItem = (CSetItem)item;
        if (csetItem != null && !csetItem.isActive ()) {
          removedItems.add (item);
        }
      }
    }
    for (TestItem item : removedItems)
    {
      items.remove (item);
    }
  }

  public void computeAbilityDiff (double examineeAbility) // = theta
  {
    double sum = 0.0;
    double avgB;
    int cnt = 0;
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive () && citem.Included)
      {
        sum += citem.getAverageB();
        ++cnt;
      }
    }
    if (cnt > 0)
    {
      avgB = sum / cnt;
      irtDiff = Math.abs (examineeAbility - avgB);
    }
    else
      irtDiff = 9999.0;
  }

  // / <summary>
  // / Sort the items within the group by their selection metric with weighted
  // blueprint metric.
  // / Call this ONLY for the final selected itemgroup that will be administered
  // on test for optimal performance
  // / </summary>
  public void sort (double bpWeight)
  {
    if (items.size () == 1)
      return;
    double itemMetric;
    double maxMetric = -9999.0;
    double minMetric = 9999.0;
    // Item-level blueprint metrics not yet computed
    for (TestItem item : getActive ())
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null)
      {
        itemMetric = citem.computeBPMetric (this._bp, this._panicWeight);
        minMetric = Math.min (minMetric, itemMetric);
        maxMetric = Math.max (maxMetric, itemMetric);
      }
    }

    // The blueprint metric may be weighted differently when sorting items
    // within the group than when building the group metric
    for (TestItem item : getActive ())
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null)
        citem.setSelectionMetric (bpWeight, minMetric, maxMetric);
    }
    Collections.sort (this.items);
  }

  // previous method for 2013
  public void sort(Blueprint bp, boolean useRandomTieBreak, Random r)
  {
	    if (items.size () == 1)
	        return;
	      double itemMetric;
	      double maxMetric = -9999.0;
	      double minMetric = 9999.0;
      // Item-level blueprint metrics not yet computed
      //NOTE: will have been computed already if using bp2, but not bp1, and BPMatchByItemWithIterativeGroupItemSelection
      //  will likely have computed a value that is not based on this bp (and therefore not useful).  So continue to recalc here.
      for (CSetItem item : getActiveIncluded())
      {
          itemMetric = item.computeBPMetric(bp);
          minMetric = Math.min(minMetric, itemMetric);
          maxMetric = Math.max(maxMetric, itemMetric);
      }

      // The blueprint metric may be weighted differently when sorting items within the group than when building the group metric
      for (CSetItem item : getActiveIncluded())
      {
          item.setSelectionMetric(bp.itemWeight, minMetric, maxMetric, bp.abilityWeight, bp.rcAbilityWeight);  //will use previously calculated ability metric.
          item.BpJitter = useRandomTieBreak ? r.nextDouble() : 0.0;
      }
      Collections.sort (this.items);
  }
   
  // / <summary>
  // / Add an item to the group
  // / </summary>
  // / <param name="item"></param>
  public void addItem (CsetItem item)
  {
    this.items.add (item);
    // For customized itempools, this can only be reliable in the CsetGroup
    // object

  }

  // / <summary>
  // / Compute the blueprint satisfaction metric for this itemgroup
  // / </summary>
  // / <param name="bp"></param>
  public void computeMetricByGroup (Blueprint bp, double panicWeight)
  {
    // FUNCTION CASES
    // Met or exceeded maximum allowed
    // Below minimum required
    // Between min and max required

    // NOTE that this computation DOES NOT use the number of _items in each
    // contentlevel, only that it can satisfy to some degree
    // That is because some groups may be overpopulated and are pruned at the
    // end

    // if this group makes the cut, then it will need the blueprint and panic
    // weight to compute metrics on its _items
    _bp = bp;
    _panicWeight = panicWeight;
    BpElement cl;
    double sum = 0.0;
    int cnt = 0;
    double tmp;
    computeContentLevelCounts ();

    for (String clID : _contentLevels.keySet ())
    {
      cl = bp.elements.getElementByID (clID);
      if (cl.weight > 0)
      {
        ++cnt;
        // first check for max met or exceeded
        if (cl.maxRequired <= cl.numAdministered)
        {
          tmp = cl.weight * (cl.maxRequired - cl.numAdministered);
          // note that tmp is not used in the final calculation. It is for
          // debugging only
          sum += cl.weight * (cl.maxRequired - cl.numAdministered);
        }
        // next check for below min requirement (employ panic weight which
        // increases at end of test approaches)
        else if (cl.numAdministered < cl.minRequired)
        { // largest weight
          tmp = (double) cl.numAdministered / (double) cl.minRequired;
          tmp = 2.0 - tmp;
          sum += cl.weight * panicWeight * tmp;
        }
        // slide toward max with lesser weight
        else if (cl.numAdministered >= cl.minRequired && cl.numAdministered < cl.maxRequired)
        {
          tmp = (double) (cl.numAdministered - cl.minRequired) / (double) (cl.maxRequired - cl.minRequired);
          tmp = 1.0 - tmp;
          // FYI: this condition cannot hold if min == max
          sum += cl.weight * tmp;
        }

      }
    }
    // Use selectionMetric to store value to leverage the CompareTo method
    if (cnt > 0)
    {
      metricComputed = true;
      rawBpMetric = selectionMetric = sum / cnt;
    }
    else
    {
      metricComputed = false;
      selectionMetric = -9999; // no content levels found, this group can
                               // contribute nothing to blueprint satisfaction
    }

  }

  // / <summary>
  // / Computes the group's blueprint metric item by item,
  // / </summary>
  // / <param name="bp"></param>
  // / <param name="panicWeight"></param>
  public void computeMetricByItem (Blueprint bp, double panicWeight)
  {
    double sum = 0, itemMetric;
    int maxitms = getMaxItems(), cnt = 0;
    int active = ActiveCount;

    // if this group makes the cut, then it will need the blueprint and panic
    // weight to compute metrics on its _items
    _bp = bp;
    _panicWeight = panicWeight;
    if (maxitms < active)
      sum = 0; // this useless statement for debugging breakpoint
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive ())
        itemMetric = citem.computeBPMetric (bp, panicWeight);
    }
    if (maxitms < active) // this sort needed only if we are selecting by
                          // maxitems
    {
      Collections.sort (items);
      // Prune(active - maxitems);
    }
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive ())
      {
        sum += citem.rawBpMetric;
        ++cnt;
        if (cnt >= maxitms)
          break;
      }
    }
    // Use selectionMetric to store value to leverage the CompareTo method
    if (cnt > 0)
    {
      metricComputed = true;
      rawBpMetric = selectionMetric = sum / cnt;
    }
    else
    {
      metricComputed = false;
      selectionMetric = -9999; // no content levels found, this group can
                               // contribute nothing to blueprint satisfaction
    }
    return;
  }

  // / <summary>
  // / Compute the blueprint satisfaction metric for this itemgroup
  // / Experimental version to test alternatives to metric computation
  // / </summary>
  // / <param name="bp"></param>
  public void computeBPMetricExperimental (Blueprint bp, double panicWeight)
  {
    // FUNCTION CASES
    // Met or exceeded maximum allowed
    // Below minimum required
    // Between min and max required

    // NOTE that this computation DOES NOT use the number of _items in each
    // contentlevel, only that it can satisfy to some degree
    // That is because some groups may be overpopulated and are pruned at the
    // end

    // if this group makes the cut, then it will need the blueprint and panic
    // weight to compute metrics on its _items
    _bp = bp;
    _panicWeight = panicWeight;
    BpElement cl;
    double sum = 0.0;
    int cnt = 0;
    double tmp;
    computeContentLevelCounts ();

    // CHANGE: Compute for each item instead of content level
    // Where an item contributes to blueprint satisfaction (on a content level),
    // then add positive to metric
    // Where an item contributes to a max overrun, then subtract from metric
    // Where an item makes neither positive nor negative contribution, ignore
    // Do not average by number of _items in the group

    // TBD:
    // -- What penalty if group WOULD go over a strict max?
    // -- What benefit for groups that satisfy an entire content level?
    // -- How to identify and 'value' _items that make multiple contributions
    // without exceeding a strict max?
    // -- Should strict max violations be weighted more heavily than
    // non-strict-max violations?
    // -- Should this factor in pruning 'prevention' (where the group may exceed
    // a max in the act of satisfying it and _items may not be pruned)?

    for (String clID : _contentLevels.keySet ())
    {
      cl = bp.elements.getElementByID (clID);
      if (cl.weight > 0)
      {
        ++cnt;
        // first check for max met or exceeded
        if (cl.maxRequired <= cl.numAdministered)
        {
          tmp = cl.weight * (cl.maxRequired - cl.numAdministered);
          // note that tmp is not used in the final calculation. It is for
          // debugging only
          sum += cl.weight * (cl.maxRequired - cl.numAdministered);
        }
        // next check for below min requirement (employ panic weight which
        // increases at end of test approaches)
        else if (cl.numAdministered < cl.minRequired)
        { // largest weight
          tmp = (double) cl.numAdministered / (double) cl.minRequired;
          tmp = 2.0 - tmp;
          sum += cl.weight * panicWeight * tmp;
        }
        // CHANGE: No contribution to blueprint satisfaction, positive or
        // negative
        else if (cl.numAdministered >= cl.minRequired && cl.numAdministered < cl.maxRequired)
        {
          tmp = (double) (cl.numAdministered - cl.minRequired) / (double) (cl.maxRequired - cl.minRequired);
          tmp = 1.0 - tmp;
          // CHANGE: Do not record this neutral contribution
          // sum += cl.weight * tmp;
        }

      }
    }

    // Use selectionMetric to store value to leverage the CompareTo method
    if (cnt > 0)
    {
      metricComputed = true;
      rawBpMetric = selectionMetric = sum; // / cnt;
    }
    else
    {
      metricComputed = false;
      selectionMetric = -9999; // no content levels found, this group can
                               // contribute nothing to blueprint satisfaction
    }

  }

  // / <summary>
  // / Normalizes the selection metric and places the result in bpMetric
  // / </summary>
  // / <param name="min"></param>
  // / <param name="max"></param>
  public void normalizeBPMetric (double min, double max)
  {
    // THis is a unit normalized value translated by +1 so that bpweight *
    // bpmetric has a floor of bpweight
    // also reinitialize selectionMetric to initial value in preparation for
    // ability match computation
    if (min == max)
    {
      bpMetric = 2.0;
    }
    else
    { // scale the selectionMetric
      bpMetric = 1 + (selectionMetric - min) / (max - min);
    }
    selectionMetric = abilityMatch;
  }

  // / <summary>
  // / Normalizes the selection metric and places the result in bpMetric
  // / </summary>
  // / <param name="min"></param>
  // / <param name="max"></param>
  // TODO 1. added interface to CsetGroup
  //		2. split CsetGroup on old and new CsetGroup2013
  //		3. normalizeBPMetric2013 (double min, double max) 
  //			is normalizeBPMetric() for CsetGroup2013
  public void normalizeBPMetric2013 (double min, double max)
  {
	// also reinitialize selectionMetric to initial value 
	// in preparation for ability match computation
    if (min == max)
    {
      bpMetric = 1.0;
    }
    else
    { // scale the selectionMetric
      bpMetric = (selectionMetric - min) / (max - min);
    }
    selectionMetric = abilityMatch;
  }

  // / <summary>
  // / Compute the group content level counts from active items
  // / </summary>
  private void computeContentLevelCounts ()
  {
    // 9/2012: Compute the content level counts for the itemgroup to use in
    // cset1 computation
    // Each item group is specific to each student since items may be customized
    // and the itemgroup
    _contentLevels = new HashMap<String, Integer> ();
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive () && citem.getContentLevels () != null)
      {
        for (String cl : citem.getContentLevels ())
        {
          if (!this._contentLevels.containsKey (cl))
          {
            this._contentLevels.put (cl, 1);
          }
          else
          {
            this._contentLevels.put (cl, (int) this._contentLevels.get (cl) + 1);
          }
        }
      }
    }
  }

  // / <summary>
  // / Sets the ability match and the linear combination final selection metric
  // for the group
  // / </summary>
  // / <param name="abilityMatch"></param>
  // / <param name="bpWeight"></param>
  public void setSelectionMetric (double blueprintWeight, double abilityWeight, 
		  double minItemAbility, double maxItemAbility)
  {
    double abilitySum = 0.0;
    // double selectionSum = 0.0;
    for (TestItem item : getActive ())
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive ())
      {
        citem.setAbilityMetric (minItemAbility, maxItemAbility);
        abilitySum += citem.abilityMetric;
        // selectionSum += item.selectionMetric;
      }
      this.abilityMetric = abilitySum / this.ActiveCount;
      this.selectionMetric = blueprintWeight * bpMetric + abilityWeight * abilityMetric;
    }
  }
  /// <summary>
  /// Sets the ability match and the linear combination final selection metric for the group
  /// </summary>
  /// <param name="abilityMatch"></param>
  /// <param name="bpWeight"></param>
  public void setSelectionMetric2013(double blueprintWeight, double abilityWeight, double minItemAbility, double maxItemAbility,
      double rcAbilityWeight, Double rcMinItemAbility, Double rcMaxItemAbility)
  {
      double abilitySum = 0.0;
      double rcAbilitySum = 0.0;

      List<CSetItem> activeIncluded = getActiveIncluded();
      for (CSetItem item : activeIncluded)
      {
          if (abilityWeight > 0)
          {
              item.setAbilityMetric(minItemAbility, maxItemAbility);
              abilitySum += item.abilityMetric;
          }

          if (rcAbilityWeight > 0 && rcMinItemAbility != null)
          {
              item.SetRCAbilityMetric(rcMinItemAbility, rcMaxItemAbility);
              rcAbilitySum += item.rcAbilityMetric;
          }
      }

      this.abilityMetric = abilitySum / activeIncluded.size();
      this.rcAbilityMetric = rcAbilitySum / activeIncluded.size();

      this.selectionMetric = (blueprintWeight * bpMetric) + (abilityWeight * abilityMetric) + (rcAbilityWeight * rcAbilityMetric);
  }

  // / <summary>
  // / This method finds an item in the group's collection
  // / </summary>
  // / <param name="itemID"></param>
  // / <returns></returns>
  public TestItem getItem (String itemID)
  {
    for (TestItem item : items)
    {
      if (item != null && item.itemID == itemID)
        return item;
    }
    return null;
  }

  // / <summary>
  // / Get the count of _items not pruned
  // / </summary>
  public int getActiveCount ()
  {
    int cnt = 0;
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive ())
        ++cnt;
    }
    return cnt;
  }
  
  public int getActiveIncludedCount ()
  {
    int cnt = 0;
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive () && citem.Included)
        ++cnt;
    }
    return cnt;
  }

  public int getMaxItems ()
  {
    int active = getActiveCount();
    if (this.maximumNumberOfItems == -1 || this.maximumNumberOfItems >= active)
      return active;
    else
      return maximumNumberOfItems;

  }

  // / <summary>
  // / Get all active _items
  // / </summary>
  public List<TestItem> getActive ()
  {
    List<TestItem> result = new ArrayList<TestItem> ();
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.isActive ())
        result.add (item);
    }
    return result;

  }

  // / <summary>
  // / Array of counts of _items (not pruned) on each bp element in positional
  // correspondence
  // / </summary>
  public int[] getBpCounts (BpElement[] elems)
  {
    int n = elems.length;
    int[] result = new int[n];
    BpElement elem;
    for (int i = 0; i < n; ++i)
    {
      result[i] = 0;
      elem = elems[i];
      for (TestItem item : getActive ())
      {
        CsetItem citem = null;
        if (item instanceof CsetItem)
        {
          citem = (CsetItem) item;
        }
        if (citem != null && item.hasContentlevel (elem.ID))
          result[i]++;
      }
    }
    return result;
  }

  // / <summary>
  // / Array of counts of _items (not pruned) on each bp element in positional
  // correspondence
  // / This differs from BpCounts(BpElement[]) in that it reuses the result
  // array.
  // / </summary>
  public void getBpCounts (BpElement[] elems, int[] result)
  {
    int n = elems.length;
    BpElement elem;
    for (int i = 0; i < n; ++i)
    {
      result[i] = 0;
      elem = elems[i];
      for (TestItem item : getActive ())
      {
        CsetItem citem = null;
        if (item instanceof CsetItem)
        {
          citem = (CsetItem) item;
        }
        if (citem != null && item.hasContentlevel (elem.ID))
          result[i]++;
      }
    }
    return;
  }
  // true if all active items in the group have the required flag set
  public boolean isAllItemsRequired()
  {
	  boolean isAllItemsRequired = true;
	  for(TestItem item: getActive ())
	  {
		  isAllItemsRequired &=  item.isRequired;
	  }
	  return isAllItemsRequired;
  }

  // / <summary>
  // / Prunes the item closest to the BOTTOM of the rank order with the given
  // content level
  // / </summary>
  // / <param name="contentLevel"></param>
  public boolean prune (String contentLevel)
  {
    int n = items.size ();
    CsetItem item;
    for (int i = n - 1; i >= 0; --i)
    {
      item = (CsetItem) items.get (i);
      if (item.isActive () && !item.isRequired () && item.hasContentlevel (contentLevel))
      {
        item.pruned = true;
        return true;
      }
    }
    return false;

  }

  // / <summary>
  // / Prunes 'p' items lowest on the rank order. Does NOT consider how many
  // items have already been pruned
  // / </summary>
  public void prune (int p)
  {
    int n = items.size ();
    int pruned = 0;
    CsetItem item;
    for (int i = n - 1; i >= 0; --i)
    {
      if (pruned >= p)
        return;
      item = (CsetItem) items.get (i);
      if (item.isActive () && item.Included &&  !item.isRequired)
      {
        item.pruned = true;
        ++pruned;
      }
    }

  }

  // / <summary>
  // / Release all pruned required items
  // / (Required items may not remain pruned in a non-empty group)
  // / </summary>
  public void releaseRequiredPruned ()
  {
    // allow required items to be preemptively pruned to see if whole group is
    // pruned
    for (TestItem item : items)
    {
      CsetItem citem = null;
      if (item instanceof CsetItem)
      {
        citem = (CsetItem) item;
      }
      if (citem != null && citem.pruned && item.isRequired)
        citem.pruned = false;
    }
  }
  /// <summary>
  /// If the group was pruned in its entirety, then unprune it.
  /// </summary>
  /// <param name="requiredOnly">unprune only required items.</param>
  /// <returns>The number of items unpruned</returns>
  public int UnpruneGroup(boolean requiredOnly)
  {
      int unprunedCount = 0;

      if (!this.pruned)
          return unprunedCount;

      for (TestItem item : items)
      {
    	  if(item instanceof CsetItem)
    	  {
    		  CsetItem  itm = (CsetItem)item;
	          if (itm.pruned)
	          {
	              if (!requiredOnly || item.isRequired)
	              {
	            	  itm.pruned = false;
	                  unprunedCount++;
	              }
	          }
    	  }
      }
      pruned = false;
      return unprunedCount;
  }
  /// <summary>
  /// Get all items marked as included in the group.  When using the iterative bp-match
  /// formula, this may be a subset of active items.  All items are included in the group
  /// by default.
  /// </summary>
  public List<CSetItem> getActiveIncluded()
  {
           List<CSetItem> result = new ArrayList<CSetItem>();
          for (TestItem itm : items)
          {
        	  if(itm instanceof CSetItem && itm.isActive)
        	  {
        		  CSetItem item = (CSetItem) itm;  
              if (item.isActive () && item.Included)
                  result.add(item);
        	  }
          }
          return result;
  }

  /// <summary>
  /// AM: changed from ComputeContentLevelCounts and no longer keeping
  ///     counts of items per CL, since those weren't being used.
  /// populate the group content levels from active items
  /// </summary>
  public void populateContentLevels()
  {
      // Each item group is specific to each student since items may be customized and the itemgroup 
      contentLevels = new ContentLevelCollection();
      for (TestItem item : items)
      {
          boolean isActiveFlag = item.isActive;
          if(item instanceof CSetItem) {
            isActiveFlag = ((CSetItem)item).isActive ();
          }
          if (isActiveFlag && item.contentLevels != null)
          {
              for (String cl : item.contentLevels)
              {
                  if (!this.contentLevels.contains(cl))
                  {
                      this.contentLevels.add(cl);
                  }
              }
          }
      }
   }

  //==============================NEW============================================================
  /// <summary>
    /// Array of counts of items (not pruned) on each bp element in positional correspondence 
    /// </summary>
    public int[] BpCounts(List<BpElement> elems, boolean requiredOnly, boolean includedOnly)
    {
        int[] result = new int[elems.size()];
        BpCounts(elems, requiredOnly, includedOnly, result);
        return result;
    }

    /// <summary>
    /// Array of counts of items (not pruned) on each bp element in positional correspondence 
    /// This differs from BpCounts(BpElement[]) in that it reuses the result array.
    /// </summary>
    public void BpCounts(List<BpElement> elems, boolean requiredOnly, boolean includedOnly, int[] result)
    {
        for (int i = 0; i < elems.size(); ++i)
        {
            result[i] = BpCount(elems.get(i), requiredOnly, includedOnly);
        }
        return;
    }

    /// <summary>
    /// Returns the count of active items in the group associated with the bp element passed in.
    /// </summary>
    /// <param name="elem"></param>
    /// <param name="requiredOnly">Counts only required items</param>
    /// <returns></returns>
  public int BpCount(BpElement elem, boolean requiredOnly,
      boolean includedOnly) {
    int count = 0;
    CSetItem csetItem = null;
    for (TestItem item : getActive()) {
      if(item instanceof CSetItem) {
        csetItem = (CSetItem) item;
        if (csetItem.hasContentlevel(elem.ID) &&
              (!requiredOnly || csetItem.isRequired) &&
              (!includedOnly || csetItem.Included)) {
            count++;
        }
      }
    }
    return count;
  }
//==============================END NEW======================================================
}
