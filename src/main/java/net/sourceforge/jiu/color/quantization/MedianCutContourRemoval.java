/*
 * MedianCutContourRemoval
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import java.util.Hashtable;
import java.util.Vector;
import net.sourceforge.jiu.codecs.BMPCodec;
import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.ImageLoader;
import net.sourceforge.jiu.color.analysis.MatrixCreator;
import net.sourceforge.jiu.color.analysis.MeanDifference;
import net.sourceforge.jiu.color.data.CoOccurrenceFrequencyMatrix;
import net.sourceforge.jiu.color.data.CoOccurrenceMatrix;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;
import net.sourceforge.jiu.util.ComparatorInterface;
import net.sourceforge.jiu.util.Sort;
import net.sourceforge.jiu.util.Statistics;

/**
 * A data structure for storing the index values of a pair of
 * contouring colors plus their respective self co-occurrence
 * frequency values.
 * @author Marco Schmidt
 * @see MedianCutContourRemoval
 */
class ContouringColorPair implements ComparatorInterface
{
	private int index1;
	private int index2;
	private double scof1;
	private double scof2;

	/**
	 * Creates a new object of this class.
	 */
	public ContouringColorPair()
	{
	}

	/**
	 * Creates a new object of this class.
	 * @param i1 palette index of first color
	 * @param i2 palette index of second color
	 * @param sf1 self co-occurrence frequency value of first color
	 * @param sf2 self co-occurrence frequency value of second color
	 */
	public ContouringColorPair(int i1, int i2, double sf1, double sf2)
	{
		index1 = i1;
		index2 = i2;
		scof1 = sf1;
		scof2 = sf2;
	}

	public int compare(Object o1, Object o2)
	{
		ContouringColorPair p1 = (ContouringColorPair)o1;
		ContouringColorPair p2 = (ContouringColorPair)o2;
		double sum1 = p1.scof1 + p1.scof2;
		double sum2 = p2.scof1 + p2.scof2;
		if (sum1 < sum2)
		{
			return -1;
		}
		else
		if (sum1 > sum2)
		{
			return 1;
		}
		else
		{
			return 0;
		} 
	}

	public int getColorIndex(boolean smaller)
	{
		if (smaller)
		{
			return scof1 < scof2 ? index1 : index2; 
		}
		else
		{
			return scof1 < scof2 ? index2 : index1;
		}
	}
}

/**
 * Performs the <em>Median Cut</em> color quantization algorithm in combination with
 * a contour removal algorithm.
 * <p>
 * Quantization is an operation that reduces the number of colors in
 * an image while trying to remain as close to the original image
 * as possible.
 * Standard Median Cut quantization is implemented in the  
 * {@link net.sourceforge.jiu.color.quantization.MedianCutQuantizer} 
 * class.
 * <p>
 * This class implements an algorithm that improves the standard
 * implementation.
 * It repeatedly calls the original quantizer and adjusts the palette
 * in order to reduce the amount of contouring errors.
 * <h3>Image types</h3>
 * This operation requires an {@link net.sourceforge.jiu.data.RGB24Image}
 * object as input and produces a {@link net.sourceforge.jiu.data.Paletted8Image}
 * as output.
 * <h3>Usage example</h3>
 * <pre>
 * RGB24Image inputImage = ...; // image to be processed, from a file etc. 
 * MedianCutQuantizer quantizer = new MedianCutQuantizer();
 * quantizer.setPaletteSize(256);
 * MedianCutContourRemoval removal = new MedianCutContourRemoval();
 * removal.setQuantizer(quantizer);
 * removal.setInputImage(inputImage);
 * removal.setTau(11.0);
 * removal.setNumPasses(3);
 * removal.process();
 * PixelImage outputImage = removal.getOutputImage();
 * </pre> 
 * <h3>Rationale - why an extension to Median Cut?</h3>
 * Quantization without dithering can lead to contouring (banding) in the output image.
 * The contours introduced that way are not only ugly but they may lead to erroneous
 * results when processing that quantized image.
 * Dithering, an alternative group of algorithms used in combination with quantizers
 * to improve output quality, leads to output which is more pleasant to the human eye.
 * However, it introduces noise that may not be acceptable when the output image
 * is to be further processed by image processing algorithms.
 * Instead, this algorithm attempts to adjust the palette found by the Median
 * Cut algorithm.
 * The adjustments aim at reducing the amount of contouring caused by a 
 * palette found in a previous Median Cut operation. 
 * <h3><a name="howitworks">How the contour removal algorithm works</a></h3>
 * <ul>
 * <li>
 *  First, a normal Median Cut quantization operation is performed.
 *  The class
 *  {@link net.sourceforge.jiu.color.quantization.MedianCutQuantizer} 
 *  is used for that purpose.
 *  This results in a palette and an output image that was mapped from 
 *  the original using the palette.
 * </li>
 * <li>
 *  Now a {@link net.sourceforge.jiu.color.data.CoOccurrenceFrequencyMatrix} is created
 *  from a {@link net.sourceforge.jiu.color.data.CoOccurrenceMatrix}, which is in
 *  turn created from the paletted image that was produced in the previous step.
 *  The co-occurrence frequency matrix stores how often a pixel value j is the 
 *  neighbor of pixel i in the image, in relation to all occurrences of i.
 *  The matrix stores this information as <code>double</code> values between 
 *  <code>0.0</code> and <code>1.0</code>.
 *  If the value is <code>0.6</code>, j makes 60 percent of all neighboring
 *  pixels of i.
 * </li>
 * <li>
 *  Using certain heuristics that take advantage of the above mentioned matrices, 
 *  colors are classified into three groups:
 *  <ul>
 *   <li>contouring color pairs which contribute significantly to the contouring,</li>
 *   <li>compressible color pairs, two colors which are similar to each other and not contouring, and</li>
 *   <li>all colors which are not part of one of the two color pair types described before.</li>
 *  </ul>
 *  The tau value which can be specified with {@link #setTau(double)} is a 
 *  distance value in RGB space (tau is adjusted for an RGB cube where each
 *  axis can take values from 0 to 255).
 *  It is used to define a threshold for similar colors.
 *  A pair of compressible colors may not differ by more than tau.
 *  It is guaranteed that no color can be both compressible and contouring. 
 * </li>
 * <li>
 *  Note that each palette color generated by the Median Cut algorithm represents a 
 *  cuboid part of the RGB color cube.
 *  For each pair of compressible colors, their corresponding cuboids are neighbors in the cube.
 *  Now a number N of palette entries is changed.
 *  That number N is either the number of compressible color pairs found, or the number
 *  of contouring color pairs found, whichever is smaller.
 *  If N equals zero, nothing can be done and the algorithm terminates. 
 * </li>
 * <li>
 *  Now N swap operations are performed on the palette.
 *  Two palette entries of a compressible color pair are merged to form one palette entry.
 *  Their colors are similar so it will not decrease image quality much.
 *  The newly freed palette entry is used to split one color of a contouring color pair
 *  into two, thus allowing to represent a gradient type section in the image with an
 *  additional color.
 * </li>
 * <li>
 *  The original truecolor image is mapped to the new, modified palette.
 *  This whole process can now be performed again with the modified palette.
 *  That's why this operation has a {@link #setNumPasses(int)} method.
 *  Usually, more than eight iterations do not make a difference. 
 * </li>
 * </ul>
 * For an in-depth description of the algorithm see the journal article mentioned in the
 * <em>Credits</em> section below.
 * <h3>Credits</h3>
 * The algorithm was developed by 
 * <a target="_top" href="http://www-2.cs.cmu.edu/afs/cs.cmu.edu/user/js/www/homepage.html">Jefferey 
 * Shufelt </a> and described in
 * his article
 * <em>Texture Analysis for Enhanced Color Image Quantization.</em> 
 * CVGIP: Graphical Model and Image Processing 59(3): 149-163 (1997).
 * @see MedianCutQuantizer
 * @author Marco Schmidt
 */
public class MedianCutContourRemoval extends ImageToImageOperation
{
	/**
	 * The default tau value, used if none is specified
	 * with {@link #setTau(double)}.
	 * Check the class documentation to find out more about
	 * the meaning of tau: {@link MedianCutContourRemoval}.
	 */
	public static final double DEFAULT_TAU = 12;

	/**
	 * The default number of passes, used if they are not specified
	 * with {@link #setNumPasses(int)}.
	 * Check the class documentation to find out more about
	 * the meaning of that number of passes: {@link MedianCutContourRemoval}.
	 */
	public static final int DEFAULT_NUM_PASSES = 8;

	private Vector compressibleNodes;
	private Vector contouringPairs;
	private MedianCutNode[] leaves;
	private double[] meanC;
	private double meanS;
	private int numPasses = DEFAULT_NUM_PASSES;
	private Palette palette;
	private MedianCutQuantizer quantizer;
	private double stdDevS;
	private double[] stdDevC;
	private double sumMeanStdDevS;
	private double tau = DEFAULT_TAU;

	private double computeDistance(int index1, int index2)
	{
		return RGBColor.computeDistance(
			palette.getSample(RGBIndex.INDEX_RED, index1),
			palette.getSample(RGBIndex.INDEX_GREEN, index1),
			palette.getSample(RGBIndex.INDEX_BLUE, index1),
			palette.getSample(RGBIndex.INDEX_RED, index2),
			palette.getSample(RGBIndex.INDEX_GREEN, index2),
			palette.getSample(RGBIndex.INDEX_BLUE, index2)
		);
	}

	/**
	 * Computes the mean and standard deviation (stddev) values and
	 * from the argument matrix and initializes the mean / stddev 
	 * fields of this class with them. 
	 * @param matrix
	 */
	private void computeStatistics(CoOccurrenceFrequencyMatrix matrix)
	{
		final int N = quantizer.getPaletteSize();
		double values[] = new double[N];
		// compute mean of self co-occurrence frequencies
		for (int i = 0; i < N; i++)
		{
			values[i] = matrix.getValue(i, i);
		}
		meanS = Statistics.computeMean(values);
		// compute mean of self co-occurrence frequencies
		stdDevS = Statistics.computeStandardDeviation(values, meanS);
		sumMeanStdDevS = meanS + stdDevS;
		// compute mean and standard deviation of co-occurrence frequencies
		meanC = new double[N];
		stdDevC = new double[N];
		for (int j = 0; j < N; j++)
		{
			for (int i = 0; i < N; i++)
			{
				values[i] = matrix.getValue(i, j);
			}
			meanC[j] = Statistics.computeMean(values);
			stdDevC[j] = Statistics.computeStandardDeviation(values, meanC[j]);
		}
	}

	/**
	 * Takes 
	 * @return
	 */
	private Vector createContouringIndexList()
	{
		Hashtable table = new Hashtable(contouringPairs.size() * 2);
		Vector indexes = new Vector();
		Object[] contouringPairArray = toArray(contouringPairs);
		Sort.sort(contouringPairArray, new ContouringColorPair());
		for (int i = contouringPairArray.length - 1; i >= 0; i--)
		{
			ContouringColorPair pair = (ContouringColorPair)contouringPairArray[i];
			Integer index = new Integer(pair.getColorIndex(false));
			if (table.get(index) == null)
			{
				table.put(index, index);
				indexes.addElement(index);
			}
			index = new Integer(pair.getColorIndex(true));
			if (table.get(index) == null)
			{
				table.put(index, index);
				indexes.addElement(index);
			}
		}
		return indexes;
	}

	private void findColorPairs(CoOccurrenceFrequencyMatrix matrix, final CoOccurrenceMatrix A)
	{
		compressibleNodes = new Vector();
		contouringPairs = new Vector();
		final int N = quantizer.getPaletteSize();
		for (int i = 0; i < N; i++)
		{
			final double SI = matrix.getValue(i); 
			for (int j = i + 1; j < N; j++)
			{
				final double SJ = matrix.getValue(j);
				if (SI > sumMeanStdDevS && SJ > sumMeanStdDevS)
				{
					// potential contouring pair
					if (matrix.getValue(i, j) > meanC[j] + stdDevC[j] &&
					    matrix.getValue(j, i) > meanC[i] + stdDevC[i] && 
					    computeDistance(i, j) <= tau)
					{
						contouringPairs.addElement(new ContouringColorPair(i, j, SI, SJ));
					}
				}
				else
				if (SI < meanS && SJ < meanS)
				{
					MedianCutNode parentI = leaves[i].getParentNode();					
					MedianCutNode parentJ = leaves[j].getParentNode();
					// potential compressible pair
					if (parentI == parentJ && A.getValue(i, j) == 0 && parentI.getNumColors() > 1)
					{
						System.out.println("compressible: " + i + "/" + j);
						compressibleNodes.addElement(parentI);
					}
				}
			}
		}
	}

	/**
	 * Small command line application that performs a contour removal
	 * on an image.
	 * The first and only argument must be the name of image file from
	 * which the image to be quantized is loaded.
	 * @param args program arguments; must have length one, the only argument being the input image file name
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		PixelImage inputImage = ImageLoader.load(args[0]);
		if (inputImage == null)
		{
			System.err.println("Could not load image from " + args[0]);
			return;
		}
		MedianCutQuantizer quantizer = new MedianCutQuantizer();
		quantizer.setPaletteSize(256);
		MedianCutContourRemoval removal = new MedianCutContourRemoval();
		removal.setQuantizer(quantizer);
		removal.setInputImage(inputImage);
		removal.process();
		BMPCodec codec = new BMPCodec();
		codec.setImage(removal.getOutputImage());
		codec.setFile(args[1], CodecMode.SAVE);
		codec.process();
		codec.close();
		MeanDifference diff = new MeanDifference();
		diff.setImages(inputImage, removal.getOutputImage());
		diff.process();
		System.out.println("Mean difference: " + diff.getDifference());
	}

	private void mergeAndSplit()
	{
		Vector contouringIndexes = createContouringIndexList();
		final int ITERATIONS = Math.min(contouringIndexes.size(), compressibleNodes.size());
		int index = 0;
		do
		{
			// make the node a leaf by setting its two successors (which are leaves) to null
			MedianCutNode compressibleNode = (MedianCutNode)compressibleNodes.elementAt(index);
			compressibleNode.setSuccessors(null, null);
			// split the contouring color into two 
			Integer contouringIndex = (Integer)contouringIndexes.elementAt(index);
			MedianCutNode contouringNode = leaves[contouringIndex.intValue()];
			quantizer.splitNode(contouringNode);
			index++;
		}
		while (index < ITERATIONS);
	}

	public void process() throws MissingParameterException, OperationFailedException, WrongParameterException
	{
		if (quantizer == null)
		{
			throw new MissingParameterException("No MedianCutQuantizer object was specified.");
		}
		ensureInputImageIsAvailable();
		PixelImage pixelImage = getInputImage();
		if (!(pixelImage instanceof RGB24Image))
		{
			throw new WrongParameterException("Input image must implement RGB24Image.");
		}
		RGB24Image originalImage = (RGB24Image)pixelImage;
		quantizer.setInputImage(originalImage);
		quantizer.setMapping(true); // we want the quantizer to create an output image
		quantizer.setTruecolorOutput(false); // that output image must be paletted
		quantizer.process();
		for (int currentPass = 0; currentPass < numPasses; currentPass++)
		{
			Paletted8Image palImage = (Paletted8Image)quantizer.getOutputImage();
			palette = palImage.getPalette();
			// create co-occurrence matrix for paletted image
			CoOccurrenceMatrix com = MatrixCreator.createCoOccurrenceMatrix(palImage);
			// create co-occurrence frequency matrix for co-occurrence matrix
			CoOccurrenceFrequencyMatrix cofm = MatrixCreator.createCoOccurrenceFrequencyMatrix(com);
			// compute certain statistics from the co-occurrence frequency matrix
			computeStatistics(cofm);
			// find pairs of contouring and compressible colors
			leaves = quantizer.createLeafList();
			findColorPairs(cofm, com);
			if (compressibleNodes.size() == 0 ||
			    contouringPairs.size() == 0)
			{
				//System.out.println("Compressible=" + compressibleNodes.size() + contouring=" + contouringPairs.size() + " in iteration " + currentPass); 
				break;
			}
			// adjust Median-Cut-specific data structures:
			// merge compressible and split contouring nodes
			System.out.println((currentPass + 1) + " " +
				compressibleNodes.size() + " " + contouringPairs.size()); 
			mergeAndSplit();
			// create a new version of the paletted image:
			// (1) reassign palette index values for the nodes
			quantizer.setAllPaletteIndexValues();
			// (2) make it recompute the representative colors
			quantizer.findAllRepresentativeColors();
			// (3) create a new Palette object
			palette = quantizer.createPalette();
			// (4) give that to the paletted image
			Paletted8Image out = (Paletted8Image)quantizer.getOutputImage();
			out.setPalette(palette);
			// (5) map original to that new palette
			quantizer.mapImage(originalImage, out);
		}
		setOutputImage(quantizer.getOutputImage());
	}

	/**
	 * Set the
	 * {@link net.sourceforge.jiu.color.quantization.MedianCutQuantizer} 
	 * object to be used with this contour removal operation.
	 * This is a mandatory parameter.
	 * If process gets called before the quantizer object was specified,
	 * a {@link MissingParameterException} is thrown.
	 * @param medianCutQuantizer the quantizer object that will get used by this operation
	 */
	public void setQuantizer(MedianCutQuantizer medianCutQuantizer)
	{
		quantizer = medianCutQuantizer;
	}

	/**
	 * Specify the number of contour removal passes to be performed.
	 * Check out the section <a href="#howitworks">How the contour removal algorithm works</a>
	 * to learn more about the meaning of this value.
	 * If this method is not called the default value {@link #DEFAULT_NUM_PASSES}
	 * is used.
	 * @param newValue number of passes, 1 or higher
	 * @throws IllegalArgumentException if the argument is 0 or smaller
	 */
	public void setNumPasses(int newValue)
	{
		if (newValue < 1)
		{
			throw new IllegalArgumentException("Number of passes must be 1 or larger.");
		}
		numPasses = newValue;
	}

	/**
	 * Specify the tau value to be used by this operation.
	 * Check out the section <a href="#howitworks">How the contour removal algorithm works</a>
	 * to learn more about the meaning of this value.
	 * If this method is not called the default value {@link #DEFAULT_TAU}
	 * is used.
	 * @param newValue tau value, 0.0 or higher
	 * @throws IllegalArgumentException if the argument is smaller than 0.0
	 */
	public void setTau(double newValue)
	{
		if (newValue < 0.0)
		{
			throw new IllegalArgumentException("Tau value must be 0.0 or larger.");
		}
		tau = newValue;
	}

	/**
	 * Converts a Vector to an Object array.
	 * Since Java 1.2 Vector has a toArray method, but we cannot rely
	 * on 1.2 being available.
	 * @param list Vector with objects
	 * @return Object array with elements from list, in the same order
	 */
	private Object[] toArray(Vector list)
	{
		Object[] result = new Object[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			result[i] = list.elementAt(i);
		}
		return result;
	}
}
