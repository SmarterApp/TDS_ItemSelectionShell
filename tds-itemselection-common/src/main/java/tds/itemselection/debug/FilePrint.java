/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.debug;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePrint {
	
	private static Logger _logger = LoggerFactory.getLogger (FilePrint.class);
	public static Object ls = System.getProperties().get("line.separator");
	public static String csvDelimeter = ", ";
	
	// Write String to file with filepath = path
	public static void string2File(String path, String res)
	{
		try {
			File file = new File(path);
			if (!file.getParentFile().exists()) {
				System.out.println("Creating directory: " + file.getParentFile());

				boolean result = file.getParentFile().mkdirs();
				if (result) {
					System.out.println("DIR: " + file.getParentFile() + "  created");
				}
			}
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(res);
				writer.close();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	//
	// Write "fieldName, value"
	private static String oneField(String fieldName, String value)
	{
		StringBuilder stb = new StringBuilder();
		stb.append(fieldName).append(csvDelimeter).append(value).append(ls);
		return stb.toString();
	}
	// 
	//Write only name 
	private static String oneString2Line(String name)
	{
		StringBuilder stb = new StringBuilder();
		stb.append(name).append(ls);
		return stb.toString();
	}
	//
	// Write only fields with name from Collection fieldNames
	public static String fieldNamesValues(Collection<String> fieldNames, Object obj)
	{
		StringBuilder stb = new StringBuilder();
		for(String name: fieldNames)
		{
			try {
				Field[] fields = obj.getClass().getDeclaredFields();
				
				Field field = obj.getClass().getDeclaredField(name);
				field.setAccessible(true);
				Object value = field.get(obj);
				stb.append(oneField(name, value.toString()));
				
			} catch (NoSuchFieldException e) {				
				_logger.error(e.getMessage());
				stb.append(oneField(name, ""));
			} catch (SecurityException e) {
				_logger.error(e.getMessage());
				stb.append(oneField(name, ""));
			} catch (Exception e){
				_logger.error(e.getMessage());
				stb.append(oneField(name, ""));
			}
		}
		return stb.toString();
	}
	//
	//Write all fields of the object obj
	public static String fieldNamesValues(Object obj)
	{
		StringBuilder stb = new StringBuilder();
		Field[] fields = obj.getClass().getDeclaredFields();
		String name = null;
		for(Field field : fields)
		{
			try {
				name = field.getName();
				
				 if(!field.isAccessible())
					 field.setAccessible(true);
				 
				Object value = field.get(obj);
				stb.append(oneField(name, value.toString()));
				
			} catch (SecurityException e) {
				_logger.error(e.getMessage());
				stb.append(oneField(name, ""));
			} catch (Exception e){
				_logger.error(e.getMessage());
				stb.append(oneField(name, ""));
			}
		}
		return stb.toString();
	}
	//
	// Write only fields from Collection fieldNames
	public static String onlyValues(Collection<String> fieldNames, Object obj)
	{
		StringBuilder stb = new StringBuilder();
		String value = null;
		for(String name: fieldNames)
		{
			try {
				Field field = obj.getClass().getDeclaredField(name);
				 if(field == null)
					 continue;
				
				 if(!field.isAccessible())
					 field.setAccessible(true);
	 
				value = field.get(obj).toString();
				
				stb.append(oneString2Line(value));
			} catch (NoSuchFieldException e) {				
				_logger.error(e.getMessage());
				stb.append(oneString2Line(""));
			} catch (SecurityException e) {
				_logger.error(e.getMessage());
				stb.append(oneString2Line(""));
			} catch (Exception e){
				_logger.error(e.getMessage());
				stb.append(oneString2Line(""));
			}
		}
		return stb.toString();
	}
	//
	// Populate fields of the obj by values from file
	public static Object fillClass(Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		String fileIn  = "C:\\temp\\Blueprint.csv";

		try {
			   Reader reader =
			      new InputStreamReader(
			         new FileInputStream(fileIn),"UTF-8");
			   BufferedReader fin = new BufferedReader(reader);
			   Map<String, Object> namesValues = new HashMap<String,Object>();
			   String s;
			   String key;
			   while ((s=fin.readLine())!=null) {
				   String [] nameValue = s.split(csvDelimeter);
				   //namesValues.put(nameValue[0], nameValue[1]);
				  
				   if(nameValue == null || nameValue.length < 1)
					   continue;
				   
				   int dim = nameValue.length;
				   key = nameValue[0].toLowerCase();
				   
				   if(dim  == 1)
				   {
					   namesValues.put(key, null);
				   }
				   else
				   {
						 namesValues.put(key, nameValue[1]);
				   }

			   }

			            //Remember to call close. 
			            //calling close on a BufferedReader/BufferedWriter 
			            // will automatically call close on its underlying stream 
			   fin.close();
			   Field[] fields = obj.getClass().getDeclaredFields();
			   for(Field fl:  fields)
			   {
				   
				 String name = fl.getName();
				 if(!fl.isAccessible())
					 fl.setAccessible(true);
				 
				 Class<?> type = fl.getType();
				 if (type.isInstance(new Double(0.)))
				 {
					 Double value = new Double((Double)namesValues.get(name));
					 fl.setDouble(obj, value);
				 }
				 // TODO
			   }

			} catch (IOException e) {
			   e.printStackTrace();
			} finally
			{
				
			}
		return obj;
	}
	//
    public static String fieldsToString(Object obj) {
  	  StringBuilder result = new StringBuilder();
  	  String ls = System.getProperty("line.separator");
  	  result.append(ls).append( obj.getClass().getName() ).append( ":" ).append(ls);

  	  //determine fields declared in this class only (no fields of superclass)
  	  Field[] fields = obj.getClass().getDeclaredFields();

  	  //print field names paired with their values
  	  for ( Field field : fields  ) {
  	    try {
			 if(!field.isAccessible())
				 field.setAccessible(true);
			 
  	      result.append( field.getName() );
  	      result.append(csvDelimeter);
  	      //requires access to private field:
  	      result.append( field.get(obj).toString() );
  	      //result.append(field.toString());
  	    } catch ( Exception ex ) {
  	      System.out.println(ex);
  	    }
  	    result.append(ls);
  	  }
  	  return result.toString();
  	}



}
