/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import tds.itemselection.api.IItemSelection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.ISDBLoader;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;

/**
 * @author akulakov
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-context.xml")
public class TestFieldTestSelector {

	@Autowired
	@Qualifier("ftSelector")
	private IItemSelection itemSelector = null;

	@Autowired
	@Qualifier("itemDBLoader")
	private IItemSelectionDBLoader loader = null;

	@Autowired
	private DLLHelper myDllHelper = null;

	private Boolean _isSatisfied = false;

	private SqlParametersMaps parameters = new SqlParametersMaps();
	private  String OPPKEY = "cfa03bf1-cab6-487d-88d1-279a83e554b5";
	//private String OPPKEY = "51BCBEEA-E7C8-4886-AB6D-49990908E5A4";
	private UUID oppkey = (UUID.fromString(this.OPPKEY));

	private SQLConnection _connection = null;
	private boolean _preexistingAutoCommitMode = true;

	private boolean _debug = true;

	private static final Logger _logger = LoggerFactory
			.getLogger(TestFieldTestSelector.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
			_connection = myDllHelper.getSQLConnection();
			_preexistingAutoCommitMode = _connection.getAutoCommit();
			_connection.setAutoCommit(false);
		} catch (Exception e) {
			_logger.error("Exception: " + e.getMessage() + "; " + e.toString());
			throw e;
		}

		SingleDataResultSet result;
		DbResultRecord record;

		try {
			String query = "select IsSatisfied from testopportunitysegment"
					+ " where _fk_TestOpportunity = ${oppkey} "
					+ " and SegmentPosition = 1";
			parameters.put("oppkey", oppkey);
			result = myDllHelper
					.executeStatement(_connection, query, parameters, false)
					.getResultSets().next();
			record = result.getCount() > 0 ? result.getRecords().next() : null;
			if (record != null) {
				_isSatisfied = record.<Boolean> get("IsSatisfied");
			}

			query = "update testeeresponse set segment = null where  _fk_TestOpportunity=${oppkey}";
			int updateCnt = myDllHelper.executeStatement(_connection, query,
					parameters, false).getUpdateCount();
			_logger.info(String
					.format("Number of updated segment = null in TesteeResponse table is %s",
							String.valueOf(updateCnt)));

		} catch (Exception e)
		{
			System.out.println("Exception in this test: " + e);
			_logger.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		try {
			_connection.rollback();
			_connection.setAutoCommit(_preexistingAutoCommitMode);
		} catch (Exception e) {
			_logger.error(String.format("Failed rollback: %s", e.getMessage()));
			throw e;
		} finally {
			_connection.setAutoCommit(_preexistingAutoCommitMode);
		}

		try {
			final String query = "update testopportunitysegment set IsSatisfied = ${IsSatisfied}"
					+ " where _fk_TestOpportunity = ${oppkey} "
					+ " and SegmentPosition = 1";
			parameters.put("IsSatisfied", _isSatisfied);

			int updateCnt = myDllHelper.executeStatement(_connection, query,
					parameters, false).getUpdateCount();

			_logger.info("Updated segment positions = " + updateCnt);

		} catch (Exception e) {
			_logger.info("Exception in this test: " + e);
			_logger.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testGetNextItemGroup() throws Exception {

		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
		System.out.println();

		ItemGroup itemGr;
		ItemCandidatesData itemCandidates = null;
		//IItemSelectionDBLoader loader = new ISDBLoader();
		loader.setConnection(_connection);

		try {

			itemCandidates = loader.getItemCandidates(oppkey);
			if (_debug) {
				itemCandidates.dumpDebugItemCandidatesData();
			}

			if (!itemCandidates.getIsSimulation())
				itemCandidates.setSession(null);

			itemGr = itemSelector.getNextItemGroup(_connection, itemCandidates);

			System.out.println();
			_logger.info(String.format("groupID: %s", itemGr.groupID));
			_logger.info(String.format("itemsRequired: %s",
					itemGr.getNumRequired()));
			_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));
			System.out.println();

			// assert (("G-159-67").equalsIgnoreCase (itemGr.groupID));
			assert (itemGr.getNumRequired() == -1);
			assert (itemGr.getMaxItems() == 0);

			List<TestItem> items = itemGr.getItems();
			int itemsNumber = items.size();
			_logger.info(String.format("Number of items: %s", itemsNumber));
			for (TestItem item : items) {
				String itemID = item.itemID;
				String strand = item.strandName;
				String IRT_Model = item.irtModel;
				double[] bVector = item.bVector;
				int formPosition = item.position;
				double irtA = item.a;
				double irtB = item.b;
				double irtC = item.c;
				Boolean isActive = item.isActive;
				Boolean isFieldTest = item.isFieldTest;
				Boolean isRequired = item.isRequired;
				//
				System.out.println();
				_logger.info(String.format("groupID: %s", item.groupID));
				_logger.info(String.format("itemID: %s", itemID));
				_logger.info(String.format("strand: %s", strand));
				_logger.info(String.format("IRT Model: %s", IRT_Model));
				_logger.info(String.format("bVector: %s", bVector));
				_logger.info(String.format("formPosition: %d", formPosition));
				_logger.info(String.format("IRT_b: %s", irtB));
				_logger.info(String.format("IRT_a: %f", irtA));
				_logger.info(String.format("IRT_c: %f", irtC));
				_logger.info(String.format("isActive: %b", isActive));
				_logger.info(String.format("isFieldTest: %b", isFieldTest));
				_logger.info(String.format("isRequired: %b", isRequired));
				System.out.println();
				//
			}
		} catch (Exception e) {
			System.out.println("Exception in this test: " + e);
			_logger.error(e.getMessage());
			throw e;
		} catch (AssertionError error) {
			System.out.println("AssertionError in this test: " + error);
			_logger.error(error.getMessage());
			throw error;
		}

	}

	//@Test	: there are not these oppkeys
	public final void test_Global_GetNextItemGroup() throws Exception {
		System.out.println();
		_logger.info("Global Test of getNextItemGroup (Connection connection, UUID oppkey): ");
		System.out.println();
		Set<UUID> oppkeys = new HashSet<UUID>();
		int rowNumber = 0;
		int count = 0;
		try {
			String query = "SELECT  _key as oppkey "
					+ " FROM tdscore_test_session.testopportunity  "
					+ " where algorithm = 'fieldtest' and clientname = 'oregon'";

			SingleDataResultSet res;
			DbResultRecord record;
			SqlParametersMaps parameters = new SqlParametersMaps();

			res = myDllHelper
					.executeStatement(_connection, query, parameters, false)
					.getResultSets().next();
			if (res != null) {
				rowNumber = res.getCount();
				if (_debug) {
					System.out.println("Number of row = " + rowNumber);
				}
				Iterator<DbResultRecord> recItr = res.getRecords();
				while (recItr.hasNext()) {
					record = recItr.next();
					if (record != null) {
						oppkey = record.<UUID> get("oppkey");
						try {
							ItemGroup itemGr;
							ItemCandidatesData itemCandidates = null;
							//IItemSelectionDBLoader loader = new ISDBLoader();
							loader.setConnection(_connection);

							itemCandidates = loader.getItemCandidates(oppkey);
							if (_debug) {
								itemCandidates.dumpDebugItemCandidatesData();
							}

							if (!itemCandidates.getIsSimulation())
								itemCandidates.setSession(null);

							itemGr = itemSelector.getNextItemGroup(_connection,
									itemCandidates);
							checkItemGroup(itemGr);
							oppkeys.add(oppkey);
							count++;
							if (_debug) {
								System.out.println(count + " is good row;");
							}
						} catch (Exception e) {
							// to do nothing
							_logger.error(e.getMessage());
							if (_debug) {
								System.out.println(" Oppkey = " + oppkey
										+ " has status SATISFIED?");
								count++;
								System.out.println(count + " is bad row;");
							}
						}
					}
				}
			}
			if (!oppkeys.isEmpty()) {
				for (UUID oppkey : oppkeys) {
					if (_debug) {
						System.out.println(" Oppkey = " + oppkey
								+ " has status fixedform");
					}
				}
			}

		} catch (Exception e) {
			System.out.println("Exception in this test: " + e);
			_logger.error(e.getMessage());
			if (_debug)
				System.out.println(e.getMessage());
		}

	}

	private void checkItemGroup(ItemGroup itemGr) throws Exception {
		try {
			System.out.println();
			_logger.info(String.format("groupID: %s", itemGr.groupID));
			_logger.info(String.format("itemsRequired: %s",
					itemGr.getNumRequired()));
			_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));
			System.out.println();

			// assert (("G-157-67").equalsIgnoreCase (itemGr.groupID));
			assert (itemGr.getNumRequired() == -1);
			assert (itemGr.getMaxItems() == 0);

			List<TestItem> items = itemGr.getItems();
			int itemsNumber = items.size();
			_logger.info(String.format("Number of items: %s", itemsNumber));
			for (TestItem item : items) {
				String itemID = item.itemID;
				String strand = item.strandName;
				String IRT_Model = item.irtModel;
				double[] bVector = item.bVector;
				int formPosition = item.position;
				double irtA = item.a;
				double irtB = item.b;
				double irtC = item.c;
				Boolean isActive = item.isActive;
				Boolean isFieldTest = item.isFieldTest;
				Boolean isRequired = item.isRequired;
				//
				System.out.println();
				_logger.info(String.format("groupID: %s", item.groupID));
				_logger.info(String.format("itemID: %s", itemID));
				_logger.info(String.format("strand: %s", strand));
				_logger.info(String.format("IRT Model: %s", IRT_Model));
				_logger.info(String.format("bVector: %s", bVector));
				_logger.info(String.format("formPosition: %d", formPosition));
				_logger.info(String.format("IRT_b: %s", irtB));
				_logger.info(String.format("IRT_a: %f", irtA));
				_logger.info(String.format("IRT_c: %f", irtC));
				_logger.info(String.format("isActive: %b", isActive));
				_logger.info(String.format("isFieldTest: %b", isFieldTest));
				_logger.info(String.format("isRequired: %b", isRequired));
				if (_debug) {
					System.out.println();
					System.out.println(String.format("groupID: %s",
							item.groupID));
					System.out.println(String.format("itemID: %s", itemID));
					System.out.println(String.format("strand: %s", strand));
					System.out.println(String
							.format("IRT Model: %s", IRT_Model));
					System.out.println(String.format("bVector: %s", bVector));
					System.out.println(String.format("formPosition: %d",
							formPosition));
					System.out.println(String.format("IRT_b: %s", irtB));
					System.out.println(String.format("IRT_a: %f", irtA));
					System.out.println(String.format("IRT_c: %f", irtC));
					System.out.println(String.format("isActive: %b", isActive));
					System.out.println(String.format("isFieldTest: %b",
							isFieldTest));
					System.out.println(String.format("isRequired: %b",
							isRequired));
				}
				//
			}
		} catch (Exception e) {
			System.out.println("Exception in this test: " + e);
			_logger.error(e.getMessage());
			throw e;
		} catch (AssertionError error) {
			System.out.println("AssertionError in this test: " + error);
			_logger.error(error.getMessage());
			throw error;
		}
	}

}

