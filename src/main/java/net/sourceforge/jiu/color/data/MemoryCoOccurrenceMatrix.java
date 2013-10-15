/*
 * MemoryCoOccurrenceMatrix
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

import net.sourceforge.jiu.color.data.CoOccurrenceMatrix;

/**
 * This class stores a co-occurrence matrix, a two-dimensional array of int counters.
 * The dimension is given to the constructor which allocates a corresponding array.
 * <h2>Caveat</h2>
 * Does not (realistically) work with 16 bit channels because it allocates
 * dimension times dimension int values, resulting in an attempt to allocate 16 GB
 * with 16 bit images (dimension=65,536). TODO: Implement more sophisticated class,
 * creating counters on-demand.  
 * @author Marco Schmidt
 */
public class MemoryCoOccurrenceMatrix implements CoOccurrenceMatrix
{
	private final int dimension;
	private final int dimSquare;
	private final int[] data;

	/**
	 * Creates a new matrix that stores dimension times dimension int values in memory.
	 * Given that array index values are of type int, this limits dimension to about 46000
	 * (sqrt(Integer.MAX_VALUE).
	 * In practice, dimension leads to dimension times dimenstion times 4 bytes being
	 * allocated, so that memory available to the JVM may become a decisive factor.  
	 * @param dimension the matrix' dimension, which is both the number of rows and columns
	 */
	public MemoryCoOccurrenceMatrix(int dimension)
	{
		if (dimension < 1)
		{
			throw new IllegalArgumentException("Dimension of co-occurrence matrix must be >= 1.");
		}
		this.dimension = dimension;
		long longDimSquare = (long)dimension * (long)dimension;
		if (longDimSquare > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException("Dimension " + dimension + " leads to an array exceeding the maximum size of 2^31 entries.");
		}
		dimSquare = dimension * dimension;
		data = new int[dimSquare];
	}

	public void clear()
	{
		for (int i = 0; i < dimSquare; i++)
		{
			data[i] = 0;
		}
	}

	public int getDimension()
	{
		return dimension;
	}


	public int getValue(int i, int j)
	{
		if (i < 0 || i >= dimension || j < 0 || j >= dimension)
		{
			throw new IllegalArgumentException("co-occ matrix i/j arguments out of bounds: " + i + "/" + j);
		}
		return data[j * dimension + i];
	}

	public void incValue(int i, int j) throws IllegalArgumentException
	{
		data[j * dimension + i]++;
	}

	public void setValue(int i, int j, int newValue)
	{
		if (i < 0 || i >= dimension || j < 0 || j >= dimension)
		{
			throw new IllegalArgumentException("co-occ matrix setValue, i/j coordinate out of bounds: " + i + "/" + j);
		}
		data[j * dimension + i] = newValue;
	}
}
