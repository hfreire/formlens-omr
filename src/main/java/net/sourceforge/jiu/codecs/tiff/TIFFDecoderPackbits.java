/*
 * TIFFDecoderPackbits
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.io.DataInput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.tiff.TIFFDecoder;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;

/**
 * A TIFF decoder for files compressed with the <em>Packbits</em> method.
 * This compression algorithm has the value <code>32773</code> 
 * in the compression tag of an image file directory.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class TIFFDecoderPackbits extends TIFFDecoder
{
	public void decode() throws 
		InvalidFileStructureException,
		IOException
	{
		DataInput in = getInput();
		byte[] row = new byte[getBytesPerRow()];
		for (int y = getY1(); y <= getY2(); y++)
		{
			int index = 0;
			do
			{
				byte value = in.readByte();
				if (value >= 0)
				{
					int numSamples = value + 1;
					// copy bytes literally
					in.readFully(row, index, numSamples);
					index += numSamples;
				}
				else
				if (value != (byte)-128)
				{
					int numSamples = - value + 1;
					// write run
					byte sample = in.readByte();
					while (numSamples-- != 0)
					{
						row[index++] = sample;
					}
				}
			}
			while (index != row.length);
			putBytes(row, 0, row.length);
		}
	}


	public Integer[] getCompressionTypes()
	{
		return new Integer[] {new Integer(TIFFConstants.COMPRESSION_PACKBITS)};
	}
}
