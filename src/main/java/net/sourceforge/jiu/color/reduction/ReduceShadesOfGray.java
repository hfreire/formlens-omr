/*
 * ReduceShadesOfGray
 *
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.reduction;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray16Image;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Reduces the number of shades of gray of a grayscale image.
 * This class uses the most simple possible algorithm.
 * Only the most significant N bits are kept (where N is the
 * number specified with {@link #setBits}), the others are dropped
 * and the result is scaled back to either 8 or 16 bits to fit
 * into the two grayscale image types.
 * <h3>Supported image classes</h3>
 * This class works with {@link net.sourceforge.jiu.data.Gray8Image}
 * and {@link net.sourceforge.jiu.data.Gray16Image}.
 * <h3>Usage example</h3>
 * Reduce a grayscale image to 3 bit (2<sup>3</sup> = 8 shades of gray):
 * <pre>
 * ReduceShadesOfGray reduce = new ReduceShadesOfGray();
 * reduce.setBits(3);
 * reduce.setInputImage(image); // some Gray8Image or Gray16Image
 * reduce.process();
 * PixelImage reducedImage = reduce.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 * @since 0.3.0
 */
public class ReduceShadesOfGray extends ImageToImageOperation
{
	/**
	 * Number of significant bits in the destination grayscale image.
	 */
	private Integer destBits;

	/**
	 * Lookup table, for each possible input sample stores the
	 * corresponding output sample.
	 */
	private int[] lut;

	private void createLut(int inDepth)
	{
		int outDepth = destBits.intValue();
		lut = new int[1 << inDepth];
		final int SHIFT = inDepth - outDepth;
		final int MAX_IN_VALUE = (1 << inDepth) - 1;
		final int MAX_OUT_VALUE = (1 << outDepth) - 1;
		for (int i = 0; i < lut.length; i++)
		{
			int value = i >> SHIFT;
			lut[i] = (value * MAX_IN_VALUE) / MAX_OUT_VALUE;
		}
	}

	private void process(GrayIntegerImage in, final int MASK, BilevelImage out)
	{
		if (out == null)
		{
			out = new MemoryBilevelImage(in.getWidth(), in.getHeight());
		}
		out.clear(BilevelImage.BLACK);
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				if ((in.getSample(x, y) & MASK) != 0)
				{
					out.putWhite(x, y);
				}
			}
			setProgress(y, in.getHeight());
		}
		setOutputImage(out);
	}

	private void process(GrayIntegerImage in, GrayIntegerImage out)
	{
		//int bits = destBits.intValue();
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(x, y, lut[in.getSample(0, x, y)]);
			}
			setProgress(y, in.getHeight());
		}
		setOutputImage(out);
	}

	public void process() throws MissingParameterException, WrongParameterException
	{
		if (destBits == null)
		{
			throw new MissingParameterException("The number of destination bits has not been specified.");
		}
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		boolean gray8 = in instanceof Gray8Image;
		boolean gray16 = in instanceof Gray16Image;
		if (!(gray8 || gray16))
		{
			throw new WrongParameterException("Input image must be either Gray8Image or Gray16Image.");
		}
		if (destBits.intValue() == 1)
		{
			process((GrayIntegerImage)in, gray8 ? 0x80 : 0x8000, (BilevelImage)getOutputImage());
		}
		else
		if (gray8)
		{
			if (destBits.intValue() > 7)
			{
				throw new WrongParameterException("For a Gray8Image destination bits must be 7 or less.");
			}
			PixelImage out = getOutputImage();
			if (out == null)
			{
				out = new MemoryGray8Image(in.getWidth(), in.getHeight());
			}
			else
			{
				if (!(out instanceof Gray8Image))
				{
					throw new WrongParameterException("For this input image, output image must be a Gray8Image.");
				}
			}
			createLut(8);
			process((GrayIntegerImage)in, (GrayIntegerImage)out);
		}
		else
		if (gray16)
		{
			PixelImage out = getOutputImage();
			if (out == null)
			{
				out = new MemoryGray16Image(in.getWidth(), in.getHeight());
			}
			else
			{
				if (destBits.intValue() <= 8 && !(out instanceof Gray8Image))
				{
					throw new WrongParameterException("For this input image, output image must be a Gray8Image.");
				}
				if (destBits.intValue() <= 15 && !(out instanceof Gray16Image))
				{
					throw new WrongParameterException("For this input image, output image must be a Gray16Image.");
				}
			}
			createLut(16);
			process((GrayIntegerImage)in, (GrayIntegerImage)out);
		}
	}

	/**
	 * Specifies the number of bits the output image is supposed to have.
	 * @param bits number of bits in output image, from 1 to 15
	 * @throws IllegalArgumentException if bits is smaller than 1 or larger than 15
	 */
	public void setBits(int bits)
	{
		if (bits < 1)
		{
			throw new IllegalArgumentException("Number of bits must be 1 or larger.");
		}
		if (bits > 15)
		{
			throw new IllegalArgumentException("Number of bits must be 15 or smaller.");
		}
		destBits = new Integer(bits);
	}
}
