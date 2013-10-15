/*
 * MeanDifference
 * 
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.analysis;

import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * This operation determines the mean difference between two images.
 * It requires two images of the same resolution and adds the absolute difference
 * of all samples.
 * Then it divides by the number of samples in the image (width times height times
 * number of channels).
 * <h3>Supported combinations of image types</h3>
 * <ul>
 * <li>One of the two images is of type {@link net.sourceforge.jiu.data.RGB24Image},
 * the other of type {@link net.sourceforge.jiu.data.Paletted8Image}.</li>
 * <li>Both images are of the same type and that type implements {@link net.sourceforge.jiu.data.RGBIntegerImage}.</li>
 * <li>Both images are of the same type and that type implements {@link net.sourceforge.jiu.data.GrayIntegerImage}.</li>
 * </ul>
 * <h3>Usage example</h3>
 * <pre>Double meanDifference = MeanDifference.compute(image1, image2);</pre>
 * @author Marco Schmidt
 * @since 0.11.0
 */
public class MeanDifference extends Operation
{
	private double diff;
	private PixelImage image1;
	private PixelImage image2;

	/**
	 * Compute the mean difference between two images.
	 * @param image1 first image to be examined
	 * @param image2 second image to be examined
	 * @return sum of all differences divided by number of pixels
	 *  as Double or <code>null</code> on failure (image types
	 *  are incompatible)
	 * @since 0.15.0
	 */
	public static Double compute(PixelImage image1, PixelImage image2)
	{
		MeanDifference diff = new MeanDifference();
		diff.setImages(image1, image2);
		try
		{
			diff.process();
			return new Double(diff.getDifference());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Returns abs(a - b).
	 * @param a first number
	 * @param b second number
	 * @return abs(a - b)
	 */
	private static int computeDiff(int a, int b)
	{
		// code is equal to Math.abs(a - b);
		int diff = a - b;
		if (diff < 0)
		{
			return -diff;
		}
		else
		{
			return diff;
		}
	}

	/**
	 * After a call to process, returns the determined mean difference value. 
	 * @return difference value, 0.0 or larger
	 */
	public double getDifference()
	{
		return diff;
	}

	public void process() throws MissingParameterException, WrongParameterException
	{
		if (image1 == null)
		{
			throw new MissingParameterException("You must specify images using setImages.");
		}
		boolean sameType = image1.getImageType() == image2.getImageType();
		if (image1 instanceof RGB24Image && image2 instanceof Paletted8Image)
		{
			process((RGB24Image)image1, (Paletted8Image)image2);
		}
		else
		if (image2 instanceof RGB24Image && image1 instanceof Paletted8Image)
		{
			process((RGB24Image)image2, (Paletted8Image)image1);
		}
		else
		if (sameType && image1 instanceof RGBIntegerImage)
		{
			process((RGBIntegerImage)image1, (RGBIntegerImage)image2);
		}
		else
		if (sameType && image1 instanceof GrayIntegerImage)
		{
			process((GrayIntegerImage)image1, (GrayIntegerImage)image2);
		}
		else
		{
			throw new WrongParameterException("Not a supported image type combination.");
		}
	}

	private void process(GrayIntegerImage image1, GrayIntegerImage image2)
	{
		final int HEIGHT = image1.getHeight();
		final int WIDTH = image1.getWidth();
		long sum = 0;
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				sum += computeDiff(image1.getSample(x, y), image2.getSample(x, y));
			}
			setProgress(y, HEIGHT);
		}
		setDifference((double)sum / (WIDTH * HEIGHT));
	}

	private void process(RGB24Image image1, Paletted8Image image2)
	{
		final int HEIGHT = image1.getHeight();
		final int WIDTH = image1.getWidth();
		long sum = 0;
		Palette pal = image2.getPalette();
		int[] red = pal.getSamples(RGBIndex.INDEX_RED);
		int[] green = pal.getSamples(RGBIndex.INDEX_GREEN);
		int[] blue = pal.getSamples(RGBIndex.INDEX_BLUE);
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int palSample = image2.getSample(x, y);
				sum += computeDiff(image1.getSample(RGBIndex.INDEX_RED, x, y), red[palSample]);
				sum += computeDiff(image1.getSample(RGBIndex.INDEX_GREEN, x, y), green[palSample]);
				sum += computeDiff(image1.getSample(RGBIndex.INDEX_BLUE, x, y), blue[palSample]);
			}
			setProgress(y, HEIGHT);
		}
		setDifference((double)sum / (WIDTH * HEIGHT * 3));
	}

	private void process(RGBIntegerImage image1, RGBIntegerImage image2)
	{
		final int HEIGHT = image1.getHeight();
		final int WIDTH = image1.getWidth();
		long sum = 0;
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				sum += computeDiff(image1.getSample(RGBIndex.INDEX_RED, x, y), image2.getSample(RGBIndex.INDEX_RED, x, y));
				sum += computeDiff(image1.getSample(RGBIndex.INDEX_GREEN, x, y), image2.getSample(RGBIndex.INDEX_GREEN, x, y));
				sum += computeDiff(image1.getSample(RGBIndex.INDEX_BLUE, x, y), image2.getSample(RGBIndex.INDEX_BLUE, x, y));
			}
			setProgress(y, HEIGHT);
		}
		setDifference((double)sum / (WIDTH * HEIGHT * 3));
	}

	private void setDifference(double newValue)
	{
		diff = newValue;
	}

	/**
	 * Sets the two images for which the mean difference is to be 
	 * determined.
	 * @param firstImage first image
	 * @param secondImage second image
	 * @throws IllegalArgumentException if either of the images is null,
	 *  if their resolution is different or if their types are not supported
	 *  by this operation
	 */
	public void setImages(PixelImage firstImage, PixelImage secondImage)
	{
		if (firstImage == null || secondImage == null)
		{
			throw new IllegalArgumentException("Both image arguments must be non-null.");
		}
		if (firstImage.getWidth() != secondImage.getWidth())
		{
			throw new IllegalArgumentException("The images must have the same width.");
		}
		if (firstImage.getHeight() != secondImage.getHeight())
		{
			throw new IllegalArgumentException("The images must have the same height.");
		}
		image1 = firstImage;
		image2 = secondImage;
	}
}
