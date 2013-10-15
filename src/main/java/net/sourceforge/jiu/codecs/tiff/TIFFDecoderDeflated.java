/*
 * TIFFDecoderDeflated
 *
 * Copyright (c) 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.io.DataInput;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import net.sourceforge.jiu.codecs.tiff.TIFFConstants;
import net.sourceforge.jiu.codecs.tiff.TIFFDecoder;
import net.sourceforge.jiu.codecs.tiff.TIFFImageFileDirectory;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.ops.MissingParameterException;

/**
 * A TIFF decoder for files compressed with the <em>Deflated</em> method.
 * This compression algorithm has the values <code>31946</code> 
 * ({@link TIFFConstants#COMPRESSION_DEFLATED_INOFFICIAL}) and <code>8</code>
 * ({@link TIFFConstants#COMPRESSION_DEFLATED_OFFICIAL}) 
 * in the compression tag of an image file directory.
 * All types of image data can be compressed with this method.
 * <p>
 * This decoder makes use of the package java.util.zip which comes with an Inflater
 * class that does most of the work.
 * All the decoder has to do is feed the Inflater object with compressed data from
 * the input file and give decompressed data received from the Inflater to the
 * putBytes method.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class TIFFDecoderDeflated extends TIFFDecoder
{
	private DataInput in;
	private int compressedSize;

	public void decode() throws 
		InvalidFileStructureException,
		IOException
	{
		Inflater inflater = new Inflater();
		byte[] ioBuffer = new byte[20000];
		byte[] data = new byte[getBytesPerRow()];
		// determine how many bytes have to be read from inflater
		int numRows = getY2();
		TIFFImageFileDirectory ifd = getImageFileDirectory();
		if (numRows > ifd.getHeight() - 1)
		{
			numRows = ifd.getHeight() - 1;
		}
		numRows -= getY1();
		int remainingBytes = numRows * data.length;
		// now read and decompress as long as there is data left to decompress
		while (compressedSize > 0 || remainingBytes > 0)
		{
			if (inflater.needsInput())
			{
				// read compressed data from input
				int numBytes;
				if (compressedSize > ioBuffer.length)
				{
					numBytes = ioBuffer.length;
				}
				else
				{
					numBytes = compressedSize;
				}
				in.readFully(ioBuffer, 0, numBytes);
				// give data to inflater
				inflater.setInput(ioBuffer, 0, numBytes);
				compressedSize -= numBytes;
			}
			else
			{
				// determine how many bytes to decompress in this loop iteration
				int numBytes;
				if (remainingBytes > data.length)
				{
					numBytes = data.length;
				}
				else
				{
					numBytes = remainingBytes;
				}
				int numInflated;
				// do the decompression
				try
				{
					numInflated = inflater.inflate(data, 0, numBytes);
				}
				catch (DataFormatException dfe)
				{
					throw new InvalidFileStructureException("Error in compressed input data: " + dfe.toString());
				}
				// store decompressed data and update number of bytes left to decompress
				if (numInflated > 0)
				{
					putBytes(data, 0, numInflated);
					remainingBytes -= numInflated;
				}
			}
		}
	}

	public Integer[] getCompressionTypes()
	{
		return new Integer[]
		{
			new Integer(TIFFConstants.COMPRESSION_DEFLATED_INOFFICIAL),
			new Integer(TIFFConstants.COMPRESSION_DEFLATED_OFFICIAL)
		};
	}

	public void initialize() throws
		IOException, 
		MissingParameterException
	{
		super.initialize();
		in = getInput();
		compressedSize = getImageFileDirectory().getByteCount(getTileIndex());
	}	
}
