/*
 * MemoryGray8Image
 *
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryByteChannelImage;
import net.sourceforge.jiu.data.PixelImage;

/**
 * An implementation of {@link Gray8Image} that keeps the complete image in memory.
 * This class inherits most of its functionality from its parent class
 * {@link MemoryByteChannelImage}, using one byte channel.
 *
 * @author Marco Schmidt
 */
public class MemoryGray8Image extends MemoryByteChannelImage implements Gray8Image
{
	/**
	 * Creates a new MemoryGray8Image object with the specified resolution.
	 * Simply gives <code>1</code> (for one channel) and the two resolution arguments
	 * to the super constructor (of the parent class {@link MemoryByteChannelImage}).
	 * @param width the horizontal resolution, must be non-zero and positive
	 * @param height the vertical resolution, must be non-zero and positive
	 */
	public MemoryGray8Image(int width, int height)
	{
		super(1, width, height);
	}

	public PixelImage createCompatibleImage(int width, int height)
	{
		return new MemoryGray8Image(width, height);
	}

	public Class getImageType()
	{
		return Gray8Image.class;
	}

	public boolean isBlack(int x, int y)
	{
		return getByteSample(x, y) == 0;
	}

	public boolean isWhite(int x, int y)
	{
		return getByteSample(x, y) == (byte)255;
	}

	public void putBlack(int x, int y)
	{
		putSample(x, y, 0);
	}

	public void putWhite(int x, int y)
	{
		putSample(x, y, 255);
	}
}
