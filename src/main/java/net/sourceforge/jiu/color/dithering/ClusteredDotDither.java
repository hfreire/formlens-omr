/*
 * ClusteredDotDither
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.dithering;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;
import net.sourceforge.jiu.util.ComparatorInterface;
import net.sourceforge.jiu.util.Sort;

/**
 * Apply a clustered dot ordered dither to a grayscale image, converting
 * it to a bilevel image in the process.
 * Works with {@link net.sourceforge.jiu.data.GrayIntegerImage} objects
 * as input and {@link net.sourceforge.jiu.data.BilevelImage} objects
 * as output.
 * Resolution of both must be the same.
 * Use one of the predefined dither matrices or have one compiled from
 * a <em>spot function</em> (given by an object of a class implementing
 * {@link net.sourceforge.jiu.color.dithering.SpotFunction}).
 * There are a couple of classes implementing that spot function interface
 * in the dithering package.
 * If no matrix is specified, {@link #process()} creates a default matrix
 * by calling {@link #setOrder3DitherMatrix()}.
 * <h3>Usage example</h3>
 * <pre>
 * ClusteredDotDither dither = new ClusteredDotDither();
 * dither.setInputImage(image); // some GrayIntegerImage
 * dither.setDitherMatrix(8, 8, new DiamondSpotFunction());
 * dither.process();
 * PixelImage ditheredImage = dither.getOutputImage();
 * </pre>
 *
 * <h3>Credits</h3>
 * The predefined dither matrices were taken from
 * <a target="_top" href="http://netpbm.sourceforge.net">Netpbm</a>'s 
 * <code>pgmtopbm</code> program (the matrices are stored in the
 * file <code>dithers.h</code>).
 * <p>
 * I learned about spot functions and their use from Austin Donnelly's 
 * GIMP plugin <em>newsprint</em> - it has extensive comments, and the
 * <a target="_top" href="http://www.cl.cam.ac.uk/~and1000/newsprint/">newsprint website</a> 
 * explains the 
 * <a target="_top" href="http://www.cl.cam.ac.uk/~and1000/newsprint/clustered-dot.html">theoretical background</a>.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class ClusteredDotDither extends ImageToImageOperation
{
	private int ditherHeight;
	private int ditherWidth;
	private int[] ditherData;

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		if (ditherData == null)
		{
			setDefaults();
		}
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		if (!(in instanceof GrayIntegerImage))
		{
			throw new WrongParameterException("Input image must implement GrayIntegerImage.");
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			out = new MemoryBilevelImage(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		else
		{
			if (!(out instanceof BilevelImage))
			{
				throw new WrongParameterException("Output image must implement BilevelImage.");
			}
			ensureOutputImageResolution(in.getWidth(), in.getHeight());
		}
		process((GrayIntegerImage)in, (BilevelImage)out);
	}

	private void process(GrayIntegerImage in, BilevelImage out)
	{
		// find maximum entry in dither matrix
		int maxTableValue = 1;
		for (int i = 0; i < ditherData.length; i++)
		{
			if (ditherData[i] > maxTableValue)
			{
				maxTableValue = ditherData[i];
			}
		}
		maxTableValue++;

		// create adjusted dither matrix data
		final int MAX_SAMPLE = in.getMaxSample(0) + 1;
		final int[] data = new int[ditherData.length];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = ditherData[i] * MAX_SAMPLE / maxTableValue;
		}

		// do the actual dithering
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		for (int y = 0; y < HEIGHT; y++)
		{
			int ditherOffset = (y % ditherHeight) * ditherWidth;
			int samplesLeft = ditherWidth;
			for (int x = 0; x < WIDTH; x++)
			{
				if (in.getSample(0, x, y) >= data[ditherOffset++])
				{
					out.putWhite(x, y);
				}
				else
				{
					out.putBlack(x, y);
				}
				if (--samplesLeft == 0)
				{
					samplesLeft = ditherWidth;
					ditherOffset -= ditherWidth;
				}
			}
			setProgress(y, HEIGHT);
		}
	}

	private void setDefaults()
	{
		setOrder3DitherMatrix();
	}

	/**
	 * Sets the dither matrix to be used in this operation.
	 * @param width number of entries in horizontal direction
	 * @param height number of entries in vertical direction
 	 * @param data matrix entries, in order top to bottom, and in each row left to right
 	 * @throws IllegalArgumentException if width or height are smaller than one or data
 	 *  is <code>null</code> or has not at least width times height entries
	 */
	public void setDitherMatrix(int width, int height, int[] data)
	{
		if (width < 1)
		{
			throw new IllegalArgumentException("Width must be one or larger.");
		}
		if (height < 1)
		{
			throw new IllegalArgumentException("Height must be one or larger.");
		}
		if (data == null)
		{
			throw new IllegalArgumentException("Data must not be null.");
		}
		if (data.length < width * height)
		{
			throw new IllegalArgumentException("Data must have at least width times height entries.");
		}
		ditherWidth =  width;
		ditherHeight = height;
		ditherData = data;
	}

	/**
	 * Creates and sets a dither matrix of user-defined size and 
	 * compiles it from a spot function.
	 * Creates a matrix from the spot function and calls {@link #setDitherMatrix(int, int, int[])}.
	 * @param width width of matrix, must be one or larger
	 * @param height height of matrix, must be one or larger
	 * @param f the spot function to be used for compiling the matrix
	 */
	public void setDitherMatrix(int width, int height, SpotFunction f)
	{
		class MatrixElement implements ComparatorInterface
		{
			int index;
			double value;
			public int compare(Object o1, Object o2)
			{
				MatrixElement e1 = (MatrixElement)o1;
				MatrixElement e2 = (MatrixElement)o2;
				if (e1.value < e2.value)
				{
					return -1;
				}
				else
				if (e1.value == e2.value)
				{
					return 0;
				}
				else
				{
					return 1;
				}
			}
		}
		int[] data = new int[width * height];
		MatrixElement[] matrixElements = new MatrixElement[data.length];
		for (int i = 0; i < data.length;  i++)
		{
			matrixElements[i] = new MatrixElement();
			matrixElements[i].index = i;
		}
		int index = 0;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				double sx = ((double)x / (width - 1) - 0.5) * 2;
				double sy = ((double)y / (height - 1) - 0.5) * 2;
				double value = f.compute(sx, sy);
				if (value < -1.0)
				{
					value = -1.0;
				}
				else
				if (value > 1.0)
				{
					value = 1.0;
				}
				matrixElements[index++].value = value;
			}
		}
		boolean balanced = f.isBalanced();
		if (!balanced)
		{
			Sort.sort(matrixElements, matrixElements[0]);
		}
		for (int i = 0; i < data.length; i++)
		{
			MatrixElement elem = matrixElements[i];
			if (balanced)
			{
				data[elem.index] = (int)(elem.value * 254);
			}
			else
			{
				data[elem.index] = i * 255 / data.length;
			}
		}
		setDitherMatrix(width, height, data);
	}

	/** 
	 * Sets a 6 times 6 elements matrix to be used for dithering.
	 */
	public void setOrder3DitherMatrix()
	{
		setDitherMatrix(6, 6, new int[]
			{ 9, 11, 10,  8,  6,  7,
			 12, 17, 16,  5,  0,  1,
			 13, 14, 15,  4,  3,  2,
			  8,  6,  7,  9, 11, 10,
			  5,  0,  1, 12, 17, 16,
			  4,  3,  2, 13, 14, 15});
	}

	/** 
	 * Sets an 8 times 8 elements matrix to be used for dithering.
	 */
	public void setOrder4DitherMatrix()
	{
		setDitherMatrix(8, 8, new int[]
			{18,20,19,16,13,11,12,15,
			27,28,29,22, 4, 3, 2, 9,
			26,31,30,21, 5, 0, 1,10,
			23,25,24,17, 8, 6, 7,14,
			13,11,12,15,18,20,19,16,
			 4, 3, 2, 9,27,28,29,22,
			 5, 0, 1,10,26,31,30,21,
			 8, 6, 7,14,23,25,24,17});
	}

	/** 
	 * Sets a 16 times 16 elements matrix to be used for dithering.
	 */
	public void setOrder8DitherMatrix()
	{
		setDitherMatrix(16, 16, new int[] {
			 64, 69, 77, 87, 86, 76, 68, 67, 63, 58, 50, 40, 41, 51, 59, 60,
			 70, 94,100,109,108, 99, 93, 75, 57, 33, 27, 18, 19, 28, 34, 52,
			 78,101,114,116,115,112, 98, 83, 49, 26, 13, 11, 12, 15, 29, 44,
			 88,110,123,124,125,118,107, 85, 39, 17,  4,  3,  2,  9, 20, 42,
			 89,111,122,127,126,117,106, 84, 38, 16,  5,  0,  1, 10, 21, 43,
			 79,102,119,121,120,113, 97, 82, 48, 25,  8,  6,  7, 14, 30, 45,
			 71, 95,103,104,105, 96, 92, 74, 56, 32, 24, 23, 22, 31, 35, 53,
			 65, 72, 80, 90, 91, 81, 73, 66, 62, 55, 47, 37, 36, 46, 54, 61,
			 63, 58, 50, 40, 41, 51, 59, 60, 64, 69, 77, 87, 86, 76, 68, 67,
			 57, 33, 27, 18, 19, 28, 34, 52, 70, 94,100,109,108, 99, 93, 75,
			 49, 26, 13, 11, 12, 15, 29, 44, 78,101,114,116,115,112, 98, 83,
			 39, 17,  4,  3,  2,  9, 20, 42, 88,110,123,124,125,118,107, 85,
			 38, 16,  5,  0,  1, 10, 21, 43, 89,111,122,127,126,117,106, 84,
			 48, 25,  8,  6,  7, 14, 30, 45, 79,102,119,121,120,113, 97, 82,
			 56, 32, 24, 23, 22, 31, 35, 53, 71, 95,103,104,105, 96, 92, 74,
 			 62, 55, 47, 37, 36, 46, 54, 61, 65, 72, 80, 90, 91, 81, 73, 66});
	}
}
