/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemgroupselection.measurementmodels;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class IRTModel3plTest {
	
	int _numThetaValues;  // Number of theta values for unit test 
	int _numScoreValues;  // Number of score values - For this model 2 (0 and 1) 
	int _numStdErrorValues; // Number of std. error values for unit test
	
	double _Theta[];   // Pre defined theta values
	double _Score[];   // Pre defined score values 
	double _StdError[];  // Pre defined std. errors

	double _pcInformation[]; // Information for each theta
	double _pcExpectedInformation[][]; // Information for each theta, std. error
	double _pcExpectedScore[]; // Expected score for each theta
	double _pcD1[][]; //First derivative for each score, theta
	double _pcD2[][]; //Second derivative for each score, theta

	double _aParam;  // a Parameter value
	double _bParam;  // b Parameter value 
	double _cParam;  // c parameter value
	ArrayList<Double> _bParamVector; // Convenience for the expected form 
	
	IRTModel3pl _Model = null;
	double _fErrorMargin;
		
	
	@Before
    public void initObjects() throws Exception {
		_numThetaValues = 3; 
		_numStdErrorValues = 2; 
		_numScoreValues = 2; 
		_Theta = new double[_numThetaValues];
		_Score = new double[_numScoreValues]; 
		_StdError = new double[_numStdErrorValues];
		_pcInformation = new double[_numThetaValues];
		_pcExpectedInformation = new double[_numThetaValues][_numStdErrorValues];
		_pcExpectedScore = new double[_numThetaValues];
		_pcD1 = new double[_numScoreValues][_numThetaValues];
		_pcD2 = new double[_numScoreValues][_numThetaValues];
		_bParamVector = new ArrayList<Double>(); 
		
		_aParam = 0.5; // Slope
		_bParam = 0.05; // Difficulty  
		_cParam = 0.2; // Intercept
		_bParamVector.add(_bParam);
		_fErrorMargin = .00001;
			
		_Theta[0]=-5D; _Theta[1]= 0D; _Theta[2]=5D;
		_Score[0]=0; _Score[1]=1;
		_StdError[0]=0; _StdError[1]=.05;

		 // TODO: Need to pre-compute and set this values
		_pcInformation[0]=0.00392373787138677;_pcInformation[1]=0.0414851234447791;_pcInformation[2]=0.0140835628670621;
		_pcExpectedInformation[0][0]=0;_pcExpectedInformation[0][1]=0;
		_pcExpectedInformation[1][0]=0;_pcExpectedInformation[1][1]=0;
		_pcExpectedInformation[2][0]=0;_pcExpectedInformation[2][1]=0;
		_pcExpectedScore[0]=0.259299252152531;_pcExpectedScore[1]=0.595000260400392;_pcExpectedScore[2]=0.937896429954809;
		_pcD1[0][0]=-0.0370620325953316;_pcD1[0][1]=-0.246875162750245;_pcD1[0][2]=-0.461185268721756;
		_pcD1[1][0]=0.105869473329453;_pcD1[1][1]=0.168040895579067;_pcD1[1][2]=0.0305377552628617;
						
		_pcD2[0][0]=-0.0171574220375684;_pcD2[0][1]=-0.0624902353921625;_pcD2[0][2]=-0.0179007822749198;
		_pcD2[1][0]=0.0338789155388972;_pcD2[1][1]=-0.0271875416870396;_pcD2[1][2]=-0.0138308025991877;
			
		_Model = new IRTModel3pl(_aParam, _bParamVector, _cParam);
    }
	
	@Test
	public void testDifficulty()  throws Exception {	
		assertEquals(_bParam, _Model.Difficulty(), _fErrorMargin);
	}

	@Test
	public void testInformation()  throws Exception {	
		double calculatedInformation[] = new double[_numThetaValues];		
		for(int i=0; i < _numThetaValues; ++i)
			calculatedInformation[i] = _Model.Information(_Theta[i]);
		assertArrayEquals(_pcInformation, calculatedInformation, _fErrorMargin);
	}

	@Test
	public void testExpectedInformation()  throws Exception {	
		double pcExpectedInformation[] = new double [_numThetaValues * _numStdErrorValues];
		double calculatedExpectedInformation[] = new double[_numThetaValues *_numStdErrorValues];
		int k = 0;
		for(int i=0; i < _numThetaValues; ++i)
		{
			for(int j=0 ; j < _numStdErrorValues; ++j)
			{
				pcExpectedInformation[k]=_pcExpectedInformation[i][j];
				calculatedExpectedInformation[k] = _Model.ExpectedInformation(_Theta[i], _StdError[j]);
				k++;
			}
		}		
		assertArrayEquals(pcExpectedInformation, calculatedExpectedInformation, _fErrorMargin);
	}
	
	@Test
	public void testExpectedScore()  throws Exception {	
		double calculatedExpectedScore[] = new double[_numThetaValues];		
		for(int i=0; i < _numThetaValues; ++i)
			calculatedExpectedScore[i] = _Model.ExpectedScore(_Theta[i]);
		assertArrayEquals(_pcExpectedScore, calculatedExpectedScore, _fErrorMargin);
	}
	
	@Test
	public void testD1()  throws Exception {	
		double expectedD1[] = new double [_numScoreValues * _numThetaValues ];
		double calculatedD1[] = new double[_numScoreValues * _numThetaValues ];
		int k = 0;
		for(int i=0; i < _numScoreValues; ++i)
		{
			for(int j=0 ; j < _numThetaValues; ++j)
			{
				expectedD1[k]=_pcD1[i][j];
				calculatedD1[k] = _Model.D1LnlWrtTheta(_Score[i], _Theta[j]);
				k++;
			}
		}		
		assertArrayEquals(expectedD1, calculatedD1, _fErrorMargin);
	}

	@Test
	public void testD2()  throws Exception {	
		double expectedD2[] = new double [_numScoreValues * _numThetaValues ];
		double calculatedD2[] = new double[_numScoreValues * _numThetaValues ];
		int k = 0;
		for(int i=0; i < _numScoreValues; ++i)
		{
			for(int j=0 ; j < _numThetaValues; ++j)
			{
				expectedD2[k]=_pcD2[i][j];
				calculatedD2[k] = _Model.D2LnlWrtTheta(_Score[i], _Theta[j]);
				k++;
			}
		}		
		assertArrayEquals(expectedD2, calculatedD2, _fErrorMargin);
	}
		
	
	@Test(expected=Exception.class)
	public void test_NoDifficulty_ConstructorThrowsException() throws Exception {
		ArrayList<Double> bVector = new ArrayList<Double>(); // Add no b values 
	    IRTModel3pl model = new IRTModel3pl(_aParam, bVector, _cParam);
	}
	
	@Test(expected=Exception.class)
	public void test_MorethanOne_bParam_ConstructorThrowsException() throws Exception {
		ArrayList<Double> bVector = new ArrayList<Double>(); 
		bVector.add(0D);bVector.add(1D); // Add more than 1 b values
		IRTModel3pl model = new IRTModel3pl(_aParam, bVector, _cParam);
	}
}
