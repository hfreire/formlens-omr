/*
 * ArrayRotation
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

/**
 * Provides static methods to rotate (in steps of 90 degrees), flip and mirror array elements.
 * The image data is expected to be available as an array of integer values, being stored as rows top-to-bottom.
 * Within each row, the data is laid out from left to right.
 * This class may also been useful for transposing matrices.
 * <p>
 * The rotation by 90 and 270 degrees in-place (i.e., without using a 
 * second array to copy to) is based on ideas and code developed
 * by others.
 * See <a target="_top" href="http://facweb.cs.depaul.edu/tchristopher/rotates.htm">Rotation of arrays</a>
 * by Thomas W. Christopher.
 * <p>
 * I also got very useful advice from Hans-Bernhard Broeker and others in 
 * <a href="news:comp.graphics.algorithms">comp.graphics.algorithms</a>.
 * There is a thread titled <em>In-place rotation of pixel images</em> starting Oct 11, 2000.
 * <p>
 * <em>Note: This class should be adjusted if Java ever supports genericity.
 * Then rotation functionality could be provided for all kinds of arrays.</em>
 * @author Hans-Bernhard Broeker
 * @author Thomas W.&nbsp;Christopher
 * @author Marco Schmidt
 */
public class ArrayRotation
{
	private ArrayRotation()
	{
	}

	/**
	 * This method checks several properties of the arguments.
	 * If any of the properties is not fulfilled, an explaining 
	 * {@link java.lang.IllegalArgumentException} is thrown.
	 * Otherwise, nothing happens.
	 * This method is supposed to be called at the beginning of several
	 * other methods in this class.
	 * Properties checked:
	 * <ul>
	 * <li><code>pixels</code> is non-null</li>
	 * <li><code>width</code> and <code>height</code> are larger than zero</li>
	 * <li>number of elements in <code>pixels</code> is at least <code>width</code> times <code>height</code></li>
	 * </ul>
	 */
	public static void checkPixelArray(int[] pixels, int width, int height)
	{
		if (pixels == null)
		{
			throw new IllegalArgumentException("Error -- the pixel array must be initialized.");
		}
		if (width < 1 || height < 1)
		{
			throw new IllegalArgumentException("Error -- width and height must be >= 1 (width=" + 
				width + ", height=" + height + ").");
		}
		if (pixels.length < width * height)
		{
			throw new IllegalArgumentException("Error -- pixel array must have at least width * height pixels, has only " + 
				pixels.length + " pixels.");
		}
	}

	/**
	 * Flips the argument image, i.e., the top line becomes the bottom line
	 * and vice versa, etc.
	 * This method first checks the validity of the arguments that define the image
	 * by a call to {@link checkPixelArray}.
	 * Then the image data is flipped in place, no additional memory is required.
	 * Note that after applying this operation twice you will get the original
	 * image back.
	 *
	 * @param pixels the array of pixels that form the image to be flipped
	 * @param width the horizontal resolution of the image; must be larger than 0
	 * @param height the vertical resolution of the image; must be larger than 0
	 * @exception IllegalArgumentException if the arguments are invalid
	 */
	private static final void flipInPlace(int[] pixels, int width, int height)
	{
		checkPixelArray(pixels, width, height);
		int y1 = 0;
		int y2 = height - 1;
		while (y1 < y2)
		{
			int offset1 = y1 * width;
			int offset2 = y2 * width;
			for (int x = 0; x < width; x++)
			{
				int temp = pixels[offset1];
				pixels[offset1++] = pixels[offset2];
				pixels[offset2++] = temp;
			}
		}
	}

	private static final int[] flip(int[] pixels, int width, int height)
	{
		checkPixelArray(pixels, width, height);
		int[] result = new int[width * height];
		for (int y1 = 0, y2 = height - 1; y1 < height; y1++, y2--)
		{
			int offset1 = y1 * width;
			int offset2 = y2 * width;
			for (int x = 0; x < width; x++)
			{
				result[offset2++] = pixels[offset1++];
			}
		}
		return result;
	}

	/**
	 * Flips the image given by the arguments.
	 * The inPlace argument determines if the pixels array is modified or not.
	 * If inPlace is true, no additional array is allocated.
	 * Otherwise, an array of width times height items is allocated and the 
	 * flipped image will be stored in this array.
	 * @param inPlace if <code>true</code> all work is done on the <code>pixels</code> array;
	 *                otherwise, a second array is allocated and the <code>pixels</code> array
	 *                remains unmodified
	 * @param pixels the array of pixels that form the image to be flipped
	 * @param width the horizontal resolution of the image; must be larger than 0
	 * @param height the vertical resolution of the image; must be larger than 0
	 * @return the flipped image as int array; equals <code>pixels</code> if 
	 * <code>inPlace</code> is true
	 * @exception IllegalArgumentException if the pixel resolution 
	 * is invalid or the pixels array is not initialized or its length smaller
	 * than <code>width</code> times <code>height</code>
	 */
	public static final int[] flip(boolean inPlace, int[] pixels, int width, int height)
	{
		if (inPlace)
		{
			flipInPlace(pixels, width, height);
			return pixels;
		}
		else
		{
			return flip(pixels, width, height);
		}
	}

	/**
	 * Mirrors the image given by the arguments.
	 * For each row, pixels are swapped, leftmost and rightmost,
	 * second-leftmost and second-rightmost, and so on.
	 * The inPlace argument determines if the pixels array is modified or not.
	 * If inPlace is true, no additional array is used.
	 * Otherwise, an array of width times height items is allocated and the 
	 * mirrored image will be stored in this array.
	 * @param inPlace if <code>true</code> all work is done on the <code>pixels</code> array;
	 *                otherwise, a second array is allocated and the <code>pixels</code> array
	 *                remains unmodified
	 * @param pixels the array of pixels that form the image to be flipped
	 * @param width the horizontal resolution of the image; must be larger than 0
	 * @param height the vertical resolution of the image; must be larger than 0
	 * @return the flipped image as int array; equals <code>pixels</code> if 
	 * <code>inPlace</code> is true
	 * @exception IllegalArgumentException if the pixel resolution 
	 * is invalid or the pixels array is not initialized or its length smaller
	 * than <code>width</code> times <code>height</code>
	 */
	/*public static int[] mirror(boolean inPlace, int[] pixels, int width, int height)
	{
		if (inPlace)
		{
			//flipInPlace(pixels, width, height);
			return pixels;
		}
		else
		{
			return pixels;//flip(pixels, width, height);
		}
	}*/

	/**
	 * Rotates the argument image by 180 degrees.
	 * The resulting image will have exactly the same pixel resolution.
	 * Note that this operation is the same as two consecutive 90 degree
	 * rotations in the same direction.
	 * Another way of implementing a 180 degree rotation is first flipping
	 * and then mirroring the original image (or vice versa).
	 * <p>
	 * If <code>inPlace</code> is true, the rotation is done on the 
	 * argument <code>pixels</code> array.
	 * Otherwise a new array of sufficient length is allocated and the 
	 * rotated image will be stored in this new array, not modifying the 
	 * content of the <code>pixels</code> array.
	 * <p>
	 * @param inPlace determines whether the rotated image is written to the argument array
	 * @param pixels the array of pixels that form the image to be rotated
	 * @param width the horizontal resolution of the image; must be larger than 0
	 * @param height the vertical resolution of the image; must be larger than 0
	 * @return the flipped image as int array; equals <code>pixels</code> if 
	 * <code>inPlace</code> is true
	 * @exception IllegalArgumentException if the pixel resolution 
	 * is invalid or the pixels array is not initialized or its length smaller
	 * than <code>width</code> times <code>height</code>
	 */
	public static int[] rotate180(boolean inPlace, int[] pixels, int width, int height)
	{
		if (inPlace)
		{
			rotateInPlace180(pixels, width, height);
			return pixels;
		}
		else
		{
			return pixels;//rotateToCopy180(pixels, width, height);
		}
	}

	/*private static int[] rotate180(int[] pixels, int width, int height)
	{
		checkPixelArray(pixels, width, height);
		int numPixels = width * height;
		int[] result = new int[numPixels];
		int x1 = 0;
		int x2 = numPixels - 1;
		while (x1 < x2)
		{
			int temp = pixels[x1];
			pixels[x1++] = pixels[x2];
			pixels[x2--] = temp;
		}
		return result;
	}*/

	private static void rotateInPlace180(int[] pixels, int width, int height)
	{
		checkPixelArray(pixels, width, height);
		int x1 = 0;
		int x2 = width * height - 1;
		while (x1 < x2)
		{
			int temp = pixels[x1];
			pixels[x1++] = pixels[x2];
			pixels[x2--] = temp;
		}
	}

	/*private static void rotateToArray90Left(int[] src, int[] dest, int width, int height)
	{
		checkPixelArray(src, width, height);
		checkPixelArray(dest, width, height);
		if (src == dest)
		{
			throw new IllegalArgumentException("rotate90Left assumes that the argument arrays are not the same.");
		}
		int x_ = -1;
		int y_ = -1;
		try
		{
			int offset = 0;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				x_ = x;
				y_ = y;
				dest[(width - x - 1) * height + y] = src[offset++];
			}
		}
		}
		catch (ArrayIndexOutOfBoundsException ae)
		{
			System.out.println("bounds; x=" + x_ + " y=" + y_);
		}
	}*/

	/*private static int pred(int k, int width, int height)
	{
		return (k % height) * width + k / height;
	}

	private static void rotateInPlace90Left(int[] pixels, int width, int height)
	{
		int LxM = height * width;
		int i, j, k, stillToMove;
		for (i = 0, stillToMove = LxM; stillToMove > 0; i++)
		{
			for (j = pred(i, width, height); j > i; j = pred(j, width, height))
    			;
    		if (j < i)
    		{
    			continue;
    		}
		    for (k = i, j = pred(i, width, height); j != i; k = j,
		    	j = pred(j, width, height))
		    {
        		//exchange(k,j);
        		int temp = pixels[k];
        		pixels[k] = pixels[j];
        		pixels[j] = temp;
        		--stillToMove;
    		}
    		--stillToMove;
		}
	}*/

	public static void rotate90Left(int width, int height, byte[] src, int srcOffset, byte[] dest, int destOffset)
	{
		int offset = srcOffset;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				dest[destOffset + (width - x - 1) * height + y] = src[offset++];
			}
		}
	}

	public static void rotate90Right(int width, int height, byte[] src, int srcOffset, byte[] dest, int destOffset)
	{
		int offset = srcOffset;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				dest[destOffset + x * height + (height - 1 - y)] = src[offset++];
			}
		}
	}

	public static void rotate180(int width, int height, byte[] src, int srcOffset, byte[] dest, int destOffset)
	{
		int n = width * height;
		destOffset = destOffset + n - 1;
		while (n-- > 0)
		{
			dest[destOffset--] = src[srcOffset++];
		}
	}

	/*	public static int[] rotate90Left(boolean inPlace, int[] pixels, int width,
		int height) throws IllegalArgumentException
	{
		if (inPlace)
		{
			rotateInPlace90Left(pixels, width, height);
			return pixels;
		}
		else
		{
			int[] dest = new int[width * height];
			rotateToArray90Left(pixels, dest, width, height);
			return dest;
		}
	}*/

	private static void rotateInPlace90Right(int[] pixels, int width, int height)
	{
		throw new IllegalArgumentException("This method not implemented yet.");
	}

	private static int[] rotate90Right(int[] pixels, int width, int height)
	{
		int[] result = new int[width * height];
		int offset = 0;
		for (int y = 0; y < height; y++)
		{
			int srcOffset = height - 1 - y;
			for (int x = 0; x < width; x++)
			{
				result[srcOffset] = pixels[offset++];
				srcOffset += height;
			}
		}
		return result;
	}

	public static int[] rotate90Right(boolean inPlace, int[] pixels, int width, int height)
	{
		if (inPlace)
		{
			rotateInPlace90Right(pixels, width, height);
			return pixels;
		}
		else
		{
			return rotate90Right(pixels, width, height);
		}
	}
}
