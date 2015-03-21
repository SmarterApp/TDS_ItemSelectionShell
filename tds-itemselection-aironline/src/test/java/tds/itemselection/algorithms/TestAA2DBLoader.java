/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opentestsystem.shared.test.LifecycleManagingTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.dll.api.IItemSelectionDLL;
import tds.itemselection.DLLHelper;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.Dimension;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.base.TestItem;
import tds.itemselection.debug.FileComparison;
//import tds.itemselection.debug.DLLHelper;
import tds.itemselection.debug.FilePrint;
import tds.itemselection.impl.blueprint.BpElement;
import tds.itemselection.impl.blueprint.Blueprint;
import tds.itemselection.impl.blueprint.OffGradeItemsProps;
import tds.itemselection.impl.blueprint.ReportingCategory;
import tds.itemselection.loader.IItemSelectionDBLoader;
import tds.itemselection.loader.StudentHistory2013;
import tds.itemselection.loader.TestSegment;


@RunWith (LifecycleManagingTestRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class TestAA2DBLoader {

	@Autowired
	private IItemSelectionDLL iSelDLL = null;

	@Autowired
	@Qualifier("aa2DBLoader")
	private IItemSelectionDBLoader loader = null;

	@Autowired
	private DLLHelper _myDllHelper = null;

	private static Logger 	_logger = LoggerFactory.getLogger(TestAA2DBLoader.class);
	private UUID 			oppKey;

	private Object 			ls = System.getProperties().get("line.separator");

	private SQLConnection 	_connection 				= null;
	private Boolean 		_preexistingAutoCommitMode 	= null;
	private String segmentKey = "(SBAC)CAT-M3-ONON-S1-A1-MATH-3-Fall-2013-2014";

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

	@Test // Test-14
	public void test_loadSegment() {
		String segmentKey = "(SBAC)SBAC-OP-ADAPTIVE-G5E-ELA-5-Spring-2014-2015";
		TestSegment segment = new TestSegment(segmentKey);

		// we test this function
		try {
			loader.loadSegment(_connection, segmentKey, segment, null);
		} catch (ReturnStatusException | ItemSelectionException e) {

			e.printStackTrace();
		}
//		String path = "c:\\temp\\TEST9" ;
		String path = "C:\\temp\\TEST14\\Java_Results" ;

		String path1 = path + "\\Java1Blueprint.csv";
		String path2 = path + "\\Java2ReportingCategories.csv";			
		String path3 = path + "\\Java3BlueprintElements.csv";		
		String path4 = path + "\\Java4Groups.csv";			
		String path5 = path + "\\Java5TestItems.csv";
		
		FilePrint.string2File(path1, resultBp2String(segment.segmentBlueprint));
		FilePrint.string2File(path2, resultRC2String(segment.segmentBlueprint.getReportingCategories()));
		FilePrint.string2File(path3, resultBpElem2String(segment.segmentBlueprint.getBPElements()));
		FilePrint.string2File(path4, resultGroups2String(segment.segmentItemPool.getItemGroups()));
		FilePrint.string2File(path5, resultItems2String(segment.segmentItemPool.getItems()));
		
		System.out.println("Test was completed successfully");
		
	}
	//@Test
	public void test_loadSegment_Main()throws ReturnStatusException {
		String segmentKey = "(SBAC)CAT-M3-ONON-S1-A1-MATH-3-Fall-2013-2014";

		TestSegment segment = new TestSegment(segmentKey);
		double percent = 0.;

		// we test this function
		try {
			loader.loadSegment(_connection, segmentKey, segment, null);
		} catch (ReturnStatusException | ItemSelectionException e) {
			_logger.error(e.getMessage());
			throw new ReturnStatusException("Load Segment test is failed");
		}
		// To compare with
		String path = "c:\\temp\\TEST9" ;

		String path1 = path + "\\NET1Blueprint.csv";
		String path2 = path + "\\NET2ReportingCategories.csv";			
		String path3 = path + "\\NET3BlueprintElements.csv";		
		String path4 = path + "\\NET4Groups.csv";			
		String path5 = path + "\\NET5TestItems.csv";
		
		try {
			percent = FileComparison.compare2(path1, segment.segmentBlueprint);
			_logger.debug("Percent coinsiding fields in NET and Java Blueprint = " + percent);
			System.out.println("Percent coinsiding fields in NET and Java Blueprint = " + percent);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			_logger.error(e.getMessage());		
		}
		
		try {
			percent = FileComparison.compare3(path2, segment.segmentBlueprint.getReportingCategories(), "ID");
			_logger.debug("Percent coinsiding fields in NET and Java ReportingCategories = " + percent);
			System.out.println("Percent coinsiding fields in NET and Java ReportingCategories = " + percent);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			_logger.error(e.getMessage());		
		}
		
		try {
			percent = FileComparison.compare3(path3, segment.segmentBlueprint.getBPElements(), "ID");
			_logger.debug("Percent coinsiding fields in NET and Java BPElements = " + percent);
			System.out.println("Percent coinsiding fields in NET and Java BPElements = " + percent);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			_logger.error(e.getMessage());		
		}
		
		try {
			percent = FileComparison.compare3(path4, segment.segmentItemPool.getItemGroups(), "groupID");
			_logger.debug("Percent coinsiding fields in NET and Java temGroups = " + percent);
			System.out.println("Percent coinsiding fields in NET and Java temGroups = " + percent);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			_logger.error(e.getMessage());		
		}
		
		try {
			percent = FileComparison.compare3(path5, segment.segmentItemPool.getItems(), "itemID");
			_logger.debug("Percent coinsiding fields in NET and Java Items = " + percent);
			System.out.println("Percent coinsiding fields in NET and Java Items = " + percent);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			_logger.error(e.getMessage());		
		}
	}
	
	//@Test
	public void test_loadHistory_Main() throws ReturnStatusException {
		String OPPKEY = "a1674ef0-9042-428e-beab-9f082bdc93f8";
//		String OPPKEY = "24f000c7-a32f-439b-a55b-9a6e74af0649";
		UUID oppkey = (UUID.fromString(OPPKEY));
		_logger.info("Oppkey =  " + OPPKEY);
		double percent = 0.;
		
		StudentHistory2013 stHistory = null;
		
		try {
			stHistory = loader.loadOppHistory(_connection, oppkey, segmentKey);
		} catch (ItemSelectionException e) {
			_logger.error(e.getMessage());
			throw new ReturnStatusException("Load History test is failed");
		}
		if(stHistory != null)
		{
			// To compare with
			String path = "c:\\temp\\TEST9" ;
	
			String path1 = path + "\\NET6GroupStrings.csv";
			String path2 = path + "\\NET7Responses.csv";			
			
			try {
				percent = FileComparison.compare3(path1, stHistory.getGroups(), "groupString");
				_logger.debug("Percent coinsiding fields in NET and Java PreviousGroups = " + percent);
				System.out.println("Percent coinsiding fields in NET and Java PreviousGroups = " + percent);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				_logger.error(e.getMessage());		
			}
			
			try {
				percent = FileComparison.compare3(path2, stHistory.getResponses(), "itemID");
				_logger.debug("Percent coinsiding fields in NET and Java Responses = " + percent);
				System.out.println("Percent coinsiding fields in NET and Java Responses = " + percent);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				_logger.error(e.getMessage());		
			}
		}
	}
	
	//@Test
	public void test_dump_loadSegment() {
		String segmentKey = "(SBAC)CAT-M3-ONON-S1-A1-MATH-3-Fall-2013-2014";
		TestSegment segment = new TestSegment(segmentKey);

		// we test this function
		try {

			loader.loadSegment(_connection, segmentKey, segment, null);
		} catch (ReturnStatusException | ItemSelectionException e) {
			e.printStackTrace();
		}

		String path1 = "c:\\temp\\TEST9\\Java1Blueprint_2.csv";
		FilePrint.string2File(path1, dumpBp2String(segment.segmentBlueprint));
			
	}
	
	
	//@Test
	public void test_loadSegment2File() {
		TestSegment segment = new TestSegment(segmentKey);
		// we test this function
		try {
			loader.loadSegment(_connection, segmentKey, segment, null);
		} catch (ReturnStatusException | ItemSelectionException e) {
			e.printStackTrace();
		}
		String path1 = "C:\\temp\\TEST9\\JAVA1Blueprint.csv";
		String res = FilePrint.fieldsToString(segment.getBp());
		FilePrint.string2File(path1, res);
		
		double ret = 0.0;

		try {
			ret = FileComparison.compare(path1, segment.getBp())* 100.;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		System.out.println("% of the coinsistent fields = " + ret + "%");		
	}

	@Test
	public void test_loadSegment_offGradeItems() {
		String segmentKey = "(SBAC)SBAC-OP-ADAPTIVE-G5M-MATH-5-Spring-2014-2015";
		TestSegment segment = new TestSegment(segmentKey);

		// we test this function
		try {
			loader.loadSegment(_connection, segmentKey, segment, null); // null <==> not Simulation
		} catch (ReturnStatusException | ItemSelectionException e) {
			e.printStackTrace();
		}
		String path = "c:\\temp\\TEST14\\Java_Results" ;

		String path1 = path + "\\Java1Blueprint.csv";
		String path2 = path + "\\Java2ReportingCategories.csv";			
		String path3 = path + "\\Java3BlueprintElements.csv";		
		String path4 = path + "\\Java4Groups.csv";			
		String path5 = path + "\\Java5TestItems.csv";
		String path7 = path + "\\Java1_Bp_7_OffgradeItems.csv";
		
		FilePrint.string2File(path1, resultBp2String(segment.segmentBlueprint));
		FilePrint.string2File(path2, resultRC2String(segment.segmentBlueprint.getReportingCategories()));
		FilePrint.string2File(path3, resultBpElem2String(segment.segmentBlueprint.getBPElements()));
		FilePrint.string2File(path4, resultGroups2String(segment.segmentItemPool.getItemGroups()));
		FilePrint.string2File(path5, resultItems2String(segment.segmentItemPool.getItems()));
		FilePrint.string2File(path7, resultOffgradesItems2String(segment.segmentBlueprint));
	}

	//@Test
	public void test_loadOppHistory()
	{
		String OPPKEY = "a1674ef0-9042-428e-beab-9f082bdc93f8";
//		String OPPKEY = "24f000c7-a32f-439b-a55b-9a6e74af0649";
		UUID oppkey = (UUID.fromString(OPPKEY));
		_logger.info("Oppkey =  " + OPPKEY);
		
		StudentHistory2013 stHistory = null;
		
		try {
			stHistory = loader.loadOppHistory(_connection, oppkey, segmentKey);
		} catch (ItemSelectionException e) {
			e.printStackTrace();
		}
		String path1 = "C:\\temp\\StudentHistory.csv";
		String res = FilePrint.fieldsToString(stHistory);
		FilePrint.string2File(path1, res);
		
		double ret = 0.0;

		try {
			ret = FileComparison.compare(path1, stHistory)* 100.;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		System.out.println("% of the coinsistent fields = " + ret + "%");		
	}
	
	//@Test
	public void test_loadSegment2() {
		String segmentKey = "(SBAC)CAT-M3-ONON-S1-A1-MATH-3-Fall-2013-2014";
		TestSegment segment = new TestSegment(segmentKey);
		double ret = 0.;

		// we test this function
		try {
			loader.loadSegment(_connection, segmentKey, segment, null);
		} catch (ReturnStatusException | ItemSelectionException e) {
			e.printStackTrace();
		}
		String path1 = "C:\\temp\\AA_NET-Outputs\\JAVA1Blueprint.csv";
		try {
			ret = FileComparison.compare(path1, segment.getBp())* 100.;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		System.out.println("% of the coinsistent fields = " + ret + "%");
	}
	
	/**
	 * This test with localhost:3306/session databases
	 * see opentestsystem-override-test-properties.xml
	 * @throws Exception
	 */
	@Test
	public final void test_addOffGradeItems() throws Exception {

		_logger.info("Test of addOffGradeItems(SQLConnection connection, "
				+ "UUID oppkey, String poolfilterProperty, String segmentkey, _Ref<String> reason) for AdaptiveSelector2013: ");			

		try {
			String OPPKEY = "6A587A25-F2CE-455B-BAB5-BC8B3ABC15E3";	// main test
			UUID oppkey = (UUID.fromString(OPPKEY));
			_logger.info("Oppkey =  " + OPPKEY);
			
			String designation = "OFFGRADE ABOVE";
			String segmentKey = "(SBAC)CAT-M3-ONON-S1-A1-MATH-3-Fall-2013-2014";
			_Ref<String> reason = new _Ref<String>();
			
			String status =  loader.addOffGradeItems(_connection, oppkey,
					designation /* poolfilterProperty */, segmentKey, reason);
			
			System.out.println("status = " + status + "; reason = " + ((reason.get().isEmpty())? "empty!": reason.get()));

		} catch (Exception e) {
			_logger.error("Failed to add Offgrade items: " + e.getMessage());
			throw new ReturnStatusException(e);
		}
	
	}
	
	private String dumpBp2String2(Blueprint bp) {

		return FilePrint.fieldNamesValues(bp);
	}

	//
	private String dumpBp2String(Blueprint bp) {
		StringBuilder stb = new StringBuilder();
		
		List<String> fieldNames = Arrays.asList("segmentKey",
				"segmentID",
				"segmentPosition",
				"minOpItems",
				"maxOpItems",
				"bpWeight",
				"itemWeight",
				"abilityOffset",
				"randomizerIndex",
				"randomizerInitialIndex",
				"startInfo",
				"startAbility",
				"cSet2Size",
				"cset1Order",
				"slope",
				"intercept",
				"adaptiveVersion",
				"abilityWeight",
				"rcAbilityWeight",
				"rcAbilityWeight",
				"precisionTarget",
				"precisionTargetMetWeight",
				"precisionTargetNotMetWeight",
				"adaptiveCut",
				"tooCloseSEs",
				"terminateBasedOnOverallInformation",
				"terminateBasedOnReportingCategoryInformation",
				"terminateBasedOnCount",
				"terminateBasedOnScoreTooClose",
				"terminateBaseOnFlagsAnd"
				);
		
		return FilePrint.fieldNamesValues(fieldNames, bp);
	}
		//
		private String resultBp2String(Blueprint bp) {
			StringBuilder stb = new StringBuilder();

		stb.append("segmentkey").append(FilePrint.csvDelimeter)
				.append(bp.segmentKey).append(FilePrint.ls);
		stb.append("SegmentID").append(FilePrint.csvDelimeter)
				.append(bp.segmentID).append(FilePrint.ls);
		stb.append("segmentPosition").append(FilePrint.csvDelimeter)
				.append(bp.segmentPosition).append(FilePrint.ls);
		stb.append("minOpItems").append(FilePrint.csvDelimeter)
				.append(bp.minOpItems).append(FilePrint.ls);
		stb.append("maxOpItems").append(FilePrint.csvDelimeter)
				.append(bp.maxOpItems).append(FilePrint.ls);
		stb.append("bpWeight").append(FilePrint.csvDelimeter)
				.append(bp.bpWeight).append(FilePrint.ls);
		stb.append("itemWeight").append(FilePrint.csvDelimeter)
				.append(bp.itemWeight).append(FilePrint.ls);
		stb.append("abilityOffset").append(FilePrint.csvDelimeter)
				.append(bp.abilityOffset).append(FilePrint.ls);
		stb.append("randomizer").append(FilePrint.csvDelimeter)
				.append(bp.randomizerIndex).append(FilePrint.ls);
		stb.append("initialRandom").append(FilePrint.csvDelimeter)
				.append(bp.randomizerInitialIndex).append(FilePrint.ls);
		stb.append("startInfo").append(FilePrint.csvDelimeter).append(bp.info)
				.append(FilePrint.ls);
		stb.append("startAbility").append(FilePrint.csvDelimeter)
				.append(bp.startAbility).append(FilePrint.ls);
		stb.append("cset1size").append(FilePrint.csvDelimeter)
				.append(bp.cSet1Size).append(FilePrint.ls);
		stb.append("cset1Order").append(FilePrint.csvDelimeter)
				.append(bp.cset1Order).append(FilePrint.ls);
		stb.append("slope").append(FilePrint.csvDelimeter).append(bp.slope)
				.append(FilePrint.ls);
		stb.append("intercept").append(FilePrint.csvDelimeter)
				.append(bp.intercept).append(FilePrint.ls);
		stb.append("adaptiveVersion").append(FilePrint.csvDelimeter)
				.append(bp.adaptiveVersion).append(FilePrint.ls);
		stb.append("abilityWeight").append(FilePrint.csvDelimeter)
				.append(bp.abilityWeight).append(FilePrint.ls);
		stb.append("rcAbilityWeight").append(FilePrint.csvDelimeter)
				.append(bp.rcAbilityWeight).append(FilePrint.ls);
		stb.append("precisionTarget").append(FilePrint.csvDelimeter)
				.append(bp.precisionTarget).append(FilePrint.ls);
		stb.append("precisionTargetMetWeight").append(FilePrint.csvDelimeter)
				.append(bp.precisionTargetMetWeight).append(FilePrint.ls);
		stb.append("precisionTargetNotMetWeight")
				.append(FilePrint.csvDelimeter)
				.append(bp.precisionTargetNotMetWeight).append(FilePrint.ls);
		stb.append("segAdaptiveCut").append(FilePrint.csvDelimeter)
				.append(bp.adaptiveCut).append(FilePrint.ls);
		stb.append("tooCloseSEs").append(FilePrint.csvDelimeter)
				.append(bp.tooCloseSEs).append(FilePrint.ls);
		stb.append("terminationOverallInfo").append(FilePrint.csvDelimeter)
				.append(bp.terminateBasedOnOverallInformation)
				.append(FilePrint.ls);
		stb.append("terminationRCInfo").append(FilePrint.csvDelimeter)
				.append(bp.terminateBasedOnReportingCategoryInformation)
				.append(FilePrint.ls);
		stb.append("terminationMinCount").append(FilePrint.csvDelimeter)
				.append(bp.terminateBasedOnCount).append(FilePrint.ls);
		stb.append("terminationTooClose").append(FilePrint.csvDelimeter)
				.append(bp.terminateBasedOnScoreTooClose).append(FilePrint.ls);
		stb.append("terminationFlagsAnd").append(FilePrint.csvDelimeter)
				.append(bp.terminateBaseOnFlagsAnd).append(FilePrint.ls);

		return stb.toString();
	}

	//
	private String resultRC2String(List<ReportingCategory> rcs) {
		StringBuilder stb = new StringBuilder();

		stb.append("ID").append(FilePrint.csvDelimeter);
		stb.append("minRequired").append(FilePrint.csvDelimeter);
		stb.append("maxRequired").append(FilePrint.csvDelimeter);
		stb.append("isStrictMax").append(FilePrint.csvDelimeter);
		stb.append("bpweight").append(FilePrint.csvDelimeter);
		stb.append("adaptiveCut").append(FilePrint.csvDelimeter);
		stb.append("startInfo").append(FilePrint.csvDelimeter);
		stb.append("startAbility").append(FilePrint.csvDelimeter);
		stb.append("abilityWeight").append(FilePrint.csvDelimeter);
		stb.append("adaptiveWeight").append(FilePrint.csvDelimeter);
		stb.append("precisionTarget").append(FilePrint.csvDelimeter);
		stb.append("precisionTargetMetWeight").append(FilePrint.csvDelimeter);
		stb.append("precisionTargetNotMetWeight")
				.append(FilePrint.csvDelimeter);
		stb.append("bpElementType").append(FilePrint.ls);

		for (ReportingCategory rc : rcs) {
			stb.append(rc.ID).append(FilePrint.csvDelimeter);
			stb.append(rc.minRequired).append(FilePrint.csvDelimeter);
			stb.append(rc.maxRequired).append(FilePrint.csvDelimeter);
			stb.append(rc.isStrictMax).append(FilePrint.csvDelimeter);
			stb.append(rc.weight).append(FilePrint.csvDelimeter);
			stb.append(rc.adaptiveCut).append(FilePrint.csvDelimeter);
			stb.append(rc.startInfo).append(FilePrint.csvDelimeter);
			stb.append(rc.getStartAbility()).append(FilePrint.csvDelimeter);
			stb.append(rc.abilityWeight).append(FilePrint.csvDelimeter);
			stb.append(rc.adaptiveWeight).append(FilePrint.csvDelimeter);
			stb.append(rc.precisionTarget).append(FilePrint.csvDelimeter);
			stb.append(rc.precisionTargetMetWeight).append(
					FilePrint.csvDelimeter);
			stb.append(rc.precisionTargetNotMetWeight).append(
					FilePrint.csvDelimeter);
			stb.append(rc.bpElementType).append(FilePrint.ls);
		}
		return stb.toString();
	}

	//
	private String resultBpElem2String(List<BpElement> bpElements) {
		StringBuilder stb = new StringBuilder();

		stb.append("ID").append(FilePrint.csvDelimeter);
		stb.append("minRequired").append(FilePrint.csvDelimeter);
		stb.append("maxRequired").append(FilePrint.csvDelimeter);
		stb.append("isStrictMax").append(FilePrint.csvDelimeter);
		stb.append("bpweight").append(FilePrint.csvDelimeter);
		stb.append("bpElementType").append(FilePrint.ls);

		for (BpElement elem : bpElements) {
			stb.append(elem.ID).append(FilePrint.csvDelimeter);
			stb.append(elem.minRequired).append(FilePrint.csvDelimeter);
			stb.append(elem.maxRequired).append(FilePrint.csvDelimeter);
			stb.append(elem.isStrictMax).append(FilePrint.csvDelimeter);
			stb.append(elem.weight).append(FilePrint.csvDelimeter);
			stb.append(elem.bpElementType).append(FilePrint.ls);
		}
		return stb.toString();
	}

	// resultGroups2String
	private String resultOffgradesItems2String(Blueprint bp) {
		StringBuilder stb = new StringBuilder();
		
		OffGradeItemsProps offGradeItemsProps = bp.offGradeItemsProps;
	    //      specific to examinee
	    String 	offGradePoolFilter = bp.offGradePoolFilter;
	    /**
		public Map<String, Integer> countByDesignator = new HashMap<String, Integer>(); // ex: { { "OFFGRADE ABOVE", 50 }, { "OFFGRADE BELOW", 35 } }
	    public Integer 	minItemsAdministered; 
	    public Double 	proficientTheta 	= null; // the cut score representing proficient, can be null
	    public Integer 	proficientPLevel 	= null; // the integer level associated with the previous value, can be null
	    public Double 	probAffectProficiency = 0.0001; // default value
	     */

		stb.append("minItemsAdministered").append(FilePrint.csvDelimeter);
		stb.append("proficientTheta").append(FilePrint.csvDelimeter);
		stb.append("proficientPLevel").append(FilePrint.csvDelimeter);
		stb.append("probAffectProficiency").append(FilePrint.csvDelimeter);
		stb.append("offGradePoolFilter").append(FilePrint.ls);

		stb.append(offGradeItemsProps.minItemsAdministered).append(FilePrint.csvDelimeter);
		stb.append(offGradeItemsProps.proficientTheta).append(FilePrint.csvDelimeter);
		stb.append(offGradeItemsProps.proficientPLevel).append(FilePrint.csvDelimeter);
		stb.append(offGradeItemsProps.probAffectProficiency).append(FilePrint.csvDelimeter);
		stb.append(offGradePoolFilter).append(FilePrint.ls);
		
		stb.append("countByDesignator").append(FilePrint.csvDelimeter);
		stb.append("propvalue").append(FilePrint.csvDelimeter);
		stb.append("count").append(FilePrint.ls);
		
		for(String offName: offGradeItemsProps.countByDesignator.keySet())
		{
			stb.append("").append(FilePrint.csvDelimeter);
			stb.append(offName).append(FilePrint.csvDelimeter);
			stb.append(offGradeItemsProps.countByDesignator.get(offName)).append(FilePrint.ls);			
		}
		return stb.toString();
		
	}
	
	// resultGroups2String
	private String resultGroups2String(Collection<ItemGroup> itemGroups) {
		List<ItemGroup> itemGrps = new ArrayList<ItemGroup>(itemGroups);
		return resultGroups2String(itemGrps);
	}

	private String resultGroups2String(List<ItemGroup> itemGroups) {
		StringBuilder stb = new StringBuilder();

		stb.append("groupID").append(FilePrint.csvDelimeter);
		stb.append("numRequired").append(FilePrint.csvDelimeter);
		stb.append("maxItems").append(FilePrint.ls);

		for (ItemGroup gr : itemGroups) {
			stb.append(gr.groupID).append(FilePrint.csvDelimeter);
			stb.append(gr.getNumRequired()).append(FilePrint.csvDelimeter);
			stb.append(gr.getMaxItems()).append(FilePrint.ls);
		}
		return stb.toString();
	}

	//
	private String resultItems2String(Collection<TestItem> items) {
		List<TestItem> itms = new ArrayList<TestItem>(items);
		return resultItems2String(itms);
	}

	//
	private String resultItems2String(List<TestItem> items) {
		StringBuilder stb = new StringBuilder();

		stb.append("itemID").append(FilePrint.csvDelimeter);
		stb.append("groupID").append(FilePrint.csvDelimeter);
		stb.append("segmentPosition").append(FilePrint.csvDelimeter);
		stb.append("isActive").append(FilePrint.csvDelimeter);
		stb.append("position").append(FilePrint.csvDelimeter);
		stb.append("groupID").append(FilePrint.csvDelimeter);
		stb.append("strand").append(FilePrint.csvDelimeter);
		stb.append("isRequired").append(FilePrint.csvDelimeter);
		stb.append("isFieldTest").append(FilePrint.csvDelimeter);
		stb.append("irtModel").append(FilePrint.csvDelimeter);
		stb.append("IsOverall").append(FilePrint.csvDelimeter);
		stb.append("ScorePoints").append(FilePrint.csvDelimeter);
		stb.append("a").append(FilePrint.csvDelimeter);
		stb.append("b").append(FilePrint.csvDelimeter);
		stb.append("c").append(FilePrint.csvDelimeter);
		stb.append("AverageB").append(FilePrint.csvDelimeter);
		stb.append("CL1").append(FilePrint.csvDelimeter);
		stb.append("CL2").append(FilePrint.csvDelimeter);
		stb.append("CL3").append(FilePrint.csvDelimeter);
		stb.append("CL4").append(FilePrint.csvDelimeter);
		stb.append("CL5").append(FilePrint.csvDelimeter);
		stb.append("CL6").append(FilePrint.csvDelimeter);
		stb.append(FilePrint.ls);

		for (TestItem it : items) {
			stb.append(field2String(it.itemID)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.groupID)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.segmentPosition)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.isActive)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.position)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.groupID)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.strandName)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.isRequired)).append(FilePrint.csvDelimeter);
			stb.append(field2String(it.isFieldTest)).append(FilePrint.csvDelimeter);

			List<Dimension> dms = it.dimensions;
			if(dms == null || dms.size() == 0)
			{
				stb.append(",,,,,,,");
			}
			else
			{
			for (Dimension dm : dms) {
				stb.append(field2String(dm.IRTModelName)).append(FilePrint.csvDelimeter);
				stb.append(field2String(dm.isOverall)).append(FilePrint.csvDelimeter);
				stb.append(field2String(dm.getScorePoints())).append(FilePrint.csvDelimeter);
				stb.append(field2String(dm.ParamA)).append(FilePrint.csvDelimeter);
				stb.append(field2String(dm.bVector)).append(FilePrint.csvDelimeter);
				stb.append(field2String(dm.ParamC)).append(FilePrint.csvDelimeter);
				stb.append(field2String(dm.averageB)).append(FilePrint.csvDelimeter);
			}
			}
			List<String> cls = it.contentLevels;
			for (String ctlstr : cls) {
				stb.append(ctlstr).append(FilePrint.csvDelimeter);
			}
			stb.append(FilePrint.ls);
		}
		return stb.toString();
	}

	private String field2String(Object ob)
	{
		StringBuilder stb = new StringBuilder();
		String nnull = "NULL";
		if(ob == null)
			return nnull;
		else
			return stb.append(ob).toString();
	}
}


