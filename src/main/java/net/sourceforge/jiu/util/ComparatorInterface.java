/*
 * ComparatorInterface
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

/**
 * To be able to do sorting in Java 1.1 as defined in java.util.Arrays (which
 * is only available in Java 1.2 and higher), we offer a java.util.Comparator 
 * clone under a different name: ComparatorInterface.
 * Sorting will be provided by the {@link Sort} class of this package.
 */
public interface ComparatorInterface
{
	/**
	 * Compares the two argument objects and returns their relation.
	 * Returns 
	 * <ul>
	 * <li>a value &lt; 0 if <code>o1</code> is smaller than <code>o2</code>,</li>
	 * <li>0 if <code>o1</code> is equal to <code>o2</code> and</li>
	 * <li>a value &gt; 0 if <code>o1</code> is greater than <code>o2</code>.</li>
	 * </ul>
	 */
	int compare(Object o1, Object o2);
}
