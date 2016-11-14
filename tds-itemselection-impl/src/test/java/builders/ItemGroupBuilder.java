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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fairway on 11/3/16.
 */
public class ItemGroupBuilder {
    private List<TestItem> items = new ArrayList<TestItem>();
    private String groupId = "(SBAC_PT)SBAC-MSB-IRP-CAT-Calc-MATH-7-Summer-2015-2016";
    private Integer numberOfItemsRequired = 3;
    private Integer maximumNumberOfItems = 3;
    private int bankKey = 1;
    private String segmentKey = "(SBAC_PT)SBAC-MSB-IRP-CAT-Calc-MATH-7-Summer-2015-2016";
    private String segmentId = "(SBAC_PT)SBAC-MSB-IRP-CAT-Calc-MATH-7-Summer-2015-2016";
    private int segmentPosition = 2;

    public ItemGroup build() {
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setItems(items);
        itemGroup.setGroupID(groupId);
        itemGroup.setNumberOfItemsRequired(numberOfItemsRequired);
        itemGroup.setMaximumNumberOfItems(maximumNumberOfItems);
        itemGroup.setBankkey(bankKey);
        itemGroup.setSegmentKey(segmentKey);
        itemGroup.setSegmentID(segmentId);
        itemGroup.setSegmentPosition(segmentPosition);
        return itemGroup;
    }


    public ItemGroupBuilder withItems(List<TestItem> items) {
        this.items = items;
        return this;
    }

    public ItemGroupBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public ItemGroupBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public ItemGroupBuilder withSegmentId(String segmentId) {
        this.segmentId = segmentId;
        return this;
    }

    public ItemGroupBuilder withNumberOfItemsRequired(Integer numberOfItemsRequired) {
        this.numberOfItemsRequired = numberOfItemsRequired;
        return this;
    }

    public ItemGroupBuilder withMaximumNumberOfItems(Integer maximumNumberOfItems) {
        this.maximumNumberOfItems = maximumNumberOfItems;
        return this;
    }

    public ItemGroupBuilder withBankKey(int bankKey) {
        this.bankKey = bankKey;
        return this;
    }

    public ItemGroupBuilder withSegmentPosition(int segmentPosition) {
        this.segmentPosition = segmentPosition;
        return this;
    }
}
