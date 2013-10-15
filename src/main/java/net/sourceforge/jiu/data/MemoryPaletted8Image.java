/*
 * Paletted8Image
 *
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.PixelImage;

/**
 * This class stores a paletted image with one byte per sample in memory.
 *
 * @author Marco Schmidt
 * @see net.sourceforge.jiu.data.ByteChannelImage
 * @see net.sourceforge.jiu.data.IntegerImage
 * @see net.sourceforge.jiu.data.Palette
 */
public class MemoryPaletted8Image extends MemoryByteChannelImage implements Paletted8Image
{
	/**
	 * This image's palette.
	 */
	private Palette palette;
	private int maxSampleValue;

	/**
	 * Create an image of byte channels.
	 * Image data will be completely in memory, so memory requirements are 
	 * <code>width * height * numChannels</code> bytes.
	 * Note that the data will not be initialized, so you should not assume
	 * anything about its content.
	 * @param width the horizontal resolution, must be non-zero and positive
	 * @param height the vertical resolution, must be non-zero and positive
	 * @throws IllegalArgumentException if any of the parameters are smaller than 1
	 */
	public MemoryPaletted8Image(int width, int height)
	{
		super(1, width, height);
		palette = null;
		maxSampleValue = 255;
	}

	public MemoryPaletted8Image(int width, int height, Palette palette)
	{
		this(width, height);
		setPalette(palette);
	}

	public static void checkPalette(Palette palette)
	{
		if (palette == null)
		{
			throw new IllegalArgumentException("Palette must be non-null.");
		}
		else
		{
			int numEntries = palette.getNumEntries();
			if (numEntries < 1 || numEntries > 256)
			{
				throw new IllegalArgumentException("Number of entries must " +
					"be from 1..256 for a Paletted8Image; got: " + numEntries);
			}
		}
	}

	public PixelImage createCompatibleImage(int width, int height)
	{
		Palette newPalette = null;
		Palette myPalette = getPalette();
		if (myPalette != null)
		{
			newPalette = (Palette)myPalette.clone();
		}
		return new MemoryPaletted8Image(width, height, newPalette);
	}

	public long getAllocatedMemory()
	{
		long result = super.getAllocatedMemory();
		Palette myPalette = getPalette();
		if (myPalette != null)
		{
			result += myPalette.getAllocatedMemory();
		}
		return result;
	}

	public Class getImageType()
	{
		return Paletted8Image.class;
	}

	public int getMaxSample(int channel)
	{
		return maxSampleValue;
	}

	/**
	 * Returns this image's palette.
	 * @see #setPalette
	 */
	public Palette getPalette()
	{
		return palette;
	}

	public String getTypeDescription()
	{
		return "Paletted image, 8 bits per pixel";
	}

	/**
	 * Sets this image's palette to a new value.
	 * @see #getPalette
	 */
	public void setPalette(Palette palette)
	{
		if (palette != null && palette.getNumEntries() > 256)
		{
			throw new IllegalArgumentException("Cannot use palette with more " +
				"than 256 entries in a Paletted8Image.");
		}
		this.palette = palette;
		if (palette == null)
		{
			maxSampleValue = 255;
		}
		else
		{
			maxSampleValue = palette.getNumEntries() - 1;
		}
	}
}
