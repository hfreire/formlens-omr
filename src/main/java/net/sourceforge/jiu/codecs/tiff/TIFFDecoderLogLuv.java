/*
 * TIFFDecoderLogLuv
 *
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.io.DataInput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.tiff.TIFFConstants;
import net.sourceforge.jiu.codecs.tiff.TIFFDecoder;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.ops.MissingParameterException;

/**
 * A TIFF decoder for files compressed with the <em>LogLuv RLE</em> method.
 * This compression algorithm has the value <code>34676</code> 
 * ({@link TIFFConstants#COMPRESSION_SGI_LOG_RLE})
 * in the compression tag of an image file directory.
 * Only image data with a photometric interpretation value of 
 * {@link TIFFConstants#PHOTOMETRIC_TRUECOLOR_LOGLUV} can be compressed with this method.
 * <p>
 * This implementation is based on the file <code>tif_luv.c</code> which
 * is part of the TIFF library <a target="_top" href="http://www.libtiff.org">libtiff</a>.
 * The original implementation was written by Greg W. Larson.
 * <p>
 * Learn more about the color type and its encoding on Greg's page
 * <a target="_top" href="http://positron.cs.berkeley.edu/~gwlarson/pixformat/tiffluv.html">LogLuv 
 * Encoding for TIFF Images</a>.
 * You will also find numerous sample image files there.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class TIFFDecoderLogLuv extends TIFFDecoder
{
	private DataInput in;
	private int compressedSize;
	private int tileWidth;
	private boolean rle;

	public void decode() throws 
		InvalidFileStructureException,
		IOException
	{
		byte[] row = new byte[getBytesPerRow()];
		rle = getImageFileDirectory().getCompression() == TIFFConstants.COMPRESSION_SGI_LOG_RLE;
		for (int y = getY1(); y <= getY2(); y++)
		{
			decodeRow(row);
			putBytes(row, 0, row.length);
		}
	}

	private void decodeRow(byte[] row) throws
		InvalidFileStructureException,
		IOException
	{
		if (rle)
		{
			decodeRowRLE(row);
		}
		else
		{
			decodeRowPacked24(row);
		}
	}

	private void decodeRowPacked24(byte[] row) throws 
		InvalidFileStructureException,
		IOException
	{
		int num = getImageFileDirectory().getTileWidth() * 3;
		in.readFully(row, 0, num);
	}

	private void decodeRowRLE(byte[] row) throws 
		InvalidFileStructureException,
		IOException
	{
		final int BYTES_PER_PIXEL;
		if (getImageFileDirectory().getPhotometricInterpretation() == TIFFConstants.PHOTOMETRIC_LOGL)
		{
			BYTES_PER_PIXEL = 2; // LogL
		}
		else
		{
			BYTES_PER_PIXEL = 4; // LogLuv
		}
		for (int initialOffset = 0; initialOffset < BYTES_PER_PIXEL; initialOffset++)
		{
			int offset = initialOffset;
			int numPixels = tileWidth;
			do
			{
				int v1 = in.readUnsignedByte();
				if ((v1 & 128) != 0)
				{
					// run
					int runCount = v1 + (2 - 128);
					numPixels -= runCount;
					compressedSize -= 2;
					byte v2 = in.readByte();
					while (runCount-- != 0)
					{
						row[offset] = v2;
						offset += BYTES_PER_PIXEL;
					}
				}
				else
				{
					// non-run, copy data
					int runCount = v1;
					numPixels -= runCount;
					compressedSize = compressedSize - runCount - 1;
					while (runCount-- != 0)
					{
						row[offset] = in.readByte();
						offset += BYTES_PER_PIXEL;
					}
				}
				if (compressedSize < 0)
				{
					throw new InvalidFileStructureException("Ran out of compressed input bytes before completing the decoding process.");
				}
			}
			while (numPixels > 0);
		}
	}

	public Integer[] getCompressionTypes()
	{
		return new Integer[] {new Integer(TIFFConstants.COMPRESSION_SGI_LOG_RLE), new Integer(TIFFConstants.COMPRESSION_SGI_LOG_24_PACKED)};
	}

	public void initialize() throws
		IOException, 
		MissingParameterException
	{
		super.initialize();
		in = getInput();
		compressedSize = getImageFileDirectory().getByteCount(getTileIndex());
		tileWidth = getImageFileDirectory().getTileWidth();
	}	
}
