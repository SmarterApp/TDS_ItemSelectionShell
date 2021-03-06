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

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import tds.dll.api.ICommonDLL;
import tds.itemselection.DLLHelper;
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
import AIR.Common.Sql.AbstractDateUtilDll;

/**
 * @author akulakov
 * 
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
@Ignore("Tests do not have the proper configuraiton files included")
public class TestFixedFormSelector
{

	@Autowired
	ICommonDLL iCommonDll = null;

	@Autowired
	private AbstractDateUtilDll dateUtil = null;

	@Autowired
	@Qualifier("ffSelector")
	private IItemSelection itemSelector 				= null;

	@Autowired
	@Qualifier("itemDBLoader")
	private IItemSelectionDBLoader loader 				= null;

	@Autowired
	private DLLHelper 		myDllHelper 				= null;

	private SQLConnection 	_connection 				= null;
	private boolean 		_preexistingAutoCommitMode 	= true;
	private UUID 			oppkey 						= null;
	private boolean 		_debug 						= true;

	private static final Logger _logger = LoggerFactory.getLogger(TestFixedFormSelector.class);

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

    try {
    	_connection = myDllHelper.getSQLConnection();
      _preexistingAutoCommitMode = _connection.getAutoCommit ();
      _connection.setAutoCommit (false);
    } catch (Exception e) {
      _logger.error ("Exception: " + e.getMessage () + "; " + e.toString ());
      throw e;
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown () throws Exception {
    try {
      _connection.rollback ();
      _connection.setAutoCommit (_preexistingAutoCommitMode);
    } catch (Exception e) {
      _logger.error (String.format ("Failed rollback: %s", e.getMessage ()));
      throw e;
    } finally {
      _connection.setAutoCommit (_preexistingAutoCommitMode);
    }
  }

  /**
   * Test method for
   * {@link tds.itemselection.algorithms.FixedFormSelector#getNextItemGroup(java.sql.Connection, java.util.UUID)}
   * .
   * 
   * @throws Exception
   */
  @Test
  public final void testGetNextItemGroup () throws Exception {
	  // clientname = 'Minnesota'
	    oppkey = (UUID.fromString ("13D734F8-A604-47AF-BF0C-55D08E7839FA"));
	    
	   // clientname = 'SBAC'
	   // oppkey = (UUID.fromString ("cfa03bf1-cab6-487d-88d1-279a83e554b5"));

    System.out.println ();
    _logger.info ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    if(_debug)
    {
        System.out.println ();    	
        System.out.println ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    }

    ItemCandidatesData itemCandidates = null;
    
    try {

      itemCandidates = loader.getItemCandidates (_connection, oppkey);
      if(_debug)
      {
        itemCandidates.dumpDebugItemCandidatesData ();
      }
      
      if (!itemCandidates.getIsSimulation())
    	  itemCandidates.setSession(null);	
      ItemGroup itemGr = itemSelector.getNextItemGroup (_connection, itemCandidates);
      checkItemGroup (itemGr);

    } catch (SQLException exp) {
      System.out.println ("SQLException occured in this test: " + exp);
      _logger.error (exp.getMessage ());
      throw exp;
    } catch (Exception e)
    {
      System.out.println ("Exception in this test: " + e);
      _logger.error (e.getMessage ());
      throw e;
    } catch (AssertionError error)
    {
      System.out.println ("AssertionError in this test: " + error);
      _logger.error (error.getMessage ());
      throw error;
    }

  }

  //@Test
  public final void testGetNextItemGroup2 () throws Exception {
	  // clientname = 'oregon'
     oppkey = (UUID.fromString ("033227BD-259A-418A-9AE4-9AB82C0B8996"));

    System.out.println ();
    _logger.info ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    if(_debug)
    {
        System.out.println ();    	
        System.out.println ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    }

    ItemCandidatesData itemCandidates = null;
    
    try {

      itemCandidates = loader.getItemCandidates (_connection, oppkey);
      if(_debug)
      {
        itemCandidates.dumpDebugItemCandidatesData ();
      }
      
      if (!itemCandidates.getIsSimulation())
    	  itemCandidates.setSession(null);	
      ItemGroup itemGr = itemSelector.getNextItemGroup (_connection, itemCandidates);

      checkItemGroup (itemGr);

    } catch (SQLException exp) {
      System.out.println ("SQLException occured in this test: " + exp);
      _logger.error (exp.getMessage ());
      throw exp;
    } catch (Exception e)
    {
      System.out.println ("Exception in this test: " + e);
      _logger.error (e.getMessage ());
      throw e;
    } catch (AssertionError error)
    {
      System.out.println ("AssertionError in this test: " + error);
      _logger.error (error.getMessage ());
      throw error;
    }
  }

  //@Test
  public final void test_Global_GetNextItemGroup () throws Exception {
	   System.out.println ();
	    _logger.info ("Global Test of getNextItemGroup (Connection connection, UUID oppkey): ");
	    System.out.println ();
	    Set<UUID> oppkeys = new HashSet<UUID> ();
	    int rowNumber = 0;
	    int count = 0;
	    try
	    {
	    	String query = "SELECT  _key as oppkey "
	    			+ " FROM tdscore_test_session.testopportunity  "
	    			+ " where algorithm = 'fixedform' and clientname = 'Minnesota'";
	    	
			SingleDataResultSet res;
			DbResultRecord record;
			SqlParametersMaps parameters = new SqlParametersMaps();

			res = myDllHelper
					.executeStatement(_connection, query, parameters, false)
					.getResultSets().next();
			if (res != null) {
				rowNumber = res.getCount();
				if(_debug)
				{
					System.out.println("Number of row = " + rowNumber);
				}
				Iterator<DbResultRecord> recItr = res.getRecords();
				while (recItr.hasNext()) {
					record = recItr.next();
					if (record != null) {
						oppkey = record.<UUID> get("oppkey");
						try{							
						    ItemCandidatesData itemCandidates = null;

						      itemCandidates = loader.getItemCandidates (_connection, oppkey);
						      if(_debug)
						      {
						        itemCandidates.dumpDebugItemCandidatesData ();
						      }
						      
						      if (!itemCandidates.getIsSimulation())
						    	  itemCandidates.setSession(null);	
						      ItemGroup itemGr = itemSelector.getNextItemGroup (_connection, itemCandidates);

						    checkItemGroup (itemGr);
						    oppkeys.add(oppkey);
						    count++;
							if(_debug)
							{
								System.out.println(count  + " is good row;");
							}					    
						} catch(Exception e)
						{
							// to do nothing
							_logger.error(e.getMessage());
							if(_debug)
							{
								System.out.println(" Oppkey = " + oppkey + " has status SATISFIED?");
								count++;
								System.out.println(count  + " is bad row;");
							}							
						}						
					}
				}
			}
			if(!oppkeys.isEmpty())
			{
				for(UUID oppkey: oppkeys)
				{
					if(_debug)
					{
						System.out.println(" Oppkey = " + oppkey + " has status fixedform");
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
  //@Test
  public final void testGetNextItemGroup3 () throws Exception {
 
    System.out.println ();
    _logger.info ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    System.out.println ();
    Set<UUID> oppkeys = new HashSet<UUID> ();
    int count = 0;
    try
    {

      oppkeys.add (UUID.fromString ("000297C1-0D4F-4578-835A-970A3943195F"));
      oppkeys.add (UUID.fromString ("00050C4C-8BA4-48A0-B12D-211ECADFE6AF"));
      oppkeys.add (UUID.fromString ("00057660-FC35-45A8-B9C9-D3E88CC4734A"));
      oppkeys.add (UUID.fromString ("00061ABD-0A07-41F5-8689-A9593F475436"));
    } catch (Exception e)
    {
      System.out.println ("Exception in this test: " + e);
      _logger.error (e.getMessage ());
    }

    for (UUID oppkey : oppkeys)
    {
      count++;
      ItemCandidatesData itemCandidates = null;
      
      try {

        itemCandidates = loader.getItemCandidates (_connection, oppkey);
        if(_debug)
        {
          itemCandidates.dumpDebugItemCandidatesData ();
        }
        
        if (!itemCandidates.getIsSimulation())
      	  itemCandidates.setSession(null);	
        ItemGroup itemGr = itemSelector.getNextItemGroup (_connection, itemCandidates);
        checkItemGroup (itemGr);

      } catch (SQLException exp) {
        System.out.println ("SQLException occured in this test: " + exp);
        _logger.error (exp.getMessage ());
      } catch (Exception e)
      {
        System.out.println ("Exception in this test: " + e);
        _logger.error (e.getMessage ());
      } catch (AssertionError error)
      {
        System.out.println ("AssertionError in this test: " + error);
        _logger.error (error.getMessage ());
      }
    }
    _logger.info ("Count = " + count);
  }
  //@Test
  public final void testGetNextItemGroup4 () throws Exception {
	  // clientname = 'Hawaii_PT'

    oppkey = (UUID.fromString ("86BCF7F0-655D-48FB-BD05-0A8138A61C46"));

    System.out.println ();
    _logger.info ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    if(_debug)
    {
        System.out.println ();    	
        System.out.println ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    }

    ItemCandidatesData itemCandidates = null;
    
    try {

      itemCandidates = loader.getItemCandidates (_connection, oppkey);
      if(_debug)
      {
        itemCandidates.dumpDebugItemCandidatesData ();
      }
      
      if (!itemCandidates.getIsSimulation())
    	  itemCandidates.setSession(null);	
      ItemGroup itemGr = itemSelector.getNextItemGroup (_connection, itemCandidates);
      checkItemGroup (itemGr);

    } catch (SQLException exp) {
      System.out.println ("SQLException occured in this test: " + exp);
      _logger.error (exp.getMessage ());
      throw exp;
    } catch (Exception e)
    {
      System.out.println ("Exception in this test: " + e);
      _logger.error (e.getMessage ());
      throw e;
    } catch (AssertionError error)
    {
      System.out.println ("AssertionError in this test: " + error);
      _logger.error (error.getMessage ());
      throw error;
    }
  }
  
  //@Test
  public final void testGetNextItemGroup5 () throws Exception {
	  // clientname = 'Delaware'

    oppkey = (UUID.fromString ("51BCBEEA-E7C8-4886-AB6D-49990908E5A4"));

    System.out.println ();
    _logger.info ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    if(_debug)
    {
        System.out.println ();    	
        System.out.println ("Test of getNextItemGroup (Connection connection, UUID oppkey): ");
    }

    ItemCandidatesData itemCandidates = null;
    
    try {

      itemCandidates = loader.getItemCandidates (_connection, oppkey);
      if(_debug)
      {
        itemCandidates.dumpDebugItemCandidatesData ();
      }
      
      if (!itemCandidates.getIsSimulation())
    	  itemCandidates.setSession(null);	
      ItemGroup itemGr = itemSelector.getNextItemGroup (_connection, itemCandidates);
      checkItemGroup (itemGr);

    } catch (SQLException exp) {
      System.out.println ("SQLException occured in this test: " + exp);
      _logger.error (exp.getMessage ());
      throw exp;
    } catch (Exception e)
    {
      System.out.println ("Exception in this test: " + e);
      _logger.error (e.getMessage ());
      throw e;
    } catch (AssertionError error)
    {
      System.out.println ("AssertionError in this test: " + error);
      _logger.error (error.getMessage ());
      throw error;
    }

  }
  private void checkItemGroup (ItemGroup itemGr) throws Exception
  {
    try
    {
      _logger.info (String.format ("groupID: %s", itemGr.groupID));
      _logger.info (String.format ("itemsRequired: %s", itemGr.getNumberOfItemsRequired ()));
      _logger.info (String.format ("maxReqItems: %s", itemGr.getMaxItems ()));
      System.out.println ();

      List<TestItem> items = itemGr.getItems ();
      int itemsNumber = items.size ();
      _logger.info (String.format ("Number of items: %s", itemsNumber));
      for (TestItem item : items)
      {
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
        _logger.info (String.format ("groupID: %s", item.groupID));
        _logger.info (String.format ("itemID: %s", itemID));
        _logger.info (String.format ("strand: %s", strand));
        _logger.info (String.format ("IRT Model: %s", IRT_Model));
        _logger.info (String.format ("bVector: %s", bVector));
        _logger.info (String.format ("formPosition: %d", formPosition));
        _logger.info (String.format ("IRT_b: %s", irtB));
        _logger.info (String.format ("IRT_a: %f", irtA));
        _logger.info (String.format ("IRT_c: %f", irtC));
        _logger.info (String.format ("isActive: %b", isActive));
        _logger.info (String.format ("isFieldTest: %b", isFieldTest));
        _logger.info (String.format ("isRequired: %b", isRequired));
        System.out.println ();
//        if(_debug)
//        {
//        System.out.println ();
//        System.out.println (String.format ("groupID: %s", item.groupID));
//        System.out.println (String.format ("itemID: %s", itemID));
//        System.out.println (String.format ("strand: %s", strand));
//        System.out.println (String.format ("IRT Model: %s", IRT_Model));
//        System.out.println (String.format ("bVector: %s", bVector));
//        System.out.println (String.format ("formPosition: %d", formPosition));
//        System.out.println (String.format ("IRT_b: %s", irtB));
//        System.out.println (String.format ("IRT_a: %f", irtA));
//        System.out.println (String.format ("IRT_c: %f", irtC));
//        System.out.println (String.format ("isActive: %b", isActive));
//        System.out.println (String.format ("isFieldTest: %b", isFieldTest));
//        System.out.println (String.format ("isRequired: %b", isRequired));
//        }
        //
      }
    } catch (Exception e)
    {
      System.out.println ("Exception in this test: " + e);
      _logger.error (e.getMessage ());
      throw e;
    } catch (AssertionError error)
    {
      System.out.println ("AssertionError in this test: " + error);
      _logger.error (error.getMessage ());
      throw error;
    }
  }
}

