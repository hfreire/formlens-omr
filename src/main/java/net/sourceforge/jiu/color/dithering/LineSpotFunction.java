/*
 * LineSpotFunction
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.dithering;

import net.sourceforge.jiu.color.dithering.SpotFunction;

/**
 * A line spot function.
 * @author Marco Schmidt
 * @since 0.9.0
 * @see ClusteredDotDither
 */
public class LineSpotFunction implements SpotFunction
{
	public double compute(double x, double y)
	{
		if (y < 0)
		{
			return -y;
		}
		else
		{
			return y;
		}
	}

	public boolean isBalanced()
	{
		return true;
	}
}
