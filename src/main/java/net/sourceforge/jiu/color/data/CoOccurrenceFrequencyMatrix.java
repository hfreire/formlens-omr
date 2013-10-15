/*
 * CoOccurrenceFrequencyMatrix
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

/**
 * An interface for a co-occurrence frequency matrix.
 * Also provides access to some statistical data.
 * This class is not a pure data type for it also demands a method {@link #computeStatistics}
 * which takes the matrix coefficients and computes mean, standard deviation and
 * other properties from it.
 * @author Marco Schmidt
 */
public interface CoOccurrenceFrequencyMatrix
{
	/**
	 * Sets all frequency values in this matrix to <code>0.0</code>.
	 */
	void clear();

	/**
	 * Computes mean, standard deviation and the sum of those two
	 * so that these values can be queried by the appropriate 
	 * get methods.
	 */
	void computeStatistics();

	/**
	 * Returns the sum of mean and standard deviation for all pairs (index, x), with x running from 0 to getDimension() - 1.
	 * The result is equal to {@link #getMean} + {@link #getStddev}
	 */ 
	double getScofMean();

	/**
	 * Returns the mean for all pairs (index, i), with i running from 0 to {@link #getDimension()} - 1.
	 */ 
	double getMean(int index);

	/**
	 * Returns the standard deviation of the values getValue(index, i)
	 * with i running from 0 to {@link #getDimension()} - 1.
	 * @param index first argument to all calls of getValue used to determine the standard deviation
	 */ 
	double getStddev(int index);

	/**
	 * Returns the standard deviation for all pairs (i, i), with i running from 0 to getDimension() - 1.
	 * @return standard deviation for pairs
	 */ 
	double getScofStddev();

	double getScofSum();

	/**
	 * Returns the dimension of this matrix.
	 */
	int getDimension();

	/**
	 * Returns the value for the self co-occurrence frequency of i (i being from
	 * 0 to {@link #getDimension()} - 1).
	 * The result is the same as a call to <code>getValue(i, i)</code>.
	 * @param i index into the matrix, must be larger than or equal to 0 and smaller than {@link #getDimension()}
	 */
	double getValue(int i);

	double getValue(int i, int j);

	void setValue(int i, int j, double newValue);
}
