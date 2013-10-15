/*
 * Lanczos3Filter
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * The Lanczos 3 resample filter.
 * @author Marco Schmidt
 * @since 0.10.0
 * @see Resample
 * @see ResampleFilter
 */
public class Lanczos3Filter extends ResampleFilter
{
	private double sinc(double value)
	{
		if (value != 0.0f)
		{
			value = value * Math.PI;
			return Math.sin(value) / value;
		}
		else
		{
			return 1.0;
		}
	}

	public float apply(float value)
	{
		if (value < 0.0f)
		{
			value = -value;
		}
		if (value < 3.0f)
		{
			return (float)(sinc(value) * sinc(value / 3.0));
		}
		else
		{
			return 0.0f;
		}
	}

	public String getName()
	{
		return "Lanczos3";
	}

	public float getRecommendedSamplingRadius()
	{
		return 3.0f;
	}
}
