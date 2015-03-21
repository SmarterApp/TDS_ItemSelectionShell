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
import java.util.List;
import java.util.Random;
import java.util.Set;

import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.CsetGroup;

public class PruningStrategySmarter extends PruningStrategy {

	public PruningStrategySmarter(Random random) {
		super(random, true);
	}
        protected boolean PruneBpElementForStrictMax(BpElement elem, boolean pruneBelowGroupMaxItems)
        {
            boolean pruned = super.PruneBpElementForStrictMax(elem, pruneBelowGroupMaxItems);
            pruned |= PruneGroupsWithTooManyRequiredItems(elem);
            return pruned;
        }

        /// <summary>
        /// Adds to the base unprune a final pass to unprune groups were previously 
        /// pruned if necessary.  If a group is unpruned, only the required items will
        /// be unpruned; optional items will remain pruned.
        /// </summary>
        /// <param name="blueprint"></param>
        protected void Unprune(Blueprint blueprint, List<BpElement> bpElements, CsetGroup currentGroup)
        {
            super.Unprune(blueprint, bpElements, currentGroup);
            // add a pass to unprune groups if necessary
            for (BpElement elem : bpElements)
            {
               UnpruneBpElement(elem, false, true);
            }
        }

        /// <summary>
        /// Unprune items and groups until we can satisfy the min test length.
        /// </summary>
        /// <param name="blueprint">true if we were able to satisfy the min test length</param>
        /// <returns></returns>
        protected  boolean UnpruneToSatisfyMinTestLength(Blueprint blueprint, List<CsetItem> items)
        {
            if (UnpruneToSatisfyMinTestLength(blueprint, items))
                return true;

            // if we haven't met min test len yet, start unpruning groups that may have been pruned
            //  due to the required item count violating a strict max.
            for (CsetItem itm : items)
            {
                if (itm.getParentGroup ().isPruned())
                    blueprint.poolcount += itm.getParentGroup ().UnpruneGroup(false); // note: unprune all items in the group at this point
                
                if (blueprint.poolcount + blueprint.numAdministered >= blueprint.minOpItems)
                    return true;
            }
            return false;
        }

        /// <summary>
        /// Prunes groups where the number of required items would exceed a strict max.
        /// All items are pruned (including required) and the group is marked as pruned.
        /// </summary>
        /// <param name="elem"></param>
        /// <returns></returns>
        private boolean PruneGroupsWithTooManyRequiredItems(BpElement elem)
        {
            boolean pruned = false;

            if (elem.isStrictMax && elem.getItems() != null)
            {
                // if we've evaluated a group, flag it so that we don't eval it again when
                //  we hit another item in the same group.
                Set<String> evaledGroups = new HashSet<String>();

                for (CSetItem item : elem.getItems().values())
                {
                    // only check active items in groups that we haven't already checked
                    if (item.isActive && !evaledGroups.contains(item.getParentGroup ().groupID))
                    {
                        evaledGroups.add(item.getParentGroup ().groupID);

                        //// skip groups with only 1 item?  Assuming not.
                        //if (item.getParentGroup ().items.Count == 1)
                        //    continue;

                        int numRqdItemsOnThisElement = item.getParentGroup ().BpCount(elem, true, false);
                        if (numRqdItemsOnThisElement > 0 // in case we already had to administer > strict max; we only want to consider groups with required items on this element
                            && numRqdItemsOnThisElement + elem.numAdministered > elem.maxRequired)
                        {
                            // mark all items in the group as pruned, effectively removing the group from consideration.
                            //  Note that required items may also be pruned in this case, since we're pruning the entire group.
                            for (TestItem groupItem : item.getParentGroup().getActive())
                            {
                                CsetItem citem = null;
                                if (item instanceof CsetItem)
                                {
                                  citem = (CsetItem) groupItem;
                                }
                    			if(citem.itemID.equalsIgnoreCase("200-30912")|| citem.itemID.equalsIgnoreCase("200-31213"))
                    			{
                    				int a = 0;
                    			}

                                citem.pruned = true;
                            }
                            // mark the group as pruned in case we need to unprune it.
                            item.getParentGroup().setPruned(true);
                            pruned = true;
                        }
                    }
                }
            }
            return pruned;
        }
    }
