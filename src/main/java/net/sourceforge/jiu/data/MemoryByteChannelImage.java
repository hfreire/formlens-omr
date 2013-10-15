/*
 * MemoryByteChannelImage
 *
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

/**
 * An implementation of {@link ByteChannelImage} that stores image channels as
 * <code>byte[]</code> arrays in memory.
 * An image can have an arbitrary number of channels.
 * <p>
 * This class is abstract because it is merely a data container.
 * It takes a subclass like {@link MemoryGray8Image} to give meaning to the values.
 *
 * @author Marco Schmidt
 */
public abstract class MemoryByteChannelImage implements ByteChannelImage
{
	private final byte[][] data;
	private final byte[] firstChannel; // == data[0]
	private final int numChannels; // == data.length
	private final int width;
	private final int height;
	private final int numPixels; // == width * height

	/**
	 * Create an image of byte channels.
	 * Image data will be completely in memory, so memory requirements are 
	 * <code>width * height * numChannels</code> bytes.
	 * Note that the data will not be initialized, so you should not assume
	 * anything about its content.
	 *
	 * @param numChannels the number of channels in this image, must be
	 *  non-zero and positive
	 * @param width the horizontal resolution, must be non-zero and positive
	 * @param height the vertical resolution, must be non-zero and positive
	 * @throws IllegalArgumentException if any of the parameters are invalid
	 *  or if width times height exceeds two GB
	 * @throws OutOfMemoryException if there is not enough free memory for
	 *  the specified resolution
	 */
	public MemoryByteChannelImage(int numChannels, int width, int height)
	{
		if (width < 1)
		{
			throw new IllegalArgumentException("Width must be larger than " +
				"0: " + width);
		}
		if (height < 1)
		{
			throw new IllegalArgumentException("Height must be larger than" +
				" 0: " + height);
		}
		if (numChannels < 1)
		{
			throw new IllegalArgumentException("Number of channels must be " +
				"larger than 0: " + numChannels);
		}
		this.width = width;
		this.height = height;
		this.numChannels = numChannels;
		numPixels = width * height;
		data = new byte[numChannels][];
		for (int i = 0; i < numChannels; i++)
		{
			data[i] = new byte[numPixels];
		}
		firstChannel = data[0];
	}

	/**
	 * Throws an exception if the arguments do not form a valid horizontal 
	 * sequence of samples.
	 * To be valid, all of the following requirements must be met:
	 */
	protected void checkPositionAndNumber(int channel, int x, int y, int w, int h)
	{
		if (channel < 0 || channel >= numChannels)
		{
			throw new IllegalArgumentException("Illegal channel index value: " + channel +
				". Must be from 0 to " + (numChannels - 1) + ".");
		}
		if (x < 0 || x >= getWidth())
		{
			throw new IllegalArgumentException("The value for x is invalid: " + x + ".");
		}
		if (w < 1)
		{
			throw new IllegalArgumentException("The value for w is invalid: " + w + ".");
		}
		if (x + w > getWidth())
		{
			throw new IllegalArgumentException("The values x + w exceed the " +
				"width of this image; x=" + x + ", w=" + w + ", width=" + 
				getWidth());
		}
		if (h < 1)
		{
			throw new IllegalArgumentException("The value for h is invalid: " + h + ".");
		}
		if (y < 0 || y >= getHeight())
		{
			throw new IllegalArgumentException("The value for y is invalid: " + y + ".");
		}
		if (y + h > getHeight())
		{
			throw new IllegalArgumentException("The values y + h exceed the " +
				"height of this image; y=" + y + ", h=" + h + ", height=" + 
				getHeight());
		}
	}

	public void clear(byte newValue)
	{
		clear(0, newValue);
	}

	public void clear(int channelIndex, byte newValue)
	{
		// check the validity of the channel index
		checkPositionAndNumber(channelIndex, 0, 0, 1, 1);
		// get the correct channel as byte[]
		final byte[] CHANNEL = data[channelIndex];
		// fill channel with the argument value
		final byte VALUE = (byte)newValue;
		final int LENGTH = CHANNEL.length;
		for (int i = 0; i < LENGTH; i++)
		{
			CHANNEL[i] = VALUE;
		}
	}

	public void clear(int newValue)
	{
		clear(0, (byte)newValue);
	}

	public void clear(int channelIndex, int newValue)
	{
		clear(channelIndex, (byte)newValue);
	}

	public abstract PixelImage createCompatibleImage(int width, int height);

	public PixelImage createCopy()
	{
		PixelImage copy = createCompatibleImage(getWidth(), getHeight());
		MemoryByteChannelImage result = (MemoryByteChannelImage)copy;
		for (int channelIndex = 0; channelIndex < getNumChannels(); channelIndex++)
		{
			System.arraycopy(data[channelIndex], 0, result.data[channelIndex], 0, data[channelIndex].length);
		}
		return result;
	}

	public long getAllocatedMemory()
	{
		long result = 0;
		if (data != null)
		{
			int channelIndex = 0;
			while (channelIndex < data.length)
			{
				byte[] array = data[channelIndex++];
				if (array != null)
				{
					result += array.length;
				}
			}
		}
		return result;
	}

	public int getBitsPerPixel()
	{
		return numChannels * 8;
	}

	public byte getByteSample(int channel, int x, int y)
	{
		/* advantage of the following approach: we don't check arguments 
		   before we access the data (too costly); instead, we have the VM 
		   throw an array index out of bounds exception and then determine 
		   which of the arguments was wrong;
		   that's better than checking before access all of the time => 
		   the VM checks anyway
		   we then throw a meaningful IllegalArgumentException (in 
		   checkPositionAndNumber)
		   disadvantage: some erroneous arguments aren't noticed, example:
		   width=100, height=20, x=100, y=0
		   will not result in an error (because only 0..99 are valid for x) 
		   but in the return of sample(0/1)
		   
		    */
		try
		{
			return data[channel][y * width + x];
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(channel, x, y, 1, 1);
			return -1;
		}
	}

	public byte getByteSample(int x, int y)
	{
		return getByteSample(0, x, y);
	}

	public void getByteSamples(int channel, int x, int y, int w, int h, byte[] dest, int destOffset)
	{
		checkPositionAndNumber(channel, x, y, w, h);
		byte[] src = data[channel];
		try
		{
			int srcOffset = y * width + x;
			while (h-- > 0)
			{
				java.lang.System.arraycopy(src, srcOffset, dest, destOffset, w);
				srcOffset += width;
				destOffset += w;
			}
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
		}
	}

	public final int getHeight()
	{
		return height;
	}

	public int getMaxSample(int channel)
	{
		return 255;
	}

	public int getNumChannels()
	{
		return numChannels;
	}

	public final int getSample(int x, int y)
	{
		try
		{
			return firstChannel[y * width + x] & 0xff;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(0, x, y, 1, 1);
			return -1;
		}
	}

	public final int getSample(int channel, int x, int y)
	{
		try
		{
			return data[channel][y * width + x] & 0xff;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(channel, x, y, 1, 1);
			return -1;
		}
	}

	public void getSamples(int channel, int x, int y, int w, int h, int[] dest, int destOffs)
	{
		if (w < 1 || h < 1)
		{
			return;
		}
		byte[] src = data[channel];
		int srcOffs = y * width + x;
		while (h-- != 0)
		{
			int loop = w;
			int from = srcOffs;
			while (loop-- != 0)
			{
				dest[destOffs++] = src[from++] & 0xff;
			}
			srcOffs += width;
		}
	}

	public final int getWidth()
	{
		return width;
	}

	public final void putByteSample(int channel, int x, int y, byte newValue)
	{
		checkPositionAndNumber(channel, x, y, 1, 1);
		try
		{
			data[channel][y * width + x] = newValue;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(channel, x, y, 1, 1);
		}
	}

	public final void putByteSample(int x, int y, byte newValue)
	{
		checkPositionAndNumber(0, x, y, 1, 1);
		try
		{
			firstChannel[y * width + x] = newValue;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(0, x, y, 1, 1);
		}
	}

	public void putByteSamples(int channel, int x, int y, int w, int h, byte[] src, int srcOffset)
	{
		checkPositionAndNumber(channel, x, y, w, h);
		byte[] dest = data[channel];
		int destOffset = y * width + x;
		while (h-- > 0)
		{
			java.lang.System.arraycopy(src, srcOffset, dest, destOffset, w);
			srcOffset += w;
			destOffset += width;
		}
	}

	public void putSamples(int channel, int x, int y, int w, int h, int[] src, int srcOffs)
	{
		checkPositionAndNumber(channel, x, y, w, h);
		byte[] dest = data[channel];
		int destOffs = y * width + x;
		while (h-- != 0)
		{
			int loop = w;
			int to = destOffs;
			while (loop-- != 0)
			{
				dest[to++] = (byte)src[srcOffs++];
			}
			destOffs += width;
		}
	}

	public final void putSample(int x, int y, int newValue)
	{
		putByteSample(0, x, y, (byte)newValue);
	}

	public final void putSample(int channel, int x, int y, int newValue)
	{
		putByteSample(channel, x, y, (byte)newValue);
	}
}
