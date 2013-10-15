/*
 * MedianFilter
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.filters.AreaFilterOperation;
import net.sourceforge.jiu.util.Median;

/**
 * Applies a Median filter that replaces each pixel by the median of
 * itself and its neighbors.
 * The number of neighbors can be defined with the setArea methods.
 * <p>
 * Can be used as despeckle filter, but the image will lose sharpness.
 * The larger the area becomes, the less noise and the less sharpness will remain,
 * and the longer it will take.
 * <p>
 * Uses {@link net.sourceforge.jiu.util.Median} to do the search for the median value.
 * <h3>Usage example</h3>
 * <pre>
 * PixelImage image = ...; // some GrayIntegerImage or RGBIntegerImage
 * MedianFilter filter = new MedianFilter();
 * filter.setArea(5, 5);
 * filter.setInputImage(image);
 * filter.process();
 * PixelImage filteredImage = filter.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class MedianFilter extends AreaFilterOperation
{
	public final int computeSample(int[] samples, int numSamples)
	{
		return Median.find(samples, 0, numSamples - 1);
	}
}
