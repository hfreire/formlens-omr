/*
 * ScaleReplication
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Changes the pixel resolution of an image by replicating (or dropping) pixels.
 * A fast but low quality scaling algorithm that works with all kinds
 * of image types.
 * {@link Resample} provides better quality, but is slower and works with
 * intensity-based image data types only.
 *
 * <h3>Usage example</h3>
 *
 * The input image will be scaled to an image that is twice as wide as
 * itself and three times as high.
 *
 * <pre>
 * ScaleReplication scale = new ScaleReplication();
 * scale.setInputImage(image); // something implementing IntegerImage
 * scale.setSize(image.getWidth() * 2, image.getHeight() * 2);
 * scale.process();
 * PixelImage scaledImage = scale.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class ScaleReplication extends ImageToImageOperation
{
	private Integer outWidth;
	private Integer outHeight;

	private void process(IntegerImage in, IntegerImage out)
	{
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(outWidth.intValue(), outHeight.intValue());
			setOutputImage(out);
		}
		int IN_MAX_X = in.getWidth() - 1;
		int IN_MAX_Y = in.getHeight() - 1;
		int OUT_WIDTH = outWidth.intValue();
		int OUT_HEIGHT = outHeight.intValue();
		for (int y = 0; y < OUT_HEIGHT; y++)
		{
			final int SRC_Y = (int)(IN_MAX_Y * (y + 1) / OUT_HEIGHT);
			for (int x = 0; x < OUT_WIDTH; x++)
			{
				final int SRC_X = (int)(IN_MAX_X * (x + 1) / OUT_WIDTH);
				for (int c = 0; c < in.getNumChannels(); c++)
				{
					out.putSample(c, x, y, in.getSample(c, SRC_X, SRC_Y));
				}
			}
			setProgress(y, OUT_HEIGHT);
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		PixelImage pin = getInputImage();
		if (pin == null)
		{
			throw new MissingParameterException("Input image object missing.");
		}
		if (!(pin instanceof IntegerImage))
		{
			throw new WrongParameterException("ScaleReplication only works on IntegerImage objects.");
		}
		if (outWidth == null)
		{
			throw new MissingParameterException("Output width value missing.");
		}
		if (outHeight == null)
		{
			throw new MissingParameterException("Output height value missing.");
		}
		ensureImagesHaveSameResolution();
		process((IntegerImage)pin, (IntegerImage)getOutputImage());
	}

	/**
	 * Specify the resolution to be used for the image to be created.
	 * @param width horizontal resolution of the new image
	 * @param height vertical resolution of the new image
	 * @throws IllegalArgumentException if any of the arguments is smaller than 1
	 */
	public void setSize(int width, int height)
	{
		if (width < 1)
		{
			throw new IllegalArgumentException("Output width must be larger than 0.");
		}
		if (height < 1)
		{
			throw new IllegalArgumentException("Output height must be larger than 0.");
		}
		outWidth = new Integer(width);
		outHeight = new Integer(height);
	}
}
