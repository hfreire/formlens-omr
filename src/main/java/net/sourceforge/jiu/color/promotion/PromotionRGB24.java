/*
 * PromotionRGB24
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.promotion;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Converts several image types to RGB.
 * Promoting is a lossless operation that will only lead to an output image
 * that holds the same image in a way that demands more memory.
 * <p>
 * If you give an image implementing RGB24Image to this operation, a
 * WrongParameterException will be thrown.
 * This operation could also return the input image, but this might lead
 * to the wrong impression that a copy of the input was produced which
 * can be modified without changing the original.
 *
 * @author Marco Schmidt
 */
public class PromotionRGB24 extends ImageToImageOperation
{
	private void prepare(PixelImage in) throws
		MissingParameterException,
		WrongParameterException
	{
		if (in == null)
		{
			throw new MissingParameterException("Missing input image.");
		}
		if (!
		     (
		      (in instanceof BilevelImage) || 
		      (in instanceof Paletted8Image) || 
		      (in instanceof Gray8Image)
		     )
		   )
		{
			throw new WrongParameterException("Unsupported input image type: " + in.getClass().getName());
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			setOutputImage(new MemoryRGB24Image(in.getWidth(), in.getHeight()));
		}
		else
		{
			if (!(out instanceof RGB24Image))
			{
				throw new WrongParameterException("Specified output image type must be of class RGB24Image; got " + in.getClass().getName());
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

	private void process(BilevelImage in, RGB24Image out)
	{
		final int HEIGHT = in.getHeight();
		final byte MAX = (byte)255;
		final byte MIN = (byte)0;
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				if (in.isBlack(x, y))
				{
					out.putByteSample(RGBIndex.INDEX_RED, x, y, MIN);
					out.putByteSample(RGBIndex.INDEX_GREEN, x, y, MIN);
					out.putByteSample(RGBIndex.INDEX_BLUE, x, y, MIN);
				}
				else
				{
					out.putByteSample(RGBIndex.INDEX_RED, x, y, MAX);
					out.putByteSample(RGBIndex.INDEX_GREEN, x, y, MAX);
					out.putByteSample(RGBIndex.INDEX_BLUE, x, y, MAX);
				}
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(Paletted8Image in, RGB24Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		final Palette PAL = in.getPalette();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int value = in.getSample(0, x, y);
				out.putSample(RGBIndex.INDEX_RED, x, y, PAL.getSample(RGBIndex.INDEX_RED, value));
				out.putSample(RGBIndex.INDEX_GREEN, x, y, PAL.getSample(RGBIndex.INDEX_GREEN, value));
				out.putSample(RGBIndex.INDEX_BLUE, x, y, PAL.getSample(RGBIndex.INDEX_BLUE, value));
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(Gray8Image in, RGB24Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int value = in.getSample(0, x, y);
				out.putSample(RGBIndex.INDEX_RED, x, y, value);
				out.putSample(RGBIndex.INDEX_GREEN, x, y, value);
				out.putSample(RGBIndex.INDEX_BLUE, x, y, value);
			}
			setProgress(y, HEIGHT);
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		PixelImage in = getInputImage();
		prepare(in);
		RGB24Image out = (RGB24Image)getOutputImage();
		if (in instanceof BilevelImage)
		{
			process((BilevelImage)in, out);
		}
		else
		if (in instanceof Gray8Image)
		{
			process((Gray8Image)in, out);
		}
		else
		if (in instanceof Paletted8Image)
		{
			process((Paletted8Image)in, out);
		}
	}
}
