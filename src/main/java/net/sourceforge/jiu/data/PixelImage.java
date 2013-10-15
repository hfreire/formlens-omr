/*
 * PixelImage
 *
 * Copyright (c) 2000, 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

/**
 * The base interface for all image data types in JIU.
 * These image data classes and interfaces share the following properties:
 * <ul>
 * <li>They are made up of a rectangular grid of <em>pixels</em> (color dots) and 
 *   have a fixed <em>width</em> (horizontal number of pixels) and <em>height</em>
 *   (vertical number of pixels).</li>
 * <li>Coordinates to access pixels in the image are defined to run from 0 to 
 *   WIDTH - 1 in horizontal direction from left to right and in vertical 
 *   direction from 0 to HEIGHT - 1 (from top to bottom).</li>
 * <li>They have one or more channels. A pixel at a given position is made up
 *   of the <em>samples</em> (primitive values) of all the channels at that 
 *   position.
 *   A pixel can thus be considered a vector (in the mathematical sense) of one or more samples.
 *   Each channel has the same width and height.</li>
 * </ul>
 *
 * @author Marco Schmidt
 */
public interface PixelImage
{
	/**
	 * Creates an instance of the same class as this one, with width and height
	 * given by the arguments.
	 * @param width the horizontal resolution of the new image
	 * @param height the vertical resolution of the new image
	 * @return the new image
	 * @throws IllegalArgumentException if width or height are smaller than one
	 */
	PixelImage createCompatibleImage(int width, int height);

	/**
	 * Creates an new image object that will be of the same type as this one,
	 * with the same image data, using entirely new resources.
	 * @return the new image object
	 */
	PixelImage createCopy();

	/**
	 * Returns the number of bytes that were dynamically allocated for 
	 * this image object.
	 * @return allocated memory in bytes
	 */
	long getAllocatedMemory();

	/**
	 * Returns the number of bits per pixel of this image.
	 * That is the number of bits per sample for all channels of this image.
	 * Does not include any transparency channels.
	 */
	 int getBitsPerPixel();

	/**
	 * Returns the vertical resolution of the image in pixels.
	 * Must be one or larger.
	 * @return height in pixels
	 */
	int getHeight();

	/**
	 * If there is a single interface or class that describes the image data type
	 * of this class, the {@link java.lang.Class} object associated with that
	 * interface (or class) is returned (or <code>null</code> otherwise).
	 * This {@link java.lang.Class} object, if available for two image objects,
	 * can be used to find out if they are compatible.
	 * Example: {@link net.sourceforge.jiu.data.MemoryGray8Image} returns 
	 * <code>net.sourceforge.jiu.data.Gray8Image.class</code>.
	 */
	Class getImageType();

	/**
	 * Returns the number of channels in this image.
	 * Must be one or larger.
	 * @return the number of channels
	 */
	int getNumChannels();

	/**
	 * Returns the horizontal resolution of the image in pixels.
	 * Must be one or larger.
	 * @return width in pixels
	 */
	int getWidth();
}
