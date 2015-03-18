/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.aironline;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.DLLHelper;
import tds.itemselection.algorithms.TestAdaptiveSelector2;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.debug.FilePrint;
import tds.itemselection.impl.blueprint.ActualInfoComputation;
import tds.itemselection.impl.bpmatchcomputation.BPMatchByItemWithIterativeGroupItemSelection;
import tds.itemselection.impl.item.PruningStrategySmarter;
import tds.itemselection.impl.sets.Cset1Factory2013;
//import tds.itemselection.debug.DLLHelper;
import tds.itemselection.impl.sets.CsetGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.TestSegment;
import tds.itemselection.termination.TerminationManager;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;

@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class TestSelector2013 {
	@Autowired
	@Qualifier("aa2013Selector")
	private IItemSelection itemSelector = null;

	@Autowired
	@Qualifier("aa2DBLoader")
	private IItemSelectionDBLoader loader = null;

	@Autowired
	private DLLHelper _myDllHelper = null;

	private static final Logger _logger = LoggerFactory.getLogger(TestSelector2013.class);
	private SQLConnection 		_connection 				= null;
	private Boolean 			_preexistingAutoCommitMode 	= null;
	private boolean 			_debug 						= true;
	private String 				csvDelimeter 				= ", ";

	@Before
	public void setUp() throws Exception {
	    try
	    {
	      _connection = _myDllHelper.getSQLConnection();
	      _preexistingAutoCommitMode = _connection.getAutoCommit ();
	      _connection.setAutoCommit (false);
	    } catch (Exception e) {
	      System.out.println ("Exception in this test: " + e);
	      _logger.error (e.getMessage ());
	      throw e;
	    }
	}

	@After
	public void tearDown() throws Exception {
	    try
	    {
	      _connection.rollback ();
	      _connection.setAutoCommit (_preexistingAutoCommitMode);
	      _logger.info ("All tranzactions are rollbacked");

	    } catch (Exception e)
	    {
	      _connection.rollback ();
	      _connection.setAutoCommit (_preexistingAutoCommitMode);
	      _logger.info ("All tranzactions are rollbacked");
	      _logger.info ("Exception in the testGetNextItemGroup test: " + e);
	      _logger.error (e.getMessage ());
	      throw e;
	    }
	}

	/**
	 * main test
	 * This test with localhost:3306/session_aa2 
	 * databases
	 * see opentestsystem-override-properties_aa2.xml
	 * 
	 * test for Jon -- 
	 * tds-db.dev.opentestsystem.org:3306/session
	 * @throws Exception
	 */
	@Test
	public final void testGetNextItemGroup() throws Exception {
		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey) for AdaptiveSelector2013: ");
		System.out.println();

		try {
			String OPPKEY = "c50e7abe-673b-4d14-a7d0-81108de317a4";	// test for Jon
//			String OPPKEY = "0514d9cb-1e14-4c04-ab75-5e143245861a";	// main test
//			String OPPKEY = "a1674ef0-9042-428e-beab-9f082bdc93f8"; // This is student with 3 previous items!
//			String OPPKEY = "24f000c7-a32f-439b-a55b-9a6e74af0649";
			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);

			ItemGroup itemGr;
			ItemCandidatesData itemCandidates = null;

			itemCandidates = loader.getItemCandidates(_connection, oppkey);
			if (_debug) {
				itemCandidates.dumpDebugItemCandidatesData();
			}

			if (!itemCandidates.getIsSimulation())
				itemCandidates.setSession(null);

			itemGr = itemSelector.getNextItemGroup(_connection, itemCandidates);
			// TODO itemGr.dump();

			if(itemGr != null)
			{
				System.out.println(String.format("groupID: %s", itemGr.groupID));
				System.out.println(String.format("itemsRequired: %s",	itemGr.getNumRequired()));
				System.out.println(String.format("maxReqItems: %s", itemGr.getMaxItems()));
				_logger.info(String.format("groupID: %s", itemGr.groupID));
				_logger.info(String.format("itemsRequired: %s",	itemGr.getNumRequired()));
				_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));
	
				List<TestItem> items = itemGr.getItems();
				int itemsNumber = items.size();
				System.out.println(String.format("Number of items: %s", itemsNumber));
				_logger.info(String.format("Number of items: %s", itemsNumber));
//				for (TestItem item : items) {
//					System.out.println("Group ID = " + item.groupID);
//					System.out.println("Item ID = " + item.itemID);
//					item.dump();					
//				}
				System.out.println("TEST FINISHED");				
			} else
			{
				_logger.info("Selected Item Group is NULL");
				System.out.println("Selected Item Group is NULL. Algorithm = " + itemCandidates.getAlgorithm());
			}
		} catch (Exception e) {
			System.out.println("Exception in the testGetNextItemGroup test: "
					+ e);
			if (e instanceof java.lang.NullPointerException) {
				System.out.println("Stack: " + e.getStackTrace());
			}
			_logger.error(e.getMessage());
			throw e;
		} catch (AssertionError error) {
			System.out
					.println("AssertionError in the testGetNextItemGroup test: "
							+ error);
			_logger.error(error.getMessage());
			throw error;
		}				
	}
	/**
	 * This test with localhost:3306/session databases
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetNextItemGroupAfterOffgradeItemsOption() throws Exception {
		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey) for AdaptiveSelector2013: ");
		System.out.println();

		try {
			String OPPKEY = "93152d7f-8b04-442d-916b-eb9f5c5c43fd";	
			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);

			ItemGroup itemGr;
			ItemCandidatesData itemCandidates = null;

			itemCandidates = loader.getItemCandidates(_connection, oppkey);
			if (_debug) {
				itemCandidates.dumpDebugItemCandidatesData();
			}

			if (!itemCandidates.getIsSimulation())
				itemCandidates.setSession(null);

			itemGr = itemSelector.getNextItemGroup(_connection, itemCandidates);
			// TODO itemGr.dump();

			if(itemGr != null)
			{
				System.out.println(String.format("groupID: %s", itemGr.groupID));
				System.out.println(String.format("itemsRequired: %s",	itemGr.getNumRequired()));
				System.out.println(String.format("maxReqItems: %s", itemGr.getMaxItems()));
				_logger.info(String.format("groupID: %s", itemGr.groupID));
				_logger.info(String.format("itemsRequired: %s",	itemGr.getNumRequired()));
				_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));
	
				List<TestItem> items = itemGr.getItems();
				int itemsNumber = items.size();
				System.out.println(String.format("Number of items: %s", itemsNumber));
				_logger.info(String.format("Number of items: %s", itemsNumber));
				System.out.println("TEST FINISHED");
			} else
			{
				_logger.info("Selected Item Group is NULL");
				System.out.println("Selected Item Group is NULL. Algorithm = " + itemCandidates.getAlgorithm());
			}
		} catch (Exception e) {
			System.out.println("Exception in the testGetNextItemGroup test: "
					+ e);
			if (e instanceof java.lang.NullPointerException) {
				System.out.println("Stack: " + e.getStackTrace());
			}
			_logger.error(e.getMessage());
			throw e;
		} catch (AssertionError error) {
			System.out
					.println("AssertionError in the testGetNextItemGroup test: "
							+ error);
			_logger.error(error.getMessage());
			throw error;
		}				
	}
	/**
	 * This test with localhost:3306/session databases (copy of the same tds-db.dev.opentestsystem.org)
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testOffgradeItemsOption() throws Exception {

		_logger.info("Test of OffgradeItems option for AdaptiveSelector2013: ");
		System.out.println("Test of OffgradeItems option for AdaptiveSelector2013: ");

		try {
			String OPPKEY = "93152d7f-8b04-442d-916b-eb9f5c5c43fd"; 
			// student2: '(SBAC)SBAC-OP-ADAPTIVE-G5M-MATH-5-Spring-2014-2015'
			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);
			System.out.println("Oppkey =  " + OPPKEY);

			ItemCandidatesData itemCandidates = null;

			itemCandidates = loader.getItemCandidates(_connection, oppkey);
			if (_debug) {
				itemCandidates.dumpDebugItemCandidatesData();
				// itemCandidates.dumpItemCandidatesData ();
			}

			if (!itemCandidates.getIsSimulation())
				itemCandidates.setSession(null);

			// TODO: compare itemCandidates.getSegmentKey() and segmentKey bellow
			String segmentKey = "(SBAC)SBAC-OP-ADAPTIVE-G5M-MATH-5-Spring-2014-2015";
			TestSegment segment = new TestSegment(segmentKey);

			try {
				loader.loadSegment(_connection, segmentKey, segment, null); // null <==> not Simulation
			} catch (ReturnStatusException | ItemSelectionException e) {
				e.printStackTrace();
			}

			// first part of public ItemGroup selectNext() function
			Random rand = new Random();
			Cset1Factory2013 csetFactory = new Cset1Factory2013(
					itemCandidates.getOppkey(), loader,
					new BPMatchByItemWithIterativeGroupItemSelection(rand),
					new ActualInfoComputation(), new PruningStrategySmarter(
							rand));
			csetFactory.setSegment(segment);

			try {
				// load all previous responses and calculate working actuals
				csetFactory.LoadHistory(_connection);
				// not check termination!

				// now that we have a working bp and theta estimate, and the
				// test is not terminated,
				// check to see if we need to append any off-grade items to the
				// pool
				if (csetFactory.getBp().offGradeItemsProps.countByDesignator.size() > 0 
					// 1 or more off-grade designators are configured for this test
					&& (csetFactory.getBp().offGradePoolFilter == null 
						|| csetFactory.getBp().offGradePoolFilter.isEmpty())) 
					// have not already added off-grade items to the pool !!!
				{
					String filter = csetFactory.getBp().getOffGradeFilter(); // here filter changed after 23 responses!!!
					if (filter != null && !filter.isEmpty()) {
						_Ref<String> reason = new _Ref<>();
						String status = loader.addOffGradeItems(_connection, itemCandidates.getOppkey(), filter, null, reason); 
						// filter = designator = poolfilterProperty = ("OFFGRADE ABOVE"/"OFFGRADE BELOW"/null)
						if (!status.equalsIgnoreCase("success"))
							throw new ReturnStatusException(
									String.format("Attempt to include off-grade items: %s returned a status of:  %s, reason:  %s", filter, status, reason));
						if (reason.get().isEmpty()) {
							// the student's custom item pool has been updated
							// with off-grade items; reload history to include
							// the updated itempool
							csetFactory.LoadHistory(_connection);
						}
						System.out.println("status = " + status + "; reason = " + reason.get());
					}
				}
			} catch (Exception e) {
				System.out.println("Exception in the testOffgradeItemsOption test: "+ e);
				if (e instanceof java.lang.NullPointerException) {
					System.out.println("Stack: " + e.getStackTrace());
				}
				_logger.error(e.getMessage());
				throw e;
			} catch (AssertionError error) {
				System.out.println("AssertionError in the testOffgradeItemsOption test: " + error);
				_logger.error(error.getMessage());
				throw error;
			}

		} catch (Exception e) {
			System.out
					.println("Exception in the testOffgradeItemsOption test: "
							+ e);
			if (e instanceof java.lang.NullPointerException) {
				System.out.println("Stack: " + e.getStackTrace());
			}
			_logger.error(e.getMessage());
			throw e;
		} catch (AssertionError error) {
			System.out
					.println("AssertionError in the testOffgradeItemsOption test: "
							+ error);
			_logger.error(error.getMessage());
			throw error;
		}
	}
	//@Test //comment rollback
	// this is not test. It is testeeresponse table update for OffgradeItems test 10
	public void TestUpdateTesteeresponseDB() throws Exception {
		try{
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(
					"C:/temp/TEST10-OffgradeItems/Java_Responses25_93152d7f-8b04-442d-916b-eb9f5c5c43fd.csv"));
			String line = null;
			int insertedCnt;
			int counter = 0;
			String[] columnNames = null;
			String[] parts;
			StringBuilder stb = new StringBuilder();
			String OPPKEY = "0x93152d7f8b04442d916beb9f5c5c43fd"; 
			//UUID oppkey = (UUID.fromString(OPPKEY));

			//stb.append("groupID").append(FilePrint.csvDelimeter);
			String preffix = null;
			while ((line = reader.readLine()) != null) {
				if(counter ==  0)
				{
					columnNames = line.split(",");
					stb.append("insert into session.testeeresponse (").append(columnNames[0]);
					for(int i = 1; i < columnNames.length; i++)
					{
						stb.append(csvDelimeter).append(columnNames[i]);
					}
					stb.append(") ");
					preffix = stb.toString();
				    counter++;
				}
				else
				{
					parts = line.split(",");
					
					stb = new StringBuilder().append(preffix);
					stb.append(" values(").append(OPPKEY);
					
					for(int i = 1; i < columnNames.length; i++)
					{
						stb.append(csvDelimeter).append(upgradeColumnValue(columnNames[i], parts[i]));
					}
					
					stb.append(")");
					String query = stb.toString();
				    System.out.println(query);
	
				    SqlParametersMaps parameters = new SqlParametersMaps ();
				    insertedCnt =  _myDllHelper.executeStatement (_connection, query, parameters, true).getUpdateCount();
				    	
				    System.out.println("insertedCnt = " + insertedCnt);

				    counter++;
				}
			}
			counter--;
			System.out.println("Number of insert records = " + counter);
		
		} catch(Exception e)
		{
			
		}
	}

	private String upgradeColumnValue(String columnName, String value) {
		if(value.equalsIgnoreCase("NULL"))
			return value;
		else if(columnName.equalsIgnoreCase("answer")
				|| columnName.equalsIgnoreCase("format")
				|| columnName.equalsIgnoreCase("response")
				|| columnName.equalsIgnoreCase("hostname")
				|| columnName.equalsIgnoreCase("groupid")
				|| columnName.equalsIgnoreCase("scorestatus")
				|| columnName.equalsIgnoreCase("scoreRationale")
				|| columnName.equalsIgnoreCase("_efk_ItemKey")
				|| columnName.equalsIgnoreCase("contentLevel")
				|| columnName.equalsIgnoreCase("segmentID")
				|| columnName.equalsIgnoreCase("satellite")
				
				)
		{
			return "\'" + value + "\'";
		}
		else if(columnName.equalsIgnoreCase("DateGenerated")
				|| columnName.equalsIgnoreCase("DateSubmitted")
				|| columnName.equalsIgnoreCase("DateFirstResponse")
				|| columnName.equalsIgnoreCase("dateSystemAltered")
				|| columnName.equalsIgnoreCase("dateInactivated")
				|| columnName.equalsIgnoreCase("scoringDate")
				|| columnName.equalsIgnoreCase("scoredDate")
				|| columnName.equalsIgnoreCase("dateLastVisited")
				)
		{
			return "STR_TO_DATE(\'" + value + "\', \'%c/%e/%Y %r\')";			
		}
		else if(columnName.equalsIgnoreCase("scoreDimensions"))
		{
			value = value.substring(1, value.length() - 1);
			String repvalue = value.replace("\"\"", "\"");
			return "\'" + repvalue + "\'";
		}
		else
			return value;
	}

	//@Test
	// this is not test. it is temporary file to update tblsetofadmititems.clstring column on my MySQL
	public void TestUpdateDB() throws Exception {
		try {

			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(
					"C:/Users/akulakov/Documents/AA_Work_DOCUMENTS/AA__AA2-JAVAvsNET/tblSetofAdminItems-clString.txt"));
			String line = null;
			int insertedCnt;
			int counter = 0;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\\s");
				
				if(parts[0] == null)
				{
					System.out.println("Item has _fk_item = null and clstring = " + parts[1] );
					break;
				}
				
				String SQL_QUERY = "update  ${ItemBankDB}.tblsetofadminitems "
						+ " set clstring = ${clstring} "
						+ " where _fk_item = ${item} and _fk_adminsubject = '(SBAC)CAT-M3-ONON-S1-A1-MATH-3-Fall-2013-2014'";

			    SqlParametersMaps parameters = new SqlParametersMaps ().put ("clstring", parts[1]).put ("item", parts[0]);
			    String query = _myDllHelper.fixDataBaseNames (SQL_QUERY);
			    insertedCnt =  _myDllHelper.executeStatement (_connection, query, parameters, true).getUpdateCount ();
			    if(insertedCnt != 1)
			    {
			    	System.out.println("insertedCnt = " + insertedCnt);
			    }
			    counter++;
			}
			System.out.println("Number of records = " + counter);
		} catch (Exception e) {
		}

	}
	
	//@Test
	public void Test_textTypeField() throws SQLException, ReturnStatusException {
		try {
	
			SingleDataResultSet result;
			// String OPPKEY = "a1674ef0-9042-428e-beab-9f082bdc93f8";
			// UUID oppkey = (UUID.fromString(OPPKEY));
			// _logger.info("Oppkey =  " + OPPKEY);
			String clstring = "";
			String expectedCLString = "MG3_Test1_S1_Claim1_DOK1;SBAC-1;SBAC-1|P;SBAC-1|P|TS03;SBAC-1|P|TS03|A-3";
	
			String SQL_QUERY = "select clstring from ${ItemBankDB}.tblsetofadminitems where "
					+ " _fk_item = '200-1004' and "
					+ "_fk_adminsubject = '(SBAC)CAT-M3-ONON-S1-A1-MATH-3-Fall-2013-2014'";
	
			SqlParametersMaps parameters = new SqlParametersMaps();
			String query = _myDllHelper.fixDataBaseNames(SQL_QUERY);
	
			result = _myDllHelper
					.executeStatement(_connection, query, parameters, false)
					.getResultSets().next();
			DbResultRecord record = result.getCount() > 0 ? result.getRecords()
					.next() : null;
			if (record != null) {
				clstring = record.<String> get("clstring");
			}
			assertTrue(expectedCLString.equals(clstring));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
