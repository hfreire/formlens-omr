/*
 * RGBToGrayConversion
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.reduction;

import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.MemoryGray16Image;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Converts RGB color images (both truecolor and paletted) to grayscale images.
 * The weights to be used with the three base colors red, green and blue can be 
 * modified with a call to
 * {@link #setColorWeights(float, float, float)}.
 * <h3>Supported image types</h3>
 * {@link RGB24Image} and {@link Paletted8Image} can be used as input image types.
 * A {@link Gray8Image} be will be created from them.
 * <p>
 * Could be optimized to use int multiplication instead of float multiplication.
 * <h3>Usage example</h3>
 * Convert some PixelImage rgbImage to grayscale:
 * <pre>PixelImage grayImg = RGBToGrayConversion.convert(rgbImage);</pre>
 * Using your own color weights can be done like this.
 * You may also want to specify an output grayscale image if you have
 * one to reuse.
 * <pre>
 * RGBToGrayConversion rgbtogray = new RGBToGrayConversion();
 * rgbtogray.setInputImage(image);
 * rgbtogray.setColorWeights(0.3f, 0.3f, 0.4f);
 * rgbtogray.process();
 * PixelImage grayImage = rgbtogray.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class RGBToGrayConversion extends ImageToImageOperation
{
	/**
	 * The default weight for red samples in the conversion, 0.3f.
	 */
	public static final float DEFAULT_RED_WEIGHT = 0.3f;

	/**
	 * The default weight for green samples in the conversion, 0.59f.
	 */
	public static final float DEFAULT_GREEN_WEIGHT = 0.59f;

	/**
	 * The default weight for blue samples in the conversion, 0.11f.
	 */
	public static final float DEFAULT_BLUE_WEIGHT = 0.11f;

	private float redWeight = DEFAULT_RED_WEIGHT;
	private float greenWeight = DEFAULT_GREEN_WEIGHT;
	private float blueWeight = DEFAULT_BLUE_WEIGHT;

	/**
	 * Static convenience method to convert an RGB image to a grayscale image.
	 * @param rgbImage input RGB image to be converted
	 * @return a new grayscale image, created from the RGB input image
	 * @throws MissingParameterException rgbImage is null
	 * @throws WrongParameterException rgbImage's type is unsupported
	 * @since 0.14.2
	 */
	public static PixelImage convert(PixelImage rgbImage) throws 
		MissingParameterException, WrongParameterException
	{
		RGBToGrayConversion op = new RGBToGrayConversion();
		op.setInputImage(rgbImage);
		op.process();
		return op.getOutputImage();
	}

	private void process(RGBIntegerImage in, GrayIntegerImage out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y);
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y);
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y);
				out.putSample(x, y, (int)(red * redWeight + green * greenWeight + blue * blueWeight));
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		if (in instanceof RGB24Image)
		{
			process((RGB24Image)in);
		}
		else
		if (in instanceof RGB48Image)
		{
			process((RGB48Image)in);
		}
		else
		if (in instanceof Paletted8Image)
		{
			process((Paletted8Image)in);
		}
		else
		{
			throw new WrongParameterException("Type of input image unsupported: " +  in.getImageType().getName());
		}
	}

	private void process(Paletted8Image in) throws
		MissingParameterException,
		WrongParameterException
	{
		PixelImage image = getOutputImage();
		Gray8Image out = null;
		if (image == null)
		{
			out = new MemoryGray8Image(in.getWidth(), in.getHeight());
		}
		else
		{
			if (!(image instanceof Gray8Image))
			{
				throw new WrongParameterException("Specified output image must be of type Gray8Image for input image of type Paletted8Image.");
			}
			out = (Gray8Image)image;
			ensureImagesHaveSameResolution();
		}
		Palette palette = in.getPalette();
		int[] lut = new int[palette.getNumEntries()];
		for (int i = 0; i < lut.length; i++)
		{
			int red = palette.getSample(RGBIndex.INDEX_RED, i);
			int green = palette.getSample(RGBIndex.INDEX_GREEN, i);
			int blue = palette.getSample(RGBIndex.INDEX_BLUE, i);
			lut[i] = (int)(red * redWeight + green * greenWeight + blue * blueWeight);
		}
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				try
				{
					out.putSample(0, x, y, lut[in.getSample(0, x, y)]);
				}
				catch (ArrayIndexOutOfBoundsException aioobe)
				{
				}
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	private void process(RGB24Image in) throws WrongParameterException
	{
		PixelImage out = getOutputImage();
		if (out == null)
		{
			out = new MemoryGray8Image(in.getWidth(), in.getHeight());
		}
		else
		{
			if (!(out instanceof Gray8Image))
			{
				throw new WrongParameterException("Specified output image must be of type Gray8Image for input image of type RGB24Image.");
			}
			ensureImagesHaveSameResolution();
		}
		process(in, (GrayIntegerImage)out);
	}

	private void process(RGB48Image in) throws WrongParameterException
	{
		PixelImage out = getOutputImage();
		if (out == null)
		{
			out = new MemoryGray16Image(in.getWidth(), in.getHeight());
		}
		else
		{
			if (!(out instanceof Gray16Image))
			{
				throw new WrongParameterException("Specified output image must be of type Gray16Image for input image of type RGB48Image.");
			}
			ensureImagesHaveSameResolution();
		}
		process(in, (GrayIntegerImage)out);
	}

	/**
	 * Sets the weights for the three colors red, green and blue used in the conversion procedure.
	 * For each RGB value <code>(r, g, b)</code> to be converted (whether in a truecolor 
	 * image or in the palette), the formula is <code>gray = r * red + g * green + b * blue</code>.
	 * The default values for these weights are {@link #DEFAULT_RED_WEIGHT}, 
	 * {@link #DEFAULT_GREEN_WEIGHT} and {@link #DEFAULT_BLUE_WEIGHT}.
	 * This method lets the user change these values.
	 * Each of these arguments must be &gt;= 0.0f and &lt;= 1.0f.
	 * The sum of the three must be &lt;= 1.0f.
	 * For any resulting gray value to be spread over the complete scale from 0.0f to 1.0f it is
	 * preferable for the sum to be equal to or at least close to 1.0f.
	 * However, this is not checked.
	 * The smaller the sum of the weights is, the darker the resulting gray image will become.
	 * @param red weight of the red sample in the conversion, between <code>0.0f</code> and <code>1.0f</code>
	 * @param green weight of the green sample in the conversion, between <code>0.0f</code> and <code>1.0f</code>
	 * @param blue weight of the blue sample in the conversion, between <code>0.0f</code> and <code>1.0f</code>
	 * @throws IllegalArgumentException if any one of the above mentioned constraints for the arguments is not met
	 */
	public void setColorWeights(float red, float green, float blue)
	{
		if (red < 0.0f)
		{
			throw new IllegalArgumentException("The red weight must be larger than or equal to 0; got " + red);
		}
		if (green < 0.0f)
		{
			throw new IllegalArgumentException("The green weight must be larger than or equal to 0; got " + green);
		}
		if (blue < 0.0f)
		{
			throw new IllegalArgumentException("The blue weight must be larger than or equal to 0; got " + blue);
		}
		if (red > 1.0f)
		{
			throw new IllegalArgumentException("The red weight must be smaller than or equal to 1.0f; got " + red);
		}
		if (green > 1.0f)
		{
			throw new IllegalArgumentException("The green weight must be smaller than or equal to 1.0f; got " + green);
		}
		if (blue > 1.0f)
		{
			throw new IllegalArgumentException("The blue weight must be smaller than or equal to 1.0f; got " + blue);
		}
		float sum = red + green + blue;
		if (sum > 1.0f)
		{
			throw new IllegalArgumentException("The sum of the three weights must be smaller than or equal to 1.0f; got " + sum);
		}
		redWeight = red;
		greenWeight = green;
		blueWeight = blue;
	}
}
