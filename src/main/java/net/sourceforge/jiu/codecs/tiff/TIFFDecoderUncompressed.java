/*
 * TIFFDecoderUncompressed
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.io.DataInput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.tiff.TIFFDecoder;

/**
 * A TIFF decoder for uncompressed TIFF files.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class TIFFDecoderUncompressed extends TIFFDecoder
{
	public void decode() throws IOException
	{
		DataInput in = getInput();
		byte[] row = new byte[getBytesPerRow()];
		for (int y = getY1(); y <= getY2(); y++)
		{
			in.readFully(row);
			putBytes(row, 0, row.length);
		}
	}

	public Integer[] getCompressionTypes()
	{
		return new Integer[] {new Integer(TIFFConstants.COMPRESSION_NONE)};
	}
}
