/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import tds.dll.api.ICommonDLL;
import tds.dll.api.IReportingDLL;
import AIR.Common.DB.SQLConnection;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (locations = "/test-context.xml")
public class TestReportingDLL1 {
	private static final Logger _logger = LoggerFactory
			.getLogger(TestReportingDLL1.class);

	@Autowired
	private IReportingDLL _irepDLL = null;

	@Autowired
	private ICommonDLL _commonDLL = null;

	@Autowired
	private DLLHelper myDllHelper = null;

	private SQLConnection _connection = null;
	private boolean _preexistingAutoCommitMode = true;
	private UUID oppkey = null;
	private boolean _debug = true;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

    try {
    	_connection = myDllHelper.getSQLConnection();
      _preexistingAutoCommitMode = _connection.getAutoCommit ();
      _connection.setAutoCommit (false);
    } catch (Exception e) {
      _logger.error ("Exception: " + e.getMessage () + "; " + e.toString ());
      throw e;
    }
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown () throws Exception {
    try {
      _connection.rollback ();
      _connection.setAutoCommit (_preexistingAutoCommitMode);
    } catch (Exception e) {
      _logger.error (String.format ("Failed rollback: %s", e.getMessage ()));
      throw e;
    } finally {
      _connection.setAutoCommit (_preexistingAutoCommitMode);
    }
  }

	@Test
	public final void test_XML_GetOppXML_New() throws Exception {

		List<UUID> oppkeys = new ArrayList<UUID>(Arrays.asList(//
				UUID.fromString("4fc53d12-a297-4882-aa4b-f58304d34187"), // main variant
				UUID.fromString("cd0852f3-5c6f-437e-86c9-dfa47c2f0bc4"),
				UUID.fromString("1eafa846-d189-443d-b693-01982b7e218e"),
				UUID.fromString("8ca26830-f999-434c-96c5-ac751dbf6c7c"), 
				UUID.fromString("90ba10fe-1631-41f4-92d8-2b3a00a72aac" // 
						)));
		BufferedWriter writer = null;
		int n = 0;
		long time = 0L;
		long allTime = 0L;
		String xmlFile = null;
		try {

			for (UUID oppkey : oppkeys) {
				String file = "C:\\Install\\springsource 3.3\\sts-3.3.0.RELEASE\\testlogs\\Tests\\TDSReport";
				file = file + oppkey.toString();
				file = file + ".xml";
				File logFile = new File(file);
				if (!logFile.getParentFile().exists()) {
					_logger.info("Creating directory: "
							+ logFile.getParentFile());

					boolean result = logFile.getParentFile().mkdirs();
					if (result) {
						_logger.info("DIR: " + logFile.getParentFile()
								+ "  created");
					}
				}

				long beginTime = System.currentTimeMillis();
				long endTime;

				String currentRes = _irepDLL.XML_GetOppXML_SP(_connection,
						oppkey, true);

				endTime = System.currentTimeMillis();
				time = endTime - beginTime;
				System.out.println("time = " + (time) + " ms");
				allTime += time;

				System.out.println(logFile.getCanonicalPath());
				writer = new BufferedWriter(new FileWriter(logFile));
				writer.write(currentRes);
				writer.close();
				n++;
			}
			System.out.println("time = " + (allTime) + " ms; " + n
					+ " XML reports. Average time per XML report: "
					+ (allTime / n) + " ms.");
		} catch (Exception e) {
			_logger.error("Exception: " + e.getMessage() + "; " + e.toString());
			throw e;
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	//@Test
	public final void test__XML_GetOpportunityItems_SP_2() throws Exception {
		UUID oppkey = UUID.fromString("4fc53d12-a297-4882-aa4b-f58304d34187");// 2014-07-28:

		try {
			BufferedWriter writer = null;
			long time;
			try {
				String file = "C:\\Install\\springsource 3.3\\sts-3.3.0.RELEASE\\testlogs\\Tests\\OppItems";
				file = file + oppkey.toString();
				file = file + ".xml";
				long beginTime = System.currentTimeMillis();
				long endTime;

				String currentRes = _irepDLL._XML_GetOpportunityItems_SP(_connection,
						oppkey, false);

				endTime = System.currentTimeMillis();
				time = endTime - beginTime;
				System.out.println("time = " + (time) + " ms");

				File logFile = new File(file);
				// System.out.println (logFile.getCanonicalPath ());
				writer = new BufferedWriter(new FileWriter(logFile));
				writer.write(currentRes);

			} catch (Exception e) {
				_logger.error("Exception: " + e.getMessage() + "; "
						+ e.toString());
				throw e;
			} finally {
				try {
					// Close the writer regardless of what happens...
					writer.close();
				} catch (Exception e) {
				}
			}

		} catch (Exception e) {
			_logger.error("Exception: " + e.getMessage() + "; " + e.toString());
			throw e;
		}
	}
	
	//@Test
	public final void test__XML_Input_Output() throws Exception {

		
		String file = "C:\\temp\\EugeniaLetter\\Letter1.txt";

	     BufferedReader bfr = new BufferedReader(new FileReader (file)); 
	     BufferedWriter writer = null;
	     StringBuffer output = new StringBuffer ();
	     String line = null;
	      while ((line = bfr.readLine ()) != null)
	      {
	        output.append (line);
	      }
	      bfr.close ();
	      File logFile = new File(file);
			// System.out.println (logFile.getCanonicalPath ());
			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(output.toString());

	}
	
	public final void test_Eugenia1()
	{
		// In/Out relatad Me!
		String fileIn  = "C:\\temp\\EugeniaLetter\\Letter1Out.txt";
		String fileOut = "C:\\temp\\EugeniaLetter\\Letter2In.txt";

		try {
			   Reader reader =
			      new InputStreamReader(
			         new FileInputStream(fileIn),"UTF-8");
			   BufferedReader fin = new BufferedReader(reader);
			   Writer writer =
			      new OutputStreamWriter(
			         new FileOutputStream(fileOut), "UTF-8");
			   // the same:
			   // Reader in  = new BufferedReader(new InputStreamReader ( new FileInputStream (infileIn   ), "UTF-8"));
			   // Writer out = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(outfilename), "UTF-8"));
			   BufferedWriter fout = new BufferedWriter(writer);
			   String s;
			   while ((s=fin.readLine())!=null) {
			      fout.write(s);
			      fout.newLine();
			   }

			            //Remember to call close. 
			            //calling close on a BufferedReader/BufferedWriter 
			            // will automatically call close on its underlying stream 
			   fin.close();
			   fout.close();
			} catch (IOException e) {
			   e.printStackTrace();
			} finally
			{
				
			}
	}
    private static void handleCharacters(Reader reader)
            throws IOException {
        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            System.out.println("Do something with " + ch);
        }
    }
}
