/*
 * CoOccurrenceMatrix
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

/**
 * An interface for co-occurrence matrices.
 * An implementing class stores <code>int</code> counter values for pairs of pixels.
 * These counters represent the number of times two pixels are direct
 * neighbors in an image.
 * @author Marco Schmidt
 */
public interface CoOccurrenceMatrix
{
	/**
	 * Sets all counters to zero.
	 */
	void clear();

	/**
	 * Returns the dimension of this matrix.
	 * This is the number of rows and columns.
	 * @return matrix dimension (larger than zero)
	 */
	int getDimension();

	/**
	 * Returns the matrix value at a given position.
	 * @param i column index, from 0 to {@link #getDimension} - 1
	 * @param j row index, from 0 to {@link #getDimension} - 1
	 * @throws IllegalArgumentException for invalid index pairs (i, j)
	 */
	int getValue(int i, int j);

	/**
	 * Increases the counter for pair (i, j) by one.
	 * This method can be implemented by the call 
	 * <code>setValue(i, j, getValue(i, j) + 1);</code>.
	 * @param i column index, from 0 to {@link #getDimension} - 1
	 * @param j row index, from 0 to {@link #getDimension} - 1
	 * @throws IllegalArgumentException for invalid index pairs (i, j)
	 */
	void incValue(int i, int j);

	/**
	 * Sets the counter for pair (i, j) to a new value.
	 * @param i column index, from 0 to {@link #getDimension} - 1
	 * @param j row index, from 0 to {@link #getDimension} - 1
	 * @throws IllegalArgumentException for invalid index pairs (i, j)
	 */	 
	void setValue(int i, int j, int newValue);
}
