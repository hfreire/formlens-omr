/*
 * OctreeNode
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.util.ComparatorInterface;

/**
 * A single node in an octree.
 * @author Marco Schmidt
 * @since 0.6.0
 * @see OctreeColorQuantizer
 */
public class OctreeNode implements ComparatorInterface, RGBIndex
{
	private int paletteIndex;
	private int pixelCount;
	private int redSum;
	private int greenSum;
	private int blueSum;
	private int red;
	private int green;
	private int blue;
	private OctreeNode[] children;

	/**
	 * Add a color red-green-blue to the octree, given by its root node.
	 * This methods follows the octree down to the bitsPerSample'th level,
	 * creating nodes as necessary.
	 * Increases the pixelCount of a leaf node (if the node already exists)
	 * or initializes a newly-created leaf.
	 * @param root root node of the octree
	 * @param red the red intensity value of the color to be added
	 * @param green the green intensity value of the color to be added
	 * @param blue the blue intensity value of the color to be added
	 * @param bitsPerSample
	 */
	public static boolean add(OctreeNode root, int red, int green, int blue, int bitsPerSample)
	{
		OctreeNode node = root;
		boolean newColor = false;
		int shift = bitsPerSample - 1;
		do
		{
			if (shift >= 0)
			{
				// not a leaf
				OctreeNode[] children = node.children;
				if (children == null)
				{
					children = new OctreeNode[8];
					node.children = children;
				}
				int index = computeIndex(red, green, blue, shift);
				node = children[index];
				if (node == null)
				{
					node = new OctreeNode();
					children[index] = node;
					newColor = true;
				}
				shift--;
			}
			else
			{
				// leaf; update its red/green/blue/pixel count and leave
				node.update(red, green, blue);
				return newColor;
			}
		}
		while (true);
	}

	public int compare(Object o1, Object o2)
	{
		OctreeNode n1 = (OctreeNode)o1;
		OctreeNode n2 = (OctreeNode)o2;
		int pc1 = n1.pixelCount;
		int pc2 = n2.pixelCount;
		if (pc1 < pc2)
		{
			return -1;
		}
		else
		if (pc1 == pc2)
		{
			return 0;
		}
		else
		{
			return 1;
		}
	}

	private static int computeIndex(int red, int green, int blue, int shift)
	{
		return (((red >> shift) & 1) << 2) |
			(((green >> shift) & 1) << 1) |
			((blue >> shift) & 1);
	}

	/**
	 * Adds the sums for red, green and blue values and
	 * the pixel count values of all child nodes and
	 * stores the results in this node.
	 * Does nothing if this is a leaf.
	 * Otherwise, recursively calls itself with all
	 * non-null child nodes and adds their sums for red,
	 * green and blue and the number of pixel values.
	 * Then stores these values in this node.
	 * They will be used when the octree is pruned to have
	 * a certain number of leaves.
	 */
	public void copyChildSums()
	{
		if (children == null)
		{
			return;
		}
		redSum = 0;
		greenSum = 0;
		blueSum = 0;
		pixelCount = 0;
		for (int i = 0; i < children.length; i++)
		{
			OctreeNode child = children[i];
			if (child != null)
			{
				child.copyChildSums();
				redSum += child.redSum;
				greenSum += child.greenSum;
				blueSum += child.blueSum;
				pixelCount += child.pixelCount;
			}
		}
	}

	public void determineRepresentativeColor()
	{
		if (pixelCount > 0)
		{
			red = redSum / pixelCount;
			green = greenSum / pixelCount;
			blue = blueSum / pixelCount;
		}
	}

	/*public static OctreeNode findMinimumNode(OctreeNode node, OctreeNode currentMinimumNode, int minimumNodePixelCount)
	{
		OctreeNode[] children = node.getChildren();
		if (children == null)
		{
			return currentMinimumNode;
		}
		int sum = 0;
		boolean hasOnlyLeafChildren = true;
		for (int i = 0; i < children.length; i++)
		{
			OctreeNode child = children[i];
			if (child != null)
			{
				if (child.isLeaf())
				{
					sum += child.pixelCount;
				}
				else
				{
					hasOnlyLeafChildren = false;
					//findMinimumNode(child, currentMinimumNode, minimumNodePixelCount);
				}
			}
		}
		return currentMinimumNode;
	}*/

	public int getBlue()
	{
		return blue;
	}

	public OctreeNode[] getChildren()
	{
		return children;
	}

	public int getGreen()
	{
		return green;
	}

	public int getNumChildren()
	{
		int result = 0;
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				if (children[i] != null)
				{
					result++;
				}
			}
		}
		return result;
	}

	public int getPaletteIndex()
	{
		return paletteIndex;
	}

	public int getRed()
	{
		return red;
	}

	public boolean isLeaf()
	{
		return children == null;
	}

	/**
	 * Returns the index of the best match for origRgb in the palette or
	 * -1 if the best match could not be determined.
	 * If there was a best match, quantizedRgb is filled with the quantized color's
	 * RGB values.
	 */
	public int map(int[] origRgb, int[] quantizedRgb)
	{
		return map(origRgb[INDEX_RED], origRgb[INDEX_GREEN], origRgb[INDEX_BLUE], 7, quantizedRgb);
	}

	private final int map(final int r, final int g, final int b, final int shift, final int[] quantizedRgb)
	{
		if (children == null)
		{
			quantizedRgb[INDEX_RED] = red;
			quantizedRgb[INDEX_GREEN] = green;
			quantizedRgb[INDEX_BLUE] = blue;
			return paletteIndex;
		}
		int index = computeIndex(r, g, b, shift);
		OctreeNode node = children[index];
		if (node == null)
		{
			return -1;
		}
		return node.map(r, g, b, shift - 1, quantizedRgb);
	}

	public void setChildren(OctreeNode[] newChildren)
	{
		children = newChildren;
	}

	public void setPaletteIndex(int index)
	{
		paletteIndex = index;
	}

	private void update(int red, int green, int blue)
	{
		redSum += red;
		greenSum += green;
		blueSum += blue;
		pixelCount++;
	}
}
