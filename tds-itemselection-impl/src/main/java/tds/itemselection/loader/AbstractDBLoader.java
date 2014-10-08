/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.loader;

import java.sql.SQLException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import tds.dll.api.IItemSelectionDLL;
import tds.itemselection.base.ItemCandidatesData;
import AIR.Common.DB.DbComparator;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

public abstract class AbstractDBLoader implements IItemSelectionDBLoader {

	protected SQLConnection   connection;
	
	public SQLConnection getConnection() {
		return connection;
	}

	public void setConnection(SQLConnection connection) {
		this.connection = connection;
	}

	@Autowired
	IItemSelectionDLL iSelDLL = null;

	private static Logger _logger = LoggerFactory
			.getLogger(AbstractDBLoader.class);

	@Override
	public ItemCandidatesData getItemCandidates(UUID oppkey)
			throws ReturnStatusException, SQLException {
		ItemCandidatesData res = null;

		SingleDataResultSet result = iSelDLL.AA_GetNextItemCandidates_SP(
				connection, oppkey);
		DbResultRecord record = result.getCount() > 0 ? result.getRecords()
				.next() : null;
		if (record != null) {
			if (DbComparator.isEqual(
					record.<String> get(IItemSelectionDLL.ALGORITHM),
					IItemSelectionDLL.SATISFIED)) {
				_logger.info("This oppkey was selected previous time");
				res = new ItemCandidatesData(oppkey,
						IItemSelectionDLL.SATISFIED);
			} else {
				res = new ItemCandidatesData(
				record.<UUID> get(IItemSelectionDLL.OPPKEY),
				record.<String> get(IItemSelectionDLL.ALGORITHM),
				record.<String> get(IItemSelectionDLL.SEGMENTKEY),
				record.<String> get(IItemSelectionDLL.SEGMENTID),
				record.<Integer> get(IItemSelectionDLL.SEGMENT),
				record.<String> get(IItemSelectionDLL.GROUPID),
				record.<String> get(IItemSelectionDLL.BLOCKID),
				record.<UUID> get(IItemSelectionDLL.SESSION),
				record.<Boolean> get(IItemSelectionDLL.ISSIMULATION));
			}
		}
		return res;
	}
}
