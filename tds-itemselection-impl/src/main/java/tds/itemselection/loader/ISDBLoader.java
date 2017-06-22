/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IItemSelectionDLL;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.impl.ItemResponse;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.OffGradeItemsProps;
import tds.itemselection.impl.blueprint.Strand;
import tds.itemselection.impl.sets.CsetGroupString;
import tds.itemselection.impl.sets.ItemPool;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;

/**
 * @author akulakov
 * 
 */
public class ISDBLoader extends AbstractDBLoader implements IItemSelectionDBLoader
{

  @Autowired
  IItemSelectionDLL     iSelDLL = null;

  private static Logger _logger = LoggerFactory.getLogger (ISDBLoader.class);

  public ISDBLoader () {
  };

  /*
  * 
  */
  public ItemGroup getItemGroup (SQLConnection connection, UUID oppkey,
      String segmentKey,
      String groupID,
      String blockID,
      Boolean isFieldTest) throws ReturnStatusException
  {

    MultiDataResultSet multiDataResultSet = iSelDLL.AA_GetItemgroup_SP (connection, oppkey,
        segmentKey, groupID, blockID, isFieldTest, false);

    Iterator<SingleDataResultSet> setItr = multiDataResultSet.getResultSets ();

    SingleDataResultSet groupTable = null;
    SingleDataResultSet itemTable = null;

    // if multiDataResultSet has 2 SinleDataResultSets: first -> groups;
    // second -> items
    // if has only one: it is items
    if (setItr.hasNext ())
    {
      if (multiDataResultSet.getUpdateCount () > 1)
      {
        groupTable = setItr.next ();
        itemTable = setItr.next ();
      }
      else
      {
        itemTable = setItr.next ();
      }
    }
    Integer numRequired = null;
    Integer maxItems = null;
    DbResultRecord record;
    Iterator<DbResultRecord> recItr = groupTable.getRecords ();
    record = groupTable.getCount () > 0 ? groupTable.getRecords ().next () : null;
    if (record != null) {
      groupID = record.<String> get ("groupID");
      numRequired = record.<Integer> get ("numRequired");
      maxItems = record.<Integer> get ("maxItems");
    }
    Integer _maxItems = null;
    if( maxItems != null && maxItems < 1000000)
    {
      _maxItems = new Integer(maxItems.toString ());          
    }
    else
    {
      throw new ReturnStatusException(" Max items = " + maxItems + " is null or big");
    }
      
    ItemGroup itemGr = new ItemGroup (groupID, numRequired, _maxItems);
    //
    recItr = itemTable.getRecords ();
    String itemID = null;
    String strand = null;
    String IRT_Model = null;
    String bVector = null;
    int formPosition = 0;
    Float irtA = null;
    Float irtC = null;
    Double IRT_b = null;
    Double IRT_a = null;
    Double IRT_c = null;
    Boolean isRequired = null;
    //
    while (recItr.hasNext ())
    {
      record = recItr.next ();
      if (record != null) {
        itemID 	= record.<String> get ("itemID");
        groupID = record.<String> get ("groupID");
        strand 	= record.<String> get ("strand");
        bVector = record.<String> get ("bVector");
        formPosition = record.<Integer> get ("formPosition");
        isFieldTest = record.<Boolean> get ("isFieldTest");
        isRequired  = record.<Boolean> get ("isRequired");
        // Following parameters don't needed for fixed form algorithm
        String irt_BStr = record.<String> get ("IRT_b");
        IRT_b = (irt_BStr != null)? Double.parseDouble (irt_BStr):  Double.NaN;
        irtA = record.<Float> get ("IRT_a");
        irtC = record.<Float> get ("IRT_c");
        IRT_a = (irtA != null)? new Double (irtA): Double.NaN;
        IRT_c = (irtC != null)? new Double (irtC): Double.NaN;
        IRT_Model = record.<String> get ("IRT_Model");
      }
      TestItem item = new TestItem (itemID, groupID, formPosition, true, isFieldTest, strand
          , isRequired, IRT_b, IRT_a, IRT_c, IRT_Model, bVector);
      itemGr.addItem (item);
    }

    return itemGr;
  }

  /**
   * @param _oppkey
   * @param segmentKey
   * @param _customPool
   * @param _previousGroups
   * @param excludeGroups
   * @param _responses
   * 
   * @param _startAbility
   * @throws ItemSelectionException
   */
  // / <summary>
  // / Loads the test opportunity's data history
  // / </summary>
  // / <param name="oppkey">Input the opportunity key</param>
  // / <param name="groupStrings">Return a list of CsetGroupString
  // objects</param>
  // / <param name="fieldTestList">Return Delimited list of field test item
  // groups selected for this test opp</param>
  // / <param name="items">Return items administered and scored this test as a
  // list of ItemResponse objects</param>
  // / <param name="startAbility">Return this testopp start ability</param>
  // / <returns></returns>
  public StudentHistory2013 loadOppHistory (SQLConnection connection, UUID oppkey,
      String segmentKey) throws ItemSelectionException {

    StudentHistory2013 result = new StudentHistory2013 ();

    Integer position = null;
    String itemgroupString = null;
    UUID oppk = null;
    String ID = null;
    String groupID = null;
    Integer itemScore = null;
    Boolean isFT;
    Integer segment = null;
    CsetGroupString GS;
    ItemResponse resp;

    SingleDataResultSet res;
    DbResultRecord record;

    try {

      MultiDataResultSet mDRSet = iSelDLL.AA_GetDataHistory_SP (connection, oppkey, segmentKey);
      Iterator<SingleDataResultSet> dsetItr = mDRSet.getResultSets ();
      // Table 0: 1 row,
      // cols: initialAbility itempool
      res = dsetItr.next ();
      record = res.getCount () > 0 ? res.getRecords ().next () : null;
      if (record != null) {
        result.setStartAbility (record.<Float> get ("initialAbility"));
        result.setCustomPool (record.<String> get ("itempool"));
      }
      // Table 1: groupStrings. 1 row for each previously completed test in the
      // same subject.
      // Cols: oppkey dateStarted itemgroupString
      // The rows are ordered by dateStarted, so we convert that to an integer
      // sequence number for the CsetGroupString
      List<CsetGroupString> previousGroups = new ArrayList<CsetGroupString> ();
      res = dsetItr.next ();
      Iterator<DbResultRecord> recItr = res.getRecords ();
      position = 1;
      while (recItr.hasNext ())
      {
        record = recItr.next ();
        if (record != null) {
          oppk = record.<UUID> get ("oppkey");
          itemgroupString = record.<String> get ("itemgroupString");
          GS = new CsetGroupString (oppk, position, itemgroupString);
          previousGroups.add (GS);
          position++;
        }
      }
      result.setGroups (previousGroups);
      // Table 2: Field test items selected for this test. 1 row for each field
      // test item group pre-selected
      // Cols: FTGroupID
      Map<String, String> excludeGroups = new HashMap<String, String> ();
      res = dsetItr.next ();
      recItr = res.getRecords ();
      while (recItr.hasNext ())
      {
        record = recItr.next ();
        if (record != null) {
          ID = record.<String> get ("FTGroupID");
          excludeGroups.put (ID, ID);
        }
      }
      result.setExcludeGroups (excludeGroups);
      // Table 3: Items administered this test. 1 Row for each item previously
      // selected (administered or not)
      // Cols: segmentPosition segmentID page position groupID itemID score
      // isFieldTest Irt_B strand
      Map<String, ItemResponse> responses = new HashMap<String, ItemResponse> ();
      res = dsetItr.next ();
      recItr = res.getRecords ();
      while (recItr.hasNext ())
      {
        record = recItr.next ();
        if (record != null) {
          segment = record.<Integer> get ("segmentPosition");
          ID = record.<String> get ("itemID");
          groupID = record.<String> get ("groupID");

          position = record.<Integer> get ("position");
          itemScore = record.<Integer> get ("score");
          isFT = record.<Boolean> get ("isFieldTest");

          resp = new ItemResponse (segment, ID, groupID, position, itemScore, isFT);
          responses.put (ID, resp);
        }
      }
      result.setResponses (responses);
    } catch (Exception e)
    {
      _logger.error ("Error occurs in LoadOppHistory method: " + e.getMessage ());
      throw new ItemSelectionException (e.getMessage (), e);
    }
    return result;

  }

  // / <summary>
  // / Loads a segment's blueprint and itempool
  // / </summary>
  // / <param name="segmentKey">The database key to the test segment</param>
  // / <param name="segment">A pointer to the segment object to fill with
  // data</param>
  // / <param name="sessionKey">For simulations, the non-null database key to
  // the session</param>
  public void loadSegment (SQLConnection connection, String segmentKey, TestSegment segment, UUID sessionKey) throws ReturnStatusException, ItemSelectionException
  {
    MultiDataResultSet dataSets = null;
    if (sessionKey == null)
    {
      dataSets = iSelDLL.AA_GetSegment_SP (connection, segmentKey);
    }
    else
    {
      dataSets = iSelDLL.AA_SIM_GetSegment_SP (connection, sessionKey, segmentKey);
    }

    Iterator<SingleDataResultSet> sItr = dataSets.getResultSets ();

    SingleDataResultSet segTbl = null;
    SingleDataResultSet clTbl = null;
    SingleDataResultSet grpTbl = null;
    SingleDataResultSet itemTbl = null;

    if (dataSets.getUpdateCount () < 4)
    {
      String error = "loadSegment method return is corrupted: there is not any single data set";
      _logger.error (error);
      throw new ItemSelectionException (error);
    }

    try {
      segTbl = sItr.next ();
      // C#: The Segment-level Blueprint:
      // segmentkey ParentTest segmentPosition SegmentID bpWeight itemWeight
      // abilityOffset cset1size randomizer initialRandom minOpItems maxOpItems
      // startAbility startInfo slope intercept FTStartPos FTEndPos FTMinItems
      // FTMaxItems selectionAlgorithm
      clTbl = sItr.next ();
      // The itemgroups (note: groups of single items with no stimuli are NOT
      // represented, but must be created from the items below
      // C#:Items with no group are driven by the item's isRequired flag, so
      // itemsRequired = 0 and maxItems = 1
      // itemGroup itemsRequired maxItems bpweight
      grpTbl = sItr.next ();
      // C#:The Items:
      // GID itemkey position isRequired strand isActive irtModel irtB irtA irtC
      // bVector clString
      itemTbl = sItr.next ();

      DbResultRecord record;

      record = segTbl.getCount () > 0 ? segTbl.getRecords ().next () : null;
      if (record != null) {
    	Long tmp = record.<Long> get ("refreshMinutes");
        segment.refreshMinutes = new Integer(tmp.toString());
        segment.parentTest = record.<String> get ("ParentTest");
        tmp = record.<Long> get ("segmentPosition");
        segment.position = new Integer(tmp.toString());
      }

      // The content-levels (BpElements)
      // contentLevel minItems maxItems isStrictMax bpweight adaptiveCut
      // StartAbility StartInfo Scalar isStrand
      loadSegmentBlueprint (segTbl, clTbl, segment.getBp ());
      loadSegmentItempool (grpTbl, itemTbl, segment.getPool ());

      // Not sure if we really need the pool of sibling items.
      if (segment.parentTest != segmentKey && sItr.hasNext ())
      {
        loadSegmentSiblingPool (sItr.next (), segment.getPool ());
      }
    } catch (Exception e)
    {
      throw new ItemSelectionException (" Error occurs in loadSegment method: " + e.getMessage ());
    }

  }

  private void loadSegmentBlueprint (SingleDataResultSet bpTbl, SingleDataResultSet clTbl, Blueprint blueprint)
  {
    BpElement elem;
    Strand strand;
    DbResultRecord record;

    // The Segment-level Blueprint:
    // segmentkey ParentTest segmentPosition SegmentID bpWeight itemWeight
    // abilityOffset cset1size cset1Order
    // randomizer initialRandom minOpItems maxOpItems startAbility startInfo
    // slope intercept
    // FTStartPos FTEndPos FTMinItems FTMaxItems selectionAlgorithm,
    // adaptiveVersion

    record = bpTbl.getCount () > 0 ? bpTbl.getRecords ().next () : null;
    if (record != null) {
      String segmentKey = record.<String> get ("segmentkey");
      String segmentID 	= record.<String> get ("SegmentID");
      Long tmp = record.<Long> get ("segmentPosition");
      Integer segmentPosition = new Integer(tmp.toString());
      tmp = record.<Long> get ("minOpItems");
      Integer minOpItems = new Integer(tmp.toString());
      tmp = record.<Long> get ("maxOpItems");
      Integer maxOpItems = new Integer(tmp.toString());
      Double bpWeight = new Double(record.<Float> get ("bpWeight"));
      Double itemWeight = new Double(record.<Float> get ("itemWeight"));
      Double abilityOffset = new Double(record.<Float> get ("abilityOffset"));
      Integer randomizer = record.<Integer> get ("randomizer");
      Integer initialRandom = record.<Integer> get ("initialRandom");
      Double startInfo = new Double(record.<Float> get ("startInfo"));
      Double startAbility = new Double(record.<Float> get ("startAbility"));
      Integer cset1size = record.<Integer> get ("cset1size");
      String cset1Order = record.<String> get ("cset1Order");
      Double slope = new Double(record.<Float> get ("slope"));
      Double intercept = new Double(record.<Float> get ("intercept"));
      String adaptiveVersion = record.<String> get ("adaptiveVersion");
      Integer abilityWeight = (int)(float) record.<Float> get ("abilityweight");
      // Not needed for old algorithm
      OffGradeItemsProps offGradeItemsProps = new OffGradeItemsProps();
      String offGradePoolFilter = null;
      int minOpItemsTest = 1; // default value
      int maxOpItemsTest = 10;// default value

      blueprint.Initialize (
          segmentKey,
          segmentID,
          segmentPosition,
          minOpItems,
          maxOpItems,
          bpWeight,
          itemWeight,
          abilityOffset,
          randomizer,
          initialRandom,
          startInfo,
          startAbility,
          cset1size,
          cset1Order,
          slope,
          intercept,
          adaptiveVersion,
          offGradeItemsProps,
          offGradePoolFilter,
          minOpItemsTest,
          maxOpItemsTest,
          abilityWeight
          );
    }
    // C#: The content-levels (BpElements)
    // contentLevel minItems maxItems isStrictMax bpweight adaptiveCut
    // StartAbility StartInfo Scalar isStrand
    Iterator<DbResultRecord> recItr = clTbl.getRecords ();
    // C#: bpelements only, strands and strictmax content levels
    while (recItr.hasNext ())
    {
      record = recItr.next ();
      if (record != null)
      {
        String contentLevel = record.<String> get ("contentLevel");
        Integer minItems = record.<Integer> get ("minItems");
        Integer maxItems = record.<Integer> get ("maxItems");
        Double bpweight = new Double(record.<Float> get ("bpweight"));
        Boolean isStrictMax = record.<Boolean> get ("isStrictMax");

        if (record.<Long> get ("isStrand") == 1)
        {
          Double adaptiveCut = new Double(record.<Float> get ("adaptiveCut"));
          Double StartInfo = new Double(record.<Float> get ("StartInfo"));
          Double StartAbility = new Double(record.<Float> get ("StartAbility"));
          Double Scalar = new Double(record.<Float> get ("Scalar"));

          strand = new Strand (contentLevel, minItems, maxItems, bpweight
              , isStrictMax, adaptiveCut, StartInfo, StartAbility, Scalar);

          blueprint.AddStrand (strand);
        }
        else
        {
          elem = new BpElement (contentLevel, minItems, maxItems, isStrictMax, bpweight);

          blueprint.AddElement (elem);
        }
      }
    }
  }

  private void loadSegmentItempool (SingleDataResultSet grpTbl, SingleDataResultSet itmTbl, ItemPool pool)
  {
    // C#: The itemgroups (note: groups of single items with no stimuli are NOT
    // represented, but must be created from the items below
    // They can be identified by the fact that their 'groupID' starts with 'I-'
    // Items with no group are driven by the item's isRequired flag, so
    // itemsRequired = 0 and maxItems = 1
    // itemGroup itemsRequired maxItems bpweight
    ItemGroup grp;
    TestItem item;
    DbResultRecord record;

    Iterator<DbResultRecord> recItr = grpTbl.getRecords ();
    while (recItr.hasNext ()) // C#: bpelements only, strands and strictmax
                              // content levels
    {
      record = recItr.next ();
      if (record != null)
      {
        String itemGroup = record.<String> get ("itemGroup");
        Integer itemsRequired = record.<Integer> get ("itemsRequired");
        Integer maxItems = record.<Integer> get ("maxItems");

        grp = new ItemGroup (itemGroup, itemsRequired, maxItems);
        pool.addItemgroup (grp);
      }
    }

    // C#: The Items:
    // GID itemkey position isRequired strand isFieldTest isActive irtModel irtB
    // irtA irtC bVector clString
    // TestItem(string ID, string group, int position, bool isActive, string
    // strand, bool isRequired, bool isFieldTest,
    // double irtB, double irtA, double irtC, string irtModel, string bVector,
    // string contentLevels)

    recItr = itmTbl.getRecords ();
    while (recItr.hasNext ()) // C#: bpelements only, strands and strictmax
                              // content levels
    {
      record = recItr.next ();
      if (record != null)
      {
        String itemkey = record.<String> get ("itemkey");
        String GID = record.<String> get ("GID");
        Integer position = record.<Integer> get ("position");
        Boolean isActive = record.<Boolean> get ("isActive");
        String strand = record.<String> get ("strand");
        Boolean isRequired = record.<Boolean> get ("isRequired");
        Boolean isFieldTest = record.<Boolean> get ("isFieldTest");
        //TODO what?
        Double irtB = new Double(record.<String> get ("irtB"));
        Double irtA = new Double(record.<Float> get ("irtA"));
        Double irtC = new Double(record.<Float> get ("irtC"));
        String irtModel = record.<String> get ("irtModel");
        String bVector = record.<String> get ("bVector");
        String clString = record.<String> get ("clString");

        item = new TestItem (itemkey, GID, position, isActive, strand, isRequired, isFieldTest
            , irtB, irtA, irtC, irtModel, bVector, clString);
        pool.addItem (item);
      }
    }
  }

  private void loadSegmentSiblingPool (SingleDataResultSet itmTbl, ItemPool itemPool)
  {
    TestItem item;
    // C#: The Items:
    // GID itemkey position isRequired strand isFieldTest isActive irtModel irtB
    // irtA irtC bVector clString
    // public TestItem(string ID, string group, int position, bool isActive,
    // bool isFieldTest, string strand, bool isRequired,
    // double irtB, double irtA, double irtC, string irtModel, string bVector)
    Iterator<DbResultRecord> recItr = itmTbl.getRecords ();
    DbResultRecord record;
    while (recItr.hasNext ())
    {
      record = recItr.next ();
      if (record != null)
      {
        String itemkey = record.<String> get ("itemkey");
        String GID = record.<String> get ("GID");
        Integer position = record.<Integer> get ("position");
        Boolean isActive = record.<Boolean> get ("isActive");
        String strand = record.<String> get ("strand");
        Boolean isRequired = record.<Boolean> get ("isRequired");
        Boolean isFieldTest = record.<Boolean> get ("isFieldTest");
        //TODO what?
        Double irtB = new Double(record.<String> get ("irtB"));
        Double irtA = new Double(record.<Float> get ("irtA"));
        Double irtC = new Double(record.<Float> get ("irtC"));
        String irtModel = record.<String> get ("irtModel");
        String bVector = record.<String> get ("bVector");
        // String clString = record.<String> get("clString");
        // C#: ,I["clString"].ToString()); content levels not required for
        // sibling items

        item = new TestItem (itemkey, GID, position, isActive, strand, isRequired, isFieldTest
            , irtB, irtA, irtC, irtModel, bVector, null);
        itemPool.addSiblingItem (item);
      }
    }
  }

	@Override
	public boolean setSegmentSatisfied(SQLConnection connection, UUID oppkey, Integer segmentPosition,
			String reason) throws ReturnStatusException {

		return iSelDLL.AA_SetSegmentSatisfied_SP(connection, oppkey,
				segmentPosition, reason);
	}

	@Override
	public String addOffGradeItems(SQLConnection connection, UUID oppkey,
			String designation, String segmentKey, _Ref<String> reason)
			throws ReturnStatusException {
		// TODO Auto-generated method stub
		return null;
	}
}
