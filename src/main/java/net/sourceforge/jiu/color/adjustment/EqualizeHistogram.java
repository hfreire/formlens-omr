/*
 * EqualizeHistogram
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.adjustment;

import net.sourceforge.jiu.color.analysis.Histogram1DCreator;
import net.sourceforge.jiu.color.data.Histogram1D;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * Equalize the image using histogram information separately for each channel.
 * Works for intensity-based image types like {@link net.sourceforge.jiu.data.Gray8Image} or 
 * {@link net.sourceforge.jiu.data.RGB24Image}.
 *
 * @author Marco Schmidt
 * @since 0.6.0
 */
public class EqualizeHistogram extends LookupTableOperation
{
	/**
	 * Creates an object of this class and initializes the lookup
	 * tables with the argument input image.
	 * @param in the input image
	 */
	public EqualizeHistogram(IntegerImage in) throws OperationFailedException
	{
		super(in.getNumChannels());
		setInputImage(in);
		initTables(in);
	}

	private void initTables(IntegerImage in) throws OperationFailedException
	{
		for (int channelIndex = 0; channelIndex < in.getNumChannels(); channelIndex++)
		{
			Histogram1DCreator hc = new Histogram1DCreator();
			hc.setImage(in, channelIndex);
			hc.process();
			Histogram1D hist = hc.getHistogram();

			final int MAX_SAMPLE = in.getMaxSample(channelIndex);
			int[] data = new int[MAX_SAMPLE + 1];
			int NUM_PIXELS = in.getWidth() * in.getHeight();
			long sum = 0;
			for (int i = 0; i < data.length; i++)
			{
				sum += hist.getEntry(i);
				long result = sum * MAX_SAMPLE / NUM_PIXELS; 
				if (result > (long)Integer.MAX_VALUE)
				{
					throw new IllegalStateException("Result does not fit into an int.");
				}
				data[i] = (int)result;
			}
			setTable(channelIndex, data);
		}
	}
}
