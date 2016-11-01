/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import AIR.Common.DB.SQLConnection;
import tds.itemselection.api.IItemSelection;
import tds.itemselection.api.ItemSelectionException;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.loader.IItemSelectionDBLoader;

import java.util.List;

/**
 * @author akulakov
 * 
 */
public class FieldTestSelector  extends AbstractItemSelector  implements IItemSelection
{
  @Autowired
  @Qualifier("itemDBLoader")
  private IItemSelectionDBLoader loader = null;

  private static Logger  _logger  = LoggerFactory.getLogger (FieldTestSelector.class);

  public ItemGroup getNextItemGroup (SQLConnection connection, ItemCandidatesData itemCandidates) throws ItemSelectionException {

    final String messageTemplate = "Exception %1$s executing field test selection algorithm. Exception error: %2$s";

    ItemGroup result = null;
    try {
      result = loader.getItemGroup (connection, itemCandidates.getOppkey(), itemCandidates.getSegmentKey (), 
          itemCandidates.getGroupID (), itemCandidates.getBlockID (), true); 
    } catch (Exception e)
    {
      String error = String.format (messageTemplate, "Exception", e.getMessage());
      _logger.error (error);
      throw new ItemSelectionException(error);
    }
    return result;
  }

  @Override
  public ItemGroup getNextItemGroup(SQLConnection connection, ItemCandidatesData itemData, List<ItemGroup> itemGroups) throws ItemSelectionException {
    return getNextItemGroup(connection, itemData);
  }

}
