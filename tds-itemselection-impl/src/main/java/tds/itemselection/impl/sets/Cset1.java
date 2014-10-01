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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.base.ItemGroup;
import tds.itemselection.expectedability.ExpectedAbilityComputationSmarter;
import tds.itemselection.expectedability.ExpectedInfoComputation;
import tds.itemselection.impl.blueprint.Blueprint;


/**
 * @author akulakov
 * 
 */
public class Cset1
{
  private static Logger  _logger  = LoggerFactory.getLogger (Cset1.class);
  // this is a clone of the test segment's blueprint upon which all computation
  // will be done
  // AK: cycle references?
  private Blueprint                        _bp = null;

  private Map<String, ItemGroup> _itemGroups; // this one for quick
                                                        // finding of and adding
  // items to a group
  public List<CsetGroup>                   itemGroups; // this is for extending
                                                        // the CsetGroup with

  // Min/max ability are used to determine final range of ability matches computed and normalize all ability matches to range [0,1]
  public Double MinItemAbility;
  public Double MaxItemAbility;
  public Double MinRCItemAbility;
  public Double MaxRCItemAbility;

  public Blueprint getBlueprint ()
  {
    return _bp;
  }

  public Cset1 (Blueprint bp)
  {
    _bp = bp;
    this._itemGroups = new HashMap<String, ItemGroup> ();
    this.itemGroups = new ArrayList<CsetGroup> ();
    MinItemAbility = Double.MAX_VALUE;
    MaxItemAbility = Double.MIN_VALUE;
    MinRCItemAbility = null;
    MaxRCItemAbility = null;

  }

  /*
   * /// <summary> /// Add a cset1group after computation for blueprint
   * satisfaction /// </summary> /// <param name="itemgroup"></param> public
   * void Add(CSETGroup itemgroup) { _itemGroups.Add(itemgroup.groupID,
   * itemgroup); itemGroups.Add(itemgroup); foreach (CSETItem item in
   * itemgroup.GetActive) { items.Add(item); } }
   */
  // / <summary>
  // / OBSOLETE: Use Add(CSETGroup)
  // / </summary>
  // / <param name="itemgroup"></param>
  public void addItemgroup (CsetGroup itemgroup)
  {
    _itemGroups.put (itemgroup.getGroupID (), itemgroup);
    itemGroups.add (itemgroup);

  }

  // / <summary>
  // / Once every item and item group ability match is computed by Adaptive
  // Algorithm, call this method to normalize to unit range and compute linear
  // combination of ability with blueprint metrics.
  // / </summary>
  // / <param name="minGroupAbility"></param>
  // / <param name="maxGroupAbility"></param>
  // / <param name="minItemAbility"></param>
  // / <param name="maxItemAbility"></param>
	public void setSelectionMetrics(double blueprintWeight,
			double minItemAbility, double maxItemAbility) {

		double abilityWeight = (this._bp.cset1Order.equalsIgnoreCase("DISTRIBUTION")) ? 0.0
				: 1.0;
		for (CsetGroup group : this.itemGroups) {

			group.setSelectionMetric(blueprintWeight, abilityWeight,
					minItemAbility, maxItemAbility);
		}

		Collections.sort(this.itemGroups);
	}
	
    /// <summary>
    /// Once every item and item group ability match is computed by Adaptive Algorithm, call this method to normalize to unit range and compute linear combination of ability with blueprint metrics.
    /// </summary>
    /// <param name="minGroupAbility"></param>
    /// <param name="maxGroupAbility"></param>
    /// <param name="minItemAbility"></param>
    /// <param name="maxItemAbility"></param>
    public void setSelectionMetrics()
    {
        double abilityWeight = (this._bp.cset1Order.equalsIgnoreCase("DISTRIBUTION")) ? 0.0 : this._bp.abilityWeight;
        double rcAbilityWeight = (this._bp.cset1Order.equalsIgnoreCase("DISTRIBUTION")) ? 0.0 : this._bp.rcAbilityWeight;
        for (CsetGroup group : this.itemGroups)
        {
            group.setSelectionMetric2013(this._bp.bpWeight, abilityWeight, this.MinItemAbility, this.MaxItemAbility,
                rcAbilityWeight, this.MinRCItemAbility, this.MaxRCItemAbility);
        }

		Collections.sort(this.itemGroups);
    }


	public void ComputeExpectedAbility(ExpectedInfoComputation comp) {
		for (CsetGroup group : itemGroups) {
			comp.ComputeExpectedInfo(this.getBlueprint(), group);
		}
		this.MaxItemAbility = comp.MaxItemAbility;
		this.MinItemAbility = comp.MinItemAbility;
		this.MaxRCItemAbility = comp.MaxRCItemAbility;
		this.MinRCItemAbility = comp.MinRCItemAbility;

	}
}
