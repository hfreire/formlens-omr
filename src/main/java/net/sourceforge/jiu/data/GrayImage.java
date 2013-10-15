/*
 * GrayImage
 *
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

/**
 * An interface for grayscale images.
 * Grayscale images have only one channel.
 * Each sample is a shade of gray, an intensity value between black (zero) and white (maximum value).
 * Black and white photos are really grayscale photos.
 * For images that only use black and white, see {@link BilevelImage}.
 *
 * @author Marco Schmidt
 * @since 0.8.0
 */
public interface GrayImage
{
	/**
	 * Returns if the pixel specified by the location in the arguments is black.
	 * @param x the horizontal location of the pixel
	 * @param y the vertical location of the pixel
	 * @throws IllegalArgumentException if any of the parameters are invalid
	 */
	boolean isBlack(int x, int y);

	/**
	 * Returns if the pixel specified by the location in the arguments is white.
	 * @param x the horizontal location of the pixel
	 * @param y the vertical location of the pixel
	 * @throws IllegalArgumentException if any of the parameters are invalid
	 */
	boolean isWhite(int x, int y);

	/**
	 * Sets a pixel to black (minimum intensity value).
	 * @param x horizontal position of the pixel's location
	 * @param y vertical position of the pixel's location
	 */
	void putBlack(int x, int y);

	/**
	 * Sets a pixel to white (maximum intensity value).
	 * @param x horizontal position of the pixel's location
	 * @param y vertical position of the pixel's location
	 */
	void putWhite(int x, int y);
}
