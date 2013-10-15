/*
 * TriangleFilter
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * A triangle filter (also known as linear or bilinear filter).
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class TriangleFilter extends ResampleFilter
{
	public float apply(float value)
	{
		if (value < 0.0f)
		{
			value = -value;
		}
		if (value < 1.0f)
		{
			return 1.0f - value;
		}
		else
		{
			return 0.0f;
		}
	}

	public String getName()
	{
		return "Triangle (bilinear)";
	}

	public float getRecommendedSamplingRadius()
	{
		return 1.0f;
	}
}
