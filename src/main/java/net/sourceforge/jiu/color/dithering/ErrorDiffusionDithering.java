/*
 * ErrorDiffusionDithering
 * 
 * Copyright (c) 2001, 2002, 2003, 2004 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.dithering;

import net.sourceforge.jiu.color.quantization.RGBQuantizer;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * This class is used to apply error diffusion dithering to images that are being reduced in their color depth.
 * Works with {@link net.sourceforge.jiu.data.GrayIntegerImage} and
 * {@link net.sourceforge.jiu.data.RGBIntegerImage} objects.
 * For RGB images, a quantizer must be specified via {@link #setQuantizer}.
 * That quantizer must have been initialized (it must have searched for / given a palette that it can map to).
 * <p>
 * This class offers six predefined types of error diffusion dithering.
 * In addition, user-defined types can be integrated by providing a
 * information on how the error is to be distributed; see the 
 * description of {@link #setTemplateData}.
 *
 * <h3>Usage examples</h3>
 * <h4>Color</h4>
 * This small program maps some RGB24Image object to a Paletted8Image
 * with 120 entries in its palette, using Stucki error
 * diffusion dithering in combination with an octree color quantizer.
 * <pre>
 * MemoryRGB24Image image = ...; // some RGB image
 * OctreeColorQuantizer quantizer = new OctreeColorQuantizer();
 * quantizer.setInputImage(image);
 * quantizer.setPaletteSize(120);
 * quantizer.init();
 * ErrorDiffusionDithering edd = new ErrorDiffusionDithering();
 * edd.setTemplateType(ErrorDiffusionDithering.TYPE_STUCKI);
 * edd.setQuantizer(quantizer);
 * edd.setInputImage(image);
 * edd.process();
 * PixelImage quantizedImage = edd.getOutputImage();
 * </pre>
 * <h4>Grayscale to black and white</h4>
 * In this example, a {@link net.sourceforge.jiu.data.Gray8Image} object
 * is reduced to black and white using Floyd-Steinberg dithering.
 * <pre>
 * Gray8Image image = ...; // some grayscale image
 * ErrorDiffusionDithering edd = new ErrorDiffusionDithering();
 * edd.setGrayscaleOutputBits(1);
 * edd.setInputImage(image);
 * edd.process();
 * PixelImage ditheredImage = edd.getOutputImage();
 * // if you need something more specific than PixelImage: 
 * BilevelImage output = null;
 * // ditheredImage should be a BilevelImage...
 * if (ditheredImage instanceof BilevelImage
 * {
 *   // ... and it is!
 *   output = (BilevelImage)ditheredImage;
 * }
 * </pre>
 * <h3>TODO</h3>
 * Adjust this class to be able to process 16 bits per sample.
 * <h3>Theoretical background</h3>
 * The predefined templates were taken from the book <em>Bit-mapped 
 * graphics</em> (2nd edition) by Steve Rimmer, published by 
 * Windcrest / McGraw-Hill, ISBN 0-8306-4208-0.
 * The part on error diffusion dithering starts on page 375.
 * <p>
 * Several sources recommend Robert Ulichney's book
 * <a target="_top"
 * href="http://crl.research.compaq.com/who/people/ulichney/bib/DigitalHalftoning/Digital-Halftoning.html"><em>Digital
 * Halftoning</em></a> for this topic (published by The MIT Press, ISBN 0-262-21009-6).
 * Unfortunately, I wasn't able to get a copy (or the CD-ROM version published by
 * <a target="_top" href="http://www.ddj.com">Dr. Dobb's Journal</a>).
 *
 * @since 0.5.0
 * @author Marco Schmidt
 */
public class ErrorDiffusionDithering extends ImageToImageOperation implements RGBIndex
{
	/**
	 * Constant for Floyd-Steinberg error diffusion.
	 * The quantization error is distributed to four neighboring pixels.
	 */
	public static final int TYPE_FLOYD_STEINBERG = 0;

	/**
	 * Constant for Stucki error diffusion.
	 * The quantization error is distributed to twelve neighboring pixels.
	 */
	public static final int TYPE_STUCKI = 1;

	/**
	 * Constant for Burkes error diffusion.
	 * The quantization error is distributed to seven neighboring pixels.
	 */
	public static final int TYPE_BURKES = 2;

	/**
	 * Constant for Burkes error diffusion.
	 * The quantization error is distributed to ten neighboring pixels.
	 */
	public static final int TYPE_SIERRA = 3;

	/**
	 * Constant for Burkes error diffusion.
	 * The quantization error is distributed to twelve neighboring pixels.
	 */
	public static final int TYPE_JARVIS_JUDICE_NINKE= 4;

	/**
	 * Constant for Burkes error diffusion.
	 * The quantization error is distributed to twelve neighboring pixels.
	 */
	public static final int TYPE_STEVENSON_ARCE = 5;

	/**
	 * The default error diffusion type, to be used if none is specified by the user:
	 * (@link #TYPE_FLOYD_STEINBERG}.
	 */
	public static final int DEFAULT_TYPE = TYPE_FLOYD_STEINBERG;

	/**
	 * The index for the horizontal position of a neighbor pixel.
	 * For a description, see the constructor {@link #setTemplateData}.
	 */
	public static final int INDEX_X_POS = 0;

	/**
	 * The index for the vertical position of a neighbor pixel.
	 * For a description, see the constructor {@link #setTemplateData}.
	 */
	public static final int INDEX_Y_POS = 1;

	/**
	 * The index of the numerator of the relative part of the error of a neighbor pixel.
	 * For a description, see the constructor {@link #setTemplateData}.
	 */
	public static final int INDEX_ERROR_NUMERATOR = 2;

	/**
	 * The index of the denominator of the relative part of the error of a neighbor pixel.
	 * For a description, see the constructor {@link #setTemplateData}.
	 */
	public static final int INDEX_ERROR_DENOMINATOR = 3;

	private static final int[][] FLOYD_STEINBERG_DATA =
		{{ 1,  0, 7, 16},
		 {-1,  1, 3, 16},
		 { 0,  1, 5, 16},
		 { 1,  1, 1, 16}};
	private static final int[][] STUCKI_DATA =
		{{ 1,  0, 8, 42},
		 { 2,  0, 4, 42},
		 {-2,  1, 2, 42},
		 {-1,  1, 4, 42},
		 { 0,  1, 8, 42},
		 { 1,  1, 4, 42},
		 { 2,  1, 2, 42},
		 {-2,  2, 1, 42},
		 {-1,  2, 2, 42},
		 { 0,  2, 4, 42},
		 { 1,  2, 2, 42},
		 { 2,  2, 1, 42}};
	private static final int[][] BURKES_DATA =
		{{ 1,  0, 8, 32},
		 { 2,  0, 4, 32},
		 {-2,  1, 2, 32},
		 {-1,  1, 4, 32},
		 { 0,  1, 8, 32},
		 { 1,  1, 4, 32},
		 { 2,  1, 2, 32}};
	private static final int[][] SIERRA_DATA =
		{{ 1,  0, 5, 32},
		 { 2,  1, 3, 32},
		 {-2,  1, 2, 32},
		 {-1,  1, 4, 32},
		 { 0,  1, 5, 32},
		 { 1,  1, 4, 32},
		 { 2,  1, 2, 32},
		 {-1,  2, 2, 32},
		 { 0,  2, 3, 32},
		 { 1,  2, 2, 32}};
	private static final int[][] JARVIS_JUDICE_NINKE_DATA =
		{{ 1,  0, 7, 48},
		 { 2,  0, 5, 48},
		 {-2,  1, 3, 48},
		 {-1,  1, 5, 48},
		 { 0,  1, 7, 48},
		 { 1,  1, 5, 48},
		 { 2,  1, 3, 48},
		 {-2,  2, 1, 48},
		 {-1,  2, 3, 48},
		 { 0,  2, 5, 48},
		 { 1,  2, 3, 48},
		 { 2,  2, 1, 48}};
	private static final int[][] STEVENSON_ARCE_DATA =
		{{ 2,  0, 32, 200},
		 {-3,  1, 12, 200},
		 {-1,  1, 26, 200},
		 { 1,  1, 30, 200},
		 { 3,  1, 16, 200},
		 {-2,  2, 12, 200},
		 { 0,  2, 26, 200},
		 { 2,  2, 12, 200},
		 {-3,  3,  5, 200},
		 {-1,  3, 12, 200},
		 { 1,  3, 12, 200},
		 { 3,  3,  5, 200}};
	private int grayBits;
	private int imageWidth;
	private int leftColumns;
	private int rightColumns;
	private int newWidth;
	private int numRows;
	private int[][] templateData;
	private int[] errorNum;
	private int[] errorDen;
	private int[] indexLut;
	private RGBQuantizer quantizer;
	private boolean useTruecolorOutput;

	/**
	 * Creates a new object of this class and set the dithering type to
	 * {@link #DEFAULT_TYPE}.
	 */
	public ErrorDiffusionDithering()
	{
		setTemplateType(DEFAULT_TYPE);
	}

	/**
	 * Clamps the argument value to interval 0..max.
	 * @param value the value to be adjusted
	 * @param max the maximum allowed value (minimum is always 0)
	 * @return the adjusted value
	 */
	private static int adjust(int value, int max)
	{
		if (value <= 0)
		{
			return 0;
		}
		else
		if (value >= max)
		{
			return max;
		}
		else
		{
			return value;
		}
	}

	/**
	 * Copies data from input image to argument buffer.
	 * @param channelIndex index of the channel of the input image from which data is to be copied
	 * @param rowIndex index of the row of the input image from which data is to be copied
	 * @param dest the array to which data is to be copied
	 * @param destOffset index of the first element in the dest array to which data will be copied
	 */
	private void fillBuffer(int channelIndex, int rowIndex, int[] dest, int destOffset)
	{
		IntegerImage in = (IntegerImage)getInputImage();
		final int LAST = destOffset + imageWidth;
		int x = 0;
		while (destOffset != LAST)
		{
			dest[destOffset++] = in.getSample(channelIndex, x++, rowIndex);
		}
	}

	private void init(int[][] data, int imageWidth)
	{
		if (data == null)
		{
			throw new IllegalArgumentException("Data must not be null.");
		}
		if (imageWidth < 1)
		{
			throw new IllegalArgumentException("Image width must be larger than 0.");
		}
		this.imageWidth = imageWidth;
		leftColumns = 0;
		rightColumns = 0;
		numRows = 1;
		errorNum = new int[data.length];
		errorDen = new int[data.length];
		for (int i = 0; i < data.length; i++)
		{
			if (data[i] == null)
			{
				throw new IllegalArgumentException("Each int[] array of data must be initialized; array #" + i + " is not.");
			}
			if (data[i].length != 4)
			{
				throw new IllegalArgumentException("Each int[] array of data must be of length 4; array #" + i + " has length " + data[i].length + ".");
			}
			int x = data[i][INDEX_X_POS];
			if (x < 0)
			{
				x = - x;
				if (x > leftColumns)
				{
					leftColumns = x;
				}
			}
			else
			if (x > 0)
			{
				if (x > rightColumns)
				{
					rightColumns = x;
				}
			}
			int y = data[i][INDEX_Y_POS];
			if (y < 0)
			{
				throw new IllegalArgumentException("The y values must be >= 0; that is not true for array index #" + i + ".");
			}
			if (y > numRows - 1)
			{
				numRows = y + 1;
			}
			if (x <= 0 && y == 0)
			{
				throw new IllegalArgumentException("If y is equal to 0, x must not be <= 0; this is true for array index #" + i + ".");
			}
			if (data[i][INDEX_ERROR_NUMERATOR] == 0 || data[i][INDEX_ERROR_DENOMINATOR] == 0)
			{
				throw new IllegalArgumentException("Neither numerator nor denominator can be 0; this is the case for array index #" + i + ".");
			}
			errorNum[i] = data[i][INDEX_ERROR_NUMERATOR];
			errorDen[i] = data[i][INDEX_ERROR_DENOMINATOR];
		}
		newWidth = imageWidth + leftColumns + rightColumns;
		//System.out.println("new width=" + newWidth);
		indexLut = new int[data.length];
		for (int i = 0; i < indexLut.length; i++)
		{
			indexLut[i] =  data[i][INDEX_Y_POS] * newWidth + data[i][INDEX_X_POS];
			//System.out.println("lut i=" + i + "=" + indexLut[i]);
		}
	}

	/**
	 * Quantizes the input image, distributing quantization errors to neighboring
	 * pixels.
	 * Works for {@link Gray8Image} (then {@link #setGrayscaleOutputBits(int)}
	 * must have been called to set a number of output bits between 1 and 7) objects and 
	 * {@link RGB24Image} (then a quantizer must be specified using 
	 * {@link #setQuantizer(RGBQuantizer)}) objects.
	 */
	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		PixelImage out = getOutputImage();
		if (in instanceof Gray8Image)
		{
			init(templateData, in.getWidth());
			if (grayBits == 1)
			{
				process((Gray8Image)in, (BilevelImage)out);
			}
			else
			if (grayBits > 1 && grayBits < 8)
			{
				process((Gray8Image)in, (Gray8Image)out);
			}
			else
			{
				throw new WrongParameterException("Cannot handle gray bits other than 1..7.");
			}
		}
		else
		if (in instanceof RGB24Image)
		{
			init(templateData, in.getWidth());
			if (quantizer == null)
			{
				throw new MissingParameterException("No quantizer was specified.");
			}
			if (useTruecolorOutput)
			{
				process((RGB24Image)in, (RGB24Image)out);
			}
			else
			{
				process((RGB24Image)in, (Paletted8Image)out);
			}
		}
		else
		{
			throw new WrongParameterException("Cannot handle this image: " + in.toString());
		}
	}

	private void process(Gray8Image in, BilevelImage out)
	{
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		if (out == null)
		{
			out = new MemoryBilevelImage(WIDTH, HEIGHT);
		}
		final int NUM_ERROR_PIXELS = errorNum.length;
		// create buffer
		int[] buffer = new int[newWidth * numRows];
		//System.out.println("buffer  length=" + buffer.length);
		// fill buffer with numRows (or HEIGHT, whatever is smaller) rows of data
		int n = Math.min(numRows, HEIGHT);
		int offset = leftColumns;
		int bufferYIndex = 0;
		while (n-- > 0)
		{
			fillBuffer(0, bufferYIndex++, buffer, offset);
			offset += newWidth;
		}
		int bufferLastRowOffset = offset - newWidth;
		// set complete output image to black
		out.clear(BilevelImage.BLACK);
		for (int y = 0; y < HEIGHT; y++)
		{
			int bufferIndex = leftColumns;
			for (int x = 0; x < WIDTH; x++)
			{
				int value = buffer[bufferIndex];
				if (value < 0)
				{
					value = 0;
				}
				else
				if (value > 255)
				{
					value = 255;
				}
				int error;
				if ((value & 0x80) == 0)
				{
					// black pixel need not be written to output image
					// because all of its pixels have initially been set
					// to that color
					error = value;
				}
				else
				{
					// white
					out.putWhite(x, y);
					error = value - 255;
				}
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					buffer[bufferIndex + indexLut[i]] += errorPart;
				}
				bufferIndex++;
			}
			for (int i = 0, j = newWidth; j < buffer.length; i++, j++)
			{
				buffer[i] = buffer[j];
			}
			if (bufferYIndex < HEIGHT)
			{
				fillBuffer(0, bufferYIndex++, buffer, bufferLastRowOffset);
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	private void process(Gray8Image in, Gray8Image out)
	{
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		final int RIGHT_SHIFT = 8 - grayBits;
		final int[] GRAY_LUT = new int[1 << grayBits];
		for (int i = 0; i < GRAY_LUT.length; i++)
		{
			GRAY_LUT[i] = i * 255 / (GRAY_LUT.length - 1);
		}
		if (out == null)
		{
			out = new MemoryGray8Image(WIDTH, HEIGHT);
		}
		final int NUM_ERROR_PIXELS = errorNum.length;
		// create buffer
		int[] buffer = new int[newWidth * numRows];
		// fill buffer with numRows (or HEIGHT, whatever is smaller) rows of data
		int n = Math.min(numRows, HEIGHT);
		int offset = leftColumns;
		int bufferYIndex = 0;
		while (n-- > 0)
		{
			fillBuffer(0, bufferYIndex++, buffer, offset);
			offset += newWidth;
		}
		int bufferLastRowOffset = offset - newWidth;
		for (int y = 0; y < HEIGHT; y++)
		{
			int bufferIndex = leftColumns;
			for (int x = 0; x < WIDTH; x++)
			{
				int value = buffer[bufferIndex];
				if (value < 0)
				{
					value = 0;
				}
				else
				if (value > 255)
				{
					value = 255;
				}
				int quantized = GRAY_LUT[value >> RIGHT_SHIFT];
				out.putSample(0, x, y, quantized);
				int error = value - quantized;
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					buffer[bufferIndex + indexLut[i]] += errorPart;
				}
				bufferIndex++;
			}
			for (int i = 0, j = newWidth; j < buffer.length; i++, j++)
			{
				buffer[i] = buffer[j];
			}
			if (bufferYIndex < HEIGHT)
			{
				fillBuffer(0, bufferYIndex++, buffer, bufferLastRowOffset);
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	private void process(RGB24Image in, Paletted8Image out)
	{
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		final int MAX = 255;
		if (out == null)
		{
			out = new MemoryPaletted8Image(WIDTH, HEIGHT, quantizer.createPalette());
		}
		final int NUM_ERROR_PIXELS = errorNum.length;
		// create buffers
		int[] redBuffer = new int[newWidth * numRows];
		int[] greenBuffer = new int[newWidth * numRows];
		int[] blueBuffer = new int[newWidth * numRows];
		//System.out.println("buffer  length=" + buffer.length);
		// fill buffer with numRows (or HEIGHT, whatever is smaller) rows of data
		int n = Math.min(numRows, HEIGHT);
		int offset = leftColumns;
		int bufferYIndex = 0;
		while (n-- > 0)
		{
			fillBuffer(INDEX_RED, bufferYIndex, redBuffer, offset);
			fillBuffer(INDEX_GREEN, bufferYIndex, greenBuffer, offset);
			fillBuffer(INDEX_BLUE, bufferYIndex++, blueBuffer, offset);
			offset += newWidth;
		}
		int bufferLastRowOffset = offset - newWidth;
		int[] originalRgb = new int[3];
		int[] quantizedRgb = new int[3];
		for (int y = 0; y < HEIGHT; y++)
		{
			int bufferIndex = leftColumns;
			for (int x = 0; x < WIDTH; x++)
			{
				originalRgb[INDEX_RED] = adjust(redBuffer[bufferIndex], MAX);
				originalRgb[INDEX_GREEN] = adjust(greenBuffer[bufferIndex], MAX);
				originalRgb[INDEX_BLUE] = adjust(blueBuffer[bufferIndex], MAX);
				int paletteIndex = quantizer.map(originalRgb, quantizedRgb);
				out.putSample(0, x, y, paletteIndex);
				// red
				int error = originalRgb[INDEX_RED] - quantizedRgb[INDEX_RED];
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					redBuffer[bufferIndex + indexLut[i]] += errorPart;
				}
				// green
				error = originalRgb[INDEX_GREEN] - quantizedRgb[INDEX_GREEN];
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					greenBuffer[bufferIndex + indexLut[i]] += errorPart;
				}
				// blue
				error = originalRgb[INDEX_BLUE] - quantizedRgb[INDEX_BLUE];
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					blueBuffer[bufferIndex + indexLut[i]] += errorPart;
				}
				bufferIndex++;
			}
			System.arraycopy(redBuffer, newWidth, redBuffer, 0, redBuffer.length - newWidth);
			System.arraycopy(greenBuffer, newWidth, greenBuffer, 0, greenBuffer.length - newWidth);
			System.arraycopy(blueBuffer, newWidth, blueBuffer, 0, blueBuffer.length - newWidth);
			if (bufferYIndex < HEIGHT)
			{
				fillBuffer(INDEX_RED, bufferYIndex, redBuffer, bufferLastRowOffset);
				fillBuffer(INDEX_GREEN, bufferYIndex, greenBuffer, bufferLastRowOffset);
				fillBuffer(INDEX_BLUE, bufferYIndex++, blueBuffer, bufferLastRowOffset);
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	private void process(RGB24Image in, RGB24Image out)
	{
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		final int MAX = 255;
		if (out == null)
		{
			out = (RGB24Image)in.createCompatibleImage(WIDTH, HEIGHT);
		}
		final int NUM_ERROR_PIXELS = errorNum.length;
		// create buffers
		int[] redBuffer = new int[newWidth * numRows];
		int[] greenBuffer = new int[newWidth * numRows];
		int[] blueBuffer = new int[newWidth * numRows];
		// fill buffer with numRows (or HEIGHT, whatever is smaller) rows of data
		int n = Math.min(numRows, HEIGHT);
		int offset = leftColumns;
		int bufferYIndex = 0;
		while (n-- > 0)
		{
			fillBuffer(INDEX_RED, bufferYIndex, redBuffer, offset);
			fillBuffer(INDEX_GREEN, bufferYIndex, greenBuffer, offset);
			fillBuffer(INDEX_BLUE, bufferYIndex++, blueBuffer, offset);
			offset += newWidth;
		}
		int bufferLastRowOffset = offset - newWidth;
		int[] originalRgb = new int[3];
		int[] quantizedRgb = new int[3];
		for (int y = 0; y < HEIGHT; y++)
		{
			int bufferIndex = leftColumns;
			for (int x = 0; x < WIDTH; x++)
			{
				originalRgb[INDEX_RED] = adjust(redBuffer[bufferIndex], MAX);
				originalRgb[INDEX_GREEN] = adjust(greenBuffer[bufferIndex], MAX);
				originalRgb[INDEX_BLUE] = adjust(blueBuffer[bufferIndex], MAX);
				/*int paletteIndex = quantizer.map(originalRgb, quantizedRgb);
				out.putSample(0, x, y, paletteIndex);*/
				out.putSample(INDEX_RED, x, y, quantizedRgb[INDEX_RED]);
				out.putSample(INDEX_GREEN, x, y, quantizedRgb[INDEX_GREEN]);
				out.putSample(INDEX_BLUE, x, y, quantizedRgb[INDEX_BLUE]);
				// red
				int error = originalRgb[INDEX_RED] - quantizedRgb[INDEX_RED];
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					redBuffer[bufferIndex + indexLut[i]] += errorPart;
				}
				// green
				error = originalRgb[INDEX_GREEN] - quantizedRgb[INDEX_GREEN];
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					greenBuffer[bufferIndex + indexLut[i]] += errorPart;
				}
				// blue
				error = originalRgb[INDEX_BLUE] - quantizedRgb[INDEX_BLUE];
				for (int i = 0; i < NUM_ERROR_PIXELS; i++)
				{
					int errorPart = error * errorNum[i] / errorDen[i];
					blueBuffer[bufferIndex + indexLut[i]] += errorPart;
				}
				bufferIndex++;
			}
			/*for (int i = 0, j = newWidth; j < buffer.length; i++, j++)
			{
				buffer[i] = buffer[j];
			}*/
			System.arraycopy(redBuffer, newWidth, redBuffer, 0, redBuffer.length - newWidth);
			System.arraycopy(greenBuffer, newWidth, greenBuffer, 0, greenBuffer.length - newWidth);
			System.arraycopy(blueBuffer, newWidth, blueBuffer, 0, blueBuffer.length - newWidth);
			if (bufferYIndex < HEIGHT)
			{
				fillBuffer(INDEX_RED, bufferYIndex, redBuffer, bufferLastRowOffset);
				fillBuffer(INDEX_GREEN, bufferYIndex, greenBuffer, bufferLastRowOffset);
				fillBuffer(INDEX_BLUE, bufferYIndex++, blueBuffer, bufferLastRowOffset);
			}
			setProgress(y, HEIGHT);
		}
		setOutputImage(out);
	}

	/**
	 * Sets the number of bits to be in the output image when a grayscale image 
	 * is quantized.
	 * If the input image is of type {@link Gray8Image}, only values between 1 and 7
	 * are valid.
	 * @param numBits the number of bits in the output image
	 */
	public void setGrayscaleOutputBits(int numBits)
	{
		grayBits = numBits;
	}

	/**
	 * Sets the color quantizer to be used (if the input image is
	 * a truecolor image).
	 * @param q an object of a class implementing the RGBQuantizer interface
	 */
	public void setQuantizer(RGBQuantizer q)
	{
		quantizer = q;
	}

	/**
	 * Set information on how errors are to be distributed by this error diffusion
	 * dithering operation.
	 * <p>
	 * Error diffusion dithering works by quantizing each pixel and distributing the
	 * resulting error to neighboring pixels.
	 * Quantizing maps a pixel to another pixel.
	 * Each pixel is made up of one or more samples (as an example, three samples
	 * r<sub>orig</sub>, g<sub>orig</sub> and b<sub>orig</sub> for the 
	 * original pixel of an RGB image and r<sub>quant</sub>, g<sub>quant</sub> and 
	 * b<sub>quant</sub> for the quantized pixel).
	 * <p>
	 * The process of quantization attempts to find a quantized pixel that is as
	 * close to the original as possible.
	 * In the ideal case, the difference between original and quantized pixel is
	 * zero for each sample.
	 * Otherwise, this <em>quantization error</em> is non-zero, positive or negative.
	 * Example: original pixel (12, 43, 33), quantized pixel (10, 47, 40); the 
	 * error is (12 - 10, 43 - 47, 40 - 33) = (2, -4, 7).
	 * The error (2, -4, 7) is to be distributed to neighboring pixels.
	 * <p>
	 * The <code>data</code> argument of this constructor describes how to do that.
	 * It is a two-dimensional array of int values.
	 * Each of the one-dimensional int arrays of <code>data</code> describe
	 * one neighboring pixel and the relative amount of the error that it gets.
	 * That is why <code>data.length</code> specifies the number of neighboring
	 * pixels involved in distributing the error.
	 * Let's call the pixel that was just quantized the <em>current pixel</em>.
	 * It is at image position (x, y).
	 * <p>
	 * Each of the one-dimensional arrays that are part of <code>data</code>
	 * must have a length of 4.
	 * The meaning of these four values is now described.
	 * The values can be accessed by the INDEX_xyz constants of this class.
	 * These four values describe the position of one neighboring pixel and 
	 * the relative amount of the error that will be added to or subtracted
	 * from it.
	 * <ul>
	 * <li>{@link #INDEX_X_POS} (0): 
	 *     the difference between the horizontal position of the current pixel, x,
	 *     and the neighboring pixel; can take a positive or negative value,
	 *     or zero; exception: the y position of the current pixel is zero;
	 *     in that case, this value must be larger than zero, because 
	 *     neighboring pixels that get part of the error must be to the right of
	 *     or below the current pixel</li>
	 * <li>{@link #INDEX_Y_POS} (1): 
	 *     the difference between the vertical position of the current pixel, y,
	 *     and the neighboring pixel; must be equal to or larger than 0</li>
	 * <li>{@link #INDEX_ERROR_NUMERATOR} (2): 
	 *     the numerator of the relative part of the error that wil be added
	 *     to this neighboring pixel; must not be equal to 0</li>
	 * <li>{@link #INDEX_ERROR_DENOMINATOR} (3): 
	 *     the denominator of the relative part of the error that wil be added
	 *     to this neighboring pixel; must not be equal to 0</li>
	 * </ul>
	 * Example: the predefined dithering type Floyd-Steinberg.
	 * It has the following <code>data</code> array:
	 * <pre>
	 * int[][] FLOYD_STEINBERG = {{ 1,  0, 7, 16},
	 *   {-1,  1, 3, 16},
	 *   { 0,  1, 5, 16},
	 *   { 1,  1, 1, 16}};
	 * </pre>
	 * Each of the one-dimensional arrays is of length 4.
	 * Accidentally, there are also four one-dimensional arrays.
	 * The number of arrays is up to the designer.
	 * The first array {1, 0, 7, 16} is interpreted as follows--go to
	 * the pixel with a horizontal difference of 1 and a vertical difference of 0
	 * (so, the pixel to the right of the current pixel) and add 7 / 16th of the 
	 * quantization error to it.
	 * Then go to the pixel at position (-1, 1) (one to the left, one row below the
	 * current row) and add 3 / 16th of the error to it.
	 * The other two one-dimensional arrays are processed just like that.
	 * <p>
	 * As you can see, the four relative errors 1/16, 3/16, 5/16 and 7/16 sum up to
	 * 1 (or 16/16); this is in a precondition to make sure that the error
	 * is distributed completely.
	 *
	 * @param data contains a description of how the error is to be distributed
	 */
	public void setTemplateData(int[][] data)
	{
		templateData = data;
	}

	/**
	 * When dithering an RGB input image, this method specifies whether the 
	 * output will be an {@link net.sourceforge.jiu.data.RGBIntegerImage}
	 * (<code>true</code>) or a {@link net.sourceforge.jiu.data.Paletted8Image} (<code>false</code>).
	 * @param truecolor true if truecolor output is wanted
	 */
	public void setTruecolorOutput(boolean truecolor)
	{
		useTruecolorOutput = truecolor;
	}

	/**
	 * Sets a new template type.
	 * The argument must be one of the TYPE_xyz constants of this class.
	 * @param type int value, one of the TYPE_xyz constants of this class
	 * @throws IllegalArgumentException if the argument is not of the TYPE_xyz constants
	 */
	public void setTemplateType(int type)
	{
		switch(type)
		{
			case(TYPE_FLOYD_STEINBERG):
			{
				templateData = FLOYD_STEINBERG_DATA;
				break;
			}
			case(TYPE_STUCKI):
			{
				templateData = STUCKI_DATA;
				break;
			}
			case(TYPE_BURKES):
			{
				templateData = BURKES_DATA;
				break;
			}
			case(TYPE_SIERRA):
			{
				templateData = SIERRA_DATA;
				break;
			}
			case(TYPE_JARVIS_JUDICE_NINKE):
			{
				templateData = JARVIS_JUDICE_NINKE_DATA;
				break;
			}
			case(TYPE_STEVENSON_ARCE):
			{
				templateData = STEVENSON_ARCE_DATA;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Unknown template type: " + type + ".");
			}
		}
	}
}
