/*
 * PromotionGray8
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.promotion;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Converts BilevelImage objects to Gray8Image objects.
 * Promotion is a lossless operation that will lead to an output image
 * that holds the same image in a way that demands more memory.
 *
 * @author Marco Schmidt
 * @since 0.8.0
 */
public class PromotionGray8 extends ImageToImageOperation
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
		      (in instanceof BilevelImage)
		     )
		   )
		{
			throw new WrongParameterException("Unsupported input image type: " + in.getClass().getName());
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			setOutputImage(new MemoryGray8Image(in.getWidth(), in.getHeight()));
		}
		else
		{
			if (!(out instanceof Gray8Image))
			{
				throw new WrongParameterException("Specified output image type must be of class Gray8Image; got " + in.getClass().getName());
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

	private void process(BilevelImage in, Gray8Image out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		final byte MAX = (byte)255;
		final byte MIN = (byte)0;
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				if (in.isBlack(x, y))
				{
					out.putByteSample(0, x, y, MIN);
				}
				else
				{
					out.putByteSample(0, x, y, MAX);
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
		prepare(in);
		Gray8Image out = (Gray8Image)getOutputImage();
		if (in instanceof BilevelImage)
		{
			process((BilevelImage)in, out);
		}
	}
}
