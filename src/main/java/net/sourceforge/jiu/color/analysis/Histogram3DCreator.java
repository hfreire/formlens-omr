/*
 * Histogram3DCreator
 *
 * Copyright (c) 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.analysis;

import net.sourceforge.jiu.color.data.Histogram3D;
import net.sourceforge.jiu.color.data.NaiveHistogram3D;
import net.sourceforge.jiu.color.data.OnDemandHistogram3D;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * This class creates three-dimensional histograms for images with integer samples.
 * Only {@link net.sourceforge.jiu.data.IntegerImage} is supported.
 * Existing histogram objects can be given to this operation to be reused.
 * <p>
 * <em>Note: Before JIU 0.10.0 there was a single HistogramCreator class.</em>
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class Histogram3DCreator extends Operation
{
	private Histogram3D hist;
	private IntegerImage image;
	private int index1;
	private int index2;
	private int index3;
	private boolean naive;

	/**
	 * Static convenience method to count the number of colors in
	 * any three channel {@link IntegerImage} object.
	 * @param image the IntegerImage whose number of used colors is to be determined
	 * @return the number of colors or null on failure (image does not have three
	 *  channels, etc.)
	 * @since 0.15.0
	 */
	public static Integer count(IntegerImage image)
	{
		if (image.getNumChannels() == 3)
		{
			try
			{
				Histogram3DCreator hc = new Histogram3DCreator();
				hc.setImage((RGBIntegerImage)image);
				hc.process();
				Histogram3D hist = hc.getHistogram();
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
			if (naive)
			{
				hist = new NaiveHistogram3D(image.getMaxSample(index1) + 1, 
					image.getMaxSample(index2) + 1, 
					image.getMaxSample(index3) + 1);
			}
			else
			{
				hist = new OnDemandHistogram3D(image.getMaxSample(index1) + 1, 
					image.getMaxSample(index2) + 1, 
					image.getMaxSample(index3) + 1);
			}
		}
	}

	/**
	 * Returns the histogram initialized in this operation.
	 */
	public Histogram3D getHistogram()
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
		if (hist.getMaxValue(0) < image.getMaxSample(index1) ||
		    hist.getMaxValue(1) < image.getMaxSample(index2) ||
		    hist.getMaxValue(2) < image.getMaxSample(index3))
		{
			throw new WrongParameterException("Histogram is not large enough for image (hist max value / image max samples).");
		}
		hist.clear();
		final int WIDTH = image.getWidth();
		final int HEIGHT = image.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				hist.increaseEntry(
					image.getSample(index1, x, y),
					image.getSample(index2, x, y), 
					image.getSample(index3, x, y));
			}
			setProgress(y, HEIGHT);
		}
	}

	/**
	 * Sets the histogram object to be reused for this operation.
	 * If this method is not called, a new histogram will be created.
	 * @param histogram the histogram object to be used in this operation
	 */
	public void setHistogram3D(Histogram3D histogram)
	{
		hist = histogram;
	}

	/**
	 * The image for which a histogram will be initialized.
	 * Simply calls {@link #setImage(IntegerImage, int, int, int)} 
	 * with 0, 1 and 2 as parameters.
	 * @param newImage the image for the histogram initialization
	 */
	public void setImage(IntegerImage newImage)
	{
		setImage(newImage, 0, 1, 2);
	}

 	/**
	 * The image for which a histogram will be initialized.
	 * Simply calls {@link #setImage(IntegerImage, int, int, int)} 
	 * with 0, 1 and 2 as parameters.
	 * @param newImage
	 */
	public void setImage(IntegerImage newImage, int channelIndex1, int channelIndex2, int channelIndex3)
	{
		if (newImage == null)
		{
			throw new IllegalArgumentException("Image argument must not be null.");
		}
		
		if (channelIndex1 < 0 || channelIndex1 >= newImage.getNumChannels() ||
		    channelIndex2 < 0 || channelIndex2 >= newImage.getNumChannels() ||
		    channelIndex3 < 0 || channelIndex3 >= newImage.getNumChannels())
		{
			throw new IllegalArgumentException("The three index arguments must be >= 0 and < the number of channels.");
		}
		if (channelIndex1 == channelIndex2 || channelIndex2 == channelIndex3 || channelIndex1 == channelIndex3)
		{
			throw new IllegalArgumentException("The three index arguments must be different from each other.");
		}
		image = newImage;
		index1 = channelIndex1;
		index2 = channelIndex2;
		index3 = channelIndex3;
	}
}
