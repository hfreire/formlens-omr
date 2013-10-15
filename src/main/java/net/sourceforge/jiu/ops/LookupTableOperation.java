/*
 * LookupTableOperation
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * An operation that replaces samples with values taken from a lookup table.
 * Operations where each pixel is treated independently from its neighbors
 * and where a pixel value is always mapped to the same new pixel value
 * can be implemented this way.
 *
 * @author Marco Schmidt
 * @since 0.6.0
 */
public abstract class LookupTableOperation extends ImageToImageOperation
{
	private int[][] intTables;
	private int numTables;

	/**
	 * Creates a LookupTableOperation for one lookup table.
	 */
	public LookupTableOperation()
	{
		this(1);
	}

	/**
	 * Creates an object of this class, calling the super constructor with two <code>null</code>
	 * arguments and allocates space for the argument number of lookup tables.
	 * @param numTables number of tables to be used in this operation
	 */
	public LookupTableOperation(int numTables)
	{
		super(null, null);
		if (numTables < 1)
		{
			throw new IllegalArgumentException("The number of tables must be at least 1; got " + numTables);
		}
		intTables = new int[numTables][];
		this.numTables = numTables;
	}

	/**
	 * Returns the number of tables in this operation.
	 * @return number of tables
	 */
	public int getNumTables()
	{
		return numTables;
	}

	/**
	 * Returns one of the internal <code>int</code> lookup tables.
	 * @param channelIndex the zero-based index of the table to be returned;
	 *   from 0 to getNumTables() - 1
	 * @return the channelIndex'th table
	 */
	public int[] getTable(int channelIndex)
	{
		return intTables[channelIndex];
	}

	public void prepareImages() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		if (!(in instanceof IntegerImage))
		{
			throw new WrongParameterException("Input image must be of type IntegerImage.");
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			out = in.createCompatibleImage(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		else
		{
			if (in.getNumChannels() != out.getNumChannels())
			{
				throw new WrongParameterException("Output image must have same number of channels as input image.");
			}
			ensureImagesHaveSameResolution();
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		prepareImages();
		process((IntegerImage)getInputImage(), (IntegerImage)getOutputImage());
	}

	private void process(IntegerImage in, IntegerImage out)
	{
		boolean useFirstTableOnly = getNumTables() < in.getNumChannels();
		final int TOTAL_ITEMS = in.getHeight() * in.getNumChannels();
		int processedItems = 0;
		for (int channelIndex = 0; channelIndex < in.getNumChannels(); channelIndex++)
		{
			int tableIndex;
			if (useFirstTableOnly)
			{
				tableIndex = 0;
			}
			else
			{
				tableIndex = channelIndex;
			}
			process(in, out, channelIndex, tableIndex, processedItems, TOTAL_ITEMS);
			processedItems += in.getHeight();
		}
	}

	private void process(IntegerImage in, IntegerImage out, final int CHANNEL_INDEX,
		int tableIndex, int processedItems, final int TOTAL_ITEMS)
	{
		final int[] TABLE = getTable(tableIndex);
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				out.putSample(CHANNEL_INDEX, x, y, TABLE[in.getSample(CHANNEL_INDEX, x, y)]);
			}
			setProgress(processedItems++, TOTAL_ITEMS);
		}
	}

	/**
	 * Resets the number of tables to be used in this operation to the 
	 * argument and drops all actual table data initialized so far.
	 * After a call to this method, {@link #getTable} will return
	 * <code>null</code> as long as no new table data is provided
	 * via {@link #setTable} or {@link #setTables}.
	 * @param numberOfTables the new number of tables for this operation, must be <code>1</code> or larger
	 * @throws IllegalArgumentException if the number is zero or smaller
	 */
	public void setNumTables(int numberOfTables)
	{
		if (numberOfTables < 1)
		{
			throw new IllegalArgumentException("Number of tables argument must be larger than zero.");
		}
		numTables = numberOfTables;
		intTables = new int[numTables][];
	}

	/**
	 * Provides a new lookup table for one of the channels.
	 * @param channelIndex the index of the channel for which a table is provided; must be at least <code>0</code> and smaller than {@link #getNumTables}
	 * @param tableData the actual table to be used for lookup
	 * @throws IllegalArgumentException if the channel index is not in the valid interval (see above)
	 */
	public void setTable(int channelIndex, int[] tableData)
	{
		if (channelIndex < 0)
		{
			throw new IllegalArgumentException("The channelIndex argument must be at least 0; got " + channelIndex);
		}
		if (channelIndex >= getNumTables())
		{
			throw new IllegalArgumentException("The channelIndex argument must be smaller than the number of tables " + 
				getNumTables() + "; got " + channelIndex);
		}
		intTables[channelIndex] = tableData;
	}

	/**
	 * Sets the tables for all channels to the argument table.
	 * Useful when the same table can be used for all channels.
	 * @param tableData the data that will be used as lookup table for all channels
	 */
	public void setTables(int[] tableData)
	{
		for (int i = 0; i < getNumTables(); i++)
		{
			setTable(i, tableData);
		}
	}
}
