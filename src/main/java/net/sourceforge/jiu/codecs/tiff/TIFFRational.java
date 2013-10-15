/*
 * TIFFRational
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

/**
 * Data class to store a TIFF rational number.
 * A TIFF rational number is a fraction given by 32 bit integer numerator and denominator values.
 * It is one of the data types used in TIFF tags ({@link TIFFTag}).
 * For more information on TIFF's internals, see {@link TIFFCodec}, which lists a few links 
 * to TIFF specification documents.
 * @author Marco Schmidt
 */
public class TIFFRational
{
	private int numerator;
	private int denominator;

	/**
	 * Creates a TiffRational object from the arguments.
	 * @param numerator the numerator of the fraction stored in this object
	 * @param denominator the denominator of the fraction stored in this object
	 * @throws IllegalArgumentException if denominator is <code>0</code> (division by zero is not allowed)
	 */
	public TIFFRational(int numerator, int denominator)
	{
		if (denominator == 0)
		{
			throw new IllegalArgumentException("A zero denominator is not allowed.");
		}
		this.numerator = numerator;
		this.denominator = denominator;
	}

	/**
	 * Returns the denominator value that was given to the constructor.
	 * @return denominator value
	 */
	public int getDenominator()
	{
		return denominator;
	}

	/**
	 * Returns the fraction as a <code>double</code> value.
	 * @return the fraction stored in this object
	 * @see #getAsFloat
	 */
	public double getAsDouble()
	{
		return (double)numerator / (double)denominator;
	}

	/**
	 * Returns the fraction as a <code>float</code> value.
	 * @return the fraction stored in this object
	 * @see #getAsDouble
	 */
	public float getAsFloat()
	{
		return (float)numerator / (float)denominator;
	}

	/**
	 * Returns the numerator value that was given to the constructor.
	 * @return numerator value
	 */
	public int getNumerator()
	{
		return numerator;
	}
}
