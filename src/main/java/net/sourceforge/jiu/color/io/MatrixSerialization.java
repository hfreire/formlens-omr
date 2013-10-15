/*
 * MatrixSerialization
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.io;

import java.io.PrintStream;
import net.sourceforge.jiu.color.data.CoOccurrenceMatrix;
import net.sourceforge.jiu.color.data.CoOccurrenceFrequencyMatrix;

/**
 * Write co-occurrence and co-occurrence frequency matrices to text files.
 *
 * @author Marco Schmidt
 * @since 0.6.0
 */
public class MatrixSerialization
{
	private MatrixSerialization()
	{
	}

	public static void save(CoOccurrenceMatrix matrix, PrintStream out)
	{
		if (matrix == null || out == null)
		{
			return;
		}
		int dim = matrix.getDimension();
		out.println(Integer.toString(dim));
		for (int i = 0; i < dim; i++)
		{
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < dim; j++)
			{
				sb.append(matrix.getValue(i, j));
				sb.append(' ');
			}
			out.println(sb.toString());
		}
	}

	public static void save(CoOccurrenceFrequencyMatrix matrix, PrintStream out)
	{
		if (matrix == null || out == null)
		{
			return;
		}
		int dim = matrix.getDimension();
		out.println(Integer.toString(dim));
		for (int i = 0; i < dim; i++)
		{
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < dim; j++)
			{
				sb.append(matrix.getValue(i, j));
				sb.append(' ');
			}
			out.println(sb.toString());
		}
	}
}
