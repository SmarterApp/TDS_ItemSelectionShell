/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

//import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

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
 * 
 */

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
@TestExecutionListeners (DependencyInjectionTestExecutionListener.class)
public class TestAdaptiveSelector
{
	@Autowired
	@Qualifier("aaSelector")
	private IItemSelection itemSelector = null;

	@Autowired
	@Qualifier("itemDBLoader")
	private IItemSelectionDBLoader loader = null;

	@Autowired
	private DLLHelper _myDllHelper = null;

	private SqlParametersMaps _parameters = new SqlParametersMaps();

	private Map<Integer, Integer> _positionToSegment = new HashMap<Integer, Integer>();

	private static final Logger _logger = LoggerFactory
			.getLogger(TestAdaptiveSelector.class);
	private SQLConnection _connection = null;
	private Boolean _preexistingAutoCommitMode = null;
	private boolean _debug = true;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp () throws Exception {
    try
    {
      _connection = _myDllHelper.getSQLConnection();
      _preexistingAutoCommitMode = _connection.getAutoCommit ();
      _connection.setAutoCommit (false);

//      String query = "select IsSatisfied from testopportunitysegment"
//          + " where _fk_TestOpportunity = ${oppkey} "
//          + " and SegmentPosition = 1";
//      _parameters.put ("oppkey", _oppkey);
//      result = _myDllHelper.executeStatement (_connection, query, _parameters, false).getResultSets ().next ();
//      record = result.getCount () > 0 ? result.getRecords ().next () : null;
//      if (record != null) {
//        record.<Boolean> get ("IsSatisfied");
//      }
//
//      query = "select segment, position from testeeresponse  where  _fk_TestOpportunity= ${oppkey}";
//      result = _myDllHelper.executeStatement (_connection, query, _parameters, false).getResultSets ().next ();
//      Iterator<DbResultRecord> records = result.getCount () > 0 ? result.getRecords () : null;
//      if (records != null)
//      {
//        while (records.hasNext ())
//        {
//          record = records.next ();
//          if (record != null) {
//            _positionToSegment.put (new Integer (record.<Integer> get ("position")), new Integer (record.<Integer> get ("segment")));
//          }
//        }
//      }
//
//      query = "update testeeresponse set segment = null where  _fk_TestOpportunity=${oppkey}";
//      int updateCnt = _myDllHelper.executeStatement (_connection, query, _parameters, false).getUpdateCount ();
//      _logger.info (String.format ("Number of updated segment = null in TesteeResponse table is %s", String.valueOf (updateCnt)));

    } catch (Exception e)

    {
      System.out.println ("Exception in this test: " + e);
      _logger.error (e.getMessage ());
      throw e;
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown () throws Exception {
  
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
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
		System.out.println();

		try {
			// private String OPPKEY = "51BCBEEA-E7C8-4886-AB6D-49990908E5A4";
			// private String OPPKEY = "8DAA4B41-0B74-4415-9A98-BCB45EAF2BCB";
			// private String OPPKEY = "04EFD6B8-2223-4565-B743-50B9424B5419";
			// private String OPPKEY = "0F77019C-2E51-4199-9C77-0A6844ACCDE7";
			
			//String OPPKEY = "51BCBEEA-E7C8-4886-AB6D-49990908E5A4";
			String OPPKEY = "cfa03bf1-cab6-487d-88d1-279a83e554b5";
			// '04EFD6B822234565B74350B9424B5419' '(Oregon_PT)OAKS-Math-6-Fall-2012-2013'
			// '8DAA4B410B7444159A98BCB45EAF2BCB' '(Minnesota)MCA III MG4O-S1-Mathematics-4-Fall-2012-2013' ;
			// '0F77019C2E5141999C770A6844ACCDE7' '(Minnesota_PT)MCA III Sampler-Science-9-Winter-2012-2013'
			// '51BCBEEAE7C84886AB6D49990908E5A4' '(Delaware)DCAS-Reading-10-Fall-2012-2013'
			UUID oppkey = (UUID.fromString(OPPKEY));

			ItemGroup itemGr;
			ItemCandidatesData itemCandidates = null;
			loader.setConnection(_connection);


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
				item.dump();
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
	public final void test_Global_GetNextItemGroup() throws Exception {
		 UUID  oppkey = null;
		System.out.println();
		_logger.info("Global Test of getNextItemGroup (Connection connection, UUID oppkey): ");
		if(_debug)
		{
			System.out.print("Global Test of getNextItemGroup (Connection connection, UUID oppkey): ");
		}
		System.out.println();
		Set<UUID> oppkeys = new HashSet<UUID>();
		int rowNumber = 0;
		int count = 0;
		try {
			String query = "SELECT  _key as oppkey "
					+ " FROM tdscore_test_session.testopportunity  "
					+ " where algorithm = 'adaptive' and clientname = 'SBAC'";

			SingleDataResultSet res;
			DbResultRecord record;
			SqlParametersMaps parameters = new SqlParametersMaps();

			res = _myDllHelper
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
						ItemGroup itemGr;
						ItemCandidatesData itemCandidates = null;
						IItemSelectionDBLoader loader = new ISDBLoader();
						loader.setConnection(_connection);

						try {

							itemCandidates = loader.getItemCandidates(oppkey);
							if (_debug) {
								itemCandidates.dumpDebugItemCandidatesData();
							}

							if (!itemCandidates.getIsSimulation())
								itemCandidates.setSession(null);

							itemGr = itemSelector.getNextItemGroup(_connection, itemCandidates);
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
				for (UUID oppk : oppkeys) {
					if (_debug) {
						System.out.println(" Oppkey = " + oppk
								+ " has status adatpive");
					}
				}
			} else if(_debug)
			{
				System.out.println(" Oppkeys  is empty!!");
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
