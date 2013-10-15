/*
 * TIFFDecoder
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.color.conversion.CMYKConversion;
import net.sourceforge.jiu.color.conversion.LogLuvConversion;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.ByteChannelImage;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.ShortChannelImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.util.ArrayConverter;

/**
 * The abstract base class for a TIFF decoder, a class that decompresses one tile or
 * strip of image data and understands one or more compression types.
 * Each child class implements the decoding of a particular TIFF compression type
 * in its {@link #decode} method.
 * <p>
 * This class does all the work of storing decompressed data (given as a byte array)
 * in the image object.
 * Given the many variants (sample order, color depth, color space etc.) this is 
 * a larger portion of code. 
 * @author Marco Schmidt
 * @since 0.7.0
 */
public abstract class TIFFDecoder
{
	private TIFFCodec codec;
	private TIFFImageFileDirectory ifd;
	private int currentRow;
	private int leftColumn;
	private int rightColumn;
	private int topRow;
	private int bottomRow;
	private byte[] rowBuffer;
	private int bufferIndex;
	private int tileIndex;
	private int processedTileRows;
	private int totalTileRows;

	public TIFFDecoder()
	{
		tileIndex = -1;
	}

	/**
	 * Decode data from input and write the decompressed pixel data to
	 * the image associated with this decoder.
	 * Child classes must override this method to implement the decoding
	 * for a particular compression type.
	 */
	public abstract void decode() throws 
		InvalidFileStructureException,
		IOException;

	/**
	 * Returns the number of bytes per row for the strip or tile
	 * that this decoder deals with.
	 * So with a tiled TIFF and an image width of 500 and a tile width of 100,
	 * for an eight bit grayscale image this would return 100 (not 500).
	 * @return number of bytes per row
	 */
	public int getBytesPerRow()
	{
		return ifd.getBytesPerRow();
	}

	/**
	 * Returns the codec from which this decoder is used.
	 * @return TIFFCodec object using this decoder
	 */
	public TIFFCodec getCodec()
	{
		return codec;
	}

	/**
	 * Returns an array with Integer values of all compression types supported by
	 * this decoder (see the COMPRESSION_xyz constants in {@link TIFFConstants}.
	 * Normally, this is only one value, but some compression types got assigned more than one constant
	 * (e.g. deflated).
	 * Also, a decoder could be capable of dealing with more than one type of compression
	 * if the compression types are similar enough to justify that.
	 * However, typically a decoder can only deal with one type of compression.
	 * @return array with Integer objects of all TIFF compression constants supported by this decoder
	 */
	public abstract Integer[] getCompressionTypes();

	/**
	 * Returns the IFD for the image this decoder is supposed to uncompress
	 * (partially).
	 * @return IFD object
	 */
	public TIFFImageFileDirectory getImageFileDirectory()
	{
		return ifd;
	}

	/**
	 * Returns the input stream from which this decoder is supposed 
	 * to read data.
	 */
	public DataInput getInput()
	{
		return codec.getRandomAccessFile();
	}

	/**
	 * Returns the zero-based index of the tile or strip this decoder
	 * is supposed to be decompressing.
	 * @return tile index
	 */
	public int getTileIndex()
	{
		return tileIndex;
	}

	/**
	 * Returns the leftmost column of the image strip / tile to be read 
	 * by this decoder.
	 */
	public int getX1()
	{
		return leftColumn;
	}

	/**
	 * Returns the rightmost column of the image strip / tile to be read 
	 * by this decoder.
	 */
	public int getX2()
	{
		return rightColumn;
	}

	/**
	 * Returns the top row of the image strip / tile to be read 
	 * by this decoder.
	 */
	public int getY1()
	{
		return topRow;
	}

	/**
	 * Returns the bottom row of the image strip / tile to be read 
	 * by this decoder.
	 */
	public int getY2()
	{
		return bottomRow;
	}

	/**
	 * Check if all necessary parameters have been given to this decoder
	 * and initialize several internal fields from them.
	 * Required parameters are a TIFFCodec object, a TIFFImageFileDirectory object and
	 * a tile index.
	 */
	public void initialize() throws 
		IOException, 
		MissingParameterException
	{
		if (tileIndex < 0)
		{
			throw new MissingParameterException("Tile index was not initialized.");
		}
		if (codec == null)
		{
			throw new MissingParameterException("No TIFFCodec object was given to this decoder.");
		}
		if (ifd == null)
		{
			throw new MissingParameterException("No TIFFImageFileDirectory object was given to this decoder.");
		}

		RandomAccessFile raf = codec.getRandomAccessFile();
		long offset = ifd.getTileOffset(tileIndex) & 0x00000000ffffffffL;
		raf.seek(offset);

		leftColumn = ifd.getTileX1(tileIndex);
		rightColumn = ifd.getTileX2(tileIndex);
		topRow = ifd.getTileY1(tileIndex);
		bottomRow = ifd.getTileY2(tileIndex);
		currentRow = topRow;
		processedTileRows = tileIndex * ifd.getTileHeight();
		totalTileRows = ifd.getTileHeight() * ifd.getNumTiles();
		rowBuffer = new byte[ifd.getBytesPerRow()];
	}

	/**
	 * Adds a number of bytes to the internal row buffer.
	 * If the row buffer gets full (a complete line is available)
	 * that data will be copied to the image.
	 * Note that more than one line, exactly one line or only part
	 * of a line can be stored in the <code>number</code> bytes
	 * in <code>data</code>.
	 * @param data byte array with image data that has been decoded
	 * @param offset int index into data where the first byte to be stored is situated
	 * @param number int number of bytes to be stored
	 */
	public void putBytes(byte[] data, int offset, int number)
	{
		// assert(bufferIndex < rowBuffer.length);
		while (number > 0)
		{
			int remaining = rowBuffer.length - bufferIndex;
			int numCopy;
			if (number > remaining)
			{
				numCopy = remaining;
			}
			else
			{
				numCopy = number;
			}
			System.arraycopy(data, offset, rowBuffer, bufferIndex, numCopy);
			number -= numCopy;
			offset += numCopy;
			bufferIndex += numCopy;
			if (bufferIndex == getBytesPerRow())
			{
				storeRow(rowBuffer, 0);
				bufferIndex = 0;
			}
		}
	}

	/**
	 * Specify the codec to be used with this decoder.
	 * This is a mandatory parameter - without it, {@link #initialize}
	 * will throw an exception.
	 * @param tiffCodec TIFFCodec object to be used by this decoder
	 * @see #getCodec
	 */
	public void setCodec(TIFFCodec tiffCodec)
	{
		codec = tiffCodec;
	}

	/**
	 * Specify the IFD to be used with this decoder.
	 * This is a mandatory parameter - without it, {@link #initialize}
	 * will throw an exception.
	 * @param tiffIfd object to be used by this decoder
	 * @see #getImageFileDirectory
	 */
	public void setImageFileDirectory(TIFFImageFileDirectory tiffIfd)
	{
		ifd = tiffIfd;
	}

	/**
	 * Specify the zero-based tile index for the tile or strip to be decompressed
	 * by this decoder.
	 * This is a mandatory parameter - without it, {@link #initialize}
	 * will throw an exception.
	 * @param index zero-based tile / strip index
	 * @see #getTileIndex
	 */
	public void setTileIndex(int index)
	{
		if (index < 0)
		{
			throw new IllegalArgumentException("Tile index must be 0 or larger.");
		}
		tileIndex = index;
	}

	private void storeRow(byte[] data, int offset)
	{
		codec.setProgress(processedTileRows++, totalTileRows);
		// get current row number and increase field currentRow by one
		int y = currentRow++;
		// buffer index field is reset to zero so that putBytes will start at the beginning of the buffer next time
		bufferIndex = 0;
		// leave if we don't need that row because of bounds
		if (!codec.isRowRequired(y))
		{
			return;
		}
		// adjust y so that it will be in bounds coordinate space
		y -= codec.getBoundsY1();
		// get leftmost and rightmost pixel index of the current tile
		int x1 = getX1();
		int x2 = getX2();
		// compute number of pixels, adjust for bounds
		int numPixels = x2 - x1 + 1;
		int leftPixels = 0;
		if (getX1() < codec.getBoundsX1())
		{
			leftPixels = codec.getBoundsX1() - getX1();
		}
		int rightPixels = 0;
		if (getX2() > codec.getBoundsX2())
		{
			rightPixels = getX2() - codec.getBoundsX2();
		}
		numPixels -= (rightPixels + leftPixels);
		switch(ifd.getImageType())
		{
			case(TIFFImageFileDirectory.TYPE_BILEVEL_BYTE):
			{
				BilevelImage image = (BilevelImage)codec.getImage();
				int index = offset + leftPixels;
				int x = getX1() - codec.getBoundsX1() + leftPixels;
				while (numPixels-- > 0)
				{
					if (data[index++] == (byte)BilevelImage.BLACK)
					{
						image.putBlack(x++, y);
					}
					else
					{
						image.putWhite(x++, y);
					}
				}
				break;
			}
			case(TIFFImageFileDirectory.TYPE_BILEVEL_PACKED):
			{
				BilevelImage image = (BilevelImage)codec.getImage();
				int x = getX1() - codec.getBoundsX1() + leftPixels;
				image.putPackedBytes(x, y, numPixels, data, offset + (leftPixels / 8), leftPixels % 8);
				break;
			}
			case(TIFFImageFileDirectory.TYPE_GRAY4):
			{
				ByteChannelImage image = (ByteChannelImage)codec.getImage();
				byte[] dest = new byte[data.length * 2];
				ArrayConverter.decodePacked4Bit(data, 0, dest, 0, data.length);
				for (int i = 0; i < dest.length; i++)
				{
					int value = dest[i] & 15;
					value = (value << 4) | value;
					dest[i] = (byte)value;
				}
				image.putByteSamples(0, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, offset + leftPixels);
				break;
			}
			case(TIFFImageFileDirectory.TYPE_PALETTED4):
			{
				ByteChannelImage image = (ByteChannelImage)codec.getImage();
				byte[] dest = new byte[data.length * 2];
				ArrayConverter.decodePacked4Bit(data, 0, dest, 0, data.length);
				image.putByteSamples(0, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, offset + leftPixels);
				break;
			}
			case(TIFFImageFileDirectory.TYPE_GRAY8):
			case(TIFFImageFileDirectory.TYPE_PALETTED8):
			{
				ByteChannelImage image = (ByteChannelImage)codec.getImage();
				image.putByteSamples(0, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, data, offset + leftPixels);
				break;
			}
			case(TIFFImageFileDirectory.TYPE_CMYK32_INTERLEAVED):
			{
				ByteChannelImage image = (ByteChannelImage)codec.getImage();
				byte[] dest = new byte[data.length];
				int numSamples = ifd.getTileWidth();
				CMYKConversion.convertCMYK32InterleavedToRGB24Planar(
					data, 0,
					dest, 0,
					dest, numSamples,
					dest, numSamples * 2,
					numSamples);
				image.putByteSamples(RGBIndex.INDEX_RED, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, leftPixels);
				image.putByteSamples(RGBIndex.INDEX_GREEN, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, numSamples + leftPixels);
				image.putByteSamples(RGBIndex.INDEX_BLUE, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, 2 * numSamples + leftPixels);
				break;
			}
/*			case(TIFFImageFileDirectory.TYPE_CMYK32_PLANAR):
			{
				ByteChannelImage image = (ByteChannelImage)codec.getImage();
				byte[] dest = new byte[data.length];
				int numSamples = ifd.getTileWidth();
				CMYKConversion.convertCMYK32PlanarToRGB24Planar(
					data, 0,
					data, numPixels,
					data, numPixels * 2,
					data, numPixels * 3,
					dest, 0,
					dest, numSamples,
					dest, numSamples * 2,
					numSamples);
				image.putByteSamples(RGBIndex.INDEX_RED, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, leftPixels);
				image.putByteSamples(RGBIndex.INDEX_GREEN, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, numSamples + leftPixels);
				image.putByteSamples(RGBIndex.INDEX_BLUE, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, dest, 2 * numSamples + leftPixels);
				break;
			}*/
			case(TIFFImageFileDirectory.TYPE_RGB24_INTERLEAVED):
			{
				ByteChannelImage image = (ByteChannelImage)codec.getImage();
				offset += leftPixels * 3;
				for (int i = 0, x = getX1() - codec.getBoundsX1() + leftPixels; i < numPixels; i++, x++)
				{
					image.putByteSample(RGBIndex.INDEX_RED, x, y, data[offset++]);
					image.putByteSample(RGBIndex.INDEX_GREEN, x, y, data[offset++]);
					image.putByteSample(RGBIndex.INDEX_BLUE, x, y, data[offset++]);
				}
				break;
			}
			case(TIFFImageFileDirectory.TYPE_RGB48_INTERLEAVED):
			{
				ShortChannelImage image = (ShortChannelImage)codec.getImage();
				offset += leftPixels * 3;
				short[] triplet = new short[3];
				boolean littleEndian = codec.getByteOrder() == TIFFCodec.BYTE_ORDER_INTEL;
				for (int i = 0, x = getX1() - codec.getBoundsX1() + leftPixels; i < numPixels; i++, x++)
				{
					for (int j = 0; j < 3; j++, offset += 2)
					{
						if (littleEndian)
						{
							triplet[j] = ArrayConverter.getShortLE(data, offset);
						}
						else
						{
							triplet[j] = ArrayConverter.getShortBE(data, offset);
						}
					}
					image.putShortSample(RGBIndex.INDEX_RED, x, y, triplet[0]);
					image.putShortSample(RGBIndex.INDEX_GREEN, x, y, triplet[1]);
					image.putShortSample(RGBIndex.INDEX_BLUE, x, y, triplet[2]);
				}
				break;
			}
			case(TIFFImageFileDirectory.TYPE_LOGLUV32_INTERLEAVED):
			{
				if (getImageFileDirectory().getCompression() == TIFFConstants.COMPRESSION_SGI_LOG_RLE)
				{
					ByteChannelImage image = (ByteChannelImage)codec.getImage();
					int numSamples = ifd.getTileWidth();
					byte[] red = new byte[numSamples];
					byte[] green = new byte[numSamples];
					byte[] blue = new byte[numSamples];
					LogLuvConversion.convertLogLuv32InterleavedtoRGB24Planar(data, red, green, blue, numSamples);
					image.putByteSamples(RGBIndex.INDEX_RED, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, red, leftPixels);
					image.putByteSamples(RGBIndex.INDEX_GREEN, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, green, leftPixels);
					image.putByteSamples(RGBIndex.INDEX_BLUE, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, blue, leftPixels);
				}
				else
				if (getImageFileDirectory().getCompression() == TIFFConstants.COMPRESSION_SGI_LOG_24_PACKED)
				{
					ByteChannelImage image = (ByteChannelImage)codec.getImage();
					int numSamples = ifd.getTileWidth();
					byte[] red = new byte[numSamples];
					byte[] green = new byte[numSamples];
					byte[] blue = new byte[numSamples];
					LogLuvConversion.convertLogLuv24InterleavedtoRGB24Planar(data, red, green, blue, numSamples);
					image.putByteSamples(RGBIndex.INDEX_RED, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, red, leftPixels);
					image.putByteSamples(RGBIndex.INDEX_GREEN, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, green, leftPixels);
					image.putByteSamples(RGBIndex.INDEX_BLUE, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, blue, leftPixels);
				}
				break;
			}
			case(TIFFImageFileDirectory.TYPE_LOGL):
			{
				ByteChannelImage image = (ByteChannelImage)codec.getImage();
				int numSamples = ifd.getTileWidth();
				byte[] gray = new byte[numSamples];
				LogLuvConversion.convertLogL16toGray8(data, gray, numSamples);
				image.putByteSamples(0, getX1() - codec.getBoundsX1() + leftPixels, y, numPixels, 1, gray, leftPixels);
				break;
			}
		}
	}
}
