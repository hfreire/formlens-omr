/*
 * BSplineFilter
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * A B-spline resample filter.
 * @author Marco Schmidt
 * @since 0.10.0
 * @see Resample
 * @see ResampleFilter
 */
public class BSplineFilter extends ResampleFilter
{
	public float apply(float value)
	{
		if (value < 0.0f)
		{
			value = - value;
		}
		if (value < 1.0f)
		{
			float tt = value * value;
			return 0.5f * tt * value - tt + (2.0f / 3.0f);
		}
		else
		if (value < 2.0f)
		{
			value = 2.0f - value;
			return (1.0f / 6.0f) * value * value * value;
		}
		else
		{
			return 0.0f;
		}
	}

	public String getName()
	{
		return "B-Spline";
	}

	public float getRecommendedSamplingRadius()
	{
		return 2.0f;
	}
}
