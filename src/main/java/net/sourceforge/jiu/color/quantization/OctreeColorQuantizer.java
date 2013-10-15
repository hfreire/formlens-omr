/*
 * OctreeColorQuantizer
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.color.quantization.RGBQuantizer;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;
import net.sourceforge.jiu.util.Sort;

/**
 * Performs the octree color quantization algorithm for a given RGB truecolor image.
 * The quality is usually somewhat inferior to the results of {@link MedianCutQuantizer}.
 * Note that you can improve the quality by applying a dithering algorithm.
 * See {@link net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering}.
 *
 * <h3>Usage example</h3>
 * This reduces some RGB24Image image to a 16 color paletted image:
 * <pre>
 * MemoryRGB24Image image = ...; // initialize
 * OctreeColorQuantizer ocq = new OctreeColorQuantizer();
 * ocq.setInputImage(image);
 * ocq.setPaletteSize(16);
 * ocq.process();
 * PixelImage quantizedImage = ocq.getOutputImage();
 * </pre>
 *
 * <h3>Credits</h3>
 * 
 * @author Marco Schmidt
 * @since 0.6.0
 */
public class OctreeColorQuantizer extends ImageToImageOperation implements RGBIndex, RGBQuantizer
{
	/**
	 * The default number of colors in the palette.
	 * Will be used when no other value is specified via {@link #setPaletteSize}.
	 */
	public static final int DEFAULT_PALETTE_SIZE = 256;

	private int paletteSize = DEFAULT_PALETTE_SIZE;
	private OctreeNode root;
	private Palette palette;
	private int[] redValues;
	private int[] greenValues;
	private int[] blueValues;

	/**
	 * If node is a leaf node, this method assigns palette index values 
	 * and determines the representative color, otherwise it simply
	 * recursively calls itself for all child nodes.
	 * The updated index value is returned.
	 * It is increased whenever a leaf is assigned that index value.
	 *
	 * @param node the node of the octree that will itself (and its children) be processed
	 * @param index the current index in the palette index assignment procedure
	 * @return updated index value; may have been increased while node or its child(ren) -
	 *  were assigned index values
	 */
	private int assignPaletteIndexValues(OctreeNode node, int index)
	{
		if (node == null)
		{
			return index;
		}
		if (node.isLeaf())
		{
			node.setPaletteIndex(index);
			node.determineRepresentativeColor();
			return index + 1;
		}
		else
		{
			OctreeNode[] children = node.getChildren();
			if (children != null)
			{
				for (int i = 0; i < 8; i++)
				{
					if (children[i] != null)
					{
						index = assignPaletteIndexValues(children[i], index);
					}
				}
			}
			return index;
		}
	}

	public Palette createPalette()
	{
		if (palette == null)
		{
			int numValues = assignPaletteIndexValues(root, 0);
			palette = new Palette(numValues);
			initPalette(root, palette);
			redValues = new int[numValues];
			greenValues = new int[numValues];
			blueValues = new int[numValues];
			for (int i = 0; i < numValues; i++)
			{
				redValues[i] = palette.getSample(INDEX_RED, i);
				greenValues[i] = palette.getSample(INDEX_GREEN, i);
				blueValues[i] = palette.getSample(INDEX_BLUE, i);
			}
			return palette;
		}
		else
		{
			return (Palette)palette.clone();
		}
	}

	/**
	 * Creates an octree and prepares this quantizer so that colors can be mapped to
	 * palette index values.
	 * If you use {@link #process()} you must not call this method.
	 * On the other hand, if you want to do the mapping yourself - maybe if you
	 * want to do mapping and dithering interchangeably - call this method first,
	 * then do the mapping yourself.
	 * @throws MissingParameterException if parameters like the input image are missing
	 * @throws WrongParameterException if parameters exist but are of the wrong type
	 */
	public void init() throws
		MissingParameterException,
		WrongParameterException
	{
		PixelImage pi = getInputImage();
		if (pi == null)
		{
			throw new MissingParameterException("Input image needed.");
		}
		if (!(pi instanceof RGB24Image))
		{
			throw new WrongParameterException("Input image must be of type RGB24Image.");
		}
		initOctree();
		pruneOctree();
	}

	private int initOctree()
	{
		int numUniqueColors = 0;
		root = new OctreeNode();
		RGB24Image in = (RGB24Image)getInputImage();
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(INDEX_RED, x, y);
				int green = in.getSample(INDEX_GREEN, x, y);
				int blue = in.getSample(INDEX_BLUE, x, y);
				if (OctreeNode.add(root, red, green, blue, 8))
				{
					numUniqueColors++;
				}
			}
		}
		root.copyChildSums();
		return numUniqueColors;
	}

	private void initPalette(OctreeNode node, Palette palette)
	{
		if (node == null)
		{
			return;
		}
		if (node.isLeaf())
		{
			int index = node.getPaletteIndex();
			palette.put(index, node.getRed(), node.getGreen(), node.getBlue());
			return;
		}
		OctreeNode[] children = node.getChildren();
		if (children == null)
		{
			return;
		}
		for (int i = 0; i < children.length; i++)
		{
			OctreeNode child = children[i];
			if (child != null)
			{
				initPalette(child, palette);
			}
		}
	}

	/**
	 * Maps an RGB color <code>origRgb</code> to one of the colors in the
	 * color map; that color will be written to <code>quantizedRgb</code>
	 * and its palette index will be returned.
	 * @param origRgb the color to be mapped to the best-possible counterpart in the
	 *  palette; the array is indexed by the constants from {@link RGBIndex}
	 * @param quantizedRgb the resulting color from the palette will be written
	 *  to this array; it is also indexed by the constants from {@link RGBIndex}
	 * @return index of the found color in the palette
	 */
	public int map(int[] origRgb, int[] quantizedRgb)
	{
		int result = root.map(origRgb, quantizedRgb);
		if (result == -1)
		{
			int minIndex = 0;
			int minDistance = Integer.MAX_VALUE;
			int i = 0;
			int red = origRgb[INDEX_RED];
			int green = origRgb[INDEX_GREEN];
			int blue = origRgb[INDEX_BLUE];
			while (i < redValues.length)
			{
				int v = (redValues[i] - red);
				int sum = v * v;
				v = (greenValues[i] - green);
				sum += v * v;
				v = (blueValues[i] - blue);
				sum += v * v;
				if (sum < minDistance)
				{
					minIndex = i;
					minDistance = sum;
				}
				i++;
			}
			quantizedRgb[INDEX_RED] = redValues[minIndex];
			quantizedRgb[INDEX_GREEN] = greenValues[minIndex];
			quantizedRgb[INDEX_BLUE] = blueValues[minIndex];
			return minIndex;
		}
		else
		{
			// quantizedRgb was filled with values in root.map
			return result;
		}
	}

	private void mapImage()
	{
		RGB24Image in = (RGB24Image)getInputImage();
		Palette palette = createPalette();
		Paletted8Image out = new MemoryPaletted8Image(in.getWidth(), in.getHeight(), palette);
		int[] origRgb = new int[3];
		int[] quantizedRgb = new int[3];
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				origRgb[INDEX_RED] = in.getSample(INDEX_RED, x, y);
				origRgb[INDEX_GREEN] = in.getSample(INDEX_GREEN, x, y);
				origRgb[INDEX_BLUE] = in.getSample(INDEX_BLUE, x, y);
				int index = map(origRgb, quantizedRgb);
				out.putSample(0, x, y, index);
			}
		}
		setOutputImage(out);
	}

	/**
	 * Initializes an octree, reduces it have as many leaves (or less) as
	 * the desired palette size and maps the original image to the newly-created
	 * palette.
	 */
	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		init();
		mapImage();
	}

	/**
	 * Reduces the octree until it has as many leaves (or less) than specified
	 * by the <code>paletteSize</code> argument in the constructor
	 * {@link #OctreeColorQuantizer(int)}.
	 */
	private void pruneOctree()
	{
		// create an array for as many palette entries as specified by paletteSize
		OctreeNode[] a = new OctreeNode[paletteSize];
		// initial length is 1, the only element is the root
		a[0] = root;
		int length = 1;
		// create a comparator that will be used to sort the nodes by their pixel count,
		// in ascending order
		OctreeNode comparator = new OctreeNode();
		// loop and split leaves as long as the number of nodes (length) is smaller
		// than the desired palette size
		while (length < paletteSize)
		{
			Sort.sort(a, 0, length - 1, comparator);
			int index = length - 1;
			while (index >= 0)
			{
				OctreeNode node = a[index];
				int numChildren = node.getNumChildren();
				// check if current length minus the node which may be split
				// plus the number of its children does not exceed the desired palette size
				if (numChildren > 0 && length - 1 + numChildren < paletteSize)
				{
					// child nodes fit in here
					if (index < length - 1)
					{
						System.arraycopy(a, index + 1, a, index, length - index - 1);
					}
					length--;
					OctreeNode[] children = node.getChildren();
					for (int i = 0; i < children.length; i++)
					{
						if (children[i] != null)
						{
							a[length++] = children[i];
						}
					}
					break;
				}
				else
				{
					index--;
				}
			}
			if (index == -1)
			{
				// we could not find a node to be split
				break;
			}
		}
		// in some cases it is not possible to get exactly paletteSize leaves;
		// adjust paletteSize to be equal to the number of leaves
		// note that length will never be larger than paletteSize, only smaller
		// in some cases
		paletteSize = length; 
		// make all found nodes leaves by setting their child nodes to null
		for (int i = 0; i < length; i++)
		{
			a[i].setChildren(null);
		}
	}

	public void setPaletteSize(int newPaletteSize)
	{
		if (newPaletteSize < 1)
		{
			throw new IllegalArgumentException("Palette size must be 1 or larger.");
		}
		if (newPaletteSize > 256)
		{
			throw new IllegalArgumentException("Palette size must be 256 or smaller.");
		}
		paletteSize = newPaletteSize;
	}
}
