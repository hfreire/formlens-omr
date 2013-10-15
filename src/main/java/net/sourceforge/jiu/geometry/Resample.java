/*
 * Resample
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

// 2007-04-19 optimization results
//* 10393 original speed in ms
//* 6053 innerloop fix
//* 5688 if .. continue loop break
//* 5485 replaced contrib[] array access by variable
//* 5173 doing the same on first pass (except innerloop fix which was ok there)
// global gain is 2x faster

/* This is the beginning of resample.pas, the Unit on which this class is based:
// -----------------------------------------------------------------------------
// Project:	bitmap resampler
// Module:	resample
// Description: Interpolated Bitmap Resampling using filters.
// Version:	01.03
// Release:	4
// Date:	29-JUN-1999
// Target:	Win32, Delphi 2, 3 & 4
// Author(s):	anme: Anders Melander, anders@melander.dk
// Copyright	(c) 1997-99 by Anders Melander
// Formatting:	2 space indent, 8 space tabs, 80 columns.
// -----------------------------------------------------------------------------
// This software is copyrighted as noted above.  It may be freely copied,
// modified, and redistributed, provided that the copyright notice(s) is
// preserved on all copies.
//
// There is no warranty or other guarantee of fitness for this software,
// it is provided solely "as is".  Bug reports or fixes may be sent
// to the author, who may or may not act on them as he desires.
//
// You may not include this software in a program or other software product
// without supplying the source, or without informing the end-user that the
// source is available for no extra charge.
//
// If you modify this software, you should include a notice in the "Revision
// history" section giving the name of the person performing the modification,
// the date of modification, and the reason for such modification.
// -----------------------------------------------------------------------------
// Here's some additional copyrights for you:
//
// From filter.c:
// The authors and the publisher hold no copyright restrictions
// on any of these files; this source code is public domain, and
// is freely available to the entire computer graphics community
// for study, use, and modification.  We do request that the
// comment at the top of each file, identifying the original
// author and its original publication in the book Graphics
// Gems, be retained in all programs that use these files.
//
// -----------------------------------------------------------------------------
// Revision history:
//
// 0100	110997	anme	- Adapted from Dale Schumacher's fzoom v0.20.
//
// 0101	110198	anme	- Added Lanczos3 and Mitchell filters.
//			- Fixed range bug.
//			  Min value was not checked on conversion from Single to
//			  byte.
//			- Numerous optimizations.
//			- Added TImage stretch on form resize.
//			- Added support for Delphi 2 via TCanvas.Pixels.
//			- Renamed module from stretch to resample.
//			- Moved demo code to separate module.
//
// 0102 150398	anme	- Fixed a problem that caused all pixels to be shifted
//			  1/2 pixel down and to the right (in source
//			  coordinates). Thanks to David Ullrich for the
//			  solution.
//
// 0103	170898	anme	- Fixed typo: Renamed Strecth function to Stretch.
//			  Thanks to Graham Stratford for spotting it.
//			  Sorry about that.
//	081298	anme	- Added check for too small destination bitmap.
//			  Thanks to Jeppe Oland for bringing this problem to my
//			  attention.
// 	260399	anme	- Fixed a problem with resampling of very narrow
//			  bitmaps. Thanks to Holger Dors for bringing the
//			  problem to my attention.
//			- Removed dependency of math unit.
//	290699	jobe	- Subsampling improvements by Josha Beukema.
//
// -----------------------------------------------------------------------------
// Credits:
// The algorithms and methods used in this library are based on the article
// "General Filtered Image Rescaling" by Dale Schumacher which appeared in the
// book Graphics Gems III, published by Academic Press, Inc.
//
// The edge offset problem was fixed by:
//   * David Ullrich <ullrich@hardy.math.okstate.edu>
//
// The subsampling problem was fixed by:
//   * Josha Beukema <jbeukema@inn.nl>
// -----------------------------------------------------------------------------
// To do (in rough order of priority):
// * Implement Dale Schumacher's "Optimized Bitmap Scaling Routines".
// * Optimize to use integer math instead of floating point where possible.
// -----------------------------------------------------------------------------
*/

/**
 * Resizes grayscale and truecolor images using filters.
 * For other image types (including paletted or bilevel images), you might
 * want to use the {@link ScaleReplication} class or convert the images to
 * grayscale (or RGB truecolor) first and then use this class.
 * Several algorithms for resampling are implemented, they differ in resulting image quality 
 * and computational complexity.
 *
 * <h3>Usage example</h3>
 * This will scale <code>image</code> to 150 percent of its original size
 * in both directions, using the Lanczos3 filter type:
 * <pre>
 * Resample resample = new Resample();
 * resample.setInputImage(image);
 * resample.setSize(image.getWidth() * 3 / 2, image.getHeight() * 3 / 2);
 * resample.setFilter(Resample.FILTER_TYPE_LANCZOS3);
 * resample.process();
 * PixelImage scaledImage = resample.getOutputImage();
 * </pre>
 * 
 * <h3>Known problems</h3>
 * <ul>
 * <li>Scaling down certain images (with stripe or checkers patterns in them) will
 *  lead to moire effects in the resulting image.
 *  These effects can be somewhat reduced by scaling down in several step (e.g.
 *  first from 1600 x 1200 to 800 x 600, then from 800 x 600 to 400 x 300, and so on).</li>
 * <li>Scaling down with filter type {@link #FILTER_TYPE_BOX} can lead to errors in the scaled image.
 *  No fix known yet. Workaround: Use the class {@link ScaleReplication}.</li>
 * </ul>
 *
 * <h3>Origin</h3>
 * This code is a port of Anders Melander's
 * Object Pascal (Delphi) unit <tt>resample.pas</tt> to Java.
 * The Delphi code is an adaptation (with some improvements) of 
 * Dale Schumacher's <tt>fzoom</tt> C code.
 * <del>Check out the homepage for the Delphi resample code, a demo application
 * to compare the different filtering algorithms is also provided:
 * <a target="_top" href="http://www.melander.dk/delphi/resampler/index.html">http://www.melander.dk/delphi/resampler/index.html</a>.
 * You will also find the original C code there.</del>
 * <ins>The site seems to have gone for good.</ins>
 *
 * <h3>Theory</h3>
 * The theoretical background for all implementations is Dale Schumacher's article
 * <em>General Filtered Image Rescaling</em>
 * in <em>Graphics Gems III</em>, editor David Kirk, Academic Press, pages 8-16, 1994.
 * <p>
 * The <em>Graphics Gems Repository</em> can be found at
 * <a target="_top" href="http://www.acm.org/tog/GraphicsGems/">http://www.acm.org/tog/GraphicsGems/</a>.
 * It also includes information on the books and how to order them.
 *
 * @author Marco Schmidt
 */
public class Resample extends ImageToImageOperation
{
	/**
	 * Constant for the Box filter (also known as Nearest Neighbor filter).
	 */
	public static final int FILTER_TYPE_BOX = 0;

	/**
	 * Constant for the Triangle filter (also known as Linear filter or Bilinear filter).
	 */
	public static final int FILTER_TYPE_TRIANGLE = 1;

	/**
	 * Constant for the Hermite filter.
	 */
	public static final int FILTER_TYPE_HERMITE = 2;

	/**
	 * Constant for the Bell filter.
	 */
	public static final int FILTER_TYPE_BELL = 3;

	/**
	 * Constant for the B-Spline filter.
	 */
	public static final int FILTER_TYPE_B_SPLINE = 4;

	/**
	 * Constant for the Lanczos3 filter.
	 */
	public static final int FILTER_TYPE_LANCZOS3 = 5;

	/**
	 * Constant for the Mitchell filter.
	 */
	public static final int FILTER_TYPE_MITCHELL = 6;

	class Contributor
	{
		int pixel; // Source pixel
		float weight; // Pixel weight
	}

	class CList
	{
		int n;
		Contributor[] p;
	}

	private Integer outWidth;
	private Integer outHeight;
	private ResampleFilter filter;

	private static ResampleFilter createFilter(int filterType)
	{
		switch(filterType)
		{
			case(FILTER_TYPE_BOX): return new BoxFilter();
			case(FILTER_TYPE_TRIANGLE): return new TriangleFilter();
			case(FILTER_TYPE_HERMITE): return new HermiteFilter();
			case(FILTER_TYPE_BELL): return new BellFilter();
			case(FILTER_TYPE_B_SPLINE): return new BSplineFilter();
			case(FILTER_TYPE_LANCZOS3): return new Lanczos3Filter();
			case(FILTER_TYPE_MITCHELL): return new MitchellFilter();
			default:
			{
				throw new IllegalArgumentException("Unknown filter type in Resample: " + filterType);
			}
		}
	}

	/**
	 * Returns the filter to be used in this operation.
	 * @return ResampleFilter object or <code>null</code> if none was defined yet
	 */
	public ResampleFilter getFilter()
	{
		return filter;
	}

	/**
	 * Returns the names of all predefined filters.
	 * Each FILTER_TYPE_xyz constant can be used as an index into the array that is returned.
	 * Names are retrieved by creating an object of each predefined filter class and calling its
	 * getName method.
	 * @return String array with filter names
	 */
	public static String[] getFilterNames()
	{
		String[] result = new String[getNumFilters()];
		for (int i = 0; i < getNumFilters(); i++)
		{
			ResampleFilter filter = createFilter(i);
			result[i] = filter.getName();
		}
		return result;
	}

	/**
	 * Returns the number of predefined filters.
	 * @return number of filters
	 */
	public static int getNumFilters()
	{
		return 7;
	}

	/**
	 * This method does the actual work of rescaling an image.
	 */
	private void process(IntegerImage in, IntegerImage out)
	{
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(outWidth.intValue(), outHeight.intValue());
			setOutputImage(out);
		}
		if (filter == null)
		{
			filter = new TriangleFilter();
		}
		float fwidth = filter.getSamplingRadius();
		final int dstWidth = outWidth.intValue();
		final int dstHeight = outHeight.intValue();
		final int srcWidth = in.getWidth();
		final int srcHeight = in.getHeight();
 		/* if (SrcWidth < 1) or (SrcHeight < 1) then
    raise Exception.Create('Source bitmap too small');*/
		// Create intermediate image to hold horizontal zoom
		IntegerImage work = (IntegerImage)in.createCompatibleImage(dstWidth, srcHeight);
		float xscale;
		float yscale;
		if (srcWidth == 1)
		{
			xscale = (float)dstWidth / (float)srcWidth;
		}
		else
		{
			xscale = (float)(dstWidth - 1) / (float)(srcWidth - 1);
		}
		if (srcHeight == 1)
		{
			yscale = (float)dstHeight / (float)srcHeight;
		}
		else
		{
			yscale = (float)(dstHeight - 1) / (float)(srcHeight - 1);
		}

		/* Marco: the following two variables are used for progress notification */
		int processedItems = 0;
		int totalItems = /*dstWidth +*/ srcHeight + /*dstHeight +*/ dstWidth;

		// --------------------------------------------
		// Pre-calculate filter contributions for a row
		// -----------------------------------------------
		CList[] contrib = new CList[dstWidth];
		for (int i = 0; i < contrib.length; i++)
		{
			contrib[i] = new CList();
		}
		
		// Horizontal sub-sampling
		// Scales from bigger to smaller width
		if (xscale < 1.0f)
		{
			float width = fwidth / xscale;
			float fscale = 1.0f / xscale;
			int numPixels = (int)(width * 2.0f + 1);
			for (int i = 0; i < dstWidth; i++)
			{
				contrib[i].n = 0;
				contrib[i].p = new Contributor[numPixels];
				for (int j = 0; j < contrib[i].p.length; j++)
				{
					contrib[i].p[j] = new Contributor();
				}
				float center = i / xscale;
				int left = (int)Math.floor(center - width);
				int right = (int)Math.ceil(center + width);
				for (int j = left; j <= right; j++)
				{
					float weight = filter.apply((center - j) / fscale) / fscale;
					if (weight == 0.0f)
					{
						continue;
					}
					int n;
					if (j < 0)
					{
						n = -j;
					}
					else
					if (j >= srcWidth)
					{
						n = srcWidth - j + srcWidth - 1;
					}
					else
					{
						n = j;
					}
					int k = contrib[i].n;
					contrib[i].n = contrib[i].n + 1;
					contrib[i].p[k].pixel = n;
					contrib[i].p[k].weight = weight;
				}
				//setProgress(processedItems++, totalItems);
			}
		}
		else
    	// Horizontal super-sampling
	   	// Scales from smaller to bigger width
    	{
			int numPixels = (int)(fwidth * 2.0f + 1);
			for (int i = 0; i < dstWidth; i++)
			{
    			contrib[i].n = 0;
				contrib[i].p = new Contributor[numPixels];
				for (int j = 0; j < contrib[i].p.length; j++)
				{
					contrib[i].p[j] = new Contributor();
				}
				float center = i / xscale;
				int left = (int)Math.floor(center - fwidth);
				int right = (int)Math.ceil(center + fwidth);
				for (int j = left; j <= right; j++)
				{
					float weight = filter.apply(center - j);
					if (weight == 0.0f)
					{
						continue;
					}
					int n;
					if (j < 0)
					{
						n = -j;
					}
					else
					if (j >= srcWidth)
					{
						n = srcWidth - j + srcWidth - 1;
					}
					else
					{
						n = j;
					}
					int k = contrib[i].n;
					if (n < 0 || n >= srcWidth)
					{
						weight = 0.0f;
					}
					contrib[i].n = contrib[i].n + 1;
					contrib[i].p[k].pixel = n;
					contrib[i].p[k].weight = weight;
				}
				//setProgress(processedItems++, totalItems);
			}
		}
		
		// ----------------------------------------------------
		// Apply filter to sample horizontally from Src to Work
		// ----------------------------------------------------

		// start of Java-specific code
		// Marco: adjusted code to work with multi-channel images
		//        where each channel can have a different maximum sample value (not only 255)
		final int NUM_CHANNELS = work.getNumChannels();
		final int[] MAX = new int[NUM_CHANNELS];
		for (int k = 0; k < NUM_CHANNELS; k++)
		{
			MAX[k] = work.getMaxSample(k);
		}
		// end of Java-specific code

		for (int k = 0; k < srcHeight; k++)
		{
			for (int i = 0; i < dstWidth; i++)
			{
				for (int channel = 0; channel < NUM_CHANNELS; channel++)
				{
                    CList c=contrib[i];
					float sample = 0.0f;
                    int max=c.n;
					for (int j = 0; j < max; j++)
					{
						sample+=in.getSample(channel, c.p[j].pixel, k) * c.p[j].weight;
					}
					// Marco: procedure BoundRound included directly
					int result = (int)sample;
					if (result < 0)
					{
						result = 0;
					}
					else
					if (result > MAX[channel])
					{
						result = MAX[channel];
					}
					work.putSample(channel, i, k, result);
				}
			}
			setProgress(processedItems++, totalItems);
		}

		/* Marco: no need for "free memory" code as Java has garbage collection:
		    // Free the memory allocated for horizontal filter weights
    		for i := 0 to DstWidth-1 do
		      	FreeMem(contrib^[i].p);

		    FreeMem(contrib);
		*/

		// -----------------------------------------------
		// Pre-calculate filter contributions for a column
		// -----------------------------------------------

    	/*GetMem(contrib, DstHeight* sizeof(TCList));*/
		contrib = new CList[dstHeight];
		for (int i = 0; i < contrib.length; i++)
		{
			contrib[i] = new CList();
		}
		// Vertical sub-sampling
		// Scales from bigger to smaller height
		if (yscale < 1.0f)
		{
			float width = fwidth / yscale;
			float fscale = 1.0f / yscale;
			int numContributors = (int)(width * 2.0f + 1);
			for (int i = 0; i < dstHeight; i++)
			{
				contrib[i].n = 0;
				contrib[i].p = new Contributor[numContributors];
				for (int j = 0; j < contrib[i].p.length; j++)
				{
					contrib[i].p[j] = new Contributor();
				}
				float center = i / yscale;
				int left = (int)Math.floor(center - width);
				int right = (int)Math.ceil(center + width);
				for (int j = left; j <= right; j++)
				{
					float weight = filter.apply((center - j) / fscale) / fscale;
					// change suggested by Mike Dillon; not thoroughly tested;
					// old version:
					// float weight = filter.apply(center - j);
					if (weight == 0.0f)
					{
						continue;
					}
					int n;
					if (j < 0)
					{
						n = -j;
					}
					else
					if (j >= srcHeight)
					{
						n = srcHeight - j + srcHeight - 1;
					}
					else
					{
						n = j;
					}
					int k = contrib[i].n;
					contrib[i].n = contrib[i].n + 1;
					if (n < 0 || n >= srcHeight)
					{
						weight = 0.0f;// Flag that cell should not be used
					}
					contrib[i].p[k].pixel = n;
					contrib[i].p[k].weight = weight;
				}
				//setProgress(processedItems++, totalItems);
			}
		}
		else
		// Vertical super-sampling
		// Scales from smaller to bigger height
		{
			int numContributors = (int)(fwidth * 2.0f + 1);
			for (int i = 0; i < dstHeight; i++)
			{
				contrib[i].n = 0;
				contrib[i].p = new Contributor[numContributors];
				for (int j = 0; j < contrib[i].p.length; j++)
				{
					contrib[i].p[j] = new Contributor();
				}
				float center = i / yscale;
				int left = (int)Math.floor(center - fwidth);
				int right = (int)Math.ceil(center + fwidth);
				for (int j = left; j <= right; j++)
				{
					float weight = filter.apply(center - j);
					if (weight == 0.0f)
					{
						continue;
					}
					int n;
					if (j < 0)
					{
						n = -j;
					}
					else
					if (j >= srcHeight)
					{
						n = srcHeight - j + srcHeight - 1;
					}
					else
					{
						n = j;
					}
					int k = contrib[i].n;
					contrib[i].n = contrib[i].n + 1;
					if (n < 0 || n >= srcHeight)
					{
						weight = 0.0f;// Flag that cell should not be used
					}
					contrib[i].p[k].pixel = n;
					contrib[i].p[k].weight = weight;
				}
				//setProgress(processedItems++, totalItems);
			}
		}

		// --------------------------------------------------
		// Apply filter to sample vertically from Work to Dst
		// --------------------------------------------------
		for (int k = 0; k < dstWidth; k++)
		{
			for (int i = 0; i < dstHeight; i++)
			{
				for (int channel = 0; channel < NUM_CHANNELS; channel++)
				{
					float sample = 0.0f;
                    CList c=contrib[i];
                    int max=c.n;
                    for (int j = 0; j < max; j++)
					{
                      sample += work.getSample(channel, k, c.p[j].pixel) * c.p[j].weight;
					}
                    int result = (int)sample; 
                    if (result < 0)
                    {
                        result = 0;
                    }
                    else
                    if (result > MAX[channel])
                    {
                        result = MAX[channel];
                    }
                    out.putSample(channel, k, i, result);
				}
			}
			setProgress(processedItems++, totalItems);
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		if (outWidth == null && outHeight == null && getOutputImage() != null)
		{
			PixelImage out = getOutputImage();
			outWidth = new Integer(out.getWidth());
			outHeight = new Integer(out.getHeight());
		}
		if (outWidth == null)
		{
			throw new MissingParameterException("Output width has not been initialized");
		}
		if (outHeight == null)
		{
			throw new MissingParameterException("Output height has not been initialized");
		}
		PixelImage image = getInputImage();
		if (image.getWidth() == outWidth.intValue() && 
		    image.getHeight() == outHeight.intValue())
		{
			throw new WrongParameterException("Input image already has the size specified by setSize.");
		}
		ensureOutputImageResolution(outWidth.intValue(), outHeight.intValue());
		if (image instanceof IntegerImage)
		{
			process((IntegerImage)image, (IntegerImage)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Input image must implement IntegerImage.");
		}
	}

	/**
	 * Set the pixel resolution of the output image.
	 * @param width the horizontal resolution of the output image
	 * @param height the vertical resolution of the output image
	 */
	public void setSize(int width, int height)
	{
		outWidth = new Integer(width);
		outHeight = new Integer(height);
	}

	/**
	 * Set a new filter object to be used with this operation.
	 * @param newFilter a resample filter to be used for scaling
	 */
	public void setFilter(ResampleFilter newFilter)
	{
		filter = newFilter;
	}

	/**
	 * Sets a new filter type, using the default sampling radius of that filter.
	 * @param filterType the new filter type, one of the FILTER_TYPE_xyz constants of this class
	 */
	public void setFilter(int filterType)
	{
		setFilter(createFilter(filterType));
	}

	/**
	 * Sets a new filter type with a user-defined sampling radius.
	 * @param filterType the new filter type, one of the FILTER_TYPE_xyz constants of this class
	 * @param samplingRadius the sampling radius to be used with that filter, must be larger than 0.0f
	 */
	public void setFilter(int filterType, float samplingRadius)
	{
		ResampleFilter newFilter = createFilter(filterType);
		newFilter.setSamplingRadius(samplingRadius);
		setFilter(newFilter);
	}
}
