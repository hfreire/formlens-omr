/*
 * MemoryRGB24Image
 *
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.MemoryByteChannelImage;
import net.sourceforge.jiu.data.RGB24Image;

/**
 * A class to store 24 bit RGB truecolor images in memory.
 * @author Marco Schmidt
 * @see RGB24Image
 */
public class MemoryRGB24Image extends MemoryByteChannelImage implements RGB24Image
{
	/**
	 * Creates a new object of this class, with width and height as
	 * specified by the arguments.
	 * @param width the horizontal resolution of the new image in pixels
	 * @param height the vertical resolution of the new image in pixels
	 */
	public MemoryRGB24Image(int width, int height)
	{
		super(3, width, height);
	}

	public PixelImage createCompatibleImage(int width, int height)
	{
		return new MemoryRGB24Image(width, height);
	}

	public Class getImageType()
	{
		return RGB24Image.class;
	}
}
