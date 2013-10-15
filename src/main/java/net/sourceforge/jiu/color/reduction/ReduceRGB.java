/*
 * ReduceRGB
 *
 * Copyright (c) 2003, 2004 Marco Schmidt.
 * Copyright (c) 2009 Knut Arild Erstad.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.reduction;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.MemoryRGB48Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Reduces the color depth of RGB truecolor images.
 * This class uses a simple approach, it just drops some of the 
 * lowest bits and scales the value back to eight or sixteen bits per sample.
 * <h3>Supported image classes</h3>
 * This class works with {@link net.sourceforge.jiu.data.RGB24Image}
 * and {@link net.sourceforge.jiu.data.RGB48Image}.
 * <h3>Usage example</h3>
 * Reduce a 24 or 48 bits per pixel RGB image to 15 bits per pixel:
 * <pre>
 * PixelImage inputImage = ...; // initialize
 * ReduceRGB reduce = new ReduceRGB();
 * reduce.setBitsPerSample(5);
 * reduce.setInputImage(inputImage);
 * reduce.process();
 * PixelImage reducedImage = reduce.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 * @since 0.12.0
 * @see net.sourceforge.jiu.color.reduction.ReduceShadesOfGray
 */
public class ReduceRGB extends ImageToImageOperation
{
	/**
	 * Number of significant bits per channel in the destination RGB image.
	 */
	private Integer destBits;

	/*private byte[] createByteLut(int inDepth)
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
	}*/


	public void process() throws MissingParameterException, WrongParameterException
	{
		if (destBits == null)
		{
			throw new MissingParameterException(
				"The number of destination bits has not been specified.");
		}
		int bits = destBits.intValue();
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		boolean rgb24 = in instanceof RGB24Image;
		boolean rgb48 = in instanceof RGB48Image;
		if (!(rgb24 || rgb48))
		{
			throw new WrongParameterException(
				"Input image must be either RGB24Image or RGB48Image.");
		}
		if (rgb24 && bits >= 8)
		{
			throw new WrongParameterException(
				"Number of output bits per sample must be 7 or lower for RGB24Image.");
		}
		PixelImage out = getOutputImage();
		int inDepth = 0;
		if (rgb24)
		{
			inDepth = 8;
		}
		if (rgb48)
		{
			inDepth = 16;
		}
		int maxOutputValue = 1;
		int maxShiftedValue = (1 << bits) - 1;
		if (bits <= 8)
		{
			if (out == null)
			{
				out = new MemoryRGB24Image(in.getWidth(), in.getHeight());
				setOutputImage(out);
			}
			else
			{
				if (!(out instanceof RGB24Image))
				{
					throw new WrongParameterException(
						"Output image must be of type RGB24Image.");
				}
			}
			maxOutputValue = 255;
		}
		else
		if (bits <= 16)
		{
			if (out == null)
			{
				out = new MemoryRGB48Image(in.getWidth(), in.getHeight());
				setOutputImage(out);
			}
			else
			{
				if (!(out instanceof RGB48Image))
				{
					throw new WrongParameterException(
						"Output image must be of type RGB48Image.");
				}
			}
			maxOutputValue = 65535;
		}
		else
		{
			throw new WrongParameterException("Can only process up to 16 bits per sample.");
		}
		final int SHIFT = inDepth - bits;
		IntegerImage ii = (IntegerImage)in;
		IntegerImage oo = (IntegerImage)out;
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				for (int c = 0; c < 3; c++)
				{
					int inputSample = ii.getSample(c, x, y);
					int outputSample = ((inputSample >> SHIFT) * maxOutputValue) / maxShiftedValue;
					oo.putSample(c, x, y, outputSample);
				}
			}
			setProgress(y, in.getHeight());
		}
	}

	/**
	 * Specifies the number of bits per sample in the output image.
	 * @param bits number of bits in output image, from 1 to 15
	 * @throws IllegalArgumentException if bits is smaller than 1 or larger than 15
	 */
	public void setBitsPerSample(int bits)
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
