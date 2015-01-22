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

/*
    /// <summary>
    /// Cumulative Distribution Function
    /// Copied from SIM; same as AM
    /// NOTE: there's a more precise version (though slower) in the Ordered Probit calc in REVISE, 
    ///     but this should be sufficient for our purposes.
    /// </summary>
 */
public class CDF {
    private static double Sqrt2PI = Math.sqrt(2 * Math.acos(-1.0));
    private static double SQRTH = 7.07106781186547524401E-1;
    private static double MAXLOG = 7.09782712893383996732E2;

    private double mean;
    private double stdDev;

    public CDF(double mean, double stDev)
    {
        this.mean = mean;
        this.stdDev = stDev;
    }

    public double Calculate(double x)
    {
        double z = (x - mean) / stdDev;
        return _CDF(z);
    }

    private double _PDF(double x)
    {
        double z = -x * x / 2;
        return Math.exp(z) / Sqrt2PI;
    }

    /// <summary>
    /// Returns the area under the Gaussian probability density function, integrated from minus infinity to a.
    ///
    /// Seems to be good to essentially 16 decimal places.
    /// From http://www.codeproject.com/KB/cs/SpecialFunction.aspx
    /// </summary>
    /// <param name="a"></param>
    /// <returns></returns>

    public static double _CDF(double a)
    {
        double x, y, z;

        x = a * SQRTH;
        z = Math.abs(x);

        if (z < SQRTH) y = 0.5 + 0.5 * erf(x);
        else
        {
            y = 0.5 * erfc(z);
            if (x > 0) y = 1.0 - y;
        }

        return y;
    }

    /// <summary>
    /// Returns the complementary error function of the specified number.
    /// </summary>
    /// <param name="a"></param>
    /// <returns></returns>
    private static double erfc(double a)
    {
        double x, y, z, p, q;

        double[] P = {
					 2.46196981473530512524E-10,
					 5.64189564831068821977E-1,
					 7.46321056442269912687E0,
					 4.86371970985681366614E1,
					 1.96520832956077098242E2,
					 5.26445194995477358631E2,
					 9.34528527171957607540E2,
					 1.02755188689515710272E3,
					 5.57535335369399327526E2
				 };
        double[] Q = {
					 //1.0
					 1.32281951154744992508E1,
					 8.67072140885989742329E1,
					 3.54937778887819891062E2,
					 9.75708501743205489753E2,
					 1.82390916687909736289E3,
					 2.24633760818710981792E3,
					 1.65666309194161350182E3,
					 5.57535340817727675546E2
				 };

        double[] R = {
					 5.64189583547755073984E-1,
					 1.27536670759978104416E0,
					 5.01905042251180477414E0,
					 6.16021097993053585195E0,
					 7.40974269950448939160E0,
					 2.97886665372100240670E0
				 };
        double[] S = {
					 //1.00000000000000000000E0, 
					 2.26052863220117276590E0,
					 9.39603524938001434673E0,
					 1.20489539808096656605E1,
					 1.70814450747565897222E1,
					 9.60896809063285878198E0,
					 3.36907645100081516050E0
				 };

        if (a < 0.0) x = -a;
        else x = a;

        if (x < 1.0) return 1.0 - erf(a);

        z = -a * a;

        if (z < -1 * MAXLOG)
        {
            if (a < 0) return (2.0);
            else return (0.0);
        }

        z = Math.exp(z);

        if (x < 8.0)
        {
            p = polevl(x, P, 8);
            q = p1evl(x, Q, 8);
        }
        else
        {
            p = polevl(x, R, 5);
            q = p1evl(x, S, 6);
        }

        y = (z * p) / q;

        if (a < 0) y = 2.0 - y;

        if (y == 0.0)
        {
            if (a < 0) return 2.0;
            else return (0.0);
        }

        return y;
    }


    /// <summary>
    /// Returns the error function of the specified number.
    /// </summary>
    /// <param name="x"></param>
    /// <returns></returns>
    public static double erf(double x)
    {
        double y, z;
        double[] T = {
					 9.60497373987051638749E0,
					 9.00260197203842689217E1,
					 2.23200534594684319226E3,
					 7.00332514112805075473E3,
					 5.55923013010394962768E4
				 };
        double[] U = {
					 //1.00000000000000000000E0,
					 3.35617141647503099647E1,
					 5.21357949780152679795E2,
					 4.59432382970980127987E3,
					 2.26290000613890934246E4,
					 4.92673942608635921086E4
				 };

        if (Math.abs(x) > 1.0) return (1.0 - erfc(x));
        z = x * x;
        y = x * polevl(z, T, 4) / p1evl(z, U, 5);
        return y;
    }


    /// <summary>
    /// Evaluates polynomial of degree N
    /// </summary>
    /// <param name="x"></param>
    /// <param name="coef"></param>
    /// <param name="N"></param>
    /// <returns></returns>
    private static double polevl(double x, double[] coef, int N)
    {
        double ans;

        ans = coef[0];

        for (int i = 1; i <= N; i++)
        {
            ans = ans * x + coef[i];
        }

        return ans;
    }

    /// <summary>
    /// Evaluates polynomial of degree N with assumtion that coef[N] = 1.0
    /// </summary>
    /// <param name="x"></param>
    /// <param name="coef"></param>
    /// <param name="N"></param>
    /// <returns></returns>		
    private static double p1evl(double x, double[] coef, int N)
    {
        double ans;

        ans = x + coef[0];

        for (int i = 1; i < N; i++)
        {
            ans = ans * x + coef[i];
        }

        return ans;
    }
}
/* There was 1st variant
     /// <summary>
    /// Cumulative Distribution Function
    /// Copied from SIM; same as AM
    /// TODO: verify with Paul.  Are these magic numbers constants in nature?
    /// </summary>
    public class CDF
    {
        private static double Sqrt2PI = Math.Sqrt(2 * Math.Acos(-1.0));

        private double mean;
        private double stdDev;

        public CDF(double mean, double stDev)
        {
            this.mean = mean;
            this.stdDev = stDev;
        }

        public double Calculate(double x)
        {
            double z = (x - mean) / stdDev;
            return _CDF(z);
        }

        private double _CDF(double z)
        {
            double t = 1.0 / (1.0 + .33267 * Math.Abs(z));

            if (z > 0) return 1.0 - _PDF(z) * (.4361836 * t + -.1201676 * t * t + .9372980 * t * t * t);
            return _PDF(z) * (.4361836 * t + -.1201676 * t * t + .9372980 * t * t * t);
        }

        private double _PDF(double x)
        {
            double z = -x * x / 2;
            return Math.Exp(z) / Sqrt2PI;
        }
    }

 } 
*/