/*
 * HueSaturationValue
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.color.adjustment;

import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Adjusts saturation and value of a color image, optionally hue as well.
 * <p>
 * Supported image types: {@link RGBIntegerImage}, {@link Paletted8Image}.
 * @author Marco Schmidt
 * @since 0.5.0
 */
public class HueSaturationValue extends ImageToImageOperation implements RGBIndex
{
	private float hue;
	private boolean modifyHue;
	private float sMult;
	private boolean sNegative;
	private float vMult;
	private boolean vNegative;

	private final void adjust(int[] orig, int[] adjusted, final float maxSample)
	{
		// get r-g-b as values from 0.0f to 1.0f
		float r = orig[INDEX_RED] / maxSample;
		float g = orig[INDEX_GREEN] / maxSample;
		float b = orig[INDEX_BLUE] / maxSample;
		// (1) compute h-s-v
		float max = Math.max(Math.max(r, g), b);
		float min = Math.min(Math.min(r, g), b);
		float v = max;
		float s;
		if (max != 0.0f)
		{
			s = (max - min) / max;
		}
		else
		{
			s = 0.0f;
		}
		float h;
		if (s == 0.0f)
		{
			h = Float.NaN;
		}
		else
		{
			float delta = max - min;
			if (r == max)
			{
				h = (g - b) / delta;
			}
			else
			if (g == max)
			{
				h = 2.0f + (b - r) / delta;
			}
			else
			{
				h = 4.0f + (r - g) / delta;
			}
			h *= 60.0f;
			if (h < 0.0f)
			{
				h += 360.0f;
			}
		}
		// (2) adjust h-s-v
		if (modifyHue)
		{
			h = hue;
		}
		if (sNegative)
		{
			s *= sMult;
		}
		else
		{
			s += (1.0f - s) * sMult;
		}
		if (vNegative)
		{
			v *= vMult;
		}
		else
		{
			v += (1.0f - v) * vMult;
		}
		// (3) convert back to r-g-b
		if (s == 0.0f)
		{
			if (h == Float.NaN)
			{
				int value = (int)(v * maxSample);
				adjusted[INDEX_RED] = value;
				adjusted[INDEX_GREEN] = value;
				adjusted[INDEX_BLUE] = value;
				return;
			}
			else
			{
				return;
			}
		}
		if (h == 360.0f)
		{
			h = 0.0f;
		}
		h /= 60.0f;
		int i = (int)Math.floor(h);
		float f = h - i;
		float p = v * (1 - s);
		float q = v * (1 - (s * f));
		float t = v * (1 - (s * (1 - f)));
		switch(i)
		{
			case(0):
			{
				adjusted[INDEX_RED] = (int)(v * maxSample);
				adjusted[INDEX_GREEN] = (int)(t * maxSample);
				adjusted[INDEX_BLUE] = (int)(p * maxSample);
				break;
			}
			case(1):
			{
				adjusted[INDEX_RED] = (int)(q * maxSample);
				adjusted[INDEX_GREEN] = (int)(v * maxSample);
				adjusted[INDEX_BLUE] = (int)(p * maxSample);
				break;
			}
			case(2):
			{
				adjusted[INDEX_RED] = (int)(p * maxSample);
				adjusted[INDEX_GREEN] = (int)(v * maxSample);
				adjusted[INDEX_BLUE] = (int)(t * maxSample);
				break;
			}
			case(3):
			{
				adjusted[INDEX_RED] = (int)(p * maxSample);
				adjusted[INDEX_GREEN] = (int)(q * maxSample);
				adjusted[INDEX_BLUE] = (int)(v * maxSample);
				break;
			}
			case(4):
			{
				adjusted[INDEX_RED] = (int)(t * maxSample);
				adjusted[INDEX_GREEN] = (int)(p * maxSample);
				adjusted[INDEX_BLUE] = (int)(v * maxSample);
				break;
			}
			case(5):
			{
				adjusted[INDEX_RED] = (int)(v * maxSample);
				adjusted[INDEX_GREEN] = (int)(p * maxSample);
				adjusted[INDEX_BLUE] = (int)(q * maxSample);
				break;
			}
		}
	}

	private void process(Paletted8Image in, Paletted8Image out)
	{
		Palette inPal = in.getPalette();
		Palette outPal = out.getPalette();
		int[] orig = new int[3];
		int[] adjusted = new int[3];
		final int MAX = inPal.getMaxValue();
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		for (int i = 0; i < inPal.getNumEntries(); i++)
		{
			orig[INDEX_RED] = inPal.getSample(INDEX_RED, i);
			orig[INDEX_GREEN] = inPal.getSample(INDEX_GREEN, i);
			orig[INDEX_BLUE] = inPal.getSample(INDEX_BLUE, i);
			adjust(orig, adjusted, MAX);
			outPal.putSample(INDEX_RED, i, adjusted[INDEX_RED]);
			outPal.putSample(INDEX_GREEN, i, adjusted[INDEX_GREEN]);
			outPal.putSample(INDEX_BLUE, i, adjusted[INDEX_BLUE]);
		}
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				out.putSample(0, x, y, in.getSample(0, x, y));
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(RGBIntegerImage in, RGBIntegerImage out)
	{
		final int MAX = in.getMaxSample(0);
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		int[] orig = new int[3];
		int[] adjusted = new int[3];
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				orig[INDEX_RED] = in.getSample(INDEX_RED, x, y);
				orig[INDEX_GREEN] = in.getSample(INDEX_GREEN, x, y);
				orig[INDEX_BLUE] = in.getSample(INDEX_BLUE, x, y);
				adjust(orig, adjusted, MAX);
				out.putSample(INDEX_RED, x, y, adjusted[INDEX_RED]);
				out.putSample(INDEX_GREEN, x, y, adjusted[INDEX_GREEN]);
				out.putSample(INDEX_BLUE, x, y, adjusted[INDEX_BLUE]);
			}
			setProgress(y, HEIGHT);
		}
	}

	public void process() throws 
		MissingParameterException, 
		WrongParameterException
	{
		PixelImage in = getInputImage();
		if (in == null)
		{
			throw new MissingParameterException("Input image missing.");
		}
		PixelImage out = getOutputImage();
		if (out == null)
		{
			out = in.createCompatibleImage(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		if (in instanceof RGBIntegerImage)
		{
			process((RGBIntegerImage)in, (RGBIntegerImage)out);
		}
		else
		if (in instanceof Paletted8Image)
		{
			process((Paletted8Image)in, (Paletted8Image)out);
		}
		else
		{
			throw new WrongParameterException("Input image type not supported.");
		}
	}

	/**
	 * Set the values for the adjustment of hue, saturation and value (brightness).
	 * Saturation and value must be from the interval -100 to 100 (also see {@link #setSaturationValue(int, int)}).
	 * Hue must be from the interval 0 to 359.
	 * @param hue the hue to be used for the complete image, between 0 and 359
	 * @param saturation change of saturation, between -100 and 100
	 * @param value change of saturation, between -100 and 100
	 * @throws IllegalArgumentException if one of the arguments does not stay within
	 *  the valid interval
	 */
	public void setHueSaturationValue(int hue, int saturation, int value)
	{
		if (hue < 0 || hue >= 360)
		{
			throw new IllegalArgumentException("Hue must be from 0..359; got " + hue);
		}
		modifyHue = true;
		this.hue = hue;
		setSv(saturation, value);
	}

	/**
	 * Set the amount of change to saturation and value (brightness) for this operation,
	 * between -100 and 100.
	 * Calling this method also tells the operation not to modify the hue of the image.
	 * @param saturation change of saturation, between -100 and 100
	 * @param value change of saturation, between -100 and 100
	 * @throws IllegalArgumentException if one of the two arguments does not stay within
	 *  the -100 .. 100 interval
	 */
	public void setSaturationValue(int saturation, int value)
	{
		modifyHue = false;
		setSv(saturation, value);
	}

	private void setSv(int saturation, int value)
	{
		if (saturation < -100 || saturation > 100)
		{
			throw new IllegalArgumentException("Saturation must be from -100..100; got " + saturation);
		}
		sNegative = (saturation < 0);
		if (sNegative)
		{
			sMult = (100.0f + saturation) / 100.0f;
		}
		else
		if (saturation > 0)
		{
			sMult = ((float)saturation) / 100.0f;
		}
		else
		{
			sMult = 0.0f;
		}
		if (value < -100 || value > 100)
		{
			throw new IllegalArgumentException("Saturation must be from -100..100; got " + value);
		}
		vNegative = (value < 0);
		if (vNegative)
		{
			vMult = (100.0f + value) / 100.0f;
		}
		else
		if (value > 0)
		{
			vMult = ((float)value) / 100.0f;
		}
		else
		{
			vMult = 0.0f;
		}
	}
}
