/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.aironline;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import tds.dll.api.IStudentDLL;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.loader.AA2DBLoader;
import tds.itemselection.loader.IItemSelectionDBLoader;
import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.Configuration.AppSettings;
import AIR.Common.DB.AbstractConnectionManager;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Data.ReturnStatus;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author akulakov
 * 
 */
@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class TestTMPAdaptiveService
{
  private static final Logger       _logger            = LoggerFactory.getLogger (TestTMPAdaptiveService.class);

  @Autowired
  private AbstractConnectionManager _connectionManager = null;

  @Autowired
  private IItemSelection            _iitemSelector     = null;

  @Autowired
  private IStudentDLL               _stDLL             = null;

  private SQLConnection             _connection        = null;
  private boolean                   _preexistingAutoCommitMode      = true;

  private UUID                      _oppkey            = null;
  private UUID                      _sessionKey        = null;
  private UUID                      _browserKey        = null;
  private boolean 					_debug			= true;	
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp () throws Exception {
    try{
    _connection = _connectionManager.getConnection ();
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
    try{
      _connection.rollback ();
      _connection.setAutoCommit (_preexistingAutoCommitMode);
      } catch (Exception e) {
        _logger.error (String.format ("Failed rollback: %s", e.getMessage ()));
        throw e;
      } finally {
        _connection.setAutoCommit (_preexistingAutoCommitMode);
      }
  }


  // Success Test Case
  @Test
  public void testCreateNextItemGroup () throws SQLException, ReturnStatusException, Exception {

    try {

      _oppkey = UUID.fromString ("13D734F8-A604-47AF-BF0C-55D08E7839FA");
      _sessionKey = UUID.fromString ("38893e8d-a5d2-4bf8-906e-3c2cbfbacc30");
      _browserKey = UUID.fromString ("3acfb69f-95ea-43ff-b16e-3b19f2abe904");
      
      OpportunityInstance oppInstance = new OpportunityInstance (_oppkey, _sessionKey, _browserKey);
      int lastPage = 0;
      int lastPosition = 0;

      PageGroup pageGroup = createNextItemGroup (oppInstance, lastPage, lastPosition);

      checkPageGroup (pageGroup);

    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }

  }

  //@Test
  public void testCreateNextItemGroup2 () throws SQLException, ReturnStatusException, Exception {

    try {	// 'D1DF8AF0D0694FABAE6E83D525272BEC'
    		// '88899080003E420EA3A84E9ECA6A0E71', 
    		// '3DDAED595D9F41EE923597F7634C4C6E'


      _oppkey = UUID.fromString ("B9514C7C-CAF5-420E-AA75-67459486CBD4");
      _sessionKey = UUID.fromString ("3025C2B8-9C48-41C0-9D61-7DA95A4EA2EC");
      _browserKey = UUID.fromString ("F55CC1C7-F6B4-4D20-8DA1-90BFD0D610E9");
      
      OpportunityInstance oppInstance = new OpportunityInstance (_oppkey, _sessionKey, _browserKey);
      int lastPage = 0;
      int lastPosition = 0;

      PageGroup pageGroup = createNextItemGroup (oppInstance, lastPage, lastPosition);

      checkPageGroup (pageGroup);

    } catch (ReturnStatusException exp) {
      ReturnStatus returnStatus = exp.getReturnStatus ();
      _logger.error ("Status: " + returnStatus.getStatus ());
      _logger.error ("Reason: " + returnStatus.getReason ());
      _logger.error (exp.getMessage ());
    } catch (Exception exp) {
      exp.printStackTrace ();
      _logger.error (exp.getMessage ());
    }

  }

  private void checkPageGroup (PageGroup pageGroup) throws SQLException, ReturnStatusException, Exception
  {

    System.out.println ();
    _logger.info (String.format ("groupID: %s", pageGroup.getGroupID ()));
    _logger.info (String.format ("itemsRequired: %s", pageGroup.getItemsRequired ()));
    _logger.info (String.format ("maxReqItems: %s", pageGroup.getNumber ()));
    if(_debug)
    {
	    System.out.println ();
	    System.out.println (String.format ("groupID: %s", pageGroup.getGroupID ()));
	    System.out.println (String.format ("itemsRequired: %s", pageGroup.getItemsRequired ()));
	    System.out.println (String.format ("maxReqItems: %s", pageGroup.getNumber ()));
    }

    assert (pageGroup.getItemsRequired () == -1);
    assert (pageGroup.getNumber () == 0);

    List<ItemResponse> items = pageGroup.getListOfItemResponse ();
    int itemsNumber = items.size ();
    _logger.info (String.format ("Number of items: %s", itemsNumber));
    if(_debug)
    {
	    System.out.println ();
	    System.out.println (String.format ("Number of items: %s", itemsNumber));
    }
    for (ItemResponse item : items)
    {
      String itemID = item.itemID;
      String strand = item.get_strand ();// .strand;
      String IRT_Model = item.getBaseItem ().irtModel;// .irtModel;
      double[] bVector = item.getBaseItem ().bVector;
      int formPosition = item.itemPosition;// .formPosition;
      double irtA = item.getBaseItem ().a;
      double irtB = item.getBaseItem ().b;
      double irtC = item.getBaseItem ().c;
      Boolean isActive = item.getBaseItem ().isActive;// .isActive;
      Boolean isFieldTest = item.getBaseItem ().isFieldTest ();
      Boolean isRequired = item.getBaseItem ().isRequired ();
      //
      System.out.println ();
      System.out.println ("As FixedFormTest");
      _logger.info (String.format ("groupID: %s", pageGroup.getGroupID ()));
      _logger.info (String.format ("itemID: %s", itemID));
      _logger.info (String.format ("strand: %s", strand));
      _logger.info (String.format ("IRT Model: %s", IRT_Model));
      _logger.info (String.format ("bVector: %s", bVector));
      _logger.info (String.format ("formPosition: %d", formPosition));
      _logger.info (String.format ("IRT_b: %s", irtB));
      _logger.info (String.format ("IRT_a: %f", irtA));
      _logger.info (String.format ("IRT_c: %f", irtC));
      _logger.info (String.format ("isSelected: %b", isActive));
      _logger.info (String.format ("isFieldTest: %b", isFieldTest));
      _logger.info (String.format ("isRequired: %b", isRequired));
      if(_debug)
      {
  	    System.out.println ();
        System.out.println ("As FixedFormTest");
        System.out.println (String.format ("groupID: %s", pageGroup.getGroupID ()));
        System.out.println (String.format ("itemID: %s", itemID));
        System.out.println (String.format ("strand: %s", strand));
        System.out.println (String.format ("IRT Model: %s", IRT_Model));
        System.out.println (String.format ("bVector: %s", bVector));
        System.out.println (String.format ("formPosition: %d", formPosition));
        System.out.println (String.format ("IRT_b: %s", irtB));
        System.out.println (String.format ("IRT_a: %f", irtA));
        System.out.println (String.format ("IRT_c: %f", irtC));
        System.out.println (String.format ("isSelected: %b", isActive));
        System.out.println (String.format ("isFieldTest: %b", isFieldTest));
        System.out.println (String.format ("isRequired: %b", isRequired));
      }
      //
    }
    System.out.println ("As AdaptiveServiseTest");
    Assert.assertTrue (pageGroup != null);
    if (pageGroup != null) {
      _logger.info ("File path::" + pageGroup.getFilePath ());
      _logger.info ("Group Id::" + pageGroup.getGroupID ());
      _logger.info ("ID::" + pageGroup.getId ());
      _logger.info ("Items Left Required::" + pageGroup.getItemsLeftRequired ());
      _logger.info ("Items REquired::" + pageGroup.getItemsRequired ());
      _logger.info ("Number::" + pageGroup.getNumber ());
      _logger.info ("Segment Id::" + pageGroup.getSegmentID ());
      _logger.info ("Segment Position::" + pageGroup.getSegmentPos ());

      _logger.info ("Completed::" + pageGroup.getIsCompleted ());
      _logger.info ("List of item response::" + pageGroup.getListOfItemResponse ());
      if(_debug)
      {
  	    System.out.println ();
        System.out.println ("File path::" + pageGroup.getFilePath ());
        System.out.println ("Group Id::" + pageGroup.getGroupID ());
        System.out.println ("ID::" + pageGroup.getId ());
        System.out.println ("Items Left Required::" + pageGroup.getItemsLeftRequired ());
        System.out.println ("Items REquired::" + pageGroup.getItemsRequired ());
        System.out.println ("Number::" + pageGroup.getNumber ());
        System.out.println ("Segment Id::" + pageGroup.getSegmentID ());
        System.out.println ("Segment Position::" + pageGroup.getSegmentPos ());

        System.out.println ("Completed::" + pageGroup.getIsCompleted ());
        System.out.println ("List of item response::" + pageGroup.getListOfItemResponse ());
      }
   }

  }

  private AdaptiveGroup createItemGroupAdaptive (ItemGroup itemGroup, int page)
  {
    AdaptiveGroup adaptiveGroup = new AdaptiveGroup ();
    adaptiveGroup.setPage (page);
    adaptiveGroup.setBankKey (itemGroup.getBankkey ());
    adaptiveGroup.setGroupID (itemGroup.getGroupID ());
    adaptiveGroup.setSegmentPosition (itemGroup.getSegmentPosition ());
    adaptiveGroup.setSegmentID (itemGroup.getSegmentID ());
    adaptiveGroup.setNumItemsRequired (itemGroup.getNumberOfItemsRequired ());
    return adaptiveGroup;
  }

  private AdaptiveItem createItemAdaptive (AdaptiveGroup adaptiveGroup, TestItem testItem, int position)
  {
    AdaptiveItem adaptiveItem = new AdaptiveItem ();
    adaptiveItem.setPage (adaptiveGroup.getPage ());
    adaptiveItem.setPosition (position);
    adaptiveItem.setGroupID (testItem.getGroupID ());
    adaptiveItem.setItemID (testItem.getItemID ());
    adaptiveItem.setSegment (adaptiveGroup.getSegmentPosition ());
    adaptiveItem.setSegmentID (adaptiveGroup.getSegmentID ());
    adaptiveItem.setIsRequired (testItem.isRequired ());
    adaptiveItem.setIsFieldTest (testItem.isFieldTest ());
    return adaptiveItem;
  }

  public PageGroup createNextItemGroup (OpportunityInstance oppInstance, int lastPage, int lastPosition)
      throws ReturnStatusException, SQLException
  {
    PageGroup pageGroup = null;

    try {
      // generate next item group
      ItemGroup itemGroup;
      ItemCandidatesData itemCandidates = null;
      IItemSelectionDBLoader loader = new AA2DBLoader();
      loader.setConnection (_connection);
      
      try {

        itemCandidates = loader.getItemCandidates (oppInstance.getKey ());
        if(_debug)
        {
          itemCandidates.dumpDebugItemCandidatesData ();
        }
        
        if (!itemCandidates.getIsSimulation())
      	  itemCandidates.setSession(null);	

        itemGroup = _iitemSelector.getNextItemGroup (_connection, itemCandidates );

      } catch (ItemSelectionException e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (String.format ("Adaptive error: %1$s", e.getMessage ()));
      }

      // create own adaptive group wrapper
      lastPage++;
      AdaptiveGroup adaptiveGroup = createItemGroupAdaptive (itemGroup, lastPage);

      for (TestItem testItem : itemGroup.getItems ())
      {
        lastPosition++;

        AdaptiveItem adaptiveItem = createItemAdaptive (adaptiveGroup, testItem, lastPosition);
        adaptiveGroup.getItems ().add (adaptiveItem);
      }
      try {
        pageGroup = insertItems (oppInstance, adaptiveGroup);
      } catch (Exception e) {
        _logger.error (e.getMessage ());
        throw new ReturnStatusException (e);
      }

    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return pageGroup;
  }

  // insert items we got from adaptive algorithm to the session db
  public PageGroup insertItems (OpportunityInstance oppInstance, AdaptiveGroup adaptiveGroup) throws ReturnStatusException {
    OpportunityItems sqlOppItems = null;
    if (adaptiveGroup == null)
      return null;
    try {
      List<AdaptiveItem> items = adaptiveGroup.getItems ();
      int insertCount = items.size (); // # of responses to insert
      // nothing to do, return
      if (insertCount == 0)
        return null;
      sqlOppItems = insertItems2 (oppInstance, adaptiveGroup);
      ReturnStatus returnedStatus = sqlOppItems.getReturnStatus ();
      // check if the return status is "inserted", otherwise it failed
      if (returnedStatus == null || !returnedStatus.getStatus ().equalsIgnoreCase ("inserted")) {
        throw new ReturnStatusException (returnedStatus);
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return PageGroup.Create (sqlOppItems.getItems ());
  }

  public OpportunityItems insertItems2 (OpportunityInstance oppInstance, AdaptiveGroup adaptiveGroup) throws ReturnStatusException {
    // create item keys delimited string
    OpportunityItems opportunityItems = new OpportunityItems ();
    List<OpportunityItem> opportunityItemList = new ArrayList<OpportunityItem> ();

    // String itemKeys = getItemKeys (adaptiveGroup.getItems ());
    String itemKeys = null;
    try  {

      MultiDataResultSet resultSets = _stDLL.T_InsertItems_SP (_connection, oppInstance.getKey (), oppInstance.getSessionKey (), oppInstance.getBrowserKey (), adaptiveGroup.getSegmentPosition (),
          adaptiveGroup.getSegmentID (), adaptiveGroup.getPage (), adaptiveGroup.getGroupID (), itemKeys, '|', new Integer (adaptiveGroup.getNumItemsRequired ()), new Float (0), new Integer (0),
          false);
      Iterator<SingleDataResultSet> results = resultSets.getResultSets ();
      // first expected result set

      SingleDataResultSet firstResultSet = results.next ();
      ReturnStatusException.getInstanceIfAvailable (firstResultSet);
      Iterator<DbResultRecord> records = firstResultSet.getRecords ();
      if (records.hasNext ()) {
        DbResultRecord record = records.next ();

        if (!record.hasColumn ("dateCreated"))
          return opportunityItems;
        // set datecreated for each item
        String dateCreated = record.<String> get ("dateCreated");
        while (records.hasNext ()) {
          OpportunityItem oppItem = new OpportunityItem ();
          String itemID = record.<String> get ("bankitemkey");
          oppItem.id = itemID;
          // get data from SP
          oppItem.setBankKey (record.<Integer> get ("bankkey"));
          oppItem.setItemKey (record.<Integer> get ("itemkey"));
          oppItem.setPage (record.<Integer> get ("page"));
          oppItem.setPosition (record.<Integer> get ("position"));
          oppItem.setFormat (record.<String> get ("format"));
          oppItem.setDateCreated (dateCreated);

          AdaptiveItem adaptiveItem = (AdaptiveItem) CollectionUtils.find (adaptiveGroup.getItems (), new Predicate ()
          {
            public boolean evaluate (Object object) {
              if (((AdaptiveItem) object).getItemID () != null)
                return true;
              return false;
            }
          });
          // find matching adaptive item
          // AdaptiveItem adaptiveItem = adaptiveGroup.getItems ().Find(ai =>
          // ai.ItemID == itemID);
          // check if item was found
          if (adaptiveItem == null) {
            String error = "T_InsertItems: The item key  %1$d was returned but was not found in [%2$s].";
            throw new ReturnStatusException (String.format (error, oppItem.getItemKey (), itemKeys));
          }
          // get data from adaptive algorithm
          oppItem.setGroupID (adaptiveGroup.getGroupID ());
          oppItem.setSegment (adaptiveGroup.getSegmentPosition ());
          oppItem.setSegmentID (adaptiveGroup.getSegmentID ());
          oppItem.setIsRequired (adaptiveItem.isRequired ());
          oppItem.setIsFieldTest (adaptiveItem.isFieldTest ());
          oppItem.setGroupItemsRequired (adaptiveGroup.getNumItemsRequired ());

          // manually set data
          oppItem.setIsVisible (true);
          oppItem.setIsSelected (false);
          oppItem.setIsValid (false);
          oppItem.setMarkForReview (false);
          oppItem.setSequence (0);
          oppItem.setStimulusFile (null);
          oppItem.setItemFile (null);

          // DEBUG: Check if items should all be marked as not required

          if (_logger.isDebugEnabled ()) {
            boolean itemsNeverRequired = AppSettings.getBoolean ("debug.itemsNeverRequired").getValue ();
            if (itemsNeverRequired) {
              oppItem.setIsRequired (false);
              oppItem.setGroupItemsRequired (0);
            }
          }

          opportunityItemList.add (oppItem);
        }
      }
      if (results.hasNext ()) {
        SingleDataResultSet secondResultSet = results.next ();
        records = secondResultSet.getRecords ();
        while (records.hasNext ()) {
          DbResultRecord record = records.next ();
          dumpRecord (record);
        }
      }
    } catch (ReturnStatusException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return opportunityItems;
  }

  private void dumpRecord (DbResultRecord record) throws ReturnStatusException {
    System.out.println ();
    String columnName = null;
    Iterator<String> itNames = record.getColumnNames ();
    while (itNames.hasNext ())
    {
      columnName = itNames.next ();
      Object value = record.get (record.getColumnToIndex (columnName).get ());
      _logger.info (String.format ("%s: %s", columnName, value.toString ()));
      if(_debug)
      {
    	  System.out.println (String.format ("%s: %s", columnName, value.toString ()));  
      }
    }
    System.out.println ();
  }

}

class OpportunityInstance
{
  private final UUID _oppKey;
  private final UUID _sessionKey;
  private final UUID _browserKey;

  @JsonProperty ("Key")
  public UUID getKey () {
    return _oppKey;
  }

  @JsonProperty ("SessionKey")
  public UUID getSessionKey () {
    return _sessionKey;
  }

  @JsonProperty ("BrowserKey")
  public UUID getBrowserKey () {
    return _browserKey;
  }

  public OpportunityInstance (UUID oppKey, UUID sessionKey, UUID browserKey) {
    _oppKey = oppKey;
    _sessionKey = sessionKey;
    _browserKey = browserKey;
  }

}

class ItemResponses extends ArrayList<ItemResponse>
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ItemResponses () {
    super ();
  }

  public List<ItemResponse> getListOfItemResponse () {
    return this;
  }

  public void setListOfItemResponse (List<ItemResponse> listOfItemResponse) {
    this.addAll (listOfItemResponse);
  }

}

class PageGroup extends ItemResponses
{
  private static final long serialVersionUID = 1L;
  private int               _number;
  private String            _groupID;
  private int               _segmentPos;
  private String            _segmentID;
  private String            _filePath;
  private int               _numRequired;
  public boolean            _printed;
  public String             _id;

  // /**
  // * @return
  // */
  // public List<ItemResponse> getListOfItemResponse () {
  // // TODO Auto-generated method stub
  // return null;
  // }

  public PageGroup (OpportunityItem responseData)
  {
    _number = responseData.getPage ();
    _groupID = responseData.getGroupID ();
    _segmentPos = responseData.getSegment ();
    _segmentID = responseData.getSegmentID ();
    _filePath = responseData.getStimulusFile ();
    _numRequired = responseData.getGroupItemsRequired ();
  }

  /**
   * @return the _number
   */
  @JsonProperty ("Number")
  public int getNumber () {
    return _number;
  }

  /**
   * @return the _groupID
   */
  @JsonProperty ("ID")
  public String getGroupID () {
    return _groupID;
  }

  /**
   * @return the _segmentPos
   */
  @JsonProperty ("SegmentPosition")
  public int getSegmentPos () {
    return _segmentPos;
  }

  /**
   * @return the _segmentID
   */
  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  /**
   * @return the _filePath
   */
  @JsonProperty ("FilePath")
  public String getFilePath () {
    return _filePath;
  }

  /**
   * @return the _printed
   */
  public boolean isPrinted () {
    return _printed;
  }

  /**
   * @param _printed
   *          the _printed to set
   */
  public void setPrinted (boolean _printed) {
    this._printed = _printed;
  }

  /**
   * @return the _id
   */
  public String getId () {
    return _id;
  }

  /**
   * @param _id
   *          the _id to set
   */
  public void setId (String _id) {
    this._id = _id;
  }

  public static PageGroup Create (List<OpportunityItem> oppItems)
  {
    PageGroup pageGroup = null;

    for (OpportunityItem oppItem : oppItems)
    {
      if (pageGroup == null)
      {
        pageGroup = new PageGroup (oppItem);
      }

      ItemResponse itemResponse = new ItemResponse (oppItem.getSegment (),
          oppItem.getGroupID (),
          oppItem.getGroupID (),
          oppItem.getPosition (),
          oppItem.getValue (),
          0.12345,
          oppItem.getScore (),
          false);
      /*
       * 
       * public ItemResponse (int segment, String ID, String groupID, int
       * position, String strand, double IRTb, int score, boolean isFieldTest) {
       * this.groupID = groupID; this.segment = segment; this.ID = ID;
       * this.position = position; this._b = IRTb; this._strand = strand;
       * this.score = score; this.isFT = isFieldTest; }
       */
      pageGroup.add (itemResponse);
    }

    return pageGroup;
  }

  // / <summary>
  // / First response in the group
  // / </summary>
  public ItemResponse getFirst ()
  {
    // TODO
    return this.get (0);
    // return this.FirstOrDefault();
  }

  // / <summary>
  // / Last response in the group
  // / </summary>
  public ItemResponse getLast ()
  {
    return getLast ();
    // return this.LastOrDefault();
  }

  // / <summary>
  // / The total number of items required to have responses.
  // / </summary>
  public int getItemsRequired ()
  {
    // adaptive algorithm will return -1 when all items are required
    int itemsRequired = _numRequired;

    if (itemsRequired == -1)
    {
      itemsRequired = this.size ();
    }
    else
    {
      // if the # of visible responses is less than the required then we need to
      // adjust the required
      itemsRequired = (this.size () < itemsRequired) ? this.size () : itemsRequired;
    }

    return itemsRequired;
  }

  // / <summary>
  // / The number of items left in the group that need responses to satisfy the
  // group or at the item level.
  // / </summary>
  public int getItemsLeftRequired ()
  {
    // total responses required to satisfy the group
    int groupRequired = getItemsRequired ();

    // total responses that are marked as being required
    int itemsRequired = 0;

    return (groupRequired > itemsRequired) ? groupRequired : itemsRequired;
  }

  // / <summary>
  // / Determines if the number of responses required for this group has been
  // met.
  // / </summary>
  @JsonProperty ("IsCompleted")
  public boolean getIsCompleted ()
  {
    return getItemsLeftRequired () == 0;
  }

  public boolean equals (PageGroup other) {
    /*
     * if (ReferenceEquals(null, other)) return false; if (ReferenceEquals(this,
     * other)) return true; return equals(other.getName(), Name);
     */
    if (other == null)
      return false;
    if (other == this)
      return true;
    return other.getGroupID () == _groupID;
  }

  @Override
  public boolean equals (Object obj) {

    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (!(obj instanceof PageGroup))
      return false;
    return equals ((PageGroup) obj);

  }

  @Override
  public int hashCode ()
  {
    return (_groupID != null ? _groupID.hashCode () : 0);
  }
}

class OpportunityItem
{
  public String   id;

  private long    _bankKey;
  private long    _itemKey;
  private int     _page;
  private int     _position;
  private int     _segment;
  private String  _segmentID;
  private String  _groupID;
  private int     _sequence;
  private String  _dateCreated;
  private String  _format;
  private String  _value;
  private boolean _markForReview;
  private int     _score;
  private boolean _isFieldTest;
  private boolean _isSelected;
  private boolean _isRequired;
  private boolean _isValid;

  private int     _groupItemsRequired;
  private String  _itemFile;
  private String  _stimulusFile;
  private boolean _isVisible;
  private boolean _isPrintable;

  @JsonProperty ("BankKey")
  public long getBankKey () {
    return _bankKey;
  }

  public void setBankKey (long bankKey) {
    _bankKey = bankKey;
  }

  @JsonProperty ("ItemKey")
  public long getItemKey () {
    return _itemKey;
  }

  public void setItemKey (long itemKey) {
    _itemKey = itemKey;
  }

  @JsonProperty ("Page")
  public int getPage () {
    return _page;
  }

  public void setPage (int page) {
    _page = page;
  }

  @JsonProperty ("Position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int position) {
    _position = position;
  }

  @JsonProperty ("Segment")
  public int getSegment () {
    return _segment;
  }

  public void setSegment (int segment) {
    _segment = segment;
  }

  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  public void setSegmentID (String segmentID) {
    _segmentID = segmentID;
  }

  @JsonProperty ("GroupID")
  public String getGroupID () {
    return _groupID;
  }

  public void setGroupID (String groupID) {
    _groupID = groupID;
  }

  @JsonProperty ("Sequence")
  public int getSequence () {
    return _sequence;
  }

  public void setSequence (int sequence) {
    _sequence = sequence;
  }

  @JsonProperty ("DateCreated")
  public String getDateCreated () {
    return _dateCreated;
  }

  public void setDateCreated (String dateCreated) {
    _dateCreated = dateCreated;
  }

  @JsonProperty ("Format")
  public String getFormat () {
    return _format;
  }

  public void setFormat (String format) {
    _format = format;
  }

  @JsonProperty ("Value")
  public String getValue () {
    return _value;
  }

  public void setValue (String value) {
    _value = value;
  }

  @JsonProperty ("MarkForReview")
  public boolean isMarkForReview () {
    return _markForReview;
  }

  public void setMarkForReview (boolean markForReview) {
    _markForReview = markForReview;
  }

  @JsonProperty ("Score")
  public int getScore () {
    return _score;
  }

  public void setScore (int score) {
    _score = score;
  }

  @JsonProperty ("IsFieldTest")
  public boolean isFieldTest () {
    return _isFieldTest;
  }

  public void setIsFieldTest (boolean isFieldTest) {
    _isFieldTest = isFieldTest;
  }

  @JsonProperty ("IsSelected")
  public boolean isSelected () {
    return _isSelected;
  }

  public void setIsSelected (boolean isSelected) {
    _isSelected = isSelected;
  }

  @JsonProperty ("IsRequired")
  public boolean isRequired () {
    return _isRequired;
  }

  public void setIsRequired (boolean isRequired) {
    _isRequired = isRequired;
  }

  @JsonProperty ("IsValid")
  public boolean isValid () {
    return _isValid;
  }

  public void setIsValid (boolean isValid) {
    _isValid = isValid;
  }

  @JsonProperty ("GroupItemsRequired")
  public int getGroupItemsRequired () {
    return _groupItemsRequired;
  }

  public void setGroupItemsRequired (int groupItemsRequired) {
    _groupItemsRequired = groupItemsRequired;
  }

  @JsonProperty ("ItemFile")
  public String getItemFile () {
    return _itemFile;
  }

  public void setItemFile (String itemFile) {
    _itemFile = itemFile;
  }

  @JsonProperty ("StimulusFile")
  public String getStimulusFile () {
    return _stimulusFile;
  }

  public void setStimulusFile (String stimulusFile) {
    _stimulusFile = stimulusFile;
  }

  @JsonProperty ("IsVisible")
  public boolean isVisible () {
    return _isVisible;
  }

  public void setIsVisible (boolean isVisible) {
    _isVisible = isVisible;
  }

  @JsonProperty ("IsPrintable")
  public boolean isPrintable () {
    return _isPrintable;
  }

  public void setIsPrintable (boolean isPrintable) {
    _isPrintable = isPrintable;
  }
}

class OpportunityItems
{
  private List<OpportunityItem> _items        = new ArrayList<OpportunityItem> ();
  private ReturnStatus          _returnStatus = null;

  public OpportunityItems ()
  {

  }

  @JsonProperty ("Items")
  public List<OpportunityItem> getItems () {
    return _items;
  }

  public void setItems (List<OpportunityItem> _items) {
    this._items = _items;
  }

  /**
   * @return the _returnStatus
   */
  public ReturnStatus getReturnStatus () {
    return _returnStatus;
  }

  /**
   * @param _returnStatus
   *          the _returnStatus to set
   */
  public void setReturnStatus (ReturnStatus _returnStatus) {
    this._returnStatus = _returnStatus;
  }

}

class AdaptiveItem
{
  private String  _itemID;
  private String  _groupID;
  private boolean _isRequired;
  private boolean _isFieldTest;
  private int     _page;
  private int     _position;
  private int     _segment;
  private String  _segmentID;

  @JsonProperty ("ItemID")
  public String getItemID () {
    return _itemID;
  }

  public void setItemID (String _itemID) {
    this._itemID = _itemID;
  }

  @JsonProperty ("GroupID")
  public String getGroupID () {
    return _groupID;
  }

  public void setGroupID (String _groupID) {
    this._groupID = _groupID;
  }

  @JsonProperty ("IsRequired")
  public boolean isRequired () {
    return _isRequired;
  }

  public void setIsRequired (boolean _isRequired) {
    this._isRequired = _isRequired;
  }

  @JsonProperty ("IsFieldTest")
  public boolean isFieldTest () {
    return _isFieldTest;
  }

  public void setIsFieldTest (boolean _isFieldTest) {
    this._isFieldTest = _isFieldTest;
  }

  @JsonProperty ("Page")
  public int getPage () {
    return _page;
  }

  public void setPage (int _page) {
    this._page = _page;
  }

  @JsonProperty ("Position")
  public int getPosition () {
    return _position;
  }

  public void setPosition (int _position) {
    this._position = _position;
  }

  @JsonProperty ("Segment")
  public int getSegment () {
    return _segment;
  }

  public void setSegment (int _segment) {
    this._segment = _segment;
  }

  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  public void setSegmentID (String _segmentID) {
    this._segmentID = _segmentID;
  }

}

class AdaptiveGroup
{
  private final List<AdaptiveItem> _items = new ArrayList<AdaptiveItem> ();
  private int                      _page;
  private String                   _groupID;
  private long                     _bankKey;
  private int                      _segmentPosition;
  private String                   _segmentID;
  private int                      _numItemsRequired;

  public AdaptiveGroup () {
  }

  public List<AdaptiveItem> getItems () {
    return _items;
  }

  @JsonProperty ("Page")
  public int getPage () {
    return _page;
  }

  public void setPage (int _page) {
    this._page = _page;
  }

  @JsonProperty ("GroupID")
  public String getGroupID () {
    return _groupID;
  }

  public void setGroupID (String _groupID) {
    this._groupID = _groupID;
  }

  @JsonProperty ("BankKey")
  public long getBankKey () {
    return _bankKey;
  }

  public void setBankKey (long _bankKey) {
    this._bankKey = _bankKey;
  }

  @JsonProperty ("SegmentPosition")
  public int getSegmentPosition () {
    return _segmentPosition;
  }

  public void setSegmentPosition (int _segmentPosition) {
    this._segmentPosition = _segmentPosition;
  }

  @JsonProperty ("SegmentID")
  public String getSegmentID () {
    return _segmentID;
  }

  public void setSegmentID (String _segmentID) {
    this._segmentID = _segmentID;
  }

  @JsonProperty ("NumItemsRequired")
  public int getNumItemsRequired () {
    return _numItemsRequired;
  }

  public void setNumItemsRequired (int _numItemsRequired) {
    this._numItemsRequired = _numItemsRequired;
  }

}
