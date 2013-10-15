/*
 * RGBColorList
 *
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.quantization;

import net.sourceforge.jiu.color.data.Histogram3D;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.util.ComparatorInterface;
import net.sourceforge.jiu.util.Sort;

/**
 * Holds an array of {@link RGBColor} objects.
 * @author Marco Schmidt
 */
public class RGBColorList implements RGBIndex
{
	private RGBColor[] list;
	private final int numEntries;

	/**
	 * Creates a color list with room for a fixed number of entries.
	 * @param numberOfEntries the number of entries in the new list (must be larger than zero)
	 * @throws IllegalArgumentException if the argument is smaller than one
	 */
	private RGBColorList(final int NUM_ENTRIES)
	{
		if (NUM_ENTRIES < 1)
		{
			throw new IllegalArgumentException("RGBColorList must have at least one entry; got " + NUM_ENTRIES);
		}
		numEntries = NUM_ENTRIES;
		list = new RGBColor[NUM_ENTRIES];
	}

	/**
	 * Creates a new list and initializes it with the argument histogram.
	 * All values from the histogram with a counter larger than zero will
	 * be added to the list (which will include all colors that appear at least
	 * once in the image on which the histogram was created).
	 * @param hist the histogram from which the list will be initialized
	 * @throws IllegalArgumentException thrown if no histogram entry has a non-zero counter
	 */
	public RGBColorList(Histogram3D hist)
	{
		this(hist.getNumUsedEntries());
		int i = 0;
		final int MAX_RED = hist.getMaxValue(INDEX_RED);
		final int MAX_GREEN = hist.getMaxValue(INDEX_GREEN);
		final int MAX_BLUE = hist.getMaxValue(INDEX_BLUE);
		for (int r = 0; r <= MAX_RED; r++)
		{
			for (int g = 0; g <= MAX_GREEN; g++)
			{
				for (int b = 0; b <= MAX_BLUE; b++)
				{
					int counter = hist.getEntry(r, g, b);
					if (counter > 0)
					{
						list[i++] = new RGBColor(r, g, b, counter);
					}
				}
			}
		}
	}

	/**
	 * In a given interval of the list this method searches for the color axis 
	 * that has the largest distribution of values.
	 * Returns a pair of int values;
	 * the first value is the component (0, 1 or 2),
	 * the second value is the difference between the minimum and maximum value found in the list.
	 * Only checks colors from index i1 to i2 of the list.
	 */
	public int[] findExtrema(int i1, int i2)
	{
		if (i1 < 0 || i1 >= numEntries || i2 < 0 || i2 >= numEntries || i1 > i2)
		{
			return null;
		}
		int[] max = new int[3];
		int[] min = new int[3];
		RGBColor c = list[i1];
		for (int i = 0; i < 3; i++)
		{
			min[i] = max[i] = c.getSample(i);
		}
		int i = i1 + 1;
		while (i < i2)
		{
			c = list[i++];
			for (int j = 0; j < 3; j++)
			{
				int cSample = c.getSample(j);
				if (cSample < min[j])
				{
					min[j] = cSample;
				}
				else
				{
					if (cSample > max[j])
					{
						max[j] = cSample;
					}
				}
			}
		}
		// first value: sample index (0 - 2); second value: difference
		int[] result = new int[2];
		result[0] = result[1] = -1;
		for (i = 0; i < 3; i++)
		{
			int newDiff = max[i] - min[i];
			if (newDiff > result[1])
			{
				result[0] = i;
				result[1] = newDiff;
			}
		}
		return result;
	}

	/**
	 * Returns an {@link RGBColor} object from this list, given by its zero-based
	 * index value.
	 * @param index zero-based index into the list; must be smaller than {@link #getNumEntries()}
	 * @return the color object
	 */
	public RGBColor getColor(int index)
	{
		return list[index];
	}

	/**
	 * Returns the number of color objects in this list.
	 * @return number of colors in the list
	 */
	public int getNumEntries()
	{
		return list.length;
	}

	/**
	 * Sorts an interval of the array of colors by one of the three components (RGB).
	 * @param index1 the index of the first element in the interval
	 * @param index2 the index of the last element in the interval
	 * @param axis the color component by which the interval is to be sorted, {@link #INDEX_RED}, {@link #INDEX_GREEN} or {@link #INDEX_BLUE}
	 */
	public void sortByAxis(int index1, int index2, int axis) 
	{
		Sort.sort(list, index1, index2, new RGBColorComparator(axis));
	}

	/**
	 * Sorts an interval of the array of colors by their counters.
	 * @param index1 the index of the first element in the interval
	 * @param index2 the index of the last element in the interval
	 */
	public void sortByCounter(int index1, int index2) 
	{
		Sort.sort(list, index1, index2, new ComparatorInterface()
		{
			public int compare(Object obj1, Object obj2)
			{
				RGBColor col1 = (RGBColor)obj1;
				RGBColor col2 = (RGBColor)obj2;
				return col1.getCounter() - col2.getCounter();
			}
		});
	}
}
