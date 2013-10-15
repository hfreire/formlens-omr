/*
 * MemoryCoOccurrenceFrequencyMatrix
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

import net.sourceforge.jiu.color.data.BaseCoOccurrenceFrequencyMatrix;

/**
 * Implements the {@link CoOccurrenceFrequencyMatrix} interface by using a large array
 * of values in memory.
 * @author Marco Schmidt
 */
public class MemoryCoOccurrenceFrequencyMatrix extends BaseCoOccurrenceFrequencyMatrix
{
	/** will be initialized in constructor and never changed */
	private final int dimension;
	/** total number of values, equals dimension * dimension and data.length */
	private int numValues;
	/** co occurrence frequency values */
	private double[] data;

	/**
	 * Creates a co-occurrence frequency matrix of given dimension;
	 * allocates dimension times dimension double values for
	 * internal array;
	 * does not call clear() to set everything to zero, must be
	 * done by user (or automatically in init).
	 * Dimension should be number of colors in palette.
	 * @throws IllegalArgumentException if dimension is smaller than 1 
	 */
	public MemoryCoOccurrenceFrequencyMatrix(int dimension) 
	{
		if (dimension < 1)
		{
			throw new IllegalArgumentException("Dimension of co-occurrence frequency matrix must be >= 1.");
		}
		this.dimension = dimension;
		this.numValues = dimension * dimension;
		data = new double[numValues];
	}

	/**
	 * Sets all values of this matrix to zero.
	 */
	public void clear()
	{
		if (data == null)
		{
			return;
		}
		for (int i = 0; i < numValues; i++)
		{
			data[i] = 0.0;
		}
	}

	public int getDimension()
	{
		return dimension;
	}

	/**
	 * Returns the value of this matrix at row i, column i.
	 * Argument is zero-based, so make sure that
	 * 0 &lt;= i &lt; getDimension().
	 * Other values will raise an IllegalArgumentException.
	 * Simply calls getValue(i, i).
	 */
	public double getValue(int i) throws IllegalArgumentException
	{
		return getValue(i, i);
	}

	/**
	 * Returns the value of this matrix at row j, column i.
	 * Both arguments are zero-based, so make sure that
	 * 0 &lt;= i, j &lt; getDimension().
	 * Other values will raise an IllegalArgumentException.
	 */
	public double getValue(int i, int j) throws IllegalArgumentException
	{
		if (i < 0 || i >= dimension || j < 0 || j >= dimension)
		{
			throw new IllegalArgumentException(
				"i/j arguments out of bounds: " + i + "/" + j);
		}
		return data[j * dimension + i];
	}

	/**
	 * Sets value at row j, column i to newValue.
	 * Both arguments are zero-based, so make sure that
	 * 0 &lt;= i, j &lt; getDimension().
	 * Other values will raise an IllegalArgumentException.
	 */
	public void setValue(int i, int j, double newValue)
		throws IllegalArgumentException
	{
		if (i < 0 || i >= dimension || j < 0 || j >= dimension)
		{
			throw new IllegalArgumentException("i/j coordinate out of bounds: " + i + "/" + j);
		}
		data[j * dimension + i] = newValue;
	}
}
