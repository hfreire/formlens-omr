/*
 * Flip
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Flips images (top row becomes bottom row and vice versa, and so on).
 * <p>
 * Supported image types: {@link IntegerImage}.
 * <p>
 * Usage example:
 * <pre>
 * Flip flip = new Flip();
 * flip.setInputImage(image); // image is some IntegerImage object
 * flip.process();
 * PixelImage flippedImage = flip.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class Flip extends ImageToImageOperation
{
	private void process(IntegerImage in, IntegerImage out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		final int TOTAL_ITEMS = in.getNumChannels() * HEIGHT;
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(WIDTH, HEIGHT);
			setOutputImage(out);
		}
		int processedItems = 0;
		for (int c = 0; c < in.getNumChannels(); c++)
		{
			for (int y1 = 0, y2 = HEIGHT - 1; y1 < HEIGHT; y1++, y2--)
			{
				for (int x = 0; x < WIDTH; x++)
				{
					out.putSample(c, x, y2, in.getSample(c, x, y1));
				}
				setProgress(processedItems++, TOTAL_ITEMS);
			}
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		if (in instanceof IntegerImage)
		{
			process((IntegerImage)in, (IntegerImage)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Input image must be of type IntegerImage.");
		}
	}
}
