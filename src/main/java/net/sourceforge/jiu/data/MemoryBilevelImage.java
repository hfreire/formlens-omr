/*
 * MemoryBilevelImage
 *
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.util.ArrayConverter;

/**
 * An implementation of the {@link BilevelImage} interface that stores image
 * data in a <code>byte</code> array in memory.
 * An image of <code>width</code> times <code>height</code> pixels will require
 * <code>(width + 7) / 8 * height</code> bytes of memory.
 * @author Marco Schmidt
 */
public class MemoryBilevelImage implements BilevelImage
{
	private final int BYTES_PER_ROW;
	private final byte[] data;
	private final int HEIGHT;
	private final int WIDTH;

	/**
	 * Create a new MemoryBilevelImage object with the specified resolution.
	 * @param width the horizontal resolution of the new image, must be larger than zero
	 * @param height the vertical resolution of the new image, must be larger than zero
	 * @throws IllegalArgumentException if any of the two parameters is smaller than one
	 */
	public MemoryBilevelImage(int width, int height)
	{
		if (width < 1)
		{
			throw new IllegalArgumentException("Width must be larger than zero; got " + width);
		}
		if (height < 1)
		{
			throw new IllegalArgumentException("Height must be larger than zero; got " + height);
		}
		BYTES_PER_ROW = (width + 7) / 8;
		WIDTH = width;
		HEIGHT = height;
		data = new byte[BYTES_PER_ROW * HEIGHT];
	}

	private void checkBitOffset(int bitOffset)
	{
		if (bitOffset < 0 || bitOffset > 7)
		{
			throw new IllegalArgumentException("A bit offset value must be from the interval 0..7.");
		}
	}

	private void checkPositionAndNumber(int x, int y, int w, int h)
	{
		if (w < 0)
		{
			throw new IllegalArgumentException("Negative number of samples " +
				"to be copied: " + w);
		}
		if (h < 0)
		{
			throw new IllegalArgumentException("Negative number of rows " +
				"to be copied: " + h);
		}
		if (x < 0 || x >= getWidth())
		{
			throw new IllegalArgumentException("The value for x is invalid: " + x + ".");
		}
		if (y < 0 || y >= getHeight())
		{
			throw new IllegalArgumentException("The value for y is invalid: " + y + ".");
		}
		if (x + w > getWidth())
		{
			throw new IllegalArgumentException("Cannot copy " + w + " values starting at " +
				"offset " + x + " (width is only " + getWidth() + ").");
		}
		if (y + h > getHeight())
		{
			throw new IllegalArgumentException("Cannot copy " + h + " rows starting at " +
				y + " (height is only " + getHeight() + ").");
		}
	}

	private void checkValue(int value)
	{
		if (value != WHITE && value != BLACK)
		{
			throw new IllegalArgumentException("Sample value must be either BilevelImage.BLACK or BilevelImage.WHITE.");
		}
	}

	public void clear(int newValue)
	{
		clear(0, newValue);
	}

	public void clear(int channelIndex, int newValue)
	{
		if (channelIndex != 0)
		{
			throw new IllegalArgumentException("Invalid channel index; " +
				"bilevel images have only one channel, so 0 is the only " +
				"valid argument; got " + channelIndex);
		}
		checkValue(newValue);
		byte value;
		if (newValue == BLACK)
		{
			value = 0;
		}
		else
		{
			value = (byte)0xff;
		}
		for (int i = 0; i < data.length; i++)
		{
			data[i] = value;
		}
	}

	public PixelImage createCompatibleImage(int width, int height)
	{
		return new MemoryBilevelImage(width, height);
	}

	public PixelImage createCopy()
	{
		PixelImage copy = createCompatibleImage(getWidth(), getHeight());
		MemoryBilevelImage result = (MemoryBilevelImage)copy;
		System.arraycopy(data, 0, result.data, 0, data.length);
		return result;
	}

	public long getAllocatedMemory()
	{
		return data.length;
	}

	public int getBitsPerPixel()
	{
		return 1;
	}

	public int getHeight()
	{
		return HEIGHT;
	}

	public Class getImageType()
	{
		return BilevelImage.class;
	}

	public int getMaxSample(int channelIndex)
	{
		return 1;
	}

	public int getNumChannels()
	{
		return 1;
	}

	public void getPackedBytes(int x, int y, int numSamples, byte[] dest, int destOffset, int destBitOffset)
	{
		checkPositionAndNumber(x, y, numSamples, 1);
		checkBitOffset(destBitOffset);
		int srcOffset = y * BYTES_PER_ROW + (x >> 3);
		int srcBitOffset = x & 7;
		ArrayConverter.copyPackedBytes(data, srcOffset, srcBitOffset, dest, destOffset, destBitOffset, numSamples);
	}

	public int getSample(int x, int y)
	{
		if (isBlack(x, y))
		{
			return BLACK;
		}
		else
		{
			return WHITE;
		}
	}

	public int getSample(int channelIndex, int x, int y)
	{
		if (channelIndex == 0)
		{
			if (isBlack(x, y))
			{
				return BLACK;
			}
			else
			{
				return WHITE;
			}
		}
		else
		{
			throw new IllegalArgumentException("The channelIndex argument must be 0 for bilevel images; got " + channelIndex);
		}
	}

	public void getSamples(int channelIndex, int x, int y, int w, int h, int[] dest, int destOffset)
	{
		final int INITIAL_MASK = 1 << (7 - (x % 8));
		int offset = y * BYTES_PER_ROW + x / 8;
		while (h-- > 0)
		{
			int mask = INITIAL_MASK;
			int srcOffset = offset;
			int remainingColumns = w;
			int srcValue = data[srcOffset++] & 0xff;
			while (remainingColumns-- != 0)
			{
				if ((srcValue & mask) == 0)
				{
					dest[destOffset++] = BLACK;
				}
				else
				{
					dest[destOffset++] = WHITE;
				}
				if (mask == 1)
				{
					mask = 128;
					srcValue = data[srcOffset++] & 0xff;
				}
				else
				{
					mask >>= 1;
				}
			}
			offset += BYTES_PER_ROW;
		}
	}

	public int getWidth()
	{
		return WIDTH;
	}

	public boolean isBlack(int x, int y)
	{
		try
		{
			int offset = y * BYTES_PER_ROW + (x >> 3);
			return (data[offset] & (byte)(1 << (7 - (x & 7)))) == 0;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(x, y, 1, 1);
		}
		return true;
	}

	public boolean isWhite(int x, int y)
	{
		try
		{
			int offset = y * BYTES_PER_ROW + (x >> 3);
			return (data[offset] & (byte)(1 << (7 - (x & 7)))) != 0;
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(x, y, 1, 1);
		}
		return true;
	}

	public void putBlack(int x, int y)
	{
		try
		{
			int offset = y * BYTES_PER_ROW + (x >> 3);
			data[offset] &= (byte)(255 - (1 << (7 - (x & 7))));
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(x, y, 1, 1);
		}
	}

	public void putPackedBytes(int x, int y, int numSamples, byte[] src, int srcOffset, int srcBitOffset)
	{
		checkPositionAndNumber(x, y, numSamples, 1);
		checkBitOffset(srcBitOffset);
		int destOffset = y * BYTES_PER_ROW + (x >> 3);
		int destBitOffset = x & 7;
		ArrayConverter.copyPackedBytes(src, srcOffset, srcBitOffset, data, destOffset, destBitOffset, numSamples);
	}

	public void putSample(int x, int y, int newValue)
	{
		putSample(0, x, y, newValue);
	}

	public void putSample(int channelIndex, int x, int y, int newValue)
	{
		checkValue(newValue);
		try
		{
			int offset = y * BYTES_PER_ROW + (x >> 3);
			if (newValue == BLACK)
			{
				data[offset] &= (byte)(255 - (1 << (7 - (x & 7))));
			}
			else
			{
				data[offset] |= (byte)(1 << (7 - (x & 7)));
			}
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(x, y, 1, 1);
		}
	}

	public void putSamples(int channelIndex, int x, int y, int w, int h, int[] src, int srcOffset)
	{
		checkPositionAndNumber(x, y, w, h);
		int INITIAL_ROW_MASK = 1 << (7 - (x & 7));
		int initialDestOffset = y * BYTES_PER_ROW + (x >> 3);
		while (h-- > 0)
		{
			int mask = INITIAL_ROW_MASK;
			int destOffset = initialDestOffset;
			int pixelsLeft = w;
			while (pixelsLeft-- > 0)
			{
				if (src[srcOffset++] == BLACK)
				{
					data[destOffset] &= (byte)(255 - mask);
				}
				else
				{
					data[destOffset] |= (byte)mask;
				}
				if (mask == 1)
				{
					mask = 128;
					destOffset++;
				}
			}
			initialDestOffset += BYTES_PER_ROW;
		}
	}

	public void putWhite(int x, int y)
	{
		try
		{
			int offset = y * BYTES_PER_ROW + (x >> 3);
			data[offset] |= (byte)(1 << (7 - (x & 7)));
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			checkPositionAndNumber(x, y, 1, 1);
		}
	}
}
