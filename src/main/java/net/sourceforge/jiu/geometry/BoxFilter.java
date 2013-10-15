/*
 * BoxFilter
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * A box filter (also known as nearest neighbor).
 * @author Marco Schmidt
 * @since 0.10.0
 * @see Resample
 * @see ResampleFilter
 */
public class BoxFilter extends ResampleFilter
{
	public float apply(float value)
	{
		if (value > -0.5f && value <= 0.5f)
		{
			return 1.0f;
		}
		else
		{
			return 0.0f;
		}
	}

	public String getName()
	{
		return "Box";
	}

	public float getRecommendedSamplingRadius()
	{
		return 0.5f;
	}
}
