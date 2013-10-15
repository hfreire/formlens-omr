/*
 * RGBColorComparator
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.util.ComparatorInterface;

/**
 * Compares two {@link RGBColor} objects.
 * @author Marco Schmidt
 */
public class RGBColorComparator implements 
	ComparatorInterface, 
	RGBIndex
{
	private int sortOrder;

	public RGBColorComparator(int aSortOrder)
	{
		setSortOrder(aSortOrder);
	}

	public int compare(Object o1, Object o2)
	{
		return ((RGBColor)o1).compareTo((RGBColor)o2, sortOrder);
	}

	/**
	 * Sets the internal sort order (it is sorted by one of the three
	 * RGB components) to the parameter.
	 */
	public void setSortOrder(int aSortOrder)
	{
		if (aSortOrder != INDEX_RED && aSortOrder != INDEX_GREEN && aSortOrder != INDEX_BLUE)
		{
			throw new IllegalArgumentException("The sort order argument must be either INDEX_RED, INDEX_GREEN or INDEX_BLUE.");
		}
		sortOrder = aSortOrder;
	}
}
