/*
 * BilevelImage
 *
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.GrayIntegerImage;

/**
 * An interface for bilevel pixel image data classes.
 * Each pixel in a bilevel image can have two possible values, {@link #BLACK} and {@link #WHITE}.
 * Those two constants are guaranteed to be <code>0</code> and <code>1</code>, although
 * you should not make any assumptions about what value any of the two constants has.
 * This is the type of image used for faxes.
 * Black and white photos are usually stored as grayscale images.
 * <p>
 * Apart from implementing {@link PixelImage} - like all image data classes in JIU - 
 * this interface is also an {@link IntegerImage} (each sample is either 0 or 1) and
 * a {@link GrayImage} (black and white are both grayscale values).
 * The combination of {@link IntegerImage} and {@link GrayImage} is {@link GrayIntegerImage},
 * which is the direct superinterface of this interface.
 *
 * <h3>Packed bytes</h3>
 *
 * There are methods to get and put <em>packed bytes</em> in this interface.
 * A packed byte is a <code>byte</code> value that stores eight horizontally neighbored bilevel 
 * pixels in it (pixel and sample can be used interchangeably in the context of bilevel images
 * because there is only one sample per pixel).
 * The most significant bit of such a packed bit is defined to be the leftmost of the
 * eight pixels, the second-most significant bit is the pixel to the right of that leftmost pixel,
 * and so on. The least significant bit is the rightmost pixel.
 * If a bit is set, the corresponing pixel value is supposed to be white, otherwise black.
 *
 * <h3>Usage examples</h3>
 * 
 * Here are some code examples that demonstrate how to access image data with this class.
 * <pre>
 * BilevelImage image = new MemoryBilevelImage(2000, 500);
 * // now set all pixels in the first row to white
 * for (int x = 0; x &lt; image.getWidth(); x++)
 * {
 *   image.putWhite(x, 0);
 * }
 * // put vertical stripes on the rest
 * for (int y = 1; y &lt; image.getHeight(); y++)
 * {
 *   for (int x = 0; x &lt; image.getWidth(); x++)
 *   {
 *     int sample;
 *     if ((x % 2) == 0)
 *     {
 *       sample = BilevelImage.BLACK;
 *     }
 *     else
 *     {
 *       sample = BilevelImage.WHITE;
 *     }
 *     image.putSample(x, y, sample);
 *   }
 * }
 * </pre>
 *
 * @author Marco Schmidt
 */
public interface BilevelImage extends GrayIntegerImage
{
	/**
	 * The value for a black pixel.
	 * To be used with all methods that require <code>int</code> arguments for sample values.
	 * You can rely on this value being either 0 or 1 (that way you can safely store it
	 * in a byte or short).
	 */
	int BLACK = 0;

	/**
	 * The value for a white pixel.
	 * To be used with all methods that require <code>int</code> arguments for sample values.
	 * You can rely on this value being either 0 or 1 (that way you can safely store it
	 * in a byte or short).
	 */
	int WHITE = 1;

	/**
	 * Sets a number of samples in the argument array from this image.
	 * @param x horizontal position of first sample of this image to read
	 * @param y vertical position of samples to be read from this image
	 * @param numSamples number of samples to be set
	 * @param dest array with packed pixels to which samples are copied
	 * @param destOffset index into dest array of the first byte value to write sample values to
	 * @param destBitOffset index of first bit of <code>dest[destOffset]</code> to write a sample to (0 is leftmost, 1 is second-leftmost up to 7, which is the rightmost)
	 */
	void getPackedBytes(int x, int y, int numSamples, byte[] dest, int destOffset, int destBitOffset);

	/**
	 * Sets a number of samples in the image from the argument array data.
	 * @param x horizontal position of first sample to be set
	 * @param y vertical position of samples to be set
	 * @param numSamples number of samples to be set
	 * @param src array with packed pixels to be set
	 * @param srcOffset index into src array of the first byte value to read sample values from
	 * @param srcBitOffset index of first bit of <code>src[srcOffset]</code> to 
	 *  read a sample from (0 is leftmost, 1 is second-leftmost up to 7, which is the rightmost)
	 */
	void putPackedBytes(int x, int y, int numSamples, byte[] src, int srcOffset, int srcBitOffset);
}
