/*
 * RGBQuantizer
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.data.Palette;

/**
 * An interface for an RGB color quantizer.
 * Color quantizers take an input image and produce an output image
 * that looks like the original but uses less colors.
 * Keeping the error (the difference between input and output image) as small 
 * as possible is important - the more similar input and output are, the better.
 * <p>
 * Similarity between to pixels (or, more accurately, the colors of two pixels)
 * can be defined by their distance in color space.
 * Imagine two colors given by <em>(r<sub>1</sub>, g<sub>1</sub>, b<sub>1</sub>)</em>
 * and
 * <em>(r<sub>2</sub>, g<sub>2</sub>, b<sub>2</sub>)</em>.
 * The distance can then be defined as 
 * <em>sqrt((r<sub>1</sub> - r<sub>2</sub>)<sup>2</sup> + 
 *  (g<sub>1</sub> - g<sub>2</sub>)<sup>2</sup> + (b<sub>1</sub> - b<sub>2</sub>)<sup>2</sup>)</em>
 * (with <em>sqrt</em> being the square root).
 * <p>
 * A quantizer has two main tasks:
 * <ol>
 * <li><strong>Find a palette.</strong>
 *  Some quantizers create <em>custom</em> palettes for a
 *  given input (e.g. {@link MedianCutQuantizer} or {@link OctreeColorQuantizer}, 
 *  other quantizers use fixed palettes (e.g. {@link UniformPaletteQuantizer}).
 *  Using a custom palette typically results in a better output image 
 *  (it is more similar because it takes into consideration the content
 *  of the input).
 * However, using <em>fixed</em> palettes requires less CPU time and memory and
 * is sufficient in many cases from a point of view of output quality.
 * <p>
 * If a quantizer does use a fixed palette, this first step obviously is not
 * so much about finding the palette but about specifying it.
 *  </li>
 * <li><strong>Map the input image to the palette.</strong>
 *  For each pixel in the truecolor input image the mapping procedure must
 *  find the color in the palette that is closest to that input pixel
 *  so that the difference between source and destination pixel
 *  is as small as possible.
 * <p>
 * The code that does the mapping from the original to any given palette
 * could be shared among quantizers - after all, the goal is always the same,
 * picking the palette entry with the smallest distance in color space
 * to the original pixel.
 * However, sometimes the data structures built while finding the palette
 * can be reused for faster mapping from the original to output.
 * This is the case for both the MedianCutQuantizer and the OctreeColorQuantizer.
 * </li>
 * </ol>
 * <p>
 * Dithering methods like error diffusion dithering
 * may be used to increase
 * the quality of the output.
 * Note however that dithering introduces noise that makes the quantized image harder to
 * compress and in some cases unusable for post-processing (the noise may be an obstacle for
 * image processing algorithms).
 * <p>
 * This quantizer interface was designed with JIU's error diffusion dithering operation 
 * {@link net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering} in mind.
 * 
 *
 * @author Marco Schmidt
 */
public interface RGBQuantizer
{
	/**
	 * Return a Palette object with the list of colors to be used in the quantization
	 * process.
	 * That palette may be fixed or created specifically for a given input image.
	 * @return Palette object for destination image
	 */
	Palette createPalette();

	/**
	 * This method maps a triplet of intensity values to its quantized counterpart 
	 * and returns the palette index of that quantized color.
	 * The index values for the two arrays are taken from RGBIndex.
	 * @param origRgb the three samples red, green and blue for which a good match is searched in the palette
	 * @param quantizedRgb will hold the three samples found to be closest to origRgb after the call to this method
	 * @return int index in the palette of the match quantizedRgb
	 */
	int map(int[] origRgb, int[] quantizedRgb);
}
