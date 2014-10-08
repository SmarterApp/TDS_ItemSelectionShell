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
/// This class represents a single instance of a bstar equation. 
/// It may be used repetitively by Initializing and then solving for each collection of strand values (thetak, cutk, gammak)
/// See BstarFunction 
/// </summary>

// TODO: (AK) rename in AAMathBStarEquation later
public class BStarEquation extends EquationSolver
{

  /* (non-Javadoc)
   * @see com.air.ItemSelection.EquationSolver#Evaluate(double)
   */

  double theta;
  double thetaK;
  double cutK;
  double gammaK;
  double b;



  public void initialize(double theta, double thetaK, double cutK, double gammaK)
  {
      this.theta = theta;
      this.thetaK = thetaK;
      this.cutK = cutK;
      this.gammaK = gammaK;
  }

  public double evaluate(double b)
  {
      this.b = b;
      double dLambda = this.dLambdaDb();
      double dU = this.dudb();
      double result = dU + this.gammaK * dLambda;
      return result;
  }

  private double dudb()
  {
      double ptheta = this.probCorrect(this.theta, this.b);
      double result = ptheta * (1 - ptheta) * (2 * ptheta - 1);
      return result;
  }

  private double dLambdaDb()
  {
      double pthetaK = this.probCorrect(this.thetaK, this.b);
      double pcutK = this.probCorrect(this.cutK, this.b);
      double term1 = 2 * pthetaK * (1 - pthetaK) * (this.cutK - this.thetaK);
      double term2 = 2 * pthetaK - 2 * pcutK;
      return term1 + term2;
  }

  private double probCorrect(double theta, double b)
  {
      return 1.0 / (1.0 + Math.exp(b - theta));
  }
}
