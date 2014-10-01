/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;



import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.SQLConnection;

public class DLLHelper  extends AbstractDLL  {
	  @Autowired
	  @Qualifier ("applicationDataSource")
	  private DataSource dataSource = null;

	  public SQLConnection getSQLConnection () throws SQLException {
	    return new SQLConnection (dataSource.getConnection ());
	  }

}
