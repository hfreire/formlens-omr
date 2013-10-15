/*
 * PaletteSerialization
 *
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import net.sourceforge.jiu.codecs.ImageLoader;
import net.sourceforge.jiu.codecs.PNMCodec;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * This class loads and saves palettes.
 * Loading is done using the {@link ImageLoader} class - an image
 * is loaded which is supposed to have no more than 256 pixels, the palette entries.
 * When saving, the {@link PNMCodec} is used to store palettes as .ppm files.
 *
 * @author Marco Schmidt
 * @since 0.5.0
 */
public class PaletteSerialization implements RGBIndex
{
	private PaletteSerialization()
	{
	}

	/**
	 * Create a palette from the pixels of the argument image.
	 */
	public static Palette convertImageToPalette(RGB24Image image)
	{
		if (image == null)
		{
			return null;
		}
		int numPixels = image.getWidth() * image.getHeight();
		if (numPixels > 256)
		{
			// too many pixels
			return null;
		}
		Palette result = new Palette(numPixels, 255);
		int index = 0;
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				result.put(index++, image.getSample(INDEX_RED, x, y), 
					image.getSample(INDEX_GREEN, x, y), 
					image.getSample(INDEX_BLUE, x, y));
			}
		}
		return result;
	}

	/**
	 * Creates an RGB24Image from the palette entries, each entry
	 * becomes a pixel in an image of width 1 and height
	 * palette.getNumEntries().
	 */
	public static RGB24Image convertPaletteToImage(Palette palette)
	{
		RGB24Image result = new MemoryRGB24Image(1, palette.getNumEntries());
		for (int index = 0; index < palette.getNumEntries(); index++)
		{
			result.putSample(INDEX_RED, 0, index, palette.getSample(INDEX_RED, index));
			result.putSample(INDEX_GREEN, 0, index, palette.getSample(INDEX_GREEN, index));
			result.putSample(INDEX_BLUE, 0, index, palette.getSample(INDEX_BLUE, index));
		}
		return result;
	}

	/**
	 * Loads a palette from the argument file.
	 * Uses {@link net.sourceforge.jiu.codecs.ImageLoader} to load an
	 * image from the argument file, then calls {@link #convertImageToPalette}
	 * and returns the palette created that way.
	 */
	public static Palette load(File paletteFile)
	{
		PixelImage image;
		try
		{
			image = ImageLoader.load(paletteFile, (Vector)null);
		}
		catch (Exception e)
		{
			return null;
		}
		if (!(image instanceof RGB24Image))
		{
			return null;
		}
		return convertImageToPalette((RGB24Image)image);
	}

	/** 
	 * Saves the palette to the given file as a PPM image file.
	 * Uses {@link net.sourceforge.jiu.codecs.PNMCodec}.
	 */
	public static void save(Palette palette, File paletteFile) throws
		IOException
	{
		RGB24Image image = convertPaletteToImage(palette);
		PNMCodec codec = new PNMCodec();
		codec.setOutputStream(new FileOutputStream(paletteFile));
		codec.setAscii(true);
		codec.setImage(image);
		try
		{
			codec.process();
		}
		catch(OperationFailedException ofe)
		{
			throw new IOException("I/O error: " + ofe.toString());
		}
	}
}
