/*
 * PCDCodec
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.IOException;
import java.io.RandomAccessFile;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.color.YCbCrIndex;
import net.sourceforge.jiu.color.conversion.PCDYCbCrConversion;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.util.ArrayRotation;
import net.sourceforge.jiu.util.ArrayScaling;

/**
 * A codec to read Kodak Photo-CD (image pac) image files. 
 * Typical file extension is <code>.pcd</code>.
 * PCD is designed to store the same image in several resolutions.
 * Not all resolutions are always present in a file.
 * Typically, the first five resolutions are available and the file size
 * is between four and six megabytes.
 * Lossless compression (Huffman encoding) is used to store the higher resolution images.
 * All images are in 24 bit YCbCr colorspace, with a component subsampling of 4:1:1 (Y:Cb:Cr) 
 * in both horizontal and vertical direction.
 * <h3>Limitations</h3>
 * Only the lowest three resolutions are supported by this codec.
 * <h3>Sample PCD files</h3>
 * You can download sample PCD image files from
 * <a href="http://www.kodak.com/digitalImaging/samples/imageIntro.shtml">Kodak's
 * website</a>.
 *
 * @author Marco Schmidt
 */
public class PCDCodec extends ImageCodec implements YCbCrIndex
{
	/**
	 * Base/16, the minimum pixel resolution, 192 x 128 pixels.
	 */
	public static final int PCD_RESOLUTION_1 = 0;

	/**
	 * Base/4, the second pixel resolution, 384 x 256 pixels.
	 */
	public static final int PCD_RESOLUTION_2 = 1;

	/**
	 * Base, the third pixel resolution, 768 x 512 pixels.
	 */
	public static final int PCD_RESOLUTION_3 = 2;

	/**
	 * Base*4, the fourth pixel resolution, 1536 x 1024 pixels. <em>Unsupported</em>
	 */
	public static final int PCD_RESOLUTION_4 = 3;

	/**
	 * Base*16, the fifth pixel resolution, 3072 x 2048 pixels. <em>Unsupported</em>
	 */
	public static final int PCD_RESOLUTION_5 = 4;

	/**
	 * Base*64, the sixth pixel resolution, 6144 x 4096 pixels. <em>Unsupported</em>
	 */
	public static final int PCD_RESOLUTION_6 = 5;

	/**
	 * Index for the default resolution , Base ({@link #PCD_RESOLUTION_3}).
	 */
	public static final int PCD_RESOLUTION_DEFAULT = PCD_RESOLUTION_3;

	/**
	 * This two-dimensional int array holds all possible pixel resolutions for
	 * a PCD file. Use one of the PCD resolution constants (e.g.
	 * {@link #PCD_RESOLUTION_3} as first index.
	 * The second index must be 0 or 1 and leads to either width or
	 * height.
	 * Example: <code>PCD_RESOLUTION[PCD_RESOLUTION_3][1]</code> will evalute
	 * as 512, which can be width or height, depending on the image being
	 * in landscape or portrait mode.
	 * You may want to use these resolution values in your program
	 * to prompt the user which resolution to load from the file.
	 */
	public static final int[][] PCD_RESOLUTIONS =
		{{192, 128}, {384, 256}, {768, 512},
		 {1536, 1024}, {3072, 2048}, {6144, 4096}};
	// offsets into the file for the three uncompressed resolutions
	private static final long[] PCD_FILE_OFFSETS =
		{0x2000, 0xb800, 0x30000};
	/*private static final long[] PCD_BASE_LENGTH =
		{0x2000, 0xb800, 0x30000};*/
	// some constants to understand the orientation of an image
	private static final int NO_ROTATION = 0;
	private static final int ROTATE_90_LEFT = 1;
	private static final int ROTATE_180 = 2;
	private static final int ROTATE_90_RIGHT = 3;
	// 2048 bytes
	private static final int SECTOR_SIZE = 0x800;
	// "PCD_IPI"
	private static final byte[] MAGIC =
		{0x50, 0x43, 0x44, 0x5f, 0x49, 0x50, 0x49};
	private boolean performColorConversion;
	private boolean monochrome;
	private int numChannels;
	private int resolutionIndex;
	private RandomAccessFile in;
	private byte[][] data;

	/**
	 * This constructor chooses the default settings for PCD image loading:
	 * <ul>
	 * <li>load color image (all channels, not only luminance)</li>
	 * <li>perform color conversion from PCD's native YCbCr color space to RGB</li>
	 * <li>load the image in the default resolution 
	 *  {@link #PCD_RESOLUTION_DEFAULT}, 768 x 512 pixels (or vice versa)</li>
	 * </ul>
	 */
	public PCDCodec()
	{
		super();
		setColorConversion(true);
		setMonochrome(false);
		setResolutionIndex(PCD_RESOLUTION_DEFAULT);
	}

	private byte[][] allocateMemory()
	{
		int numPixels = PCD_RESOLUTIONS[resolutionIndex][0] *
			PCD_RESOLUTIONS[resolutionIndex][1];
		byte[][] result = new byte[numChannels][];
		for (int i = 0; i < numChannels; i++)
		{
			result[i] = new byte[numPixels];
		}
		return result;
	}

	/*private void checkByteArray(
		byte[][] data, 
		int numPixels) throws IllegalArgumentException
	{
		// check if array is non-null
		if (data == null)
		{
			throw new IllegalArgumentException("Error: Image channel array is not initialized.");
		}
		// check if array has enough entries
		int channels;
		if (monochrome)
		{
			channels = 1;
			if (data.length < 1)
			{
				throw new IllegalArgumentException("Error: Image channel " +
					"array must have at least one channel for monochrome " +
					"images.");
			}
		}
		else
		{
			channels = 3;
			if (data.length < 3)
			{
				throw new IllegalArgumentException("Error: Image channel " +
					"array must have at least three channels for color images.");
			}
		}
		// check if each channel has enough entries for the samples
		for (int i = 0; i < channels; i++)
		{
			if (data[i].length < numPixels)
			{
				throw new IllegalArgumentException("Error: Image channel #" + i + 
					" is not large enough (" + numPixels + " entries required, " +
					data[i].length + " found).");
			}
		}
	}*/

	private void convertToRgb(int width, int height)
	{
		byte[] red = new byte[width];
		byte[] green = new byte[width];
		byte[] blue = new byte[width];
		int offset = 0;
		for (int y = 0; y < height; y++)
		{
			PCDYCbCrConversion.convertYccToRgb(
				data[INDEX_Y], 
				data[INDEX_CB], 
				data[INDEX_CR], 
				offset,
				red, 
				green, 
				blue, 
				0, 
				width);
			System.arraycopy(red, 0, data[0], offset, width);
			System.arraycopy(green, 0, data[1], offset, width);
			System.arraycopy(blue, 0, data[2], offset, width);
			offset += width;
		}
	}

	private IntegerImage createImage(int width, int height)
	{
		if (monochrome)
		{
			Gray8Image image = new MemoryGray8Image(width, height);
			int offset = 0;
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					image.putByteSample(0, x, y, data[0][offset++]);
				}
			}
			return image;
		}
		else
		if (performColorConversion)
		{
			RGB24Image image = new MemoryRGB24Image(width, height);
			int offset = 0;
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					image.putByteSample(RGB24Image.INDEX_RED, x, y, data[0][offset]);
					image.putByteSample(RGB24Image.INDEX_GREEN, x, y, data[1][offset]);
					image.putByteSample(RGB24Image.INDEX_BLUE, x, y, data[2][offset]);
					offset++;
				}
			}
			return image;
		}
		else
		{
			return null;
		}
	}

	public String[] getFileExtensions()
	{
		return new String[] {".pcd"};
	}

	public String getFormatName()
	{
		return "Kodak Photo-CD (PCD)";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/x-pcd"};
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
	 * Attempts to load an image.
	 * The codec must have been given an input stream, all other
	 * parameters (do not convert color to RGB, load monochrome channel 
	 * only, load other resolution than default) can optionally be
	 * chosen by calling the corresponding methods.
	 *
	 * @return loaded image
	 * @throws IOException if there were reading errors
	 * @throws OutOfMemoryException if there was not enough free memory 
	 *  available
	 * @throws InvalidFileStructureException if the file seems to be a PCD
	 *  stream but has logical errors in it
	 * @throws WrongFileFormatException if this is not a PCD file
	 */
	private void load() throws
		InvalidFileStructureException,
		IOException, 
		UnsupportedTypeException,
		WrongFileFormatException
	{
		if (resolutionIndex != PCD_RESOLUTION_1 &&
		    resolutionIndex != PCD_RESOLUTION_2 &&
		    resolutionIndex != PCD_RESOLUTION_3)
		{
			throw new UnsupportedTypeException("Error reading PCD input " +
				"stream. Only the three lowest resolutions are supported.");
		}
		if (in == null)
		{
			throw new IllegalArgumentException("Input file is missing " +
				"(use PCDCodec.setInput(RandomAccessFile).");
		}
		if (in.length() < 16 * 1024)
		{
			throw new WrongFileFormatException("Not a PCD file.");
		}
		byte[] sector = new byte[SECTOR_SIZE];
		// read first sector; first 7 bytes must be 0xff
		in.readFully(sector);
		for (int i = 0; i < 7; i++)
		{
			if (sector[i] != -1)
			{
				throw new WrongFileFormatException("Input is not a valid PCD " +
					"file (wrong magic byte sequence).");
			}
		}
		// read second sector and check more magic bytes
		in.readFully(sector);
		for (int i = 0; i < MAGIC.length; i++)
		{
			if (sector[i] != MAGIC[i])
			{
				throw new WrongFileFormatException("Input is not a valid PCD " +
					"file (wrong magic byte sequence).");
			}
		}
		// get image orientation and resolution
		int rotationAngle = sector[0x602] & 0x03;
		int width = PCD_RESOLUTIONS[resolutionIndex][0];
		int height = PCD_RESOLUTIONS[resolutionIndex][1];
		int realWidth = width;
		int realHeight = height;
		if (rotationAngle == ROTATE_90_LEFT || rotationAngle == ROTATE_90_RIGHT)
		{
			realWidth = height;
			realHeight = width;
		}
		if (!hasBounds())
		{
			setBounds(0, 0, realWidth - 1, realHeight - 1);
		}
		// determine which uncompressed image will be loaded
		int uncompressedResolution = resolutionIndex;
		if (resolutionIndex > PCD_RESOLUTION_3)
		{
			uncompressedResolution = PCD_RESOLUTION_3;
		}
		// load uncompressed image
		data = allocateMemory();
		loadUncompressedImage(uncompressedResolution);
		// reverse color subsampling if necessary
		if (!monochrome)
		{
			ArrayScaling.scaleUp200Percent(data[INDEX_CB],
				PCD_RESOLUTIONS[uncompressedResolution][0] / 2,
				PCD_RESOLUTIONS[uncompressedResolution][1] / 2);
			ArrayScaling.scaleUp200Percent(data[INDEX_CR],
				PCD_RESOLUTIONS[uncompressedResolution][0] / 2,
				PCD_RESOLUTIONS[uncompressedResolution][1] / 2);
		}
		// TODO load higher resolution by decoding differences to uncompressed image
		// ...
		// convert to RGB color space if possible and desired
		if ((!monochrome) && performColorConversion)
		{
			convertToRgb(width, height);
		}
		// rotate the image if necessary
		rotateArrays(rotationAngle, width, height);
		// adjust width and height
		if (rotationAngle == ROTATE_90_LEFT || rotationAngle == ROTATE_90_RIGHT)
		{
			int temp = width;
			width = height;
			height = temp;
		}
		setImage(createImage(width, height));
	}

	/**
	 * Loads one of the three lowest resolution images from the file.
	 * First skips as many bytes as there are between the current
	 * stream offset and the offset of the image in the PCD file
	 * (first three images are at fixed positions).
	 * Then reads the pixels from in to data.
     * <p>
	 * Note that there are <code>width</code> times <code>height</code>
	 * samples for Y, but only one fourth that many samples for each Cb and Cr
	 * (because of the 4:1:1 subsampling of the two chroma components).
	 * <p>
	 * @param resolution one of PCD_RESOLUTION_1, PCD_RESOLUTION_2 or PCD_RESOLUTION_3
	 * @throws an IOException if there were any reading errors
	 */
	private void loadUncompressedImage(int resolution)
		throws IllegalArgumentException, IOException
	{
		if (resolution != PCD_RESOLUTION_1 &&
		    resolution != PCD_RESOLUTION_2 &&
		    resolution != PCD_RESOLUTION_3)
		{
			throw new IllegalArgumentException("Error loading " +
				"PCD image, only the lowest three resolutions are " +
				"uncompressed.");
		}
		in.seek(PCD_FILE_OFFSETS[resolution]);
		int fullWidth = PCD_RESOLUTIONS[resolution][0];
		int fullHeight = PCD_RESOLUTIONS[resolution][1];
		int halfWidth = fullWidth / 2;
		int halfHeight = fullHeight / 2;
		int offset1 = 0;
		int offset2 = 0;
		for (int y = 0; y < halfHeight; y++)
		{
			// read two luminance rows
			in.readFully(data[INDEX_Y], offset1, fullWidth * 2);
			offset1 += (fullWidth * 2);
			if (monochrome)
			{
				if (in.skipBytes(fullWidth) != fullWidth)
				{
					throw new IOException("Could not skip " + fullWidth +
						" bytes.");
				}
			}
			else
			{
				// read one row for each cb and cr
				in.readFully(data[INDEX_CB], offset2, halfWidth);
				in.readFully(data[INDEX_CR], offset2, halfWidth);
				offset2 += halfWidth;
			}
		}
	}

	/**
	 * Checks the parameter and loads an image.
	 */
	public void process() throws
		InvalidFileStructureException,
		MissingParameterException,
		OperationFailedException,
		UnsupportedTypeException,
		WrongFileFormatException
	{
		in = getRandomAccessFile();
		if (in == null)
		{
			throw new MissingParameterException("RandomAccessFile object needed in PCDCodec.");
		}
		if (getMode() != CodecMode.LOAD)
		{
			throw new UnsupportedTypeException("PCDCodec can only load images.");
		}
		try
		{
			load();
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("I/O error: " + ioe.toString());
		}
	}

	private void rotateArrays(int rotationAngle, int width, int height)
	{
		if (rotationAngle == NO_ROTATION)
		{
			return;	
		}
		int numPixels = width * height;
		for (int i = 0; i < numChannels; i++)
		{
			byte[] dest = new byte[numPixels];
			switch(rotationAngle)
			{
				case(ROTATE_90_LEFT):
				{
					ArrayRotation.rotate90Left(width, height, data[i], 0, dest, 0);
					break;
				}
				case(ROTATE_90_RIGHT):
				{
					ArrayRotation.rotate90Right(width, height, data[i], 0, dest, 0);
					break;
				}
				case(ROTATE_180):
				{
					ArrayRotation.rotate180(width, height, data[i], 0, dest, 0);
					break;
				}
			}
			System.arraycopy(dest, 0, data[i], 0, numPixels);
		}
	}

	/*private void scaleUp(int currentResolution)
	{
		int width = PCD_RESOLUTIONS[currentResolution][0];
		int height = PCD_RESOLUTIONS[currentResolution][1];
		ArrayScaling.scaleUp200Percent(data[INDEX_Y], width, height);
		if (!monochrome)
		{
			ArrayScaling.scaleUp200Percent(data[INDEX_CB], width, height);
			ArrayScaling.scaleUp200Percent(data[INDEX_CR], width, height);
		}
	}*/

	/**
	 * Specify whether color is converted from PCD's YCbCr color space to
	 * RGB color space.
	 * The default is <code>true</code>, and you should only change this
	 * if you really know what you are doing.
	 * If you simply want the luminance (gray) channel, use 
	 * {@link #setMonochrome(boolean)} with <code>true</code> as parameter.
	 * @param performColorConversion boolean that determines whether color conversion is applied
	 */
	public void setColorConversion(boolean performColorConversion)
	{
		this.performColorConversion = performColorConversion;
	}

	public void setFile(String fileName, CodecMode codecMode) throws 
		IOException, 
		UnsupportedCodecModeException
	{
		if (codecMode == CodecMode.LOAD)
		{
			setRandomAccessFile(new RandomAccessFile(fileName, "r"), CodecMode.LOAD); 
		}
		else
		{
			throw new UnsupportedCodecModeException("This PCD codec can only load images.");
		}
	}

	/**
	 * Specifies whether the image is to be loaded as gray or color image.
	 * If argument is true, only the gray channel is loaded.
	 */
	public void setMonochrome(boolean monochrome)
	{
		this.monochrome = monochrome;
		if (monochrome)
		{
			numChannels = 1;
		}
		else
		{
			numChannels = 3;
		}
	}

	public void setResolutionIndex(int resolutionIndex)
	{
		this.resolutionIndex = resolutionIndex;
	}
}
