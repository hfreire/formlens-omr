/*
 * Crop
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
 * Copies a rectangular area of one image to another image that is exactly as large
 * as that rectangular area.
 * Works with all image data classes implementing {@link net.sourceforge.jiu.data.IntegerImage}.
 * <em>Make sure to use zero-based parameters when defining the bounds with
 * {@link #setBounds}!</em>
 * <h3>Usage example</h3>
 * In this example we assume that the input image is larger than 20 pixels in both directions.
 * Ten pixels will be removed from any of its four borders.
 * <pre>
 * PixelImage image = ...; // something implementing IntegerImage
 * Crop crop = new Crop();
 * crop.setInputImage(image);
 * crop.setBounds(10, 10, image.getWidth() - 9, image.getHeight() - 9);
 * crop.process();
 * PixelImage croppedImage = crop.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class Crop extends ImageToImageOperation
{
	private int x1;
	private int y1;
	private int x2;
	private int y2;

	private void checkBounds() throws WrongParameterException
	{
		PixelImage in = getInputImage();
		if (x1 >= in.getWidth())
		{
			throw new WrongParameterException("x1 must be smaller than input image width.");
		}
		if (x2 >= in.getWidth())
		{
			throw new WrongParameterException("x2 must be smaller than input image width.");
		}
		if (y1 >= in.getHeight())
		{
			throw new WrongParameterException("y1 must be smaller than input image height.");
		}
		if (y2 >= in.getHeight())
		{
			throw new WrongParameterException("y2 must be smaller than input image height.");
		}
	}

	private void process(IntegerImage in, IntegerImage out)
	{
		final int OUT_WIDTH = x2 - x1 + 1;
		final int OUT_HEIGHT = y2 - y1 + 1;
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(OUT_WIDTH, OUT_HEIGHT);
			setOutputImage(out);
		}
		int totalItems = in.getNumChannels() * OUT_HEIGHT;
		int processedItems = 0;
		for (int c = 0; c < in.getNumChannels(); c++)
		{
			for (int yfrom = y1, yto = 0; yto < OUT_HEIGHT; yfrom++, yto++)
			{
				for (int xfrom = x1, xto = 0; xto < OUT_WIDTH; xfrom++, xto++)
				{
					out.putSample(c, xto, yto, in.getSample(c, xfrom, yfrom));
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
		checkBounds();
		ensureOutputImageResolution(x2 - x1 + 1, y2 - y1 + 1);
		if (getInputImage() instanceof IntegerImage)
		{
			process((IntegerImage)getInputImage(), (IntegerImage)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Input image must implement IntegerImage.");
		}
	}

	/**
	 * Specify the rectangular section of the original image that is to be
	 * copied to the output image by this operation.
	 * Note that the arguments are not checked directly against any input image that may have
	 * been provided to this Crop object, that checking is done later in {@link #process()}.
	 * If any of the arguments provided here are outside of the input image's resolution
	 * (e.g. x1 == 100 although the input image's width is only 60), a 
	 * {@link net.sourceforge.jiu.ops.WrongParameterException} will be thrown from
	 * within {@link #process()}.
	 * <p>
	 * Note that the arguments to this method are zero-based, so the first column and row
	 * are 0, the second 1, the third 2, and so on.
	 * If you have a image that is 200 pixels wide and 100 pixels high,
	 * values from 0 to 199 are valid for the x arguments, and values from 0 to 99 are valid
	 * for the vertical direction.
	 * @param x1 horizontal position of upper left corner of the rectangle
	 * @param y1 vertical position of upper left corner of the rectangle
	 * @param x2 horizontal position of lower right corner of the rectangle
	 * @param y2 vertical position of lower right corner of the rectangle
	 * @throws IllegalArgumentException if any of the arguments is negative or x1 larger than x2 or y1 larger than y2
	 */
	public void setBounds(int x1, int y1, int x2, int y2) throws IllegalArgumentException
	{
		if (x1 < 0)
		{
			throw new IllegalArgumentException("x1 must not be negative.");
		}
		if (y1 < 0)
		{
			throw new IllegalArgumentException("y1 must not be negative.");
		}
		if (x2 < 0)
		{
			throw new IllegalArgumentException("x2 must not be negative.");
		}
		if (y2 < 0)
		{
			throw new IllegalArgumentException("y2 must not be negative.");
		}
		if (x1 > x2)
		{
			throw new IllegalArgumentException("x1 must not be larger than x2.");
		}
		if (y1 > y2)
		{
			throw new IllegalArgumentException("y1 must not be larger than y2.");
		}
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
}
