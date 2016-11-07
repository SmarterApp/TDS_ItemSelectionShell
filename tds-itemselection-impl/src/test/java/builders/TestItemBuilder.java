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

import tds.itemselection.base.TestItem;

public class TestItemBuilder {

    private String groupId = "group";
    private String itemId = "item";

    public TestItem build() {
        TestItem testItem = new TestItem();
        testItem.setGroupID(groupId);
        testItem.setItemID(itemId);
        return testItem;
    }

    public TestItemBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public TestItemBuilder withItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }
}
