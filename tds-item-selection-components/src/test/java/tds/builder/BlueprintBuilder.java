/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.builder;

import java.util.ArrayList;
import java.util.List;

import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.BpElements;
import tds.itemselection.impl.blueprint.Strand;
import tds.itemselection.impl.sets.CSetItem;

public class BlueprintBuilder {
  private BpElements elements = new BpElements();
  private List<BpElement> strictMaxes = new ArrayList<BpElement>();
  private List<Strand> strands = new ArrayList<Strand>();
  private String segmentKey; // _Key field to tblSetofAdminSubjects : ITEMBANK
  private String segmentID;  // TestID field : tblSetofAdminSubjects : ITEMBANK
  private Integer segmentPosition;
  private double abilityWeight = 1.0; // explicit ability weight; used : new
  private double itemWeight; // weight of blueprint satisfaction relative to
  private int randomizerIndex; // use this to randomly select next itemgroup
  private int randomizerInitialIndex; // use this to randomly select first
  private double abilityOffset = Blueprint.ABILITY_OFFSET; // a spurious value to offset
  // the ability estimate by
  // number of top-ranked itemgroups by blueprint satisfaction to send on to
  // ability match stage

  // secondary ordering for blueprint satisfaction
  private String cset1Order = Blueprint.CSET1_ORDER;
  private double startAbility;
  private double slope = Blueprint.SLOPE;
  private double intercept = Blueprint.INTERCEPT;
  private double startInfo = Blueprint. START_INFO;
  private String adaptiveVersion = Blueprint.ADAPTIVE_VERSION;
  // adaptiveVersion required to discriminate between different methods of
  // computing blueprint metric (for now)
  // to extend adaptiveVersion, expect the database to pass comma-delimited
  // string of versioning aspects

  // when releasing used or pruned back into the pool for a content level,
  // release all for the content level or only enough to satisfy min?
  // TBD: Make this a configurable part of the blueprint
  private boolean releaseAll = true; // on a given BpElement for preemptively
  // pruned items when the pool has too
  // few items for a min
  private boolean recycleAll = true; // on a given BpElement for recycling
  // items when the pool has too few items
  // for a min

  // examinee specific. not to be used for
  // number of operational items administered so far
  private int numAdministered = 0;
  // the number of operational items administered for the entire test (across segments)
  private int numAdministeredTest = 0;
  // an adaptive algorithm parameter used in computing ability metric for
  // overall
  private double info;
  // examinee ability estimate in logit scale at overall test level
  private double theta;
  // keep track of the last item position for which ability computations were
  // updated
  private int lastAbilityPosition = 0;
  private int poolcount = 0;
  private List<CSetItem> _items = new ArrayList<CSetItem>();

  public Blueprint build() {
    Blueprint bp = new Blueprint();
    bp.segmentPosition = segmentPosition;
    bp.segmentKey = segmentKey;
    bp.segmentID = segmentID;
    return bp;
  }

  public BlueprintBuilder withSegmentPosition(final int segmentPosition) {
    this.segmentPosition = segmentPosition;
    return this;
  }

  public BlueprintBuilder withSegmentKey(final String segmentKey) {
    this.segmentKey = segmentKey;
    return this;
  }

  public BlueprintBuilder withSegmentId(final String segmentId) {
    this.segmentID = segmentId;
    return this;
  }
}
