/*
 * Invert
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Creates an inverted (negated) version of an image.
 * This is done by subtracting each sample value of a channel 
 * from the maximum sample for that channel.
 * The maximum sample for a channel is given by 
 * {@link net.sourceforge.jiu.data.IntegerImage#getMaxSample}. 
 * For paletted images, just the palette is treated that way.
 * Supported image types: {@link net.sourceforge.jiu.data.IntegerImage}.
 * Input and output image can be the same object.
 * <h3>Usage</h3>
 * There are two ways of using this class.
 * Either create an Invert object and set all paramters yourself.
 * This lets you reuse image objects or add a progress listener.
 * <pre>
 * Invert invert = new Invert();
 * invert.setInputImage(image);
 * invert.addProgressListener(listener); // this is optional
 * invert.process();
 * PixelImage invertedImage = invert.getOutputImage();
 * </pre>
 * The other method is by sing the static convenience method 
 * <pre>
 * PixelImage invertedImage = Invert.invert(someImage);
 * </pre>
 * You will have to catch the potential exceptions in both cases.
 * @author Marco Schmidt
 */
public class Invert extends ImageToImageOperation
{
	/**
	 * Helper method to return an inverted image from the argument image. 
	 * @param inputImage image to be inverted
	 * @return new image object with inverted image
	 * @throws OperationFailedException on operation errors
	 */
	public static PixelImage invert(PixelImage inputImage) throws OperationFailedException
	{
		Invert invert = new Invert();
		invert.setInputImage(inputImage);
		invert.process();
		return invert.getOutputImage();
	}

	private void prepare(PixelImage in) throws
		MissingParameterException,
		WrongParameterException
	{
		if (in == null)
		{
			throw new MissingParameterException("Missing input image.");
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			setOutputImage(in.createCompatibleImage(in.getWidth(), in.getHeight()));
		}
		else
		{
			if (in.getClass() != out.getClass())
			{
				throw new WrongParameterException("Specified output image type must be the same as input image type.");
			}
			if (in.getWidth() != out.getWidth())
			{
				throw new WrongParameterException("Specified output image must have same width as input image.");
			}
			if (in.getHeight() != out.getHeight())
			{
				throw new WrongParameterException("Specified output image must have same height as input image.");
			}
		}
	}

	private void process(Paletted8Image in)
	{
		// prepare(PixelImage) has made sure that we have a compatible output image
		Paletted8Image out = (Paletted8Image)getOutputImage();

		// invert palette of output image
		Palette pal = out.getPalette();
		final int MAX = pal.getMaxValue();
		for (int entryIndex = 0; entryIndex < pal.getNumEntries(); entryIndex++)
		{
			for (int channelIndex = 0; channelIndex < 3; channelIndex++)
			{
				pal.putSample(channelIndex, entryIndex, MAX - pal.getSample(channelIndex, entryIndex));
			}
		}

		// copy image content
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				out.putSample(0, x, y, in.getSample(0, x, y));
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(IntegerImage in)
	{
		IntegerImage out = (IntegerImage)getOutputImage();
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		final int CHANNELS = in.getNumChannels();
		final int TOTAL_ITEMS = CHANNELS * HEIGHT;
		int processedItems = 0;
		for (int channel = 0; channel < CHANNELS; channel++)
		{
			final int MAX = in.getMaxSample(channel);
			for (int y = 0; y < HEIGHT; y++)
			{
				for (int x = 0; x < WIDTH; x++)
				{
					out.putSample(channel, x, y, MAX - in.getSample(channel, x, y));
				}
				setProgress(processedItems++, TOTAL_ITEMS);
			}
		}
	}

	/**
	 * Inverts the input image, reusing an output image if one has been specified.
	 * For paletted images, inverts the palette.
	 * For all other types, subtracts each sample of each channel from the maximum
	 * value of that channel.
	 * @throws MissingParameterException if the input image is missing
	 * @throws WrongParameterException if any of the specified image parameters are unsupported or of the wrong width or height
	 */
	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		PixelImage in = getInputImage();
		prepare(in);
		if (in instanceof Paletted8Image)
		{
			process((Paletted8Image)in);
		}
		else
		if (in instanceof IntegerImage)
		{
			process((IntegerImage)in);
		}
		else
		{
			throw new WrongParameterException("Input image type unsupported: " + in.toString());
		}
	}
}
