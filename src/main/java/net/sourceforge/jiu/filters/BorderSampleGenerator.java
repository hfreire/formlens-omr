/*
 * BorderSampleGenerator
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.data.IntegerImage;

/**
 * Abstract base class for classes that fill an <code>int</code> array with samples
 * from a rectangular region of an image's channel by
 * (1) copying <code>int</code> samples from an {@link net.sourceforge.jiu.data.IntegerImage} object 
 * and by (2) generating samples that lie outside of the image.
 * To be used by {@link ConvolutionKernelFilter} and other operations 
 * that require rectangular parts of an image that may not lie fully
 * inside of the image.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public abstract class BorderSampleGenerator
{
	private int areaWidth;
	private int areaHeight;
	private int channelIndex;
	private IntegerImage image;

	/**
	 * Initialize width and height of the area to be covered in every call to
	 * {@link #fill}, also provides the image to be used for data copying.
	 * The current channel is set to 0.
	 * @param integerImage the image from which samples will be copied
	 * @param areaWidth number of columns of the area to be covered in {@link #fill}
	 * @param areaHeight number of rows of the area to be covered in {@link #fill}
	 */
	public BorderSampleGenerator(IntegerImage integerImage, int areaWidth, int areaHeight)
	{
		image = integerImage;
		if (image == null)
		{
			throw new IllegalArgumentException("The image argument must be non-null.");
		}
		this.areaWidth = areaWidth;
		if (areaWidth < 1 || (areaWidth % 2) == 0)
		{
			throw new IllegalArgumentException("Area width must be a positive odd number.");
		}
		this.areaHeight = areaHeight;
		if (areaHeight < 1 || (areaHeight % 2) == 0)
		{
			throw new IllegalArgumentException("Area height must be a positive odd number.");
		}
	}

	/**
	 * Fills the argument array with samples from the current channel of the image
	 * given to the constructor, generating samples that lie outside of the image.
	 * The samples are copied (or generated) from the row y to row y + areaHeight - 1,
	 * and within each row from column x to x + areaWidth - 1.
	 * <p>
	 * The implementation of this method is left to the child classes.
	 * There are different ways to generate new samples, and each child class
	 * is supposed to implement another way.
	 * Obviously, the child classes also must copy samples from the image.
	 * @param x leftmost column to be copied or generated
	 * @param y top row to be copied or generated
	 * @param samples array to which samples will be written; must have at least
	 *  {@link #getAreaWidth} times {@link #getAreaHeight} elements
	 */
	public abstract void fill(int x, int y, int[] samples);

	/**
	 * Returns the number of rows from which data is copied or generated 
	 * with every call to {@link #fill}.
	 * @return number or rows of a fill area
	 */
	public int getAreaHeight()
	{
		return areaHeight;
	}

	/**
	 * Returns the number of columns from which data is copied or generated 
	 * with every call to {@link #fill}.
	 * @return number or columns of a fill area
	 */
	public int getAreaWidth()
	{
		return areaWidth;
	}

	/**
	 * Returns the index of the channel of the image from which data is copied.
	 * @see #setChannelIndex
	 * @return number or rows
	 */
	public int getChannelIndex()
	{
		return channelIndex;
	}

	/**
	 * Returns the image from which data is copied.
	 * @return image object
	 */
	public IntegerImage getImage()
	{
		return image;
	}

	/**
	 * Sets the channel from which data is copied in {@link #fill}.
	 * @see #getChannelIndex
	 */
	public void setChannelIndex(int newChannelIndex)
	{
		if (newChannelIndex < 0 || newChannelIndex >= image.getNumChannels())
		{
			throw new IllegalArgumentException("Illegal channel index: " + 
				newChannelIndex + " (must be from 0 to " +
				(image.getNumChannels() - 1) + ").");
		}
		else
		{
			channelIndex = newChannelIndex;
		}
	}
}
