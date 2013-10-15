/*
 * ShortChannelImage
 *
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

/**
 * An extension of the {@link IntegerImage} interface that restricts the image to 
 * <code>short</code> samples.
 * The minimum sample value for all channels is <code>0</code>, 
 * the maximum sample value <code>65535</code> (2<sup>16</sup> - 1).
 * <p>
 * Number of channels and resolution must be given to the constructor
 * and cannot be changed after creation.
 * <p>
 * Each channel of the image is made up of <code>short</code> values.
 * Note that shorts in Java are signed, they can take values from <code>-32768</code> to <code>32767</code>.
 * If you use {@link IntegerImage}'s getSample and putSample methods
 * you don't have to deal with this, you always get <code>int</code> samples
 * that are in the 0 .. 65535 interval.
 * <p>
 * To manually convert a Java <code>short</code> value to an <code>int</code> value 
 * in the range of 0 to 65535, do the following:
 * <pre>
 * short s = ...; // initialize short value
 * int i = s & 0xffff; 
 * // i now is a value between 0 and 65535
 * </pre>
 * @author Marco Schmidt
 * @since 0.11.0
 */
public interface ShortChannelImage extends IntegerImage
{
	/**
	 * Sets all samples of the first channel to the argument short value.
	 * Equal to <code>clear(0, newValue);</code>.
	 * @param newValue all samples in the first channel are set to this value
	 * @see #clear(int, short)
	 * @see #clear(int)
	 * @see #clear(int, int)
	 */
	void clear(short newValue);

	/**
	 * Sets all samples of one channel to a new value.
	 * @param channelIndex zero-based index of the channel to be cleared (must be smaller than {@link #getNumChannels()}
	 * @param newValue all samples in the channel will be set to this value
	 */
	void clear(int channelIndex, short newValue);

	/**
	 * Returns a single short sample from the first channel and the specified position.
	 * A call to this method is the same as <code>getShortSample(0, x, y)</code>.
	 * @param x horizontal position of the sample to be returned (must be between <code>0</code> and {@link #getWidth()}<code> - 1</code>
	 * @param y vertical position of the sample to be returned (must be between <code>0</code> and {@link #getHeight()}<code> - 1</code>
	 * @return the requested short sample
	 */
	short getShortSample(int x, int y);

	/**
	 * Returns a single short sample from the image.
	 * When possible, try copying several samples at a time for higher speed ({@link #getShortSamples}).
	 * @param channel the number of the channel of the sample; must be from <code>0</code> to <code>{@link #getNumChannels()} - 1</code>
	 * @param x the column of the sample to be returned; must be from <code>0</code> to <code>{@link #getWidth()} - 1</code>
	 * @param y the row of the sample; must be from <code>0</code> to <code>{@link #getHeight()} - 1</code>
	 * @return the sample, a single short value
	 * @throws IllegalArgumentException if the arguments hurt one of the preconditions above
	 * @see #getShortSamples
	 */
	short getShortSample(int channel, int x, int y);

	/**
	 * Copies samples from this image to a short array.
	 * Copies <code>num</code> samples in row <code>y</code> of channel
	 * <code>channel</code>, starting at horizontal offset <code>x</code>.
	 * Data will be written to the <code>dest</code> array, starting at
	 * offset <code>destOffset</code>.
	 * Data will be copied from one row only, so a maximum of 
	 * <code>getWidth()</code>
	 * samples can be copied with a call to this method.
	 * 
	 * @param channelIndex the index of the channel to be copied from; must be 
	 *  from <code>0</code> to <code>getNumChannels() - 1</code>
	 * @param x the horizontal offset where copying will start; must be from
	 *  <code>0</code> to <code>getWidth() - 1</code>
	 * @param y the row from which will be copied; must be from 
	 *  <code>0</code> to <code>getHeight() - 1</code>
	 * @param w number of columns to be copied
	 * @param h number of rows to be copied
	 * @param dest the array where the data will be copied to; must have a 
	 *  length of at least <code>destOffset + num</code>
	 * @param destOffset the offset into <code>dest</code> where this method
	 *  will start copying data
	 * @throws IllegalArgumentException if the arguments hurt one of the many
	 *  preconditions above
	 */
	void getShortSamples(int channelIndex, int x, int y, int w, int h, short[] dest, int destOffset);

	/**
	 * Sets one short sample in one channel to a new value.
	 */
	void putShortSample(int channel, int x, int y, short newValue);

	/**
	 * Sets one short sample in the first channel (index <code>0</code>) to a new value.
	 * Result is equal to <code>putShortSample(0, x, y, newValue);</code>.
	 */
	void putShortSample(int x, int y, short newValue);

	/**
	 * Copies a number of samples from the argument array to this image.
	 */
	void putShortSamples(int channel, int x, int y, int w, int h, short[] src, int srcOffset);
}
