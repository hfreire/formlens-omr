/*
 * AutoDetectColorType
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.reduction;

import net.sourceforge.jiu.color.data.Histogram3D;
import net.sourceforge.jiu.color.data.OnDemandHistogram3D;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Detects the minimum (in terms of memory) color type of an image.
 * Can convert the original image to that new input type on demand.
 * <p>
 * Input parameters: image to be examined, boolean that specifies whether
 * conversion will be performed (default is true, conversion is performed).
 * Output parameters: converted image, boolean that expresses whether
 * a conversion was possible.
 * <p>
 * Supported types for input image: RGB24Image, Gray8Image, Paletted8Image.
 * <p>
 * BilevelImage is not supported because there is no smaller image type,
 * so bilevel images cannot be reduced.
 * <p>
 * This operation is not a {@link net.sourceforge.jiu.ops.ImageToImageOperation} because this 
 * class need not necessarily produce a new image 
 * (with {@link #setConversion}(false)).
 * <h3>Usage example</h3>
 * This code snippet loads an image and attempts to reduce it to the
 * minimum color type that will hold it.
 * <pre>
 * PixelImage image = ImageLoader.load("test.bmp");
 * AutoDetectColorType op = new AutoDetectColorType();
 * op.setInputImage(image);
 * op.process();
 * if (op.isReducible())
 * {
 *   image = op.getOutputImage();
 * }
 * </pre>
 *
 * @author Marco Schmidt
 */
public class AutoDetectColorType extends Operation
{
	public static final int TYPE_UNKNOWN = -1;
	public static final int TYPE_BILEVEL = 0;
	public static final int TYPE_GRAY16 = 4;
	public static final int TYPE_GRAY8 = 1;
	public static final int TYPE_PALETTED8 = 2;
	public static final int TYPE_RGB24 = 3;
	public static final int TYPE_RGB48 = 5;

	private PixelImage inputImage;
	private PixelImage outputImage;
	private boolean doConvert;
	private int type;
	private Histogram3D hist;

	public AutoDetectColorType()
	{
		doConvert = true;
		type = TYPE_UNKNOWN;
	}

	/**
	 * Creates a bilevel image from any grayscale (or RGB) image
	 * that has been checked to be bilevel.
	 */
	private void createBilevelFromGrayOrRgb(IntegerImage in)
	{
		MemoryBilevelImage out = new MemoryBilevelImage(in.getWidth(), in.getHeight());
		out.clear(BilevelImage.BLACK);
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				if (in.getSample(0, x, y) != 0)
				{
					out.putWhite(x, y);
				}
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}

	private void createBilevelFromPaletted(Paletted8Image in)
	{
		Palette palette = in.getPalette();
		MemoryBilevelImage out = new MemoryBilevelImage(in.getWidth(), in.getHeight());
		out.clear(BilevelImage.BLACK);
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				if (palette.getSample(RGBIndex.INDEX_RED, in.getSample(0, x, y)) != 0)
				{
					out.putWhite(x, y);
				}
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}

	// works for RGB24 and RGB48 input image, assumed that
	// a matching output image type was chosen (Gray8 for RGB24, Gray16 for
	// RGB48)
	private void createGrayFromRgb(IntegerImage in, IntegerImage out)
	{
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(0, x, y, in.getSample(0, x, y));
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}

	private void createGray8FromGray16(Gray16Image in)
	{
		Gray8Image out = new MemoryGray8Image(in.getWidth(), in.getHeight());
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(0, x, y, in.getSample(0, x, y) & 0xff);
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}

	// assumes that in fact has a palette with gray colors only
	private void createGray8FromPaletted8(Paletted8Image in, Gray8Image out)
	{
		Palette palette = in.getPalette();
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(0, x, y, palette.getSample(0, in.getSample(0, x, y)));
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}

	private void createPaletted8FromRgb24(RGB24Image in)
	{
		// create palette from histogram
		int uniqueColors = hist.getNumUsedEntries();
		Palette palette = new Palette(uniqueColors, 255);
		int index = 0;
		for (int r = 0; r < 256; r++)
		{
			for (int g = 0; g < 256; g++)
			{
				for (int b = 0; b < 256; b++)
				{
					if (hist.getEntry(r, g, b) != 0)
					{
						hist.setEntry(r, g, b, index);
						palette.putSample(RGBIndex.INDEX_RED, index, r);
						palette.putSample(RGBIndex.INDEX_GREEN, index, g);
						palette.putSample(RGBIndex.INDEX_BLUE, index, b);
						index++;
					}
				}
			}
		}
		Paletted8Image out = new MemoryPaletted8Image(in.getWidth(), in.getHeight(), palette);
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y);
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y);
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y);
				out.putSample(0, x, y, hist.getEntry(red, green, blue));
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}

	private void createPaletted8FromRgb48(RGB48Image in)
	{
		// create palette from histogram
		int uniqueColors = hist.getNumUsedEntries();
		Palette palette = new Palette(uniqueColors, 255);
		int index = 0;
		for (int r = 0; r < 256; r++)
		{
			for (int g = 0; g < 256; g++)
			{
				for (int b = 0; b < 256; b++)
				{
					if (hist.getEntry(r, g, b) != 0)
					{
						hist.setEntry(r, g, b, index);
						palette.putSample(RGBIndex.INDEX_RED, index, r);
						palette.putSample(RGBIndex.INDEX_GREEN, index, g);
						palette.putSample(RGBIndex.INDEX_BLUE, index, b);
						index++;
					}
				}
			}
		}
		Paletted8Image out = new MemoryPaletted8Image(in.getWidth(), in.getHeight(), palette);
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y) >> 8;
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y) >> 8;
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y) >> 8;
				out.putSample(0, x, y, hist.getEntry(red, green, blue));
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}

	private void createRgb24FromRgb48(RGB48Image in, RGB24Image out)
	{
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				out.putSample(RGBIndex.INDEX_RED, x, y, in.getSample(RGBIndex.INDEX_RED, x, y) >> 8);
				out.putSample(RGBIndex.INDEX_GREEN, x, y, in.getSample(RGBIndex.INDEX_GREEN, x, y) >> 8);
				out.putSample(RGBIndex.INDEX_BLUE, x, y, in.getSample(RGBIndex.INDEX_BLUE, x, y) >> 8);
			}
			setProgress(y, in.getHeight());
		}
		outputImage = out;
	}


	/**
	 * Returns the reduced output image if one was created in {@link #process()}.
	 * @return newly-created output image
	 */
	public PixelImage getOutputImage()
	{
		return outputImage;
	}

	/**
	 * Returns the type of the minimum image type found (one of the TYPE_xyz constants
	 * of this class).
	 * Can only be called after a successful call to process.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * This method can be called after {@link #process()} to find out if the input
	 * image in fact can be reduced to a &quot;smaller&quot; image type.
	 * If this method returns <code>true</code> and if conversion was desired by the
	 * user (can be specified via {@link #setConversion}), the reduced image can 
	 * be retrieved via {@link #getOutputImage()}.
	 * @return if image was found to be reducible in process()
	 */
	public boolean isReducible()
	{
		return type != TYPE_UNKNOWN;
	}

	// works for Gray8 and Gray16
	private boolean isGrayBilevel(IntegerImage in)
	{
		final int HEIGHT = in.getHeight();
		final int MAX = in.getMaxSample(0);
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int value = in.getSample(0, x, y);
				if (value != 0 && value != MAX)
				{
					return false; // not a grayscale image
				}
			}
		}
		return true;
	}

	private boolean isGray16Gray8(Gray16Image in)
	{
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < WIDTH; x++)
			{
				int value = in.getSample(0, x, y);
				int lsb = value & 0xff;
				int msb = (value >> 8) & 0xff;
				if (lsb != msb)
				{
					return false;
				}
			}
		}
		return true;
	}

	private boolean isRgb48Gray8(RGB48Image in)
	{
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y);
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y);
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y);
				if (red != green || green != blue)
				{
					return false;
				}
				int lsb = red & 0xff;
				int msb = red >> 8;
				if (lsb != msb)
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Assumes that it has already been verified that the input 48 bpp
	 * RGB image is also a 24 bpp RGB image.
	 * @param in input image to be checked
	 * @return if this image can be losslessly converted to a Paletted8Image
	 */
	private boolean isRgb48Paletted8(RGB48Image in)
	{
		int uniqueColors = 0;
		hist = new OnDemandHistogram3D(255, 255, 255);
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y) >> 8;
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y) >> 8;
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y) >> 8;
				if (hist.getEntry(red, green, blue) == 0)
				{
					hist.increaseEntry(red, green, blue);
					uniqueColors++;
					if (uniqueColors > 256)
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isRgb48Rgb24(RGB48Image in)
	{
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				for (int channel = 0; channel < 3; channel++)
				{
					int sample = in.getSample(channel, x, y);
					if ((sample & 0xff) != ((sample & 0xff00) >> 8))
					{
						return false; 
					}
				}
			}
		}
		return true;
	}

	// works for RGB24 and RGB48
	private boolean isRgbBilevel(IntegerImage in)
	{
		final int HEIGHT = in.getHeight();
		final int MAX = in.getMaxSample(0);
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y);
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y);
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y);
				if (red != green || green != blue || (blue != 0  && blue != MAX))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns if the input RGB image can be losslessly converted to 
	 * a grayscale image.
	 * @param in RGB image to be checked
	 * @return true if input is gray, false otherwise
	 */
	private boolean isRgbGray(RGBIntegerImage in)
	{
		final int HEIGHT = in.getHeight();
		for (int y = 0; y < HEIGHT; y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y);
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y);
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y);
				if (red != green || green != blue)
				{
					return false;
				}
			}
		}
		return true;
	}

	private boolean isRgb24Paletted8(RGB24Image in)
	{
		int uniqueColors = 0;
		hist = new OnDemandHistogram3D(255, 255, 255);
		for (int y = 0; y < in.getHeight(); y++)
		{
			for (int x = 0; x < in.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y);
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y);
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y);
				if (hist.getEntry(red, green, blue) == 0)
				{
					hist.increaseEntry(red, green, blue);
					uniqueColors++;
					if (uniqueColors > 256)
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	public void process() throws 
		MissingParameterException,
		WrongParameterException
	{
		if (inputImage == null)
		{
			throw new MissingParameterException("No input image available");
		}
		// GRAY8
		if (inputImage instanceof Gray8Image)
		{
			if (isGrayBilevel((Gray8Image)inputImage))
			{
				type = TYPE_BILEVEL;
				if (doConvert)
				{
					createBilevelFromGrayOrRgb((Gray8Image)inputImage);
				}
			}
		}
		else
		// GRAY16
		if (inputImage instanceof Gray16Image)
		{
			if (isGrayBilevel((Gray16Image)inputImage))
			{
				type = TYPE_BILEVEL;
				if (doConvert)
				{
					createBilevelFromGrayOrRgb((Gray16Image)inputImage);
				}
			}
			else
			if (isGray16Gray8((Gray16Image)inputImage))
			{
				type = TYPE_GRAY16;
				if (doConvert)
				{
					createGray8FromGray16((Gray16Image)inputImage);
				}
			}
		}
		else
		// RGB24
		if (inputImage instanceof RGB24Image)
		{
			if (isRgbBilevel((RGB24Image)inputImage))
			{
				type = TYPE_BILEVEL;
				if (doConvert)
				{
					createBilevelFromGrayOrRgb((RGB24Image)inputImage);
				}
			}
			else
			if (isRgbGray((RGB24Image)inputImage))
			{
				type = TYPE_GRAY8;
				if (doConvert)
				{
					outputImage = new MemoryGray8Image(inputImage.getWidth(), inputImage.getHeight());
					createGrayFromRgb((RGB24Image)inputImage, (Gray8Image)outputImage);
				}
			}
			else
			if (isRgb24Paletted8((RGB24Image)inputImage))
			{
				type = TYPE_PALETTED8;
				if (doConvert)
				{
					createPaletted8FromRgb24((RGB24Image)inputImage);
				}
			}
		}
		else
		// RGB48
		if (inputImage instanceof RGB48Image)
		{
			if (isRgbBilevel((RGB48Image)inputImage))
			{
				type = TYPE_BILEVEL;
				if (doConvert)
				{
					createBilevelFromGrayOrRgb((RGB48Image)inputImage);
				}
			}
			else
			if (isRgb48Gray8((RGB48Image)inputImage))
			{
				type = TYPE_GRAY8;
				if (doConvert)
				{
					outputImage = new MemoryGray8Image(inputImage.getWidth(), inputImage.getHeight());
					// this create method works because it works with int and the least significant 8
					// bits are equal to the most significant 8 bits if isRgb48Gray8 returned true
					createGrayFromRgb((RGB48Image)inputImage, (Gray8Image)outputImage);
				}
			}
			else
			if (isRgbGray((RGB48Image)inputImage))
			{
				type = TYPE_GRAY16;
				if (doConvert)
				{
					outputImage = new MemoryGray8Image(inputImage.getWidth(), inputImage.getHeight());
					createGrayFromRgb((RGB24Image)inputImage, (Gray8Image)outputImage);
				}
			}
			else
			if (isRgb48Rgb24((RGB48Image)inputImage))
			{
				// RGB48 input is RGB24; is it also Paletted8?
				if (isRgb48Paletted8((RGB48Image)inputImage))
				{
					type = TYPE_PALETTED8;
					if (doConvert)
					{
						createPaletted8FromRgb48((RGB48Image)inputImage);
					}
				}
				else
				{
					type = TYPE_RGB24;
					if (doConvert)
					{
						outputImage = new MemoryRGB24Image(inputImage.getWidth(), inputImage.getHeight());
						createRgb24FromRgb48((RGB48Image)inputImage, (RGB24Image)outputImage);
					}
				}
			}
		}
		else
		// PALETTED8
		if (inputImage instanceof Paletted8Image)
		{
			Paletted8Image in = (Paletted8Image)inputImage;
			Palette palette = in.getPalette();
			if (palette.isBlackAndWhite())
			{
				type = TYPE_BILEVEL;
				if (doConvert)
				{
					createBilevelFromPaletted(in);
				}
			}
			else
			if (palette.isGray())
			{
				type = TYPE_GRAY8;
				if (doConvert)
				{
					Gray8Image out = new MemoryGray8Image(in.getWidth(), in.getHeight());
					createGray8FromPaletted8(in, out);
				}
			}
		}
		else
		{
			throw new WrongParameterException("Not a supported or reducible image type: " + inputImage.toString());
		}
	}

	/**
	 * This method can be used to specify whether the input image is to be converted
	 * to the minimum image type if it is clear that such a conversion is possible.
	 * The default value is <code>true</code>.
	 * If this is set to <code>false</code>, it can still be 
	 * @param convert if true, the conversion will be performed
	 */
	public void setConversion(boolean convert)
	{
		doConvert = convert;
	}

	/**
	 * This method must be used to specify the mandatory input image.
	 * @param image PixelImage object to be examined
	 */
	public void setInputImage(PixelImage image)
	{
		inputImage = image;
	}
}
