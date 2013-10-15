/*
 * MemoryRGB48Image
 *
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.MemoryShortChannelImage;
import net.sourceforge.jiu.data.RGB48Image;

/**
 * A class to store 48 bit RGB truecolor images in memory.
 * @author Marco Schmidt
 * @since 0.12.0
 * @see RGB24Image
 */
public class MemoryRGB48Image extends MemoryShortChannelImage implements RGB48Image
{
	/**
	 * Creates a new object of this class, with width and height as
	 * specified by the arguments.
	 * @param width the horizontal resolution of the new image in pixels
	 * @param height the vertical resolution of the new image in pixels
	 */
	public MemoryRGB48Image(int width, int height)
	{
		super(3, width, height);
	}

	public PixelImage createCompatibleImage(int width, int height)
	{
		return new MemoryRGB48Image(width, height);
	}

	public Class getImageType()
	{
		return RGB48Image.class;
	}
}
