/*
 * NormalizeHistogram
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.adjustment;

import net.sourceforge.jiu.color.analysis.Histogram1DCreator;
import net.sourceforge.jiu.color.data.Histogram1D;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * Normalize the image using histogram information, separately for each
 * channel.
 * Works for intensity-based image types like {@link net.sourceforge.jiu.data.Gray8Image} or 
 * {@link net.sourceforge.jiu.data.RGB24Image}.
 *
 * @author Marco Schmidt
 * @since 0.6.0
 */
public class NormalizeHistogram extends LookupTableOperation
{
	/**
	 * Creates an object of this class and initializes the lookup
	 * tables with the argument input image.
	 */
	public NormalizeHistogram(IntegerImage in) throws OperationFailedException
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

			int min = 0;
			while (hist.getEntry(min) == 0)
			{
				min++;
			}
			int maxSample = in.getMaxSample(channelIndex);
			int max = maxSample;
			while (hist.getEntry(max) == 0)
			{
				max--;
			}
			int[] data = new int[maxSample + 1];
			int usedInterval = max - min + 1;
			for (int i = 0; i < data.length; i++)
			{
				data[i] = min + usedInterval * i / data.length;
			}
			setTable(channelIndex, data);
		}
	}
}
