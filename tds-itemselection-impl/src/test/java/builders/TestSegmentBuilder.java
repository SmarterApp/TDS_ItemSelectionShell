package builders;

import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.sets.ItemPool;
import tds.itemselection.loader.TestSegment;

/**
 * Created by fairway on 10/28/16.
 */
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

    private TestSegmentBuilder withPosition(int position) {
        this.position = position;
        return this;
    }

    private TestSegmentBuilder withLoaded(boolean loaded) {
        this.loaded = loaded;
        return this;
    }

    private TestSegmentBuilder withError(String error) {
        this.error = error;
        return this;
    }

    private TestSegmentBuilder withBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
        return this;
    }

    private TestSegmentBuilder withItemPool(ItemPool itemPool) {
        this.itemPool = itemPool;
        return this;
    }
}
