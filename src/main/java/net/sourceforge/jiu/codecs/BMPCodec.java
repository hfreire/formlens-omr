/*
 * BMPCodec
 *
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
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
import net.sourceforge.jiu.data.ByteChannelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.util.ArrayConverter;

/**
 * A codec to read and write Windows BMP image files.
 * <p>
 * Typical file extensions are <code>.bmp</code> and <code>.rle</code>
 * (the latter is only used for compressed files).
 * <h3>Bounds</h3>
 * <p>
 * This codec supports the bounds concept for loading and saving.
 * </p>
 * <h3>Supported BMP types when loading</h3>
 * <ul>
 * <li>Bilevel, 1 bit per pixel, uncompressed.
 *  BMP supports palettes for bilevel images, but the content of that
 *  palette is ignored and 0 is considered black and 1 white.
 *  Any class implementing {@link net.sourceforge.jiu.data.BilevelImage}
 *  can be given to the codec and it will load the image to that object
 *  (if the image's resolution is sufficient).
 *  If no image object is given to the codec, a new
 *  {@link net.sourceforge.jiu.data.MemoryBilevelImage} will be created.</li>
 * <li>Paletted, 4 bits per pixel, uncompressed or RLE4 compression.
 *  Both types are loaded to a {@link net.sourceforge.jiu.data.Paletted8Image} object.
 *  This requires 50 % more space than is necessary, but there is
 *  no dedicated 4 bit image data class in JIU.</li>
 * <li>Paletted, 8 bits per pixel, uncompressed or RLE8 compression.
 *  Both types are loaded to a {@link net.sourceforge.jiu.data.Paletted8Image} object.</li>
 * <li>RGB truecolor, 24 bits per pixel, uncompressed.
 *  This is loaded to a {@link net.sourceforge.jiu.data.RGB24Image} object.</li>
 * </ul>
 * There is no support for 16 bpp images or BI_BITFIELDS compression (for lack of test files).
 * <p>
 * <h3>Supported JIU image data classes when saving to BMP</h3>
 * <ul>
 * <li>{@link net.sourceforge.jiu.data.BilevelImage} objects are stored as 1 bit per pixel BMP files.</li>
 * <li>{@link net.sourceforge.jiu.data.Gray8Image} and 
 *  {@link net.sourceforge.jiu.data.Paletted8Image} objects are stored as 
 *  paletted 8 bits per pixel files.
 *  It doesn't really matter how many entries the palette has, the BMP file's
 *  palette will always have 256 entries,  filled up with zero entries if necessary.</li>
 * <li>{@link net.sourceforge.jiu.data.RGB24Image} objects are stored as 24 bpp BMP files.</li>
 * </ul>
 * There is no support for compressed BMP files when saving.
 * <p>
 * <h3>I/O classes</h3>
 * BMPCodec works with all input and output classes supported by ImageCodec
 * ({@link java.io.InputStream}, {@link java.io.OutputStream}, 
 *  {@link java.io.DataInput}, {@link java.io.DataOutput},
 *  {@link java.io.RandomAccessFile}).
 * <h3>Problems</h3>
 * <p>The RLE-compressed BMP files that I could test this codec on seem to
 *  have an end-of-line code at the end of every line instead of relying 
 *  on the decoder to know when it has unpacked enough bytes for a line.
 *  Whenever this codec encounters an EOL symbol and has a current column
 *  value of <code>0</code>, the EOL is ignored.
 * <h3>Usage examples</h3>
 * Write an image to a BMP file.
 * <pre>
 * BMPCodec codec = new BMPCodec();
 * codec.setImage(image);
 * codec.setFile("out.bmp", CodecMode.SAVE);
 * codec.process();
 * codec.close();
 * </pre>
 * Read an image from a BMP file.
 * <pre>
 * BMPCodec codec = new BMPCodec();
 * codec.setFile("image.bmp", CodecMode.LOAD);
 * codec.process();
 * codec.close();
 * PixelImage image = codec.getImage();
 * </pre>
 * @author Marco Schmidt
 * @since 0.7.0
 */
public class BMPCodec extends ImageCodec
{
	private int colorDepth;
	private int compression;
	private int dataOffset;
	private int imageHeight;
	private int imageWidth;
	private DataInput in;
	private DataOutput out;
	private Palette palette;

	public String[] getFileExtensions()
	{
		return new String[] {".bmp", ".rle"};
	}

	public String getFormatName()
	{
		return "Windows BMP";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/bmp", "image/x-ms-bmp"};
	}

	public boolean isLoadingSupported()
	{
		return true;
	}

	public boolean isSavingSupported()
	{
		return true;
	}

	private void load() throws
		MissingParameterException, 
		OperationFailedException, 
		UnsupportedTypeException,
		WrongFileFormatException
	{
		in = getInputAsDataInput();
		if (in == null)
		{
			throw new MissingParameterException("Input stream / random access file parameter missing.");
		}
		// now write the output stream
		try
		{
			loadHeader();
			loadStream();
		}
		catch (IOException ioe)
		{
			// wrap any I/O failures in an OperationFailedException
			throw new OperationFailedException("I/O failure: " + ioe.toString());
		}
	}

	private void loadCompressedPaletted4Stream() throws IOException
	{
		Paletted8Image image = (Paletted8Image)getImage();
		int imageBytesPerRow = imageWidth;
		int bytesPerRow = imageBytesPerRow;
		int mod = bytesPerRow % 4;
		if (mod != 0)
		{
			bytesPerRow += 4 - mod;
		}
		final int COLUMNS = getBoundsWidth();
		final int ROWS = imageHeight - getBoundsY1();
		final int X1 = getBoundsX1();
		int processedRows = 0;
		byte[] row = new byte[bytesPerRow];
		int x = 0;
		int y = imageHeight - 1;
		boolean endOfBitmap = false;
		boolean delta = false;
		int newX = 0;
		int newY = 0;
		while (processedRows < ROWS)
		{
			int v1 = in.readUnsignedByte();
			int v2 = in.readUnsignedByte();
			if (v1 == 0)
			{
				switch(v2)
				{
					case(0):
					{
						// end of line
						if (x != 0)
						{
							x = bytesPerRow;
						}
						break;
					}
					case(1):
					{
						// end of bitmap
						x = bytesPerRow;
						endOfBitmap = true;
						break;
					}
					case(2):
					{
						// delta
						delta = true;
						newX = x + in.readUnsignedByte();
						newY = y - in.readUnsignedByte();
						x = bytesPerRow;
						break;
					}
					default:
					{
						// copy the next v2 (3..255) samples from file to output
						// two samples are packed into one byte
						// if the number of bytes used to pack is not a multiple of 2,
						// an additional padding byte is in the stream and must be skipped
						boolean paddingByte = (((v2 + 1) / 2) % 2) != 0;
						while (v2 > 1)
						{
							int packed = in.readUnsignedByte();
							int sample1 = (packed >> 4) & 0x0f;
							int sample2 = packed & 0x0f;
							row[x++] = (byte)sample1;
							row[x++] = (byte)sample2;
							v2 -= 2;
						}
						if (v2 == 1)
						{
							int packed = in.readUnsignedByte();
							int sample = (packed >> 4) & 0x0f;
							row[x++] = (byte)sample;
						}
						if (paddingByte)
						{
							v2 = in.readUnsignedByte();
						}
						break;
					}
				}
			}
			else
			{
				// rle: replicate the two samples in v2 as many times as v1 says
				byte sample1 = (byte)((v2 >> 4) & 0x0f);
				byte sample2 = (byte)(v2 & 0x0f);
				while (v1 > 1)
				{
					row[x++] = sample1;
					row[x++] = sample2;
					v1 -= 2;
				}
				if (v1 == 1)
				{
					row[x++] = sample1;
				}
			}
			// end of line?
			if (x == bytesPerRow)
			{
				if (y <= getBoundsY2())
				{
					image.putByteSamples(0, 0, y - getBoundsY1(), COLUMNS, 1, row, X1);
				}
				if (delta)
				{
					x = newX;
					y = newY;
				}
				else
				{
					x = 0;
					y--;
				}
				if (endOfBitmap)
				{
					processedRows = ROWS - 1;
				}
				setProgress(processedRows, ROWS);
				processedRows++;
				delta = false;
			}
		}
	}

	private void loadCompressedPaletted8Stream() throws IOException
	{
		Paletted8Image image = (Paletted8Image)getImage();
		int imageBytesPerRow = imageWidth;
		int bytesPerRow = imageBytesPerRow;
		int mod = bytesPerRow % 4;
		if (mod != 0)
		{
			bytesPerRow += 4 - mod;
		}
		final int COLUMNS = getBoundsWidth();
		final int ROWS = imageHeight - getBoundsY1();
		final int X1 = getBoundsX1();
		int processedRows = 0;
		byte[] row = new byte[bytesPerRow];
		int x = 0;
		int y = imageHeight - 1;
		boolean endOfBitmap = false;
		boolean delta = false;
		int newX = 0;
		int newY = 0;
		while (processedRows < ROWS)
		{
			int v1 = in.readUnsignedByte();
			int v2 = in.readUnsignedByte();
			if (v1 == 0)
			{
				switch(v2)
				{
					case(0):
					{
						// end of line
						if (x != 0)
						{
							x = bytesPerRow;
						}
						break;
					}
					case(1):
					{
						// end of bitmap
						x = bytesPerRow;
						endOfBitmap = true;
						break;
					}
					case(2):
					{
						// delta
						delta = true;
						newX = x + in.readUnsignedByte();
						newY = y - in.readUnsignedByte();
						x = bytesPerRow;
						break;
					}
					default:
					{
						// copy the next v2 (3..255) bytes from file to output
						boolean paddingByte = (v2 % 2) != 0;
						while (v2-- > 0)
						{
							row[x++] = (byte)in.readUnsignedByte();
						}
						if (paddingByte)
						{
							v2 = in.readUnsignedByte();
						}
						break;
					}
				}
			}
			else
			{
				// rle: replicate v2 as many times as v1 says
				byte value = (byte)v2;
				while (v1-- > 0)
				{
					row[x++] = value;
				}
			}
			// end of line?
			if (x == bytesPerRow)
			{
				if (y <= getBoundsY2())
				{
					image.putByteSamples(0, 0, y - getBoundsY1(), COLUMNS, 1, row, X1);
				}
				if (delta)
				{
					x = newX;
					y = newY;
				}
				else
				{
					x = 0;
					y--;
				}
				if (endOfBitmap)
				{
					processedRows = ROWS - 1;
				}
				setProgress(processedRows, ROWS);
				processedRows++;
				delta = false;
			}
		}
	}

	private void loadHeader() throws
		IOException,
		MissingParameterException, 
		OperationFailedException, 
		UnsupportedTypeException,
		WrongFileFormatException
	{
		byte[] header = new byte[54];
		in.readFully(header);
		if (header[0] != 'B' || header[1] != 'M')
		{
			throw new WrongFileFormatException("Not a BMP file (first two bytes are not 0x42 0x4d).");
		}
		dataOffset = ArrayConverter.getIntLE(header, 0x0a);
		if (dataOffset < 54)
		{
			throw new InvalidFileStructureException("BMP data expected to be 54dec or larger, got " + dataOffset);
		}
		imageWidth = ArrayConverter.getIntLE(header, 0x12);
		imageHeight = ArrayConverter.getIntLE(header, 0x16);
		if (imageWidth < 1 || imageHeight < 1)
		{
			throw new InvalidFileStructureException("BMP image width and height must be larger than 0, got " + imageWidth + " x " + imageHeight);
		}
		int planes = ArrayConverter.getShortLE(header, 0x1a);
		if (planes != 1)
		{
			throw new InvalidFileStructureException("Can only handle BMP number of planes = 1, got " + planes);
		}
		colorDepth = ArrayConverter.getShortLE(header, 0x1c);
		if (colorDepth != 1 && colorDepth != 4 && colorDepth != 8 && colorDepth != 24)
		{
			// TO DO: add support for 16 bpp BMP reading
			throw new InvalidFileStructureException("Unsupported BMP color depth: " + colorDepth);
		}
		compression = ArrayConverter.getIntLE(header, 0x1e);
		if (compression != 0 && !(compression == 1 && colorDepth == 8) && !(compression == 2 && colorDepth == 4))
		{
			throw new InvalidFileStructureException("Unsupported BMP compression type / color depth combination: " + 
				compression + " / " + colorDepth);
		}
		float dpiXValue = ArrayConverter.getIntLE(header, 0x26) / (100.0f / 2.54f);
		float dpiYValue = ArrayConverter.getIntLE(header, 0x2a) / (100.0f / 2.54f);
		setDpi((int)dpiXValue, (int)dpiYValue);
	}

	private void loadStream() throws
		IOException,
		MissingParameterException, 
		OperationFailedException, 
		UnsupportedTypeException
	{
		// 1. check bounds, initialize them if necessary
		setBoundsIfNecessary(imageWidth, imageHeight);
		checkBounds(imageWidth, imageHeight);
		// 2. read palette if the image isn't truecolor (even monochrome BMPs have a palette)
		int bytesToSkip;
		if (colorDepth <= 8)
		{
			int numPaletteEntries = 1 << colorDepth;
			int expectedPaletteSize = 4 * numPaletteEntries;
			int headerSpaceLeft = dataOffset - 54;
			bytesToSkip = headerSpaceLeft - expectedPaletteSize;
			if (bytesToSkip < 0)
			{
				throw new InvalidFileStructureException("Not enough space in header for palette with " + 
					numPaletteEntries + "entries.");
			}
			palette = new Palette(numPaletteEntries);
			for (int index = 0; index < numPaletteEntries; index++)
			{
				int blue = in.readUnsignedByte();
				int green = in.readUnsignedByte();
				int red = in.readUnsignedByte();
				in.readUnsignedByte();
				palette.put(index, red, green, blue);
			}
		}
		else
		{
			bytesToSkip = dataOffset - 54;
		}
		// 3. seek to beginning of image data
		while (bytesToSkip > 0)
		{
			int skipped = in.skipBytes(bytesToSkip);
			if (skipped > 0)
			{
				bytesToSkip -= skipped;
			}
		}
		// 4. check if we have an image object that we are supposed to reuse
		//    if there is one, check if it has the correct type
		//    if there is none, create a new one
		PixelImage image = getImage();
		if (image == null)
		{
			switch(colorDepth)
			{
				case(1):
				{
					setImage(new MemoryBilevelImage(getBoundsWidth(), getBoundsHeight()));
					break;
				}
				case(4):
				case(8):
				{
					setImage(new MemoryPaletted8Image(getBoundsWidth(), getBoundsHeight(), palette));
					break;
				}
				case(24):
				{
					setImage(new MemoryRGB24Image(getBoundsWidth(), getBoundsHeight()));
					break;
				}
				// loadHeader would have thrown an exception for any other color depths
			}
		}
		else
		{
			// TODO: check if image is of correct type
		}
		// now read actual image data
		if (compression == 0)
		{
			loadUncompressedStream();
		}
		else
		if (compression == 1)
		{
			loadCompressedPaletted8Stream();
		}
		else
		if (compression == 2)
		{
			loadCompressedPaletted4Stream();
		}
	}

	private void loadUncompressedBilevelStream() throws 
		IOException, 
		OperationFailedException
	{
		if ((getBoundsX1() % 8) != 0)
		{
			throw new OperationFailedException("When loading bilevel images, horizontal X1 bounds must be a multiple of 8; got " + getBoundsX1());
		}
		BilevelImage image = (BilevelImage)getImage();
		int imageBytesPerRow = (imageWidth + 7) / 8;
		int bytesPerRow = imageBytesPerRow;
		int mod = bytesPerRow % 4;
		if (mod != 0)
		{
			bytesPerRow += 4 - mod;
		}
		int bottomRowsToSkip = imageHeight - 1 - getBoundsY2();
		int bytesToSkip = bottomRowsToSkip * bytesPerRow;
		while (bytesToSkip > 0)
		{
			int skipped = in.skipBytes(bytesToSkip);
			if (skipped > 0)
			{
				bytesToSkip -= skipped;
			}
		}
		final int COLUMNS = getBoundsWidth();
		final int ROWS = getBoundsHeight();
		final int SRC_OFFSET = getBoundsX1() / 8;
		final int SRC_BIT_OFFSET = getBoundsX1() % 8;
		int y = image.getHeight() - 1;
		int processedRows = 0;
		byte[] row = new byte[bytesPerRow];
		while (processedRows < ROWS)
		{
			in.readFully(row);
			image.putPackedBytes(0, y, COLUMNS, row, SRC_OFFSET, SRC_BIT_OFFSET);
			y--;
			setProgress(processedRows, ROWS);
			processedRows++;
		}
	}

	private void loadUncompressedPaletted4Stream() throws
		IOException
	{
		Paletted8Image image = (Paletted8Image)getImage();
		int imageBytesPerRow = (imageWidth + 1) / 2;
		int bytesPerRow = imageBytesPerRow;
		int mod = bytesPerRow % 4;
		if (mod != 0)
		{
			bytesPerRow += 4 - mod;
		}
		int bottomRowsToSkip = imageHeight - 1 - getBoundsY2();
		int bytesToSkip = bottomRowsToSkip * bytesPerRow;
		while (bytesToSkip > 0)
		{
			int skipped = in.skipBytes(bytesToSkip);
			if (skipped > 0)
			{
				bytesToSkip -= skipped;
			}
		}
		final int COLUMNS = getBoundsWidth();
		final int ROWS = getBoundsHeight();
		final int X1 = getBoundsX1();
		int y = image.getHeight() - 1;
		int processedRows = 0;
		byte[] row = new byte[bytesPerRow];
		byte[] samples = new byte[bytesPerRow * 2];
		while (processedRows < ROWS)
		{
			in.readFully(row);
			ArrayConverter.decodePacked4Bit(row, 0, samples, 0, row.length);
			image.putByteSamples(0, 0, y, COLUMNS, 1, samples, X1);
			y--;
			setProgress(processedRows, ROWS);
			processedRows++;
		}
	}

	private void loadUncompressedPaletted8Stream() throws IOException
	{
		Paletted8Image image = (Paletted8Image)getImage();
		int imageBytesPerRow = imageWidth;
		int bytesPerRow = imageBytesPerRow;
		int mod = bytesPerRow % 4;
		if (mod != 0)
		{
			bytesPerRow += 4 - mod;
		}
		int bottomRowsToSkip = imageHeight - 1 - getBoundsY2();
		int bytesToSkip = bottomRowsToSkip * bytesPerRow;
		while (bytesToSkip > 0)
		{
			int skipped = in.skipBytes(bytesToSkip);
			if (skipped > 0)
			{
				bytesToSkip -= skipped;
			}
		}
		final int COLUMNS = getBoundsWidth();
		final int ROWS = getBoundsHeight();
		final int X1 = getBoundsX1();
		int y = image.getHeight() - 1;
		int processedRows = 0;
		byte[] row = new byte[bytesPerRow];
		while (processedRows < ROWS)
		{
			in.readFully(row);
			image.putByteSamples(0, 0, y, COLUMNS, 1, row, X1);
			y--;
			setProgress(processedRows, ROWS);
			processedRows++;
		}
	}

	private void loadUncompressedRgb24Stream() throws IOException
	{
		RGB24Image image = (RGB24Image)getImage();
		int imageBytesPerRow = imageWidth * 3;
		int bytesPerRow = imageBytesPerRow;
		int mod = bytesPerRow % 4;
		if (mod != 0)
		{
			bytesPerRow += 4 - mod;
		}
		int bottomRowsToSkip = imageHeight - 1 - getBoundsY2();
		int bytesToSkip = bottomRowsToSkip * bytesPerRow;
		while (bytesToSkip > 0)
		{
			int skipped = in.skipBytes(bytesToSkip);
			if (skipped > 0)
			{
				bytesToSkip -= skipped;
			}
		}
		final int COLUMNS = getBoundsWidth();
		final int ROWS = getBoundsHeight();
		final int X1 = getBoundsX1();
		int y = image.getHeight() - 1;
		int processedRows = 0;
		byte[] row = new byte[bytesPerRow];
		byte[] samples = new byte[COLUMNS];
		while (processedRows < ROWS)
		{
			in.readFully(row);
			// copy red samples to array samples and store those samples
			for (int x = X1 * 3 + 2, i = 0; i < COLUMNS; x += 3, i++)
			{
				samples[i] = row[x];
			}
			image.putByteSamples(RGBIndex.INDEX_RED, 0, y, COLUMNS, 1, samples, 0);
			// copy green samples to array samples and store those samples
			for (int x = X1 * 3 + 1, i = 0; i < COLUMNS; x += 3, i++)
			{
				samples[i] = row[x];
			}
			image.putByteSamples(RGBIndex.INDEX_GREEN, 0, y, COLUMNS, 1, samples, 0);
			// copy blue samples to array samples and store those samples
			for (int x = X1 * 3, i = 0; i < COLUMNS; x += 3, i++)
			{
				samples[i] = row[x];
			}
			image.putByteSamples(RGBIndex.INDEX_BLUE, 0, y, COLUMNS, 1, samples, 0);
			y--;
			setProgress(processedRows, ROWS);
			processedRows++;
		}
	}

	private void loadUncompressedStream() throws
		IOException,
		OperationFailedException
	{
		switch(colorDepth)
		{
			case(1):
			{
				loadUncompressedBilevelStream();
				break;
			}
			case(4):
			{
				loadUncompressedPaletted4Stream();
				break;
			}
			case(8):
			{
				loadUncompressedPaletted8Stream();
				break;
			}
			case(24):
			{
				loadUncompressedRgb24Stream();
				break;
			}
		}
	}

	public void process() throws 
		MissingParameterException, 
		OperationFailedException
	{
		initModeFromIOObjects();
		if (getMode() == CodecMode.LOAD)
		{
			load();
		}
		else
		{
			save();
		}
	}

	private void save() throws
		MissingParameterException, 
		OperationFailedException, 
		UnsupportedTypeException
	{
		// check parameters of this operation
		// 1 image to be saved
		// 1.1 is it available?
		PixelImage image = getImage();
		if (image == null)
		{
			throw new MissingParameterException("No image available.");
		}
		// 1.2 is it supported?
		if (!(image instanceof Paletted8Image ||
		      image instanceof Gray8Image ||
		      image instanceof BilevelImage ||
		      image instanceof RGB24Image))
		{
			throw new UnsupportedTypeException("Unsupported image type: " + image.getClass().getName());
		}
		// 2 is output stream available?
		out = getOutputAsDataOutput();
		if (out == null)
		{
			throw new MissingParameterException("Output stream / random access file parameter missing.");
		}
		// now write the output stream
		try
		{
			writeStream();
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("I/O failure: " + ioe.toString());
		}
	}

	public String suggestFileExtension(PixelImage image)
	{
		return ".bmp";
	}

	private void writeHeader(PixelImage image, int filesize, int offset, int numBits) throws IOException
	{
		out.write(0x42); // 'B'
		out.write(0x4d); // 'M'
		writeInt(filesize);
		writeShort(0);
		writeShort(0);
		writeInt(offset);

		writeInt(40); // BITMAP_INFO header length
		writeInt(getBoundsWidth());
		writeInt(getBoundsHeight());
		writeShort(1); // # of planes
		writeShort(numBits);
		writeInt(0); // compression (0 = none)
		writeInt(filesize - offset); // size of image data in bytes
		writeInt((int)(getDpiX() * (100f / 2.54f))); // horizontal resolution in dpi
		writeInt((int)(getDpiY() * (100f / 2.54f))); // vertical resolution in dpi
		writeInt(0); // # of used colors
		writeInt(0); // # of important colors
	}

	// we can't use out.writeInt because we need little endian byte order
	private void writeInt(int value) throws IOException
	{
		out.write(value & 0xff);
		out.write((value >> 8) & 0xff);
		out.write((value >> 16) & 0xff);
		out.write((value >> 24) & 0xff);
	}

	/**
	 * Write the palette associated with the image getImage().
	 * Required not only for image objects that implement PalettedImage
	 * but also for BilevelImage and Grayscale8Image.
	 * For the latter two the palette values must be explicitly written into the file.
	 */
	private void writePalette() throws IOException
	{
		PixelImage pi = getImage();
		if (pi == null)
		{
			return;
		}
		if (pi instanceof Paletted8Image)
		{
			// always write 256 entries; if there aren't enough
			// in the palette, fill it up to 256 with (0, 0, 0, 0)
			Palette palette = ((Paletted8Image)pi).getPalette();
			for (int i = 0; i < 256; i++)
			{
				if (i < palette.getNumEntries())
				{
					out.write(palette.getSample(RGBIndex.INDEX_BLUE, i));
					out.write(palette.getSample(RGBIndex.INDEX_GREEN, i));
					out.write(palette.getSample(RGBIndex.INDEX_RED, i));
					out.write(0);
				}
				else
				{
					out.writeInt(0); // writes four 0 bytes
				}
			}
		}
		if (pi instanceof Gray8Image)
		{
			for (int i = 0; i < 256; i++)
			{
				out.write(i);
				out.write(i);
				out.write(i);
				out.write(0);
			}
		}
		if (pi instanceof BilevelImage)
		{
			for (int i = 0; i < 2; i++)
			{
				out.write(i * 255);
				out.write(i * 255);
				out.write(i * 255);
				out.write(0);
			}
		}
	}

	// we can't use out.writeShort because we need little endian byte order
	private void writeShort(int value) throws IOException
	{
		out.write(value & 0xff);
		out.write((value >> 8) & 0xff);
	}

	private void writeStream() throws IOException
	{
		PixelImage image = getImage();
		setBoundsIfNecessary(image.getWidth(), image.getHeight());
		int width = getBoundsWidth();
		int height = getBoundsHeight();
		ByteChannelImage bcimg = null;
		BilevelImage bilevelImage = null;
		RGB24Image rgbimg = null;
		int bytesPerRow = 0;
		int offset = 54;
		int numBits = 0;
		int numPackedBytes = 0;
		if (image instanceof Paletted8Image ||
		    image instanceof Gray8Image)
		{
			bcimg = (ByteChannelImage)image;
			bytesPerRow = width;
			offset += 1024;
			numBits = 8;
		}
		else
		if (image instanceof BilevelImage)
		{
			bilevelImage = (BilevelImage)image;
			numPackedBytes = (width + 7) / 8;
			bytesPerRow = numPackedBytes;
			offset += 8;
			numBits = 1;
		}
		else
		if (image instanceof RGB24Image)
		{
			rgbimg = (RGB24Image)image;
			bytesPerRow = width * 3;
			numBits = 24;
		}
		if ((bytesPerRow % 4) != 0)
		{
			bytesPerRow = ((bytesPerRow + 3) / 4) * 4;
		}
		int filesize = offset + bytesPerRow * height;
		writeHeader(image, filesize, offset, numBits);
		writePalette();
		byte[] row = new byte[bytesPerRow];
		final int X1 = getBoundsX1();
		for (int y = getBoundsY2(), processed = 0; processed < height; y--, processed++)
		{
			if (bilevelImage != null)
			{
				bilevelImage.getPackedBytes(X1, y, width, row, 0, 0);
			}
			else
			if (bcimg != null)
			{
				bcimg.getByteSamples(0, 0, y, width, 1, row, 0);
			}
			else
			if (rgbimg != null)
			{
				int offs = 0;
				for (int x = X1; x < X1 + width; x++)
				{
					row[offs++] = rgbimg.getByteSample(RGBIndex.INDEX_BLUE, x, y);
					row[offs++] = rgbimg.getByteSample(RGBIndex.INDEX_GREEN, x, y);
					row[offs++] = rgbimg.getByteSample(RGBIndex.INDEX_RED, x, y);
				}
			}
			else
			{
				// error
			}
			out.write(row);
			setProgress(processed, height);
			if (getAbort())
			{
				break;
			}
		}
		close();
	}
}
