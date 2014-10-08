/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *   
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.itemselection.impl.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RandomUnpruneOrder implements IUnpruneOrderStrategy {

	public <T> Collection<T> OrderCollection(Collection<T> collection) {
        List<T> shuffledCollection = new ArrayList<T>();
        for (T thing : collection)
            shuffledCollection.add(thing);

        Shuffle(shuffledCollection);
        return shuffledCollection;
	}
	
    private Random rand;
    public RandomUnpruneOrder(Random rand)
    {
        this.rand = rand;
    }



    protected <T> void Shuffle(List<T> shuffleMe)
    {
        if (shuffleMe.size() <= 1)
            return;
        for (int i = shuffleMe.size() - 1; i > 0; i--)
        {
            int j = rand.nextInt( i);
            SwapElements(shuffleMe, j, i);
        }
    }

    protected <T> void SwapElements(List<T> shuffleMe, int ixa, int ixb)
    {
        T temp = shuffleMe.get(ixa);
        shuffleMe.add(ixa, shuffleMe.get(ixb));
        shuffleMe.add(ixb, temp);
    }
}

