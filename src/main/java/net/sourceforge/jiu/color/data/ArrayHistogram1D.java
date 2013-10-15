/*
 * ArrayHistogram1D
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

import net.sourceforge.jiu.color.data.Histogram1D;

/**
 * A one-dimensional histogram data class that stores its counters in memory.
 * Counters are stored in an <code>int</code> array of length 
 * {@link #getMaxValue()}<code> + 1</code> so that <code>k</code>
 * values will require <code>k * 4</code> bytes.
 * @author Marco Schmidt
 */
public class ArrayHistogram1D implements Histogram1D
{
	private int[] data;

	/**
	 * Creates a histogram with the argument's number of values, from
	 * <code>0</code> to <code>numValues - 1</code>.
	 *
	 * @param numValues the number of counters in the histogram; must be one or larger
	 * @throws IllegalArgumentException if the argument is smaller than one
	 */
	public ArrayHistogram1D(int numValues)
	{
		if (numValues < 1)
		{
			throw new IllegalArgumentException("Must have at least one entry; numValues=" + numValues);
		}
		data = new int[numValues];
	}

	public void clear()
	{
		// OPTIMIZE
		// we could use java.util.Arrays.fill, but that would require Java 1.2+:
		// Arrays.fill(data, 0);
		for (int i = 0; i < data.length; i++)
		{
			data[i] = 0;
		}
	}

	public int getEntry(int index)
	{
		try
		{
			return data[index];
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Not a valid index: " + index);
		}
	}

	public int getMaxValue()
	{
		return data.length - 1;
	}

	public int getNumUsedEntries()
	{
		int result = 0;
		for (int i = 0; i < data.length; i++)
		{
			if (data[i] > 0)
			{
				result++;
			}
		}
		return result;
	}

	public void increaseEntry(int index)
	{
		try
		{
			data[index]++;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Not a valid index: " + index);
		}
	}

	public void setEntry(int index, int newValue)
	{
		try
		{
			data[index] = newValue;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Not a valid index: " + index);
		}
	}
}
