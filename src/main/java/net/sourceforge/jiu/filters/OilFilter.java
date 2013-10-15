/*
 * OilFilter
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.filters.AreaFilterOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Applies a filter that makes the image look like an oil painting.
 * This is accomplished by creating a histogram of the neighboring samples
 * for each input sample and storing the value that occurs most often
 * in the output image.
 * If two or more samples occur an equal number of times, the lowest
 * sample value is picked.
 * <h3>Supported image types</h3>
 * Can process both {@link net.sourceforge.jiu.data.GrayIntegerImage} and
 * {@link net.sourceforge.jiu.data.RGBIntegerImage}.
 * Note that this operation becomes very slow with 16 bits per sample
 * because a lot of runs over a 65536 element array are necessary.
 * <h3>Usage example</h3>
 * <pre>
 * PixelImage image = ...; // some GrayIntegerImage or RGBIntegerImage
 * OilFilter filter = new OilFilter();
 * filter.setArea(5, 5);
 * filter.setInputImage(image);
 * filter.process();
 * PixelImage filteredImage = filter.getOutputImage();
 * </pre>
 * <h3>Credits</h3>
 * Idea taken from the <a target="_top" 
 * href="http://www.acme.com/java/software/Acme.JPM.Filters.Oil.html#_top_">
 * Oil class</a> of Jef Poskanzer's <a target="_top"
 * href="http://www.acme.com/java/software/">ACME package</a>.
 * @author Marco Schmidt
 */
public class OilFilter extends AreaFilterOperation
{
	private int[] hist;
	private int[] zeroes;

	public final int computeSample(int[] samples, int numSamples)
	{
		/* for each sample find the neighbor that occurs most often
		   in the area surrounding that pixel specified by 
		   getAreaWidth x getAreaHeight */

		// clear histogram
		System.arraycopy(zeroes, 0, hist, 0, hist.length);

		// initialize histogram
		int index = numSamples;	
		do
		{
			hist[samples[--index]]++;
		}
		while (index != 0);

		// now find the value that occurs most frequently
		int maxIndex = 0;
		int maxValue = hist[0];
		index = 1;
		final int HIST_LENGTH = hist.length;
		while (index != HIST_LENGTH)
		{
			int value = hist[index];
			if (value > maxValue)
			{
				maxIndex = index;
				maxValue = value;
			}
			index++;
		}

		// return value that occurs most frequently
		// if several samples occur most frequently, the smallest is returned
		return maxIndex;
	}

	public void process() throws 
		MissingParameterException,
		WrongParameterException	
	{
		ensureInputImageIsAvailable();
		PixelImage image = getInputImage();
		if (image instanceof IntegerImage)
		{
			IntegerImage ii = (IntegerImage)image;
			int max = ii.getMaxSample(0);
			int index = 1;
			while (index < ii.getNumChannels())
			{
				int maxSample = ii.getMaxSample(index++);
				if (maxSample > max)
				{
					max = maxSample;
				}
			}
			hist = new int[max + 1];
			zeroes = new int[hist.length];
			for (int i = 0; i < zeroes.length; i++)
			{
				zeroes[i] = 0;
			}
		}
		super.process();
	}
}
