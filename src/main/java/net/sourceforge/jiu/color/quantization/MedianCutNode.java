/*
 * MedianCutNode
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.color.quantization.RGBColor;
import net.sourceforge.jiu.data.RGBIndex;

/**
 * An instance of this node class represents a cuboid part 
 * of the color cube representing the three-dimensional RGB color space.
 * @author Marco Schmidt
 * @see MedianCutQuantizer
 */
public class MedianCutNode implements RGBIndex
{
	private int axis;
	private boolean axisDetermined;
	private int diff;
	private int index1;
	private int index2;
	private MedianCutNode leftSuccessor;
	private int[] max;
	private int medianValue;
	private int middleIndex;
	private int[] min;
	private int paletteIndex;
	private MedianCutNode parent;
	private int[] reprColor;
	private MedianCutNode rightSuccessor;

	/**
	 * Creates a node for a Median Cut tree of nodes with index values for
	 * some external color array and the parent node.
	 * This parent is null for the root node.
	 * @param parent the parent node of this new node, should be null only for the root node
	 * @param index1 the index value of the first element of colors in the color list
	 * @param index2 the index value of the last element of colors in the color list; must be larger than or equal to index1
	 * @throws IllegalArgumentException if index1 is larger than index2
	 */
	public MedianCutNode(MedianCutNode parent, int index1, int index2)
	{
		if (index1 > index2)
		{
			throw new IllegalArgumentException("MedianCutNode constructor, index1 must be smaller than or equal to index2.");
		}
		this.parent = parent;
		this.index1 = index1;
		this.index2 = index2;
		determineMiddleIndex();
		leftSuccessor = null;
		rightSuccessor = null;
		diff = 0;
		axisDetermined = false;
		max = new int[3];
		min = new int[3];
		paletteIndex = -1;
	}

	/**
	 * Returns if this node can be split into two.
	 * This is true if and only if this is a leaf and if the color
	 * list index values represent an interval of at least length 2.
	 * @return if this node can be split into two nodes
	 */
	public boolean canBeSplit()
	{
		return isLeaf() && index1 != index2;
	}

	/**
	 * Computes the distance in RGB color space between the representative color of this node and the
	 * argument node and returns it as non-negative value.
	 */
	public double computeRgbDistance(MedianCutNode node)
	{
		int[] c = node.reprColor;
		return RGBColor.computeDistance(reprColor[INDEX_RED], reprColor[INDEX_GREEN], reprColor[INDEX_BLUE], c[INDEX_RED], c[INDEX_GREEN], c[INDEX_BLUE]);
	}

	/**
	 * Computes the middle index value of this node.
	 * It uses the index values given to this node's constructor, index1 and index2.
	 */
	private void determineMiddleIndex()
	{
		if (index1 == index2)
		{
			middleIndex = index1;
		}
		else
		{
			middleIndex = (index1 + index2) / 2;
		}
	}

	/**
	 * Returns the axis of the channel whose samples are most widely
	 * distributed among the colors that belong to this node.
	 * @return index of axis, one of the {@link RGBIndex} constants
	 * @throws IllegalArgumentException if that axis has not been determined
	 */
	public int getAxisOfLargestDistribution()
	{
		if (axisDetermined)
		{
			return axis;
		}
		else
		{
			throw new IllegalArgumentException("The axis has not been determined and can thus not be returned.");
		}
	}

	public int getDifferenceOfLargestDistribution()
	{
		if (axisDetermined)
		{
			return diff;
		}
		else
		{
			throw new IllegalArgumentException("The axis has not been determined and can thus not be returned.");
		}
	}

	public int getLeftIndex()
	{
		return index1;
	}

	/**
	 * Returns left successor node (or null if this node is a leaf).
	 */
	public MedianCutNode getLeftSuccessor()
	{
		return leftSuccessor;
	}

	public int getMaxColorSample(int index)
	{
		return max[index];
	}

	public int getMedianValue()
	{
		return medianValue;
	}

	public int getMiddleIndex()
	{
		return middleIndex;
	}

	public int getMinColorSample(int index)
	{
		return min[index];
	}

	public int getNumColors()
	{
		return index2 - index1 + 1;
	}

	public int getPaletteIndex()
	{
		return paletteIndex;
	}

	/**
	 * Returns parent node (or null if this node is the root node).
	 */
	public MedianCutNode getParentNode()
	{
		return parent;
	}

	public int[] getRepresentativeColor()
	{
		return reprColor;
	}

	public int getRightIndex()
	{
		return index2;
	}

	/**
	 * Returns right successor node (or null if this node is a leaf).
	 */
	public MedianCutNode getRightSuccessor()
	{
		return rightSuccessor;
	}

	public MedianCutNode getSuccessor(int[] rgb)
	{
		if (rgb[axis] <= medianValue)
		{
			return leftSuccessor;
		}
		else
		{
			return rightSuccessor;
		}
	}

	public boolean isAxisDetermined()
	{
		return axisDetermined;
	}

	/**
	 * Returns if this node is a leaf by checking if both successors are null.
	 * Note that the case of one successor being null and the other non-null
	 * should never happen.
	 * @return if this node is a leaf (true)
	 */
	public boolean isLeaf()
	{
		return (leftSuccessor == null && rightSuccessor == null);
	}

	public void setLargestDistribution(int newAxis, int newDifference)
	{
		if (newAxis != INDEX_RED && newAxis != INDEX_GREEN && newAxis != INDEX_BLUE)
		{
			throw new IllegalArgumentException("Axis must be either INDEX_RED, INDEX_GREEN or INDEX_BLUE.");
		}
		axis = newAxis;
		diff = newDifference;
		axisDetermined = true;
	}

	public void setMaxColor(int red, int green, int blue)
	{
		max[INDEX_RED] = red;
		max[INDEX_GREEN] = green;
		max[INDEX_BLUE] = blue;
	}

	public void setMaxColorSample(int index, int value)
	{
		max[index] = value;
	}

	public void setMedianValue(int newMedianValue)
	{
		medianValue = newMedianValue;
	}

	public void setMinColor(int red, int green, int blue)
	{
		min[INDEX_RED] = red;
		min[INDEX_GREEN] = green;
		min[INDEX_BLUE] = blue;
	}

	public void setMinColorSample(int index, int value)
	{
		min[index] = value;
	}

	public void setPaletteIndex(int newPaletteIndex)
	{
		paletteIndex = newPaletteIndex;
	}

	public void setRepresentativeColor(int[] aRepresentativeColor)
	{
		if (aRepresentativeColor != null && aRepresentativeColor.length != 3)
		{
			throw new IllegalArgumentException("Representative color array argument must have a length of 3.");
		}
		reprColor = aRepresentativeColor;
	}

	/**
	 * Sets the successor nodes for this node.
	 * The successors must be either both null or both initialized.
	 * They must not be equal.
	 * @param left the left successor node
	 * @param right the left successor node
	 */
	public void setSuccessors(MedianCutNode left, MedianCutNode right)
	{
		if ((left == null && right != null) ||
		    (left != null && right == null))
		{
			throw new IllegalArgumentException("The successor nodes must be either both null or both initialized	.");
		}
		if (left != null && left == right)
		{
			throw new IllegalArgumentException("The successor nodes must not be the same.");
		}
		leftSuccessor = left;
		rightSuccessor = right;
	}
}
