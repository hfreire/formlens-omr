/*
 * OrderedDither
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.dithering;

import net.sourceforge.jiu.color.quantization.UniformPaletteQuantizer;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * This operation reduces the color depth of RGB truecolor images and grayscale images
 * by applying ordered dithering.
 * A relatively nice output image (for a human observer) will be created at the 
 * cost of introducing noise into the output image.
 * The algorithm is relatively fast, but its quality is usually inferior to what
 * can be reached by using {@link ErrorDiffusionDithering} or similar other methods.
 * <h3>Supported conversions</h3>
 * <ul>
 * <li><strong>Grayscale to bilevel</strong> maps GrayIntegerImage objects
 *  to BilevelImage objects.</li>
 * <li><strong>Grayscale to grayscale</strong> maps GrayIntegerImage objects
 *  to other GrayIntegerImage objects.
 *  Right now, only Gray8Image objects are supported.
 *  After reducing the number of bits, each sample is immediately scaled back
 *  to 8 bits per pixel in order to fit into another Gray8Image object.</li>
 * <li><strong>Truecolor to paletted</strong> maps RGBIntegerImage objects
 *  to Paletted8Image objects; the sum of the output bits must not be larger
 *  than 8</li>
 * </ul>
 * <h3>Usage example</h3>
 * The following code snippet demonstrates how to create a paletted
 * image with 256 colors, using three bits for red and green and two
 * bits for blue, from an RGB truecolor image.
 * <pre>
 * OrderedDither od = new OrderedDither();
 * od.setRgbBits(3, 3, 2);
 * od.setInputImage(image);
 * od.process();
 * Paletted8Image ditheredImage = (Paletted8Image)od.getOutputImage();
 * </pre>
 * @author Marco Schmidt
 */
public class OrderedDither extends ImageToImageOperation implements RGBIndex
{
	private int[] values;
	private int valueWidth;
	private int valueHeight;
	private int grayBits = 3;
	private int redBits = 3;
	private int greenBits = 3;
	private int blueBits = 2;

	private void process(Gray8Image in, Gray8Image out)
	{
		if (out == null)
		{
			out = new MemoryGray8Image(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		int D1 = 4;
		int D2 = 4;
		int D1D2 = D1 * D2;
		final int[] DITHER_MATRIX = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};
		final int SPACE = 255 / ((1 << grayBits) - 1);
		final int SHIFT = 8 - grayBits;
		final int NUM_VALUES = 1 << grayBits;
		final byte[] OUTPUT_SAMPLES = new byte[NUM_VALUES];
		for (int i = 0; i < OUTPUT_SAMPLES.length; i++)
		{
			OUTPUT_SAMPLES[i] = (byte)((i * 255) / NUM_VALUES);
		}
		final int[] DITHER_SIGNAL = new int[D1D2];
		for (int i = 0; i < D1D2; i++)
		{
			DITHER_SIGNAL[i] = (2 * DITHER_MATRIX[i] - D1D2 + 1) * SPACE / (2 * D1D2);
		}
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		int rowOffset = 0;
		for (int y = 0; y < HEIGHT; y++)
		{
			int offset = rowOffset;
			final int MAX_OFFSET = rowOffset + D1;
			for (int x = 0; x < WIDTH; x++)
			{
				int sample = in.getSample(0, x, y);
				sample = sample + DITHER_SIGNAL[ offset++ ];
				if (offset == MAX_OFFSET)
				{
					offset = rowOffset;
				}
				if (sample < 0)
				{
					sample = 0;
				}
				else
				if (sample > 255)
				{
					sample = 255;
				}
				out.putByteSample(0, x, y, OUTPUT_SAMPLES[sample >> SHIFT]);
			}
			rowOffset += D1;
			if (rowOffset >= DITHER_SIGNAL.length)
			{
				rowOffset = 0;
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(Gray8Image in, BilevelImage out)
	{
		if (out == null)
		{
			out = new MemoryBilevelImage(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		if (values == null)
		{
			setStandardThresholdValues();
		}
		out.clear(BilevelImage.BLACK);
		int rowOffset = 0;
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			int offset = rowOffset;
			final int MAX_OFFSET = rowOffset + valueWidth;
			for (int x = 0; x < in.getWidth(); x++)
			{
				if (in.getSample(x, y) >= values[offset++])
				{
					out.putWhite(x, y);
				}
				if (offset == MAX_OFFSET)
				{
					offset = rowOffset;
				}
			}
			setProgress(y, HEIGHT);
			rowOffset += valueWidth;
			if (rowOffset >= values.length)
			{
				rowOffset = 0;
			}
		}
	}

	private void process(RGB24Image in, Paletted8Image out)
	{
		UniformPaletteQuantizer upq = new UniformPaletteQuantizer(redBits, greenBits, blueBits);
		if (out == null)
		{
			out = new MemoryPaletted8Image(in.getWidth(), in.getHeight(), upq.createPalette());
			setOutputImage(out);
		}
		int D1 = 4;
		int D2 = 4;
		int D1D2 = D1 * D2;
		final int[] DITHER_MATRIX = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};
		final int RED_SPACE = 255 / ((1 << redBits) - 1);
		final int GREEN_SPACE = 255 / ((1 << greenBits) - 1);
		final int BLUE_SPACE = 255 / ((1 << blueBits) - 1);
		/*final int RED_SHIFT = 8 - redBits;
		final int GREEN_SHIFT = 8 - redBits;
		final int BLUE_SHIFT = 8 - redBits;
		final int NUM_RED_VALUES = 1 << redBits;
		final int NUM_GREEN_VALUES = 1 << greenBits;
		final int NUM_BLUE_VALUES = 1 << blueBits;*/
		final int[] RED_DITHER_SIGNAL = new int[D1D2];
		final int[] GREEN_DITHER_SIGNAL = new int[D1D2];
		final int[] BLUE_DITHER_SIGNAL = new int[D1D2];
		for (int i = 0; i < D1D2; i++)
		{
			RED_DITHER_SIGNAL[i] = (2 * DITHER_MATRIX[i] - D1D2 + 1) * RED_SPACE / (2 * D1D2);
			GREEN_DITHER_SIGNAL[i] = (2 * DITHER_MATRIX[i] - D1D2 + 1) * GREEN_SPACE / (2 * D1D2);
			BLUE_DITHER_SIGNAL[i] = (2 * DITHER_MATRIX[i] - D1D2 + 1) * BLUE_SPACE / (2 * D1D2);
		}
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		int rowOffset = 0;
		for (int y = 0; y < HEIGHT; y++)
		{
			int offset = rowOffset;
			final int MAX_OFFSET = rowOffset + D1;
			for (int x = 0; x < WIDTH; x++)
			{
				int redSample = in.getSample(INDEX_RED, x, y);
				redSample = redSample + RED_DITHER_SIGNAL[ offset ];
				if (redSample < 0)
				{
					redSample = 0;
				}
				else
				if (redSample > 255)
				{
					redSample = 255;
				}
				int greenSample = in.getSample(INDEX_GREEN, x, y);
				greenSample = greenSample + GREEN_DITHER_SIGNAL[ offset ];
				if (greenSample < 0)
				{
					greenSample = 0;
				}
				else
				if (greenSample > 255)
				{
					greenSample = 255;
				}
				int blueSample = in.getSample(INDEX_BLUE, x, y);
				blueSample = blueSample + BLUE_DITHER_SIGNAL[ offset ];
				if (blueSample < 0)
				{
					blueSample = 0;
				}
				else
				if (blueSample > 255)
				{
					blueSample = 255;
				}
				out.putSample(0, x, y, upq.mapToIndex(redSample, greenSample, blueSample));
				offset++;
				if (offset == MAX_OFFSET)
				{
					offset = rowOffset;
				}
			}
			rowOffset += D1;
			if (rowOffset >= DITHER_MATRIX.length)
			{
				rowOffset = 0;
			}
			setProgress(y, HEIGHT);
		}
	}

	private void process(RGB24Image in, RGB24Image out)
	{
		//UniformPaletteQuantizer upq = new UniformPaletteQuantizer(redBits, greenBits, blueBits);
		//System.out.println("RGB=>RGB, r=" + redBits+  ", g=" + greenBits + ", b=" + blueBits);
		if (out == null)
		{
			out = new MemoryRGB24Image(in.getWidth(), in.getHeight());
			setOutputImage(out);
		}
		int D1 = 4;
		int D2 = 4;
		int D1D2 = D1 * D2;
		final int[] DITHER_MATRIX = {0, 8, 2, 10, 12, 4, 14, 6, 3, 11, 1, 9, 15, 7, 13, 5};
		final int RED_SPACE = 255 / ((1 << redBits) - 1);
		final int GREEN_SPACE = 255 / ((1 << greenBits) - 1);
		final int BLUE_SPACE = 255 / ((1 << blueBits) - 1);
		final int RED_SHIFT = 8 - redBits;
		final int GREEN_SHIFT = 8 - greenBits;
		final int BLUE_SHIFT = 8 - blueBits;
		final int MAX_RED = (1 << redBits) - 1;
		final int MAX_GREEN = (1 << greenBits) - 1;
		final int MAX_BLUE = (1 << blueBits) - 1;
		/*final int NUM_RED_VALUES = 1 << redBits;
		final int NUM_GREEN_VALUES = 1 << greenBits;
		final int NUM_BLUE_VALUES = 1 << blueBits;*/
		final int[] RED_DITHER_SIGNAL = new int[D1D2];
		final int[] GREEN_DITHER_SIGNAL = new int[D1D2];
		final int[] BLUE_DITHER_SIGNAL = new int[D1D2];
		for (int i = 0; i < D1D2; i++)
		{
			RED_DITHER_SIGNAL[i] = (2 * DITHER_MATRIX[i] - D1D2 + 1) * RED_SPACE / (2 * D1D2);
			GREEN_DITHER_SIGNAL[i] = (2 * DITHER_MATRIX[i] - D1D2 + 1) * GREEN_SPACE / (2 * D1D2);
			BLUE_DITHER_SIGNAL[i] = (2 * DITHER_MATRIX[i] - D1D2 + 1) * BLUE_SPACE / (2 * D1D2);
		}
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		int rowOffset = 0;
		for (int y = 0; y < HEIGHT; y++)
		{
			int offset = rowOffset;
			final int MAX_OFFSET = rowOffset + D1;
			for (int x = 0; x < WIDTH; x++)
			{
				// RED
				int redSample = in.getSample(INDEX_RED, x, y);
				redSample = redSample + RED_DITHER_SIGNAL[ offset ];
				if (redSample < 0)
				{
					redSample = 0;
				}
				else
				if (redSample > 255)
				{
					redSample = 255;
				}
				redSample >>= RED_SHIFT;
				out.putSample(RGBIndex.INDEX_RED, x, y, redSample * 255 / MAX_RED);
				// GREEN
				int greenSample = in.getSample(INDEX_GREEN, x, y);
				greenSample = greenSample + GREEN_DITHER_SIGNAL[ offset ];
				if (greenSample < 0)
				{
					greenSample = 0;
				}
				else
				if (greenSample > 255)
				{
					greenSample = 255;
				}
				greenSample >>= GREEN_SHIFT;
				out.putSample(RGBIndex.INDEX_GREEN, x, y, greenSample * 255 / MAX_GREEN);
				// BLUE
				int blueSample = in.getSample(INDEX_BLUE, x, y);
				blueSample = blueSample + BLUE_DITHER_SIGNAL[offset];
				if (blueSample < 0)
				{
					blueSample = 0;
				}
				else
				if (blueSample > 255)
				{
					blueSample = 255;
				}
				blueSample >>= BLUE_SHIFT;
				out.putSample(RGBIndex.INDEX_BLUE, x, y, blueSample * 255 / MAX_BLUE);
				//out.putSample(0, x, y, upq.mapToIndex(redSample, greenSample, blueSample));
				offset++;
				if (offset == MAX_OFFSET)
				{
					offset = rowOffset;
				}
			}
			rowOffset += D1;
			if (rowOffset >= DITHER_MATRIX.length)
			{
				rowOffset = 0;
			}
			setProgress(y, HEIGHT);
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		PixelImage out = getOutputImage();
		if (in instanceof RGB24Image)
		{
			int sum = redBits + greenBits + blueBits;
			if (sum > 8)
			{
				process((RGB24Image)in, (RGB24Image)out);
			}
			else
			{
				process((RGB24Image)in, (Paletted8Image)out);
			}
		}
		else
		if (grayBits == 1)
		{
			process((Gray8Image)in, (BilevelImage)out);
		}
		else
		if (grayBits >= 2 && grayBits <= 7)
		{
			process((Gray8Image)in, (Gray8Image)out);
		}
	}

	public void setOutputBits(int bits)
	{
		if (bits >= 1 && bits <= 7)
		{
			grayBits = bits;
		}
		else
		{
			throw new IllegalArgumentException("Grayscale output bits must be from 1..7; got " + bits);
		}
	}

	/**
	 * Sets the number of bits to be used for each RGB component in the output image.
	 * Each argument must be one or larger.
	 * The values defined by this method are only used if the input image implements
	 * RGBIntegerImage.
	 * Later, in {@link #process}, these values are checked against
	 * the actual number of bits per component in the input image.
	 * If any of the arguments of this method is equal to or larger 
	 * than those actual bits per channel values, a WrongParameterException
	 * will then be thrown.
	 * Right now, there is no way how this can be checked, because
	 * the input image may not have been defined yet.
	 * @param red number of bits for the red channel in the output image
	 * @param green number of bits for the green channel in the output image
	 * @param blue number of bits for the blue channel in the output image
	 * @throws IllegalArgumentException if at least one argument is smaller than <code>1</code>
	 */
	public void setRgbBits(int red, int green, int blue)
	{
		if (red > 0 && green > 0 && blue > 0)
		{
			redBits = red;
			greenBits = green;
			blueBits = blue;
		}
		else
		{
			throw new IllegalArgumentException("All parameters must be 1 or larger.");
		}
	}

	/**
	 * Calls {@link #setThresholdValues} with a 16 x 16 matrix.
	 */
	public void setStandardThresholdValues()
	{
		final int[] VALUES =
		{
		   0,192, 48,240, 12,204, 60,252,  3,195, 51,243, 15,207, 63,255,
 		 128, 64,176,112,140, 76,188,124,131, 67,179,115,143, 79,191,127,
  		  32,224, 16,208, 44,236, 28,220, 35,227, 19,211, 47,239, 31,223,
 		 160, 96,144, 80,172,108,156, 92,163, 99,147, 83,175,111,159, 95,
   		   8,200, 56,248,  4,196, 52,244, 11,203, 59,251,  7,199, 55,247,
 		 136, 72,184,120,132, 68,180,116,139, 75,187,123,135, 71,183,119,
  		  40,232, 24,216, 36,228, 20,212, 43,235, 27,219, 39,231, 23,215,
 		 168,104,152, 88,164,100,148, 84,171,107,155, 91,167,103,151, 87,
   		   2,194, 50,242, 14,206, 62,254,  1,193, 49,241, 13,205, 61,253,
 		 130, 66,178,114,142, 78,190,126,129, 65,177,113,141, 77,189,125,
  		  34,226, 18,210, 46,238, 30,222, 33,225, 17,209, 45,237, 29,221,
		 162, 98,146, 82,174,110,158, 94,161, 97,145, 81,173,109,157, 93,
  		  10,202, 58,250,  6,198, 54,246,  9,201, 57,249,  5,197, 53,245,
 		 138, 74,186,122,134, 70,182,118,137, 73,185,121,133, 69,181,117,
  		  42,234, 26,218, 38,230, 22,214, 41,233, 25,217, 37,229, 21,213,
 		 170,106,154, 90,166,102,150, 86,169,105,153, 89,165,101,149, 85
		};
		setThresholdValues(VALUES, 16, 16);
	}

	/**
	 * Defines a matrix of threshold values that will be used for grayscale
	 * dithering.
	 * @param values the int values to use for comparing
	 * @param valueWidth 
	 * @param valueHeight
	 */
	public void setThresholdValues(int[] values, int valueWidth, int valueHeight)
	{
		if (values == null)
		{
			throw new IllegalArgumentException("The value array must be non-null.");
		}
		if (valueWidth < 1)
		{
			throw new IllegalArgumentException("The width argument must be at least 1.");
		}
		if (valueHeight < 1)
		{
			throw new IllegalArgumentException("The height argument must be at least 1.");
		}
		this.values = values;
		this.valueWidth = valueWidth;
		this.valueHeight = valueHeight;
		if (this.valueHeight * this.valueWidth < this.values.length)
		{
			throw new IllegalArgumentException("The array must have at least valuesWidth * valuesHeight elements..");
		}
	}
}
