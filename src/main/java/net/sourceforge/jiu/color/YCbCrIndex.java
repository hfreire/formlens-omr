/*
 * YCbCrIndex
 *
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color;

/**
 * This interface simply provides three integer constants as index
 * values for the three channels of an YCbCr image: gray,
 * blue chrominance and red chrominance.
 * The three values are guaranteed to lie in the interval 0 to 2.
 * Furthermore, all three values are different from each other, so
 * that the complete interval from 0 to 2 is used.
 * @see net.sourceforge.jiu.data.RGBIndex
 * @author Marco Schmidt
 */
public interface YCbCrIndex
{
	/**
	 * Index value for the luminance (gray) component.
	 */
	int INDEX_Y = 0;

	/**
	 * Index value for the blue chrominance component.
	 */
	int INDEX_CB = 1;

	/**
	 * Index value for the red chrominance component.
	 */
	int INDEX_CR = 2;
}
