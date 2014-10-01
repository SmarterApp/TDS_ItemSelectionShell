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
import static org.junit.Assert.fail;

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
import org.springframework.test.context.ContextConfiguration;

import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.algorithms.DLLHelper;
import tds.itemselection.api.IAIROnline;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;

@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class TestAIROnline2013 {

	@Autowired
	private IAIROnline _aironline;

	@Autowired
	private DLLHelper _myDllHelper = null;

	private static final Logger _logger = LoggerFactory.getLogger(TestAIROnline2013.class);
	private SQLConnection _connection = null;
	private Boolean _preexistingAutoCommitMode = null;
	private boolean _debug = true;

	@Before
	public void setUp() throws Exception {
		try {
			_connection = _myDllHelper.getSQLConnection();
			_preexistingAutoCommitMode = _connection.getAutoCommit();
			_connection.setAutoCommit(false);
		} catch (Exception e) {
			System.out.println("Exception in this test: " + e);
			_logger.error(e.getMessage());
			throw e;
		}
	}

	@After
	public void tearDown() throws Exception {
		try {
			_connection.rollback();
			_connection.setAutoCommit(_preexistingAutoCommitMode);
			_logger.info("All tranzactions are rollbacked");

		} catch (Exception e) {
			_connection.rollback();
			_connection.setAutoCommit(_preexistingAutoCommitMode);
			_logger.info("All tranzactions are rollbacked");
			_logger.info("Exception in the testGetNextItemGroup test: " + e);
			_logger.error(e.getMessage());
			throw e;
		}
	}

	// @Test
	// public void testCreateNextItemGroup () throws SQLException,
	// ReturnStatusException, Exception {

	@Test
	public final void testGetNextItemGroup() throws Exception {
		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey, _Ref<String> errorRef) "
				+ "for AdaptiveSelector2013: ");

		try {
			String OPPKEY = "0514d9cb-1e14-4c04-ab75-5e143245861a"; // main test
			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);

	    	_Ref<String> errorRef = new _Ref<>();
	    	
			ItemGroup itemGr = _aironline.getNextItemGroup(_connection, oppkey, errorRef);
			
	        if(errorRef.get() != null  && !errorRef.get().isEmpty())
	        {
	        	 _logger.error (errorRef.get());
	        	 System.out.println(String.format(errorRef.get()));
	        	 throw new ReturnStatusException (errorRef.get());
	        }
	        
			if (itemGr != null) {
				// TODO delete System.out.println() !!!
				System.out.println(String.format("groupID: %s", itemGr.groupID));
				System.out.println(String.format("itemsRequired: %s", itemGr.getNumRequired()));
				System.out.println(String.format("maxReqItems: %s", itemGr.getMaxItems()));
				_logger.info(String.format("groupID: %s", itemGr.groupID));
				_logger.info(String.format("itemsRequired: %s", itemGr.getNumRequired()));
				_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));

				List<TestItem> items = itemGr.getItems();
				int itemsNumber = items.size();
				System.out.println(String.format("Number of items: %s",
						itemsNumber));
				_logger.info(String.format("Number of items: %s", itemsNumber));

			} else {
				_logger.info("Selected Item Group is NULL");
				System.out.println("Selected Item Group is NULL.");
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

	@Test
	public final void testGetNextItemGroup_2() throws Exception {
		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey, _Ref<String> errorRef) "
				+ "for AdaptiveSelector2013: ");
		System.out.println();

		try {
			String OPPKEY = "a1674ef0-9042-428e-beab-9f082bdc93f8"; // This is student with 3 previous items!

			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);

	    	_Ref<String> errorRef = new _Ref<>();
	    	
			ItemGroup itemGr = _aironline.getNextItemGroup(_connection, oppkey, errorRef);
			
	        if(errorRef.get() != null  && !errorRef.get().isEmpty())
	        {
	        	 _logger.error (errorRef.get());
	        	 System.out.println(String.format(errorRef.get()));
	        	 throw new ReturnStatusException (errorRef.get());
	        }
	        
			if (itemGr != null) {
				// TODO delete System.out.println() !!!
				System.out.println(String.format("groupID: %s", itemGr.groupID));
				System.out.println(String.format("itemsRequired: %s", itemGr.getNumRequired()));
				System.out.println(String.format("maxReqItems: %s", itemGr.getMaxItems()));
				_logger.info(String.format("groupID: %s", itemGr.groupID));
				_logger.info(String.format("itemsRequired: %s", itemGr.getNumRequired()));
				_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));

				List<TestItem> items = itemGr.getItems();
				int itemsNumber = items.size();
				System.out.println(String.format("Number of items: %s",
						itemsNumber));
				_logger.info(String.format("Number of items: %s", itemsNumber));

			} else {
				_logger.info("Selected Item Group is NULL");
				System.out.println("Selected Item Group is NULL.");
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
			System.out.println("AssertionError in the testGetNextItemGroup test: " + error);
			_logger.error(error.getMessage());
			throw error;
		}
	}
	@Test
	public final void testGetNextItemGroup_3() throws Exception {
		System.out.println();
		_logger.info("Test of getNextItemGroup (Connection connection, UUID oppkey, _Ref<String> errorRef) "
				+ "for AdaptiveSelector2013: ");

		try {
			String OPPKEY = "24f000c7-a32f-439b-a55b-9a6e74af0649";
			// return error
			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);

	    	_Ref<String> errorRef = new _Ref<>();
	    	
			ItemGroup itemGr = _aironline.getNextItemGroup(_connection, oppkey, errorRef);
			
	        if(errorRef.get() != null  && !errorRef.get().isEmpty())
	        {
	        	 _logger.error (errorRef.get());
	     		 System.out.println();
	        	 System.out.println(String.format("ErrorRef: %s", errorRef.get()));
	        	 assertTrue("ErrorRef: Test Completed: algorithm =  SATISFIED", errorRef.get().equalsIgnoreCase("Test Complete"));
	        }
	        
			if (itemGr != null) {
				// TODO delete System.out.println() !!!
				System.out.println(String.format("groupID: %s", itemGr.groupID));
				System.out.println(String.format("itemsRequired: %s", itemGr.getNumRequired()));
				System.out.println(String.format("maxReqItems: %s", itemGr.getMaxItems()));
				_logger.info(String.format("groupID: %s", itemGr.groupID));
				_logger.info(String.format("itemsRequired: %s", itemGr.getNumRequired()));
				_logger.info(String.format("maxReqItems: %s", itemGr.getMaxItems()));

				List<TestItem> items = itemGr.getItems();
				int itemsNumber = items.size();
				System.out.println(String.format("Number of items: %s",
						itemsNumber));
				_logger.info(String.format("Number of items: %s", itemsNumber));

			} else {
				_logger.info("Selected Item Group is NULL");
				System.out.println("Selected Item Group is NULL.");
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
