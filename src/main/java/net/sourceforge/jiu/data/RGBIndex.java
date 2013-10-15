/*
 * RGBIndex
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

/**
 * This interface provides three <code>int</code> constants as index
 * values for the three channels of an RGB image: red, green and blue.
 * The three values are guaranteed to lie in the interval 0 to 2.
 * Furthermore, all three values are different from each other, so
 * that the complete interval from 0 to 2 is used.
 * @author Marco Schmidt
 */
public interface RGBIndex
{
	/**
	 * The index value for the red channel.
	 */
	int INDEX_RED = 0;

	/**
	 * The index value for the green channel.
	 */
	int INDEX_GREEN = 1;

	/**
	 * The index value for the blue channel.
	 */
	int INDEX_BLUE = 2;
}
