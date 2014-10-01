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
/// Stateless class for computations required by the adaptive algorithm
/// </summary>

// TODO: (AK) rename it in AAMathPhistarDiff
// and make static class and all methods?
public class PhistarDiff
{
  public PhistarDiff() { }
  //function y=p(b,t)
  //y=1./(1+exp(b-t));
  private double probCorrect(double b, double theta)
  {
      return 1.0 / (1.0 + Math.exp(b - theta));
  }

  //function y=pD1(b,t)
  //y=-exp(b-t)./(1+exp(b-t)).^2;
  private double pD1(double b, double theta)
  {
      double e = Math.exp(b - theta);
      double result = -e / ((1 + e) * (1 + e));  // TODO: (AK) Why "-" ? This is pD1(1-logit(t-b))
      return result;
  }

  //function y=pD2(b,t)
  //y=2*exp(2*b-2*t)./(1+exp(b-t)).^3 - exp(b-t)./(1+exp(b-t)).^2 ;
  private double pD2(double b, double theta)
  {
      double e = Math.exp(b - theta);
      double eplus1 = (1 + e);
      double esqrd = eplus1 * eplus1;
      double ecube = esqrd * eplus1;
      double e2 = Math.exp(2 * b - 2 * theta);
      double result = 2 * e2 / ecube - e / esqrd;  //  This is correct formula for pD2(logit(t-b))!     
      return result;
  }

  /// <summary>
  /// Offered undocumented because none was given to the programmer (L. Albright)
  /// However, LLT2ndDeriv likely translates to "2nd derivative of the Log Likelihood of Theta
  /// (with respect to b?)
  /// function y=LLT2ndDer(b,t1,t2)

  //P1 = p(b,t1);
  //P1p = pD1(b,t1);
  //P1pp = pD2(b,t1);
  //P2 = p(b,t2);
  //P2p = pD1(b,t2);
  //P2pp = pD2(b,t2);

  //y = (-(P2p*(2*P1p*(-1 + P2)*P2 + (P1 - 2*P1*P2 + P2^2)*P2p)) + (-1 + P2)*P2*(-P1 + P2)*P2pp)/((-1 + 

  //P2)^2*P2^2) + P1pp*(-log(1 - P2) + log(P2));
  /// </summary>
  /// <param name="b"></param>
  /// <param name="thetaOverall"></param>
  /// <param name="thetaStrand"></param>
  /// <returns></returns>
  private double LLT2ndDeriv(double b, double theta1, double theta2)
  {
      double p1 = this.probCorrect(b, theta1);
      double p1p = this.pD1(b, theta1);
      double p1pp = this.pD2(b, theta1);
      double p2 = this.probCorrect(b, theta2);
      double p2p = pD1(b, theta2);
      double p2pp = pD2(b, theta2);
      double log1 = Math.log(1 - p2);
      double log2 = Math.log(p2);

      double result =
          (-(p2p * (2 * p1p * (-1 + p2) * p2 + (p1 - 2 * p1 * p2 + p2 * p2) * p2p))
          + (-1 + p2) * p2 * (-p1 + p2) * p2pp) / ((-1 + p2) * (-1 + p2) * p2 * p2) 
          + p1pp * (-log1 + log2);
      /*
                  double term1a =
                      -1 * p2p * (2 * p1p * (-1 + p2) * p2 + (p1 - 2 * p1 * p2 + p2 * p2) * p2p);
                  double term1b =
                      ((-1 + p2) * p2 * (-p1 + p2) * p2pp);
                  double term1c = ((-1 + p2) * (-1 + p2) * p2 * p2);
                  double term1 = (term1a + term1b) / term1c;
                  double term2 = p1pp * (-log1 + log2);
      
                  double result2 = term1 + term2;
       */
      return result;
  }

  //        function y=boptSecondDerivativeFormula2(b,t0,tk,ts,L,w0,wk,I)

  //d2Info=(1/8)*(-2+cosh(b-t0))*sech((b-t0)/2)^4*w0/I;
  //y=exp(-L/2)*sqrt(2/pi)/sqrt(L)*wk*(LLT2ndDer(b,tk,tk)-LLT2ndDer(b,tk,ts))+d2Info;
  private double bopt2ndDeriv(double b, 
                              double thetaOverall, 
                              double thetaStrand, 
                              double thetaCut, 
                              double Lambda,
                              double overallWeight, 
                              double strandWeight, 
                              double Information)
  {
      double cosh = Math.cosh(b - thetaOverall);
      double s = (1.0 / Math.cosh((b - thetaOverall) / 2.0));     // C# math lib doesn't have hyperbolic secant, use this identity
      double sech_4 = s * s * s * s;
      double d2info = (1.0 / 8.0) * (-2 + cosh) * sech_4 * overallWeight / Information; // Why -2 + cosh ? It can be negative!
      // Why (1.0 / 8.0)? In sech_4 there is ((1.0 / 16.0)) already ?
      double f1a = Math.exp(-Lambda / 2);
      double f1b = Math.sqrt(2.0 / Math.PI);
      double f1c = Math.sqrt(Lambda);
      double f1 = f1a * f1b / f1c;  // (AK) f1*'s correspond to normal distribution. (Is it correct?)
      double f2 = this.LLT2ndDeriv(b, thetaStrand, thetaStrand) - this.LLT2ndDeriv(b, thetaStrand, thetaCut);
      double result = f1 * strandWeight * f2 + d2info;
      return result;
  }

  

  /// <summary>
  /// Use this to bypass member variable argument vectors
  /// </summary>
  /// <param name="b"></param>
  /// <param name="cut"></param>
  /// <param name="theta"></param>
  /// <param name="thetaK"></param>
  /// <param name="lambdaK"></param>
  /// <param name="weight"></param>
  /// <param name="weightK"></param>
  /// <param name="infoK"></param>
  /// <returns></returns>
  public double Evaluate( double b, 
                          double cut,         // = thetaCut
                          double theta,       // = thetaOverall
                          double thetaK,      // = thetaStrand
                          double lambdaK, 
                          double weight,      // = overallWeight
                          double weightK,     // = strandWeight
                          double infoK)
  {
      return this.bopt2ndDeriv(b, theta, thetaK, cut, lambdaK, weight, weightK, infoK);
  }

}
