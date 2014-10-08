/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
/**
 * (c) Copyright American Institutes for Research, unpublished work created 2008-2013
 *  All use, disclosure, and/or reproduction of this material is
 *  prohibited unless authorized in writing. All rights reserved.
 *
 *  Rights in this program belong to:
 *  American Institutes for Research.
 *  
 *  Code based on ScoringEngine C# project - Re-factored here 
 */

package tds.itemgroupselection.measurementmodels;

import java.util.*;
import java.io.*;

/**
 * Class to allow integration using Gauss-Hermite quadrature method
 * @author aphilip
 *
 */
public final class GaussHermiteQuadrature 
{
	// Degree 3
    private static Double[] abscissae3 = { 
    	-1.7320508075688772935, 0D, 1.7320508075688772935 
    };
    private static Double[] weights3 = { 
    	0.16666666666666666667, 0.66666666666666666667, 0.16666666666666666667 
    };

    // Degree 4
    private static Double[] abscissae4 = { 
    	-2.3344142183389772393, -0.74196378430272585765, 0.74196378430272585765, 2.3344142183389772393 
    };
    private static Double[] weights4 = { 
    	0.045875854768068491817, 0.45412414523193150818, 0.45412414523193150818, 0.045875854768068491817 
    };

    // Degree 5
    private static Double[] abscissae5 = { 
    	-2.8569700138728056542, -1.3556261799742658658, 0D, 1.3556261799742658658, 
    	2.8569700138728056542 
    };
    private static Double[] weights5 = { 
    	0.011257411327720688933, 0.22207592200561264440, 0.53333333333333333333, 0.22207592200561264440, 
    	0.011257411327720688933 
    };

    // Degree 7
    private static Double[] abscissae7 = {
    	-3.7504397177257422563, -2.3667594107345412886, -1.1544053947399681272, 0D, 
        1.1544053947399681272, 2.3667594107345412886, 3.7504397177257422563
    };
    private static Double[] weights7 = {
    	0.00054826885597221779162, 0.030757123967586497040, 0.24012317860501271374, 0.45714285714285714286, 
        0.24012317860501271374, 0.030757123967586497040, 0.00054826885597221779162
    };

    // Degree 20
    private static Double[] abscissae20 = {
    	-7.6190485416797582914, -6.5105901570136544864, -5.5787388058932011527, -4.7345813340460553439, 
    	-3.9439673506573162603, -3.1890148165533894149, -2.4586636111723677513, -1.7452473208141267149, 
    	-1.0429453488027510315, -0.34696415708135592797, 0.34696415708135592797, 1.0429453488027510315, 
    	1.7452473208141267149, 2.4586636111723677513, 3.1890148165533894149, 3.9439673506573162603, 
    	4.7345813340460553439, 5.5787388058932011527, 6.5105901570136544864, 7.6190485416797582914
    };
    private static Double[] weights20 = {
    	1.2578006724379270154E-13, 2.4820623623151786456E-10, 6.1274902599829475405E-8, 4.4021210902308528331E-6, 
    	0.00012882627996192944940, 0.0018301031310804927956, 0.013997837447101003350, 0.061506372063976906552, 
    	0.16173933398399996172, 0.26079306344955485915, 0.26079306344955485915, 0.16173933398399996172, 
    	0.061506372063976906552, 0.013997837447101003350, 0.0018301031310804927956, 0.00012882627996192944940, 
    	4.4021210902308528331E-6, 6.1274902599829475405E-8, 2.4820623623151786456E-10, 1.2578006724379270154E-13
    };

    // Degree 21
    private static Double[] abscissae21 = {
    	-7.8493828951138219930, -6.7514447187174607668, -5.8293820073044713717, -4.9949639447820251929, 
    	-4.2143439816884213500, -3.4698466904753762952, -2.7505929810523730936, -2.0491024682571626618, 
    	-1.3597658232112302657, -0.67804569244064402621, 0D, 0.67804569244064402621, 
    	1.3597658232112302657, 2.0491024682571626618, 2.7505929810523730936, 3.4698466904753762952, 
    	4.2143439816884213500, 4.9949639447820251929, 5.8293820073044713717, 6.7514447187174607668, 
    	7.8493828951138219930
    };
    private static Double[] weights21 = {
    	2.0989912195656767434E-14, 4.9753686041217240051E-11, 1.4506612844930866107E-8, 1.2253548361482535298E-6, 
    	0.000042192347425516757386, 0.00070804779548153646931, 0.0064396970514087768692, 0.033952729786542835053, 
    	0.10839228562641944345, 0.21533371569505968660, 0.27026018357287707133, 0.21533371569505968660, 
    	0.10839228562641944345, 0.033952729786542835053, 0.0064396970514087768692, 0.00070804779548153646931, 
    	0.000042192347425516757386, 1.2253548361482535298E-6, 1.4506612844930866107E-8, 4.9753686041217240051E-11, 
    	2.0989912195656767434E-14
    };

    private static Map<Integer, Double[]> GaussHermitePolynomialWeights ; 
    private static Map<Integer, Double[]> GaussHermitePolynomialAbscissae;       
	private static Integer Degree = 5;    
	private static Double[] Weights; 
    private static Double[] Abscissae;       
    
    private static Properties config;	
    static {
    	GaussHermitePolynomialAbscissae = new HashMap<Integer, Double[]>();
    	GaussHermitePolynomialAbscissae.put(3, abscissae3);
    	GaussHermitePolynomialAbscissae.put(4, abscissae4);
    	GaussHermitePolynomialAbscissae.put(5, abscissae5);
    	GaussHermitePolynomialAbscissae.put(7, abscissae7);
    	GaussHermitePolynomialAbscissae.put(20, abscissae20);
    	GaussHermitePolynomialAbscissae.put(21, abscissae21);
    	
    	GaussHermitePolynomialWeights = new HashMap<Integer, Double[]>();
    	GaussHermitePolynomialWeights.put(3, weights3);
    	GaussHermitePolynomialWeights.put(4, weights4);
    	GaussHermitePolynomialWeights.put(5, weights5);
    	GaussHermitePolynomialWeights.put(7, weights7);
    	GaussHermitePolynomialWeights.put(20, weights20);
    	GaussHermitePolynomialWeights.put(21, weights21);
    	Properties fallback = new Properties();
    	fallback.put("Degree", Degree.toString());
    	config = new Properties(fallback);
    	try {
    		FileInputStream stream = new FileInputStream("config.properties");
    		try {
    			config.load(stream);
    			if (config.get("Degree") != null)
    				Degree = Integer.parseInt(config.get("Degree").toString());
    			Weights = GaussHermitePolynomialWeights.get(Degree);
    			Abscissae = GaussHermitePolynomialAbscissae.get(Degree);
    		}
    		finally {
    			 stream.close();
    		}
    	}
    	catch (IOException ex) {
			Weights = GaussHermitePolynomialWeights.get(Degree);
			Abscissae = GaussHermitePolynomialAbscissae.get(Degree);
    	}    	  
    }
    
	/**
     * Integrate using the passed function 
     * @param f
     * @return
     */
    public static double Integrate(IUnaryFunction f)
    {
        double sum = 0;
        for (int i = 0; i < Degree; i++)
            sum += Weights[i]*f.Apply(Abscissae[i]);
        return sum;
    }
    
}
