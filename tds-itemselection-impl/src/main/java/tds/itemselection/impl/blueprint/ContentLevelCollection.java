/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.blueprint;

import java.util.HashSet;
import java.util.List;

public class ContentLevelCollection {

    public final static double NO_CLs = -9999;
    
    private HashSet<String> contentLevels;
    
    public boolean contains(String cl)
    {
    	return contentLevels.contains(cl);
    }
    
    public void add(String cl)
    {
    	if(!contentLevels.contains(cl))
    	{
    		contentLevels.add(cl);
    	}
    }

    public ContentLevelCollection()
    {
        contentLevels = new HashSet<String>();
    }
    public ContentLevelCollection(List<String> contentLevels)
    {
        this.contentLevels = new HashSet<String>(contentLevels);
    }

    /// <summary>
    /// Compute the bp-metric for this collection of content levels (incl strand and affinity groups)
    /// WRT the blueprint provided.
    /// </summary>
    /// <param name="bp">The blueprint to match against</param>
    /// <param name="recordNeutralContribution">If false, only count CLs that are at or over max, or that are under min.
    /// Previously from CSETGroup.ComputeBPMetric_Experimental, which wasn't being used.</param>
    public BpMetric computeBpMetric(Blueprint bp, boolean recordNeutralContribution)
    {
        double sum = 0.0;
        int count = 0;
        double tmp;

        for (String clID : contentLevels)
        {
            BpElement cl = bp.elements.getElementByID(clID);
            if (cl != null && cl.weight > 0)
            {
                ++count;
                // first check for max met or exceeded
                if (cl.maxRequired <= cl.numAdministered)
                {
                    tmp = cl.weight * (cl.maxRequired - cl.numAdministered - 1);
                    sum += tmp;
                }
                // next check for below min requirement (employ panic weight which increases at end of test approaches)
                else if (cl.numAdministered < cl.minRequired)
                {  // largest weight
                    tmp = (double)cl.numAdministered / (double)cl.minRequired;
                    tmp = 2.0 - tmp;
                    sum += cl.weight * bp.getPanicWeight() * tmp;  //panicWeight = m_it = T / (T - t).
                }
                // slide toward max with lesser weight 
                else if (recordNeutralContribution 
                    && cl.numAdministered >= cl.minRequired && cl.numAdministered < cl.maxRequired)
                {
                    tmp = (double)(cl.numAdministered - cl.minRequired) / (double)(cl.maxRequired - cl.minRequired);
                    tmp = 1.0 - tmp;
                    // FYI: this condition cannot hold if min == max
                    sum += cl.weight * tmp;
                }
            }
        }

        return new BpMetric(sum, count);
    }

//    #region ICollection<String> Members
//
//    public void Add(String item)
//    {
//        contentLevels.Add(item);
//    }
//
//    public void Clear()
//    {
//        contentLevels.Clear();
//    }
//
//    public boolean Contains(String item)
//    {
//        return contentLevels.Contains(item);
//    }
//
//    public void CopyTo(String[] array, int arrayIndex)
//    {
//        contentLevels.CopyTo(array, arrayIndex);
//    }
//
//    public int Count
//    {
//        get { return contentLevels.Count; }
//    }
//
//    public boolean IsReadOnly
//    {
//        get { return false; }
//    }
//
//    public boolean Remove(String item)
//    {
//        return contentLevels.Remove(item);
//    }
//
//    #endregion
//
//    #region IEnumerable<String> Members
//
//    public IEnumerator<String> GetEnumerator()
//    {
//        return contentLevels.GetEnumerator();
//    }
//
//    #endregion
//
//    #region IEnumerable Members
//
//    System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator()
//    {
//        return contentLevels.GetEnumerator();
//    }
//
//    #endregion


}
