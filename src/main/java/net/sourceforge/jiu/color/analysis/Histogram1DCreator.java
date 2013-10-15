/*
 * Histogram1DCreator
 *
 * Copyright (c) 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.analysis;

import net.sourceforge.jiu.color.data.ArrayHistogram1D;
import net.sourceforge.jiu.color.data.Histogram1D;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * This class creates one-dimensional histograms for images with integer samples.
 * Only {@link net.sourceforge.jiu.data.IntegerImage} objects are supported.
 * <p>
 * Existing histogram objects can be given to this operation to be reused.
 * Give an existing {@link net.sourceforge.jiu.color.data.Histogram1D} object to this operation via 
 * {@link #setHistogram(Histogram1D)}.
 * <p>
 * The histogram can be created for any channel of an IntegerImage.
 * The first channel (index 0) is the default channel.
 * Use {@link #setImage(IntegerImage, int)} to specify another one.
 * <p>
 * <em>Note: Before JIU 0.10.0 there was a single HistogramCreator class.</em>
 * <h3>Usage example</h3>
 * Creates a histogram for the third channel of an image, whose index is two
 * because channels are zero-based:
 * <pre>
 * Histogram1DCreator hc = new Histogram1DCreator();
 * hc.setImage(image, 2);
 * hc.process();
 * Histogram1D hist = hc.getHistogram();
 * </pre>
 * If you just want to learn the number of different samples in an image,
 * there's a static convenience method count for that. Again, we use the
 * third channel:
 * <pre>Integer numUsedSamples = Histogram1DCreator.count(image, 2);</pre>
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class Histogram1DCreator extends Operation
{
	private Histogram1D hist;
	private int channelIndex;
	private IntegerImage image;

	/**
	 * Static convenience method to count the number of different samples
	 * in the first channel of the argument image.
	 * @param image the IntegerImage to be examined
	 * @return the number of different values or null on failure
	 *  (image is <code>null</code>, histogram data structure could not
	 *  be allocated)
	 * @since 0.15.0
	 */
	public static Integer count(IntegerImage image)
	{
		return count(image, 0);
	}

	/**
	 * Static convenience method to count the number of different samples
	 * in one of the channels of the argument image.
	 * @param image the IntegerImage to be examined
	 * @param channelIndex the zero-based index of the channel to use
	 * @return the number of different values or null on failure
	 *  (image is <code>null</code>, channel index is invalid,
	 *  histogram data structure could not be allocated)
	 * @since 0.15.0
	 */
	public static Integer count(IntegerImage image, int channelIndex)
	{
		if (image != null && channelIndex >= 0 && channelIndex < image.getNumChannels())
		{
			try
			{
				Histogram1DCreator hc = new Histogram1DCreator();
				hc.setImage((RGBIntegerImage)image);
				hc.process();
				Histogram1D hist = hc.getHistogram();
				return new Integer(hist.getNumUsedEntries());
			}
			catch (Exception e)
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	private void createHistogramIfNecessary()
	{
		if (hist == null)
		{
			hist = new ArrayHistogram1D(image.getMaxSample(0) + 1);
		}
	}

	/**
	 * Returns the histogram used in this operation.
	 * @return histogram object, newly-created or reused one
	 * @see #setHistogram
	 */
	public Histogram1D getHistogram()
	{
		return hist;
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		if (image == null)
		{
			throw new MissingParameterException("Image parameter missing.");
		}
		createHistogramIfNecessary();
		if (hist.getMaxValue() < image.getMaxSample(channelIndex))
		{
			throw new WrongParameterException("Histogram does not have enough entries.");
		}
		hist.clear();
		final int WIDTH = image.getWidth();
		final int HEIGHT = image.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				hist.increaseEntry(image.getSample(channelIndex, x, y));
			}
			setProgress(y, HEIGHT);
		}
	}

	/**
	 * Sets a histogram object to be used for this operation.
	 * Within {@link #process} it will be checked if this histogram is large enough
	 * for the image.
	 * @see #getHistogram
	 */
	public void setHistogram(Histogram1D histogram)
	{
		hist = histogram;
	}

	/**
	 * Set the image for which the histogram is to be initialized.
	 * The first channel (index 0) is used by default.
	 * @param newImage image object to be used
	 * @see #setImage(IntegerImage, int)
	 */
	public void setImage(IntegerImage newImage)
	{
		setImage(newImage, 0);
	}

	/**
	 * Set the image and the channel index for which the histogram is to be initialized.
	 * @param newImage image object to be used
	 * @param imageChannelIndex must not be negative and must be smaller than newImage.getNumChannels()
	 * @see #setImage(IntegerImage)
	 */
	public void setImage(IntegerImage newImage, int imageChannelIndex)
	{
		if (newImage == null)
		{
			throw new IllegalArgumentException("Image argument must be non-null.");
		}
		if (imageChannelIndex < 0 || imageChannelIndex >= newImage.getNumChannels())
		{
			throw new IllegalArgumentException("Invalid channel for given image.");
		}
		image = newImage;
		channelIndex = imageChannelIndex;
	}
}
