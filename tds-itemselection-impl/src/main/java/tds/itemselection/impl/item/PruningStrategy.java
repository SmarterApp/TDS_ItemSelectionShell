/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.item;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import tds.itemselection.base.TestItem;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.CsetGroup;

public class PruningStrategy {
    
	protected IUnpruneOrderStrategy UnpruneOrderStrategy;

    // in case we attempt to use random number generation with an unprune strategy of no-order,
    //  we'll create a rnd.
    private Random rnd;
    protected Random getRand()
    {
            if (rnd == null)
                rnd = new Random();
            return rnd;
     }
	private void setRandom( Random value)
	{
		rnd = value;
	}

    /// <summary>
    /// Whether or not to use the blueprint's releaseAll flag when unpruning
    /// non-strand bp elements.  Passing false will not release all.
    /// </summary>
    protected boolean UseBpReleaseAllNonStrand;

    public PruningStrategy() 
    {
    	rnd = null;
    	UseBpReleaseAllNonStrand = false;
    }

    /// <summary>
    /// </summary>
    /// <param name="rand">If not null, will evaluate both bp elements and items randomly when unpruning using this rng.</param>
    public PruningStrategy(Random rand, boolean  useBpReleaseAllNonStrand)
    {
        this.UseBpReleaseAllNonStrand = useBpReleaseAllNonStrand;
        this.rnd = rand;
        this.UnpruneOrderStrategy =
            rand != null ? (IUnpruneOrderStrategy)new RandomUnpruneOrder(rand)
            : new NoUnpruneOrder();
    }

    /// <summary>
    /// Pruned items form the pool that are at or above strict maxes, then will
    /// unprune as necesary to meet mins, and to meet test length.
    /// </summary>
    /// <param name="blueprint"></param>
    /// <param name="pruneBelowGroupMaxItems">If false, will not prune a group below its maxItems.</param>
    public void PruneStrictMaxes(Blueprint blueprint, boolean pruneBelowGroupMaxItems)
    {
        PruneStrictMaxes(blueprint, pruneBelowGroupMaxItems, null);
    }

    /// <summary>
    /// Pruned items form the pool that are at or above strict maxes, then will
    /// unprune as necesary to meet mins, and to meet test length.  If currentGroup
    /// is not null, will attempt to unprune that first.
    /// </summary>
    /// <param name="blueprint"></param>
    /// <param name="pruneBelowGroupMaxItems">If false, will not prune a group below its maxItems.</param>
    /// <param name="currentGroup">If not null, will unprune this group first.  Used in iterative bp-match routine.</param>
    public void PruneStrictMaxes(Blueprint blueprint, boolean pruneBelowGroupMaxItems, CsetGroup currentGroup)
    {
        boolean pruned = false;

        for (BpElement elem : blueprint.strictMaxes)
        {
            pruned |= PruneBpElementForStrictMax(elem, pruneBelowGroupMaxItems);
        }

        if (!pruned) return;

        // Pruning may have caused some elements to be unable to satisfy minitem requirements
        // So give them the chance to unprune just enough
        Unprune(blueprint, UnpruneOrderStrategy.OrderCollection( blueprint.elements.getValues()), currentGroup);

        // set the poolcount after pruning
        blueprint.poolcount = 0;
        for (CSetItem itm : blueprint.getItems())
        {
            if (itm.isActive)
                ++blueprint.poolcount;
        }
        // check for adequate items to complete test
        if (blueprint.poolcount + blueprint.numAdministered >= blueprint.minOpItems)
            return;

        // still not enough items to meet the min test length
        UnpruneToSatisfyMinTestLength(blueprint, UnpruneOrderStrategy.OrderCollection(blueprint.getItems()));
    }

    protected boolean PruneBpElementForStrictMax(BpElement elem, boolean pruneBelowGroupMaxItems)
    {
        // IMPORTANT: These items are shared with other BpElements, so they, too, will be pruned by these items
        // This can cause some content levels to fall short of their ability to meet minitem requirements
        // So they will need to 'Unprune' (below)
        if (elem.isStrictMax && elem.numAdministered >= elem.maxRequired && elem.getItems() != null)
        {
            boolean pruned = false;
            for (CSetItem item : elem.getItems().values())
            {
                CsetGroup grp = item.getParentGroup();
                if (item.isActive && !item.isRequired && 
                    (grp.getMaxItems() == -1 || 
                        (pruneBelowGroupMaxItems || grp.getActiveCount() > grp.getMaxItems())))
                {        // pruning can only apply to items currently in the pool
                    item.pruned = true;
                    pruned = true;
                }
            }
            return pruned;
        }
        return false;
    }

    protected void Unprune(Blueprint blueprint, Collection<BpElement> collection, CsetGroup currentGroup)
    {
        // Pruning may have caused some elements to be unable to satisfy minitem requirements
        // So give them the chance to unprune just enough
        
        // if a group was passed in, attempt to unprune that first.
        if (currentGroup != null)
            UnpruneGroup(blueprint, currentGroup);

        for (BpElement elem : collection)
        {
            // by starting at lower levels, we may be more selective
            if (elem.bpElementType != BpElement.BpElementType.Strand)
                UnpruneBpElement(elem, UseBpReleaseAllNonStrand ? blueprint.releaseAll : false, false);
        }

        for (BpElement elem : collection)
        {
            if (elem.bpElementType == BpElement.BpElementType.Strand)
                UnpruneBpElement(elem, blueprint.releaseAll, false);
        }
    }

    /// <summary>
    /// Will unprune items from currentGroup in order to meet mins
    /// on the bp elements associated with the group's pruned items.
    /// </summary>
    /// <param name="bp"></param>
    /// <param name="currentGroup"></param>
    private void UnpruneGroup(Blueprint bp, CsetGroup currentGroup)
    {
//        // Unprune the most valuable items first.
//        //  Note that for items pruned in the initial pruning step prior to computing bp match
//        //  values, there will no no bp metric.  And for items pruned in the iterative
//        //  bp-match routine, the bp metric will reflect the state of the bp at the time that the
//        //  item was pruned.  So to have accurate bp match values for pruned items according to the
//        //  current state of the bp, we'll need to recalculate it.
//        //  Also setting the bp jitter to break ties randomly.
//        for (TestItem item : currentGroup.getItems())
//        {
//            item.ComputeBPMetric(bp);
//            item.BpJitter = rnd.nextDouble();
//        }
//        currentGroup.items.sort();
//
//        // sorts highest value to lowest; unprune from the top
//        for(CSetItem item : currentGroup.items)
//        {
//            if (item.pruned)
//            {
//                // see if unpruning this item would help us to meet a min on 
//                //  one or more bp elements
//                for (string cl : item.contentLevels)
//                {
//                    BpElement elem = bp.elements.Get(cl);
//                    if (elem._items == null || elem.minRequired < 1)
//                        continue;
//                    int pcnt = elem.Poolcount;
//                    int itemcnt = elem._items.Count;
//                    
//                    if (pcnt != itemcnt && pcnt + elem.numAdministered < elem.minRequired)
//                    {
//                        // unpruning this item will help us to achieve a min.  Do it and move
//                        //  on to the next item in the group
//                        item.pruned = false;
//                        break;
//                    }
//                }
//            }
//        }
    }

    protected void UnpruneBpElement(BpElement elem, boolean releaseAll, boolean unpruneGroup)
    {
        // Use the blueprint elements to decide which items to leave pruned
        if (elem.getItems() == null || elem.minRequired < 1) return;     // no items this element
        int pcnt = elem.getPoolCount ();
        int itemcnt = elem.getItems().size();
        if (pcnt + elem.numAdministered >= elem.minRequired || pcnt == itemcnt)
            return;

        // no reason to reorder if we'll be releasing all
        Collection<CSetItem> items =  (releaseAll ? elem.getItems().values() : UnpruneOrderStrategy.OrderCollection((List<CSetItem>) elem.getItems().values()));
        for (CSetItem item : items)
        {
            if (item.pruned)
            {
                if (item.getParentGroup().isPruned()) // only possible in the new alg
                {
                    // if the group is pruned, we'll either unprune all required items in the group
                    //  or do nothing, depending on the value of unpruneGroup.  We won't unprune
                    //  individual items on a pruned group w/o unpruning the group.
                    if (unpruneGroup)
                        pcnt += item.getParentGroup().UnpruneGroup(true);
                }
                else
                {
                    item.pruned = false;
                    ++pcnt;
                }
                if (!releaseAll && (pcnt + elem.numAdministered >= elem.minRequired))
                    return;
            }
        }
    }

    /// <summary>
    /// Unprune items until we satisfy the min test length
    /// </summary>
    /// <param name="blueprint"></param>
    /// <returns>true if we were able to satisfy the min test length</returns>
    protected boolean UnpruneToSatisfyMinTestLength(Blueprint blueprint, Collection<CSetItem> collection)
    {
        // unprune items until there are enough items to complete test or all items are returned to pool
        // This does not honor the 'releaseAll' flag.
        for (CSetItem itm : collection)
        {
            if (itm.pruned && !itm.getParentGroup().isPruned())
            {
                itm.pruned = false;
                ++blueprint.poolcount;
            }
            if (blueprint.poolcount + blueprint.numAdministered >= blueprint.minOpItems)
                return true;
        }
        return false;
    }

}
