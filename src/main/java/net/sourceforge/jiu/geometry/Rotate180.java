/*
 * Rotate180
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
 * Rotates images by 180 degrees.
 * The result is the same as a a {@link Flip} operation followed by a {@link Mirror} operation (or vice versa).
 * Input image must implement {@link net.sourceforge.jiu.data.IntegerImage}.
 * <h3>Usage example</h3>
 * <pre>
 * Rotate180 rotate = new Rotate180();
 * rotate.setInputImage(image); // something implementing IntegerImage
 * rotate.process();
 * PixelImage rotatedImage = rotate.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class Rotate180 extends ImageToImageOperation
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
		int totalItems = in.getNumChannels() * HEIGHT;
		int processedItems = 0;
		for (int c = 0; c < in.getNumChannels(); c++)
		{
			for (int y1 = 0, y2 = HEIGHT - 1; y1 < HEIGHT; y1++, y2--)
			{
				for (int x1 = 0, x2 = WIDTH - 1; x1 < WIDTH; x1++, x2--)
				{
					out.putSample(c, x2, y2, in.getSample(c, x1, y1));
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
		PixelImage image = getInputImage();
		if (image instanceof IntegerImage)
		{
			process((IntegerImage)image, (IntegerImage)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Input image must implement IntegerImage.");
		}
	}
}
