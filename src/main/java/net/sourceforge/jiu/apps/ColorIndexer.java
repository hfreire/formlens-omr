/*
 * ColorIndexer
 *
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */
package net.sourceforge.jiu.apps;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import net.sourceforge.jiu.color.adjustment.Contrast;
import net.sourceforge.jiu.color.promotion.PromotionRGB24;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.geometry.Resample;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;
import net.sourceforge.jiu.ops.BatchProcessorOperation;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * Loads image files and generates color index information for them.
 * @author Marco Schmidt
 * @since 0.12.0
 */
public class ColorIndexer extends BatchProcessorOperation
{
	private int maxLength = 256;
	private int contrastChange = 100;
	private NumberFormat formatter = new DecimalFormat("#.##");

	public static final int BLACK = 0;
	public static final int RED = 4;
	public static final int GREEN = 2;
	public static final int BLUE = 1;
	public static final int YELLOW = 6;
	public static final int MAGENTA = 5;
	public static final int CYAN = 3;
	public static final int WHITE = 7;
	public static final String[] COLOR_NAMES =
		{"black", "blue", "green", "cyan", "red", "magenta", "yellow", "white"};

	public static void main(String[] args)
	{
		ColorIndexer indexer = new ColorIndexer();
		for (int i = 0; i < args.length; i++)
		{
			String name = args[i];
			File file = new File(name);
			if (file.isFile())
			{
				indexer.addInputFileName(name);
			}
			else
			if (file.isDirectory())
			{
				indexer.addDirectoryTree(name);
			}
		}
		indexer.process();
		System.out.println("Done.");
	}

	private PixelImage convertToRgb24(PixelImage in)
	{
		if (in == null)
		{
			return null;
		}
		if (in instanceof RGB24Image)
		{
			return in;
		}
		try
		{
			PromotionRGB24 pr = new PromotionRGB24();
			pr.setInputImage(in);
			pr.process();
			return pr.getOutputImage();
		}
		catch (OperationFailedException ofe)
		{
			return null;
		}
	}

	private PixelImage adjustColor(PixelImage img)
	{
		if (img == null || contrastChange == 0)
		{
			return img;
		}
		try
		{
			Contrast con = new Contrast();
			con.setInputImage(img);
			con.setContrast(contrastChange);
			con.process();
			return con.getOutputImage();
/*			HueSaturationValue hsv = new HueSaturationValue();
			hsv.setInputImage(img);
			hsv.setSaturationValue(30, 0);
			hsv.process();
			return hsv.getOutputImage();*/
		}
		catch (OperationFailedException ofe)
		{
			return null;
		}
	}

	private PixelImage scale(PixelImage in)
	{
		if (in == null)
		{
			return null;
		}
		if (in.getWidth() <= maxLength && in.getHeight() <= maxLength)
		{
			return in;
		}
		try
		{
			Resample res = new Resample();
			res.setFilter(Resample.FILTER_TYPE_LANCZOS3);
			res.setInputImage(in);
			float thumbRatio = 1.0f;
			float imageRatio = (float)in.getWidth() / (float)in.getHeight();
			int width = maxLength;
			int height = maxLength;
			if (thumbRatio < imageRatio)
			{
			  height = (int)(maxLength / imageRatio);
			}
			else
			{
			  width = (int)(maxLength * imageRatio);
			}
			//float x = (float)in.getWidth() / maxLength;
			//float y = (float)in.getHeight() / maxLength;
			res.setSize(width, height);
			res.process();
			return res.getOutputImage();
		}
		catch (OperationFailedException ofe)
		{
			return null;
		}
	}

	private int[] count(PixelImage image)
	{
		if (image == null)
		{
			return null;
		}
		RGB24Image in = (RGB24Image)image;
		int[] result = new int[8];
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int red = in.getSample(RGBIndex.INDEX_RED, x, y) >> 7;
				int green = in.getSample(RGBIndex.INDEX_GREEN, x, y) >> 7;
				int blue = in.getSample(RGBIndex.INDEX_BLUE, x, y) >> 7;
				int index = (red << 2) | (green << 1) | blue;
				result[index]++;
			}
		}
		return result;
	}

	/*private void save(PixelImage img, String fileName)
	{
		PNGCodec codec = new PNGCodec();
		try
		{
			codec.setImage(img);
			codec.setFile(fileName, CodecMode.SAVE);
			codec.process();
			codec.close();
		}
		catch (OperationFailedException ofe)
		{
		}
		catch (IOException ioe)
		{
		}
	}*/

	private void store(String name, int[] occ)
	{
		if (name == null || occ == null)
		{
			return;
		}
		int sum = 0;
		for (int i = 0; i < occ.length; i++)
		{
			sum += occ[i];
		}
		System.out.print(name);
		System.out.print(';');
		for (int i = 0; i < occ.length; i++)
		{
			float rel = (float)occ[i] / sum;
			if (rel < 0.01f)
			{
				continue;
			}
			System.out.print(COLOR_NAMES[i] + " = " + formatter.format(rel) + ";");
		}
		System.out.println();
	}

	public void processFile(String inputDirectory, String inputFileName, String outputDirectory)
	{
		File dir = new File(inputDirectory);
		File file = new File(dir, inputFileName);
		String name = file.getAbsolutePath();
		// load image
		PixelImage image = ToolkitLoader.loadViaToolkitOrCodecs(name);
		// convert to RGB24 if necessary
		image = convertToRgb24(image);
		// scale down if necessary
		image = scale(image);
		// increase contrast
		image = adjustColor(image);
		// save the modified image, for testing purposes
		/*File outFile = new File("f:\\", "th_" + inputFileName + ".png");
		save(image, outFile.getAbsolutePath());*/
		// determine occurrence of colors
		int[] occur = count(image);
		// store
		store(name, occur);
	}
}
