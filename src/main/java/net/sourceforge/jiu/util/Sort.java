/*
 * Sort
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

import net.sourceforge.jiu.util.ComparatorInterface;

/**
 * Provides sorting of an Object array.
 * @author Marco Schmidt
 */
public class Sort
{
	/**
	 * This class is supposed to have static methods only.
	 * To hide any constructor, we define an empty private one.
	 */
	private Sort()
	{
	}

	/**
	 * Sorts some (or all) elements of an Object array according to a specified comparator.
	 * This method does exactly the same as java.util.Arrays.sort(Object[], int, int, Comparator).
	 * Unfortunately, this method is not available in Java 1.1, so it must be provided here.
	 * <p>
	 * As for the implementation of this method, it is taken from Arrays.java as found in 
	 * Classpath 0.0.2 (2001-01-06).
	 * Go to <a href="http://www.classpath.org">www.classpath.org</a> to learn more
	 * about the project, which implements the Java core libraries under the GPL.
	 *
	 * @param a the array which is to be sorted
	 * @param from the index value of the first element of the interval to be sorted
	 * @param to the index value of the last element of the interval to be sorted
	 * @param c the comparator used to query the relation between two objects
	 */
	public static void sort(Object[] a, int from, int to, ComparatorInterface c)
	{
		if (a == null)
		{
			throw new IllegalArgumentException("The object array to be sorted must be non-null.");
		}
		if (from > to)
		{
			throw new IllegalArgumentException("The from parameter (" + from + ") must be smaller than or equal to the to parameter (" + to + ").");
		}
		if (to >= a.length)
		{
			throw new IllegalArgumentException("The to parameter (" + to + ") must be smaller than the array length (" + a.length + ").");
		}
		if (c == null)
		{
			throw new IllegalArgumentException("The comparator parameter must be non-null.");
		}
		// First presort the array in chunks of length 6 with insertion sort. 
		// mergesort would give too much overhead for this length.
		for (int chunk = from; chunk < to; chunk += 6)
		{
			int end = Math.min(chunk + 6, to);
			for (int i = chunk + 1; i < end; i++)
			{
				if (c.compare(a[i - 1], a[i]) > 0)
				{
					// not already sorted
					int j = i;
					Object elem = a[j];
					do
		  {
		    a[j] = a[j - 1];
		    j--;
		  }
		while (j > chunk && c.compare(a[j - 1], elem) > 0);
		a[j] = elem;
	      }
	  }
      }

    int len = to - from;
    // If length is smaller or equal 6 we are done.
    if (len <= 6)
      return;

    Object[]src = a;
    Object[]dest = new Object[len];
    Object[]t = null;		// t is used for swapping src and dest

    // The difference of the fromIndex of the src and dest array.
    int srcDestDiff = -from;

    // The merges are done in this loop
    for (int size = 6; size < len; size <<= 1)
      {
	for (int start = from; start < to; start += size << 1)
	  {
	    // mid ist the start of the second sublist;
	    // end the start of the next sublist (or end of array).
	    int mid = start + size;
	    int end = Math.min(to, mid + size);

	    // The second list is empty or the elements are already in
	    // order - no need to merge
	    if (mid >= end || c.compare(src[mid - 1], src[mid]) <= 0)
	      {
		System.arraycopy(src, start,
				 dest, start + srcDestDiff, end - start);

		// The two halves just need swapping - no need to merge
	      }
	    else if (c.compare(src[start], src[end - 1]) > 0)
	      {
		System.arraycopy(src, start,
				 dest, end - size + srcDestDiff, size);
		System.arraycopy(src, mid,
				 dest, start + srcDestDiff, end - mid);

	      }
	    else
	      {
		// Declare a lot of variables to save repeating
		// calculations.  Hopefully a decent JIT will put these
		// in registers and make this fast
		int p1 = start;
		int p2 = mid;
		int i = start + srcDestDiff;

		// The main merge loop; terminates as soon as either
		// half is ended
		while (p1 < mid && p2 < end)
		  {
		    dest[i++] =
		      src[c.compare(src[p1], src[p2]) <= 0 ? p1++ : p2++];
		  }

		// Finish up by copying the remainder of whichever half
		// wasn't finished.
		if (p1 < mid)
		  System.arraycopy(src, p1, dest, i, mid - p1);
		else
		  System.arraycopy(src, p2, dest, i, end - p2);
	      }
	  }
	// swap src and dest ready for the next merge
	t = src;
	src = dest;
	dest = t;
	from += srcDestDiff;
	to += srcDestDiff;
	srcDestDiff = -srcDestDiff;
		}

    	// make sure the result ends up back in the right place.  Note
	    // that src and dest may have been swapped above, so src 
    	// contains the sorted array.
    	if (src != a)
      	{
			// Note that from == 0.
			System.arraycopy(src, 0, a, srcDestDiff, to);
		}
	}

	/**
	 * Sort the complete argument array according to the argument comparator.
	 * Simply calls <code>sort(a, 0, a.length - 1, comparator);</code>
	 * @param a array to be sorted
	 * @param comparator the comparator used to compare to array entries
	 */
	public static void sort(Object[] a, ComparatorInterface comparator)
	{
		sort(a, 0, a.length - 1, comparator);
	}
}
