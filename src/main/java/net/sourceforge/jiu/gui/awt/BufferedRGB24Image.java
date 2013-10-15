/*
 * BufferedRGB24Image
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import java.awt.image.BufferedImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;

/**
 * A bridge class to use {@link java.awt.image.BufferedImage} objects (class defined
 * in the standard runtime library, package <code>java.awt.image</code>) as 
 * {@link net.sourceforge.jiu.data.RGB24Image} objects within JIU.
 * This class encapsulates a single {@link java.awt.image.BufferedImage} object.
 * It enables reusing existing BufferedImage objects as input or
 * output of JIU operations, 
 * removing the necessity for the conversion step from <code>java.awt.Image</code> 
 * to <code>net.sourceforge.jiu.data.PixelImage</code> (or vice versa) 
 * and thus reducing memory consumption.
 * The name of this class is a combination of BufferedImage (the class of the object
 * that is encapsulated) and RGB24Image (the JIU image data interface).
 * <p>
 * Internally, this class uses {@link java.awt.image.BufferedImage}'s getRGB and 
 * setRGB methods to access image data.
 * This approach is slower than working directly on the BufferedImage's data
 * buffers.
 * However, using getRGB and setRGB, this class will work with all types of BufferedImage objects.
 * <p>
 * Note that while the abstract <code>java.awt.Image</code> class existed from the very
 * beginning (version 1.0) of the Java runtime library, <code>java.awt.image.BufferedImage</code>
 * has not been added until version 1.2.
 * <h3>Usage example</h3>
 * This code snippet demonstrates to how combine functionality from Java's runtime
 * library with JIU by using this class.
 * Requires Java 1.4 or higher.
 * Obviously, BufferedRGB24Image objects can only be used with operations that
 * work on classes implementing RGB24Image.
 * <pre>
 *  import java.awt.image.BufferedImage;
 *  import java.io.File;
 *  import javax.imageio.ImageIO;
 *  import net.sourceforge.jiu.color.Invert;
 *  import net.sourceforge.jiu.data.PixelImage;
 *  import net.sourceforge.jiu.gui.awt.BufferedRGB24Image;
 *  ...
 *  BufferedImage bufferedImage = ImageIO.read(new File("image.jpg"));
 *  BufferedRGB24Image image = new BufferedRGB24Image(bufferedImage);
 *  Invert invert = new Invert();
 *  invert.setInputImage(image);
 *  invert.process();
 *  PixelImage outputImage = invert.getOutputImage();
 * </pre>
 * If you can be sure that an image object can be input and output
 * image at the same time (as is the case with some operations), you 
 * can even work with only one BufferedRGB24Image object.
 * Invert is one of these operations, so the following would work:
 * <pre>
 *  Invert invert = new Invert();
 *  invert.setInputImage(image);
 *  invert.setOutputImage(image);
 *  invert.process();
 *  // image now is inverted
 * </pre>
 *
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class BufferedRGB24Image implements RGB24Image
{
	private static final int RED_SHIFT = 16;
	private static final int GREEN_SHIFT = 8;
	private static final int BLUE_SHIFT = 0;

	/** 
	 * Masks for the three RGB channels.
	 * RGB_CLEAR[i] is an int with all bits on, except for those occupied by the channel i.
	 * RGB_CLEAR[i] can thus be used to bitwise AND an ARGB value so that the sample for channel i will be cleared.
	 */
	private static final int[] RGB_CLEAR = new int[3];
	private static final int[] RGB_SHIFT = new int[3];

	static
	{
		RGB_SHIFT[INDEX_RED] = RED_SHIFT;
		RGB_SHIFT[INDEX_GREEN] = GREEN_SHIFT;
		RGB_SHIFT[INDEX_BLUE] = BLUE_SHIFT;
		RGB_CLEAR[INDEX_RED] = 0xff00ffff;
		RGB_CLEAR[INDEX_GREEN] = 0xffff00ff;
		RGB_CLEAR[INDEX_BLUE] = 0xffffff00;
	}

	private final int HEIGHT;
	private final BufferedImage image;
	private final int WIDTH;

	/**
	 * Creates a new BufferedRGB24Image object, storing the argument
	 * BufferedImage object internally.
	 * All image data access will be delegated to that BufferedImage object's methods.
	 * @param bufferedImage the underlying BufferedImage object for this BufferedRGB24Image object
	 */
	public BufferedRGB24Image(BufferedImage bufferedImage)
	{
		image = bufferedImage;
		if (image == null)
		{
			throw new IllegalArgumentException("Argument image object must not be null.");
		}
		WIDTH = image.getWidth();
		HEIGHT = image.getHeight();
	}

	/**
	 * Sets all the RGB samples in this image to the argument, keeping
	 * the alpha value.
	 * @param newValue all samples in the image will be set to this value
	 */
	public void clear(byte newValue)
	{
		final int RGB = (newValue & 0xff) | (newValue & 0xff) << 8 | (newValue & 0xff) << 16;
		for (int y = 0; y < getHeight(); y++)
		{
			for (int x = 0; x < getWidth(); x++)
			{
				int rgba = image.getRGB(x, y);
				rgba = (rgba & 0xff000000) | RGB;
				image.setRGB(x, y, rgba);
			}
		}
	}

	public void clear(int newValue)
	{
		clear((byte)newValue);
	}

	public void clear(int channelIndex, byte newValue)
	{
		final int MASK = RGB_CLEAR[channelIndex];
		final int SAMPLE = (newValue & 0xff) << RGB_SHIFT[channelIndex];
		for (int y = 0; y < getHeight(); y++)
		{
			for (int x = 0; x < getWidth(); x++)
			{
				int rgba = image.getRGB(x, y);
				rgba = (rgba & MASK) | SAMPLE;
				image.setRGB(x, y, rgba);
			}
		}
	}

	public void clear(int channelIndex, int newValue)
	{
		clear(channelIndex, (byte)newValue);
	}

	public PixelImage createCompatibleImage(int width, int height)
	{
		BufferedImage newBufferedImage = new BufferedImage(width, height, image.getType());
		return new BufferedRGB24Image(newBufferedImage);
	}

	public PixelImage createCopy()
	{
		BufferedImage newBufferedImage = new BufferedImage(getWidth(), getHeight(), image.getType());
		image.copyData(newBufferedImage.getRaster());
		return new BufferedRGB24Image(newBufferedImage);
	}

	public long getAllocatedMemory()
	{
		/* actually, number of pixels times 4 is just a guess,
		   BufferedImage allows for all kinds of data buffers;
		   for a more accurate approximation these data buffers
		   must be examined */
		return 4L * (long)getWidth() * (long)getHeight();
	}

	public int getBitsPerPixel()
	{
		return 24;
	}

	public byte getByteSample(int x, int y)
	{
		return getByteSample(0, x, y);
	}

	public byte getByteSample(int channelIndex, int x, int y)
	{
		return (byte)((image.getRGB(x, y) >> RGB_SHIFT[channelIndex]) & 0xff);
	}

	public void getByteSamples(int channelIndex, int x, int y, int w, int h, byte[] dest, int destOffset)
	{
		final int SHIFT = RGB_SHIFT[channelIndex];
		int[] row = new int[w];
		while (h-- > 0)
		{
			image.getRGB(x, y++, w, 1, row, 0, w);
			int columns = w;
			int rowIndex = 0;
			while (columns-- > 0)
			{
				dest[destOffset++] = (byte)((row[rowIndex++] >> SHIFT) & 0xff);
			}
		}
	}

	public void getByteSamples(int x, int y, int w, int h, byte[] dest, int destOffset)
	{
		getByteSamples(0, x, y, w, h, dest, destOffset);
	}

	public int getHeight()
	{
		return HEIGHT;
	}

	public Class getImageType()
	{
		return RGB24Image.class;
	}

	public int getMaxSample(int channel)
	{
		if (channel == INDEX_BLUE || channel == INDEX_RED || channel == INDEX_GREEN)
		{
			return 255;
		}
		else
		{
			throw new IllegalArgumentException("Not a valid channel index: "  + channel);
		}
	}

	public int getNumChannels()
	{
		return 3;
	}

	public int getSample(int x, int y)
	{
		return getSample(0, x, y);
	}

	public int getSample(int channelIndex, int x, int y)
	{
		return (image.getRGB(x, y) >> RGB_SHIFT[channelIndex]) & 0xff;
	}

	public void getSamples(int x, int y, int w, int h, int[] dest, int destOffs)
	{
		getSamples(0, x, y, w, h, dest, destOffs);
	}

	public void getSamples(int channelIndex, int x, int y, int w, int h, int[] dest, int destOffs)
	{
		final int SHIFT = RGB_SHIFT[channelIndex];
		int[] row = new int[w];
		while (h-- > 0)
		{
			image.getRGB(x, y++, w, 1, row, 0, w);
			int columns = w;
			int rowIndex = 0;
			while (columns-- > 0)
			{
				dest[destOffs++] = (row[rowIndex++] >> SHIFT) & 0xff;
			}
		}
	}

	public int getWidth()
	{
		return WIDTH;
	}

	public void putByteSample(int channelIndex, int x, int y, byte newValue)
	{
		int argb = image.getRGB(x, y) & RGB_CLEAR[channelIndex];
		image.setRGB(x, y, argb | ((newValue & 0xff) << RGB_SHIFT[channelIndex]));
	}

	public void putByteSample(int x, int y, byte newValue)
	{
		putByteSample(0, x, y, newValue);
	}

	public void putByteSamples(int channelIndex, int x, int y, int w, int h, byte[] src, int srcOffset)
	{
		final int SHIFT = RGB_SHIFT[channelIndex];
		final int MASK = RGB_CLEAR[channelIndex];
		int[] row = new int[w];
		while (h-- > 0)
		{
			image.getRGB(x, y, w, 1, row, 0, w);
			int columns = w;
			int rowIndex = 0;
			while (columns-- > 0)
			{
				int argb = row[rowIndex] & MASK;
				row[rowIndex++] = argb | ((src[srcOffset++] & 0xff) << SHIFT);
			}
			image.setRGB(x, y++, w, 1, row, 0, w);
		}
	}

	public void putByteSamples(int x, int y, int w, int h, byte[] src, int srcOffset)
	{
		putByteSamples(0, x, y, w, h, src, srcOffset);
	}

	public void putSample(int x, int y, int newValue)
	{
		putSample(0, x, y, newValue);
	}

	public void putSample(int channelIndex, int x, int y, int newValue)
	{
		int argb = image.getRGB(x, y) & RGB_CLEAR[channelIndex];
		image.setRGB(x, y, argb | (newValue << RGB_SHIFT[channelIndex]));
	}

	public void putSamples(int channelIndex, int x, int y, int w, int h, int[] src, int srcOffset)
	{
		final int SHIFT = RGB_SHIFT[channelIndex];
		final int MASK = RGB_CLEAR[channelIndex];
		int[] row = new int[w];
		while (h-- > 0)
		{
			image.getRGB(x, y, w, 1, row, 0, w);
			int columns = w;
			int rowIndex = 0;
			while (columns-- > 0)
			{
				int argb = row[rowIndex] & MASK;
				row[rowIndex++] = argb | (src[srcOffset++] << SHIFT);
			}
			image.setRGB(x, y++, w, 1, row, 0, w);
		}
	}
}