/*
 * Histogram3D
 *
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

/**
 * An interface for classes that store three-dimensional histograms.
 * Histograms count the occurrence of values, so a three-dimensional
 * histogram has counters for three-dimensional values.
 * The 3D histograms are used (as an example) to count the occurrence of 
 * each color in an RGB image.
 *
 * @author Marco Schmidt
 * @see Histogram1D
 */
public interface Histogram3D
{
	/**
	 * Sets all counters to zero.
	 */
	void clear();

	/**
	 * Returns the counter value of (index1, index2, index3).
	 * @param index1 first of the three values forming the threedimensional index
	 * @param index2 second of the three values forming the threedimensional index
	 * @param index3 three of the three values forming the threedimensional index
	 * @return the counter value of the desired index
	 * @throws IllegalArgumentException if the index formed by the arguments is invalid
	 */
	int getEntry(int index1, int index2, int index3);

	/**
	 * Returns the maximum index value for one of the three indexes.
	 * @throws IllegalArgumentException if the index formed by the arguments is invalid
	 */
	int getMaxValue(int index);

	/**
	 * Returns the number of used entries (those entries with
	 * a counter value larger than zero).
	 * @return number of non-zero counter values
	 */
	int getNumUsedEntries();

	/**
	 * Increases the counter value of (index1, index2, index3) by one.
	 * This method can be implemented by the one-liner
	 * <code>setEntry(index1, index2, index3, getEntry(index1, index2, index3) + 1);</code>
	 * However, implementations of this method may take advantage of
	 * implementation details to provide a more efficient approach.
	 * @param index1 first of the three values forming the threedimensional index
	 * @param index2 second of the three values forming the threedimensional index
	 * @param index3 three of the three values forming the threedimensional index
	 * @throws IllegalArgumentException if the index formed by the arguments is invalid
	 */
	void increaseEntry(int index1, int index2, int index3);

	/**
	 * Sets the counter value of (index1, index2, index3) to newValue.
	 *
	 * @param index1 first of the three values forming the threedimensional index
	 * @param index2 second of the three values forming the threedimensional index
	 * @param index3 three of the three values forming the threedimensional index
	 * @param newValue the counter value that is assigned to the argument index
	 * @throws IllegalArgumentException if the index formed by the first three arguments is invalid
	 */
	void setEntry(int index1, int index2, int index3, int newValue);
}
