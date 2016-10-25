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
import java.util.ArrayList;
import java.util.Iterator;
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

	
	@Autowired
	IItemSelectionDLL iSelDLL = null;

	private static Logger _logger = LoggerFactory
			.getLogger(AbstractDBLoader.class);

	@Override
	public ItemCandidatesData getItemCandidates(SQLConnection connection, UUID oppkey)
			throws ReturnStatusException, SQLException {
		return getItemCandidates(connection, oppkey, false);
	}

	@Override
	public ItemCandidatesData getItemCandidates(SQLConnection connection, UUID oppkey, boolean isMsb)
			throws ReturnStatusException, SQLException {
		ItemCandidatesData res = null;

		SingleDataResultSet result = iSelDLL.AA_GetNextItemCandidates_SP(
				connection, oppkey, isMsb);
		DbResultRecord record = result.getCount() > 0 ? result.getRecords()
				.next() : null;
		return parseData(record, oppkey);
	}

	private ItemCandidatesData parseData(DbResultRecord record, UUID oppkey) {
		ItemCandidatesData itemCandidatesData = null;
		if (record != null) {
			if (DbComparator.isEqual(
					record.<String> get(IItemSelectionDLL.ALGORITHM),
					IItemSelectionDLL.SATISFIED)) {
				_logger.info("This oppkey was selected previous time");
				itemCandidatesData = new ItemCandidatesData(oppkey,
						IItemSelectionDLL.SATISFIED);
			} else {
				itemCandidatesData = new ItemCandidatesData(
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
		return itemCandidatesData;
	}

	@Override
	public ArrayList<ItemCandidatesData> getAllItemCandidates(SQLConnection connection, UUID oppkey, boolean isMsb)
			throws ReturnStatusException {
		ArrayList<ItemCandidatesData> itemCandidates = new ArrayList<>();
		SingleDataResultSet result = iSelDLL.AA_GetNextItemCandidates_SP(
				connection, oppkey, isMsb);
		Iterator<DbResultRecord> recordIterator = result.getRecords();
		while(recordIterator.hasNext()) {
			itemCandidates.add(parseData(recordIterator.next(), oppkey));
		}
		return itemCandidates;
	}
}
