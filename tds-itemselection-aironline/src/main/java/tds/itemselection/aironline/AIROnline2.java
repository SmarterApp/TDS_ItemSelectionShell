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

import tds.itemselection.api.IAIROnline;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.loader.SegmentCollection2;
import tds.itemselection.msb.MsbAssessmentSelectionService;

public class AIROnline2  implements IAIROnline {
	
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
	 @Qualifier ("aa2Selector")
	 private IItemSelection aa2Selector;

	@Autowired
	private MsbAssessmentSelectionService msbAssessmentSelectionService;

	 private static Logger  _logger  = LoggerFactory.getLogger (AIROnline2.class);
	  
	 private boolean _debug = false;

	public ItemGroup getNextItemGroup(SQLConnection connection, UUID oppkey, boolean isMsb, _Ref<String> errorRef)
			throws ReturnStatusException {

	    ItemGroup result = null;
	    ItemCandidatesData itemCandidates = null;
	    IItemSelection selector = null;
	    String algorithm = null;

		try {

			if(isMsb) {
				SegmentCollection2 segmentCollection = SegmentCollection2.getInstance();
				itemCandidates = msbAssessmentSelectionService.selectFixedMsbSegment(connection, oppkey, segmentCollection);
			} else {
				itemCandidates = loader.getItemCandidates(connection, oppkey);
			}

			if (!itemCandidates.getIsSimulation()) {
				itemCandidates.setSession(null);
			}
			algorithm = itemCandidates.getAlgorithm();

			if (algorithm.equalsIgnoreCase("fixedform")) {
				result = ffSelector
						.getNextItemGroup(connection, itemCandidates);
			} else if (algorithm.equalsIgnoreCase("fieldtest")) {
				result = ftSelector
						.getNextItemGroup(connection, itemCandidates);
			} else if (algorithm.equalsIgnoreCase("adaptive")
					|| algorithm.equalsIgnoreCase("adaptive2")) {
				selector = aa2Selector;
				if (selector == null) {
					errorRef.set( String.format("Unsupported adaptive algorithm: %s", algorithm));
				} else {
					result = selector.getNextItemGroup(connection, itemCandidates);
					if (selector.getItemSelectorError() != null) {
						errorRef.set( selector.getItemSelectorError());
					} else if (result == null) {
						if (selector.isSegmentCompleted()) {
                            // this segment has been terminated based on configured conditions.
                            //  Call recursively in case there are more segments to administer.
                            //  Eventually we'll drop down into the SATISFIED case.
							result = getNextItemGroup(connection, oppkey, isMsb, errorRef);
						} else {
							errorRef.set("Adaptive item selection failed: Unknown error");
						}
					}
				}
			} else if (algorithm.equalsIgnoreCase("SATISFIED")) {
				errorRef.set("Test Complete");
			} else {
				errorRef.set(String.format("Unknown algorithm: %s", itemCandidates.getAlgorithm()));
			}

	      if(result != null)
	      {
 		      result.setSegmentID(itemCandidates.getSegmentID());
		      result.setSegmentKey(itemCandidates.getSegmentKey());
		      result.setSegmentPosition(new Long(itemCandidates.getSegmentPosition()));

	      }
	      else if (errorRef.get() == null) 
	      {
	    	  errorRef.set("Unable to select next itemgroup");
		      _logger.error (errorRef.get());  	  
	      }
	      	      
		} catch (Exception e) {
	    	errorRef.set( e.getMessage());
	    	//TODO insert into SystemErrors
			_logger.error(errorRef.get(), e);
		} 
		return result;
	}
}
