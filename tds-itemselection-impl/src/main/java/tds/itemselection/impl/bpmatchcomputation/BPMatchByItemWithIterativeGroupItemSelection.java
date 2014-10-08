/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.bpmatchcomputation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tds.itemselection.base.TestItem;
import tds.itemselection.expectedability.ExpectedAbilityComputationSmarter;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.ContentLevelCollection;
import tds.itemselection.impl.item.PruningStrategy;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.CSetItemByAbilityMatchDescComparer;
import tds.itemselection.impl.sets.CSetItemByRequiredSelectionMetricDescComparer;
import tds.itemselection.impl.sets.Cset1Factory2013;
import tds.itemselection.impl.sets.CsetGroup;

public class BPMatchByItemWithIterativeGroupItemSelection extends BlueprintMatchComputation{

    // will store the initial pruned state of all items in the blueprint
    //  and will use this to reset the BP to its original state after each group.
    private Map<String, Boolean> initialItemPrunedState = new HashMap<String, Boolean>();

	public BPMatchByItemWithIterativeGroupItemSelection(Random rand) {
		super(rand);
	}

    private void InitializePrunedState(Cset1Factory2013 cset1Factory)
    {
        initialItemPrunedState = new HashMap<String, Boolean>();
        for (CSetItem item : cset1Factory.getBp().getItems())
            initialItemPrunedState.put(item.itemID, item.pruned);
    }

	@Override
	protected void CalculateBpMatchForGroup(Cset1Factory2013 csetFactory,
			CsetGroup group) {
		// populate content levels
		group.populateContentLevels();
		// record the pruned state of all items the first time through.
		// The bp will be rolled back to this initial state after each group
		// is evaluated.
		if (initialItemPrunedState == null)
			InitializePrunedState(csetFactory);

		// using base pruning strategy to prune after each simulated item
		// selection
		// new strategy is too resource-intensive to use in this simulation.
		PruningStrategy pruningStrategy = new PruningStrategy(rnd, true);
		int maxitms = group.getMaxItems();
		int selectedCount = 0;
		double sum = 0;

		while (selectedCount < maxitms) {
			CSetItem maxItem = null;
			List<CSetItem> tmpItems = new ArrayList<CSetItem>();
			pruningStrategy.PruneStrictMaxes(csetFactory.getBp(), true, group); 
			// filter items over strict maxes with each item "selection"
			for (TestItem itm : group.getItems()) {
				if (itm instanceof CSetItem) {
					CSetItem item = (CSetItem) itm;
					if (item.isActive) {
						item.computeBPMetric(csetFactory.getBp());
						tmpItems.add(item);

					}
				}
			}
			// no more active items; stop
			if (tmpItems.size() == 0)
				break;

			// select the item with the best bp-match metric
			if (tmpItems.size() == 1) {
				maxItem = tmpItems.get(0); // only 1 item; that's the max
			} else {
				// sort first by required-first, then selection metric (which
				// was set in ComputeBpMetric)
				CSetItemByRequiredSelectionMetricDescComparer selectionComparer = new CSetItemByRequiredSelectionMetricDescComparer();
				Collections.sort(tmpItems, selectionComparer);

				// how many items have max value; we'll also factor in whether
				// the items are required.
				// If the top 2 items have the same metric value but only 1 is
				// required, that's the one we'll choose.
				int maxValueCount = getMaxValueCount(tmpItems,
						selectionComparer);
				if (maxValueCount == 1)
					maxItem = tmpItems.get(0);
				else {
					// If ability is weighted at 0, just randomly select an item
					if (csetFactory.getBp().abilityWeight == 0)
						maxItem = tmpItems.get(rnd.nextInt(maxValueCount));
					else {
						// ability weight != 0; calculate expected info for all
						// tied items then pick highest info if there's not
						// another tie.
						// otherwise, pick randomly. Note that item-level
						// ability match values will be saved and reused, but
						// we'll need to calculate the eventual group value,
						// because we don't yet know what the group will look
						// like.
						ExpectedAbilityComputationSmarter abilityCalc = new ExpectedAbilityComputationSmarter();
						List<CSetItem> ties = new ArrayList<CSetItem>();
						for (int i = 0; i < maxValueCount; i++) {
							ties.add(tmpItems.get(i));
							abilityCalc.ComputeExpectedInfo(
									csetFactory.getBp(), tmpItems.get(i));
						}

						// comparer will compare overall ability first, then RC.
						CSetItemByAbilityMatchDescComparer abilityComparer = new CSetItemByAbilityMatchDescComparer();
						Collections.sort(ties, abilityComparer);

						// if we still got ties, pick randomly among remaining
						// ties. Otherwise pick the max
						maxValueCount = getMaxValueCount(ties, abilityComparer);

						if (maxValueCount == 1)
							maxItem = ties.get(0);
						else
							maxItem = ties.get(rnd.nextInt(maxValueCount));
					}
				}
			}
			// mark item as used so that it doesn't get pruned and will not be
			// re-selected
			maxItem.ItemUsed = true;
			sum += maxItem.rawBpMetric;
			// update item counts in bp elements as if this item were administered
			csetFactory.getBp().UpdateSatisfaction(maxItem);
			selectedCount++;
		}
		// only use group if the number of items satisfies the min required for
		// the group
		if (selectedCount > 0 && selectedCount >= group.getNumRequired()) {
			group.metricComputed = true;
			// AM: now that groups can be pruned below their max, 
			// we're dividing by the intended size of the group, 
			// as opposed to the actual size, so as not to skew in
			// favor of smaller passages.
			group.rawBpMetric = group.selectionMetric = sum
					/ group.getIntendedSize(); // sum / selectedCount;
		} else {
			// no content levels found, this group can contribute nothing to
			// blueprint satisfaction
			group.metricComputed = false;
			group.selectionMetric = ContentLevelCollection.NO_CLs;
		}

		for (TestItem itm : group.items) {
			if (itm instanceof CSetItem) {
				CSetItem item = (CSetItem) itm;
				// include the items we selected here and exclude the ones we
				// didn't; unless we're not going to use this group, then just
				// reset.
				item.Included = !group.metricComputed || item.ItemUsed;
				item.ItemUsed = false; // reset ItemUsed since we're finished
										// pretending that the item has been
										// previously selected.
			}
		}

		// set the bp tie break here before we roll back the BP, which could
		// potentially
		// prune one of our included items. We want all included items to be
		// active so that
		// the can factor into the ability diff calculation, if that's used.
//		Will be call late		
//		SetBpTieBreakForGroup(csetFactory, group);

		// now rollback the bp for the changes we may have made to the bp counts
		// and the pruning
		RollbackBlueprint(csetFactory.getBp(), group);

	}

    /// <summary>
    /// Will roll back the bp to its original state after each group is evaluated.  The BP should not
    /// actually be updated until the group is administered.
    /// </summary>
    /// <param name="group"></param>
    private void RollbackBlueprint(Blueprint bp, CsetGroup group)
    {
        // reset the bp counts for items that were selected for this group.
        for (TestItem item : group.getActive())
            bp.UpdateSatisfaction(item, true);

        // reset the pruned values to their original state
        for (CSetItem item : bp.getItems())
        {
        	if(initialItemPrunedState.get(item.itemID) != null)
        	{
	            boolean initialItemPruned = initialItemPrunedState.get(item.itemID);
	            if (initialItemPruned != item.isPruned())
	                item.setPruned(initialItemPruned);
        	}
        }
    }

    /// <summary>
    /// Gets the count of items that have the max value according to the comparer.
    /// Just a helper method to eliminate a few lines of duplicate code above.
    /// </summary>
    /// <param name="items">A sorted list if CSetItems descending according to the comparer</param>
    /// <param name="comparer"></param>
    /// <returns></returns>
    private int getMaxValueCount(List<CSetItem> items, Comparator<CSetItem> comparator)
    {
        int maxValueCount = 1;
        for (; maxValueCount < items.size(); maxValueCount++)
            if (comparator.compare(items.get(maxValueCount), items.get(maxValueCount - 1)) != 0)
                break; // first unequal pair in the list has been found
        return maxValueCount;
    }

    // 
    public void execute(Cset1Factory2013 csetFactory, CsetGroup group)
    {
        // if this group has no more than 1 item, we can just run bp2.
        //  Same if all items in the group are marked as required.
        if (group.getActiveCount() <= 1 || group.isAllItemsRequired())
        {
            new BP2(rnd).execute(csetFactory, group);
        }
        else
        { 
           super.execute(csetFactory, group);
        }
    }


}
