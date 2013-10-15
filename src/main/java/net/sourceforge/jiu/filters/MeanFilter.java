/*
 * MeanFilter
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.filters.AreaFilterOperation;

/**
 * Applies a mean filter that replaces each pixel by the mean of itself and its neighbors.
 * The number of neighbors can be defined by the setArea methods.
 * This filter only works with intensity-based image types.
 * More precisely, only {@link net.sourceforge.jiu.data.GrayIntegerImage} and
 * {@link net.sourceforge.jiu.data.RGBIntegerImage} will work.
 * <h3>Usage example</h3>
 * <pre>
 * PixelImage image = ...; // some GrayIntegerImage or RGBIntegerImage
 * MeanFilter filter = new MeanFilter();
 * filter.setArea(5, 5);
 * filter.setInputImage(image);
 * filter.process();
 * PixelImage filteredImage = filter.getOutputImage();
 * </pre>
 * @since 0.5.0
 * @author Marco Schmidt
 */
public class MeanFilter extends AreaFilterOperation
{
	public int computeSample(int[] samples, int numSamples)
	{
		int sum = 0;
		int index = numSamples;
		do
		{
			sum += samples[--index];
		}
		while (index != 0);
		return sum / numSamples;
	}
}
