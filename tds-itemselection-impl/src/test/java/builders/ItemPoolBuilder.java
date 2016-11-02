/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/

package builders;

import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.sets.ItemPool;

import java.util.ArrayList;
import java.util.List;

public class ItemPoolBuilder {

    public List<TestItem> items = new ArrayList<>();

    public List<ItemGroup> itemGroups = new ArrayList<>();

    public List<TestItem> siblingItems = new ArrayList<>();

    public ItemPool build() {
        ItemPool itemPool = new ItemPool();
        for(int i = 0; i < items.size(); i++) {
            itemPool.addItem(items.get(i));
        }
        for(int i = 0; i < itemGroups.size(); i++) {
            itemPool.addItemgroup(itemGroups.get(i));
        }
        for(int i = 0; i < siblingItems.size(); i++) {
            itemPool.addSiblingItem(siblingItems.get(i));
        }
        return itemPool;
    }

    public ItemPoolBuilder withItems(List<TestItem> items) {
        this.items = items;
        return this;
    }

    public ItemPoolBuilder withItemGroups(List<ItemGroup> itemGroups) {
        this.itemGroups = itemGroups;
        return this;
    }

    public ItemPoolBuilder withSiblingItems(List<TestItem> siblingItems) {
        this.siblingItems = siblingItems;
        return this;
    }
}
