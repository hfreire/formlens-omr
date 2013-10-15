/*
 * RGBColor
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.data.RGBIndex;

/**
 * Encapsulates a single color from RGB (red, green, blue) color space plus a frequency counter.
 * Each of the three RGB samples is of type int.
 * Also stores a counter of type int.
 * @author Marco Schmidt
 */
public class RGBColor implements RGBIndex
{
	/** The intensity values that make up the color. */
	private int[] samples;

	/** Stores how many times this colors appears in a certain image. */
	private int counter;

	/**
	 * Creates an instance of this class and initializes it to the given
	 * intensity values.
	 * The internal color counter is set to zero.
	 */
	public RGBColor(int red, int green, int blue)
	{
		this(red, green, blue, 0);
	}

	/**
	 * Creates an instance of this class and initializes it to the given
	 * intensity values.
	 * Also sets the internal color counter to the given parameter.
	 */
	public RGBColor(int red, int green, int blue, int counter)
	{
		samples = new int[3];
		samples[INDEX_RED] = red;
		samples[INDEX_GREEN] = green;
		samples[INDEX_BLUE] = blue;
		this.counter = counter;
	}

	/**
	 * Compares this color to the argument color, using the sortOrder argument (which is one of the
	 * three index values defined in {@link RGBIndex}.
	 * That way, the two sample values for one component (e.g. red if sortOrder == INDEX_RED) are
	 * compared.
	 *
	 * @param c the color to which this color is compared
	 * @param sortOrder the component used for the comparison
	 * @return relation between this color and the argument color
	 */
	public int compareTo(RGBColor c, int sortOrder)
	{
		int s1 = samples[sortOrder];
		int s2 = c.samples[sortOrder];
		if (s1 < s2)
		{
			return -1;
		}
		else
		if (s1 == s2)
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}

	/**
	 * For two RGB triplets (r1, g1, b1) and (r2, g2, b2) this will return
	 * the distance between those colors in RGB color space.
	 */
	public static double computeDistance(int r1, int g1, int b1, int r2, int g2, int b2)
	{
		int r = r1 - r2;
		int g = g1 - g2;
		int b = b1 - b2;
		return Math.sqrt(r * r + g * g + b * b);
	}

	/**
	 * Compares this color with another instance of RGBColor and returns true
	 * if all intensity values are equal, false otherwise.
	 */
	public boolean equals(Object obj)
	{
		RGBColor c = (RGBColor)obj;
		return (samples[0] == c.samples[0] && samples[1] == c.samples[1] && samples[2] == c.samples[2]);
	}

	public int getCounter()
	{
		return counter;
	}

	public int getSample(int index)
	{
		return samples[index];
	}

	public String toString()
	{
		return "(" + samples[INDEX_RED] + ", " + samples[INDEX_GREEN] + ", " + samples[INDEX_BLUE] + ")";
	}
}
