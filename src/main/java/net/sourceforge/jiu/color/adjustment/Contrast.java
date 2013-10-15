/*
 * Contrast
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
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
 * Adjusts the contrast of an image.
 * The amount of adjustment is given to the constructor as a percentage value between -100 and 100.
 * -100 will make the resulting image middle-gray, 0 will leave it unchanged, 100 will map it to 
 * the eight corners of the color cube.
 * <h3>Usage examples</h3>
 * Both examples increase contrast by 30 percent.
 * <p>
 * If all you want is to create a new image with adjusted contrast from the image
 * data of an existing image, simply use the static helper method:
 * <pre>PixelImage adjustedImage = Contrast.adjust(inputImage, 30);</pre>
 * This leaves the original image inputImage unchanged and allocates a second
 * image object which is here assigned to the variable adjustedImage.
 * <p>
 * If you want more control over parameters, create your own Contrast object.
 * You can then reuse image objects, e.g. to write the adjusted image data
 * to the original image object:
 * <pre>
 * Contrast op = new Contrast();
 * op.setInputImage(image);
 * op.setOutputImage(image);
 * op.setContrast(30);
 * op.process();
 * // at this point, image will contain the adjusted image data,
 * // the original data wil be overwritten 
 * </pre>
 * @author Marco Schmidt
 */
public class Contrast extends LookupTableOperation
{
	private int contrast;

	/**
	 * This static helper method is more simple to use when all
	 * you need are the standard options. 
	 * @param input the image to work on
	 * @param percentage contrast modification, from -100 to 100
	 * @return a new image with adjusted contrast
	 */
	public static PixelImage adjust(PixelImage input, int percentage)
	{
		try
		{
			Contrast op = new Contrast();
			op.setInputImage(input);
			op.setContrast(percentage);
			op.process();
			return op.getOutputImage();
		}
		catch (Exception exc)
		{
			return null;
		}
	}


	private int[] createLookupTable(int numSamples, int contrast)
	{
		int[] result = new int[numSamples];
		final int MAX = numSamples - 1;
		final float MID = MAX / 2.0f;
		for (int i = 0; i < numSamples; i++)
		{
			if (contrast < 0)
			{
				if (i < MID)
				{
					result[i] = (int)(i + (MID - i) * (- contrast) / 100.0f);
				}
				else
				{
					result[i] = (int)(MID + (i - MID) * (100.0f + contrast) / 100.0f);
				}
			}
			else
			{
				if (i < MID)
				{
					result[i] = (int)(i * (100.0f - contrast) / 100.0f);
				}
				else
				{
					result[i] = (int)(i + (MAX - i) * contrast / 100.0f);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the contrast adjustment value associated with this opperation.
	 * The value lies between -100 and 100 (including both values).
	 * @return contrast adjustment
	 * @see #setContrast
	 */
	public int getContrast()
	{
		return contrast;
	}

	private void process(Paletted8Image in, Paletted8Image out)
	{
		if (out == null)
		{
			out = (Paletted8Image)in.createCompatibleImage(in.getWidth(), in.getHeight());
		}
		Palette palette = out.getPalette();
		int numSamples = palette.getMaxValue() + 1;
		final int[] LUT = createLookupTable(numSamples, contrast);
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
				setTable(channelIndex, createLookupTable(in.getMaxSample(channelIndex) + 1, getContrast()));
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
			throw new WrongParameterException("Contrast operation cannot operate on input image type: " + in.getClass());
		}
	}

	/**
	 * Sets the value for contrast adjustment to be used within this operation.
	 * @param newContrast new contrast, between -100 and 100 (including both values)
	 * @throws IllegalArgumentException if the new contrast value is not in the above mentioned interval
	 * @see #getContrast
	 */
	public void setContrast(int newContrast)
	{
		if (newContrast < -100)
		{
			throw new IllegalArgumentException("Contrast must be at least -100: " + newContrast);
		}
		if (newContrast > 100)
		{
			throw new IllegalArgumentException("Contrast must be at most 100: " + newContrast);
		}
		contrast = newContrast;
	}
}
