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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileComparison {
	
	private static Logger _logger = LoggerFactory.getLogger (FileComparison.class);
	public static Object ls = System.getProperties().get("line.separator");
	public static String csvDelimeter = ", ";

	public static Map<String, String>  mapFromFile(String path) throws IllegalArgumentException, IllegalAccessException
	{
		//String path  = "C:\\temp\\Blueprint.csv";
		Map<String, String> result = new HashMap<String, String>();
		try {
		   Reader reader = new InputStreamReader( new FileInputStream(path),"UTF-8");
		   BufferedReader fin = new BufferedReader(reader);
		   
		   String s;
		   String r;
		   String key = null;
		   while ((s=fin.readLine())!=null) {
			   String [] nameValue = s.split(csvDelimeter);
			   if(nameValue == null || nameValue.length < 1)
				   continue;
			   
			   int dim = nameValue.length;
			   key = nameValue[0].toLowerCase();
			   
			   if(dim  == 1)
			   {
				   result.put(key, "");
			   }
			   else
			   {
				   StringBuilder strBld = new StringBuilder(); 
					 for(int i = 1; i < dim; i++)
					 {
						 if(i < dim-1){
							 strBld.append(nameValue[i]).append(csvDelimeter); 
						 } else {
							 strBld.append(nameValue[i]); 
						 }
					 }
					 result.put(key, strBld.toString());
			   }
		   }

		            //Remember to call close. 
		            //calling close on a BufferedReader/BufferedWriter 
		            // will automatically call close on its underlying stream 
		   fin.close();

		} catch (IOException e) {
		   e.printStackTrace();
		} finally
		{
			
		}
		return result;
	}
	
	public static Map<String, Map<String, String>>  mapMapFromFile(String path) throws IllegalArgumentException, IllegalAccessException
	{
		//String path  = "C:\\temp\\Blueprint.csv";
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		try {
		   Reader reader = new InputStreamReader( new FileInputStream(path),"UTF-8");
		   BufferedReader fin = new BufferedReader(reader);
		   
		   String s;
		   String r;
		   String id = null;
		   String idVal = null;
		   String [] nameValues = new String [1];
		   if(((s=fin.readLine()) != null))
		   {
			   nameValues = s.split(csvDelimeter);
			   id = nameValues[0].toLowerCase();
		   }
		   while ((s=fin.readLine())!=null) {
			   String [] valValues = s.split(csvDelimeter);
			   if(valValues == null || valValues.length < 1)
				   continue;
			   
			   int dim = valValues.length;
			   idVal = valValues[0].toLowerCase();
			   Map<String, String> values = new HashMap<String, String>();
			   
			   if(dim  == 1)
			   {
				   values.put(id, idVal);
				   result.put(idVal, values);
			   }
			   else
			   {
					 for(int i = 1; i < dim; i++)
					 {
						 values.put(nameValues[i], valValues[i]);
					 }
					 result.put(idVal, values);
			   }
		   }

		            //Remember to call close. 
		            //calling close on a BufferedReader/BufferedWriter 
		            // will automatically call close on its underlying stream 
		   fin.close();

		} catch (IOException e) {
		   e.printStackTrace();
		} finally
		{
			
		}
		return result;
	}
	//
	public static double compare(String path, Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		Map<String, String> fileRes = mapFromFile(path);
		String fieldName = null;
		String fieldValue = null;
		String mapValue = null;
		Boolean res = false;
		double ret = 0.;
		double sum = 0.;
		for (Field field : obj.getClass().getDeclaredFields ())
		{
	        if (!field.isAccessible ()) {
		          field.setAccessible (true);
	        }
	        fieldName = field.getName();
	        fieldValue = (field.get(obj) != null)?field.get(obj).toString(): null;
	        mapValue = fileRes.get(fieldName.toLowerCase());
	        
	        if(fieldValue == null && mapValue ==  null)
	        	res = true;
	        else if(fieldValue == null || mapValue ==  null)
	        	res = false;
	        else 
	        	res = fieldValue.equalsIgnoreCase(mapValue);
	        System.out.println(fieldName + ": " + mapValue + "; " + fieldValue);
	        
	        sum++;
	        if(res)
	        	ret++;
	        
	        //Assert.assertTrue(res);
		}
		return ret/sum;
	}
	
	public static double compare2(String path, Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		if(obj != null)
		{
			Map<String, String> fileRes = mapFromFile(path);
			Map<String, String> objectRes = new HashMap<String, String>();
			String fieldName = null;
			String fieldValue = null;
			String mapValue = null;
			Boolean res = false;
			double ret = 0.;
			double sum = 0.;
			for (Field field : obj.getClass().getDeclaredFields ())
			{
		        if (!field.isAccessible ()) {
			          field.setAccessible (true);
		        }
		        fieldName = field.getName();
		        fieldValue = (field.get(obj) != null)?field.get(obj).toString(): null;
		        objectRes.put(fieldName.toLowerCase(), fieldValue);
			}
			for(String mapName: fileRes.keySet())
			{
		        mapValue = fileRes.get(mapName);
		        fieldValue = objectRes.get(mapName);
		        
		        if(fieldValue == null && mapValue ==  null)
		        	res = true;
		        else if(fieldValue == null || mapValue ==  null)
		        	res = false;
		        else 
		        {
		        	res = fieldValue.equalsIgnoreCase(mapValue);
		        	System.out.println(mapName + ": " + mapValue + "; " + fieldValue);
		        }
		        
		        sum++;
		        if(res)
		        	ret++;
		        
			}
			return ret/sum * 100.;
		}
		else
		{
        	System.out.println("Object is null");
			return 0.0;
		}
	}
	//Map<String, List<String>>  mapListFromFile(String path)
	public static double compare3(String path, Object objs, String id) throws IllegalArgumentException, IllegalAccessException
	{
		try{
			if((objs != null) && ((objs instanceof List) || (objs instanceof Collection)))
			{
				Collection<Object> objList = (Collection<Object>) objs;
		
				Map<String, Map<String, String>> fileRes = mapMapFromFile(path);
				String fieldName 	= null;
				String fieldValue 	= null;
				String mapValue 	= null;
				Boolean res 		= false;
				double ret 			= 0.;
				double sum 			= 0.;
				String idValue;
				for(Object obj: objList)
				{
					Map<String, String> objValues = new HashMap<String, String>();
					for (Field field : obj.getClass().getDeclaredFields ())
					{
				        if (!field.isAccessible ()) {
					          field.setAccessible (true);
				        }
				        fieldName = field.getName();
				        fieldValue = (field.get(obj) != null)?field.get(obj).toString(): null;
				        objValues.put(fieldName.toLowerCase(), fieldValue);
					}
					Field fieldId = null;
					try {
						fieldId = obj.getClass().getField(id);
						idValue = (fieldId.get(obj) != null)? fieldId.get(obj).toString(): null;
						String idd = idValue.toLowerCase();
						Map<String, String> fileResForId = fileRes.get(idd);
						for(String mapName: fileResForId.keySet())
						{
					        mapValue = fileResForId.get(mapName);
					        fieldValue = objValues.get(mapName.toLowerCase());
					        
					        if(fieldValue == null && mapValue ==  null)
					        	res = true;
					        else if(fieldValue == null || mapValue ==  null)
					        	res = false;
					        else 
					        {
					        	res = fieldValue.equalsIgnoreCase(mapValue);
					        	System.out.println(mapName + ": " + mapValue + "; " + fieldValue);
					        }
					        
					        sum++;
					        if(res)
					        	ret++;
					        
						}
					} catch (NoSuchFieldException | SecurityException e) {
						System.out.println("No field with name " + id);			}
				}
				return (sum == 0.)? 0.: ret/sum * 100.;
			}
			else
			{
	        	System.out.println("Object is null");
				return 0.0;
			}
		} catch(Exception e)
		{
			return 0.0;
		}
	}

}
