/*
 * ArrayConverter
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

/**
 * Helper class with static methods to convert between byte arrays and primitive types.
 * Useful for serialization.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class ArrayConverter
{
	private static final int SHORT_SIZE = 2;
	private static final int INT_SIZE = 4;

	private ArrayConverter()
	{
	}

	/**
	 * Makes sure that the arguments define a valid (existing) array interval.
	 * This includes:
	 * <ul>
	 * <li>array is non-null</li>
	 * <li>offset is &gt;= 0 and smaller than array.length</li>
	 * <li>length is &gt; 0</li>
	 * <li>offset + length is &lt;= array.length</li>
	 * </ul>
	 */
	private static void checkArray(byte[] array, int offset, int length) throws IllegalArgumentException
	{
		if (array == null)
		{
			throw new IllegalArgumentException("Array must not be null.");
		}
		if (offset < 0)
		{
			throw new IllegalArgumentException("Array index must not be negative.");
		}
		if (length < 1)
		{
			throw new IllegalArgumentException("Length of value must not be smaller than one.");
		}
		if (offset >= array.length)
		{
			throw new IllegalArgumentException("Offset " + offset + 
				" is invalid, must be smaller than array length " + 
				array.length + ".");
		}
		if (offset + length > array.length)
		{
			throw new IllegalArgumentException("Value of length " + 
				length + " does not fit at offset " + offset +
				" into an array of length " + array.length + ".");
		}
	}

	/*public static void convertPacked2BitIntensityTo8BitA(byte[] src, int srcOffset, byte[] dest, int destOffset, int numPackedBytes)
	{
		final byte[] DEST_VALUES = {0, 85, (byte)170, (byte)255};
		while (numPackedBytes-- > 0)
		{
			int srcValue = src[srcOffset++] & 0xff;
			dest[destOffset++] = DEST_VALUES[(srcValue >> 6) & 3];
			dest[destOffset++] = DEST_VALUES[(srcValue >> 4) & 3];
			dest[destOffset++] = DEST_VALUES[(srcValue >> 2) & 3];
			dest[destOffset++] = DEST_VALUES[srcValue & 3];
		}
	}*/

	/**
	 * Converts bytes with two four-bit-intensity samples to 8 byte intensity 
	 * values, each stored in one byte.
	 * Two-bit values can be 0, 1, 2 or 3.
	 * These values will be scaled to the full [0;255] range so that 0 remains 0,
	 * 1 becomes 85, 2 becomes 170 and 3 becomes 255.
	 * <p>
	 * A little discussion on how to implement this method
	 * was held in the German Java newsgroup
	 * <a href="news:de.comp.lang.java">de.comp.lang.java</a>.
	 * The message I wrote to start the thread has the ID 
	 * <code>1ef7du4vfqsd2pskb6jukut6pnhn87htt2@4ax.com</code>.
	 * Read the 
	 * <a target="_top" href="http://groups.google.com/groups?as_umsgid=1ef7du4vfqsd2pskb6jukut6pnhn87htt2@4ax.com">thread 
	 * at Google Groups</a>.
	 * @param src byte array, each byte stores four two-bit intensity values
	 * @param srcOffset index into src
	 * @param dest byte array, each byte stores an eight-bit intensity values
	 * @param destOffset index into dest
	 * @param numPackedBytes number of bytes in src to be decoded	 
	 */
	public static void convertPacked2BitIntensityTo8Bit(byte[] src, int srcOffset, byte[] dest, int destOffset, int numPackedBytes)
	{
		while (numPackedBytes-- > 0)
		{
			int srcValue = src[srcOffset++] & 0xff;
			dest[destOffset++] = (byte)(((srcValue >> 6) & 3) * 85);
			dest[destOffset++] = (byte)(((srcValue >> 4) & 3) * 85);
			dest[destOffset++] = (byte)(((srcValue >> 2) & 3) * 85);
			dest[destOffset++] = (byte)((srcValue & 3) * 85);
		}
	}

	/**
	 * Converts bytes with four two-bit-intensity samples to byte-sized intensity values.
	 * Four-bit values can be from 0 to 15.
	 * These values will be scaled to the full [0;255] range so that 0 remains 0,
	 * 1 becomes 17, 2 becomes 34, ..., and 15 becomes 255.
	 * The most significant four bits in a byte become the left, the least significant
	 * four bits the right pixel.
	 * @param src byte array, each byte stores two four-bit intensity values
	 * @param srcOffset index into src
	 * @param dest byte array, each byte stores an eight-bit intensity values
	 * @param destOffset index into dest
	 * @param numPackedBytes number of bytes in src to be decoded
	 * @since 0.12.0
	 */
	public static void convertPacked4BitIntensityTo8Bit(byte[] src, int srcOffset, byte[] dest, int destOffset, int numPackedBytes)
	{
		while (numPackedBytes-- > 0)
		{
			int srcValue = src[srcOffset++] & 0xff;
			dest[destOffset++] = (byte)((srcValue & 0xf0) | ((srcValue & 0xf0) >> 4));
			dest[destOffset++] = (byte)((srcValue & 0x0f) | ((srcValue & 0x0f) << 4));
		}
	}

	/**
	 * Copies a number of bit values from one byte array to another.
	 * @param src array from which is copied
	 * @param srcOffset index into the src array of the first byte from which is copied
	 * @param srcBitOffset first bit within src[srcOffset] from which is copied (0 is left-most, 1 is second  left-most, 7 is right-most)
	 * @param dest array to which is copied
	 * @param destOffset index into the dest array of the first byte to which is copied
	 * @param destBitOffset first bit within dest[destOffset] to which is copied (0 is left-most, 1 is second  left-most, 7 is right-most)
	 * @param numSamples number of bits to be copied
	 */
	public static void copyPackedBytes(byte[] src, int srcOffset, int srcBitOffset, byte[] dest, int destOffset, int destBitOffset, int numSamples)
	{
		if (numSamples < 0)
		{
			throw new IllegalArgumentException("Number of samples to be copied must be 0 or larger.");
		}
		if (srcBitOffset == 0 && destBitOffset == 0 && numSamples > 7)
		{
			int bytes = numSamples >> 3;
			System.arraycopy(src, srcOffset, dest, destOffset, bytes);
			srcOffset += bytes;
			destOffset += bytes;
			numSamples &= 7;
		}
		int srcMask = 1 << (7 - srcBitOffset);
		int destMask = 1 << (7 - destBitOffset);
		while (numSamples-- != 0)
		{
			if ((src[srcOffset] & srcMask) == 0)
			{
				dest[destOffset] &= (byte)(255 - destMask);
			}
			else
			{
				dest[destOffset] |= (byte)destMask;
			}
			if (srcMask == 1)
			{
				srcMask = 128;
				srcOffset++;
			}
			else
			{
				srcMask >>= 1;
			}
			if (destMask == 1)
			{
				destMask = 128;
				destOffset++;
			}
			else
			{
				destMask >>= 1;
			}
		}
	}
	
	public static void decodePacked1Bit(byte[] src, int srcOffset, byte[] dest, int destOffset, int numPackedBytes)
	{
		while (numPackedBytes-- != 0)
		{
			int srcValue = src[srcOffset++] & 0xff;
			dest[destOffset++] = (byte)((srcValue >> 7) & 0x01);
			dest[destOffset++] = (byte)((srcValue >> 6) & 0x01);
			dest[destOffset++] = (byte)((srcValue >> 5) & 0x01);
			dest[destOffset++] = (byte)((srcValue >> 4) & 0x01);
			dest[destOffset++] = (byte)((srcValue >> 3) & 0x01);
			dest[destOffset++] = (byte)((srcValue >> 2) & 0x01);
			dest[destOffset++] = (byte)((srcValue >> 1) & 0x01);
			dest[destOffset++] = (byte)(srcValue & 0x01);
		}
	}

	/**
	 * Decodes bytes with four two-bit samples to single bytes.
	 * The two most significant bits of a source byte become the first value,
	 * the two least significant bits the fourth value.
	 * The method expects <code>numPackedBytes</code> bytes at <code>src[srcOffset]</code>
	 * (these will be read and interpreted) and
	 * <code>numPackedBytes * 4</code> at <code>dest[destOffset]</code> (where the decoded
	 * byte values will be stored.
	 * <p>
	 * @param src byte array, each byte stores four two-bit values
	 * @param srcOffset index into src
	 * @param dest byte array, each byte stores a single decoded value (from 0 to 3)
	 * @param destOffset index into dest
	 * @param numPackedBytes number of bytes in src to be decoded
	 * @since 0.10.0
	 */
	public static void decodePacked2Bit(byte[] src, int srcOffset, byte[] dest, int destOffset, int numPackedBytes)
	{
		while (numPackedBytes-- != 0)
		{
			int srcValue = src[srcOffset++] & 0xff;
			dest[destOffset++] = (byte)(srcValue >> 6);
			dest[destOffset++] = (byte)((srcValue >> 4) & 0x03);
			dest[destOffset++] = (byte)((srcValue >> 2) & 0x03);
			dest[destOffset++] = (byte)(srcValue & 0x03);
		}
	}

	/**
	 * Decodes bytes with two four-bit samples to single bytes.
	 * The four most significant bits of a source byte become the first value,
	 * the least significant four bits the second value.
	 * The method expects <code>numPackedBytes</code> bytes at <code>src[srcOffset]</code>
	 * (these will be read and interpreted) and
	 * <code>numPackedBytes * 2</code> at <code>dest[destOffset]</code> (where the decoded
	 * byte values will be stored.
	 * <p>
	 * @param src byte array, each byte stores two four-bit values
	 * @param srcOffset index into src
	 * @param dest byte array, each byte stores a single decoded value
	 * @param destOffset index into dest
	 * @param numPackedBytes number of bytes in src to be decoded
	 */
	public static void decodePacked4Bit(byte[] src, int srcOffset, byte[] dest, int destOffset, int numPackedBytes)
	{
		while (numPackedBytes-- > 0)
		{
			int srcValue = src[srcOffset++] & 0xff;
			dest[destOffset++] = (byte)(srcValue >> 4);
			dest[destOffset++] = (byte)(srcValue & 0x0f);
		}
	}

	/**
	 * Convert 16 bit RGB samples stored in big endian (BE) byte order
	 * with 5 bits for red and blue and 6 bits for green to 24
	 * bit RGB byte samples.
	 * @since 0.10.0
	 */
	public static void decodePackedRGB565BigEndianToRGB24(byte[] src, int srcOffset, 
		byte[] red, int redOffset,
		byte[] green, int greenOffset,
		byte[] blue, int blueOffset,
		int numPixels)
	{
		while (numPixels-- != 0)
		{
			int pixel = ((src[srcOffset] & 0xff) << 8) | (src[srcOffset + 1] & 0xff);
			srcOffset += 2;
			int r = (pixel >> 11) & 0x1f;
			int g = (pixel >> 5) & 0x3f;
			int b = pixel & 0x1f;
			red[redOffset++] = (byte)((r << 3) | ((r >> 2) & 0x07));
			green[greenOffset++] = (byte)((g << 2) | ((g >> 4) & 0x03));
			blue[blueOffset++] = (byte)((b << 3) | ((b >>2) & 0x07));
		}
	}

	public static void encodePacked2Bit(byte[] src, int srcOffset, byte[] dest, int destOffset, int numSamples)
	{
		int numBytes = numSamples / 4;
		while (numBytes-- != 0)
		{
			int b1 = src[srcOffset++] & 3;
			int b2 = src[srcOffset++] & 3;
			int b3 = src[srcOffset++] & 3;
			int b4 = src[srcOffset++] & 3;
			dest[destOffset++] = (byte)(b1 << 6 | b2 << 4 | b3 << 2 | b4);
		}
		numSamples = numSamples % 4;
		if (numSamples > 0)
		{
			int value = 0;
			int mask = 6;
			while (numSamples-- != 0)
			{
				value |= ((src[srcOffset++] & 3) << mask);
				mask -= 2;
			}
			dest[destOffset] = (byte)value;
		}
	}

	public static void encodePacked4Bit(byte[] src, int srcOffset, byte[] dest, int destOffset, int numSamples)
	{
		int numBytes = numSamples / 2;
		while (numBytes-- != 0)
		{
			int b1 = src[srcOffset++] & 15;
			int b2 = src[srcOffset++] & 15;
			dest[destOffset++] = (byte)(b1 << 4 | b2);
		}
		if ((numSamples % 2) == 1)
		{
			dest[destOffset] = (byte)((src[srcOffset] & 15) << 4);
		}
	}

	/**
	 * Convert 24 bit RGB pixels to 16 bit pixels stored in big endian (BE) byte order
	 * with 5 bits for red and blue and 6 bits for green.
	 * @since 0.10.0
	 */
	public static void encodeRGB24ToPackedRGB565BigEndian(
		byte[] red, int redOffset,
		byte[] green, int greenOffset,
		byte[] blue, int blueOffset,
		byte[] dest, int destOffset, 
		int numPixels)
	{
		while (numPixels-- != 0)
		{
			int r = (red[redOffset++] & 0xff) >> 3;
			int g = (green[greenOffset++] & 0xff) >> 2;
			int b = (blue[blueOffset++] & 0xff) >> 3;
			int pixel = r << 11 | g << 5 | b;
			dest[destOffset++] = (byte)(pixel >> 8);
			dest[destOffset++] = (byte)(pixel & 0xff);
		}
	}

	/**
	 * Reads four consecutive bytes from the given array at the 
	 * given position in big endian order and returns them as 
	 * an <code>int</code>.
	 * @param src the array from which bytes are read
	 * @param srcOffset the index into the array from which the bytes are read
	 * @return int value taken from the array
	 */
	public static int getIntBE(byte[] src, int srcOffset)
	{
		checkArray(src, srcOffset, INT_SIZE);
		return 
			(src[srcOffset + 3] & 0xff) |
			((src[srcOffset + 2] & 0xff) << 8) |
			((src[srcOffset + 1] & 0xff) << 16) |
			((src[srcOffset] & 0xff) << 24);
	}

	/**
	 * Reads four consecutive bytes from the given array at the 
	 * given position in little endian order and returns them as 
	 * an <code>int</code>.
	 * @param src the array from which bytes are read
	 * @param srcOffset the index into the array from which the bytes are read
	 * @return short value taken from the array
	 */
	public static int getIntLE(byte[] src, int srcOffset)
	{
		checkArray(src, srcOffset, INT_SIZE);
		return 
			(src[srcOffset] & 0xff) |
			((src[srcOffset + 1] & 0xff) << 8) |
			((src[srcOffset + 2] & 0xff) << 16) |
			((src[srcOffset + 3] & 0xff) << 24);
	}

	/**
	 * Reads two consecutive bytes from the given array at the 
	 * given position in big endian order and returns them as 
	 * a <code>short</code>.
	 * @param src the array from which two bytes are read
	 * @param srcOffset the index into the array from which the two bytes are read
	 * @return short value taken from the array
	 */
	public static short getShortBE(byte[] src, int srcOffset)
	{
		checkArray(src, srcOffset, SHORT_SIZE);
		return (short)
			(((src[srcOffset++] & 0xff) << 8)  |
			 (src[srcOffset++] & 0xff));
	}

	public static int getShortBEAsInt(byte[] src, int srcOffset)
	{
		checkArray(src, srcOffset, SHORT_SIZE);
		return ((src[srcOffset++] & 0xff) << 8) |
			 (src[srcOffset++] & 0xff);
	}

	/**
	 * Reads two consecutive bytes from the given array at the 
	 * given position in little endian order and returns them as 
	 * a <code>short</code>.
	 * @param src the array from which two bytes are read
	 * @param srcOffset the index into the array from which the two bytes are read
	 * @return short value taken from the array
	 */
	public static short getShortLE(byte[] src, int srcOffset)
	{
		checkArray(src, srcOffset, SHORT_SIZE);
		return (short)
			((src[srcOffset++] & 0xff)   |
			 ((src[srcOffset++] & 0xff) << 8));
	}

	/**
	 * Writes an int value into four consecutive bytes of a byte array,
	 * in big endian (network) byte order.
	 * @param dest the array to which bytes are written
	 * @param destOffset index of the array to which the first byte is written
	 * @param newValue the int value to be written to the array
	 */
	public static void setIntBE(byte[] dest, int destOffset, int newValue)
	{
		checkArray(dest, destOffset, INT_SIZE);
		dest[destOffset] = (byte)((newValue >> 24)& 0xff);
		dest[destOffset + 1] = (byte)((newValue >> 16)& 0xff);
		dest[destOffset + 2] = (byte)((newValue >> 8)& 0xff);
		dest[destOffset + 3] = (byte)(newValue & 0xff);
	}

	/**
	 * Writes an int value into four consecutive bytes of a byte array,
	 * in little endian (Intel) byte order.
	 * @param dest the array to which bytes are written
	 * @param destOffset index of the array to which the first byte is written
	 * @param newValue the int value to be written to the array
	 */
	public static void setIntLE(byte[] dest, int destOffset, int newValue)
	{
		checkArray(dest, destOffset, INT_SIZE);
		dest[destOffset] = (byte)(newValue & 0xff);
		dest[destOffset + 1] = (byte)((newValue >> 8)& 0xff);
		dest[destOffset + 2] = (byte)((newValue >> 16)& 0xff);
		dest[destOffset + 3] = (byte)((newValue >> 24)& 0xff);
	}

	public static void setShortBE(byte[] dest, int destOffset, short newValue)
	{
		checkArray(dest, destOffset, SHORT_SIZE);
		dest[destOffset] = (byte)((newValue >> 8) & 0xff);
		dest[destOffset + 1] = (byte)(newValue & 0xff);
	}

	public static void setShortLE(byte[] dest, int destOffset, short newValue)
	{
		checkArray(dest, destOffset, SHORT_SIZE);
		dest[destOffset + 1] = (byte)((newValue >> 8) & 0xff);
		dest[destOffset] = (byte)(newValue & 0xff);
	}

}
