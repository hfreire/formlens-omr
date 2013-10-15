/*
 * RGBA
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

/**
 * This class converts between the 32 bit RGBA int values (used throughout the AWT) and 
 * various standard pixel formats like 24 bits RGB, 8 bits gray, 16 bits gray, 1 bit black and white.
 * <p>
 * The conversion is done in a number of static methods.
 * This class is not supposed to be instantiated.
 * <p>
 * The method names of this class can be interpreted as follows.
 * If they contain
 * <code>fromXYZ</code> (where XYZ is a pixel format type like Gray8, RGB24 etc.), 
 * a conversion from another pixel format to RGBA is done.
 * If the names contains <code>toXYZ</code>, a conversion from RGBA to that pixel
 * format will be performed.
 * <p>
 * Not all conversions are lossless or well-defined.
 * If 48 bpp RGB truecolor is used as source, only the top eight bits of each
 * 16 bit sample will be used (thus, the procedure is lossy).
 * If RGBA data is to be converted to bilevel (black and white), the conversion
 * is undefined if there are input RGBA pixels that are neither black nor white.
 *
 * @author Marco Schmidt
 */
public class RGBA
{
	/**
	 * The default value for the alpha part of RGBA.
	 * The alpha value is eight bits long left-shifted by 24.
	 * This default value is <em>no transparency</em> - the underlying image
	 * cannot be seen: <code>0xff000000</code>.
	 */
	public static final int DEFAULT_ALPHA = 0xff000000;

	private RGBA()
	{
	}

	/**
	 * Converts pixels from bilevel packed bytes to RGBA format.
	 * A byte is supposed to store eight pixels, the most significant bit being the leftmost pixel.
	 * @param src the array with the packed bytes
	 * @param srcOffset the index of the first byte to be converted from src
	 * @param alpha the alpha value to be used for the destination RGBA values
	 * @param dest the array where the destination RGBA pixels will be stored
	 * @param destOffset the index of the first destination pixel in the dest array; 
	 *  that array must be at least destOffset + ((num + 7) / 8) large
	 * @param num the number of pixels (not bytes) to be converted
	 */
	public static void convertFromPackedBilevel(byte[] src, int srcOffset, int alpha,
		int[] dest, int destOffset, int num)
	{
		final int BLACK = alpha;
		final int WHITE = alpha | 0x00ffffff;
		int mask = 1;
		int value = 0; // 0 will never be used; value will be assigned a value in the first pass of the loop
		while (num-- > 0)
		{
			if (mask == 1)
			{	
				mask = 128;
				value = src[srcOffset++] & 0xff;
			}
			else
			{
				mask >>= 1;
			}
			if ((value & mask) == 0)
			{
				dest[destOffset++] = BLACK;
			}
			else
			{
				dest[destOffset++] = WHITE;
			}
		}
	}

	/**
	 * Convert a number of 8 bit grayscale pixels, shades of gray between 0 (for black)
	 * and 255 (for white), given as bytes, to RGBA type int pixels, adding the given
	 * alpha value.
	 * @param src array with grayscale pixels
	 * @param srcOffset index of first entry of src to be converted
	 * @param alpha transparency value to be used in resulting RGBA array (only top eight bits can be set)
	 * @param dest array to store resulting RGBA pixels
	 * @param destOffset index of first entry in dest to be used
	 * @param num number of pixels to be converted
	 */
	public static void convertFromGray8(byte[] src, int srcOffset, int alpha,
		int[] dest, int destOffset, int num)
	{
		while (num-- > 0)
		{
			int grayValue = src[srcOffset++] & 0xff;
			dest[destOffset++] = alpha | grayValue | (grayValue << 8) | (grayValue << 16);
		}
	}

	/**
	 * Convert a number of 16 bit grayscale pixels to RGBA type int pixels, adding the given
	 * alpha value.
	 * Note that the lower 8 bits of each grayscale value are dropped.
	 * @param src array with grayscale pixels
	 * @param srcOffset index of first entry of src to be converted
	 * @param alpha transparency value to be used in resulting RGBA array (only top eight bits can be set)
	 * @param dest array to store resulting RGBA pixels
	 * @param destOffset index of first entry in dest to be used
	 * @param num number of pixels to be converted
	 */
	public static void convertFromGray16(short[] src, int srcOffset, int alpha,
		int[] dest, int destOffset, int num)
	{
		while (num-- > 0)
		{
			int grayValue = (src[srcOffset++] & 0xffff) >> 8;
			dest[destOffset++] = alpha | grayValue | (grayValue << 8) | (grayValue << 16);
		}
	}

	/**
	 * Converts a byte array of palette index values to an array of RGBA values,
	 * using palette color data.
	 * @param src the byte array with the palette index values
	 * @param srcOffset index of the first entry of src to be used
	 * @param alpha transparency value to be used (only top eight bits should be set)
	 * @param red the red palette values
	 * @param green the green palette values
	 * @param blue the blue palette values
	 * @param dest the destination array to store the RGBA values
	 * @param destOffset the first entry of dest to be used
	 * @param num the number of pixels to be converted
	 */
	public static void convertFromPaletted8(byte[] src, int srcOffset, int alpha, 
		int[] red, int[] green, int[] blue, int[] dest, int destOffset, int num)
	{
		while (num-- > 0)
		{
			int index = src[srcOffset++] & 0xff;
			dest[destOffset++] = alpha | (blue[index]) | (green[index] << 8) | (red[index] << 16);
		}
	}

	/**
	 * Converts 24 bit RGB truecolor data to RGBA int values.
	 * @param srcRed the red pixel values
	 * @param srcRedOffset the first entry of srcRed to be used
	 * @param srcGreen the green pixel values
	 * @param srcGreenOffset the first entry of srcGreen to be used
	 * @param srcBlue the blue pixel values
	 * @param srcBlueOffset the first entry of srcBlue to be used
	 * @param alpha the transpancy value to be used in the destination RGBA array (only top 8 bits should be set)
	 * @param dest array to store RGBA pixel values
	 * @param destOffset first entry of dest to be used
	 * @param num number of pixels to be converted
	 */
	public static void convertFromRGB24(byte[] srcRed, int srcRedOffset, byte[] srcGreen, 
		int srcGreenOffset, byte[] srcBlue, int srcBlueOffset, int alpha, 
		int[] dest, int destOffset, int num)
	{
		while (num-- > 0)
		{
			dest[destOffset++] =
				alpha |
				(srcBlue[srcBlueOffset++] & 0xff) |
				((srcGreen[srcGreenOffset++] & 0xff) << 8) |
				((srcRed[srcRedOffset++] & 0xff) << 16);
		}
	}

	/**
	 * Converts 48 bit RGB truecolor data to RGBA int values, dropping the least
	 * significant eight bits of each short sample.
	 * @param srcRed the red pixel values
	 * @param srcRedOffset the first entry of srcRed to be used
	 * @param srcGreen the green pixel values
	 * @param srcGreenOffset the first entry of srcGreen to be used
	 * @param srcBlue the blue pixel values
	 * @param srcBlueOffset the first entry of srcBlue to be used
	 * @param alpha the transpancy value to be used in the destination RGBA array (only top 8 bits should be set)
	 * @param dest array to store RGBA pixel values
	 * @param destOffset first entry of dest to be used
	 * @param num number of pixels to be converted
	 * @since 0.12.0
	 */
	public static void convertFromRGB48(short[] srcRed, int srcRedOffset, short[] srcGreen, 
		int srcGreenOffset, short[] srcBlue, int srcBlueOffset, int alpha, 
		int[] dest, int destOffset, int num)
	{
		while (num-- > 0)
		{
			dest[destOffset++] =
				alpha |
				((srcBlue[srcBlueOffset++] & 0xff00) >> 8) |
				((srcGreen[srcGreenOffset++] & 0xff00)) |
				((srcRed[srcRedOffset++] & 0xff00) << 8);
		}
	}
}
