/*
 * MedianCutQuantizer
 *
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.color.analysis.Histogram3DCreator;
import net.sourceforge.jiu.color.data.Histogram3D;
import net.sourceforge.jiu.color.quantization.MedianCutNode;
import net.sourceforge.jiu.color.quantization.RGBColor;
import net.sourceforge.jiu.color.quantization.RGBColorList;
import net.sourceforge.jiu.color.quantization.RGBQuantizer;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Performs the <em>Median Cut</em> color quantization algorithm 
 * for a given list of colors.
 * <h3>Supported image types</h3>
 * The input image must implement {@link net.sourceforge.jiu.data.RGB24Image}.
 * <h3>Usage example</h3>
 * The following code snippet uses the default settings with a palette of 256 entries.
 * <pre>
 * MedianCutQuantizer quantizer = new MedianCutQuantizer();
 * quantizer.setInputImage(image);
 * quantizer.setPaletteSize(256);
 * quantizer.process();
 * PixelImage quantizedImage = quantizer.getOutputImage();
 * </pre>
 * If you want to combine Median Cut quantization with error diffusion dithering to
 * improve the visual quality of the output, try the
 * {@link net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering} class.
 * However, note that noise is introduced into the image with dithering methods so
 * that the resulting image may not be suitable for automatic processing.
 * <h3>Credits</h3>
 * The Median Cut algorithm was designed by 
 * <a target="_top" href="http://www.cs.cmu.edu/~ph">Paul Heckbert</a>.
 * He described it in the article <em>Color image quantization for frame
 * buffer display</em>. Comput. Graphics 16(3), 1982, 297 - 304.
 * <a target="_top" href="http://citeseer.nj.nec.com/heckbert80color.html">CiteSeer
 * page of the article</a>.
 * @author Marco Schmidt
 * @see MedianCutContourRemoval
 * @see net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering
 */
public class MedianCutQuantizer extends ImageToImageOperation implements RGBIndex, RGBQuantizer
{
	/**
	 * Constant value for a method of determining the representative color 
	 * for a set of colors by computing the average of all samples for each
	 * of the three components red, green and blue.
	 * #getMethodToDetermineRepresentativeColors
	 * #setMethodToDetermineRepresentativeColors
	 */
	public static final int METHOD_REPR_COLOR_AVERAGE = 0;

	/**
	 * Constant value for a method of determining the representative color 
	 * for a set of colors by computing the weighted average of all samples for each
	 * of the three components red, green and blue.
	 * Weighted means that each color is multiplied by the number of times it occurs 
	 * in the input image.
	 * The values of samples multiplied by their frequency are then divided by the total
	 * number of times the colors appear in the image.
	 * #getMethodToDetermineRepresentativeColors
	 * #setMethodToDetermineRepresentativeColors
	 */
	public static final int METHOD_REPR_COLOR_WEIGHTED_AVERAGE = 1;

	/**
	 * Constant value for a method of determining the representative color 
	 * for a set of colors by picking the median value of all samples for each
	 * of the three components red, green and blue.
	 * #getMethodToDetermineRepresentativeColors
	 * #setMethodToDetermineRepresentativeColors
	 */
	public static final int METHOD_REPR_COLOR_MEDIAN = 2;

	/**
	 * The default method to determine the representative color
	 * from a list of colors.
	 * Will be used if none is set by the user of this class via
	 * {@link #setMethodToDetermineRepresentativeColors}.
	 */
	public static final int DEFAULT_METHOD_REPR_COLOR = METHOD_REPR_COLOR_MEDIAN;

	private boolean doNotMap;
	private RGBColorList list;
	private int maxValue;
	private int method;
	private boolean outputTruecolor;
	private int paletteSize;
	private MedianCutNode root;

	/**
	 * Creates a MedianCutQuantizer object and 
	 * initializes its fields to default values.
	 */
	public MedianCutQuantizer()
	{
		doNotMap = false;
		maxValue = -1;
		method = METHOD_REPR_COLOR_AVERAGE;
		maxValue = 255;
		outputTruecolor = false;
		paletteSize = 256;
	}

	private void addNodes(MedianCutNode[] nodeList, MedianCutNode node)
	{
		if (node == null)
		{
			return;
		}
		if (node.isLeaf())
		{
			int index = node.getPaletteIndex();
			if (index >= 0 && index < nodeList.length)
			{
				nodeList[index] = node;
			}
			else
			{
				// ERROR ILLEGAL STATE
				throw new IllegalStateException("A node's index is invalid.");
			}
		}
		else
		{
			addNodes(nodeList, node.getLeftSuccessor());
			addNodes(nodeList, node.getRightSuccessor());
		}
	}

	private RGBColorList createColorList(RGB24Image image) throws OperationFailedException
	{
		Histogram3DCreator hc = new Histogram3DCreator();
		hc.setImage(image, RGBIndex.INDEX_RED, RGBIndex.INDEX_GREEN, RGBIndex.INDEX_BLUE);
		hc.process();
		Histogram3D hist = hc.getHistogram();
		if (hist == null)
		{
			throw new OperationFailedException("Could not create histogram from input image.");
		}
		int numUniqueColors = hist.getNumUsedEntries();
		if (numUniqueColors <= paletteSize)
		{
			throw new WrongParameterException("Input image has only " + numUniqueColors + 
				" unique color(s), so it cannot be reduced to " + paletteSize +
				" color(s).");
		}
		return new RGBColorList(hist);
	}

	/**
	 * Creates a linear list of leaf nodes.
	 * Assumes that {@link #findPalette()} was successfully run before.
	 */
	public MedianCutNode[] createLeafList()
	{
		MedianCutNode[] result = new MedianCutNode[paletteSize];
		addNodes(result, root);
		return result;
	}

	/**
	 * Creates a palette with the representative colors of all leaf nodes.
	 * Assumes that {@link #findPalette()} was successfully run before.
	 * @return palette with all representative colors
	 */
	public Palette createPalette()
	{
		MedianCutNode[] leaves = createLeafList();
		Palette result = new Palette(leaves.length);
		for (int i = 0; i < leaves.length; i++)
		{
			int[] reprColor = leaves[i].getRepresentativeColor();
			result.putSample(INDEX_RED, i, reprColor[INDEX_RED]);
			result.putSample(INDEX_GREEN, i, reprColor[INDEX_GREEN]);
			result.putSample(INDEX_BLUE, i, reprColor[INDEX_BLUE]);
		}
		return result;
	}

	/**
	 * Traverses tree given by argument node and returns leaf with largest distribution
	 * of samples for any of its three components.
	 */
	private MedianCutNode findLeafToBeSplit(MedianCutNode node)
	{
		if (node == null)
		{
			return null;
		}
		if (node.canBeSplit())
		{
			if (!node.isAxisDetermined())
			{
				int[] pairAxisDiff = list.findExtrema(node.getLeftIndex(), node.getRightIndex());
				if (pairAxisDiff == null)
				{
					return null;
				}
				node.setLargestDistribution(pairAxisDiff[0], pairAxisDiff[1]); // axis, difference
			}
			return node;
		}
		else
		{
			MedianCutNode node1 = findLeafToBeSplit(node.getLeftSuccessor());
			boolean canSplit1 = (node1 != null && node1.canBeSplit());
			MedianCutNode node2 = findLeafToBeSplit(node.getRightSuccessor());
			boolean canSplit2 = (node2 != null && node2.canBeSplit());
			if (canSplit1)
			{
				if (canSplit2)
				{
					// case 1 of 4: both nodes can be split; find out which one has the largest distribution
					// of samples for one of the three RGB channels
					if (node1.getDifferenceOfLargestDistribution() >= node2.getDifferenceOfLargestDistribution())
					{
						return node1;
					}
					else
					{
						return node2;
					}
				}
				else
				{
					// case 2 of 4: node1 can be split, node2 can't => take node1
					return node1;
				}
			}
			else
			{
				if (canSplit2)
				{
					// case 3 of 4: node2 can be split, node1 can't => take node2
					return node2;
				}
				else
				{
					// case 4 of 4: both nodes cannot be split => return null
					return null;
				}
			}
		}
	}

	/**
	 * For a given RGB value, searches the node in the internal node tree whose 
	 * representative color is closest to this color.
	 * @param rgb the color for which a match is searched; the array must have at least 
	 *  three entries; {@link RGBIndex} constants are used to address the samples
	 * @return node with best match
	 */
	public MedianCutNode findNearestNeighbor(int[] rgb)
	{
		MedianCutNode result = root;
		while (!result.isLeaf())
		{
			result = result.getSuccessor(rgb);
		}
		return result;
	}

	/**
	 * For each node in the argument array computes the distance between the
	 * representative color of that node and the color given by the three 
	 * argument samples.
	 * @return index of the node with the smallest distance or -1 if the array has a length of 0
	 */
	public int findNearestNeighbor(MedianCutNode[] nodes, int red, int green, int blue)
	{
		int index = -1;
		double distance = 1000000;
		for (int i = 0; i < nodes.length; i++)
		{
			MedianCutNode node = nodes[i];
			int[] reprColor = node.getRepresentativeColor();
			double d = RGBColor.computeDistance(red, green, blue, 
				reprColor[INDEX_RED], reprColor[INDEX_GREEN], reprColor[INDEX_BLUE]);
			if (d < distance)
			{
				distance = d;
				index = i;
			}
		}
		return index;
	}

	public void findPalette() 
	{
		int colorsLeft = paletteSize - 1;
		while (colorsLeft > 0)
		{
			// find leaf with largest sample difference
			MedianCutNode node = findLeafToBeSplit(root);
			splitNode(node);
			colorsLeft--;
		}
		findRepresentativeColors(root);
		setAllPaletteIndexValues();
	}

	public void findAllRepresentativeColors()
	{
		findRepresentativeColors(root);
	}

	/**
	 * Computes a representative color for a set of colors in the color list.
	 * Returns the color as a length three int array of sample values (which can be accessed using the 
	 * index constants from {@link RGBIndex}.
	 * The method of determining the color (the REPR_xxx constants from this class) 
	 * has been given to the constructor.
	 */
	private int[] findRepresentativeColor(int index1, int index2)
	{
		int[] result = new int[3];
		long[] temp = new long[3];
		temp[0] = 0;
		temp[1] = 0;
		temp[2] = 0;
		switch(method)
		{
			case(METHOD_REPR_COLOR_AVERAGE):
			{
				int num = index2 - index1 + 1;
				for (int i = index1; i <= index2; i++)
				{
					RGBColor color = list.getColor(i);
					temp[0] += color.getSample(0);
					temp[1] += color.getSample(1);
					temp[2] += color.getSample(2);
				}
				result[0] = (int)(temp[0] / num);
				result[1] = (int)(temp[1] / num);
				result[2] = (int)(temp[2] / num);
				return result;
			}
			case(METHOD_REPR_COLOR_WEIGHTED_AVERAGE):
			{
				long num = 0;
				for (int i = index1; i <= index2; i++)
				{
					RGBColor color = list.getColor(i);
					int counter = color.getCounter();
					temp[0] += color.getSample(0) * counter;
					temp[1] += color.getSample(1) * counter;
					temp[2] += color.getSample(2) * counter;
					num += counter;
				}
				if (num == 0)
				{
					//System.out.println("ERROR IN FINDREPRESENTATIVECOLOR (WEIGHTED AVERAGE): ZERO COUNTER");
					return null;
				}
				result[0] = (int)(temp[0] / num);
				result[1] = (int)(temp[1] / num);
				result[2] = (int)(temp[2] / num);
				return result;
			}
			case(METHOD_REPR_COLOR_MEDIAN):
			{
				RGBColor color = list.getColor((index1 + index2) / 2);
				result[0] = color.getSample(0);
				result[1] = color.getSample(1);
				result[2] = color.getSample(2);
				return result;
			}
			default: throw new IllegalStateException("Unknown method for determining a representative color.");
		}
	}

	/**
	 * Calls findRepresentativeColor with node if node is a leaf.
	 * Otherwise, recursively calls itself with both successor nodes.
	 */
	private void findRepresentativeColors(MedianCutNode node)
	{
		if (node == null)
		{
			return;
		}
		if (node.isLeaf())
		{
			node.setRepresentativeColor(findRepresentativeColor(node.getLeftIndex(), node.getRightIndex()));
		}
		else
		{
			findRepresentativeColors(node.getLeftSuccessor());
			findRepresentativeColors(node.getRightSuccessor());
		}
	}

	/**
	 * Returns the method (to be) used to determine the representative
	 * color for the list of colors of a node.
	 * Default is {@link #DEFAULT_METHOD_REPR_COLOR}.
	 * @return the method, one of the METHOD_xyz constants
	 */
	public int getMethodToDetermineRepresentativeColors()
	{
		return method;
	}

	/**
	 * Returns the number of colors in the destination image.
	 * If output is paletted, this is also the number of entries
	 * in the palette.
	 * @return number of colors in the destination
	 */
	public int getPaletteSize()
	{
		return paletteSize;
	}

	/**
	 * Returns if this operation is supposed to generate truecolor or
	 * paletted output.
	 * @return if truecolor images are to be generated
	 * @see #setTruecolorOutput(boolean)
	 */
	public boolean getTruecolorOutput()
	{
		return outputTruecolor;
	}

	public int map(int[] origRgb, int[] quantizedRgb)
	{
		MedianCutNode node = findNearestNeighbor(origRgb);
		int[] reprColor = node.getRepresentativeColor();
		quantizedRgb[INDEX_RED] = reprColor[INDEX_RED];
		quantizedRgb[INDEX_GREEN] = reprColor[INDEX_GREEN];
		quantizedRgb[INDEX_BLUE] = reprColor[INDEX_BLUE];
		return node.getPaletteIndex();
	}

	public void mapImage(RGB24Image in, RGB24Image out)
	{
		int[] rgb = new int[3];
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				rgb[INDEX_RED] = in.getSample(INDEX_RED, x, y);
				rgb[INDEX_GREEN] = in.getSample(INDEX_GREEN, x, y);
				rgb[INDEX_BLUE] = in.getSample(INDEX_BLUE, x, y);
				MedianCutNode node = findNearestNeighbor(rgb);
				int[] reprColor = node.getRepresentativeColor();
				out.putSample(INDEX_RED, x, y, reprColor[INDEX_RED]);
				out.putSample(INDEX_GREEN, x, y, reprColor[INDEX_GREEN]);
				out.putSample(INDEX_BLUE, x, y, reprColor[INDEX_BLUE]);
			}
		}
	}

	public void mapImage(RGB24Image in, Paletted8Image out)
	{
		int[] rgb = new int[3];
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				rgb[INDEX_RED] = in.getSample(INDEX_RED, x, y);
				rgb[INDEX_GREEN] = in.getSample(INDEX_GREEN, x, y);
				rgb[INDEX_BLUE] = in.getSample(INDEX_BLUE, x, y);
				MedianCutNode node = findNearestNeighbor(rgb);
				out.putSample(0, x, y, node.getPaletteIndex());
			}
		}
	}

	public void process() throws
		MissingParameterException,
		OperationFailedException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		if (in instanceof RGB24Image)
		{
			list = createColorList((RGB24Image)in);
		}
		else
		{
			throw new WrongParameterException("Input image must implement RGB24Image.");
		}
		root = new MedianCutNode(null, 0, list.getNumEntries() - 1);
		root.setMinColor(0, 0, 0);
		root.setMaxColor(maxValue, maxValue, maxValue);
		findPalette();
		if (doNotMap)
		{
			return;
		}
		PixelImage out = getOutputImage();
		if (getTruecolorOutput())
		{
			if (out == null)
			{
				out = in.createCompatibleImage(in.getWidth(), in.getHeight());
				setOutputImage(out);
			}
			else
			{
				if (!(out instanceof RGB24Image))
				{
					throw new WrongParameterException("Output image must implement RGB24Image.");
				}
			}
			mapImage((RGB24Image)in, (RGB24Image)out);
		}
		else
		{
			Palette palette = createPalette();
			if (out == null)
			{
				out = new MemoryPaletted8Image(in.getWidth(), in.getHeight(), palette);
				setOutputImage(out);
			}
			else
			{
				if (out instanceof Paletted8Image)
				{
					((Paletted8Image)out).setPalette(palette);
				}
				else
				{
					throw new WrongParameterException("Output image must implement Paletted8Image.");
				}
			}
			mapImage((RGB24Image)in, (Paletted8Image)out);
		}
	}

	public void setAllPaletteIndexValues()
	{
		int paletteEntriesAssigned = setPaletteIndexValues(root, 0);
		if (paletteEntriesAssigned != paletteSize)
		{
			throw new IllegalStateException("Assigning palette values did not result in correct number of entries.");
		}
	}

	/**
	 * Defines whether process will map the input image to an output image.
	 * If not, only the palette is determined.
	 */
	public void setMapping(boolean doMap)
	{
		doNotMap = !doMap;
	}

	/**
	 * Sets the method to determine the representative color
	 * for a list of colors.
	 * After the algorithm has determined sets of colors that lie
	 * closely together in color space and will be
	 * mapped to the same color in the destination image, 
	 * the algorithm will determine that color 
	 * @param newMethod the new method, one of the METHOD_xyz constants in this class
	 */
	public void setMethodToDetermineRepresentativeColors(int newMethod)
	{
		if (newMethod != METHOD_REPR_COLOR_AVERAGE &&
		    newMethod != METHOD_REPR_COLOR_WEIGHTED_AVERAGE &&
		    newMethod != METHOD_REPR_COLOR_MEDIAN)
		{
			throw new IllegalArgumentException("Method must be one of the METHOD_xyz constants.");
		}
		method = newMethod;
	}

	/**
	 * Recursively visits node and its descendants, assigning ascending 
	 * palette index values to leaves via MedianCutNode.setPaletteIndex(int).
	 * If this method is called with root and 0 as parameters, all leaves
	 * will get a unique palette index.
	 */
	private int setPaletteIndexValues(MedianCutNode node, int index)
	{
		if (node.isLeaf())
		{
			node.setPaletteIndex(index);
			index++;
			return index;
		}
		else
		{
			index = setPaletteIndexValues(node.getLeftSuccessor(), index);
			return setPaletteIndexValues(node.getRightSuccessor(), index);
		}
	}

	/**
	 * Sets the number of colors that this operations is supposed to reduce
	 * the original image to.
	 * @param newPaletteSize the number of colors
	 * @throws IllegalArgumentException if the argument is smaller than 1 or larger than 256
	 * @see #getPaletteSize
	 */
	public void setPaletteSize(int newPaletteSize)
	{
		if (newPaletteSize < 1)
		{
			throw new IllegalArgumentException("Palette size must be 1 or larger.");
		}
		if (newPaletteSize > 256)
		{
			throw new IllegalArgumentException("Palette size must be at most 256.");
		}
		paletteSize = newPaletteSize;
	}

	/**
	 * Lets the user specify if the output image is to be truecolor 
	 * (argument useTruecolor is <code>true</code>) or paletted
	 * (argument useTruecolor is <code>false</code>).
	 * If the color type is to be changed afterwards, use PromoteToRgb24
	 * to convert from paletted to truecolor.
	 * Reducing a truecolor image that uses only 256 or less colors to
	 * a paletted image can be done with AutoDetectColorType.
	 * @param useTruecolor 
	 */
	public void setTruecolorOutput(boolean useTruecolor)
	{
		outputTruecolor = useTruecolor;
	}

	public void splitNode(MedianCutNode node)
	{
		if (!node.isAxisDetermined())
		{
			int[] pairAxisDiff = list.findExtrema(node.getLeftIndex(), node.getRightIndex());
			node.setLargestDistribution(pairAxisDiff[0], pairAxisDiff[1]); // axis, difference
		}
		
		list.sortByAxis(node.getLeftIndex(), node.getRightIndex(), node.getAxisOfLargestDistribution());
		int middleIndex = node.getMiddleIndex();
		int leftIndex = node.getLeftIndex();
		int rightIndex = node.getRightIndex();
		RGBColor color = list.getColor(middleIndex);
		int axis = node.getAxisOfLargestDistribution();
		int medianValue = color.getSample(axis);
		node.setMedianValue(medianValue);
		if (leftIndex == rightIndex)
		{
			throw new IllegalArgumentException("Cannot split leaf that only holds one color. This should never happen.");
		}
		MedianCutNode left = new MedianCutNode(node, leftIndex, middleIndex);
		MedianCutNode right = new MedianCutNode(node, middleIndex + 1, rightIndex);
		node.setSuccessors(left, right);
		for (int i = 0; i < 3; i++)
		{
			int max = node.getMaxColorSample(i);
			left.setMaxColorSample(i, max);
			right.setMaxColorSample(i, max);
			int min = node.getMinColorSample(i);
			left.setMinColorSample(i, min);
			right.setMinColorSample(i, min);
		}
		left.setMaxColorSample(axis, medianValue);
		right.setMinColorSample(axis, medianValue + 1);
	}
}
