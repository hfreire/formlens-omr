/*
 * Palette
 *
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.RGBIndex;

/**
 * This class represents a palette, a list of RGB colors.
 * An RGB color here has three int values for its red, green and blue
 * intensity.
 * Each intensity value must be larger than or equal to zero and
 * smaller than or equal to the maximum intensity value that can be
 * given to the constructor {@link #Palette(int, int)}.
 * This maximum value is typically 255.
 * Note that the number of entries in a palette is restricted only
 * by the element index type <code>int</code> so that palettes with
 * more than 256 entries are no problem.
 * When accessing (reading or writing) samples of this palette, use
 * the constants {@link #INDEX_RED}, {@link #INDEX_GREEN} and {@link #INDEX_BLUE} of
 * this class to define a color channel.
 * @author Marco Schmidt
 * @see net.sourceforge.jiu.data.PalettedImage
 */
public class Palette implements RGBIndex
{
	private int[][] data;
	private int numEntries;
	private int maxValue;

	/**
	 * Create a palette with the given number of entries and a maximum value
	 * for each sample.
	 * @param numEntries the number of entries to be accessible in this palette
	 * @param maxValue the maximum value to be allowed for each sample
	 */
	public Palette(int numEntries, int maxValue)
	{
		if (numEntries < 1)
		{
			throw new IllegalArgumentException("Error -- numEntries must be larger than 0.");
		}
		this.numEntries = numEntries;
		this.maxValue = maxValue;
		data = new int[3][];
		for (int i = 0; i < 3; i++)
		{
			data[i] = new int[numEntries];
		}
	}

	/**
	 * Create a palette with the given number of entries and a maximum value
	 * of <code>255</code>.
	 * @param numEntries the number of entries to be accessible in this palette
	 */
	public Palette(int numEntries)
	{
		this(numEntries, 255);
	}

	/**
	 * Creates a copy of this palette, allocating a new Palette object
	 * and copying each RGB triplet to the new palette.
	 * Then returns the new palette.
	 * Thus, a &quot;deep&quot; copy of this Palette object is created,
	 * not a &quot;shallow&quot; one.
	 *
	 * @return newly-created palette
	 */
	public Object clone()
	{
		Palette result = new Palette(getNumEntries(), getMaxValue());
		for (int i = 0; i < getNumEntries(); i++)
		{
			result.putSample(INDEX_RED, i, getSample(INDEX_RED, i));
			result.putSample(INDEX_GREEN, i, getSample(INDEX_GREEN, i));
			result.putSample(INDEX_BLUE, i, getSample(INDEX_BLUE, i));
		}
		return result;
	}

	/**
	 * Returns the amount of memory in bytes allocated for this palette.
	 *
	 */
	public long getAllocatedMemory()
	{
		long result = 0;
		if (data != null)
		{
			for (int i = 0; i < data.length; i++)
			{
				if (data[i] != null)
				{
					result += data[i].length;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the maximum value allowed for a sample.
	 * @return the maximum sample value
	 */
	public int getMaxValue()
	{
		return maxValue;
	}

	/**
	 * Returns the number of entries in this palette.
	 * @return the number of entries in this palette
	 */
	public int getNumEntries()
	{
		return numEntries;
	}

	/**
	 * Returns one of the samples of this palette.
	 * @param entryIndex the index of the color to be addressed, must be from
	 *  <code>0</code> to <code>getNumEntries() - 1</code>
	 * @param channelIndex one of the three channels; must be {@link #INDEX_RED},
	 *  {@link #INDEX_GREEN} or {@link #INDEX_BLUE}
	 * @return the requested sample
	 */
	public int getSample(int channelIndex, int entryIndex)
	{
		try
		{
			return data[channelIndex][entryIndex];
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Entry must be from 0 to " + (numEntries - 1) +
				", channel from 0 to 2.");
		}
	}

	/**
	 * Returns all samples of one channel as an int array.
	 * @param channelIndex index of the channel, one of the {@link RGBIndex} constants
	 * @return array with samples
	 */
	public int[] getSamples(int channelIndex)
	{
		if (channelIndex < 0 || channelIndex > 2)
		{
			throw new IllegalArgumentException("Invalid channel index, must be from 0 to 2.");
		}
		int[] result = new int[data[channelIndex].length];
		System.arraycopy(data[channelIndex], 0, result, 0, result.length);
		return result;
	}

	/**
	 * Checks if all entries of this palette are either black or white.
	 * An entry is black if all three intensitites (red, green and blue) are
	 * <code>0</code>, it is white if they are all equal to 
	 * {@link #getMaxValue()}.
	 * No particular order of entries (e.g. first color black, second white)
	 * is demanded and no specific number of entries (e.g. 2).
	 * This means that a palette is black and white if it contains ten entries
	 * that are all black.
	 *
	 * @return if the palette contains only the colors black and white
	 */
	public boolean isBlackAndWhite()
	{
		int i = 0;
		while (i < numEntries)
		{
			if (data[INDEX_RED][i] != data[INDEX_GREEN][i] ||
			    data[INDEX_GREEN][i] != data[INDEX_BLUE][i] ||
			    (data[INDEX_BLUE][i] != 0 && data[INDEX_BLUE][i] != maxValue))
			{
				return false;
			}
			i++;
		}
		return true;
	}

	/**
	 * Checks if this palette is gray, i.e., checks if all entries are
	 * gray. This is the case if for all entries red, green and blue
	 * have the same intensity.
	 * @return if the palette contains only shades of gray
	 */
	public boolean isGray()
	{
		int i = 0;
		while (i < numEntries)
		{
			if (data[INDEX_RED][i] != data[INDEX_GREEN][i] ||
			    data[INDEX_GREEN][i] != data[INDEX_BLUE][i])
			{
				return false;
			}
			i++;
		}
		return true;
	}

	public void put(int entryIndex, int red, int green, int blue)
	{
		putSample(INDEX_RED, entryIndex, red);
		putSample(INDEX_GREEN, entryIndex, green);
		putSample(INDEX_BLUE, entryIndex, blue);
	}

	/**
	 * Sets one sample of one color entry in the palette to a new value.
	 * @param channelIndex 
	 */
	public void putSample(int channelIndex, int entryIndex, int newValue)
	{
		if (newValue < 0 || newValue > maxValue)
		{
			throw new IllegalArgumentException("Value must be from 0 to " +
				maxValue + "; argument is " + newValue + ".");
		}
		try
		{
			data[channelIndex][entryIndex] = newValue;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Entry must be from 0 to " + (numEntries - 1) +
				", channel from 0 to 2.");
		}
	}
}
