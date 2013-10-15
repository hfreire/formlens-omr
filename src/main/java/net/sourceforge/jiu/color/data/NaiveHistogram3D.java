/*
 * NaiveHistogram3D
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

import net.sourceforge.jiu.color.data.Histogram3D;

/**
 * A class for a three-dimensional histogram that allocates one <code>int</code> value
 * per counter at construction time.
 * This means that a histogram with 8 bits for each channel will have
 * 2<sup>8 + 8 + 8</sup> = 2<sup>24</sup> = 16,777,216 int values,
 * making it 64 MB large.
 *
 * @author Marco Schmidt
 */
public class NaiveHistogram3D implements
	Histogram3D
{
	private int[][][] data;
	private int[] values;

	/**
	 * Creates a histogram 
	 */
	public NaiveHistogram3D(int numValuesLevel1, int numValuesLevel2, int numValuesLevel3) throws 
		IllegalArgumentException, 
		OutOfMemoryError
	{
		if (numValuesLevel1 < 1)
		{
			throw new IllegalArgumentException("The number of values for " +
				"level 1 must be at least 1; got " + numValuesLevel1);
		}
		if (numValuesLevel2 < 1)
		{
			throw new IllegalArgumentException("The number of values for " +
				"level 2 must be at least 1; got " + numValuesLevel2);
		}
		if (numValuesLevel3 < 1)
		{
			throw new IllegalArgumentException("The number of values for " +
				"level 3 must be at least 1; got " + numValuesLevel3);
		}
		values = new int[3];
		values[0] = numValuesLevel1;
		values[1] = numValuesLevel2;
		values[2] = numValuesLevel3;
		data = new int[values[0]][][];
		for (int i1 = 0; i1 < values[0]; i1++)
		{
			data[i1] = new int[values[1]][];
			for (int i2 = 0; i2 < values[1]; i1++)
			{
				data[i1][i2] = new int[values[2]];
			}
		}
		clear();
	}

	/**
	 * Creates a histogram with the same number of values for all three dimensions.
	 * Calls {@link NaiveHistogram3D (int, int, int)}, repeating <code>numValues</code>
	 * three times.
	 * @param numValues the number of levels for all three dimensions
	 */
	public NaiveHistogram3D(int numValues) throws IllegalArgumentException, OutOfMemoryError
	{
		this(numValues, numValues, numValues);
	}

	/**
	 * Sets all counters to zero.
	 */
	public void clear()
	{
		for (int i1 = 0; i1 < values[0]; i1++)
			for (int i2 = 0; i2 < values[1]; i2++)
				for (int i3 = 0; i3 < values[2]; i3++)
					data[i1][i2][i3] = 0;
	}

	/**
	 * Returns the counter value of (index1, index2, index3).
	 * @param index1 first of the three values forming the threedimensional index
	 * @param index2 second of the three values forming the threedimensional index
	 * @param index3 three of the three values forming the threedimensional index
	 * @return the counter value of the desired index
	 * @throws IllegalArgumentException could be (read: need not necessarily) be 
	 *  thrown if the index formed by the arguments is invalid
	 */
	public int getEntry(int index1, int index2, int index3) throws
		IllegalArgumentException
	{
		try
		{
			return data[index1][index2][index3];
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Not a valid index (" + index1 +
				", " + index2 + ", " + index3 + ")");
		}
	}

	public int getMaxValue(int index) throws 
		IllegalArgumentException
	{
		if (index >= 0 && index <= 2)
		{
			return values[index] - 1;
		}
		else
		{
			throw new IllegalArgumentException("The index argument must be " +
				"from 0 to 2; got " + index);
		}
	}

	/**
	 * Returns the number of used entries (those entries with
	 * a counter value larger than zero).
	 * @return number of non-zero counter values
	 */
	public int getNumUsedEntries()
	{
		int result = 0;
		for (int i1 = 0; i1 < values[0]; i1++)
			for (int i2 = 0; i2 < values[1]; i2++)
				for (int i3 = 0; i3 < values[2]; i3++)
					if (data[i1][i2][i3] > 0)
						result++;
		return result;
	}

	/**
	 * Increases the counter value of (index1, index2, index3) by one.
	 * This method can easily be implemented by the one-liner
	 * <code>setEntry(index1, index2, index3, getEntry(index1, index2, index3) + 1);</code>
	 * However, this method is expected to be faster in some contexts.
	 *
	 * @param index1 first of the three values forming the threedimensional index
	 * @param index2 second of the three values forming the threedimensional index
	 * @param index3 three of the three values forming the threedimensional index
	 * @throws IllegalArgumentException could be (read: need not necessarily) be 
	 *  thrown if the index formed by the arguments is invalid
	 */
	public void increaseEntry(int index1, int index2, int index3) throws IllegalArgumentException
	{
		try
		{
			data[index1][index2][index3]++;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Not a valid index (" + index1 +
				", " + index2 + ", " + index3 + ")");
		}
	}

	/**
	 * Sets the counter value of (index1, index2, index3) to newValue.
	 *
	 * @param index1 first of the three values forming the threedimensional index
	 * @param index2 second of the three values forming the threedimensional index
	 * @param index3 three of the three values forming the threedimensional index
	 * @param newValue the counter value that is assigned to the argument index
	 * @throws IllegalArgumentException could be (read: need not necessarily) be 
	 *  thrown if the index formed by the first three arguments is invalid
	 */
	public void setEntry(int index1, int index2, int index3, int newValue) throws IllegalArgumentException
	{
		try
		{
			data[index1][index2][index3] = newValue;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Not a valid index (" + index1 +
				", " + index2 + ", " + index3 + ")");
		}
	}
}
