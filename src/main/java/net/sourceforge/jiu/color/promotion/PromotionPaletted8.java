/*
 * PromotionPaletted8
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.promotion;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Converts {@link BilevelImage} and {@link Gray8Image} objects to 
 * {@link Paletted8Image} objects.
 * This lossless operation will only lead to an output image
 * that holds the input image in a way that demands more memory.
 *
 * @author Marco Schmidt
 * @since 0.8.0
 */
public class PromotionPaletted8 extends ImageToImageOperation
{
	private void prepare(PixelImage in) throws
		MissingParameterException,
		WrongParameterException
	{
		if (in == null)
		{
			throw new MissingParameterException("Missing input image.");
		}
		Palette palette = null;
		if (in instanceof BilevelImage)
		{
			palette = new Palette(2, 255);
			palette.put(0, 0, 0, 0);
			palette.put(1, 255, 255, 255);
		}
		else
		if (in instanceof Gray8Image)
		{
			palette = new Palette(256, 255);
			for (int i = 0; i < 256; i++)
			{
				palette.put(i, i, i, i);
			}
		}
		else
		{
			throw new WrongParameterException("Unsupported input image type: " + in.getClass().getName());
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			setOutputImage(new MemoryPaletted8Image(in.getWidth(), in.getHeight(), palette));
		}
		else
		{
			if (!(out instanceof Paletted8Image))
			{
				throw new WrongParameterException("Specified output image type must be of class Paletted8Image; got " + in.getClass().getName());
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

	private void process(BilevelImage in, Paletted8Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				if (in.isBlack(x, y))
				{
					out.putByteSample(0, x, y, (byte)0);
				}
				else
				{
					out.putByteSample(0, x, y, (byte)1);
				}
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(Gray8Image in, Paletted8Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		// simple copy
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				out.putSample(0, x, y, in.getSample(0, x, y));
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
		Paletted8Image out = (Paletted8Image)getOutputImage();
		if (in instanceof BilevelImage)
		{
			process((BilevelImage)in, out);
		}
		else
		if (in instanceof Gray8Image)
		{
			process((Gray8Image)in, out);
		}
	}
}
