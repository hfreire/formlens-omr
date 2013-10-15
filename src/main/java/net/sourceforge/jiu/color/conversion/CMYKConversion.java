/*
 * CMYKConversion
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.conversion;

import net.sourceforge.jiu.data.RGBIndex;

/**
 * Convert from CMYK color space to RGB color space.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class CMYKConversion
{
	private CMYKConversion()
	{
	}

	private static int convertToByte(int value)
	{
		if (value <= 0)
		{
			return 0;
		}
		else
		if (value >= 255)
		{
			return 255;
		}
		else
		{
			return value;
		}
	}

	/**
	 * Converts a 32 bit CMYK pixel to a 24 bit RGB pixel.
	 * Red, green and blue sample will be written at the indexes that {@link net.sourceforge.jiu.data.RGBIndex} defines for them.
	 * @param cyan the cyan sample, must lie in the interval 0 to 255
	 * @param magenta the magenta sample, must lie in the interval 0 to 255
	 * @param yellow the yellow sample, must lie in the interval 0 to 255
	 * @param black the black sample, must lie in the interval 0 to 255
	 * @param rgb byte array for the destination R-G-B pixel, must have length 3 or larger, will be accessed using RGBIndex, each sample will lie in the interval 0 to 255
	 */
	public static void convertCMYK32ToRGB24(int cyan, int magenta, int yellow, int black, int[] rgb)
	{
		int red = 255 - cyan;
		int green = 255 - magenta;
		int blue = 255 - yellow;
		red -= black;
		green -= black;
		blue -= black;
		rgb[RGBIndex.INDEX_RED] = convertToByte(red);
		rgb[RGBIndex.INDEX_GREEN] = convertToByte(green);
		rgb[RGBIndex.INDEX_BLUE] = convertToByte(blue);
	}

	/**
	 * Converts a number of CMYK pixels stored in interleaved order (all samples of one pixel 
	 * together: CMYKCMYKCMYK...) to RGB pixels which are stored as planes (all red samples 
	 * together, etc.).
	 * @param cmyk a byte array with numPixels times four samples stored in order C-M-Y-K
	 * @param cmykOffset the index of the first byte that is to be accessed
	 * @param red the byte array to which the red samples will be written by this method
	 * @param redOffset the offset into the red array of the first sample to be written
	 * @param green the byte array to which the green samples will be written by this method
	 * @param greenOffset the offset into the green array of the first sample to be written
	 * @param blue the byte array to which the blue samples will be written by this method
	 * @param blueOffset the offset into the blue array of the first sample to be written
	 */
	public static void convertCMYK32InterleavedToRGB24Planar(
		byte[] cmyk, int cmykOffset, 
		byte[] red, int redOffset, 
		byte[] green, int greenOffset, 
		byte[] blue, int blueOffset, 
		int numPixels)
	{
		int[] rgb = new int[3];
		while (numPixels-- != 0)
		{
			convertCMYK32ToRGB24(
				cmyk[cmykOffset] & 0xff, 
				cmyk[cmykOffset + 1] & 0xff, 
				cmyk[cmykOffset + 2] & 0xff, 
				cmyk[cmykOffset + 3] & 0xff, 
				rgb);
			cmykOffset += 4;
			red[redOffset++] = (byte)rgb[RGBIndex.INDEX_RED];
			green[greenOffset++] = (byte)rgb[RGBIndex.INDEX_GREEN];
			blue[blueOffset++] = (byte)rgb[RGBIndex.INDEX_BLUE];
		}
	}

	public static void convertCMYK32PlanarToRGB24Planar(
		byte[] cyan, int cyanOffset,
		byte[] magenta, int magentaOffset,
		byte[] yellow, int yellowOffset,
		byte[] black, int blackOffset,
		byte[] red, int redOffset, 
		byte[] green, int greenOffset, 
		byte[] blue, int blueOffset, 
		int numPixels)
	{
		int[] rgb = new int[3];
		while (numPixels-- != 0)
		{
			convertCMYK32ToRGB24(
				cyan[cyanOffset++] & 0xff, 
				magenta[magentaOffset++] & 0xff, 
				yellow[yellowOffset++] & 0xff, 
				black[blackOffset++] & 0xff, 
				rgb);
			red[redOffset++] = (byte)rgb[RGBIndex.INDEX_RED];
			green[greenOffset++] = (byte)rgb[RGBIndex.INDEX_GREEN];
			blue[blueOffset++] = (byte)rgb[RGBIndex.INDEX_BLUE];
		}
	}
}
