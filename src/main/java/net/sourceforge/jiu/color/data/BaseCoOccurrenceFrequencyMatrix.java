/*
 * BaseCoOccurrenceFrequencyMatrix
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.data;

/**
 * This abstract class encapsulates all data of a co-occurrence
 * frequency matrix except for the frequency values.
 * The method computeStatistics is implemented.
 * Any class extending this class only has to
 * deal with storing the frequency values ({@link MemoryCoOccurrenceFrequencyMatrix}
 * does this by using a one-dimensional array internally).
 * @author Marco Schmidt
 */
public abstract class BaseCoOccurrenceFrequencyMatrix implements CoOccurrenceFrequencyMatrix
{
	/** co-occurrence frequency mean $\mu_{C(j)}$ */
	private double[] cofMean;
	/** co-occurrence frequency standard deviation $\sigma_{C(j)}$ */
	private double[] cofStddev;
	/** self co-occurrence frequency mean $\mu_S$ */
	private double scofMean;
	/** self co-occurrence frequency standard deviation $\sigma_S$ */
	private double scofStddev;
	/** equals scofMean + scofStddev */
	private double scofSum;

	private void computeCoOccurrenceFrequencyMeanValues()
	{
		cofMean = new double[getDimension()];
		for (int j = 0; j < getDimension(); j++)
		{
			double result = 0.0;
			for (int i = 0; i < getDimension(); i++)
			{
				result += getValue(i, j);
			}
			cofMean[j] = result / ((double)getDimension());
		}
		//System.out.println("DEBUG: done computing cofm mean values");
	}

	private void computeCoOccurrenceFrequencyStandardDeviationValues()
	{
		cofStddev = new double[getDimension()];
		for (int j = 0; j < getDimension(); j++)
		{
			double result = 0.0;
			for (int i = 0; i < getDimension(); i++)
			{
				double value = getValue(i, j) - cofMean[j];
				result += (value * value);
			}
			cofStddev[j] = Math.sqrt(result);
		}
		//System.out.println("DEBUG: done computing cofm stddev values");
	}

	private void computeSelfCoOccurrenceFrequencyMeanValue()
	{
		double sum = 0.0;
		for (int i = 0; i < getDimension(); i++)
		{
			sum += getValue(i, i);
		}
		scofMean = sum / (getDimension());
		//System.out.println("DEBUG: scof mean=" + scofMean);
	}

	private void computeSelfCoOccurrenceFrequencyStandardDeviationValue()
	{
		double result = 0.0;
		for (int i = 0; i < getDimension(); i++)
		{
			double value = getValue(i, i) - getScofMean();
			result += (value * value);
		}
		scofStddev = Math.sqrt(result);
		//System.out.println("DEBUG: scof stddev=" + scofStddev);
	}

	/**
	 * Assumes that the co-occurrence frequency values have been initialized.
	 * Computes mean and standard deviation for co-occurrence and self co-occurrence
	 * frequency values.
	 */
	public void computeStatistics()
	{
		// we must keep this order because stddev needs mean!
		computeSelfCoOccurrenceFrequencyMeanValue();
		computeSelfCoOccurrenceFrequencyStandardDeviationValue();
		scofSum = getScofMean() + getScofStddev();
		//System.out.println("DEBUG: scof sum=" + scofSum);
		computeCoOccurrenceFrequencyMeanValues();
		computeCoOccurrenceFrequencyStandardDeviationValues();
	}

	/**
	 * Prints co-occurrence frequency values to standard output, one line
	 * per matrix row j.
	 * Calls getValue(i, j) with each column i for each row j.
	 */
	/*private void dump()
	{
		for (int j = 0; j < getDimension(); j++)
		{
			for (int i = 0; i < getDimension(); i++)
			{
				System.out.print(getValue(i, j) + " ");
			}
			System.out.println("");
		}
	}*/

	/**
	 * Prints self co-occurrence frequency values to standard output, eight
	 * values per row.
	 * Calls getValue(i, i) with each value i from 0 to getDimension() - 1.
	 */
/*	private void dumpScofValues()
	{
		for (int j = 0; j < getDimension(); j++)
		{
			System.out.print(getValue(j) + " ");
			if (j % 8 == 0 && j > 0)
			{
				System.out.println("");
			}
		}
		System.out.println("");
	}*/

	/**
	 * Returns the mean of the co-occurrence frequency values.
	 */
	public double getMean(int index)
	{
		return cofMean[index];
	}

	public double getStddev(int index)
	{
		return cofStddev[index];
	}

	/**
	 * Returns the mean of all self co-occurrence frequency values.
	 * This value is called $\mu_S$ in Shufelt's paper.
	 * This value is determined once within computeStatistics().
	 */
	public double getScofMean()
	{
		return scofMean;
	}

	/**
	 * Returns the standard deviation of all self co-occurrence frequency
	 * values.
	 * This value is called $\sigma_S$ in Shufelt's paper.
	 * This value is determined once within a call to computeStatistics().
	 */
	public double getScofStddev()
	{
		return scofStddev;
	}

	/**
	 * Return the sum of mean and standard deviation of the self
	 * co-occurrence frequency values.
	 * Assumes that {@link #computeStatistics} has been called already.
	 * @return sum of mean and standard deviation of the self co-occurrence
	 *  frequency values
	 */
	public double getScofSum()
	{
		return scofSum;
	}
}
