/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.aironline;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.api.IAIROnline;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;

public class AIROnline2012 implements IAIROnline {
	
	 @Autowired
	 @Qualifier("aa2DBLoader")
	  private IItemSelectionDBLoader loader = null;

	 @Autowired
	 @Qualifier ("ffSelector")
	 private IItemSelection ffSelector;

	 @Autowired
	 @Qualifier ("ftSelector")
	 private IItemSelection ftSelector;

	 @Autowired
	 @Qualifier ("aaSelector")
	 private IItemSelection aaSelector;

	 @Autowired
	 @Qualifier ("aa2Selector")
	 private IItemSelection aa2Selector;

	  private static Logger  _logger  = LoggerFactory.getLogger (AIROnline2012.class);
	  
	  private boolean _debug = true;

	public ItemGroup getNextItemGroup(SQLConnection connection, UUID oppkey, _Ref<String> errorRef)
			throws ReturnStatusException {

	    ItemGroup result = null;
	    ItemCandidatesData itemCandidates = null;
	    IItemSelection selector = null;
	    String algorithm = null;
	    loader.setConnection (connection);

	    try {

	      itemCandidates = loader.getItemCandidates (oppkey);
	      
          if (!itemCandidates.getIsSimulation())
        	  itemCandidates.setSession(null);
          algorithm = itemCandidates.getAlgorithm();
        
          if(algorithm.equalsIgnoreCase("fixedform"))
          {
        	  selector = ffSelector;
          } else if(algorithm.equalsIgnoreCase("fieldtest"))
          {
        	  selector = ftSelector; 
          } else if(algorithm.equalsIgnoreCase("adaptive"))
          {
        	  selector = aaSelector; 
          }        		  
          else if(algorithm.equalsIgnoreCase("adaptive2") )
          {
        	  selector = aa2Selector; 
          } else if(algorithm.equalsIgnoreCase("SATISFIED"))
          {
        	  errorRef.set("Test Complete");
          } else
          {
        	  errorRef.set( String.format("Unknown algorithm: %s " , itemCandidates.getAlgorithm()));
          }

          if(selector != null)
          {
        	  result = selector.getNextItemGroup(connection, itemCandidates);
          }
          
	      if(result != null)
	      {
		      result.setSegmentID(itemCandidates.getSegmentID());
		      result.setSegmentKey(itemCandidates.getSegmentKey());
		      result.setSegmentPosition(new Long(itemCandidates.getSegmentPosition()));
	      }
	      else
	      {
	    	  errorRef.set("Unable to select next itemgroup");
		      _logger.error (errorRef.get());  	  
	      }
	      
		} catch (Exception e) {
	    	errorRef.set( e.getMessage());
	    	//TODO insert into SystemErrors
			_logger.error(errorRef.get());
		} 
	    
		return result;
	}
}
