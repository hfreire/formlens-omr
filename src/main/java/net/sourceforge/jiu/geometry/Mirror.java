/*
 * Mirror
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Mirrors images (leftmost column becomes rightmost column and vice versa, and so on).
 * <p>
 * Supported image types: {@link IntegerImage}.
 * <h3>Usage example</h3>
 * <pre>
 * PixelImage image = ...; // something implementing IntegerImage
 * Mirror mirror = new Mirror();
 * mirror.setInputImage(image);
 * mirror.process();
 * PixelImage mirroredImage = mirror.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class Mirror extends ImageToImageOperation
{
	private void process(IntegerImage in, IntegerImage out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(WIDTH, HEIGHT);
			setOutputImage(out);
		}
		int totalItems = in.getNumChannels() * WIDTH;
		int processedItems = 0;
		for (int c = 0; c < in.getNumChannels(); c++)
		{
			for (int x1 = 0, x2 = WIDTH - 1; x1 < WIDTH; x1++, x2--)
			{
				for (int y = 0; y < HEIGHT; y++)
				{
					out.putSample(c, x2, y, in.getSample(c, x1, y));
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
