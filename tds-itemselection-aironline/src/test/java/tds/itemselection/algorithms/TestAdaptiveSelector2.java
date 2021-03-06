/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

import tds.itemselection.DLLHelper;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.loader.IItemSelectionDBLoader;
import AIR.Common.DB.SQLConnection;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
@TestExecutionListeners (DependencyInjectionTestExecutionListener.class)
@Ignore("Tests do not have the proper configuraiton files included")
public class TestAdaptiveSelector2 {
	@Autowired
	@Qualifier("aa2Selector")
	private IItemSelection itemSelector = null;

	@Autowired
	@Qualifier("aa2DBLoader")
	private IItemSelectionDBLoader loader = null;

	@Autowired
	private DLLHelper _myDllHelper = null;

	private static final Logger _logger = LoggerFactory.getLogger(TestAdaptiveSelector2.class);
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
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
		System.out.println();

		try {
			String OPPKEY = "cfa03bf1-cab6-487d-88d1-279a83e554b5";
			UUID oppkey = (UUID.fromString(OPPKEY));

			ItemGroup itemGr;
			ItemCandidatesData itemCandidates = null;

			itemCandidates = loader.getItemCandidates(_connection, oppkey);
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
}
