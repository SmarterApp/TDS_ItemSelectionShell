/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.item;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.ContentLevelCollection;
import tds.itemselection.impl.sets.CsetGroup;

/**
 * @author akulakov
 * 
 */
public class CsetItem extends TestItem
{
  private static Logger  _logger  = LoggerFactory.getLogger (CsetItem.class);
  // / <summary>
  // / An item in the Cset1 class. This object is always specific to an examinee
  // / </summary>

  // -- Items in the pool customized for this testee according to accommodations
  // assigned
  // create table #itempool (GID varchar(50), itemkey varchar(50), bpmetric
  // float, isRequired bit, aaMetric float, admincount int, lastUsed datetime,
  // irtB float, used int );
  private CsetGroup _parentGroup;

  public double     bpMetric;                                                  // final
                                                                                // unit-normalized
                                                                                // metric
  public double     workingMetric;                                             // working
                                                                                // version
                                                                                // from
                                                                                // which
                                                                                // normalized
                                                                                // is
                                                                                // derived

  public double     abilityMatch  = 0.0;                                       // computed
                                                                                // by
                                                                                // adaptive
                                                                                // algorithm
  public double     rcAbilityMatch  = 0.0;
  public double     abilityMetric = 0.0;
  public double     selectionMetric = 0.0;
  public double     rawBpMetric   = 0.0;                                       // if
                                                                                // Csetgroup
                                                                                // computes
                                                                                // bpmetric
                                                                                // item-by-item,
                                                                                // it
                                                                                // will
                                                                                // need
                                                                                // this
                                                                                // value
  // ====================================
  public double     b_bstar;                                                   // for
                                                                                // debugging
                                                                                // purposes
                                                                                // only
  public double     phiStar;                                                   // for
                                                                                // debugging
  public double     phiDiff;                                                   // for
                                                                                // debugging
  // ====================================
	public boolean pruned = false;

	public boolean isPruned() {
		return pruned;
	}

	public void setPruned(boolean pruned) {
		this.pruned = pruned;
	}

// used by adaptive algorithm to prune the final selected itemgroup of undesirable
// items
  /**
   * Members for AdaptiveAlgorithm2013 only
   * populated in CsetGroup.populateContentlevel
  */
  protected ContentLevelCollection contentLevelCollection = null;


  public ContentLevelCollection getContentLevelCollection() {
	return contentLevelCollection;
}

public void setContentLevelCollection(
		ContentLevelCollection contentLevelCollection) {
	this.contentLevelCollection = contentLevelCollection;
}

// / <summary>
  // / An item is considered used if the group it has been in was used
  // / </summary>
  public boolean getUsed ()
  {
    return _parentGroup.getUsed ();

  }
  /// <summary>
  /// This is used with the new BP-match routine.  Indicates whether or not an item has been selected for inclusion in the group.
  /// Temporarily treates an item as used.  Default = false
  /// </summary>
  public boolean ItemUsed = false;
  /// <summary>
  /// Whether or not an item is included in the group.  Used by new bp-match routine to indicate which items should be
  /// included with the group if it is selected.  Default = true;
  /// </summary>
  public boolean Included = true;

  // / <summary>
  // / How recently was the item(group) previously administered?
  // / </summary>
  public int getChronology ()
  {
    return _parentGroup.getChronology ();

  }

  public CsetGroup getParentGroup () {
    return _parentGroup;
  }

  // / <summary>
  // / Cset1 factory may remove this item from the pool even though the base
  // item is active
  // / </summary>
  public boolean isActive ()
  {
    return !pruned && !_parentGroup.getUsed () && this.isActive;
  }

//  // / <summary>
//  // / Deprecated constructor. Was used when Cset1 was computed in database
//  // / </summary>
//  // / <param name="groupID"></param>
//  // / <param name="itemID"></param>
//  // / <param name="position"></param>
//  // / <param name="isRequired"></param>
//  // / <param name="strand"></param>
//  // / <param name="bpMetric"></param>
//  // /
//  // / <param name="irtModel"></param>
//  // / <param name="irtB"></param>
//  // / <param name="irtA"></param>
//  // / <param name="irtC"></param>
//  // / <param name="bVector"></param>
//  public CsetItem (String groupID, String itemID, int position, boolean isRequired, String strand, double bpMetric, String irtModel, double irtB, double irtA, double irtC, String bVector)
//  {
//    super (itemID, groupID, position, true, false, strand, isRequired, irtB, irtA, irtC, irtModel, bVector);
//    this.bpMetric = bpMetric;
//  }

  // / <summary>
  // / Deprecated, added for temporary backward compatibility. All content
  // levels are inherited from the _baseItem
  // / </summary>
  // / <param name="CL"></param>
  public void addContentLevel (String cl)
  {
    super.addContentLevel (cl);
  }

  // / <summary>
  // / Constructor to use when Cset1 metrics are computed in place (not in the
  // database)
  // / </summary>
  // / <param name="baseItem"></param>
  public CsetItem (TestItem baseItem)
  {
    super (baseItem);
    if(baseItem.contentLevels!=null)
    {
    	contentLevelCollection = new ContentLevelCollection(baseItem.contentLevels);
    }
  }

  public CsetItem (TestItem baseItem, CsetGroup parentGroup)
  {
    super (baseItem);
    _parentGroup = parentGroup;
    if(baseItem.contentLevels!=null)
    {
    	contentLevelCollection = new ContentLevelCollection(baseItem.contentLevels);
    }
  }

  // public boolean hasContentlevel (String clID)
  // {
  // return super.hasContentlevel (clID);
  // }

  // / <summary>
  // / Compute the blueprint satisfaction metric for this itemgroup
  // / </summary>
  // / <param name="bp"></param>
  public double computeBPMetric (Blueprint bp, double panicWeight)
  {
    // FUNCTION CASES
    // Met or exceeded maximum allowed
    // Below minimum required
    // Between min and max required

    BpElement cl;
    double sum = 0.0;
    int cnt = 0;
    double tmp;
    // TODO: (AK) getContentLevels () What Contentlevels?
    // 2 pathes: a._parentGroup -> contentLevels
    // b. _baseItem -> _contentLevels
    Set<String> dumb = new HashSet<String> ();
    for (String clID : dumb)
    {
      cl = bp.elements.getElementByID (clID);
      if (cl.weight > 0)
      {
        ++cnt;
        // first check for max exceeded
        if (cl.maxRequired <= cl.numAdministered)
        {
          sum += cl.weight * (cl.maxRequired - cl.numAdministered);
        }
        // below minimum required (employ panic weight which grows as end of
        // test approaches)
        else if (cl.numAdministered < cl.minRequired)
        { // largest weight
          tmp = (double) cl.numAdministered / (double) cl.minRequired;
          sum += cl.weight * panicWeight * (2 - tmp);
        }
        // slide toward max with lesser weight
        else if (cl.numAdministered >= cl.minRequired && cl.numAdministered < cl.maxRequired)
        {
          tmp = (double) (cl.numAdministered - cl.minRequired) / (double) (cl.maxRequired - cl.minRequired);
          // FYI: this condition cannot hold if min == max
          sum += cl.weight * (1 - tmp);
        }
        /*
         * else if (cl.numAdministered < cl.minRequired) // largest weight 
         * sum
         * += cl.weight * panicWeight * (2 - cl.numAdministered /
         * cl.minRequired); 
         * // between min and max. slide toward max with lesser
         * weight 
         * else if (cl.numAdministered >= cl.minRequired &&
         * cl.numAdministered < cl.maxRequired) 
         * // FYI: this condition cannot
         * hold 
         * if min == max sum += cl.weight * (1 - (cl.numAdministered -
         * cl.minRequired) / (cl.maxRequired - cl.minRequired)); 
         * // 9/2012:
         * Guarding against divide by zero while still factoring items over max
         * (of zero?) 
         * // else sum += 0 - cl.weight * (cl.numAdministered + 1) /
         * cl.maxRequired;
         */
      }
    }
    if (cnt > 0)
      rawBpMetric = selectionMetric = sum / cnt;
    else
      rawBpMetric = -9999; // no content levels found, this irem can contribute
                           // nothing to blueprint satisfaction
    return rawBpMetric;
  }
  
  // / <summary>
  // / Normalize bp metric and set linear combination of final selection metric
  // / </summary>
  // / <param name="bpWeight"></param>
  // / <param name="minBpMetric"></param>
  // / <param name="maxBpMetric"></param>
  public void setSelectionMetric (double bpWeight, double minBpMetric, double maxBpMetric)
  {
    if (Math.abs (maxBpMetric - minBpMetric) < .001)
      bpMetric = 2.0;
    else
      bpMetric = 1.0 + (rawBpMetric - minBpMetric) / (maxBpMetric - minBpMetric);
    selectionMetric = bpWeight * bpMetric + abilityMetric;
  }

  // / <summary>
  // / Normalize the ability match. Blueprint metric still undefined
  // / </summary>
  // / <param name="abilityMatch"></param>
  // / <param name="bpWeight"></param>
  public void setAbilityMetric (double minAbility, double maxAbility)
  {
    // where maxability effectively == minability, all item abilities = 1
    // just as where bpmax == bpmin all item bpmetrics = 1
    if (Math.abs (maxAbility - minAbility) < .001)
      abilityMetric = 2.0;
    else
      abilityMetric = 1.0 + (abilityMatch - minAbility) / (maxAbility - minAbility);
  }

  // / <summary>
  // / Used to rank-order cset items within the group
  // / </summary>
  // / <param name="rhs"></param>
  // / <returns></returns>
  /*
   * (non-Javadoc)
   * 
   * @see com.air.ItemSelection.IComparable#CompareTo(java.lang.Object)
   */
  @Override
  public int compareTo (Object rhs) {
    CsetItem r = (CsetItem) rhs;
    if (r.selectionMetric > this.selectionMetric)
      return 1;
    if (r.selectionMetric < this.selectionMetric)
      return -1;
    return 0;
  }
}
