/*
 * MaximumFilter
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.filters.AreaFilterOperation;

/**
 * Filter operation that replaces each sample by the maximum value of itself
 * and its neighbor samples.
 * <p>
 * Note that this is not the maximum operation that takes two input images
 * and, for each position, takes the maximum sample value and writes it 
 * to output.
 *
 * <h3>Usage example</h3>
 * <pre>
 * MaximumFilter filter = new MaximumFilter();
 * filter.setArea(7, 5);
 * filter.setInputImage(image);
 * filter.process();
 * PixelImage filteredImage = filter.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 * @since 0.9.0
 * @see MinimumFilter
 */
public class MaximumFilter extends AreaFilterOperation
{
	public final int computeSample(int[] samples, int numSamples)
	{
		int max = samples[0];
		int index = 1;
		while (index < numSamples)
		{
			int value = samples[index++];
			if (value > max)
			{
				max = value;
			}
		}
		return max;
	}
}
