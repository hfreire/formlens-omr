/*
 * TIFFDecoderModifiedHuffman
 *
 * Copyright (c) 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.io.DataInput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.tiff.TIFFDecoder;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.ops.MissingParameterException;

/**
 * A TIFF decoder for files compresseed with the <em>Modified Huffman</em> method
 * (also known as <em>CCITT 1D Modified Huffman Run Length Encoding</em>).
 * This compression algorithm has the value <code>2</code> 
 * in the compression tag of an image file directory.
 * Only bilevel images can be encoded with that method.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class TIFFDecoderModifiedHuffman extends TIFFDecoder
{
	private DataInput in;
	private int bitBuffer;
	private int numBufferedBits;

	public void decode() throws 
		InvalidFileStructureException,
		IOException
	{
		byte[] row = new byte[getBytesPerRow()];
		for (int y = getY1(); y <= getY2(); y++)
		{
			decodeRow(row);
			putBytes(row, 0, row.length);
		}
	}

	private int decodeBlackRun() throws
		InvalidFileStructureException,
		IOException
	{
		return decodeRun(TIFFFaxCodes.BLACK_CODES, TIFFFaxCodes.MIN_BLACK_CODE_SIZE);
	}

	private void decodeRow(byte[] row) throws 
		InvalidFileStructureException,
		IOException
	{
		reset();
		boolean black = false;
		int index = 0;
		do
		{
			// this will hold the accumulated run length for the current 
			// color at the end of this loop iteration
			int completeRunLength = 0;
			// get run lengths regarding current color until one is smaller than 64
			int runLength;
			do
			{
				if (black)
				{
					runLength = decodeBlackRun();
				}
				else
				{
					runLength = decodeWhiteRun();
				}
				completeRunLength += runLength;
			}
			while (runLength >= 64);
			// pick color value for output row
			byte value;
			if (black)
			{
				value = (byte)BilevelImage.BLACK;
			}
			else
			{
				value = (byte)BilevelImage.WHITE;
			}
			// fill row buffer with value
			while (completeRunLength-- > 0)
			{
				row[index++] = value;
			}
			// switch colors (black to white or vice versa)
			black = !black;
		}
		while (index < row.length);
	}

	private int decodeRun(int[][][] codes, int minCodeSize) throws 
		InvalidFileStructureException,
		IOException
	{
		int code = readBits(minCodeSize);
		//int currentCodeSize = minCodeSize;
		for (int i = 0; i < codes.length; i++)
		{
			int[][] data = codes[i];
			int j = 0;
			final int LENGTH = data.length;
			while (j < LENGTH)
			{
				int[] pair = data[j++];
				if (pair[TIFFFaxCodes.INDEX_CODE_WORD] == code)
				{
					return pair[TIFFFaxCodes.INDEX_CODE_VALUE];
				}
			}
			code = (code << 1) | readBit();
		}
		throw new InvalidFileStructureException("Could not identify Huffman code in TIFF file.");
	}

	private int decodeWhiteRun() throws
		InvalidFileStructureException,
		IOException
	{
		return decodeRun(TIFFFaxCodes.WHITE_CODES, TIFFFaxCodes.MIN_WHITE_CODE_SIZE);
	}

	public Integer[] getCompressionTypes()
	{
		return new Integer[] {new Integer(TIFFConstants.COMPRESSION_CCITT_GROUP3_1D_MODIFIED_HUFFMAN)};
	}

	public void initialize() throws 
		IOException, 
		MissingParameterException
	{
		super.initialize();
		in = getInput();
	}

	private int readBit() throws IOException
	{
		int result;
		if (numBufferedBits == 0)
		{
			bitBuffer = in.readUnsignedByte();
			if ((bitBuffer & 0x80) == 0)
			{
				result = 0;
			}
			else
			{
				result = 1;
			}
			bitBuffer &= 0x7f;
			numBufferedBits = 7;
		}
		else
		{
			numBufferedBits--;
			result = bitBuffer >> numBufferedBits;
			bitBuffer &= (1 << numBufferedBits) - 1;
		}
		return result;
	}

	private int readBits(int number) throws IOException
	{
		// make sure there are at least number bits
		while (numBufferedBits < number)
		{
			int b = in.readUnsignedByte();
			bitBuffer = (bitBuffer << 8) | b;
			numBufferedBits += 8;
		}
		numBufferedBits -= number;
		int result = bitBuffer >> numBufferedBits;
		bitBuffer &= (1 << numBufferedBits) - 1;
		return result;
	}

	private void reset()
	{
		bitBuffer = 0;
		numBufferedBits = 0;
	}
}
