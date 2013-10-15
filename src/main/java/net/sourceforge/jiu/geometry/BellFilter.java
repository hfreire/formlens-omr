/*
 * BellFilter
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * A Bell resample filter.
 * @author Marco Schmidt
 * @since 0.10.0
 * @see Resample
 * @see ResampleFilter
 */
public class BellFilter extends ResampleFilter
{
	public float apply(float value)
	{
		if (value < 0.0f)
		{
			value = - value;
		}
		if (value < 0.5f)
		{
			return 0.75f - (value * value);
		}
		else
		if (value < 1.5f)
		{
			value = value - 1.5f;
			return 0.5f * (value * value);
		}
		else
		{
			return 0.0f;
		}
	}

	public String getName()
	{
		return "Bell";
	}

	public float getRecommendedSamplingRadius()
	{
		return 1.5f;
	}
}
