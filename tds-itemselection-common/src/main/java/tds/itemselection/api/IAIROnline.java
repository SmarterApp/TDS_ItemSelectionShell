/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.api;

import AIR.Common.DB.SQLConnection;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import tds.itemselection.base.ItemGroup;

import java.util.UUID;

public interface IAIROnline {

	/**
	 * 
	 * @param connection
	 * @param oppkey The opportunity key
	 * @param isMsb Is this item group for a Multi-Stage Braille test?
	 * @param errorRef
	 * @return ItemGroup
	 * @throws ReturnStatusException
	 */
	ItemGroup getNextItemGroup (SQLConnection connection, UUID oppkey, boolean isMsb, _Ref<String> errorRef)  throws ReturnStatusException;

}
