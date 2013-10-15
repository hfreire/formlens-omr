/*
 * ImageCreator
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

/**
 * A class to create {@link java.awt.Image} objects from various JIU image data types
 * and vice versa.
 * java.awt.Image objects can be used with the AWT and Swing GUI environments.
 *
 * @author Marco Schmidt
 */
public class ImageCreator
{
	/**
	 * The default transparency value to be used: full opacity.
	 */
	public static final int DEFAULT_ALPHA = 0xff000000;

	private static Frame frame;

	private ImageCreator()
	{
	}

	/**
	 * Creates a {@link java.awt.Image} object from a pixel array.
	 * Internally, a {@link java.awt.Frame} object is used to call its 
	 * {@link java.awt.Frame#createImage} method
	 * with a {@link java.awt.image.MemoryImageSource} object.
	 *
	 * @param pixels the image pixel data in the typical RGBA 32-bit format, one int per pixel
	 * @param width the horizontal resolution in pixels of the image to be created
	 * @param height the vertical resolution in pixels of the image to be created
	 */
	public static Image createImage(int[] pixels, int width, int height)
	{
		if (width < 1 || height < 1)
		{
			throw new IllegalArgumentException("Error -- width and height " +
				"must both be larger than zero.");
		}
		if (pixels == null)
		{
			throw new IllegalArgumentException("Error -- the pixel array " +
				"must be non-null.");
		}
		if (pixels.length < width * height)
		{
			throw new IllegalArgumentException("Error -- the pixel array " +
				"must contain at least width times height items.");
		}
		if (frame == null)
		{
			frame = new Frame();
		}
		return frame.createImage(new MemoryImageSource(width, height, pixels, 0, width));
	}

	public static BufferedImage convertToAwtBufferedImage(PixelImage image)
	{
		if (image == null)
		{
			return null;
		}
		if (image instanceof RGB24Image)
		{
			return convertToAwtBufferedImage((RGB24Image)image);
		}
		else
		{
			throw new IllegalArgumentException("Unsupported input image type: " + image.getImageType());
		}
	}

	/**
	 * Convert a JIU {@link RGB24Image} to a {@link BufferedImage} with the 
	 * given alpha value (use {@link RGBA#DEFAULT_ALPHA} as default). 
	 * @param image JIU image to be converted
	 * @return a new BufferedImage
	 * @since 0.14.2
	 */
	public static BufferedImage convertToAwtBufferedImage(RGB24Image image)
	{
		if (image == null)
		{
			return null;
		}
		final int WIDTH = image.getWidth();
		final int HEIGHT = image.getHeight();
		BufferedImage out = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		int outBuffer[] = new int[WIDTH];
		byte red[] = new byte[WIDTH];
		byte green[] = new byte[WIDTH];
		byte blue[] = new byte[WIDTH];
		for (int y = 0; y < HEIGHT; y++)
		{
			image.getByteSamples(RGBIndex.INDEX_RED, 0, y, WIDTH, 1, red, 0);
			image.getByteSamples(RGBIndex.INDEX_GREEN, 0, y, WIDTH, 1, green, 0);
			image.getByteSamples(RGBIndex.INDEX_BLUE, 0, y, WIDTH, 1, blue, 0);
			for (int x = 0; x < WIDTH; x++)
			{
				outBuffer[x] = //0xff000000 |
					((red[x] & 0xff) << 16) | 
					((green[x] & 0xff) << 8) | 
					((blue[x] & 0xff)); 
			}
			out.setRGB(0, y, WIDTH,1, outBuffer, 0, WIDTH);
		}
		return out;
	}

	/**
	 * Creates an instance of {@link java.awt.Image} from an instance of
	 * {@link RGB24Image}.
	 * This will require <code>image.getWidth() * image.getHeight() * 4</code>
	 * bytes of free memory.
	 * This method checks the type of the argument image via instanceof
	 * and the calls the right convertToAwtImage method of this class.
	 * @param image the RGB24Image to be converted
	 * @param alpha alpha value to be used with each pixel
	 * @return newly-created AWT image instance
	 */
	public static Image convertToAwtImage(PixelImage image, int alpha)
	{
		if (image == null)
		{
			return null;
		}
		if (image instanceof RGB24Image)
		{
			return convertToAwtImage((RGB24Image)image, alpha);
		}
		else
		if (image instanceof RGB48Image)
		{
			return convertToAwtImage((RGB48Image)image, alpha);
		}
		else
		if (image instanceof Gray8Image)
		{
			return convertToAwtImage((Gray8Image)image, alpha);
		}
		else
		if (image instanceof Gray16Image)
		{
			return convertToAwtImage((Gray16Image)image, alpha);
		}
		else
		if (image instanceof Paletted8Image)
		{
			return convertToAwtImage((Paletted8Image)image, alpha);
		}
		else
		if (image instanceof BilevelImage)
		{
			return convertToAwtImage((BilevelImage)image, alpha);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Convert a BilevelImage object to an AWT image object.
	 * @param image the image to be converted
	 * @param alpha the transparency value to be written to each
	 *  pixel in the resulting image
	 * @return newly-created AWT image
	 */
	public static Image convertToAwtImage(BilevelImage image, int alpha)
	{
		if (image == null)
		{
			return null;
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit == null)
		{
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		if (width < 1 || height < 1)
		{
			return null;
		}
		int bytesPerRow = (width + 7) / 8;
		int[] pixels = new int[width * height];
		byte[] row = new byte[bytesPerRow];
		int destOffset = 0;
		for (int y = 0; y < height; y++)
		{
			image.getPackedBytes(0, y, width, row, 0, 0);
			RGBA.convertFromPackedBilevel(row, 0, alpha, pixels, destOffset, width);
			destOffset += width;
		}
		return toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
	}

	/**
	 * Creates an AWT Image object from a Gray16Image object and an alpha value.
	 * This is done by allocating a new int array with image.getWidth() times
	 * image.getHeight() elements, copying the data to those ints (using transparency
	 * information from the top eight bits of the alpha argument) and calling
	 * Toolkit.createImage with a MemoryImageSource of those int[] pixels.
	 * @param image the grayscale image to be converted
	 * @param alpha the alpha value, bits must only be set in the top eight bits
	 * @return AWT image created from the argument input image
	 */
	public static Image convertToAwtImage(Gray16Image image, int alpha)
	{
		if (image == null)
		{
			return null;
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit == null)
		{
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		if (width < 1 || height < 1)
		{
			return null;
		}
		int[] pixels = new int[width * height];
		short[] gray = new short[width];
		int destOffset = 0;
		for (int y = 0; y < height; y++)
		{
			image.getShortSamples(0, 0, y, width, 1, gray, 0);
			RGBA.convertFromGray16(gray, 0, alpha, pixels, destOffset, width);
			destOffset += width;
		}
		return toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
	}

	/**
	 * Creates an AWT Image object from a Gray8Image object and an alpha value.
	 * This is done by allocating a new int array with image.getWidth() times
	 * image.getHeight() elements, copying the data to those ints (using transparency
	 * information from the top eight bits of the alpha argument) and calling
	 * Toolkit.createImage with a MemoryImageSource of those int[] pixels.
	 *
	 * @param image the grayscale image to be converted
	 * @param alpha the alpha value, bits must only be set in the top eight bits
	 * @return AWT image created from the argument input image
	 */
	public static Image convertToAwtImage(Gray8Image image, int alpha)
	{
		if (image == null)
		{
			return null;
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit == null)
		{
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		if (width < 1 || height < 1)
		{
			return null;
		}
		int[] pixels = new int[width * height];
		byte[] gray = new byte[width];
		int destOffset = 0;
		for (int y = 0; y < height; y++)
		{
			image.getByteSamples(0, 0, y, width, 1, gray, 0);
			RGBA.convertFromGray8(gray, 0, alpha, pixels, destOffset, width);
			destOffset += width;
		}
		return toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
	}

	public static Image convertToAwtImage(Paletted8Image image, int alpha)
	{
		if (image == null)
		{
			return null;
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit == null)
		{
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		Palette palette = image.getPalette();
		if (width < 1 || height < 1 || palette == null)
		{
			return null;
		}
		int[] red = new int[palette.getNumEntries()];
		int[] green = new int[palette.getNumEntries()];
		int[] blue = new int[palette.getNumEntries()];
		for (int i = 0; i < palette.getNumEntries(); i++)
		{
			red[i] = palette.getSample(RGBIndex.INDEX_RED, i);
			green[i] = palette.getSample(RGBIndex.INDEX_GREEN, i);
			blue[i] = palette.getSample(RGBIndex.INDEX_BLUE, i);
		}
		int[] pixels = new int[width * height];
		byte[] data = new byte[width];
		int destOffset = 0;
		for (int y = 0; y < height; y++)
		{
			image.getByteSamples(0, 0, y, width, 1, data, 0);
			RGBA.convertFromPaletted8(data, 0, alpha, red, green, blue, pixels, destOffset, width);
			destOffset += width;
		}
		return toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
		
	}

	public static Image convertToAwtImage(RGB24Image image, int alpha)
	{
		if (image == null)
		{
			return null;
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit == null)
		{
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		if (width < 1 || height < 1)
		{
			return null;
		}
		int[] pixels = new int[width * height];
		byte[] red = new byte[width];
		byte[] green = new byte[width];
		byte[] blue = new byte[width];
		int destOffset = 0;
		for (int y = 0; y < height; y++)
		{
			image.getByteSamples(RGBIndex.INDEX_RED, 0, y, width, 1, red, 0);
			image.getByteSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1, green, 0);
			image.getByteSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1, blue, 0);
			RGBA.convertFromRGB24(red, 0, green, 0, blue, 0, alpha, pixels, destOffset, width);
			destOffset += width;
		}
		return toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
		
	}

	public static Image convertToAwtImage(RGB48Image image, int alpha)
	{
		if (image == null)
		{
			return null;
		}
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit == null)
		{
			return null;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		if (width < 1 || height < 1)
		{
			return null;
		}
		int[] pixels = new int[width * height];
		short[] red = new short[width];
		short[] green = new short[width];
		short[] blue = new short[width];
		int destOffset = 0;
		for (int y = 0; y < height; y++)
		{
			image.getShortSamples(RGBIndex.INDEX_RED, 0, y, width, 1, red, 0);
			image.getShortSamples(RGBIndex.INDEX_GREEN, 0, y, width, 1, green, 0);
			image.getShortSamples(RGBIndex.INDEX_BLUE, 0, y, width, 1, blue, 0);
			RGBA.convertFromRGB48(red, 0, green, 0, blue, 0, alpha, pixels, destOffset, width);
			destOffset += width;
		}
		return toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
	}

	/**
	 * Creates an {@link RGB24Image} from the argument AWT image instance.
	 * @param image AWT image object to be converted to a {@link RGB24Image}
	 * @return a {@link RGB24Image} object holding the image data from the argument image
	 */
	public static RGB24Image convertImageToRGB24Image(Image image)
	{
		if (image == null)
		{
			return null;
		}
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		if (width < 1 || height < 1)
		{
			return null;
		}
		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
		try
		{
			pg.grabPixels();
		}
		catch (InterruptedException e)
		{
			return null;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0)
		{
			return null;
		}
		RGB24Image result = new MemoryRGB24Image(width, height);
		int offset = 0;
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int pixel = pixels[offset++] & 0xffffff;
				// TODO: store alpha value; requires some sort of 
				// transparency channel data type yet to be implemented
				result.putSample(RGBIndex.INDEX_RED, x, y, pixel >> 16);
				result.putSample(RGBIndex.INDEX_GREEN, x, y, (pixel >> 8) & 0xff);
				result.putSample(RGBIndex.INDEX_BLUE, x, y, pixel & 0xff);
			}
		}
		return result;
	}
}
