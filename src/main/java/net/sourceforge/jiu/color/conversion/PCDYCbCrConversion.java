/*
 * PCDYCbCrConversion
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.conversion;

import net.sourceforge.jiu.color.YCbCrIndex;
import net.sourceforge.jiu.data.RGBIndex;

/**
 * Convert from YCbCr color space (as used in Kodak PCD files) to
 * RGB. Only works for 24 bits per pixel (8 bits per channel) image
 * data.
 *
 * @author Marco Schmidt
 */
public class PCDYCbCrConversion implements 
	RGBIndex,
	YCbCrIndex
{
	// color conversion coefficients (YCbCr to RGB)
	private static final float c11 =  0.0054980f * 256;
	private static final float c12 =  0.0000000f * 256;
	private static final float c13 =  0.0051681f * 256;
	private static final float c21 =  0.0054980f * 256;
	private static final float c22 = -0.0015446f * 256;
	private static final float c23 = -0.0026325f * 256;
	private static final float c31 =  0.0054980f * 256;
	private static final float c32 =  0.0079533f * 256;
	private static final float c33 =  0.0000000f * 256;

	private PCDYCbCrConversion()
	{
	}

	private static byte floatToByte(float f)
	{
		if (f <= 0.0)
		{
			return 0;
		}
		if (f >= 255.0)
		{
			return (byte)255;
		}
		return (byte)((int)f);
	}

	/* 
	 * Converts the color given by (y, cb, cr) to RGB color space.
	 * The three int variables y, cb and cr must be from the
	 * interval 0 to 255 (this is not checked).
	 * The rgb array will get the resulting RGB color, so it must
	 * have at least three entries.
	 * The three entries in that array will also be from 0 to 255 each.
	 *
	public static void convertYCbCrToRgb(int y, int cb, int cr, int[] rgb)
	{
		int cr137 = cr - 137;
		int cb156 = cb - 156;
		rgb[INDEX_RED] = floatToInt(c11 * y + c12 * (cb156) + c13 * (cr137));
		rgb[INDEX_GREEN] = floatToInt(c21 * y + c22 * (cb156) + c23 * (cr137));
		rgb[INDEX_BLUE] = floatToInt(c31 * y + c32 * (cb156) + c33 * (cr137));
	}

	public static int[] convertToRgb(byte[][] data, int width, int height)
		throws IllegalArgumentException
	{
		if (width < 1 || height < 1)
		{
			throw new IllegalArgumentException("Error -- width and height must be larger " +
				"than 0 (width=" + width + ", height=" + height);
		}
		if (data == null)
		{
			throw new IllegalArgumentException("Error -- data array must not be null.");
		}
		if (data.length < 3)
		{
			throw new IllegalArgumentException("Error -- data array must have at least " +
				"three items (has " + data.length);
		}
		int numPixels = width * height;
		int[] result = new int[numPixels];
		int[] rgb = new int[3];
		for (int i = 0; i < numPixels; i++)
		{
			int gray = data[INDEX_Y][i] & 0xff;
			int cb = data[INDEX_CB][i] & 0xff;
			int cr = data[INDEX_CR][i] & 0xff;
			convertYCbCrToRgb(gray, cb, cr, rgb);
			result[i] = 0xff000000 | rgb[0] << 16 | (rgb[1] << 8) | (rgb[2]);
		}
		return result;
	}*/

	private static void checkArray(byte[] data, int offset, int num) throws IllegalArgumentException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("Data array must be initialized.");
		}
		if (offset < 0 || offset + num > data.length)
		{
			throw new IllegalArgumentException("Invalid combination of " +
				"offset, number and array length: offset=" + offset +
				", num=" + num + ", data.length=" + data.length);
		}
	}

	/**
	 * Converts pixels from YCbCr to RGB color space.
	 * Input pixels are given as three byte arrays for luminance and the 
	 * two chroma components.
	 * Same for output pixels, three other arrays for red, green and blue.
	 * Offset values can be specified separately for the YCbCr and the RGB
	 * arrays.
	 * @param y the array of gray source samples
	 * @param cb the array of chroma blue source samples
	 * @param cr the array of chroma red source samples
	 * @param yccOffset offset value into the arrays y, cb and cr; color 
	 *  conversion will be started at the yccOffset'th value of each array
	 * @param r the array of red destination samples
	 * @param g the array of green destination samples
	 * @param b the array of blue destination samples
	 * @param rgbOffset offset value into the arrays r, g and b; destination samples
	 *  will be written to the three arrays starting at the rgbOffset'th value of each array
	 * @param num the number of pixels to be converted
	 * @throws IllegalArgumentException if one of the int values is negative or one
	 *  of the arrays is null or too small
	 */
	public static void convertYccToRgb(
		byte[] y, 
		byte[] cb, 
		byte[] cr, 
		int yccOffset, 
		byte[] r, 
		byte[] g, 
		byte[] b, 
		int rgbOffset, 
		int num)
		throws IllegalArgumentException
	{
		if (num < 0)
		{
			throw new IllegalArgumentException("Negative number of pixels " +
				"to be converted is invalid: " + num);
		}
		checkArray(y, yccOffset, num);
		checkArray(cb, yccOffset, num);
		checkArray(cr, yccOffset, num);
		checkArray(r, rgbOffset, num);
		checkArray(g, rgbOffset, num);					
		checkArray(b, rgbOffset, num);
		while (num-- > 0)
		{
			int gray = y[yccOffset] & 0xff;
			int chromaBlue = cb[yccOffset] & 0xff;
			int chromaRed = cr[yccOffset++] & 0xff;
			int cr137 = chromaRed - 137;
			int cb156 = chromaBlue - 156;
			r[rgbOffset] = floatToByte(c11 * gray + c12 * (cb156) + c13 * (cr137));
			g[rgbOffset] = floatToByte(c21 * gray + c22 * (cb156) + c23 * (cr137));
			b[rgbOffset++] = floatToByte(c31 * gray + c32 * (cb156) + c33 * (cr137));
		}
	}
}
