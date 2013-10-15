/*
 * JPEGCodec
 *
 * Copyright (c) 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.jpeg;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.util.ArrayConverter;

/**
 * A codec for the JPEG file format.
 * <h3>Supported JPEG types</h3>
 * The codec is still under development.
 * Nothing can be read with it right now.
 * Writing JPEG files is not even in development stage.
 * <h3>Credits</h3>
 * "<em>JPEG Still Image Data Compression Standard</em>" by William B. Pennebaker and
 * Joan L. Mitchell. Published 1993 by Van Nostrand Reinhold.
 * ISBN 0-442-01272-1.
 * This book is referenced as P&M throughout the source code.
 * It's an invaluable resource for anything related to JPEG.
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGCodec extends ImageCodec
{
	private DataInput in;

	private void decodeScan(JPEGData jpegData)
	{
		JPEGFrame frame = jpegData.getFrame();
		int width = frame.getWidth();
		int height = frame.getHeight();
		/*Gray8Image image = new MemoryGray8Image(width, height);
		int x = 0;
		int y = 0;*/
		int numMCUs = ((width + 7) / 8) * ((height + 7) / 8);
		while (numMCUs > 0)
		{
			
			numMCUs--;
		}
	}

	public String[] getFileExtensions()
	{
		return new String[] {".jpg", ".jpeg"};
	}

	public String getFormatName()
	{
		return "JPEG File Interchange Format";
	}

	public String[] getMimeTypes()
	{
		// image/pjpeg for progressive JPEGs is not included 
		// because it's not supported by this codec (yet)
		return new String[] {"image/jpeg"};
	}

	public boolean isLoadingSupported()
	{
		return true;
	}

	public boolean isSavingSupported()
	{
		return false;
	}

	private void load() throws 
		OperationFailedException, 
		WrongFileFormatException
	{
		in = getInputAsDataInput();
		if (in == null)
		{
			throw new MissingParameterException(
				"Input object missing (could not retrieve via getAsDataInput).");
		}
		try
		{
			// read and check the first two bytes
			byte[] data = new byte[4];
			in.readFully(data, 0, 2);
			int signature = ArrayConverter.getShortBEAsInt(data, 0);
			if (signature != JPEGConstants.JFIF_SIGNATURE)
			{
				throw new WrongFileFormatException(
					"Not a JFIF file (first two bytes are not 0xff 0xd8).");
			}
			// continuously read markers, updating a JPEGData object 
			JPEGData jpegData = new JPEGData();
			while (true)
			{
				// read and decode marker type and length
				in.readFully(data);
				int marker = ArrayConverter.getShortBEAsInt(data, 0);
				int length = ArrayConverter.getShortBEAsInt(data, 2);
				// read the actual marker information
				readMarker(jpegData, marker, length);
			}
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("Error reading from input.");
		}
		finally
		{
			close();
		}
	}

	public static void main(String[] args) throws Exception
	{
		if (args.length < 1)
		{
			System.err.println("Need JPEG file names as arguments.");
			System.exit(1);
		}
		for (int i = 0; i < args.length; i++)
		{
			String fileName = args[i];
			JPEGCodec codec = new JPEGCodec();
			codec.setFile(new File(fileName), CodecMode.LOAD);
			codec.process();
			//PixelImage image = codec.getImage(); 
		}
	}

	public void process() throws 
		MissingParameterException, 
		OperationFailedException,
		WrongFileFormatException
	{
		initModeFromIOObjects();
		if (getMode() == CodecMode.LOAD)
		{
			load();
		}
		else
		if (getMode() == CodecMode.SAVE)
		{
			throw new OperationFailedException("Saving is not supported.");
		}
		else
		{
			throw new OperationFailedException("Unsupported codec mode.");
		}
	}

	private void readMarker(JPEGData jpegData, int marker, int length) throws 
		InvalidFileStructureException,
		IOException,
		UnsupportedTypeException
	{
		// make sure marker is a valid marker
		if ((marker >> 8) != 0xff)
		{
			// TODO: instead of giving up, search for next occurrence of 0xff (error recovery)
			throw new InvalidFileStructureException("Marker " + 
				marker + " does not have 0xff in its top eight bits.");
		}
		// zero out everything but the least significant byte
		marker &= 0xff;
		// decrease two bytes for the marker length field
		length -= 2;
		// react on marker, possible reactions:
		// - call corresponding method if available
		// - throw exception if unsupported
		// - skip otherwise (= marker is unknown)
		switch(marker)
		{
			case(JPEGConstants.MARKER_DHT):
			{
				JPEGMarkerReader.readHuffmanTables(in, jpegData, length);
				break;
			}
			case(JPEGConstants.MARKER_DQT):
			{
				JPEGMarkerReader.readQuantizationTables(in, jpegData, length);
				break;
			}
			// Start of frame: Huffman Baseline DCT
			case(JPEGConstants.MARKER_SOF0):
			{
				JPEGMarkerReader.readStartOfFrame(in, jpegData, marker, length);
				break;
			}
			// unsupported frame types
			case(JPEGConstants.MARKER_SOF1):
			case(JPEGConstants.MARKER_SOF2):
			case(JPEGConstants.MARKER_SOF3):
			case(JPEGConstants.MARKER_SOF5):
			case(JPEGConstants.MARKER_SOF6):
			case(JPEGConstants.MARKER_SOF7):
			case(JPEGConstants.MARKER_SOF9):
			case(JPEGConstants.MARKER_SOFA):
			case(JPEGConstants.MARKER_SOFB):
			case(JPEGConstants.MARKER_SOFD):
			case(JPEGConstants.MARKER_SOFE):
			case(JPEGConstants.MARKER_SOFF):
			{
				throw new UnsupportedTypeException(
					"Unsupported JPEG SOF type: " + Integer.toHexString(marker));
			}
			case(JPEGConstants.MARKER_SOS):
			{
				JPEGMarkerReader.readStartOfScan(in, jpegData, length);
				decodeScan(jpegData);
				break;
			}
			default:
			{
				System.out.println("Unknown marker: " + Integer.toHexString(marker));
				// skip marker data
				while (length > 0)
				{
					int skipped = in.skipBytes(length);
					if (skipped > 0)
					{
						length -= skipped;
					}
				}
				break;
			}
		}
	}

	public String suggestFileExtension(PixelImage image)
	{
		return ".jpg";
	}
}
