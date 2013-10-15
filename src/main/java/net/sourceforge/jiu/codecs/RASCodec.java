/*
 * RASCodec
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;
import net.sourceforge.jiu.util.ArrayConverter;

/**
 * A codec to read and write Sun Raster (RAS) image files.
 * The typical file extension for this format is <code>.ras</code>.
 * <h3>Usage example</h3>
 * This code snippet demonstrate how to read a RAS file.
 * <pre>
 * RASCodec codec = new RASCodec();
 * codec.setFile("image.ras", CodecMode.LOAD);
 * codec.process();
 * PixelImage loadedImage = codec.getImage();
 * </pre>
 * <h3>Supported file types when reading</h3>
 * Only uncompressed RAS files are read.
 * Only 8 bit (gray and paletted) and 24 bit are supported when reading.
 * <h3>Supported image types when writing</h3>
 * Only {@link net.sourceforge.jiu.data.Paletted8Image} / uncompressed is supported when writing.
 * <h3>Bounds</h3>
 * The bounds concept of ImageCodec is supported so that you can load or save only part of an image.
 * <h3>File format documentation</h3>
 * This file format is documented as a man page <code>rasterfile(5)</code> on Sun Unix systems.
 * That documentation can also be found online, e.g. at
 * <a target="_top" href="http://www.doc.ic.ac.uk/~mac/manuals/sunos-manual-pages/sunos4/usr/man/man5/rasterfile.5.html">http://www.doc.ic.ac.uk/~mac/manuals/sunos-manual-pages/sunos4/usr/man/man5/rasterfile.5.html</a>.
 * A <a target="_top" href="http://www.google.com/search?q=rasterfile%285%29&sourceid=opera&num=0">web search for rasterfile(5)</a>
 * brings up other places as well.
 *
 * @author Marco Schmidt
 */
public class RASCodec extends ImageCodec
{
	private static final int RAS_MAGIC = 0x59a66a95;
	private static final int COMPRESSION_NONE = 0x00000001;
	private static final int RAS_HEADER_SIZE = 32;
	private int width;
	private int height;
	private int depth;
	private int length;
	private int type;
	private int mapType;
	private int mapLength;
	private int bytesPerRow;
	private int paddingBytes;
	private int numColors;
	private DataInput in;
	private DataOutput out;
	private Palette palette;

	public String getFormatName()
	{
		return "Sun Raster (RAS)";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/x-ras"};
	}

	public boolean isLoadingSupported()
	{
		return true;
	}

	public boolean isSavingSupported()
	{
		return true;
	}

	/**
	 * Loads an image from an RAS input stream.
	 * It is assumed that a stream was given to this codec using {@link #setInputStream(InputStream)}.
	 *
	 * @return the image as an instance of a class that implements {@link IntegerImage}
	 * @throws InvalidFileStructureException if the input stream is corrupt
	 * @throws java.io.IOException if there were problems reading from the input stream
	 * @throws UnsupportedTypeException if an unsupported flavor of the RAS format is encountered
	 * @throws WrongFileFormatException if this is not a valid RAS stream
	 */
	private void load() throws 
		IOException,
		OperationFailedException
	{
		in = getInputAsDataInput();
		readHeader();
		readImage();
	}

	public void process() throws OperationFailedException
	{
		try
		{
			initModeFromIOObjects();
			if (getMode() == CodecMode.LOAD)
			{
				load();
			}
			else
			if (getMode() == CodecMode.SAVE)
			{
				save();
			}
			else
			{
				throw new WrongParameterException("Could find neither objects for loading nor for saving.");
			}
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("I/O error in RAS codec: " + ioe.toString());
		}
	}

	private void readHeader() throws 
		InvalidFileStructureException,
		UnsupportedTypeException,
		WrongFileFormatException,
		WrongParameterException,
		java.io.IOException
	{
		byte[] header = new byte[RAS_HEADER_SIZE];
		in.readFully(header);
		int magic = ArrayConverter.getIntBE(header, 0);
		if (magic != RAS_MAGIC)
		{
			throw new WrongFileFormatException("This stream is not a valid " +
				"Sun RAS stream (bad magic: " + Integer.toHexString(magic) +
				" instead of " + Integer.toHexString(RAS_MAGIC));
		}
		width = ArrayConverter.getIntBE(header, 4);
		height = ArrayConverter.getIntBE(header, 8);
		if (width < 1 || height < 1)
		{
			throw new InvalidFileStructureException("Width and height must both " +
				"be larger than zero; found width=" + width + ", height=" + 
				height + ".");
		}
		setBoundsIfNecessary(width, height);
		checkBounds(width, height);
		depth = ArrayConverter.getIntBE(header, 12);
		switch (depth)
		{
			case(1):
			{
				bytesPerRow = (width + 7) / 8;
				break;
			}
			case(8):
			{
				bytesPerRow = width;
				break;
			}
			case(24):
			{
				bytesPerRow = width * 3;
				break;
			}
			default:
			{
				throw new UnsupportedTypeException("Depths other than 1, 8 and 24 " +
					"unsupported when reading RAS stream; found " + depth);
			}
		}
		paddingBytes = (bytesPerRow % 2);
		numColors = 1 << depth;
		//length = ArrayConverter.getIntBE(header, 16);
		type = ArrayConverter.getIntBE(header, 20);
		if (type != COMPRESSION_NONE)
		{
			throw new UnsupportedTypeException("Only uncompressed " +
				"RAS streams are read; found " + type);
		}
		mapType = ArrayConverter.getIntBE(header, 24);
		mapLength = ArrayConverter.getIntBE(header, 28);
		if (mapLength != 0)
		{
			if (depth != 8)
			{
				throw new UnsupportedTypeException("Cannot handle Sun RAS " +
					"input streams with color maps and a depth other than " +
					"8 (found " + depth + ").");
			}
			if (mapLength != 768)
			{
				throw new UnsupportedTypeException("Cannot handle Sun RAS " +
					"input streams with color maps of a length different " +
					"than 768; found " + mapLength);
			}
			if (mapType != 1)
			{
				throw new UnsupportedTypeException("Cannot handle Sun RAS " +
					"input streams with color maps of a type other than " +
					"1; found " + mapType);
			}
			palette = readPalette();
		}
		else
		{
			palette = null;
		}
	}

	private IntegerImage readImage() throws 
		InvalidFileStructureException,
		java.io.IOException
	{
		RGB24Image rgb24Image = null;
		Paletted8Image paletted8Image = null;
		IntegerImage result = null;
		int numChannels = 1;
		int bytesPerRow = 0;
		switch(depth)
		{
			case(8):
			{
				paletted8Image = new MemoryPaletted8Image(width, height, palette);
				result = paletted8Image;
				numChannels = 1;
				bytesPerRow = width;
				break;
			}
			case(24):
			{
				rgb24Image = new MemoryRGB24Image(width, height);
				result = rgb24Image;
				numChannels = 3;
				bytesPerRow = width;
				break;
			}
		}
		setImage(result);
		byte[][] buffer = new byte[numChannels][];
		for (int i = 0; i < numChannels; i++)
		{
			buffer[i] = new byte[bytesPerRow];
		}
		for (int y = 0, destY = -getBoundsY1(); destY <= getBoundsY2(); y++, destY++)
		{
			if (rgb24Image != null)
			{
				for (int x = 0; x < width; x++)
				{
					buffer[RGBIndex.INDEX_BLUE][x] = in.readByte();
					buffer[RGBIndex.INDEX_GREEN][x] = in.readByte();
					buffer[RGBIndex.INDEX_RED][x] = in.readByte();
				}
				rgb24Image.putByteSamples(RGBIndex.INDEX_RED, 0, destY, getBoundsWidth(), 1, buffer[0], getBoundsX1());
				rgb24Image.putByteSamples(RGBIndex.INDEX_GREEN, 0, destY, getBoundsWidth(), 1, buffer[1], getBoundsX1());
				rgb24Image.putByteSamples(RGBIndex.INDEX_BLUE, 0, destY, getBoundsWidth(), 1, buffer[2], getBoundsX1());
			}
			else
			if (paletted8Image != null)
			{
				in.readFully(buffer[0], 0, width);
				paletted8Image.putByteSamples(0, 0, destY, getBoundsWidth(), 1, buffer[0], getBoundsX1());
			}
			if (in.skipBytes(paddingBytes) != paddingBytes)
			{
				throw new InvalidFileStructureException("Could not skip " +
					"byte after row " + y + ".");
			}
			setProgress(y, getBoundsY2() + 1);
		}
		return result;
	}

	private Palette readPalette() throws 
		InvalidFileStructureException,
		java.io.IOException
	{
		Palette result = new Palette(256, 255);
		for (int channel = 0; channel < 3; channel++)
		{
			int channelIndex = -1;
			switch(channel)
			{
				case(0):
				{
					channelIndex = Palette.INDEX_RED;
					break;
				}
				case(1):
				{
					channelIndex = Palette.INDEX_GREEN;
					break;
				}
				case(2):
				{
					channelIndex = Palette.INDEX_BLUE;
					break;
				}
			}
			for (int i = 0; i < numColors; i++)
			{
				int value = in.readUnsignedByte();
				if (value == -1)
				{
					throw new InvalidFileStructureException("Unexpected end " +
						"of file when reading Sun RAS palette.");
				}
				result.putSample(channelIndex, i, value);
			}
		}
		return result;
	}

	private void save() throws 
		IOException,
		UnsupportedTypeException,
		WrongParameterException
	{
		PixelImage image = getImage();
		if (image == null || (!(image instanceof Paletted8Image)))
		{
			throw new UnsupportedTypeException("Must have non-null image that is a Paletted8Image.");
		}
		saveHeader(image);
		if (image instanceof Paletted8Image)
		{
			saveData((Paletted8Image)image);
		}
	}

	private void saveData(Paletted8Image image) throws IOException
	{
		byte[] row = new byte[getBoundsWidth()];
		for (int y1 = 0, y2 = getBoundsY1(); y1 < getBoundsHeight(); y1++, y2++)
		{
			image.getByteSamples(0, getBoundsX1(), y2, row.length, 1, row, 0);
			out.write(row);
			int num = paddingBytes;
			while (num-- > 0)
			{
				out.write(0);
			}
			setProgress(y1, getBoundsHeight());
		}
	}

	private void saveHeader(PixelImage image) throws 
		IOException,
		UnsupportedTypeException,
		WrongParameterException
		
	{
		setBoundsIfNecessary(width, height);
		checkBounds(width, height);
		out.writeInt(RAS_MAGIC);
		int width = getBoundsWidth();
		out.writeInt(width);
		int height = getBoundsHeight();
		out.writeInt(height);
		if (image instanceof BilevelImage)
		{
			depth = 1;
			bytesPerRow = (width + 7) / 8;
		}
		else
		if (image instanceof Gray8Image ||
		    image instanceof Paletted8Image)
		{
			depth = 8;
			bytesPerRow = width;
		}
		else
		if (image instanceof RGB24Image)
		{
			bytesPerRow = width * 3;
			depth = 24;
		}
		else
		{
			throw new UnsupportedTypeException("Cannot store image types " +
				"other than bilevel, gray8, paletted8 and RGB24.");
		}
		out.writeInt(depth);
		paddingBytes = (bytesPerRow % 2);
		numColors = 1 << depth;
		length = bytesPerRow * getBoundsHeight();
		out.writeInt(length); // length
		out.writeInt(COMPRESSION_NONE); // type
		mapType = 1;
		mapLength = 0;
		if (image instanceof Paletted8Image)
		{
			mapLength = 768;
		}
		out.writeInt(mapType); 
		out.writeInt(mapLength);
		if (image instanceof Paletted8Image)
		{
			Paletted8Image pal = (Paletted8Image)image;
			savePalette(pal.getPalette());
		}
	}

	private void savePalette(Palette palette) throws java.io.IOException
	{
		int numEntries = palette.getNumEntries();
		for (int channel = 0; channel < 3; channel++)
		{
			int channelIndex = -1;
			switch(channel)
			{
				case(0):
				{
					channelIndex = Palette.INDEX_RED;
					break;
				}
				case(1):
				{
					channelIndex = Palette.INDEX_GREEN;
					break;
				}
				case(2):
				{
					channelIndex = Palette.INDEX_BLUE;
					break;
				}
			}
			for (int i = 0; i < 256; i++)
			{
				int value = 0;
				if (i < numEntries)
				{
					value = palette.getSample(channelIndex, i);
				}
				out.write(value);
			}
		}
	}

	public String suggestFileExtension(PixelImage image)
	{
		return ".ras";
	}
}
