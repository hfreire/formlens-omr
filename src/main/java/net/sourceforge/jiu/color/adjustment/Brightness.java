/*
 * Brightness
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.adjustment;

import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Adjusts the brightness of an image.
 * <p>
 * The brightness change is specified as a percentage value between -100 and 100.
 * -100 will make the resulting image black, 0 will leave it unchanged, 100 will make it white.
 * <h3>Supported image types</h3>
 * <ul>
 *  <li>{@link net.sourceforge.jiu.data.GrayIntegerImage}</li>
 *  <li>{@link net.sourceforge.jiu.data.RGBIntegerImage}</li>
 *  <li>{@link net.sourceforge.jiu.data.Paletted8Image}</li>
 * </ul>
 * <h3>Usage examples</h3>
 * Both examples make the input image 30 percent brighter.
 * <p>
 * If all you want is to create a new image with adjusted brightness from the image
 * data of an existing image, simply use the static helper method:
 * <pre>PixelImage adjustedImage = Brightness.adjust(inputImage, 30);</pre>
 * This leaves the original image inputImage unchanged and allocates a second
 * image object which is here assigned to the variable adjustedImage.
 * <p>
 * If you want more control over parameters, create your own Brightness object.
 * You can then reuse image objects, e.g. to write the adjusted image data
 * to the original image object:
 * <pre>
 * Brightness brightness = new Brightness();
 * brightness.setInputImage(image);
 * brightness.setOutputImage(image);
 * brightness.setBrightness(30);
 * brightness.process();
 * // at this point, image will contain the adjusted image data,
 * // the original data wil be overwritten 
 * </pre>
 * <p>
 * @author Marco Schmidt
 */
public class Brightness extends LookupTableOperation
{
	private int brightness;

	/**
	 * This static helper method is more simple to use when all
	 * you need are the standard options. 
	 * @param input the image to work on
	 * @param percentage brightness modification, from -100 to 100
	 * @return a new image with adjusted brightness
	 */
	public static PixelImage adjust(PixelImage input, int percentage)
	{
		try
		{
			Brightness brightness = new Brightness();
			brightness.setInputImage(input);
			brightness.setBrightness(percentage);
			brightness.process();
			return brightness.getOutputImage();
		}
		catch (Exception exc)
		{
			return null;
		}
	}

	/**
	 * Creates a lookup table that holds all new values for samples 0 to
	 * numSamples - 1.
	 */
	private int[] createLookupTable(int numSamples, int brightness)
	{
		if (brightness < -100 || brightness > 100)
		{
			return null;
		}
		int[] result = new int[numSamples];
		final int MAX = numSamples - 1;
		for (int i = 0; i < numSamples; i++)
		{
			if (brightness < 0)
			{
				result[i] = (int)((float)i  * (100.0f + brightness) / 100.0f);
			}
			else
			{
				result[i] = (int)(i + (MAX - i) * brightness / 100.0f);
			}
		}
		return result;
	}

	private void process(Paletted8Image in, Paletted8Image out)
	{
		if (out == null)
		{
			out = (Paletted8Image)in.createCompatibleImage(in.getWidth(), in.getHeight());
		}
		Palette palette = out.getPalette();
		int numSamples = palette.getNumEntries();
		final int[] LUT = createLookupTable(numSamples, brightness);
		for (int c = 0; c < 3; c++)
		{
			for (int i = 0; i < palette.getNumEntries(); i++)
			{
				palette.putSample(c, i, LUT[palette.getSample(c, i)]);
			}
		}
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(0, x, y, in.getSample(0, x, y));
			}
			setProgress(y, in.getHeight());
		}
		setOutputImage(out);
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		prepareImages();
		IntegerImage in = (IntegerImage)getInputImage();
		if (in instanceof GrayIntegerImage || in instanceof RGBIntegerImage)
		{
			setNumTables(in.getNumChannels());
			for (int channelIndex = 0; channelIndex < in.getNumChannels(); channelIndex++)
			{
				setTable(channelIndex, createLookupTable(in.getMaxSample(channelIndex) + 1, brightness));
			}
			super.process();
		}
		else
		if (in instanceof Paletted8Image)
		{
			process((Paletted8Image)in, (Paletted8Image)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Brightness operation cannot operate on input image type: " + in.getClass());
		}
	}

	/**
	 * Sets the brightness adjustment value in percent (between -100 and 100).
	 * @param newBrightness the amount of change to be applied to the brightness of the input image
	 * @throws IllegalArgumentException if the argument is smaller than -100 or larger than 100
	 */
	public void setBrightness(int newBrightness)
	{
		if (newBrightness < -100)
		{
			throw new IllegalArgumentException("Brightness must be at least -100: " + newBrightness);
		}
		if (newBrightness > 100)
		{
			throw new IllegalArgumentException("Brightness must be at most 100: " + newBrightness);
		}
		brightness = newBrightness;
	}
}
