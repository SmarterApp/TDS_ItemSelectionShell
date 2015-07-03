/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

public class BpMetric {

	    public final double NO_CLs = -9999;  // no content levels found, this group can contribute nothing to blueprint satisfaction
	    public double Sum = 0.;
	    public int Count = 0;
	    public double Metric = 0.;

	    public double getSum() {
			return Sum;
		}

		private void setSum(double sum) {
			Sum = sum;
		}

		public int getCount() {
			return Count;
		}

		private void setCount(int count) {
			Count = count;
		}

		public double getMetric() {
			return Metric;
		}

		private void setMetric(double metric) {
			Metric = metric;
		}

		public boolean hasContentLevels() {
			return Metric != NO_CLs;
		}
		public BpMetric(double sum, int clCount)
	    {
	        this.Sum = sum;
	        this.Count = clCount;
	        this.Metric = clCount == 0 ? NO_CLs : sum / clCount;
	    }
	}
