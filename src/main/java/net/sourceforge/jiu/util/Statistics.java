/*
 * Statistics
 * 
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

/**
 * A number of static methods to compute statistical properties of an
 * array of double values.
 * Implements the computation of mean, variance and standard deviation
 * for <code>double</code> values.
 * @author Marco Schmidt
 * @since 0.11.0
 */
public class Statistics
{
	private Statistics()
	{
	}

	/**
	 * Computes the mean value for the argument array.
	 * Adds all values and divides them by the number of array elements.
	 * @param values double array on which the mean is to be determined
	 * @return computed mean value
	 * @throws IllegalArgumentException if the array has not at least one element
	 */
	public static double computeMean(double[] values)
	{
		return computeMean(values, 0, values.length);
	}

	/**
	 * Computes the mean value for some elements of the argument array.
	 * Adds all values and divides them by the number of array elements.
	 * @param values array from which elements are read
	 * @param offset index of the first element to be used
	 * @param number number of elements to be used
	 * @return computed mean value
	 * @throws IllegalArgumentException if the array has not at least one element
	 */
	public static double computeMean(double[] values, int offset, int number)
	{
		if (number < 1)
		{
			throw new IllegalArgumentException("The number of values to process must be one or larger.");
		}
		double sum = 0;
		final int UNTIL = offset + number;
		do
		{
			sum += values[offset++];
		}
		while (offset != UNTIL);
		return sum / number; 
	}

	/**
	 * Computes the standard deviation for the argument array of values.
	 * @param values array from which elements are read
	 * @return computed standard deviation
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeStandardDeviation(double[] values)
	{
		return computeStandardDeviation(values, 0, values.length);
	}

	/**
	 * Computes the standard deviation for the argument array of values.
	 * Reuses the mean value for that argument which must have been computed before.
	 * @param values array from which elements are read
	 * @param mean the mean value for the array, possibly computed with a 
	 *   call to {@link #computeMean(double[])}.
	 * @return computed standard deviation
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeStandardDeviation(double[] values, double mean)
	{
		return computeStandardDeviation(values, 0, values.length, mean);
	}

	/**
	 * Computes the standard deviation for some of the argument array's values.
	 * If you already have computed a mean value using {@link #computeMean(double[], int, int)},
	 * better call {@link #computeStandardDeviation(double[], int, int, double)}.
	 * Otherwise, this method has to compute mean again.
	 * @param values array from which elements are read
	 * @param offset first element to be used
	 * @param number number of elements used starting at values[offset]
	 * @return computed standard deviation
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeStandardDeviation(double[] values, int offset, int number)
	{
		double mean = computeMean(values, offset, number);
		return computeStandardDeviation(values, 0, values.length, mean);
	}

	/**
	 * Computes the standard deviation for some of the argument array's values.
	 * Use this version of the method if you already have a mean value,
	 * otherwise this method must be computed again.
	 * @param values array from which elements are read
	 * @param offset first element to be used
	 * @param number number of elements used starting at values[offset]
	 * @param mean value of the elements
	 * @return computed standard deviation
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeStandardDeviation(double[] values, int offset, int number, double mean)
	{
		return Math.sqrt(computeVariance(values, offset, number, mean));
	}

	/**
	 * Computes the variance for the argument array.
	 * @param values array from which elements are read
	 * @return variance for the array elements
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeVariance(final double[] values)
	{
		return computeVariance(values, 0, values.length);
	}

	/**
	 * Computes the variance for some of the argument array's values.
	 * @param values array from which elements are read
	 * @param mean the mean for the array elements
	 * @return variance for the array elements
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeVariance(final double[] values, final double mean)
	{
		return computeVariance(values, 0, values.length, mean);
	}

	/**
	 * Computes the variance for some of the argument array's values.
	 * If you already have computed a mean value using {@link #computeMean(double[], int, int)},
	 * better call {@link #computeVariance(double[], int, int, double)}.
	 * Otherwise, this method has to compute mean again.
	 * @param values array from which elements are read
	 * @param offset first element to be used
	 * @param number number of elements used starting at values[offset]
	 * @return computed variance
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeVariance(final double[] values, int offset, final int number)
	{
		double mean = computeMean(values, offset, number);
		return computeVariance(values, 0, values.length, mean);
	}

	/**
	 * Computes the variance for some of the argument array's values.
	 * Use this version of the method in case mean has already been
	 * computed.
	 * @param values array from which elements are read
	 * @param offset first element to be used
	 * @param number number of elements used starting at values[offset]
	 * @param mean the mean for the array elements
	 * @return computed variance
	 * @throws IllegalArgumentException if the array has not at least two elements
	 */
	public static double computeVariance(final double[] values, int offset, final int number, final double mean)
	{
		if (number < 2)
		{
			throw new IllegalArgumentException("The number of values to process must be two or larger.");
		}
		double sum = 0;
		final int UNTIL = offset + number;
		do
		{
			double diff = values[offset++] - mean;
			sum += diff * diff;
		}
		while (offset != UNTIL);
		return sum / (number - 1); 
	}
}
