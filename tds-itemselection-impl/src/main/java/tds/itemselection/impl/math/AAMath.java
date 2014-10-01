/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.math;

/**
 * @author akulakov
 *
 */
public class AAMath // is this class singleton
{
  public static double probCorrect(double theta, double b)
  {
      return 1.0 / (1.0 + Math.exp(b - theta));
  }
  public static double SEfromInfo(double info)
  {
      if (info == 0.0)
          return 0.0;
      else
          return 1 / Math.sqrt(info);
  }

}
