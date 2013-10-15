/*
 * AreaFilterOperation
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Base class for operations that convert images to images and determine
 * an output sample by doing calculations on the input sample at the same
 * position plus some neighboring samples.
 * <p>
 * Override {@link #computeSample} and the operation will work.
 * @since 0.9.0
 * @author Marco Schmidt
 */
public abstract class AreaFilterOperation extends ImageToImageOperation
{
	private int areaWidth;
	private int areaHeight;

	/**
	 * Checks if the argument is a valid area height value.
	 * The default implementation requires the argument to be odd and larger than zero.
	 * Override this method if your extension of AreaFilterOperation requires different heights.
	 * @throws IllegalArgumentException if the argument is not valid
	 */
	public void checkAreaHeight(int height)
	{
		if (height < 1)
		{
			throw new IllegalArgumentException("Height must be larger than 0.");
		}
		if ((height & 1) == 0)
		{
			throw new IllegalArgumentException("Height must be odd.");
		}
	}

	/**
	 * Checks if the argument is a valid area width value.
	 * The default implementation requires the argument to be odd and larger than zero.
	 * Override this method if your extension of AreaFilterOperation requires different widths.
	 * @throws IllegalArgumentException if the argument is not valid
	 */
	public void checkAreaWidth(int width)
	{
		if (width < 1)
		{
			throw new IllegalArgumentException("Width must be larger than 0.");
		}
		if ((width & 1) == 0)
		{
			throw new IllegalArgumentException("Width must be odd.");
		}
	}

	/**
	 * Determine the resulting sample for an array with the source sample
	 * and zero or more of its neighbors.
	 * This abstract method must be implemented by classes extending this operation.
	 * The array will hold <code>numSamples</code> samples, which will be stored
	 * starting at offset <code>0</code>.
	 * <p>
	 * Normally, <code>numSamples</code> is equal to {@link #getAreaWidth} times {@link #getAreaHeight}.
	 * Near the border of the image you may get less samples.
	 * Example: the top left sample of an image has only three neighbors (east, south-east and south), 
	 * so you will only get four samples (three neighbors and the sample itself).
	 * @param samples the array holding the sample(s)
	 * @param numSamples number of samples in the array
	 * @return sample to be written to the output image
	 */
	public abstract int computeSample(int[] samples, int numSamples);

	/**
	 * Returns the current area height.
	 * @return height of area window in pixels
	 * @see #setAreaHeight(int)
	 */
	public int getAreaHeight()
	{
		return areaHeight;
	}

	/**
	 * Returns the current area width.
	 * @return width of area window in pixels
	 * @see #setAreaWidth(int)
	 */
	public int getAreaWidth()
	{
		return areaWidth;
	}

	/**
	 * Applies the filter to one of the channels of an image.
	 */
	private void process(int channelIndex, IntegerImage in, IntegerImage out)
	{
		processBorders(channelIndex, in, out);
		processCenter(channelIndex, in, out);
/*
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		final int H_2 = areaWidth / 2;
		final int V_2 = areaHeight / 2;
		int processedItems = channelIndex * HEIGHT;
		final int TOTAL_ITEMS = in.getNumChannels() * HEIGHT;
		int[] samples = new int[areaWidth * areaHeight];
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				// collect samples from area
				int numSamples = 0;
				for (int v = y - V_2; v <= y + V_2; v++)
				{
					if (v >= 0 && v < HEIGHT)
					{
						for (int u = x - H_2; u <= x + H_2; u++)
						{
							if (u >= 0 && u < WIDTH)
							{
								samples[numSamples++] = in.getSample(channelIndex, u, v);
							}
						}
					}
				}
				// determine and set output sample
				out.putSample(channelIndex, x, y, computeSample(samples, numSamples));
			}
			setProgress(processedItems++, TOTAL_ITEMS);
		}
		*/
	}

	private void process(IntegerImage in, IntegerImage out)
	{
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		for (int channelIndex = 0; channelIndex < in.getNumChannels(); channelIndex++)
		{
			process(channelIndex, in, out);
		}
	}

	public void process() throws 
		MissingParameterException,
		WrongParameterException	
	{
		if (areaWidth == 0)
		{
			throw new MissingParameterException("Area width has not been initialized.");
		}
		if (areaHeight == 0)
		{
			throw new MissingParameterException("Area height has not been initialized.");
		}
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		PixelImage out = getOutputImage();
		if (in instanceof GrayIntegerImage || in instanceof RGBIntegerImage)
		{
			process((IntegerImage)in, (IntegerImage)out);
		}
		else
		{
			throw new WrongParameterException("Input image must implement GrayIntegerImage or RGBIntegerImage.");
		}
	}

	private void processBorders(int channelIndex, IntegerImage in, IntegerImage out)
	{
		/*processBorderNorthWest(channelIndex, in, out);
		processBorderNorth(channelIndex, in, out);
		processBorderNorthEast(channelIndex, in, out);

		processBorderEast(channelIndex, in, out);

		processBorderSouthEast(channelIndex, in, out);
		processBorderSouth(channelIndex, in, out);
		processBorderSouthWest(channelIndex, in, out);

		processBorderWest(channelIndex, in, out);*/
	}

	private void processCenter(int channelIndex, IntegerImage in, IntegerImage out)
	{
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		final int AREA_WIDTH = getAreaWidth();
		final int H_2 = AREA_WIDTH / 2;
		final int AREA_HEIGHT = getAreaHeight();
		final int V_2 = AREA_HEIGHT / 2;
		if (WIDTH < AREA_WIDTH || HEIGHT < AREA_HEIGHT)
		{
			return;
		}
		final int NUM_SAMPLES = AREA_WIDTH * AREA_HEIGHT;
		final int TOTAL_ITEMS = in.getNumChannels() * HEIGHT;
		int processedItems = channelIndex * HEIGHT + AREA_HEIGHT / 2;
		int[] samples = new int[AREA_WIDTH * AREA_HEIGHT];
		for (int y1 = 0, y2 = V_2; y2 < HEIGHT - V_2; y1++, y2++)
		{
			for (int x1 = 0, x2 = H_2; x2 < WIDTH - H_2; x1++, x2++)
			{
				in.getSamples(channelIndex, x1, y1, areaWidth, areaHeight, samples, 0);
				out.putSample(channelIndex, x2, y2, computeSample(samples, NUM_SAMPLES));
			}
			setProgress(processedItems++, TOTAL_ITEMS);
		}
	}

	/**
	 * Sets the area of the window to be used to determine each pixel's mean to
	 * the argument width and height.
	 * @param width width of window, must be 1 or larger
	 * @param height height of window, must be 1 or larger
	 * @see #setAreaHeight
	 * @see #setAreaWidth
	 */
	public void setArea(int width, int height)
	{
		setAreaWidth(width);
		setAreaHeight(height);
	}

	/**
	 * Sets the height of the area of the window to be used to determine each pixel's mean to
	 * the argument value.
	 * @param height height of window, must be odd and 1 or larger
	 * @see #getAreaHeight
	 * @see #setArea
	 * @see #setAreaWidth
	 */
	public void setAreaHeight(int height)
	{
		checkAreaHeight(height);
		areaHeight = height;
	}

	/**
	 * Sets the width of the area of the window to be used to determine each pixel's mean to
	 * the argument value.
	 * @param width width of window, must be odd and 1 or larger
	 * @see #getAreaWidth
	 * @see #setArea
	 * @see #setAreaHeight
	 */
	public void setAreaWidth(int width)
	{
		checkAreaWidth(width);
		areaWidth = width;
	}
}
