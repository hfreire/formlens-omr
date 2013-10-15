/*
 * PromotionRGB48
 * 
 * Copyright (c) 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.promotion;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryRGB48Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Converts several image types to {@link net.sourceforge.jiu.data.RGB48Image}.
 * Promotion is a lossless operation that will only lead to an output image
 * that holds the same image in a way that demands more memory.
 * <p>
 * If you give an image implementing RGB24Image to this operation, a
 * WrongParameterException will be thrown.
 * This operation could also return the input image, but this might lead
 * to the wrong impression that a copy of the input was produced which
 * can be modified without changing the original.
 * @author Marco Schmidt
 * @since 0.12.0
 */
public class PromotionRGB48 extends ImageToImageOperation
{
	private void prepare(PixelImage in) throws
		MissingParameterException,
		WrongParameterException
	{
		if (!
		     (
		      (in instanceof BilevelImage) || 
		      (in instanceof Paletted8Image) || 
			  (in instanceof Gray16Image)|| 
			  (in instanceof Gray8Image) ||
			  (in instanceof RGB24Image)
		     )
		   )
		{
			throw new WrongParameterException("Unsupported input image type: " + in.getClass().getName());
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			setOutputImage(new MemoryRGB48Image(in.getWidth(), in.getHeight()));
		}
		else
		{
			if (!(out instanceof RGB48Image))
			{
				throw new WrongParameterException("Specified output image type " +
					"must be of class RGB48Image; got " + in.getClass().getName());
			}
			ensureImagesHaveSameResolution();
		}
	}

	private void process(BilevelImage in, RGB48Image out)
	{
		final int HEIGHT = in.getHeight();
		final short MAX = (short)65535;
		final short MIN = (short)0;
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				if (in.isBlack(x, y))
				{
					out.putShortSample(RGBIndex.INDEX_RED, x, y, MIN);
					out.putShortSample(RGBIndex.INDEX_GREEN, x, y, MIN);
					out.putShortSample(RGBIndex.INDEX_BLUE, x, y, MIN);
				}
				else
				{
					out.putShortSample(RGBIndex.INDEX_RED, x, y, MAX);
					out.putShortSample(RGBIndex.INDEX_GREEN, x, y, MAX);
					out.putShortSample(RGBIndex.INDEX_BLUE, x, y, MAX);
				}
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(Paletted8Image in, RGB48Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		final Palette PAL = in.getPalette();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int value = in.getSample(0, x, y);
				int red = PAL.getSample(RGBIndex.INDEX_RED, value);
				int green = PAL.getSample(RGBIndex.INDEX_GREEN, value);
				int blue = PAL.getSample(RGBIndex.INDEX_BLUE, value);
				out.putSample(RGBIndex.INDEX_RED, x, y, red | red << 8);
				out.putSample(RGBIndex.INDEX_GREEN, x, y, green | green << 8);
				out.putSample(RGBIndex.INDEX_BLUE, x, y, blue | blue << 8);
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(Gray16Image in, RGB48Image out)
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

	private void process(Gray8Image in, RGB48Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int value = in.getSample(0, x, y);
				value = value | value << 8;
				out.putSample(RGBIndex.INDEX_RED, x, y, value);
				out.putSample(RGBIndex.INDEX_GREEN, x, y, value);
				out.putSample(RGBIndex.INDEX_BLUE, x, y, value);
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(RGB24Image in, RGB48Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y);
				red = red << 8 | red;
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y);
				green = green << 8 | green;
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y);
				blue = blue << 8 | blue;
				out.putSample(RGBIndex.INDEX_RED, x, y, red);
				out.putSample(RGBIndex.INDEX_GREEN, x, y, green);
				out.putSample(RGBIndex.INDEX_BLUE, x, y, blue);
			}
			setProgress(y, HEIGHT);
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		prepare(in);
		RGB48Image out = (RGB48Image)getOutputImage();
		if (in instanceof BilevelImage)
		{
			process((BilevelImage)in, out);
		}
		else
		if (in instanceof Gray16Image)
		{
			process((Gray16Image)in, out);
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
		else
		if (in instanceof RGB24Image)
		{
			process((RGB24Image)in, out);
		}
	}
}
