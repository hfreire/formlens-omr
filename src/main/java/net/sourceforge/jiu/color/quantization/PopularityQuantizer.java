/*
 * PopularityQuantizer
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.color.analysis.Histogram3DCreator;
import net.sourceforge.jiu.color.data.Histogram3D;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Performs the popularity color quantization algorithm that maps an image to
 * the colors occurring most frequently in the input image.
 * The number of colors in the palette can be defined by the user of this
 * operation with {@link #setPaletteSize(int)}.
 * <h3>Supported image types</h3>
 * The input image must implement {@link net.sourceforge.jiu.data.RGB24Image},
 * the output image must be of type {@link net.sourceforge.jiu.data.Paletted8Image}.
 * <h3>Usage example</h3>
 * The following code snippet uses the default settings with a palette of 256 entries.
 * <pre>
 * PopularityQuantizer quantizer = new PopularityQuantizer();
 * quantizer.setInputImage(image);
 * quantizer.setPaletteSize(256);
 * quantizer.process();
 * PixelImage quantizedImage = quantizer.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 * @since 0.11.0
 * @see ArbitraryPaletteQuantizer
 */
public class PopularityQuantizer extends ImageToImageOperation implements RGBIndex, RGBQuantizer
{
	private ArbitraryPaletteQuantizer arbQuantizer;
	private int paletteSize;
	private Palette palette;
	private boolean doNotMap;

	public Palette createPalette()
	{
		if (palette == null)
		{
			try
			{
				palette = determinePalette();
				return (Palette)palette.clone();
			}
			catch (OperationFailedException ofe)
			{
				return null;
			}
		}
		else
		{
			return (Palette)palette.clone();
		}
	}

	private Palette determinePalette() throws OperationFailedException
	{
		Histogram3DCreator hc = new Histogram3DCreator();
		hc.setImage((IntegerImage)getInputImage(), RGBIndex.INDEX_RED, RGBIndex.INDEX_GREEN, RGBIndex.INDEX_BLUE);
		hc.process();
		Histogram3D hist = hc.getHistogram();
		if (hist == null)
		{
			throw new OperationFailedException("Could not create histogram from input image.");
		}
		int numUniqueColors = hist.getNumUsedEntries();
		if (numUniqueColors <= paletteSize)
		{
			paletteSize = numUniqueColors;
		}
		RGBColorList list = new RGBColorList(hist);
		list.sortByCounter(0, list.getNumEntries() - 1);
		Palette result = new Palette(paletteSize);
		int paletteIndex = paletteSize - 1;
		int listIndex = list.getNumEntries() - 1;
		while (paletteIndex >= 0)
		{
			RGBColor color = list.getColor(listIndex--);
			result.put(paletteIndex--, 
				color.getSample(RGBIndex.INDEX_RED),
				color.getSample(RGBIndex.INDEX_GREEN),
				color.getSample(RGBIndex.INDEX_BLUE)
			);
		}
		return result;
	}

	/**
	 * Returns the number of colors in the destination image.
	 * If output is paletted, this is also the number of entries
	 * in the palette.
	 * @return number of colors in the destination
	 * @see #setPaletteSize(int)
	 */
	public int getPaletteSize()
	{
		return paletteSize;
	}

	public int map(int[] origRgb, int[] quantizedRgb)
	{
		return arbQuantizer.map(origRgb, quantizedRgb);
	}

	public void process() throws
		MissingParameterException,
		OperationFailedException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		if (!(in instanceof RGB24Image))
		{
			throw new WrongParameterException("Input image must implement RGB24Image.");
		}
		Histogram3DCreator hc = new Histogram3DCreator();
		hc.setImage((IntegerImage)in, RGBIndex.INDEX_RED, RGBIndex.INDEX_GREEN, RGBIndex.INDEX_BLUE);
		hc.process();
		Histogram3D hist = hc.getHistogram();
		if (hist == null)
		{
			throw new OperationFailedException("Could not create histogram from input image.");
		}
		int numUniqueColors = hist.getNumUsedEntries();
		if (numUniqueColors <= paletteSize)
		{
			paletteSize = numUniqueColors;
		}
		arbQuantizer = new ArbitraryPaletteQuantizer(createPalette());
		if (!doNotMap)
		{
			arbQuantizer.setInputImage(in);
			arbQuantizer.setOutputImage(getOutputImage());
			arbQuantizer.process();
			// TODO: copy ProgressListeners to arbQuantizer 
			setOutputImage(arbQuantizer.getOutputImage());
		}
	}

	/**
	 * Specifies whether this operation will map the image to the 
	 * new palette (true) or not (false).
	 * The latter may be interesting if only the palette is required.
	 * By default, this operation does map.
	 * @param newValue map to new image (true) or just search palette (false)
	 */
	public void setMapping(boolean newValue)
	{
		doNotMap = !newValue;
	}

	/**
	 * Sets the number of colors that this operations is supposed to reduce
	 * the original image to.
	 * @param newPaletteSize the number of colors
	 * @throws IllegalArgumentException if the argument is smaller than 1 or larger than 256
	 * @see #getPaletteSize
	 */
	public void setPaletteSize(int newPaletteSize)
	{
		if (newPaletteSize < 1)
		{
			throw new IllegalArgumentException("Palette size must be 1 or larger.");
		}
		if (newPaletteSize > 256)
		{
			throw new IllegalArgumentException("Palette size must be at most 256.");
		}
		paletteSize = newPaletteSize;
	}
}
