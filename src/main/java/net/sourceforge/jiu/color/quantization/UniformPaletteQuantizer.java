/*
 * UniformPaletteQuantizer
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.color.quantization.RGBQuantizer;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;

/**
 * A color quantizer that maps to a palette which is equidistantly distributed 
 * in the RGB color cube.
 * Equidistantly distributed only within each channel. 
 * @author Marco Schmidt
 */
public class UniformPaletteQuantizer extends ImageToImageOperation implements
	RGBIndex,
	RGBQuantizer
{
	private final int RED_BITS;
	private final int RED_LEFT_SHIFT;
	private final int RED_RIGHT_SHIFT;
	private final int[] RED_VALUES;
	private final int GREEN_BITS;
	private final int GREEN_LEFT_SHIFT;
	private final int GREEN_RIGHT_SHIFT;
	private final int[] GREEN_VALUES;
	private final int BLUE_BITS;
	private final int BLUE_RIGHT_SHIFT;
	private final int[] BLUE_VALUES;
	private final int TOTAL_BITS;
	private int[] PALETTE_RED;
	private int[] PALETTE_GREEN;
	private int[] PALETTE_BLUE;

	public UniformPaletteQuantizer(int redBits, int greenBits, int blueBits)
	{
		if (redBits < 1)
		{
			throw new IllegalArgumentException("Must have at least 1 bit for red.");
		}
		if (greenBits < 1)
		{
			throw new IllegalArgumentException("Must have at least 1 bit for green.");
		}
		if (blueBits < 1)
		{
			throw new IllegalArgumentException("Must have at least 1 bit for blue.");
		}
		BLUE_BITS = blueBits;
		BLUE_RIGHT_SHIFT = 8 - BLUE_BITS;
		BLUE_VALUES = new int[1 << BLUE_BITS];
		for (int i = 0; i < BLUE_VALUES.length; i++)
			BLUE_VALUES[i] = i * 255 / (BLUE_VALUES.length - 1);
		GREEN_BITS = greenBits;
		GREEN_RIGHT_SHIFT = 8 - GREEN_BITS;
		GREEN_LEFT_SHIFT = BLUE_BITS;
		GREEN_VALUES = new int[1 << GREEN_BITS];
		for (int i = 0; i < GREEN_VALUES.length; i++)
			GREEN_VALUES[i] = i * 255 / (GREEN_VALUES.length - 1);
		RED_BITS = redBits;
		RED_RIGHT_SHIFT = 8 - RED_BITS;
		RED_LEFT_SHIFT = GREEN_BITS + BLUE_BITS;
		RED_VALUES = new int[1 << RED_BITS];
		for (int i = 0; i < RED_VALUES.length; i++)
			RED_VALUES[i] = i * 255 / (RED_VALUES.length - 1);
		TOTAL_BITS = RED_BITS + GREEN_BITS + BLUE_BITS;
		if (TOTAL_BITS > 8)
		{
			throw new IllegalArgumentException("Sum of red / green / blue bits must not exceed 8.");
		}
	}

	public Palette createPalette()
	{
		int numEntries = 1 << TOTAL_BITS;
		Palette result = new Palette(numEntries, 255);
		PALETTE_RED = new int[numEntries];
		PALETTE_GREEN = new int[numEntries];
		PALETTE_BLUE = new int[numEntries];
		int index = 0;
		for (int r = 0; r < (1 << RED_BITS); r++)
		{
			for (int g = 0; g < (1 << GREEN_BITS); g++)
			{
				for (int b = 0; b < (1 << BLUE_BITS); b++)
				{
					//System.out.println(index + ":" + r + ", " + g + ", " + b);
					result.putSample(INDEX_RED, index, RED_VALUES[r]);
					PALETTE_RED[index] = RED_VALUES[r];
					result.putSample(INDEX_GREEN, index, GREEN_VALUES[g]);
					PALETTE_GREEN[index] = GREEN_VALUES[g];
					result.putSample(INDEX_BLUE, index, BLUE_VALUES[b]);
					PALETTE_BLUE[index] = BLUE_VALUES[b];
					index++;
				}
			}
		}
		return result;
	}

	public int map(int[] origRgb, int[] quantizedRgb)
	{
		int index = mapToIndex(origRgb[INDEX_RED], origRgb[INDEX_GREEN], origRgb[INDEX_BLUE]);
		quantizedRgb[INDEX_RED] = PALETTE_RED[index];
		quantizedRgb[INDEX_GREEN] = PALETTE_GREEN[index];
		quantizedRgb[INDEX_BLUE] = PALETTE_BLUE[index];
		return index;
	}

	public final int mapToIndex(int red, int green, int blue)
	{
		return
			((red >> RED_RIGHT_SHIFT) << RED_LEFT_SHIFT) |
			((green >> GREEN_RIGHT_SHIFT) << GREEN_LEFT_SHIFT) |
			(blue >> BLUE_RIGHT_SHIFT);
	}

	private void process(RGB24Image in, Paletted8Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		if (out == null)
		{
			out = new MemoryPaletted8Image(WIDTH, HEIGHT, createPalette());
		}
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int r = in.getSample(INDEX_RED, x, y);
				int g = in.getSample(INDEX_GREEN, x, y);
				int b = in.getSample(INDEX_BLUE, x, y);
				out.putSample(0, x, y, mapToIndex(r, g, b));
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	public void process()
	{
		process((RGB24Image)getInputImage(), (Paletted8Image)getOutputImage());
	}
}
