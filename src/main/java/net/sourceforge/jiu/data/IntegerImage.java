/*
 * IntegerImage
 *
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

/**
 * Extends the {@link PixelImage} interface to access image data as <code>int</code> values.
 * Image types based on <code>byte</code>, <code>char</code>, <code>short</code> and <code>int</code> will work with this interface.
 * <code>long</code> will not.
 * <p>
 * Using this interface provides a nice way of accessing a large variety of 
 * image types, but for performance reasons it might be preferable to use 
 * one of the class-specific access methods that get or put several values 
 * at a time, e.g. getByteSamples in {@link ByteChannelImage}.
 *
 * @author Marco Schmidt
 */
public interface IntegerImage extends PixelImage
{
	/**
	 * Sets all samples in the first channel to the argument value.
	 * Equal to <code>clear(0, newValue);</code>:
	 */
	void clear(int newValue);

	/**
	 * Sets all samples of the <code>channelIndex</code>'th channel to <code>newValue</code>.
	 */
	void clear(int channelIndex, int newValue);

	/**
	 * Returns the maximum value for one of the image's channels.
	 * The minimum value is always <code>0</code>.
	 * @param channel zero-based index of the channel, from <code>0</code> to {@link #getNumChannels()}<code> - 1</code>
	 * @return maximum allowed sample value
	 */
	int getMaxSample(int channel);

	/**
	 * Returns one sample of the first channel (index 0).
	 * A call to this method must have the same result as the call <code>getSample(0, x, y);</code>.
	 * @param x the horizontal position of the sample, from <code>0</code> to {@link #getWidth} <code>- 1</code>
	 * @param y the vertical position of the sample, from <code>0</code> to {@link #getHeight} <code>- 1</code>
	 * @return the desired sample
	 */
	int getSample(int x, int y);

	/**
	 * Returns one sample, specified by its channel index and location.
	 * @param channel the number of the channel, from <code>0</code> to {@link #getNumChannels} <code>- 1</code>
	 * @param x the horizontal position of the sample, from <code>0</code> to {@link #getWidth} <code>- 1</code>
	 * @param y the vertical position of the sample, from <code>0</code> to {@link #getHeight} <code>- 1</code>
	 * @return the desired sample
	 */
	int getSample(int channel, int x, int y);

	/**
	 * Copies a number of samples from this image to an <code>int[]</code> object.
	 * A rectangular part of one channel is copied.
	 * The channel index is given by  - the upper left corner of
	 * that rectangle is given by the point x / y.
	 * Width and height of that rectangle are given by w and h.
	 * Each sample will be stored as one <code>int</code> value dest,
	 * starting at index destOffs.
	 * @param channelIndex zero-based index of the channel from which data is to be copied (valid values: 0 to {@link #getNumChannels()} - 1)
	 * @param x horizontal position of upper left corner of the rectangle to be copied
	 * @param y vertical position of upper left corner of the rectangle to be copied
	 * @param w width of rectangle to be copied
	 * @param h height of rectangle to be copied
	 * @param dest int array to which the samples will be copied
	 * @param destOffs int index into the dest array for the position to which the samples will be copied
	 */
	void getSamples(int channelIndex, int x, int y, int w, int h, int[] dest, int destOffs);

	/**
	 * This method sets one sample of the first channel (index 0) to a new value.
	 * This call must have the same result as the call <code>putSample(0, x, y)</code>.
	 * The sample location is given by the spatial coordinates, x and y.
	 * @param x the horizontal position of the sample, from <code>0</code> to {@link #getWidth} <code>- 1</code>
	 * @param y the vertical position of the sample, from <code>0</code> to {@link #getHeight} <code>- 1</code>
	 * @param newValue the new value of the sample
	 */
	void putSample(int x, int y, int newValue);

	/**
	 * This method sets one sample to a new value.
	 * The sample location is given by the channel index and the spatial coordinates, x and y.
	 * @param channel the number of the channel, from <code>0</code> to {@link #getNumChannels} <code>- 1</code>
	 * @param x the horizontal position of the sample, from <code>0</code> to {@link #getWidth} <code>- 1</code>
	 * @param y the vertical position of the sample, from <code>0</code> to {@link #getHeight} <code>- 1</code>
	 * @param newValue the new value of the sample
	 */
	void putSample(int channel, int x, int y, int newValue);

	/**
	 * Copies a number of samples from an <code>int[]</code> array to this image.
	 * A rectangular part of one channel is copied - the upper left corner of
	 * that rectangle is given by the point x / y.
	 * Width and height of that rectangle are given by w and h.
	 * Each sample will be stored as one <code>int</code> value src,
	 * starting at index srcOffset.
	 * @param channel int (from 0 to getNumChannels() - 1) to indicate the channel to which data is copied
	 * @param x horizontal position of upper left corner of the rectangle to be copied
	 * @param y vertical position of upper left corner of the rectangle to be copied
	 * @param w width of rectangle to be copied
	 * @param h height of rectangle to be copied
	 * @param src int array from which the samples will be copied
	 * @param srcOffset int index into the src array for the position from which the samples will be copied
	 */
	void putSamples(int channel, int x, int y, int w, int h, int[] src, int srcOffset);
}
