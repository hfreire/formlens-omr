/*
 * OnDemandHistogram3D
 *
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

import java.util.Hashtable;
import net.sourceforge.jiu.color.data.Histogram3D;

/**
 * Internal data class for the on demand histogram. Stores
 * one triplet of int values plus an int counter.
 * Implements {@link java.lang.Object#hashCode()} and
 * {@link java.lang.Object#equals(java.lang.Object)} to
 * be used with a hash table.
 *
 * @author Marco Schmidt
 */
final class Histogram3DNode
{
	private final int sample1;
	private final int sample2;
	private final int sample3;
	private int counter;
	
	public Histogram3DNode(int s1, int s2, int s3)
	{
		sample1 = s1;
		sample2 = s2;
		sample3 = s3;
	}

	public boolean equals(Object obj)
	{
		Histogram3DNode node = (Histogram3DNode)obj;
		return sample1 == node.sample1 && sample2 == node.sample2 &&
			sample3 == node.sample3;
	}

	public int getCounter()
	{
		return counter;
	}

	public int getSample1()
	{
		return sample1;
	}

	public int getSample2()
	{
		return sample2;
	}

	public int getSample3()
	{
		return sample3;
	}

	public int hashCode()
	{
		return sample1 + sample2 + sample3;
	}

	public void increase()
	{
		setCounter(getCounter() + 1);
	}

	public void setCounter(int newValue)
	{
		counter = newValue;
	}
}

/**
 * A data class for a three-dimensional histogram, creating counters on demand only,
 * not allocating counters for all possible entries at the beginning.
 * The creation on demand happens to save space.
 * <p>
 * <em>Note:</em>
 * Rewrote from scratch for version 0.15.0 to use hash tables
 * instead of int arrays. New version creates and
 * throws away a lot of objects, which had been a problem
 * with early JVMs but should be OK these days.
 *
 * @author Marco Schmidt
 */
public class OnDemandHistogram3D implements Histogram3D
{
	private Hashtable hash;
	private int numUniqueValues;
	private final int maxValue1;
	private final int maxValue2;
	private final int maxValue3;

	/**
	 * Creates a new histogram, internally creates the hash table
	 * for triplet values.
	 */
	public OnDemandHistogram3D(int max1, int max2, int max3)
	{
		if (max1 < 1 || max2 < 1 || max3 < 1)
		{
			throw new IllegalArgumentException("All max arguments must be 1 or larger.");
		}
		maxValue1 = max1;
		maxValue2 = max2;
		maxValue3 = max3;
		clear();
	}

	public void clear()
	{
		if (hash == null)
		{
			hash = new Hashtable();
		}
		else
		{
			hash.clear();
		}
		numUniqueValues = 0;
	}

	private Histogram3DNode createNode(int v1, int v2, int v3)
	{
		if (v1 >= 0 && v2 >= 0 && v3 >= 0 &&
			v1 <= maxValue1 && v2 <= maxValue2 && v3 <= maxValue3)
		{
			return new Histogram3DNode(v1, v2, v3);
		}
		else
		{
			throw new IllegalArgumentException("At least one of the arguments was not in its valid 0..max range.");
		}
	}

	public int getEntry(int index1, int index2, int index3)
	{
		Histogram3DNode searchNode = createNode(index1, index2, index3);
		Histogram3DNode counter = (Histogram3DNode)hash.get(searchNode);
		if (counter == null)
		{
			return 0;
		}
		else
		{
			return counter.getCounter();
		}
	}

	public int getMaxValue(int index) throws IllegalArgumentException
	{
		switch(index)
		{
			case(0): return maxValue1;
			case(1): return maxValue2;
			case(2): return maxValue3;
			default: throw new IllegalArgumentException("Not a valid index, must be from 0 to 2: " + index);
		}
	}

	public int getNumUsedEntries()
	{
		return numUniqueValues;
	}

	public void increaseEntry(int index1, int index2, int index3)
	{
		Histogram3DNode searchNode = createNode(index1, index2, index3);
		Histogram3DNode counter = (Histogram3DNode)hash.get(searchNode);
		if (counter == null)
		{
			searchNode.setCounter(1);
			hash.put(searchNode, searchNode);
			numUniqueValues++;
		}
		else
		{
			counter.increase();
		}
	}

	public void setEntry(int index1, int index2, int index3, int newValue)
	{
		Histogram3DNode searchNode = createNode(index1, index2, index3);
		Histogram3DNode counter = (Histogram3DNode)hash.get(searchNode);
		if (counter == null)
		{
			searchNode.setCounter(newValue);
			hash.put(searchNode, searchNode);
			numUniqueValues++;
		}
		else
		{
			counter.setCounter(newValue);
		}
	}
}
