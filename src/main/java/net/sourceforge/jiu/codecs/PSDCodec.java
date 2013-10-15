/*
 * PSDCodec
 *
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.DataInput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * A codec to read images from Photoshop PSD files.
 * PSD was created by Adobe for their
 * <a href="http://www.adobe.com/store/products/photoshop.html">Photoshop</a>
 * image editing software.
 * Note that only a small subset of valid PSD files is supported by this codec.
 * Typical file extension is <code>.psd</code>.
 * @author Marco Schmidt
 */
public class PSDCodec extends ImageCodec
{
	private final static int MAGIC_8BPS = 0x38425053;
	private final static int COLOR_MODE_GRAYSCALE = 1;
	private final static int COLOR_MODE_INDEXED = 2;
	private final static int COLOR_MODE_RGB_TRUECOLOR = 3;
	private final static short COMPRESSION_NONE = 0;
	private final static short COMPRESSION_PACKBITS = 1;
	private int magic;
	private int channels;
	private int height;
	private int width;
	private int depth;
	private int colorMode;
	private short compression;
	private DataInput in;
	private Gray8Image gray8Image;
	private Palette palette;
	private Paletted8Image paletted8Image;
	private RGB24Image rgb24Image;

	private void allocate()
	{
		gray8Image = null;
		paletted8Image = null;
		rgb24Image = null;
		if (depth == 8 && colorMode == COLOR_MODE_RGB_TRUECOLOR)
		{
			rgb24Image = new MemoryRGB24Image(getBoundsWidth(), getBoundsHeight());
			setImage(rgb24Image);
		}
		else
		if (channels == 1 && depth == 8 && colorMode == 2)
		{
			paletted8Image = new MemoryPaletted8Image(width, height, palette);
			setImage(paletted8Image);
		}
		else
		if (channels == 1 && depth == 8 && colorMode == COLOR_MODE_GRAYSCALE)
		{
			gray8Image = new MemoryGray8Image(width, height);
			setImage(gray8Image);
		}
		else
		{
			throw new IllegalArgumentException("Unknown image type in PSD file.");
		}
	}

	private static String getColorTypeName(int colorMode)
	{
		switch(colorMode)
		{
			case(0): return "Black & white";
			case(1): return "Grayscale";
			case(2): return "Indexed";
			case(3): return "RGB truecolor";
			case(4): return "CMYK truecolor";
			case(7): return "Multichannel";
			case(8): return "Duotone";
			case(9): return "Lab";
			default: return "Unknown (" + colorMode + ")";
		}
	}

	public String getFormatName()
	{
		return "Photoshop (PSD)";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/psd", "image/x-psd"};
	}

	public boolean isLoadingSupported()
	{
		return true;
	}

	public boolean isSavingSupported()
	{
		return false;
	}

	/**
	 * Attempts to load an Image from argument stream <code>in</code> (which
	 * could, as an example, be a <code>RandomAccessFile</code> instance, it 
	 * implements the <code>DataInput</code> interface).
	 * Checks a magic byte sequence and then reads all chunks as they appear
	 * in the IFF file.
	 * Will return the resulting image or null if no image body chunk was
	 * encountered before end-of-stream.
	 * Will throw an exception if the file is corrupt, information is missing
	 * or there were reading errors.
	 */
	private void load() throws
		InvalidFileStructureException,
		IOException, 
		UnsupportedTypeException,
		WrongFileFormatException
	{
		loadHeader();
		//System.out.println(width + " x " + height + ", color=" + colorMode + ", channels=" + channels + ", depth=" + depth);
		// check values
		if (width < 1 || height < 1)
		{
			throw new InvalidFileStructureException("Cannot load image. " +
				"Invalid pixel resolution in PSD file header (" + width +
				" x " + height + ").");
		}
		if (colorMode != COLOR_MODE_RGB_TRUECOLOR &&
		    colorMode != COLOR_MODE_GRAYSCALE &&
		    colorMode != COLOR_MODE_INDEXED)
		{
			throw new UnsupportedTypeException("Cannot load image. Only RGB" +
				" truecolor and indexed color are supported for PSD files. " +
				"Found: " +getColorTypeName(colorMode));
		}
		if (depth != 8)
		{
			throw new UnsupportedTypeException("Cannot load image. Only a depth of 8 bits " +
				"per channel is supported (found " + depth + 
				" bits).");
		}

		// COLOR MODE DATA
		int colorModeSize = in.readInt();
		//System.out.println("colorModeSize=" + colorModeSize);
		byte[] colorMap = null;
		if (colorMode == COLOR_MODE_INDEXED)
		{
			if (colorModeSize != 768)
			{
				throw new InvalidFileStructureException("Cannot load image." +
					" Color map length was expected to be 768 (found " + 
					colorModeSize + ").");
			}
			colorMap = new byte[colorModeSize];
			in.readFully(colorMap);
			palette = new Palette(256, 255);
			for (int index = 0; index < 256; index++)
			{
				palette.putSample(Palette.INDEX_RED, index, colorMap[index] & 0xff);
				palette.putSample(Palette.INDEX_GREEN, index, colorMap[256 + index] & 0xff);
				palette.putSample(Palette.INDEX_BLUE, index, colorMap[512 + index] & 0xff);
			}
		}
		else
		{
			in.skipBytes(colorModeSize);
		}
		// IMAGE RESOURCES
		int resourceLength = in.readInt();
		in.skipBytes(resourceLength);
		//System.out.println("resourceLength=" + resourceLength);
		// LAYER AND MASK INFORMATION
		int miscLength = in.readInt();
		in.skipBytes(miscLength);
		//System.out.println("miscLength=" + miscLength);
		// IMAGE DATA
		compression = in.readShort();
		if (compression != COMPRESSION_NONE && compression != COMPRESSION_PACKBITS)
		{
			throw new UnsupportedTypeException("Cannot load image. Unsupported PSD " +
				"compression type (" + compression + ")");
		}
		//System.out.println("compression=" + compression);
		loadImageData();
	}

	/**
	 * Reads the PSD header to private members of this class instance.
	 * @throws IOException if there were reading errors
	 */
	private void loadHeader() throws
		IOException,
		WrongFileFormatException
	{
		magic = in.readInt();
		if (magic != MAGIC_8BPS)
		{
			throw new WrongFileFormatException("Not a valid PSD file " +
				"(wrong magic byte sequence).");
		}
		in.readShort(); // skip version short value
		in.skipBytes(6);
		channels = in.readShort();
		height = in.readInt();
		width = in.readInt();
		depth = in.readShort();
		colorMode = in.readShort();
	}

	private void loadPackbitsCompressedData(byte[] data, int offset, int num) throws
		InvalidFileStructureException,
		IOException
	{
		int x = offset;
		int max = offset + num;
		while (x < max)
		{
			byte n = in.readByte();
			boolean compressed = false;
			int count = -1;
			try
			{
				if (n >= 0)
				{
					// copy next n + 1 bytes literally
					in.readFully(data, x, n + 1);
					x += (n + 1);
				}
				else
				{
					// if n == -128, nothing happens (stupid design decision)
					if (n != -128)
					{
						compressed = true;
						// otherwise, compute counter
						count = -((int)n) + 1;
						// read another byte
						byte value = in.readByte();
						// write this byte counter times to output
						while (count-- > 0)
						{
							data[x++] = value;
						}
					}
				}
			}
			catch (ArrayIndexOutOfBoundsException ioobe)
			{
				/* if the encoder did anything wrong, the above code
				   could potentially write beyond array boundaries
				   (e.g. if runs of data exceed line boundaries);
				   this would result in an IndexOutOfBoundsException
				   thrown by the virtual machine;
				   to give a more understandable error message to the 
				   user, this exception is caught here and a
				   corresponding IOException is thrown */
				throw new InvalidFileStructureException("Error: RLE-compressed image " +
					"file seems to be corrupt (x=" + x +
					", count=" + (compressed ? (-((int)n) + 1) : n) + 
					", compressed=" + (compressed ? "y" : "n") + ", array length=" + data.length + ").");
			}
		}
	}

	private void loadImageData() throws 
		InvalidFileStructureException, 
		IOException
	{
		setBoundsIfNecessary(width, height);
		allocate();
		if (compression == COMPRESSION_PACKBITS)
		{
			// skip counters
			in.skipBytes(2 * channels * height);
		}
		byte[] data = new byte[width];
		int totalScanLines = channels * height;
		int currentScanLine = 0;
		for (int c = 0; c < channels; c++)
		{
			for (int y = 0, destY = - getBoundsY1(); y < height; y++, destY++)
			{
				if (compression == COMPRESSION_PACKBITS)
				{
					loadPackbitsCompressedData(data, 0, width);
				}
				else
				{
					if (compression == COMPRESSION_PACKBITS)
					{
						in.readFully(data, 0, width);
					}
				}
				setProgress(currentScanLine++, totalScanLines);
				if (!isRowRequired(y))
				{
					continue;
				}
				if (rgb24Image != null)
				{
					int channelIndex = RGB24Image.INDEX_RED;
					if (c == 1)
					{
						channelIndex = RGB24Image.INDEX_GREEN;
					}
					if (c == 2)
					{
						channelIndex = RGB24Image.INDEX_BLUE;
					}
					rgb24Image.putByteSamples(channelIndex, 0, destY, getBoundsWidth(), 1, data, getBoundsX1());
				}
				if (gray8Image != null)
				{
					gray8Image.putByteSamples(0, 0, destY, getBoundsWidth(), 1, data, getBoundsX1());
				}
				if (paletted8Image != null)
				{
					paletted8Image.putByteSamples(0, 0, destY, getBoundsWidth(), 1, data, getBoundsX1());
				}
			}
		}
	}

	public void process() throws
		OperationFailedException
	{
		initModeFromIOObjects();
		try
		{
			if (getMode() == CodecMode.LOAD)
			{
				in = getInputAsDataInput();
				if (in == null)
				{
					throw new MissingParameterException("Input stream / file missing.");
				}
				load();
			}
			else
			{
				throw new OperationFailedException("Only loading is supported in PSD codec.");
			}
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("I/O error: " + ioe.toString());
		}
	}
}
