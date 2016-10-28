package builders;

import tds.itemselection.base.ItemCandidatesData;

import java.util.UUID;

/**
 * Created by fairway on 10/28/16.
 */
public class ItemCandidatesDataBuilder {

    private UUID opportunityKey = UUID.fromString("86b0ee41-01d9-4a95-bd56-0544c2d5e8cd");
    private UUID sessionKey = UUID.fromString("ad1350c7-0747-45d2-8879-7289dee2566f");
    private String algorithm = "adaptive2";
    private String segmentKey = "(SBAC_PT)SBAC-MSB-IRP-CAT-Calc-MATH-7-Summer-2015-2016";
    private String segmentID = "SBAC-MSB-IRP-CAT-Calc-MATH-7";
    private int segmentPosition = 1;
    private String groupID = "";
    private String blockID = "";
    private boolean isSimulation;

    public ItemCandidatesData build() {
        return new ItemCandidatesData(
                opportunityKey
                , algorithm
                , segmentKey
                , segmentID
                , segmentPosition
                , groupID
                , blockID
                , sessionKey
                , isSimulation
        );
    }

    public ItemCandidatesDataBuilder withOpportunityKey(UUID opportunityKey) {
        this.opportunityKey = opportunityKey;
        return this;
    }

    public ItemCandidatesDataBuilder withAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public ItemCandidatesDataBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public ItemCandidatesDataBuilder withSegmentID(String segmentID) {
        this.segmentID = segmentID;
        return this;
    }

    public ItemCandidatesDataBuilder withSegmentPosition(int segmentPosition) {
        this.segmentPosition = segmentPosition;
        return this;
    }

    private ItemCandidatesDataBuilder withGroupID(String groupID) {
        this.groupID = groupID;
        return this;
    }

    private ItemCandidatesDataBuilder withBlockID(String blockID) {
        this.blockID = blockID;
        return this;
    }

    private ItemCandidatesDataBuilder withIsSimulation(boolean isSimulation) {
        this.isSimulation = isSimulation;
        return this;
    }

}
