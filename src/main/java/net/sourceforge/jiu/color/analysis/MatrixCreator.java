/*
 * MatrixCreator
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.analysis;

import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.color.data.CoOccurrenceMatrix;
import net.sourceforge.jiu.color.data.CoOccurrenceFrequencyMatrix;
import net.sourceforge.jiu.color.data.MemoryCoOccurrenceMatrix;
import net.sourceforge.jiu.color.data.MemoryCoOccurrenceFrequencyMatrix;

/**
 * This class creates and initializes co-occurrence matrices and co-occurrence 
 * frequency matrices.
 * @author Marco Schmidt
 */
public class MatrixCreator
{
	private MatrixCreator()
	{
		// to prevent instantiation
	}

	/**
	 * Create a co-occurrence matrix for a 16 bit grayscale image.
	 * @param image the image for which the matrix is created
	 * @return the resulting matrix
	 * @since 0.15.0
	 */
	public static CoOccurrenceMatrix createCoOccurrenceMatrix(Gray16Image image)
	{
		return createCoOccurrenceMatrix(image, 0);
	}

	public static CoOccurrenceMatrix createCoOccurrenceMatrix(Gray8Image image)
	{
		return createCoOccurrenceMatrix(image, 0);
	}

	public static CoOccurrenceMatrix createCoOccurrenceMatrix(Paletted8Image image)
	{
		return createCoOccurrenceMatrix(image, 0);
	}

	public static CoOccurrenceMatrix createCoOccurrenceMatrix(IntegerImage image, int channelIndex)
	{
		if (image == null)
		{
			throw new IllegalArgumentException("The image must non-null.");
		}
		if (channelIndex < 0)
		{
			throw new IllegalArgumentException("The channel index must be zero or larger.");
		}
		if (channelIndex >= image.getNumChannels())
		{
			throw new IllegalArgumentException("The channel index must be smaller than the number of channels in the image (" + image.getNumChannels() + ").");
		}
		int dim = image.getMaxSample(channelIndex) + 1;
		MemoryCoOccurrenceMatrix matrix = new MemoryCoOccurrenceMatrix(dim);
		initCoOccurrenceMatrix(image, channelIndex, matrix);
		return matrix;
	}

	/** 
	 * Initializes a co-occurrence matrix from the input image, using the <em>direct</em>
	 * four neighbor pixels.
	 * The number of entries in the palette of the argument image must be equal to the dimension
	 * of the argument matrix.
	 * @param image the image that will be used to initialize the matrix
	 * @param matrix the matrix that will first be cleared and then initialized from the image
	 * @throws IllegalArgumentException if at least one of the arguments is null or if the
	 *  palette size is not equal to the matrix dimension
	 */
	public static void initCoOccurrenceMatrix(IntegerImage image, int channelIndex, CoOccurrenceMatrix matrix)
	{
		if (image == null)
		{
			throw new IllegalArgumentException("The image must non-null.");
		}
		if (channelIndex < 0)
		{
			throw new IllegalArgumentException("The channel index must be zero or larger.");
		}
		if (channelIndex >= image.getNumChannels())
		{
			throw new IllegalArgumentException("The channel index must be smaller than the number of channels in the image (" + image.getNumChannels() + ").");
		}
		int dim = image.getMaxSample(channelIndex) + 1;
		if (matrix == null)
		{
			throw new IllegalArgumentException("The matrix must non-null.");
		}
		if (matrix.getDimension() != dim)
		{
			throw new IllegalArgumentException("Dimension of matrix (" + 
				matrix.getDimension() + " must be exactly one larger than " +
				"maximum sample value (" + (dim - 1) + ").");
		}
		matrix.clear();
		int maxX = image.getWidth() - 1;
		int maxY = image.getHeight() - 1;
		for (int y = 0; y <= maxY; y++)
		{
			for (int x = 0; x <= maxX; x++)
			{
				int index = image.getSample(channelIndex, x, y);
				if (x > 0)
				{
					int leftNeighbor = image.getSample(channelIndex, x - 1, y);
					matrix.incValue(index, leftNeighbor);
				}
				if (x < maxX)
				{
					int rightNeighbor = image.getSample(channelIndex, x + 1, y);
					matrix.incValue(index, rightNeighbor);
				}
				if (y > 0)
				{
					int topNeighbor = image.getSample(channelIndex, x, y - 1);
					matrix.incValue(index, topNeighbor);
				}
				if (y < maxY)
				{
					int bottomNeighbor = image.getSample(channelIndex, x, y + 1);
					matrix.incValue(index, bottomNeighbor);
				}
			}
		}
	}

	/**
	 * Creates a new co-occurrence frequency with the same dimension as the argument co-occurrence
	 * matrix, calls {@link #initCoOccurrenceFrequencyMatrix} with them to initialize the new matrix,
	 * then returns it.
	 * A {@link MemoryCoOccurrenceFrequencyMatrix} is created.
	 * @param A the co-occurrence matrix from which the resulting matrix will be initialized
	 * @return the newly-created co-occurrence frequency matrix
	 * @throws IllegalArgumentException if the argument matrix is null 
	 */
	public static CoOccurrenceFrequencyMatrix createCoOccurrenceFrequencyMatrix(final CoOccurrenceMatrix A)
	{
		if (A == null)
		{
			throw new IllegalArgumentException("Matrix argument must be non-null.");
		}
		int dimension = A.getDimension();
		MemoryCoOccurrenceFrequencyMatrix matrix = new MemoryCoOccurrenceFrequencyMatrix(dimension);
		initCoOccurrenceFrequencyMatrix(A, matrix);
		return matrix;
	}

	/**
 	 * Initializes a co-occurrence frequency matrix from a co-occurrence matrix.
	 * The two argument matrices must be non-null and have the same dimension.
	 * @param A co-occurrence matrix used as input
	 * @param cofm co-occurrence matrix, will be initialized by this method
	 * @throws IllegalArgumentException if either matrix is null or if the dimensions are not equal 
	 */
	public static void initCoOccurrenceFrequencyMatrix(final CoOccurrenceMatrix A, CoOccurrenceFrequencyMatrix cofm)
	{
		if (A == null)
		{
			throw new IllegalArgumentException("Co-occurrence matrix A argument must not be null.");
		}
		if (cofm == null)
		{
			throw new IllegalArgumentException("Co-occurrence frequency matrix cofm argument must not be null.");
		}
		final int DIMENSION = A.getDimension();
		if (DIMENSION != cofm.getDimension())
		{
			throw new IllegalArgumentException("Dimension of matrices A (" +
				DIMENSION + ") and cofm (" + cofm.getDimension() + ") must " +
				"be equal.");
		}
		cofm.clear();
		double totalSum = 0.0;
		for (int i = 0; i < DIMENSION; i++)
		{
			// first sum up A[i, k], k = 0..dimension-1
			double sum = 0.0;
			for (int k = 0; k < DIMENSION; k++)
			{
				sum += A.getValue(i, k);
			}
			totalSum += sum;
			for (int j = 0; j < DIMENSION; j++)
			{
				double value = A.getValue(i, j);
				double result;
				if (sum == 0.0)
				{
					result = 0.0;
				}
				else
				{
					result = value / sum;
				}
				cofm.setValue(i, j, result);
			}
		}
		cofm.computeStatistics();
	}
}
