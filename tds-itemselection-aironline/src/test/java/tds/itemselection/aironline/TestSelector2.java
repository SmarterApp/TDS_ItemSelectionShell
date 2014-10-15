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

import tds.itemselection.DLLHelper;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.loader.IItemSelectionDBLoader;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class TestSelector2 {
	@Autowired
	@Qualifier("aa2Selector")
	private IItemSelection itemSelector = null;

	@Autowired
	@Qualifier("aa2DBLoader")
	private IItemSelectionDBLoader loader = null;

	@Autowired
	private DLLHelper _myDllHelper = null;

	private static final Logger _logger = LoggerFactory.getLogger(TestSelector2.class);
	private SQLConnection 		_connection 				= null;
	private Boolean 			_preexistingAutoCommitMode 	= null;
	private boolean 			_debug 						= true;

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

	@Test
	public final void testGetNextItemGroup() throws Exception {
		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey) for AdaptiveSelector2013: ");
		System.out.println();

		try {
			String OPPKEY = "a1674ef0-9042-428e-beab-9f082bdc93f8";
			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);

			ItemGroup itemGr;
			ItemCandidatesData itemCandidates = null;

			itemCandidates = loader.getItemCandidates(_connection, oppkey);
			if (_debug) {
				itemCandidates.dumpDebugItemCandidatesData();
				//itemCandidates.dumpItemCandidatesData ();
			}

			if (!itemCandidates.getIsSimulation())
				itemCandidates.setSession(null);

			itemGr = itemSelector.getNextItemGroup(_connection, itemCandidates);
			// TODO itemGr.dump();

			if(itemGr != null)
			{
				_logger.info(String.format("groupID: %s", itemGr.groupID));
				_logger.info(String.format("itemsRequired: %s",
						itemGr.getNumRequired()));
				_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));
				System.out.println();
	
				List<TestItem> items = itemGr.getItems();
				int itemsNumber = items.size();
				_logger.info(String.format("Number of items: %s", itemsNumber));
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
