/*
 * Rotate90Right
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
 * Rotates images by 90 degrees clockwise (to the right).
 * Input image must implement {@link net.sourceforge.jiu.data.IntegerImage}.
 * <h3>Usage example</h3>
 * <pre>
 * Rotate90Right rotate = new Rotate90Right();
 * rotate.setInputImage(image);
 * rotate.process();
 * PixelImage rotatedImage = rotate.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class Rotate90Right extends ImageToImageOperation
{
	private void process(IntegerImage in, IntegerImage out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(HEIGHT, WIDTH);
			setOutputImage(out);
		}
		int totalItems = in.getNumChannels() * HEIGHT;
		int processedItems = 0;
		for (int c = 0; c < in.getNumChannels(); c++)
		{
			for (int y = 0; y < HEIGHT; y++)
			{
				for (int x = 0; x < WIDTH; x++)
				{
					out.putSample(c, (HEIGHT - y - 1), x, in.getSample(c, x, y));
				}
				setProgress(processedItems++, totalItems);
			}
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		ensureOutputImageResolution(in.getHeight(), in.getWidth());
		if (in instanceof IntegerImage)
		{
			process((IntegerImage)in, (IntegerImage)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Input image must implement IntegerImage.");
		}
	}
}
