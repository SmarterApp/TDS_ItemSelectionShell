/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import TDS.Shared.Exceptions.ReturnStatusException;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import tds.itemselection.api.IBpInfoContainer;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.TestItem;
import tds.itemselection.debug.FilePrint;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.item.CsetItem;
import tds.itemselection.impl.math.AAMath;
import tds.itemselection.impl.math.CDF;
import tds.itemselection.impl.sets.CSetItem;
import tds.itemselection.impl.sets.PriorAdmins;
import tds.itemselection.impl.sets.UsedSequence;

/**
 * @author akulakov
 * 
 */

public class Blueprint implements IBpInfoContainer {
	private static Logger _logger = LoggerFactory.getLogger(Blueprint.class);

	public static final double ABILITY_WEIGHT = 1.0;
	public static final double ABILITY_OFFSET = 0.0;
	public static final int 	CSET1_SIZE = 20;
	public static final double SLOPE = 1.0;
	public static final double INTERCEPT = 0.0;
	public static final double START_INFO = 0.2;
	public static final String CSET1_ORDER = "ABILITY";
	public static final String ADAPTIVE_VERSION = "bp1";

	public BpElements elements = new BpElements();
	// every element which has a strict maximum of items to administer
	public List<BpElement> strictMaxes = new ArrayList<BpElement>(); 
	// Following List: the elements that are used in computing ability match
	public List<Strand> strands = new ArrayList<Strand>();
	// public List<ReportingCategory> reportingCategories = new
	// ArrayList<ReportingCategory>(); // the elements that are to be used :
	// computing ability match

	// general (not examinee-specific)
	public String segmentKey; // _Key field to tblSetofAdminSubjects : ITEMBANK
	public String segmentID;  // TestID field : tblSetofAdminSubjects : ITEMBANK
	public Integer segmentPosition;

	public double abilityWeight = 1.0; // explicit ability weight; used : new
										// adaptive alg
	public double itemWeight; // weight of blueprint satisfaction relative to
								// ability when pruning items from final
								// itemgroup selected
	public int randomizerIndex; // use this to randomly select next itemgroup
								// from final cset2
	public int randomizerInitialIndex; // use this to randomly select first
										// itemgroup from cset2
	public double abilityOffset = ABILITY_OFFSET; // a spurious value to offset
													// the ability estimate by
	// number of top-ranked itemgroups by blueprint satisfaction to send on to
	// ability match stage

	// secondary ordering for blueprint satisfaction
	public String cset1Order = CSET1_ORDER;
	public double startAbility;
	public double slope = SLOPE;
	public double intercept = INTERCEPT;
	protected double startInfo = START_INFO;
	public String adaptiveVersion = ADAPTIVE_VERSION;
	// adaptiveVersion required to discriminate between different methods of
	// computing blueprint metric (for now)
	// to extend adaptiveVersion, expect the database to pass comma-delimited
	// string of versioning aspects

	// when releasing used or pruned back into the pool for a content level,
	// release all for the content level or only enough to satisfy min?
	// TBD: Make this a configurable part of the blueprint
	public boolean releaseAll = true; // on a given BpElement for preemptively
										// pruned items when the pool has too
										// few items for a min
	public boolean recycleAll = true; // on a given BpElement for recycling
										// items when the pool has too few items
										// for a min

	// examinee specific. not to be used for
	// number of operational items administered so far
	public int numAdministered = 0;
	// the number of operational items administered for the entire test (across segments)
    public int numAdministeredTest = 0; 
	// an adaptive algorithm parameter used in computing ability metric for
	// overall
	public double info;
	// examinee ability estimate in logit scale at overall test level
	public double theta;
	// keep track of the last item position for which ability computations were
	// updated
	public int lastAbilityPosition = 0;
	public int poolcount = 0;
	protected List<CSetItem> _items = new ArrayList<CSetItem>();

	/**
       m_it = T / (T - t), where T = the total test length.  This has the effect of 
       increasing the algorithm’s preference for items that have not yet met their minimums 
       as the end of the test nears and the opportunities to meet the minimum diminish
       Moved over from private field in Cset1Factory that was passed around.  Needed the value
       to reflect items selected in the new iterative bp match routine. 
       /// PvW, Xmas eve 2014: From Jon: use max op items instead of min op items for panic weight.
	 */
	public Double getPanicWeight() {
		if (numAdministered >= maxOpItems)
			return (new Double(maxOpItems));
		else
			return maxOpItems / (double) (maxOpItems - numAdministered);
	}

	public List<CSetItem> getItems() {
		return _items;
	}

	public void setItems(List<CSetItem> items) {
		this._items = items;
	}

	// As in .NET code!
	public boolean getPrecisionTargetMet() {
		return standardError <= precisionTarget;
	}

	/**
	 *  Copy the generic blueprint for examinee computation of satisfaction
	 *
	 */
	public Blueprint copy() {
		return copy(false);
	}
	
	/**
	 * Allows for copying of bp-match and ability/info 
	 * statistics.  Does not copy items.
	 * 
	 * @param preserveStatistics
	 * @return
	 */
	public Blueprint copy(Boolean preserveStatistics) {
		Blueprint bp = new Blueprint();
		BpElement newelem;
		Strand strnd;
		bp.Initialize(segmentKey, segmentID, segmentPosition, minOpItems, maxOpItems,
				bpWeight, itemWeight, abilityOffset, randomizerIndex,
				randomizerInitialIndex, info, startAbility, cSet1Size,
				cset1Order, slope, intercept, adaptiveVersion,			
				abilityWeight, 
				rcAbilityWeight, 
		        precisionTarget, 
		        precisionTargetMetWeight, 
		        precisionTargetNotMetWeight, 
		        adaptiveCut, 
		        tooCloseSEs, 
		        terminateBasedOnOverallInformation, 
		        terminateBasedOnReportingCategoryInformation, 
		        terminateBasedOnCount, 
		        terminateBasedOnScoreTooClose, 
		        terminateBaseOnFlagsAnd, 						
				offGradeItemsProps,
				offGradePoolFilter,
		        minOpItemsTest,
		        maxOpItemsTest
				);
		// ALL elements go into the master collection		
		for (BpElement elem : elements.getValues()) { 
			newelem = elem.copy();
			bp.elements.addBpElement(newelem);
			if (newelem.isStrictMax) // 'index' the strict max elements
				bp.strictMaxes.add(newelem);
		}
		bp.releaseAll = this.releaseAll;
        if (preserveStatistics)
        {
            bp.numAdministered 		= this.numAdministered;
            bp.numAdministeredTest 	= this.numAdministeredTest;
            bp.recycleAll 			= this.recycleAll;
            bp.lastAbilityPosition 	= this.lastAbilityPosition;
            bp.poolcount 			= this.poolcount;
            bp.theta 				= this.theta;
            bp.info 				= this.info;
            bp.standardError 		= this.standardError;
            bp.actualInfoCalc 		= this.actualInfoCalc;
            bp.offGradePoolFilter 	= this.offGradePoolFilter;
        }

		// We MUST preserve the original order of the strands
        for (ReportingCategory thisRc : _reportingCategories.values())   // 'index' the strands
        {
    		ReportingCategory rc = (ReportingCategory)bp.elements.getElementByID(thisRc.ID);
            bp._reportingCategories.put(thisRc.ID, rc);
        }
        for (BpElement bpElem : _bluePrintElements.values())   // 'index' the strands
        {
        	BpElement elem = bp.elements.getElementByID(bpElem.ID);
            bp._bluePrintElements.put(bpElem.ID, elem);
        }
        // The code is not needed for AA2
		for (Strand s : strands) // 'index' the strands
		{
			strnd = (Strand) bp.elements.getElementByID(s.ID);
			bp.strands.add(strnd.vectorIndex - 1, strnd);
		}

		return bp;
	}

	/** <summary>
	 * NEW Constructor to use for building generalized test blueprint. Use
	// Copy() to create a CSET1 blueprint
	 * 3/2013: Added adaptiveVersion parameter
	 * </summary>
	 * <param name="segmentkey"></param>
	 * <param name="segmentPosition"></param>
	 * <param name="minOpItems"></param>
	 * <param name="maxOpItems"></param>
	 * <param name="bpWeight"></param>
	 * <param name="itemWeight"></param>
	 * <param name="abilityOffset"></param>
	 * <param name="randomizer"></param>
	 * <param name="initialRandom"></param>
	 * <param name="startInfo"></param>
	 * <param name="startAbility"></param>
	 * <param name="cset1Size"></param>
	 * <param name="cset1Order"></param>
	 * <param name="slope"></param>
	 * <param name="intercept"></param>
	 * <param name="adaptiveVersion"></param>
	 * OffGradeItemsProps offGradeItemsProps,
	 * String offGradePoolFilter,
	 * int minOpItemsTest,
	 * int maxOpItemsTest
	 */ 
	public void Initialize(String segmentkey, String segmentID,
			int segmentPosition,
			int minOpItems, int maxOpItems, double bpWeight, double itemWeight,
			double abilityOffset, int randomizer, int initialRandom,
			double startInfo, double startAbility, int cset1Size,
			String cset1Order, double slope, double intercept,
			String adaptiveVersion,
			OffGradeItemsProps offGradeItemsProps,
			String offGradePoolFilter,
			int minOpItemsTest, int maxOpItemsTest
			) {
		this.segmentKey 			= segmentkey;
		this.segmentID				= segmentID;
		this.segmentPosition 		= segmentPosition;
		this.minOpItems 			= minOpItems;
		this.maxOpItems 			= maxOpItems;
		this.bpWeight 				= bpWeight;
		this.itemWeight 			= itemWeight;
		this.abilityOffset 			= abilityOffset;
		this.randomizerIndex 		= randomizer;
		this.randomizerInitialIndex = initialRandom;
		this.startInfo 				= startInfo;
		this.info 					= startInfo;
		this.startAbility 			= startAbility;
		this.theta 					= startAbility;
		this.cset1Order 			= cset1Order;
		this.cSet1Size 				= cset1Size;
		this.slope 					= slope;
		this.intercept 				= intercept;
		this.adaptiveVersion 		= adaptiveVersion;
		this.offGradeItemsProps 	= offGradeItemsProps;
		this.offGradePoolFilter 	= offGradePoolFilter;
        this.minOpItemsTest 		= minOpItemsTest;
        this.maxOpItemsTest 		= maxOpItemsTest;
	}

	/*
	        /// <summary>
        /// Constructor to use for building generalized test blueprint. Use Copy() to create a CSET1 blueprint
        /// 3/2013: Added adaptiveVersion parameter
        /// </summary>
        /// <param name="segmentkey"></param>
        /// <param name="segmentPosition"></param>
        /// <param name="minOpItems"></param>
        /// <param name="maxOpItems"></param>
        /// <param name="bpWeight"></param>
        /// <param name="itemWeight"></param>
        /// <param name="abilityOffset"></param>
        /// <param name="randomizer"></param>
        /// <param name="initialRandom"></param>
        /// <param name="startInfo"></param>
        /// <param name="startAbility"></param>
        /// <param name="cset1Size"></param>
        /// <param name="cset1Order"></param>
        /// <param name="slope"></param>
        /// <param name="intercept"></param>
        /// <param name="adaptiveVersion"></param>
        /// <param name="abilityWeight"></param>
        /// <param name="rcAbilityWeight"></param>
        /// <param name="precisionTarget"></param>
        /// <param name="precisionTargetMetWeight"></param>
        /// <param name="precisionTargetNotMetWeight"></param>
        /// <param name="adaptiveCut"></param>
        /// <param name="tooCloseSEs"></param>
        /// <param name="terminationOverallInfo"></param>
        /// <param name="terminationRCInfo"></param>
        /// <param name="terminationMinCount"></param>
        /// <param name="terminationTooClose"></param>
        /// <param name="terminationFlagsAnd"></param>
	 */
	public void Initialize(String segmentkey, 
			String segmentID,
			int segmentPosition,
			int minOpItems, 
			int maxOpItems, 
			double bpWeight, 
			double itemWeight,
			double abilityOffset, 
			int randomizer, 
			int initialRandom,
			double info, 
			double startAbility, 
			int cset1Size,
			String cset1Order, 
			double slope, 
			double intercept,
			String adaptiveVersion,
			double abilityWeight, 
	        double rcAbilityWeight, 
	        double precisionTarget, 
	        double precisionTargetMetWeight, 
	        double precisionTargetNotMetWeight, 
	        double adaptiveCut, 
	        double tooCloseSEs, 
	        Boolean terminationOverallInfo, 
	        Boolean terminationRCInfo, 
	        Boolean terminationMinCount, 
	        Boolean terminationTooClose, 
	        Boolean terminationFlagsAnd, 
	        OffGradeItemsProps offGradeItemsProps,
			String offGradePoolFilter,
			int minOpItemsTest, 
			int maxOpItemsTest
			) {
		this.Initialize(segmentkey, segmentID, segmentPosition, minOpItems, maxOpItems,
				bpWeight, itemWeight, abilityOffset, randomizer,
				initialRandom, info, startAbility, cset1Size,
				cset1Order, slope, intercept, adaptiveVersion,
				offGradeItemsProps,
				offGradePoolFilter,
		        minOpItemsTest,
		        maxOpItemsTest
				);
		this.abilityWeight 			= abilityWeight; 
		this.rcAbilityWeight 		= rcAbilityWeight; 
        this.precisionTarget 		= precisionTarget; 
        this.precisionTargetMetWeight = precisionTargetMetWeight; 
        this.precisionTargetNotMetWeight = precisionTargetNotMetWeight; 
        this.adaptiveCut 			= adaptiveCut; 
        this.tooCloseSEs 			= tooCloseSEs; 
        this.terminateBasedOnOverallInformation = terminationOverallInfo; 
        this.terminateBasedOnReportingCategoryInformation = terminationRCInfo; 
        this.terminateBasedOnCount 	= terminationMinCount; 
        this.terminateBasedOnScoreTooClose = terminationTooClose; 
        this.terminateBaseOnFlagsAnd = terminationFlagsAnd; 		
	}
	/*
	 * Add a CSET Item to all of its blueprint contentlevel elements
	 * </summary>
	 * <param name="item"></param>
	 */
	public void addCsItem(CSetItem cSetItem) {
		BpElement elem;
		if (_items == null)
			_items = new ArrayList<CSetItem>();
		_items.add(cSetItem);
		for (String cl : cSetItem.getContentLevels()) {
			if ((elem = elements.getElementByID(cl)) != null) {
				elem.addItem(cSetItem);
			}
		}
	}

	// / <summary>
	// / For each strict max element where numadministered >= maxitems, prune
	// all
	// items
	// / </summary>
	public void pruneStrictMaxes() {
		boolean pruned = false;
		for (BpElement elem : strictMaxes) {
			pruned |= elem.prune();
		}
		if (!pruned)
			return;

		// Pruning may have caused some elements to be unable to satisfy minitem
		// requirements
		// So give them the chance to unprune just enough
		for (BpElement elem : elements.getValues()) {
			if (!elem.isStrand) // by starting at lower levels, we may be more
								// selective
				elem.unprune(false);
		}
		for (BpElement elem : strands) {
			elem.unprune(releaseAll);
		}
		// set the poolcount after pruning
		poolcount = 0;
		for (CsetItem itm : _items) {
			if (itm.isActive)
				++poolcount;
		}
		// check for adequate items to complete test
		if (poolcount + numAdministered >= minOpItems)
			return;
		// unprune items until there are enough items to complete test or all
		// items
		// are returned to pool
		// This does not honor the 'releaseAll' flag.
		for (CsetItem itm : _items) {

			if (itm.pruned) {
				itm.pruned = false;
				++poolcount;
			}
			if (poolcount + numAdministered >= minOpItems)
				return;
		}
	}

	// / <summary>
	// / Recycles items from previous administrations to attempt to satisfy
	// blueprint minimum requirements
	// / </summary>
	public void recycleItems(PriorAdmins admins) {
		int first = admins.getFirst();
		int last = admins.getLast();
		for (int i = first; i <= last; ++i) {
			// Start below the strands to be more discriminating (assuming there
			// is
			// something there with a min > 0)
			for (BpElement elem : elements.getValues()) {
				if (!elem.isStrand && elem.minRequired > 0) {
					elem.recycle(i, recycleAll);
				}
			}
			for (Strand strand : strands) {
				if (strand.minRequired > 0)
					strand.recycle(i, recycleAll);
			}
		}
		// Now check pool capacity to meet test-level minimum requirement
		poolcount = 0;
		for (CsetItem itm : _items) {
			if (itm.isActive)
				++poolcount;
		}
		if (poolcount + numAdministered >= minOpItems)
			return;
		UsedSequence adminItems;
		// starting from first chronological set of items used, arbitrarily
		// release
		// items until min requirement can be satisfied
		for (int i = first; i <= last; ++i) {
			adminItems = admins.get(i);
			if (adminItems != null)
				for (CsetItem item : adminItems.getItems()) {
					if (item.getUsed()) {
						item.getParentGroup().setUsed(false);
						poolcount += item.getParentGroup().getActiveCount();
						if (poolcount + numAdministered >= minOpItems)
							return;
					}
				}
		}
	}

	// / <summary>
	// / Use this to set examinee-specific start ability in a cset1-local
	// blueprint
	// / Not to be used for segment-generic blueprint common to all test
	// opportunities
	// / </summary>
	// / <param name="val"></param>
	public void SetStartAbility(double val) {
		this.startAbility = val;
		this.theta = val;
		for (Strand s : strands) {
			// only propagate the overall value for the student to an RC if that
			// RC still has the
			// default value from the item bank. Otherwise, we've already
			// calculated
			// a theta for the student on that RC in a previous segment, and we
			// want to keep
			// that information. We only calculate theta for prior segments in
			// adaptive2, so
			// this will always be true for the original alg, making this check
			// a wash.
			if (s.getStartAbility() == s.theta)
				s.setStartAbility(val);
//
//			s.theta = val;
//			s.setStartAbility(val);
		}
	}

	// / <summary>
	// / Add an affinity group, or non-strand content level to the segment's
	// blueprint
	// / </summary>
	// / <param name="element"></param>
	public void AddElement(BpElement element) {
		this.elements.addBpElement(element);

		if (element.isStrictMax == true)
			this.strictMaxes.add(element);
	}

	// / <summary>
	// / Add a strand to the segment's blueprint
	// / </summary>
	// / <param name="strand"></param>
	public void AddStrand(Strand strand) {
		this.elements.addBpElement(strand); // all elements go here

		this.strands.add(strand); // strands also go here for quick retrieval
		strand.vectorIndex = this.strands.size(); // see the statistical vectors
													// below
		// that are 1-based indexing

		if (strand.isStrictMax == true) // usually also go here for quick
										// retrieval
			this.strictMaxes.add(strand);
	}

	// / <summary>
	// / Get a vector of the bp elements that have strict maxes for use in
	// pruning
	// item group
	// / </summary>
	public BpElement[] getStrictmaxVector() {
		int n = strictMaxes.size();
		BpElement[] res = new BpElement[n];
		return strictMaxes.toArray(res);
	}

	// / <summary>
	// / Update the examinee-specific blueprint for contentlevel satisfaction
	// and
	// ability
	// / </summary>
	// / <param name="resp">An item response object</param>
	// / <param name="segment">The current segment under consideration</param>
	public void ProcessResponse(ItemResponse resp, int segmentPosition) throws ReturnStatusException {

		// is this operational item response scored?
		// Then use it to update ability UpdateAbility (resp.itemPosition,
		// resp.get_strand (), resp.get_b(), resp.score);
		if (!resp.isFieldTest && resp.score > -1) 
			UpdateAbility(resp);
			
		// is this operational item on this segment? Then update blueprint
		// satisfaction
		if (!resp.isFieldTest && resp.segmentPosition == segmentPosition) 
			UpdateSatisfaction(resp.getBaseItem());
					
        if (!resp.isFieldTest)
            numAdministeredTest++;
	}

	// / <summary>
	// / Use this to incrementally update the ability estimates at the overall
	// test level or at the strand level
	// / There is no lambda value for overall ability
	// / </summary>
	// / <param name="itemB"></param>
	// / <param name="itemScore">Make sure to send the 'compressed score' which
	// converts partial credit to [0,1] for use in the AA Rasch model</param>
	private void UpdateAbility(ItemResponse response) throws ReturnStatusException {
		actualInfoCalc.compute(this, response);
		for (String cl : response.getBaseItem().contentLevels) {
			ReportingCategory rc = getReportingCategory(cl);
			if (rc != null)
				actualInfoCalc.compute(rc, response);
		}
		this.lastAbilityPosition = Math.max(lastAbilityPosition,
				response.itemPosition);
	}

	public void UpdateSatisfaction(TestItem item) {
		UpdateSatisfaction(item, false);
	}

	// / <summary>
	// / Increments every blueprint element num items administered for each
	// content level this item
	// / </summary>
	// / <param name="item"></param>
	public void UpdateSatisfaction(TestItem item, boolean undo) {
		BpElement elem;
		if (undo)
			--numAdministered;
		else
			++numAdministered;
		// ++numAdministered; // this item was administered on this test
		for (String cl : item.getContentLevels()) {
			if ((elem = elements.getElementByID(cl)) != null)
				elem.numAdministered++;
			// else this may not be an error in a segmented test except for the
			// fact
			// that ProcessResponse is only passing items in this segment
		}
	}

	// // / <summary>
	// // / Use this to incrementally update the ability estimates at the
	// overall
	// // test level or at the strand level
	// // / There is no lambda value for overall ability
	// // / </summary>
	// // / <param name="itemB"></param>
	// // / <param name="itemScore">Make sure to send the 'compressed score'
	// which
	// // converts partial credit to [0,1] for use in the AA Rasch model</param>
	// private void UpdateAbility (int position, String strand, double itemB,
	// int itemScore)
	// {
	//
	// Strand s = getStrand (strand);
	// // NOTE: PRevious item responses on different segments may not have
	// strands
	// // on this segment
	// // In these cases we just want to update the overall ability and leave
	// the
	// // strands alone
	// // if (s == null)
	// // return "Missing strand: " + strand;
	// double p = AAMath.probCorrect (this.theta, itemB);
	// this.info += p * (1 - p);
	// this.theta += (itemScore - p) / info;
	// this.lastAbilityPosition = Math.max (lastAbilityPosition, position);
	// if (s != null)
	// s.UpdateAbility (itemB, itemScore);
	//
	// }

	public int getNumStrands() {
		return this.strands.size();
	}

	public Strand getStrand(String ID) {
		for (Strand s : this.strands) {
			if (s.ID.equals(ID))
				return s;
		}
		return null;
	}

	// / <summary>
	// / Return an integer that matches the vector position of the strand in the
	// below vectors
	// / </summary>
	// / <param name="strandName"></param>
	// / <returns></returns>
	public int strandIndex(String strandName) {
		Strand s;
		for (int i = 0; i < strands.size(); ++i) {
			s = strands.get(i);
			if (s.ID == strandName)
				return i + 1;
		}
		return -1; // not found

	}

	// / <summary>
	// / Get the information vector for test and strands. Test is in position 0.
	// Strands are 1-based indexing.
	// / </summary>
	public double[] getInfoVector() {
		Strand strnd;
		double[] vals = new double[getNumStrands() + 1];
		vals[0] = this.info;
		for (int i = 0; i < strands.size(); ++i) {
			strnd = strands.get(i);
			vals[i + 1] = strnd.info;
		}
		return vals;

	}

	// / <summary>
	// / Get the theta estimates for test and strands. Test is in position 0.
	// Strands are 1-based indexing.
	// / </summary>
	public double[] getThetaVector() {
		Strand strnd;
		double[] vals = new double[getNumStrands() + 1];
		vals[0] = this.theta;
		for (int i = 0; i < strands.size(); ++i) {
			strnd = strands.get(i);
			vals[i + 1] = strnd.theta;
		}
		return vals;

	}

	// / <summary>
	// / Get the lambda values for strands in 1-base indexing. (No test-level
	// value. Position zero is undefined.)
	// / </summary>
	public double[] getLambdaVector() {
		Strand strnd;
		double[] vals = new double[getNumStrands() + 1];
		// vals[0] = this.vals; there is NO test-level lambda so position zero
		// is
		// undefined
		for (int i = 0; i < strands.size(); ++i) {
			strnd = strands.get(i);
			vals[i + 1] = strnd.lambda;
		}
		return vals;
	}

	// / <summary>
	// / The cutscores for strands in 1-based indexing. Test level cut score not
	// relevant to adaptive item selector.
	// / </summary>
	public double[] getCutscoreVector() {
		Strand strnd;
		double[] vals = new double[getNumStrands() + 1];
		// vals[0] = this.vals; there is no relevant test-level cutscore so
		// position
		// zero is undefined
		for (int i = 0; i < strands.size(); ++i) {
			strnd = strands.get(i);
			vals[i + 1] = strnd.adaptiveCut;
		}
		return vals;
	}

	// / <summary>
	// / The individual weight for each strand in 1-based indexing.
	// / </summary>
	public double[] getStrandWeights() {
		Strand strnd;
		double[] vals = new double[getNumStrands() + 1];
		// vals[0] = this.vals; there is no relevant test-level cutscore so
		// position
		// zero is undefined
		for (int i = 0; i < strands.size(); ++i) {
			strnd = strands.get(i);
			vals[i + 1] = strnd.weight;
		}
		return vals;

	}

	// ===========================================================================================
	public Integer minOpItems = 1; 	// minimum number of operational items for a segment to be complete
	public Integer maxOpItems = 10; // maximum number of operational items allowed a test
    public Integer minOpItemsTest = 1;  // minimum number of operational items for a test (cross-segment) to be complete
    public Integer maxOpItemsTest = 10;  // maximum number of operational items for a test (cross-segment) to be complete

	//
	public Double bpWeight = 1D; // (w2) weight of blueprint satisfaction metric
									// relative to ability match metric (which
									// is always 1.0). Weight associated with
									// matching blueprint spec (w2)
	// ================================Adaptive2 Algorithm============
	//
	// new for 2013
	public Double rcAbilityWeight = 1D; // (w1) abliity weight WRT the reporting
										// categories. Weight associated with
										// matching reporting category
										// information (w1)
	public double precisionTarget = Double.MAX_VALUE;
	public double precisionTargetMetWeight;
	public double precisionTargetNotMetWeight;
	public double standardError = AAMath.SEfromInfo(this.startInfo);

	/**
	 * Weight associated with matching overall information (w0)
	 */
	public Double overallInformationMatchWeight = 1D; // = abilityWeight

	/**
	 * H0 function weight before target is met (a)
	 */
	public Double hweightBeforeOverallTargetMet = 0.2D; // =
														// precisionTargetNotMetWeight

	/**
	 * H0 function weight after target is met (b)
	 */
	public Double hweightAfterOverallTargetMet = 0.1D; // =
														// precisionTargetMetWeight

	/**
	 * Required overall target information (t0)
	 */
	public Double overallTargetInformation = 10D; // = precisionTarget

	/**
	 * CSet1Size
	 */
	public Integer cSet1Size = CSET1_SIZE;

	/**
	 * CSet2Size // cSet2Size must be less or equal than cSet1Size always
	 */
	public Integer cSet2Size = 5;

	/**
	 * The overall score/proficiency, used in consideration of TermTooClose
	 */
	public Double adaptiveCut = 10D;

	/**
	 * The number of standard errors below which the difference is considered
	 * “too close” to the adaptive cut to proceed. In general, this will signal
	 * proceeding to a final segment that contains off-grade items.
	 */
	public Double tooCloseSEs = 3D;
	// ================================================================
	/**
	 * Start position for field test items
	 */
	public Integer fieldTestStartPosition; // = FTStartPos

	/**
	 * End position for field test items
	 */
	public Integer fieldTestEndPosition; // = FTEndPos

	/**
	 * Required minimum number of field test items
	 */
	public Integer fieldTestMinimumItems; // = FTMinItems

	/**
	 * Required maximum number of field test items
	 */
	public Integer fieldTestMaximumItems; // = FTMaxItems
	// ==============================================================
	// specific to examinee
	//
	private ActualInfoComputation actualInfoCalc;

	public void setActualInfoComputation(ActualInfoComputation actualInfoCalc) {
		this.actualInfoCalc = actualInfoCalc;
	}

	// ===============================================================
	// termination conditions
	/**
	 * Flag to indicate whether to terminate based on achieving required minimum
	 * in reporting categories and overall
	 */
	public Boolean terminateBasedOnCount = false;
	// public boolean terminationMinCount;

	/**
	 * Flag to indicate whether to terminate based on achieving required overall
	 * information
	 */
	public Boolean terminateBasedOnOverallInformation = false;
	// public boolean terminationOverallInfo;
	/**
	 * Flag to indicate whether to terminate based on achieving required
	 * information in each of the reporting categories
	 */
	public Boolean terminateBasedOnReportingCategoryInformation = false;
	// public boolean terminationRCInfo;
	/**
	 * Terminate if achieved a score insufficiently distant from a specified
	 * score with sufficient precision
	 */
	public Boolean terminateBasedOnScoreTooClose = false;
	// public boolean terminationTooClose;
	/**
	 * Flag to indicate whether to use AND or OR with the above conditions when
	 * they are set true to decide the termination
	 */
	public Boolean terminateBaseOnFlagsAnd = false;
	// public boolean terminationFlagsAnd;
	// termination conditions
	// ================================================================
	
	// ================================================================
    // new for 2014-2015 to support off-grade items
    public OffGradeItemsProps offGradeItemsProps = new OffGradeItemsProps();
    //      specific to examinee
    public String 	offGradePoolFilter;
    // new for 2014-2015 to support off-grade items
	// ================================================================

	/**
	 * List of blue print elements indexed by their names
	 */
	private HashMap<String, BpElement> _bluePrintElements = new HashMap<String, BpElement>();

	/**
	 * List of reporting categories indexed by their names
	 */
	private HashMap<String, ReportingCategory> _reportingCategories = new HashMap<String, ReportingCategory>();

	/**
	 * Constructor
	 */
	public Blueprint() {
	}

	/**
	 * Get blue print element named as given
	 * 
	 * @param ID
	 * @return
	 */
	public BpElement getBPElement(String ID) {
		return _bluePrintElements.containsKey(ID) ? _bluePrintElements.get(ID)
				: getReportingCategory(ID);
	}

	/**
	 * Get all blue print elements
	 * 
	 * @return
	 */
	public ArrayList<BpElement> getBPElements() {
		ArrayList<BpElement> elements = new ArrayList<BpElement>(
				_reportingCategories.values());
		elements.addAll(_bluePrintElements.values());
		return elements;
	}

	/**
	 * Get reporting category - given its name
	 * 
	 * @param ID
	 * @return
	 */
	public ReportingCategory getReportingCategory(String ID) {
		return _reportingCategories.containsKey(ID) ? _reportingCategories
				.get(ID) : null;
	}

	/**
	 * Get all the reporting categories
	 * 
	 * @return
	 */
	public ArrayList<ReportingCategory> getReportingCategories() {
		return new ArrayList<ReportingCategory>(_reportingCategories.values());
	}
	
	public void setStartAbilityRC(double val) {
        //For adaptive2, we'll store a value in the TestOpportunity.initialAbility field
        //  to signal that there is no prior ability for the student.  In this case,
        //  the default values from the IB should be used at the segment and RC levels.
        //  These have already been set as default start values when the general blueprint was 
        //  loaded, so nothing to do here.
		if(val == -9999)
			return;
		
		this.startAbility = val;
		this.theta = val;
		for (ReportingCategory rc : _reportingCategories.values()) {
			// only propagate the overall value for the student to an RC if that
			// RC still has the
			// default value from the item bank. Otherwise, we've already
			// calculated
			// a theta for the student on that RC in a previous segment, and we
			// want to keep
			// that information. We only calculate theta for prior segments in
			// adaptive2, so
			// this will always be true for the original alg, making this check
			// a wash.
			if (rc.getStartAbility() == rc.theta)
				rc.setStartAbility(val);
		}
	}


	/**
	 * Initialize overall blue print information
	 * 
	 * @param segmentRow
	 * @throws SQLException
	 */
	public void initializeOverallBluePrint(SingleDataResultSet res)
			throws SQLException {
		DbResultRecord record;
		record = res.getCount() > 0 ? res.getRecords().next() : null;
		if (record != null) {
			segmentKey 					= record.<String> get("segmentKey");
			segmentID 					= record.<String> get("segmentID");
			segmentPosition 			= long2Integer(record, "segmentPosition");

			minOpItems 					= long2Integer(record, "minOpItems");
			maxOpItems 					= long2Integer(record, "maxOpItems");

			bpWeight 					= float2Double(record, "bpWeight");
			itemWeight 					= float2Double(record, "itemWeight");
			abilityOffset				= float2Double(record, "abilityOffset");
			randomizerIndex 			= long2Integer(record, "randomizer");
			randomizerInitialIndex 		= long2Integer(record, "initialrandom");
			
			startInfo 					= float2Double(record, "startInfo");
			info 						= startInfo;
			standardError 				= AAMath.SEfromInfo(startInfo); // initialize SE based on start info

			startAbility 				= float2Double(record, "startAbility");
			theta 						= startAbility;
			
			cset1Order 					= record.<String> get("cset1Order");
			cSet1Size 					= record.<Integer> get("cset1size");
			// cSet2Size must be less or equal than cSet1Size always
			cSet2Size 					= (randomizerIndex <= cSet1Size) ? randomizerIndex : cSet1Size;
			slope 						= float2Double(record, "slope");
			intercept					= float2Double(record, "intercept");
			
			adaptiveVersion 			= record.<String> get("adaptiveVersion");
			
			overallInformationMatchWeight = float2Double(record, "abilityWeight");
			abilityWeight 				= (overallInformationMatchWeight != null) 
					? overallInformationMatchWeight: abilityWeight;
			rcAbilityWeight 			= float2Double(record, "rcAbilityWeight");

			overallTargetInformation			= float2Double(record, "precisionTarget");
			precisionTarget 	= (overallTargetInformation != null)
					?overallTargetInformation: precisionTarget;			
			precisionTargetMetWeight 	= float2Double(record, "precisionTargetMetWeight");
			precisionTargetNotMetWeight = float2Double(record, "precisionTargetNotMetWeight");
			
			fieldTestStartPosition 		= long2Integer(record, "FTStartPos");
			fieldTestEndPosition 		= long2Integer(record, "FTEndPos");
			fieldTestMinimumItems 		= long2Integer(record, "FTMinItems");
			fieldTestMaximumItems 		= long2Integer(record, "FTMaxItems");

			adaptiveCut 				= float2Double(record, "adaptiveCut");
			tooCloseSEs 				= float2Double(record, "TooCloseSEs");

			terminateBasedOnCount 		= record.<Boolean> get("terminationMinCount");
			terminateBasedOnOverallInformation = record.<Boolean> get("terminationOverallInfo");
			terminateBasedOnReportingCategoryInformation = record.<Boolean> get("terminationRCInfo");
			terminateBasedOnScoreTooClose = record.<Boolean> get("terminationTooClose");
			terminateBaseOnFlagsAnd 	= record.<Boolean> get("terminationFlagsAnd");

			minOpItemsTest 				= long2Integer(record, "minOpItemsTest");
			maxOpItemsTest 				= long2Integer(record, "maxOpItemsTest");
		}
	}

	/**
	 * Load the blue print information from tables
	 * 
	 * @param contentLevelTable
	 * @throws SQLException
	 */
	public void initializeBluePrintConstraints(SingleDataResultSet res)
			throws SQLException {
		// Get the element level information
		_bluePrintElements.clear();
		_reportingCategories.clear();
		DbResultRecord record;
		Iterator<DbResultRecord> recItr = res.getRecords();

		while (recItr.hasNext()) {
			record = recItr.next();
			Boolean isRC = (record.<String> get("isReportingCategory").equalsIgnoreCase("true")) ? true : false;
			if (isRC) {
				ReportingCategory rc = new ReportingCategory();
				rc.initialize(record);
				rc.setReportingCategory(true);
				_reportingCategories.put(rc.ID, rc);
				elements.addBpElement(rc);
			} else { // Where Affinity Group?
				BpElement bpElement = new BpElement();
				bpElement.initialize(record);
				_bluePrintElements.put(bpElement.ID, bpElement);
				elements.addBpElement(bpElement);
			}
		}
	}

	/**
	 * 
	 * Loads values from tblItemSelectionParm into the properties/fields for the
	 * corresponding bpContainer.
	 * 
	 * <param name="bpContainer">Either the Bluepring, BpElement, or
	 * ReportingCategory that is being populated</param> 
	 * <param name="bpElementID">Either Blueprint.segmentID or BpElement.ID</param>
	 */
	public void initializeBluePrintOffGradeItemsProps(SingleDataResultSet res)
			throws SQLException {
		DbResultRecord record;
		Iterator<DbResultRecord> recItr = res.getRecords();

		while (recItr.hasNext()) {
			record = recItr.next();
			
			String value 	= record.<String> get("value");
			if(value == null || value.isEmpty())
				continue;
			
			String bpElementId 	= record.<String> get("bpElementId");
			String name 	= record.<String> get("name"); // this is field name in a class
			
			if(bpElementId.equalsIgnoreCase(this.segmentID))
			{
				this.offGradeItemsProps.populateBluePrintOffGradeItemsDesignator(name, value);
			}
			else
			{// find ReportingCategoryt by bpElementId				
				ReportingCategory rc = this._reportingCategories.get(bpElementId);
				if(rc != null)
				{
					rc.putItemSelectionParam(name, value);
				}
				else
				{// find BpElement by bpElementId
					BpElement be = this._bluePrintElements.get(bpElementId);
					if(be != null)
					{
						be.putItemSelectionParam(name, value);
					}
					else
						_logger.warn("There is no bpElement with bpElementId = " + bpElementId);
				}				
			}
		}
	}
	
	//OffGradeItemCountByDesignator	
	public void initializeBluePrintOffGradeItemsDesignator(SingleDataResultSet res)
			throws SQLException {
		DbResultRecord record;
		Iterator<DbResultRecord> recItr = res.getRecords();

		while (recItr.hasNext()) {
			record = recItr.next();
			
			String propvalue 	= record.<String> get("propvalue");
			Integer count 		= long2Integer(record, "count");
			if(propvalue != null)
			this.offGradeItemsProps.countByDesignator.put(propvalue, count);		
		}
		
	}

	private Double float2Double(DbResultRecord record, String columnName) {
		try {
			return record.<Double> get(columnName);
		} catch (Exception e) {
			return new Double(record.<Float> get(columnName));
		}

	}

	private Integer long2Integer(DbResultRecord record, String columnName) {
		try {
			return record.<Integer> get(columnName);
		} catch (Exception e) {
			Long tmp = record.<Long> get(columnName);
			return new Integer(tmp.toString());
		}

	}
	
	@Override
	public Double getInfo() {
		return info;
	}

	@Override
	public void setInfo(Double info) {
		this.info = info;
	}

	@Override
	public Double getTheta() {
		return theta;
	}

	@Override
	public void setTheta(Double theta) {
		this.theta = theta;
	}

	@Override
	public Double getStandartError() {
		return standardError;
	}

	@Override
	public void setStandartError(Double standartError) {
		this.standardError = standartError;
	}

	/**
	 * Updates the standard error. Called when all responses so far have been
	 * processed and actual info values are up to date.
	 *  
	 */
    public void updateStandardError()
    {
        actualInfoCalc.calculateSE(this);
        for (ReportingCategory rc : _reportingCategories.values())
            actualInfoCalc.calculateSE(rc);
    }
    /**
    * If off-grade items are supported and configured for this test, and if the student qualifies to have
    * off-grade items added to the pool, this method will return the filter that will be used to add
    * those items.
    * 
    * <returns>The filter that should be used to add off-grade items to the student's custom item pool, or null if no off-grade items are to be added at this time.</returns>
    * */
    public String getOffGradeFilter() throws ReturnStatusException
    {
       // if we're not measuring ability, we don't have a way to determine whether we can introduce off-grade items
        if (!getMeasureAbility())
            return null;
 
        // no filters were configured
        if (this.offGradeItemsProps.countByDesignator.size() == 0)
            return null;

        // haven't hit the threshold of items administered (cross-segment) yet; return no filter
        if (numAdministeredTest < this.offGradeItemsProps.minItemsAdministered)
            return null;

        CDF cdf = new CDF(this.theta, this.standardError);
        double p = cdf.Calculate(thetaStar());

        for (String designator: this.offGradeItemsProps.countByDesignator.keySet())
        {
            switch (designator.toUpperCase())
            {
                case "OFFGRADE BELOW":
                    if ((1 - p) < this.offGradeItemsProps.probAffectProficiency)
                        return designator;
                    break;
                case "OFFGRADE ABOVE":
                    if (p < this.offGradeItemsProps.probAffectProficiency)
                        return designator;
                    break;
                default:
                    throw new ReturnStatusException(String.format("Do not understand off-grade item designator: %s", designator));
            }
        }
        return null; 
    }

    // measure ability if either of the ability weights is not 0 and the cset1Order != DISTRIBUTION, which
    // implies that on bp should factor into item selection.
    public Boolean getMeasureAbility()
    {
    	return !((abilityWeight == 0 && rcAbilityWeight == 0)
    			|| cset1Order.equalsIgnoreCase("DISTRIBUTION"));
    }

	/**
	 * Used to determine if off-grade items should be introduced into the pool
	 * θ* = K/(k-K) (k/K θ^ - t) Where k represents the number of items
	 * administered so far, K represents the total test length, θ^ is the
	 * current overall theta, and t represents the proficiency threshold NOTE:
	 * these are test-level values, not segment-level values 
	 */
    private double thetaStar() throws ReturnStatusException
    {
        // assumption: proficiency cuts on segmented tests are the same for all segments
        if (this.offGradeItemsProps.proficientTheta == null)
        {
        	String error = "Cannot evaluate off-grade item trigger.  No proficiency cut score was provided.";
        	 _logger.error ( error);
            throw new ReturnStatusException(error);
        }
        Double K = (double)this.maxOpItemsTest; 
        if (numAdministeredTest >= K) 
        	K = (double)numAdministeredTest + 1.0;
        return (K / (double)(numAdministeredTest - K)) * (((numAdministeredTest / K) * theta) - this.offGradeItemsProps.proficientTheta);
    }

	public void dumpBP()// for old algorithm
	{
		String shift = "         ";
		_logger.info(shift + "    BP:");
		_logger.info(shift + "segmentKey: " + segmentKey);
		_logger.info(shift + "position: " + segmentPosition);
		_logger.info(shift + "minOpItems: " + minOpItems);
		_logger.info(shift + "maxOpItems: " + maxOpItems);
		_logger.info(shift + "bpWeight: " + bpWeight);
		_logger.info(shift + "abilityWeight: " + abilityWeight);
		_logger.info(shift + "itemWeight: " + itemWeight);
		_logger.info(shift + "randomizerIndex: " + randomizerIndex);
		_logger.info(shift + "randomizerInitialIndex: "
				+ randomizerInitialIndex);
		_logger.info(shift + "abilityOffset: " + abilityOffset);
		_logger.info(shift + "startAbility: " + startAbility);
		_logger.info(shift + " slope: " + slope);
		_logger.info(shift + " intercept: " + intercept);
		_logger.info(shift + " startInfo: " + startInfo);
		_logger.info(shift + " adaptiveVersion: " + adaptiveVersion);
		_logger.info(shift + "numAdministered: " + numAdministered);
		_logger.info(shift + "info: " + info);
		_logger.info(shift + "theta: " + theta);
		_logger.info(shift + "lastAbilityPosition: " + lastAbilityPosition);
		_logger.info(shift + "poolcount: " + poolcount);
		_logger.info(shift + "_items.size : " + _items.size());
		_logger.info(shift + "BpElements.size: " + elements.getValues().size());
		_logger.info(shift + "strictMaxes.size: " + strictMaxes.size());
		_logger.info(shift + "strands.size: " + strands.size());
	}

	private String dumpBp2String2() {

		return FilePrint.fieldNamesValues(this);
	}

	//
	private String dumpBp2String() {
		StringBuilder stb = new StringBuilder();

		List<String> fieldNames = Arrays.asList("segmentKey", "segmentID",
				"segmentPosition", "minOpItems", "maxOpItems", "bpWeight",
				"itemWeight", "abilityOffset", "randomizerIndex",
				"randomizerInitialIndex", "startInfo", "startAbility",
				"cSet2Size", "cset1Order", "slope", "intercept",
				"adaptiveVersion", "abilityWeight", "rcAbilityWeight",
				"rcAbilityWeight", "precisionTarget",
				"precisionTargetMetWeight", "precisionTargetNotMetWeight",
				"adaptiveCut", "tooCloseSEs",
				"terminateBasedOnOverallInformation",
				"terminateBasedOnReportingCategoryInformation",
				"terminateBasedOnCount", "terminateBasedOnScoreTooClose",
				"terminateBaseOnFlagsAnd");
		return FilePrint.fieldNamesValues(fieldNames, this);
	}

	@Override // string IBpInfoContainer.name in C# code
	public String getName() {		
		return segmentKey;
	}

}
