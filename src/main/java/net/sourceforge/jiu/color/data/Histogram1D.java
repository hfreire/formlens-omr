/*
 * Histogram1D
 *
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

/**
 * An interface for a one-dimensional histogram.
 *
 * @author Marco Schmidt
 * @see Histogram3D
 */
public interface Histogram1D
{
	/**
	 * Sets all counters to zero.
	 */
	void clear();

	/**
	 * Returns the counter value for the given index.
	 * @param index the zero-based index of the desired counter value
	 * @return the counter value
	 * @throws IllegalArgumentException if the argument is not a valid index
	 */
	int getEntry(int index);

	/**
	 * Returns the maximum allowed index.
	 * The minimum is always 0.
	 * @return the maximum index value
	 */
	int getMaxValue();

	/**
	 * Returns the number of used entries (those entries with
	 * a counter value larger than zero).
	 * @return number of non-zero counter values
	 */
	int getNumUsedEntries();

	/**
	 * Increases the counter value of the given index by one.
	 * Same semantics as
	 * <code>setEntry(index, getEntry(index) + 1);</code>
	 * @param index index into the histogram
	 * @throws IllegalArgumentException if the argument index is invalid
	 */
	void increaseEntry(int index);

	/**
	 * Sets one counter to a new value.
	 * @param index index of the counter to be changed
	 * @param newValue new value for that counter
	 * @throws IllegalArgumentException if the index is invalid
	 */
	void setEntry(int index, int newValue);
}
