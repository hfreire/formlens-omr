/*
 * TextureAnalysis
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.analysis;

import net.sourceforge.jiu.color.data.CoOccurrenceMatrix;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.Operation;

/**
 * This class determines a number of properties for a given co-occurrence matrix.
 * The only input parameter is a mandatory co-occurrence matrix object
 * to be specified using {@link #setMatrix}.
 * Then {@link #process} must be called.
 * After that, the various properties can be retrieved using the 
 * corresponding get methods, e.g. {@link #getContrast}, 
 * {@link #getEnergy} etc.
 * <p>
 * Static convenience methods {@link #compute(CoOccurrenceMatrix)} and
 * {@link #compute(IntegerImage, int)} exist for simplified usage.
 * You may need to cast your {@link net.sourceforge.jiu.data.PixelImage} to
 * {@link net.sourceforge.jiu.data.IntegerImage} first.
 * <h2>Usage example</h2>
 * Use like that:
 * <pre>GrayIntegerImage img = ...;
TextureAnalysis op = TextureAnalysis.compute(img);
System.out.println("Entropy=" + op.getEntropy());</pre>
 * Use the compute method that takes a {@link net.sourceforge.jiu.color.data.CoOccurrenceFrequencyMatrix}
 * if you want to reuse a matrix object.   
 * <p>
 * <h2>Caveat: memory consumption</h2>
 * <p>
 * Will not work with 16 bit image objects because {@link net.sourceforge.jiu.color.data.MemoryCoOccurrenceMatrix}
 * is rather unsophisticated, trying to allocate 4 * 65536 * 65536 bytes (16 GB).
 * Solution is an implementation of {@link net.sourceforge.jiu.color.data.CoOccurrenceMatrix}
 * which creates element counters on demand.  
 * 
 * The following resources were helpful when creating this class:
 * <ul>
 * <li>Article <em>Suchen ohne Worte</em> by Henning M&uuml;ller in German computer magazine
 * c't <a target="_top" href="http://www.heise.de/ct/01/15/004/">15 / 2001</a>,
 * p. 162ff.</li>
 * <li><a target="_top" href="http://www.ucalgary.ca/~mhallbey/texture/texture_tutorial.html">GLCM
 * Texture: A Tutorial</a> by Mryka Hall-Beyer.</li>
 * <li><a target="_top" href="http://www.burrill.demon.co.uk/meddoc/tmnmri.html">Texture Mapping 
 * of Neurological Magnetic Resonance Images</a> by J.H.P. Burrill</li>
 * </ul>
 * @since 0.7.0
 *
 * @author Marco Schmidt
 */
public class TextureAnalysis extends Operation
{
	private CoOccurrenceMatrix matrix;
	private int contrast;
	private double correlation;
	private int dissimilarity;
	private int energy;
	private double entropy;
	private double homogeneity;
	private int sum;
	private boolean symmetry;

	/**
	 * Create a TextureAnalysis operation with the results computed from a 
	 * given matrix.
	 * @param matrix co-occurrence matrix to use for texture analysis
	 * @return TextureAnalysis object with all attributes computed
	 * @throws MissingParameterException if matrix is null
	 * @since 0.14.2
	 */
	public static TextureAnalysis compute(CoOccurrenceMatrix matrix) throws MissingParameterException
	{
		TextureAnalysis op = new TextureAnalysis();
		op.setMatrix(matrix);
		op.process();
		return op;
	}

	/**
	 * For one channel of the argument image,
	 * create a TextureAnalysis operation with all attributes
	 * @param image the IntegerImage for which the texture analysis is being done
	 * @param channelIndex zero-based index of channel to work on 
	 * @return TextureAnalysis object with all attributes computed
	 * @throws MissingParameterException if matrix is null
	 * @since 0.14.2
	 */
	public static TextureAnalysis compute(IntegerImage image, int channelIndex) throws MissingParameterException
	{
		CoOccurrenceMatrix matrix = MatrixCreator.createCoOccurrenceMatrix(image, channelIndex);
		return compute(matrix);
	}

	/**
	 * Returns the contrast value determined in {@link #process}.
	 * Also called <em>inertia</em>.
	 */
	public int getContrast()
	{
		return contrast;
	}

	/**
	 * Returns the correlation determined in {@link #process}.
	 */
	public double getCorrelation()
	{
		return correlation;
	}

	/**
	 * Returns the dissimilarity value determined in {@link #process}.
	 */
	public int getDissimilarity()
	{
		return dissimilarity;
	}

	/**
	 * Returns the energy value determined in {@link #process}.
	 */
	public int getEnergy()
	{
		return energy;
	}

	/**
	 * Returns the entropy value determined in {@link #process}.
	 */
	public double getEntropy()
	{
		return entropy;
	}

	/**
	 * Returns the homogeneity value determined in {@link #process}.
	 * Also called <em>inverse difference moment</em>.
	 */
	public double getHomogeneity()
	{
		return homogeneity;
	}

	/**
	 * Returns the sum of all entries in the matrix.
	 */
	public int getSum()
	{
		return sum;
	}

	public boolean isSymmetrical()
	{
		return symmetry;
	}

	/**
	 * Run over the input matrix and determine contrast, energy, entropy and homogeneity
	 * of that matrix.
	 * @throws MissingParameterException if no co-occurrence matrix was provided using
	 *  {@link #setMatrix}
	 */
	public void process() throws
		MissingParameterException
	{
		if (matrix == null)
		{
			throw new MissingParameterException("No input co-occurrence matrix was provided.");
		}
		final int DIMENSION = matrix.getDimension();
		int items = 0;
		final int TOTAL_ITEMS = DIMENSION * 3;
		// initialize mu_i and mu_j
		double[] muI = new double[DIMENSION];
		double[] muJ = new double[DIMENSION];
		for (int k = 0; k < DIMENSION; k++)
		{
			muI[k] = 0.0;
			muJ[k] = 0.0;
			for (int i = 0; i < DIMENSION; i++)
			{
				for (int j = 0; j < DIMENSION; j++)
				{
					int value = matrix.getValue(i, j);
					muI[k] += i * value;
					muJ[k] += j * value;
				}
			}
			setProgress(items++, TOTAL_ITEMS);
		}
		// initialize sigma_i and sigma_j
		double[] sigmaI = new double[DIMENSION];
		double[] sigmaJ = new double[DIMENSION];
		for (int k = 0; k < DIMENSION; k++)
		{
			sigmaI[k] = 0.0;
			sigmaJ[k] = 0.0;
			for (int i = 0; i < DIMENSION; i++)
			{
				for (int j = 0; j < DIMENSION; j++)
				{
					int value = matrix.getValue(i, j);
					double a = (i - muI[i]);
					sigmaI[k] += value * a * a;
					double b = (j - muJ[j]);
					sigmaJ[k] += value * b * b;
				}
			}
			setProgress(items++, TOTAL_ITEMS);
		}
		contrast = 0;
		dissimilarity = 0;
		energy = 0;
		entropy = 0;
		homogeneity = 0;
		sum = 0;
		symmetry = true;
		for (int i = 0; i < DIMENSION; i++)
		{
			for (int j = 0; j < DIMENSION; j++)
			{
				int value = matrix.getValue(i, j);
				symmetry = symmetry && value == matrix.getValue(j, i);
				sum += value;
				energy += value * value;
				int diffAbs = (i - j);
				if (diffAbs < 0)
				{
					diffAbs = - diffAbs;
				}
				dissimilarity += diffAbs * value;
				contrast += diffAbs * diffAbs * value;
				if (value != 0) // log not defined for 0
				{
					entropy += value * Math.log(value);
				}
				homogeneity += value / (1.0 + diffAbs);
				double a = sigmaI[i] * sigmaJ[j];
				if (a != 0.0)
				{
					correlation += (value * (i - muI[i]) * (j - muJ[j])) / Math.sqrt(a);
				}
			}
			setProgress(items++, TOTAL_ITEMS);
		}
	}

	/**
	 * Sets the matrix to be used by this operation to the argument value.
	 * @param m the matrix for which the various properties will be computed
	 */
	public void setMatrix(CoOccurrenceMatrix m)
	{
		matrix = m;
	}
}
