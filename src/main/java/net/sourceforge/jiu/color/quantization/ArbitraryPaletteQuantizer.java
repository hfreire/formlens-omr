/*
 * ArbitraryPaletteQuantizer
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.color.quantization.RGBQuantizer;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * A color quantizer that maps an {@link net.sourceforge.jiu.data.RGBImage} 
 * to any given palette.
 * This operation is restricted to RGB24Image and palettes with up to 256 colors.
 * It picks the color from the palette which is closest to the 
 * color to be quantized (with the minimum distance).
 * This is a rather naive implementation which, for any given color
 * to be quantized, computes the distance between it and each color
 * in the palette (read: this operation is rather slow with a large palette and input image).
 * <p>
 * It uses <a target="_top" href="http://hissa.nist.gov/dads/HTML/manhttndstnc.html">Manhattan distance</a> (L<sub>1</sub>) 
 * instead of <a target="_top" href="http://hissa.nist.gov/dads/HTML/euclidndstnc.html">Euclidean distance</a> (L<sub>2</sub>).
 * This saves a square root operation per distance computation.
 * <p>
 * There are more sophisticated <a target="_top" 
 * href="http://www.cs.sunysb.edu/~algorith/files/nearest-neighbor.shtml">nearest 
 * neighbor</a> algorithms available, left for future extensions.
 *
 * <h3>Usage example</h3>
 * <p>This example maps an RGB truecolor image to some palette
 * we create.</p>
 * <pre>
 * RGB24Image image = ...; // initialize this
 * // create some Palette object that you want to map the image to
 * Palette palette = new Palette(3); // create palette with three entries
 * palette.put(0, 33, 00, 244); // set first color
 * palette.put(1, 0, 240, 193); // set second color
 * palette.put(2, 245, 126, 136); // set third color
 * ArbitraryPaletteQuantizer quantizer = new ArbitraryPaletteQuantizer(palette);
 * quantizer.setInputImage(image);
 * quantizer.process();
 * PixelImage quantizedImage = quantizer.getOutputImage();
 * </pre>
 *
 * @author Marco Schmidt
 * @since 0.5.0
 */
public class ArbitraryPaletteQuantizer extends ImageToImageOperation implements RGBIndex, RGBQuantizer
{
	private final int[] RED;
	private final int[] GREEN;
	private final int[] BLUE;
	private Palette palette;
	private int numEntries;

	/**
	 * Creates a quantizer that will be able to map pixels (or a complete image)
	 * to the argument palette.
	 */
	public ArbitraryPaletteQuantizer(Palette palette)
	{
		this.palette = palette;
		numEntries = palette.getNumEntries();
		// create 1D lookup tables for the three components and fill them with
		// the palette entry data
		RED = new int[numEntries];
		GREEN = new int[numEntries];
		BLUE = new int[numEntries];
		for (int i = 0; i < numEntries; i++)
		{
			RED[i] = palette.getSample(INDEX_RED, i);
			GREEN[i] = palette.getSample(INDEX_GREEN, i);
			BLUE[i] = palette.getSample(INDEX_BLUE, i);
		}
	}

	/**
	 * Returns a copy of the palette that was given to the
	 * constructor of this class.
	 * @return new copy of the palette this quantizer works on
	 */
	public Palette createPalette()
	{
		return (Palette)palette.clone();
	}

	public int map(int[] origRgb, int[] quantizedRgb)
	{
		int r = origRgb[INDEX_RED];
		int g = origRgb[INDEX_GREEN];
		int b = origRgb[INDEX_BLUE];
		int minIndex = 0;
		int minDistance = Integer.MAX_VALUE;
		for (int index = 0; index < numEntries; index++)
		{
			int v = r - RED[index];
			int distance = v * v; 
			v = g - GREEN[index];
			distance += v * v;
			v = b - BLUE[index];
			distance += v * v;
			if (distance < minDistance)
			{
				minDistance = distance;
				minIndex = index;
			}
		}
		quantizedRgb[INDEX_RED] = RED[minIndex];
		quantizedRgb[INDEX_GREEN] = GREEN[minIndex];
		quantizedRgb[INDEX_BLUE] = BLUE[minIndex];
		return minIndex;
	}

	/**
	 * Finds the best match for the argument color in the palette and returns 
	 * its index.
	 * Similar to {@link #map(int[], int[])}, the quantized color is not required
	 * and thus a few assignemnts are saved by this method.
	 * @param red red sample of the pixel to be quantized
	 * @param green green sample of the pixel to be quantized
	 * @param blue blue sample of the pixel to be quantized
	 * @return index of the color in the palette that is closest to the argument color
	 */
	public int map(int red, int green, int blue)
	{
		int minIndex = 0;
		int minDistance = Integer.MAX_VALUE;
		for (int index = 0; index < numEntries; index++)
		{
			int v = red - RED[index];
			int distance = v * v; 
			v = green - GREEN[index];
			distance += v * v;
			v = blue - BLUE[index];
			distance += v * v;
			if (distance < minDistance)
			{
				minDistance = distance;
				minIndex = index;
			}
		}
		return minIndex;
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
				out.putSample(0, x, y, map(in.getSample(INDEX_RED, x, y), in.getSample(INDEX_GREEN, x, y), in.getSample(INDEX_BLUE, x, y)));
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	/**
	 * Maps the input image to an output image, using the palette given to the constructor.
	 */
	public void process() throws 
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		if (!(in instanceof RGB24Image))
		{
			throw new WrongParameterException("Input image must be of type RGB24Image.");
		}
		PixelImage out = getOutputImage();
		if (out != null && !(out instanceof Paletted8Image))
		{
			throw new WrongParameterException("Output image must be of type Paletted8Image.");
		}
		process((RGB24Image)in, (Paletted8Image)out);
	}
}
