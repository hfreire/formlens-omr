/*
 * ArrayScaling
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

import java.lang.IllegalArgumentException;

/**
 * This class currently only scales up an image given as a one-dimensional array
 * of values.
 * <p>
 * <em>Note: This class should be adjusted if Java ever supports genericity.
 * It could then work on all kinds of arrays.</em>
 *
 * @author Marco Schmidt
 */
public class ArrayScaling
{
	private ArrayScaling()
	{
	}

	/**
	 * Scales up the argument image by factor 2 in both directions.
	 * It is assumed that the first <code>width</code> times
	 * <code>height</code> values of <code>data</code> contain an image 
	 * (or image channel).
	 * The pixels (or samples) are assumed to be laid out rows top-to-bottom,
	 * within each row left-to-right.
	 * It is further assumed that the length of the <code>data</code> array is
	 * at least 4 times <code>width</code> times <code>height</code>.
	 * This method scales up the image in <code>data</code> so that after the call to this
	 * method <code>data</code> can be treated as an image (a channel) that has a horizontal
     * resolution of <code>width * 2</code> and a vertical resolution of 
     * <code>height * 2</code>.
     *
	 * @param data the array of pixels that form the image to be flipped
	 * @param width the horizontal resolution of the image; must be larger than 0
	 * @param height the vertical resolution of the image; must be larger than 0
	 * @exception IllegalArgumentException if the arguments are invalid
	 */
	public static final void scaleUp200Percent(byte[] data, int width, int height)
		throws IllegalArgumentException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("Error -- data must be non-null.");
		}
		if (width < 1 || height < 1)
		{
			throw new IllegalArgumentException("Error -- both width and " +
				"height must be larger than zero (width=" + width +
				", height=" + height + ").");
		}
		if (width * height * 4 > data.length)
		{
			throw new IllegalArgumentException("Error -- data array must hold " +
				"at least width times height times 4 values.");
		}
		int newWidth = width * 2;
		int newHeight = height * 2;
		// (1) scale up each row in horizontal direction and copy it to its destination
		//     at the same time
		int y1 = height - 1;
		int y2 = newHeight - 1;
		while (y1 >= 0)
		{
			int x = width - 1;
			int offset1 = (y1 + 1) * width - 1;
			int offset2 = (y2 + 1) * newWidth - 1;
			while (x > 0)
			{
				int v1 = data[offset1--] & 0xff;
				int v2 = data[offset1] & 0xff;
				data[offset2--] = (byte)v1;
				data[offset2--] = (byte)((v1 + v2) >> 1);
				x--;
			}
			byte v = data[offset1];
			data[offset2--] = v;
			data[offset2] = v;
			y1--;
			y2 -= 2;
		}
		// (2) take two already-copied rows from scaled image and
		//     interpolate the row between them
		int y = newHeight - 1;
		while (y > 1)
		{
			int offset1 = (y - 2) * newWidth;
			int offset2 = offset1 + newWidth;
			int offset3 = offset2 + newWidth;
			for (int x = 0; x < newWidth; x++)
			{
				int v1 = data[offset1++] & 0xff;
				int v2 = data[offset3++] & 0xff;
				data[offset2++] = (byte)((v1 + v2) >> 1);
			}
			y -= 2;
		}
		// (3) copy second row of scaled image to first row
		int x1 = 0;
		int x2 = newWidth;
		while (x1 < newWidth)
		{
			data[x1++] = data[x2++];
		}
	}
}
