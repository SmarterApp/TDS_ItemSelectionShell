/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IItemSelectionDLL;
import tds.dll.api.ISimDLL;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;

public class AA2DBLoader extends AbstractDBLoader implements IItemSelectionDBLoader {
	
	@Autowired
	IItemSelectionDLL iSelDLL = null;
	
	private static Logger _logger = LoggerFactory.getLogger(AA2DBLoader.class);

	public AA2DBLoader() {
	};

	// public ItemGroup LoadItemGroup(StudentTestOpportunity opportunity, int
	// isFieldTest) throws SQLException, Exception
	@Override
	public ItemGroup getItemGroup(SQLConnection connection, UUID oppkey, String segmentKey,
			String groupID, String blockID, Boolean isFieldTest)
			throws ReturnStatusException {

		ItemGroup group = new ItemGroup();
		MultiDataResultSet multiDataResultSet = iSelDLL.AA_GetItemgroup_SP(
				connection, oppkey, segmentKey, groupID, blockID,
				isFieldTest, false);

		Iterator<SingleDataResultSet> setItr = multiDataResultSet
				.getResultSets();

		SingleDataResultSet groupTable = null;
		SingleDataResultSet itemTable = null;

		// if multiDataResultSet has 2 SinleDataResultSets: first -> groups;
		// second -> items
		// if has only one: it is items
		// above is OLD Comments -- this comments contradicts the new java
		// implementations
		// see commented LoadItemGroup() after this implementation
		try {
			if (setItr.hasNext()) {
				if (multiDataResultSet.getUpdateCount() > 1) {
					groupTable = setItr.next();
					itemTable = setItr.next();
				} else {
					itemTable = setItr.next();
				}
			}
			DbResultRecord record;
			Iterator<DbResultRecord> recItr = groupTable.getRecords();
			record = groupTable.getCount() > 0 ? groupTable.getRecords().next()
					: null;
			if (record != null) {
				group.initialize(record);
			}
			group.items = new ArrayList<TestItem>();
			recItr = itemTable.getRecords();
			while (recItr.hasNext()) {
				record = recItr.next();
				if (record != null) {
					TestItem testItem = new TestItem();
					testItem.initialize(record);
					group.items.add(testItem);
				}
			}
		} catch (SQLException e) {
			_logger.equals(e.getMessage());
			throw new ReturnStatusException(e.getMessage());
		} catch (Exception e) {
			_logger.equals(e.getMessage());
			throw new ReturnStatusException(e.getMessage());
		}

		return group;
	}

	/**
	 * // * Method used to get item group for non-adaptive or field test items
	 * // * @param opportunity // * @param isFieldTest // * @return //
	 */

	@Override
	public void loadSegment(SQLConnection connection, String segmentKey, TestSegment segment,
			UUID sessionKey) throws ReturnStatusException,
			ItemSelectionException {
		MultiDataResultSet dataSets = null;
		Boolean controlTriples = false;
		if (sessionKey == null) { // TODO: Boolean controlTriples = false. From what we will get this parameter? 
			dataSets = iSelDLL.AA_GetSegment2_SP(connection, segmentKey, controlTriples);
		} else {
			dataSets = iSelDLL.AA_SIM_GetSegment2_SP(connection, sessionKey, segmentKey, controlTriples);
		}

		Iterator<SingleDataResultSet> sItr = dataSets.getResultSets();

		if (dataSets.getUpdateCount() < 4) {
			String error = "loadSegment method return is corrupted: there is not any single data set";
			_logger.error(error);
			throw new ItemSelectionException(error);
		}
		try {

			// Get the segment related info table
			if (sItr.hasNext()) {
				segment.initializeOverallBluePrint(sItr.next());
			}
			// Get the blueprint (content level) related info table
			if (sItr.hasNext()) {
				segment.segmentBlueprint.initializeBluePrintConstraints(sItr
						.next());
			}
			// Get the item group info table
			if (sItr.hasNext()) {
				segment.segmentItemPool.initializeItemGroups(sItr.next());
			}
			// Get the item info table
			if (sItr.hasNext()) {
				//segment.segmentItemPool.InitializeTestItems(sItr.next());
				segment.segmentItemPool.InitializeTestItems(sItr.next(), segment.segmentBlueprint.segmentPosition);
			}
			// Get the sibling item info table
			if (sItr.hasNext()
					&& !segment.segmentKey.equals(segment.parentTest)) {
				segment.segmentItemPool.InitializeSiblingItems(sItr.next());
			}
			// Get the dimension information for items
			if (sItr.hasNext()) {
				segment.segmentItemPool.InitializeItemDimensions(sItr.next());
			}
			if(controlTriples) // The same structure as in AA_GetSegment2_SP() and AA_SIM_GetSegment2_SP()
			{
				if (sItr.hasNext())
				{
					segment.segmentBlueprint.initializeBluePrintOffGradeItemsProps(sItr.next());
				}
			}
			if (sItr.hasNext())
			{
				segment.segmentBlueprint.initializeBluePrintOffGradeItemsDesignator(sItr.next());
			}
			
		} catch (Exception e) {
			throw new ItemSelectionException(
					" Error occurs in loadSegment method: " + e.getMessage());
		}
	}


	@Override
	public StudentHistory2013 loadOppHistory(SQLConnection connection, UUID oppkey, String segmentKey)
			throws ItemSelectionException {
		StudentHistory2013 studentHistory = new StudentHistory2013();

		SingleDataResultSet res;
		try {

			MultiDataResultSet mDRSet = iSelDLL.AA_GetDataHistory2_SP(
					connection, oppkey, segmentKey);
			Iterator<SingleDataResultSet> dsetItr = mDRSet.getResultSets();
			// Table 0: 1 row,
			// Get the custom item pool for the student
			if (dsetItr.hasNext()) {
				res = dsetItr.next();
				studentHistory.initializeItemPool(res);
			}
			// Table 1: groupStrings. 1 row for each previously completed test
			// in the same subject.
			// Get previous item groups for the student
			if (dsetItr.hasNext()) {
				res = dsetItr.next();
				studentHistory.initializePreviousItemGroups(res);
			}
			// Table 2: Field test items selected for this test. 1 row for each
			// field
			// Get the field test item groups
			if (dsetItr.hasNext()) {
				res = dsetItr.next();
				studentHistory.initializeFieldTestItemGroups(res);
			}
			// Table 3: Items administered this test. 1 row for each item
			// previously
			// selected (administered or not)
			// Get the previous item responses
			if (dsetItr.hasNext()) {
				res = dsetItr.next();
				studentHistory.initializePreviousResponses(res);
			}
		} catch (Exception e) {
			_logger.error("Error occurs in LoadOppHistory method: "
					+ e.getMessage());
			throw new ItemSelectionException(e.getMessage(), e);
		}
		return studentHistory;
	}

	@Override
	public boolean setSegmentSatisfied(SQLConnection connection, UUID oppkey, Integer segmentPosition,
			String reason) throws ReturnStatusException {
		
		return iSelDLL.AA_SetSegmentSatisfied_SP(connection, oppkey, segmentPosition, reason);
	}

	/**
    * Add off-grade items (depending on poolFilterProperty) to the current opportunity.
    *
    * <param name="oppkey"></param>
    * <param name="designation">From OffGradeItemPoolFilter; expecting OFFGRADE ABOVE or OFFGRADE BELOW</param>
    * <param name="segmentKey">Typlically null to add off-grade items to the entire test.  Can be passed in to do so for the current segment only.</param>
    * <param name="reason">Returns the reason for the status from the sproc to the caller.  Will be String.Empty for typical successful call.</param>
    * <returns>status: success | failed</returns>     
     */
	@Override
	public String addOffGradeItems(SQLConnection connection, UUID oppkey,
			String designation, String segmentKey, _Ref<String> reason) throws ReturnStatusException {
		String status = "failed";
		reason.set("");
		try {
			SingleDataResultSet res = iSelDLL.AA_AddOffgradeItems_SP(connection, oppkey,
					designation /* poolfilterProperty */, segmentKey);
			DbResultRecord record;
			record = res.getCount() > 0 ? res.getRecords().next() : null;
			if (record != null) {
				status = record.<String> get("status");
				reason.set(record.<String> get("reason"));
			}
		} catch (Exception e) {
			_logger.error("Failed to add Offgrade items: " + e.getMessage());
			throw new ReturnStatusException(e);
		}
		return status;
	}
	//===========================================================================================
//	/**
//	 * (c) Copyright American Institutes for Research, unpublished work created 2008-2013
//	 *  All use, disclosure, and/or reproduction of this material is
//	 *  prohibited unless authorized in writing. All rights reserved.
//	 *
//	 *  Rights in this program belong to:
//	 *  American Institutes for Research.
//	 *  
//	 *  Code based on AIROnline2012 C# project - Re-factored here 
//	 */
//===========================================================================================
	
}
