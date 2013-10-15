/*
 * GammaCorrection
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.adjustment;

import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.LookupTableOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Corrects the gamma of an image.
 * Works with {@link net.sourceforge.jiu.data.GrayIntegerImage}, 
 * {@link net.sourceforge.jiu.data.RGBIntegerImage} and 
 * {@link net.sourceforge.jiu.data.Paletted8Image}.
 * Only the palette is manipulated for paletted images.
 * <p>
 * Changes intensity values by applying the formula
 * <em>f(x) = MAX * (x / MAX)<sup>(1 / gamma)</sup></em> to each
 * <em>x</em> from <em>[0 ; MAX]</em> to them.
 * The <em>MAX</em> value is the maximum value allowed for an intensity value of the
 * corresponding channel.
 * It is determined by calling {@link net.sourceforge.jiu.data.IntegerImage#getMaxSample} on
 * the input image.
 * The <em>gamma</em> parameter must be given to a <code>GammaCorrection</code> operation
 * before the call to {@link #process} is made.
 * The valid interval for <em>gamma</em> is (0.0 ; {@link #MAX_GAMMA}] 
 * (so 0.0 is not a valid value).
 * Gamma values smaller than 1 will make the image darker, values 
 * larger than 1 will make it brighter.
 * <h3>Usage example</h3>
 * <pre>
 * GammaCorrection gamma = new GammaCorrection();
 * gamma.setInputImage(image);
 * gamma.setGamma(2.2);
 * gamma.process();
 * PixelImage correctedImage = gamma.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class GammaCorrection extends LookupTableOperation
{
	/**
	 * The maximum allowed value for gamma.
	 */
	public static final double MAX_GAMMA = 10.0;
	private double gamma;

	/**
	 * Creates a lookup table that holds all new values for samples 0 to
	 * numSamples - 1.
	 */
	private final int[] createLookupTable(int numSamples)
	{
		if (numSamples < 1)
		{
			throw new IllegalArgumentException("Number of samples argument must be one or larger.");
		}
		double g = 1.0 / gamma;
		int[] result = new int[numSamples];
		final int MAX_SAMPLE = numSamples - 1;
		final double MAX = MAX_SAMPLE;
		for (int i = 0; i < numSamples; i++)
		{
			result[i] = (int)Math.round((MAX * Math.pow((i / MAX), g)));
			if (result[i] < 0)
			{
				result[i] = 0;
			}
			if (result[i] > MAX_SAMPLE)
			{
				result[i] = MAX_SAMPLE;
			}
		}
		return result;
	}

	/**
	 * Returns the gamma value to be used for this operation.
	 * @return gamma value between 0 (not included) and {@link #MAX_GAMMA}
	 */
	public double getGamma()
	{
		return gamma;
	}

	private void process(Paletted8Image in, Paletted8Image out)
	{
		if (out == null)
		{
			out = (Paletted8Image)in.createCompatibleImage(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		Palette palette = out.getPalette();
		int numSamples = palette.getMaxValue() + 1;
		final int[] LUT = createLookupTable(numSamples);
		for (int c = 0; c < 3; c++)
		{
			for (int i = 0; i < palette.getNumEntries(); i++)
			{
				palette.putSample(c, i, LUT[palette.getSample(c, i)]);
			}
		}
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(x, y, in.getSample(x, y));
			}
			setProgress(y, in.getHeight());
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		if (in instanceof Paletted8Image)
		{
			process((Paletted8Image)getInputImage(), (Paletted8Image)getOutputImage());
		}
		else
		if (in instanceof GrayIntegerImage || in instanceof RGBIntegerImage)
		{
			setNumTables(in.getNumChannels());
			IntegerImage ii = (IntegerImage)in;
			for (int channelIndex = 0; channelIndex < in.getNumChannels(); channelIndex++)
			{
				int numSamples = ii.getMaxSample(channelIndex) + 1;
				int[] table = createLookupTable(numSamples);
				setTable(channelIndex, table);
			}
			super.process();
		}
		else
		{
			throw new WrongParameterException("Unsupported image type: " + in.getClass().getName());
		}
	}

	/**
	 * Sets a new gamma value to be used in this operation.
	 * @param newGamma the new gamma value must be &gt; 0.0 and &lt;= MAX_GAMMA
	 * @throws IllegalArgumentException if the argument is not in the described interval
	 */
	public void setGamma(double newGamma)
	{
		if (newGamma <= 0.0)
		{
			throw new IllegalArgumentException("Gamma must be larger than 0.0; got " + newGamma);
		}
		if (newGamma > MAX_GAMMA)
		{
			throw new IllegalArgumentException("Gamma must be at most " + MAX_GAMMA + "; got " + newGamma);
		}
		gamma = newGamma;
	}
}
