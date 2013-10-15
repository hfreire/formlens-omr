/*
 * MinimumFilter
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.filters.AreaFilterOperation;

/**
 * Filter operation that replaces each sample by the minimum value of itself
 * and its neighbors.
 * See {@link MaximumFilter} for a usage example.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class MinimumFilter extends AreaFilterOperation
{
	public final int computeSample(int[] samples, int numSamples)
	{
		int min = samples[--numSamples];
		while (numSamples != 0)
		{
			int value = samples[--numSamples];
			if (value < min)
			{
				min = value;
			}
		}
		return min;
	}
}
