/*
 * HistogramSerialization
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.io;

import java.io.PrintStream;
import net.sourceforge.jiu.color.data.Histogram1D;
import net.sourceforge.jiu.color.data.Histogram3D;

/**
 * This class has static methods for saving histograms.
 * Text files (actually, any PrintStream, so you could write to standard output 
 * using {@link java.lang.System#out}) are used to store the histogram information.
 * Hint: When creating a {@link java.io.PrintStream} object yourself, set the <code>autoFlush</code>
 * argument of the constructor to <code>false</code>.
 * You should also wrap your {@link java.io.OutputStream} object into a {@link java.io.BufferedOutputStream} object.
 * That may speed things up.
 * <p>
 * A simple format is used for storing the histograms.
 * The first line holds the number of components.
 * This would be 3 for a three-dimensional histogram, e.g.for RGB color images,
 * or 1 for a one-dimensional histogram as used for a grayscale image.
 * <p>
 * Next, as many lines as dimensions follow.
 * Each line holds the maximum value allowed for that component.
 * The minimum value is always zero.
 * Typically, the maximum values are all the same, e.g. 255 for each
 * component of a 24 bit RGB truecolor image.
 * <p>
 * Following these header lines is the actual histogram.
 * Each line holds a non-zero counter value for one pixel.
 * The counter is always the last integer value in the line.
 * <p>
 * Example:
 * <pre>
 * 34 0 55 4033
 * </pre>
 * For the histogram of an RGB24Image, this would mean that the pixel
 * red=34, green=0, blue=55 occurs 4033 times.
 * <pre>
 * 0 2
 * </pre>
 * For the histogram of any one channel image, this means that the value 0 occurs twice.
 */
public class HistogramSerialization
{
	private HistogramSerialization()
	{
	}

	/**
	 * Saves a one-dimensional histogram to a text output stream.
	 *
	 * @param hist the histogram that will be written to a stream
	 * @param out the stream that will be written to
	 */
	public static void save(Histogram1D hist, PrintStream out)
	{
		if (hist == null || out == null)
		{
			return;
		}
		int max = hist.getMaxValue();
		out.println("1");
		out.println(max);
		for (int i = 0; i <= max; i++)
		{
			int counter = hist.getEntry(i);
			if (counter != 0)
			{
				out.print(i);
				out.print(' ');
				out.println(counter);
			}
		}
		out.flush();
	}

	/**
	 * Saves a three-dimensional histogram to a text output stream.
	 *
	 * @param hist the histogram to be saved
	 * @param out the output stream where the histogram will be saved to
	 */
	public static void save(Histogram3D hist, PrintStream out)
	{
		if (hist == null || out == null)
		{
			return;
		}
		int max1 = hist.getMaxValue(0);
		int max2 = hist.getMaxValue(1);
		int max3 = hist.getMaxValue(2);
		out.println("3");
		out.println(max1);
		out.println(max2);
		out.println(max3);
		for (int i = 0; i <= max1; i++)
		{
			for (int j = 0; j <= max2; j++)
			{
				for (int k = 0; k <= max3; k++)
				{
					int counter = hist.getEntry(i, j, k);
					if (counter != 0)
					{
						out.print(i);
						out.print(' ');
						out.print(j);
						out.print(' ');
						out.print(k);
						out.print(' ');
						out.println(counter);
					}
				}
			}
		}
		out.flush();
	}
}
