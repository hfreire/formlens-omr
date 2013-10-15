/*
 * ReduceToBilevelThreshold
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.reduction;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Reduces a {@link net.sourceforge.jiu.data.GrayIntegerImage} to a 
 * {@link net.sourceforge.jiu.data.BilevelImage} by setting all values below
 * a certain threshold value to black and everything else to white.
 * <h3>Default value</h3>
 * If no threshold is specified via {@link #setThreshold(int)}, this operation
 * uses a default value of ({@link net.sourceforge.jiu.data.IntegerImage#getMaxSample(int)} + 1) / 2.
 * <h3>Usage example</h3>
 * This example sets all values below 33 percent luminance to black,
 * everything else to white.
 * <pre>
 * GrayIntegerImage image = ...;
 * ReduceToBilevelThreshold red = new ReduceToBilevelThreshold();
 * red.setInputImage(image);
 * red.setThreshold(image.getMaxSample(0) / 3);
 * red.process();
 * BilevelImage reducedImage= (BilevelImage)red.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class ReduceToBilevelThreshold extends ImageToImageOperation
{
	private Integer threshold;

	/**
	 * Returns the current threshold value, or <code>null</code> if
	 * none was specified and the operation's process method was not
	 * run yet.
	 * @return threshold value
	 */
	public Integer getThreshold()
	{
		return threshold;
	}

	private void process(GrayIntegerImage in, BilevelImage out) throws WrongParameterException
	{
		final int MAX_SAMPLE = in.getMaxSample(0);
		if (threshold == null)
		{
			threshold = new Integer((MAX_SAMPLE + 1) / 2);
		}
		final int THRESHOLD = threshold.intValue();
		if (THRESHOLD > MAX_SAMPLE)
		{
			throw new WrongParameterException("Threshold must be smaller than or equal to the maximum sample of the input image.");
		}
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		out.clear(BilevelImage.BLACK);
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				if (in.getSample(0, x, y) >= THRESHOLD)
				{
					out.putWhite(x, y);
				}
			}
			setProgress(y, HEIGHT);
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		PixelImage in = getInputImage();
		if (in == null)
		{
			throw new MissingParameterException("Input image missing.");
		}
		if (!(in instanceof GrayIntegerImage))
		{
			throw new WrongParameterException("Input image must implement GrayIntegerImage.");
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			out = new MemoryBilevelImage(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		if (out != null && !(out instanceof BilevelImage))
		{
			throw new WrongParameterException("Output image must implement BilevelImage.");
		}
		if (out != null && (in.getWidth() != out.getWidth() || in.getHeight() != out.getHeight()))
		{
			throw new WrongParameterException("Input and output images must have the same resolution.");
		}
		process((GrayIntegerImage)in, (BilevelImage)out);
	}

	/**
	 * Sets a new threshold value.
	 * @param newThreshold the new threshold value to be used for this operation
	 * @throws IllegalArgumentException if the threshold value is negative
	 */
	public void setThreshold(int newThreshold)
	{
		if (newThreshold < 0)
		{
			throw new IllegalArgumentException("New threshold value must be 0 or larger.");
		}
		threshold = new Integer(newThreshold);
	}
}
