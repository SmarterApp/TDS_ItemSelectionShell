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

/// <summary>
/// This class is used to solve for a single solution to an equation of 1 variable using a simple bisection method.
/// It assumes that the equation has one and only one zero crossing within the interval given to SolveBisection.
/// It is currently only used by the bstar function.
/// </summary>

// TODO: (AK) rename it in AAMathEquationSolver later
public abstract class EquationSolver
{
  public double epsilon = 0.00001;

  /// <summary>
  /// Every derived class must implement the method Evaluate, which evaluates
  /// the equation at 'atvalue' and returns the result
  /// </summary>
  /// <param name="atvalue"></param>
  /// <returns></returns>
  // TODO: (AK) rename late to -> evaluate() and 
  public abstract double evaluate(double atvalue);

  /// <summary>
  /// Solve the equation (find a zero crossing) using values within [lb, ub]
  /// </summary>
  /// <param name="lb"></param>
  /// <param name="ub"></param>
  /// <returns></returns>
  public double solveBisection(double lb, double ub)
  {
      double mid, val;
      while (lb < ub)
      {
          mid = (ub + lb) / 2.0;
          val = this.evaluate(mid);
          if (Math.abs(val) < this.epsilon) return mid;
          if (val > 0.0) lb = mid;
          else ub = mid;
      }
      return -9999.0;
  }

}
