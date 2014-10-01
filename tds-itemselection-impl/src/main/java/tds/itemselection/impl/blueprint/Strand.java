/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.itemselection.impl.math.AAMath;


/**
 * @author akulakov
 * 
 */
public class Strand extends BpElement
{
  private static Logger  _logger  = LoggerFactory.getLogger (Strand.class);
	
  // <summary>
  // Strand is an element of the blueprint
  // which may be 'customized' to an individual examinee (!AK)
  // Some values are common to all tests administered, but others are for a
  // point in time for an individual
  // TODO: (AK)
  // AK: it means that these values need to separate on two(2) subsets
  // (subclasses)!
  // / </summary>

  // independent of examinee
  // the cut/ point for strands in adaptive ability match
  public double  adaptiveCut    = -9999.0;
  // for adaptive ability match
  public double  startInfo      = 0.2;
  public double  adaptiveWeight = 5.0;
  //
  // initial theta from this point in time
  private double _startAbility;

  /**
   * @return the _startAbility
   */
  public double getStartAbility () {
    return _startAbility;
  }

  /**
   * @param _startAbility
   *          the _startAbility to set
   */
  public void setStartAbility (double _startAbility) {
    this._startAbility = _startAbility;
  }

  // specific to examinee
  public double theta;
  public double lambda      = 0.00632; // lambda is an intermediate value
                                       // used for computing Phi*_kt
  double        minLambda   = 0.00632;
  public double info        = 0.2;

  // The following are computed by the adaptive algorithm in determining best
  // item to administer
  public double gamma;
  public double bstar;
  public double phistar;
  public double phidiff;

  public int    vectorIndex = -1;     // to be set by Blueprint when added
                                       // to strands vector

  // / <summary>
  // / This constructor for examinee-independent strand
  // / </summary>
  // / <param name="name"></param>
  // / <param name="minrequired"></param>
  // / <param name="maxrequired"></param>
  // / <param name="isStrict"></param>
  // / <param name="cut"></param>
  // / <param name="info"></param>
  // / <param name="startability"></param>
  // / <param name="scalar"></param>
  public Strand (String name,
      int minrequired,
      int maxrequired,
      double bpweight,
      boolean isStrict,
      double cut,
      double info,
      double startability,
      double scalar)
  {
    super (name, minrequired, maxrequired, isStrict, bpweight);
    this.adaptiveCut = cut;
    this.startInfo = this.info = info;

    this.theta = this._startAbility = startability;
    this.adaptiveWeight = scalar;
    this.isStrand = true;
  }

  // / <summary>
  // / Make a copy to use in examinee-specific thread context
  // / </summary>
  // / <returns></returns>
  public Strand copy ()
  {
    Strand s = new Strand (ID,
        minRequired,
        maxRequired,
        weight,
        isStrictMax,
        adaptiveCut,
        startInfo,
        _startAbility,
        adaptiveWeight);
    s.vectorIndex = this.vectorIndex;
    return s;
  }

  // / <summary>
  // / This to be used for examinee-specific (OLD VERSION)
  // / </summary>
  // / <param name="name"></param>
  // / <param name="minrequired"></param>
  // / <param name="maxrequired"></param>
  // / <param name="isStrict"></param>
  // / <param name="cut"></param>
  // / <param name="info"></param>
  // / <param name="startability"></param>
  // / <param name="scalar"></param>
  // / <param name="numAdministered"></param>
  public Strand (String name,
      int minrequired,
      int maxrequired,
      boolean isStrict,
      double cut,
      double info,
      double startability,
      double scalar,
      int numAdministered)

  {
    super (name, minrequired, maxrequired, isStrict, scalar);
    this.adaptiveCut = cut;
    this.startInfo = this.info = info;
    this._startAbility = this.theta = startability;
    this.numAdministered = numAdministered;
    this.isStrand = true;
  }

  // / <summary>
  // / Use this to incrementally update the OVERALL ability estimate or for
  // strand theta
  // / There is no lambda value for overall ability
  // / </summary>
  // / <param name="itemB"></param>
  // / <param name="itemScore"></param>
  public void UpdateAbility (double itemB, int itemScore)
  {
    double p = AAMath.probCorrect (this.theta, itemB);
    this.info += p * (1 - p);
    this.theta += (itemScore - p) / info;
    UpdateLambda (p, itemB, itemScore);
  }

  // / <summary>
  // / Lambda is the cumulative log likelihood ratio
  // /
  // / </summary>
  // / <param name="itemB"></param>
  // / <param name="itemScore"></param>;
  // / <param name="pOverall">Probability correct at overall level</param>
  private void UpdateLambda (double pTheta, double itemB, int itemScore)
  {
    double z = itemScore;
    double pCut = AAMath.probCorrect (this.adaptiveCut, itemB);
    double termTheta = 2 * (z * Math.log (pTheta) + (1 - z) * Math.log (1 - pTheta));
    double termCut = 2 * (z * Math.log (pCut) + (1 - z) * Math.log (1 - pCut));
    // floor version of lambda introduced 9/16/2008 by P. Sadegh and J. Cohen to
    // defeat possible divide-by-zero-lambda situations in late-stage of
    // adaptive algorithm computation
    this.lambda = Math.max (this.minLambda, this.lambda + termTheta - termCut);
  }
  /*
   * ABOUT LAMBDA
   * 
   * Legend: k is the strand, t is time or item position. 'i' (student) is
   * omitted as this formula is for a single student
   * 
   * From
   * "Adaptive Algorithm, August 13, 2008, 2.3 Measure of an item’s contribution to content strand discrimination"
   * 
   * The value measure seeks to administer the item that would best discriminate
   * between students who are above or below the cutscore. Whether the
   * examinee’s score is above, at, or below any given content strand cutscore
   * is determined through likelihood ratio test. The amount of information for
   * cutscore discrimination is maximized by maximizing the expected incremental
   * value of the log likelihood differences. v[k,t] = chi-square(1,Lambda[k,t]
   * + lambda[k,j,t]) - chi-square(1, Lambda[k,t])
   * 
   * where Lambda[k,t] = 2Log(L(z[t] | thetaEstimate[t]) / L(z[t] | theta[t])) ,
   * which is chi-square distributed likelihood ratio of test of the hypothesis
   * that the that examinee i’s vector of responses up until time t were
   * generated by someone with a proficiency that differs significantly from the
   * cutscore. The value function rewards items expected to will maximize this
   * value. The term is the expected contribution of item j to the log of the
   * likelihood ratio. This is given by the weighted average of the contribution
   * of the item if the examinee responds correctly, and the contribution given
   * an incorrect response. The weights are given by the estimated probabilities
   * given the current proficiency estimates based on the form of the IRT model.
   */
}
