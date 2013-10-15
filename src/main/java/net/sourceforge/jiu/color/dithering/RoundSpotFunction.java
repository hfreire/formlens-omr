/*
 * RoundSpotFunction
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.dithering;

import net.sourceforge.jiu.color.dithering.SpotFunction;

/**
 * A round spot function.
 * @author Marco Schmidt
 * @since 0.9.0
 * @see ClusteredDotDither
 */
public class RoundSpotFunction implements SpotFunction
{
	public double compute(double x, double y)
	{
		return 1.0 - x * x - y * y;
	}

	public boolean isBalanced()
	{
		return false;
	}
}
