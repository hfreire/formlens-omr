/*
 * MitchellFilter
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * The Mitchell resample filter.
 * @author Marco Schmidt
 * @since 0.10.0
 * @see Resample
 * @see ResampleFilter
 */
public class MitchellFilter extends ResampleFilter
{
	private final float B = 1.0f / 3.0f;
	private final float C = 1.0f / 3.0f;

	public float apply(float value)
	{
		if (value < 0.0f)
		{
			value = -value;
		}
		float tt = value * value;
		if (value < 1.0f)
		{
			value = (((12.0f - 9.0f * B - 6.0f * C) * (value * tt))
			+ ((-18.0f + 12.0f * B + 6.0f * C) * tt)
			+ (6.0f - 2f * B));
			return value / 6.0f;
		}
		else
		if (value < 2.0f)
		{
			value = (((-1.0f * B - 6.0f * C) * (value * tt))
			+ ((6.0f * B + 30.0f * C) * tt)
			+ ((-12.0f * B - 48.0f * C) * value)
			+ (8.0f * B + 24 * C));
			return value / 6.0f;
		}
		else
		{
			return 0.0f;
		}
	}

	public String getName()
	{
		return "Mitchell";
	}

	public float getRecommendedSamplingRadius()
	{
		return 2.0f;
	}
}
