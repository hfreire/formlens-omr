/*
 * DiamondSpotFunction
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.dithering;

import net.sourceforge.jiu.color.dithering.SpotFunction;

/**
 * A diamond spot function.
 * @author Marco Schmidt
 * @since 0.9.0
 * @see ClusteredDotDither
 */
public class DiamondSpotFunction implements SpotFunction
{
	public double compute(double x, double y)
	{
		double xy = Math.abs(x) + Math.abs(y);
		if (xy <= 1)
		{
			return 0.5 * xy * xy;
		}
		else
		{
			double xy1 = xy - 1;
			return (2 * xy * xy - 4 * xy1 * xy1) / 4;
		}
	}

	public boolean isBalanced()
	{
		return false;
	}
}
