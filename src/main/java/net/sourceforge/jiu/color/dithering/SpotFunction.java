/*
 * SpotFunction
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.dithering;

/**
 * An interface for spot functions to be used for clustered dot dithering.
 * @author Marco Schmidt
 * @since 0.9.0
 * @see ClusteredDotDither
 */
public interface SpotFunction
{
	/**
	 * Compute the spot intensity at the given position.
	 * @param x horizontal position, must be between -1.0 and 1.0 (including both)
	 * @param y vertical position, must be between -1.0 and 1.0 (including both)
	 * @return the function value, must be between 0.0 and 1.0 (including both)
	 */
	double compute(double x, double y);

	/**
	 * Returns if this spot function is balanced.
	 */
	boolean isBalanced();
}
