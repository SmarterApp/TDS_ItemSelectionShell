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

import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.TestSegment;

public class TestSegmentBuilder {

    private String segmentKey = "(SBAC_PT)SBAC-MSB-IRP-CAT-Calc-MATH-7-Summer-2015-2016";
    private int refreshMinutes = 33;
    private String parentTest = "(SBAC_PT)SBAC-MSB-Mathematics-7-Summer-2015-2016";
    private int position = 2;
    private boolean loaded = false;
    private String error = null;
    private Blueprint blueprint = new BlueprintBuilder().build();
    private ItemPool itemPool = new ItemPoolBuilder().build();

    public TestSegment build() {
        TestSegment testSegment = new TestSegment(segmentKey);
        testSegment.refreshMinutes = refreshMinutes;
        testSegment.parentTest =  parentTest;
        testSegment.position = position;
        testSegment.loaded = loaded;
        testSegment.error = error;
        testSegment.setBp(blueprint);
        testSegment.setPool(itemPool);
        return testSegment;
    }

    public TestSegmentBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public TestSegmentBuilder withRefreshMinutes(int refreshMinutes) {
        this.refreshMinutes = refreshMinutes;
        return this;
    }

    public TestSegmentBuilder withParentTest(String parentTest) {
        this.parentTest = parentTest;
        return this;
    }

    public TestSegmentBuilder withPosition(int position) {
        this.position = position;
        return this;
    }

    public TestSegmentBuilder withLoaded(boolean loaded) {
        this.loaded = loaded;
        return this;
    }

    public TestSegmentBuilder withError(String error) {
        this.error = error;
        return this;
    }

    public TestSegmentBuilder withBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
        return this;
    }

    public TestSegmentBuilder withItemPool(ItemPool itemPool) {
        this.itemPool = itemPool;
        return this;
    }
}
