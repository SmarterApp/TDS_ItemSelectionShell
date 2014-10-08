/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.base;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemgroupselection.measurementmodels.IRTModel;
import AIR.Common.DB.results.DbResultRecord;

/**
 * @author akulakov
 * 
 */
public class TestItem implements Comparable<Object>
{
  
    private static String OVERALL_DIM_NAME 	= "OVERALL";
    private static String DELIM 			= ";";
    
	private static Logger _logger        	= LoggerFactory.getLogger (TestItem.class);

  // FROM dbo.AA_MakeCset1 Items in the pool customized for this testee
  // according to accommodations assigned
  // create table
  // #itempool (GID varchar(50), itemkey varchar(50), bpmetric float, isRequired
  // bit,
  // aaMetric float, admincount int, lastUsed datetime, irtB float, used int );
 
  /*
   * Common members for Adaptive Algorithm and Adaptive2 Algorithm
   */

  public String                         itemID;
  public String                         groupID;
  public int                            position; // was formPosition = // only applicable to fixed form tests
  public boolean                        isActive;
  public boolean                        isRequired;
  public boolean                        isFieldTest    = false;  
  public String                         strandName; // this is strand
  public int 							segmentPosition;
  // only applicable to fixed form tests ???
  public List<String> 					contentLevels = new ArrayList<String>();
  /*
   *  Common members for Adaptive Algorithm and Adaptive2 Algorithm
   */  
  public String                         irtModel;
  public double                         a;
  public double                         b;
  public double                         c;
  public double[]                       bVector;
  // not used yet
  public int                            _scorePoints;
  /*
   * Members for Adaptive Algorithm2 only
   */  
  /**
   * List of measurement dimensions
   */
  // String will be the member of Dimension -- name
//  public Map<String, Dimension> dimensions = new HashMap<String, Dimension>();  

  public List<Dimension> dimensions = new ArrayList<Dimension>();  
  /**
   * Flag to indicate whether this item has sub dimensions for measurement
   */
  public boolean hasDimensions = false ;
  
  /**
   * @return the _groupID
   */
  public String getGroupID () {
    return groupID;
  }

  /**
   * @param _groupID
   *          the _groupID to set
   */
  public void setGroupID (String _groupID) {
    this.groupID = _groupID;
  }

  /**
   * @return the itemID
   */
  public String getItemID () {
    return itemID;
  }

  /**
   * @param itemID
   *          the itemID to set
   */
  public void setItemID (String itemID) {
    this.itemID = itemID;
  }

  /**
   * @return the isRequired
   */
  public boolean isRequired () {
    return isRequired;
  }

  /**
   * @param isRequired
   *          the isRequired to set
   */
  public void setRequired (boolean isRequired) {
    this.isRequired = isRequired;
  }

  /**
   * @return the isFieldTest
   */
  public boolean isFieldTest () {
    return isFieldTest;
  }

	/**
	 * @param isFieldTest
	 *            the isFieldTest to set
	 */
	public void setFieldTest(boolean isFieldTest) {
		this.isFieldTest = isFieldTest;
	}

	/**
	 * 
	 * @return
	 */
	public int getSegmentPosition() {
		return segmentPosition;
	}

	/**
	 * 
	 * @param segmentPosition
	 */
	public void setSegmentPosition(int segmentPosition) {
		this.segmentPosition = segmentPosition;
	}

	public Collection<String> getContentLevels () {
    if (contentLevels == null)
      return null;
    else
      return contentLevels;
	}
  
	/*
	 * Constructors for Adaptive Algorithm
	 */
  // / <summary>
  // / Constructor for items not used in adaptive item selection
  // / </summary>
  // / <param name="ID"></param>
  // / <param name="group"></param>
  // / <param name="position"></param>
  // / <param name="isFieldTest"></param>
  // / <param name="strand"></param>
  // / <param name="isRequired"></param>
  // / <param name="irtB"></param>
  // / <param name="irtA"></param>
  // / <param name="irtC"></param>
  // / <param name="irtModel"></param>
  // / <param name="bVector"></param>
  public TestItem (String ID,
      String group,
      int position,
      boolean isActive,
      boolean isFieldTest,
      String strand,
      boolean isRequired,
      double irtB, double irtA, double irtC,
      String irtModel, String bVector)
  {
    this.itemID = ID;
    this.groupID = group;
    this.isFieldTest = isFieldTest;
    this.isActive = isActive;
    this.position = position;
    this.strandName = strand;
    this.isRequired = isRequired;
    this.b = irtB;
    this.a = irtA;
    this.c = irtC;
    this.irtModel = irtModel.toUpperCase ();
    String[] bv = bVector.split (DELIM);
    this.bVector = new double[bv.length];
    int i = 0;
    for (String b : bv)
    {
      this.bVector[i++] = Double.parseDouble (b);
    }
	dimensions = CreateDimensions(this.irtModel, this.a, this.b, bVector, this.c, 0.0);
  }

  // / <summary>
  // / Use for Loading stable test segment where we get the content levels as
  // a delimited string
  // / </summary>
  // / <param name="ID"></param>
  // / <param name="group"></param>
  // / <param name="position"></param>
  // / <param name="isFieldTest"></param>
  // / <param name="strand"></param>
  // / <param name="isRequired"></param>
  // / <param name="irtB"></param>
  // / <param name="irtA"></param>
  // / <param name="irtC"></param>
  // / <param name="irtModel"></param>
  // / <param name="bVector"></param>
  // / <param name="contentLevels"></param>
  public TestItem (String ID, String group, int position, boolean isActive, String strand, boolean isRequired, boolean isFieldTest,
      double irtB, double irtA, double irtC, String irtModel, String bVector, String contentLevels)
  {
    this.itemID = ID;
    this.groupID = group;
    this.isActive = isActive;
    this.position = position;
    this.strandName = strand;
    this.isRequired = isRequired;
    this.isFieldTest = isFieldTest;
    this.b = irtB;
    this.a = irtA;
    this.c = irtC;
    this.irtModel = irtModel.toUpperCase ();
    if(bVector != null)
    {
	    String[] bv = bVector.split (DELIM);
	    this.bVector = new double[bv.length];
	    int i = 0;
	    for (String b : bv)
	    {
	    	try{
	    		this.bVector[i] = Double.parseDouble (b); 
	    		i++;
	    	}catch(Exception e)
	    	{
	    		_logger.warn("Cannot parseDouble: " + b + ". " + e.getMessage());
	    		_logger.info("bVector[" + i + "] = 0.0.");
	    		this.bVector[i++] = 0.0;
	    		
	    	}
	    }
    }
    if(contentLevels != null)
    {
	    String[] cls = contentLevels.split (";");
	    for (String c : cls)
	    {
	      this.addContentLevel (c);
	    }
    }
	dimensions = CreateDimensions(this.irtModel, this.a, this.b, bVector, this.c, 0.0);

  }

  // / <summary>
  // / Use for field-test or fixedform items
  // / </summary>
  // / <param name="ID"></param>
  // / <param name="group"></param>
  // / <param name="position"></param>
  // / <param name="isFieldTest"></param>
  // / <param name="strand"></param>
  // / <param name="isRequired"></param>
  // / <param name="irtB"></param>
  public TestItem (String ID, String group, 
		  int position, boolean isFieldTest, String strand, 
		  boolean isRequired, double irtB)
  {
    this.itemID = ID;
    this.groupID = group;
    this.isFieldTest = isFieldTest;
    this.position = position;
    this.strandName = strand;
    this.isRequired = isRequired;
    this.b = irtB;

  }

  /**
   * @param baseItem
   */
  public TestItem (TestItem baseItem) {
    this.itemID = baseItem.itemID;
    this.groupID = baseItem.groupID;
    this.isActive = baseItem.isActive;
    this.position = baseItem.position;
    this.strandName = baseItem.strandName;
    this.isRequired = baseItem.isRequired;
    this.isFieldTest = baseItem.isFieldTest;
    this.b = baseItem.b;
    this.a = baseItem.a;
    this.c = baseItem.c;
    if(baseItem.irtModel != null)
    {
    	this.irtModel = baseItem.irtModel.toUpperCase ();
    }
    if (baseItem.bVector != null)
    {
      this.bVector = new double[baseItem.bVector.length];
      int i = 0;
      for (Double b : baseItem.bVector)
      {
        this.bVector[i++] = b;
      }
    }
    this.contentLevels = baseItem.contentLevels;
    this.dimensions = baseItem.dimensions; // TODO scorepoints, hasDimensions, segmentPosition
  }

  /**
   * @return the _scorePoints
   */
  public int getScorePoints () {
    return bVector.length;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo (Object rhs)
  {
    TestItem R = (TestItem) rhs;
    if (R.position == this.position)
      return 0;
    if (R.position > this.position)
      return -1;
    return 1;
  }

  // / <summary>
  // / For items in the selected group only, add the ID of every content level
  // this item classified on for pruning purposes.
  // / </summary>
  // / <param name="ID"></param>
  public void addContentLevel (String ID)
  {
    // Not all content levels will be relevant or even in the blueprint
    if (contentLevels == null) {
      contentLevels = new ArrayList<String>();
    }
    if (!contentLevels.contains (ID)) {
      contentLevels.add(ID);
    }
  }

  // / <summary>
  // / Is the item classified on the given content level?
  // / </summary>
  // / <param name="ID"></param>
  // / <returns></returns>
  public boolean hasContentlevel (String ID)
  {
    return contentLevels.contains(ID);
  }
  // for debug only
  public void dump()
  {
      String itemID = this.itemID;
      String strand = this.strandName;
      String IRT_Model = this.irtModel;
      double[] bVector = this.bVector;
      int formPosition = this.position;
      double irtA = this.a;
      double irtB = this.b;
      double irtC = this.c;
      Boolean isActive = this.isActive;
      Boolean isFieldTest = this.isFieldTest;
      Boolean isRequired = this.isRequired;
      //
      System.out.println ();
      _logger.info (String.format ("groupID: %s", this.groupID));
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
  }
  
  /*
   * Adaptive Algorithm2
   */ 
  /**
   * Constructor 
   */
  public TestItem()
  {            
  }
  
	public void initialize(DbResultRecord record) throws SQLException 
	{
		// Uses for AA_GetSegment2_SP()! Renamed fields: IRT_A -> irtA,...

		this.b = Double.parseDouble(record.<String> get("irtB"));
		float A = record.<Float> get("irtA");
		float C = record.<Float> get("irtC");
		this.a = new Double(A);
		this.c = new Double(C);
		
		itemID = record.<String> get("itemkey");
		groupID = record.<String> get("GID");

		position = record.<Integer> get("position");
		isActive = record.<Boolean> get("isActive");
		isRequired = record.<Boolean> get("isRequired");
		isFieldTest = record.<Boolean> get("isFieldTest");
		strandName = record.<String> get("strand");
		String strContentLevels = record.<String> get("clString");
		if (strContentLevels != null)
			contentLevels = Arrays.asList(strContentLevels.split("\\s*;\\s*"));
		
		irtModel = record.<String> get("irtModel");
		
		String bVectrs = record.<String> get("bVector");
	    String[] bv = bVectrs.split (DELIM);
	    this.bVector = new double[bv.length];
	    int i = 0;
	    for (String b : bv)
	    {
	    	try{
	    		this.bVector[i] = Double.parseDouble (b); 
	    		i++;
	    	}catch(Exception e)
	    	{
	    		_logger.info("Item ID = " + this.itemID + "; Group ID = " + this.groupID + "; IRT Model = " + this.irtModel);
	    		_logger.warn("Cannot parseDouble: " + b + ". " + e.getMessage());
	    		_logger.info("bVector[" + i + "] = 0.0.");
	    		this.bVector[i++] = 0.0;	    		
	    	}
	    }
	    
		dimensions = CreateDimensions(this.irtModel, this.a, this.b, bVectrs, this.c, 0.0);
	}

	/**
	 * Initialize dimension parameter entry collected from database
	 * 
	 * @param sDimensionName
	 * @param irtModel
	 * @param paramNum
	 * @param sParamName
	 * @param fParamValue
	 */
	public void initializeDimensionEntry(String sDimensionName,
			String irtModel, int paramNum, String sParamName, Double fParamValue) {

		if (sDimensionName == null || sDimensionName.isEmpty())
			sDimensionName = "OVERALL";

		Dimension dim = getDimByName(sDimensionName);
		if (dim == null)
		{
			dim = new Dimension();
			dim.isOverall = sDimensionName.equals("OVERALL");
			dim.name = "OVERALL";
			dimensions.add(dim);
		}
		dim.InitializeDimensionEntry(irtModel, paramNum, sParamName,
				fParamValue);
	}

	public boolean isActive() {
		
		return isActive;
	}
	
//	private Boolean containsName(String name)
//	{
//		Boolean ret = false;
//		for(Dimension dim: dimensions)
//		{
//			ret |= dim.name.equalsIgnoreCase(name);
//		}
//		return ret;
//	}
//	
	private Dimension getDimByName(String name)
	{
		Dimension ret = null;
		for(Dimension dim: dimensions)
		{
			if(dim.name.equalsIgnoreCase(name))
			{
			ret = dim ;
			break;
			}
		}
		return ret;
		
	}
    /// <summary>
    /// Not used for adaptive algorithm.  Used for FT and FF test items.
    /// TODO: we should probably get away from the IRT stats in tblSetOfAdminItems entirely.  Need to support them for now
    /// so that we don't have to touch the FT and FF sprocs/UDFs.
    /// </summary>
    /// <param name="irtModel"></param>
    /// <param name="a"></param>
    /// <param name="b"></param>
    /// <param name="bVector"></param>
    /// <param name="c"></param>
    /// <param name="abilityOffset"></param>
    /// <returns></returns>
    public List<Dimension> CreateDimensions(String irtModel, double a, double b, String bVector, double c, double abilityOffset)
    {
        List<Dimension> dims = new ArrayList<Dimension>();
        String[] tmpBV = bVector.split(DELIM);
        List<Double> bb = new ArrayList<Double>();
        for(String dm: tmpBV)
        {
        	bb.add(Double.parseDouble(dm));
        }
        Dimension dmm = createDimension(null, irtModel, a, b, bb, c, abilityOffset);
        dims.add(dmm);
        return dims;
    }

 // This is initializeDimensionEntry in TestItem 
    /// <summary>
    /// Creates a list of Dimensions for an item.
    /// </summary>
    /// <param name="segmentPosition">The segment that the item is currently appearing on.  An item may
    /// appear on 1 of multiple segments of a test, and we are handling the possibility that the IRT stats
    /// could vary for a given item depending on the segment, even though that is probably never going to happen.</param>
    /// <param name="itemKey"></param>
    /// <param name="abilityOffset"></param>
    /// <param name="itemParams"></param>
    /// <returns></returns>
//    public static List<Dimension> CreateDimensions(int segmentPosition, 
//    		String itemKey, double abilityOffset, DataTable itemParams)
    
    private Dimension createDimension(String name, String irtModel, Double a, Double b, List<Double> bVec, Double c, Double abilityOffset)
    {  
    	Dimension dim = new Dimension();
    	dim.IRTModelName = irtModel;
        dim.name = (name != null) ? name : OVERALL_DIM_NAME;
        dim.isOverall = dim.name.equalsIgnoreCase(OVERALL_DIM_NAME);
        
        double sum = 0.0;
        if(bVec != null)
        {
        	this.bVector = new double[bVec.size()];
	
	        for(int i = 0; i < bVec.size(); i++)
	        {
	        	this.bVector[i] = bVec.get(i);
	        	sum += bVec.get(i);
	        }

	        sum /= bVec.size();
        }
        dim.ParamA = a;
        dim.ParamC = c;
        dim.bVector = bVec;
        
        //dim.averageB = ((bVec == null || bVec.isEmpty())? b : (bVec.size() == 1 ? bVector[0] : sum);
        
        
        try {
			dim.irtModelInstance = IRTModel.CreateModel(irtModel, a, bVec, c);
		} catch (Exception e) {
			_logger.error("Cannot create intModel with name " + irtModel + ". Item = " + this.itemID + ". By default Model will be IRT3(1., 0., 0.)");
			try {
				dim.irtModelInstance = IRTModel.CreateModel("IRT3PL", 1., Arrays.asList(new Double(0.0)), 0.0);
			} catch (Exception e1) {
				_logger.error("Cannot create intModel with name IRT3PL");
			}
		}

        try {
			dim.setExpectedInfoIRTModel(IRTModel.CreateModel(irtModel, 1., bVec, c));
		} catch (Exception e) {
			_logger.error("Cannot create ExpectedInfoIRTModel with name " + irtModel + ". Item = " + this.itemID + ". By default Model will be IRT3(1., 0., 0.)");
			try {
				dim.setExpectedInfoIRTModel(IRTModel.CreateModel("IRT3PL", 1., Arrays.asList(new Double(0.0)), 0.0));
			} catch (Exception e1) {
				_logger.error("Cannot create intModel with name IRT3PL");
			}
		}
        
        return dim;
    }

    public void dumpAA2()
    {
        System.out.println (String.format ("groupID: %s", this.groupID));
        System.out.println (String.format ("itemID %s", this.itemID));
        System.out.println (String.format ("strand %s", this.strandName));
        System.out.println (String.format ("IRT Model %s", this.irtModel));
        System.out.println (String.format ("isActive %b", this.isActive));
        System.out.println (String.format ("isFieldTest %b", this.isFieldTest));
        System.out.println (String.format ("isRequired %b", this.isRequired));
        for(Dimension dm: this.dimensions)
        {
        	dm.dumpAA2();
        }        
    }
 // 
}

