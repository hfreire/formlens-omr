/*
 * HermiteFilter
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * A Hermite resampling filter.
 * @author Marco Schmidt
 * @since 0.10.0
 * @see Resample
 * @see ResampleFilter
 */
public class HermiteFilter extends ResampleFilter
{
	public float apply(float value)
	{
		if (value < 0.0f)
		{
			value = - value;
		}
		if (value < 1.0f)
		{
			return (2.0f * value - 3.0f) * value * value + 1.0f;
		}
		else
		{
			return 0.0f;
		}
	}

	public String getName()
	{
		return "Hermite";
	}

	public float getRecommendedSamplingRadius()
	{
		return 1.0f;
	}
}
