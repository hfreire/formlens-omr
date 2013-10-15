/*
 * IFFCodec
 *
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.DataInput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * A codec to read Amiga IFF image files.
 * IFF (Interchange File Format) is an Amiga wrapper file format for texts, images, animations, sound and other kinds of data.
 * This codec only deals with image IFF files.
 * Typical file extensions for IFF image files are <code>.lbm</code> and <code>.iff</code>.
 * <h3>Loading / saving</h3>
 * Only loading is supported by this codec.
 * <h3>Supported file types</h3>
 * Both uncompressed and run-length encoded files are read.
 * <ul>
 * <li>1 to 8 bit indexed (paletted) color</li>
 * <li>24 bit RGB truecolor</li>
 * <li>HAM6 and HAM8 images (which are a mixture of paletted and truecolor)</li>
 * </ul>
 * <h3>Usage example</h3>
 * <pre>
 * IFFCodec codec = new IFFCodec();
 * codec.setFile("image.iff", CodecMode.LOAD);
 * codec.process();
 * PixelImage image = codec.getImage();
 * </pre>
 * @author Marco Schmidt
 * @since 0.3.0
 */
public class IFFCodec extends ImageCodec
{
	private final static int MAGIC_BMHD = 0x424d4844;
	private final static int MAGIC_BODY = 0x424f4459;
	private final static int MAGIC_CMAP = 0x434d4150;
	private final static int MAGIC_CAMG = 0x43414d47;
	private final static int MAGIC_FORM = 0x464f524d;
	private final static int MAGIC_ILBM = 0x494c424d;
	private final static int MAGIC_PBM = 0x50424d20;
	private final static int SIZE_BMHD = 0x00000014;
	private final static byte COMPRESSION_NONE = 0x00;
	private final static byte COMPRESSION_RLE = 0x01;
	private int camg;
	private byte compression;
	private boolean ehb;
	private boolean ham;
	private boolean ham6;
	private boolean ham8;
	private int height;
	private int numPlanes;
	private Palette palette;
	private boolean rgb24;
	private int type;
	private int width;

	private void checkAndLoad() throws 
		InvalidFileStructureException, 
		IOException, 
		MissingParameterException, 
		UnsupportedTypeException, 
		WrongFileFormatException,
		WrongParameterException
	{
		DataInput in = getInputAsDataInput();
		if (in == null)
		{
			throw new MissingParameterException("InputStream / DataInput object is missing.");
		}
		int formMagic = in.readInt();
		if (formMagic != MAGIC_FORM)
		{
			throw new WrongFileFormatException("Cannot load image. The " +
				"input stream is not a valid IFF file (wrong magic byte " +
				"sequence).");
		}
		in.readInt(); // read and discard "file size" field
		type = in.readInt();
		if (type != MAGIC_ILBM && type != MAGIC_PBM)
		{
			throw new UnsupportedTypeException("Cannot load image. The " +
				"input stream is an IFF file, but not of type ILBM or PBM" + 
				" (" + getChunkName(type) + ")");
		}
		PixelImage result = null;
		boolean hasBMHD = false;
		boolean hasCAMG = false;
		do
		{
			int magic = in.readInt();
			//System.out.println(chunkNameToString(magic));
			int size = in.readInt();
			// chunks must always have an even number of bytes
			if ((size & 1) == 1)
			{
				size++;
			}
			//System.out.println("Chunk " + getChunkName(magic) + ", size=" + size);
			switch(magic)
			{
				case(MAGIC_BMHD): // main header with width, height, bit depth
				{
					if (hasBMHD)
					{
						throw new InvalidFileStructureException("Error in " +
							"IFF file: more than one BMHD chunk.");
					}
					if (size != SIZE_BMHD)
					{
						throw new InvalidFileStructureException("Cannot " +
							"load image. The bitmap header chunk does not " +
							"have the expected size.");
					}
					// image resolution in pixels
					width = in.readShort();
					height = in.readShort();
					if (width < 1 || height < 1)
					{
						throw new InvalidFileStructureException("Cannot " +
							"load image. The IFF file's bitmap header " +
							"contains invalid width and height values: " + 
							width + ", " + height);
					}
					// next four bytes don't matter
					in.skipBytes(4);
					// color depth, 1..8 or 24
					numPlanes = in.readByte();
					if ((numPlanes != 24) && (numPlanes < 1 || numPlanes > 8))
					{
						throw new UnsupportedTypeException("Cannot load " +
							"image, unsupported number of bits per pixel: " + 
							numPlanes);
					}
					//System.out.println("\nnum planes=" + numPlanes);
					in.readByte(); // discard "masking" value
					// compression type, must be 0 or 1
					compression = in.readByte();
					if (compression != COMPRESSION_NONE && 
					    compression != COMPRESSION_RLE)
					{
						throw new UnsupportedTypeException("Cannot load " +
							"image, unsupported compression type: " + 
							compression);
					}
					//System.out.println(getCompressionName(compression));
					in.skipBytes(9);
					hasBMHD = true;
					break;
				}
				case(MAGIC_BODY):
				{
					if (!hasBMHD)
					{
						// width still has its initialization value -1; no 
						// bitmap chunk was encountered
						throw new InvalidFileStructureException("Cannot load image. Error in " +
							"IFF input stream: No bitmap header chunk " +
							"encountered before image body chunk.");
					}
					if (palette == null && (!rgb24))
					{
						// a missing color map is allowed only for truecolor images
						throw new InvalidFileStructureException("Cannot load image. Error in " +
							"IFF input stream: No colormap chunk " +
							"encountered before image body chunk.");
					}
					result = loadImage(in);
					break;
				}
				case(MAGIC_CAMG):
				{
					if (hasCAMG)
					{
						throw new InvalidFileStructureException("Cannot load image. Error in " +
							"IFF input stream: More than one CAMG chunk.");
					}
					hasCAMG = true;
					if (size < 4)
					{
						throw new InvalidFileStructureException("Cannot load" +
							" image. CAMG must be at least four bytes large; " +
							"found: " + size);
					}
					camg = in.readInt();
					ham = (camg & 0x800) != 0;
					ehb = (camg & 0x80) != 0;
					//System.out.println("ham=" + ham);
					in.skipBytes(size - 4);
					break;
				}
				case(MAGIC_CMAP): // palette (color map)
				{
					if (palette != null)
					{
						throw new InvalidFileStructureException("Cannot " +
							"load image. Error in IFF " +
							"input stream: More than one palette.");
					}
					if (size < 3 || (size % 3) != 0)
					{
						throw new InvalidFileStructureException("Cannot " +
							"load image. The size of the colormap is " +
							"invalid: " + size);
					}
					int numColors = size / 3;
					palette = new Palette(numColors, 255);
					for (int i = 0; i < numColors; i++)
					{
						palette.putSample(Palette.INDEX_RED, i, in.readByte() & 0xff);
						palette.putSample(Palette.INDEX_GREEN, i, in.readByte() & 0xff);
						palette.putSample(Palette.INDEX_BLUE, i, in.readByte() & 0xff);
					}
					break;
				}
				default:
				{
					if (in.skipBytes(size) != size)
					{
						throw new IOException("Error skipping " + size +
							" bytes of input stream.");
					}
					break;
				}
			}
		}
		while(result == null);
		setImage(result);
	}

	/**
	 * Converts input planes to index or truecolor output values.
	 * Exact interpretation depends on the type of ILBM image storage:
	 * <ul>
	 * <li>normal mode; the 1 to 8 planes create index values which are used
	 *  with the colormap</li>
	 * <li>RGB24; each of the 24 planes adds one bit to the three intensity
	 *  values for red, green and blue; no color map is necessary</li>
	 * <li>HAM6; a six bit integer (0 to 63) is assembled from the planes
	 *  and the top two bits determine if the previous color is modified or
	 *  if the lower four bits are used as an index into the palette (which
	 *  has consequently 2<sup>4</sup> = 16 entries</li>
	 * </ul>
	 * @param sourcePlanes
	 * @param dest
	 */
	private void convertRow(byte[][] sourcePlaneData, byte[][] dest)
	{
		int sourceMask = 0x80;
		int sourceIndex = 0;
		int lastRed = 0;
		int lastGreen = 0;
		int lastBlue = 0;
		for (int x = 0; x < width; x++)
		{
			int destMask = 1;
			int index = 0;
			for (int p = 0; p < sourcePlaneData.length; p++)
			{
				if ((sourcePlaneData[p][sourceIndex] & sourceMask) != 0)
				{
					index |= destMask;
				}
				destMask <<= 1;
			}
			if ((x & 7) == 7)
			{
				sourceIndex++;
			}
			if (sourceMask == 0x01)
			{
				sourceMask = 0x80;
			}
			else
			{
				sourceMask >>= 1;
			}
			if (ham6)
			{
				//System.out.println("enter ham6");
				int paletteIndex = index & 0x0f;
				//System.out.println("palette index=" + paletteIndex);
				switch((index >> 4) & 0x03)
				{
					case(0): // HOLD
					{
						lastRed = palette.getSample(Palette.INDEX_RED, paletteIndex);
						lastGreen = palette.getSample(Palette.INDEX_GREEN, paletteIndex);
						lastBlue = palette.getSample(Palette.INDEX_BLUE, paletteIndex);
						break;
					}
					case(1): // MODIFY BLUE
					{
						lastBlue = (lastBlue & 0x0f) | (paletteIndex << 4);
						break;
					}
					case(2): // MODIFY RED
					{
						lastRed = (lastRed & 0x0f) | (paletteIndex << 4);
						break;
					}
					case(3): // MODIFY GREEN
					{
						lastGreen = (lastGreen & 0x0f) | (paletteIndex << 4);
						break;
					}
				}
				dest[0][x] = (byte)lastRed;
				dest[1][x] = (byte)lastGreen;
				dest[2][x] = (byte)lastBlue;
			}
			else
			if (ham8)
			{
				int paletteIndex = index & 0x3f;
				//System.out.println("palette index=" + paletteIndex);
				switch((index >> 6) & 0x03)
				{
					case(0): // HOLD
					{
						lastRed = palette.getSample(Palette.INDEX_RED, paletteIndex);
						lastGreen = palette.getSample(Palette.INDEX_GREEN, paletteIndex);
						lastBlue = palette.getSample(Palette.INDEX_BLUE, paletteIndex);
						break;
					}
					case(1): // MODIFY BLUE
					{
						lastBlue = (lastBlue & 0x03) | (paletteIndex << 2);
						break;
					}
					case(2): // MODIFY RED
					{
						lastRed = (lastRed & 0x03) | (paletteIndex << 2);
						break;
					}
					case(3): // MODIFY GREEN
					{
						lastGreen = (lastGreen & 0x03) | (paletteIndex << 2);
						break;
					}
				}
				dest[0][x] = (byte)lastRed;
				dest[1][x] = (byte)lastGreen;
				dest[2][x] = (byte)lastBlue;
			}
			else
			if (rgb24)
			{
				dest[2][x] = (byte)(index >> 16);
				dest[1][x] = (byte)(index >> 8);
				dest[0][x] = (byte)index;
			}
			else
			{
				/* the value is an index into the lookup table */
				//destRgbData[destOffset++] = rgbLookup[index];
				dest[0][x] = (byte)index;
			}
		}
	}

	private void createExtraHalfbritePalette()
	{
		if (palette == null)
		{
			return;
		}
		int numPaletteEntries = palette.getNumEntries();
		Palette tempPalette = new Palette(numPaletteEntries * 2, 255);
		for (int i = 0; i < numPaletteEntries; i++)
		{
			int red = palette.getSample(Palette.INDEX_RED, i);
			tempPalette.putSample(Palette.INDEX_RED, numPaletteEntries + i, red);
			tempPalette.putSample(Palette.INDEX_RED, i, (red / 2) & 0xf0);
			int green = palette.getSample(Palette.INDEX_GREEN, i);
			tempPalette.putSample(Palette.INDEX_GREEN, numPaletteEntries + i, red);
			tempPalette.putSample(Palette.INDEX_GREEN, i, (green / 2) & 0xf0);
			int blue = palette.getSample(Palette.INDEX_BLUE, i);
			tempPalette.putSample(Palette.INDEX_BLUE, numPaletteEntries + i, blue);
			tempPalette.putSample(Palette.INDEX_BLUE, i, (blue / 2) & 0xf0);
		}
		palette = tempPalette;
	}

	private static String getChunkName(int name)
	{
		StringBuffer sb = new StringBuffer(4);
		sb.setLength(4);
		sb.setCharAt(0, (char)((name >> 24) & 0xff));
		sb.setCharAt(1, (char)((name >> 16) & 0xff));
		sb.setCharAt(2, (char)((name >> 8) & 0xff));
		sb.setCharAt(3, (char)((name & 0xff)));
		return new String(sb);
	}

	/*private static String getCompressionName(byte method)
	{
		switch(method)
		{
			case(COMPRESSION_NONE): return "Uncompressed";
			case(COMPRESSION_RLE): return "RLE";
			default: return "Unknown method (" + (method & 0xff) + ")";
		}
	}*/

	public String[] getFileExtensions()
	{
		return new String[] {".lbm", ".iff"};
	}

	public String getFormatName()
	{
		return "Amiga Interchange File Format (IFF, LBM)";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/x-iff"};
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
	 * Loads data.length bytes from the input stream to the data array, 
	 * regarding the compression type.
	 * COMPRESSION_NONE will make this method load data.length bytes from
	 * the input stream.
	 * COMPRESSION_RLE will make this method decompress data.length bytes
	 * from input.
	 */
	private void loadBytes(DataInput in, byte[] data, int num, int y) throws
		InvalidFileStructureException, 
		IOException
	{
		switch(compression)
		{
			case(COMPRESSION_NONE):
			{
				in.readFully(data, 0, num);
				break;
			}
			case(COMPRESSION_RLE):
			{
				int x = 0;
				while (x < num)
				{
					int n = in.readByte() & 0xff;
					//System.out.println("value=" + n);
					boolean compressed = false;
					int count = -1;
					try
					{
						if (n < 128)
						{
							// copy next n + 1 bytes literally
							n++;
							in.readFully(data, x, n);
							x += n;
						}
						else
						{
							// if n == -128, nothing happens
							if (n > 128)
							{
								compressed = true;
								// otherwise, compute counter
								count = 257 - n;
								// read another byte
								byte value = in.readByte();
								// write this byte counter times to output
								while (count-- > 0)
								{
									data[x++] = value;
								}
							}
						}
					}
					catch (ArrayIndexOutOfBoundsException ioobe)
					{
						//System.out.println("Loading error");
						/* if the encoder did anything wrong, the above code
						   could potentially write beyond array boundaries
						   (e.g. if runs of data exceed line boundaries);
						   this would result in an ArrayIndexOutOfBoundsException
						   thrown by the virtual machine;
						   to give a more understandable error message to the 
						   user, this exception is caught here and a
						   explanatory InvalidFileStructureException is thrown */
						throw new InvalidFileStructureException("Error: " +
							"RLE-compressed image " +
							"file seems to be corrupt (compressed=" + compressed +
							", x=" + x + ", y=" + y +
							", count=" + (compressed ? (-((int)n) + 1) : n) + 
							", array length=" + data.length + ").");
					}
				}
				break;
			}
			default:
			{
				throw new InvalidFileStructureException("Error loading " +
					"image; unknown compression type (" + compression + ")");
			}
		}
	}

	/**
	 * Loads an image from given input stream in, regarding the compression
	 * type. The image will have 1 to 8 or 24 planes, a resolution given by
	 * the dimension width times height. The color map data will be used to
	 * convert index values to RGB pixels.
	 * Returns the resulting image.
	 * Will throw an IOException if either there were errors reading from the
	 * input stream or if the file does not exactly match the file format.
	 */
	private PixelImage loadImage(DataInput in) throws
		InvalidFileStructureException, 
		IOException, 
		UnsupportedTypeException,
		WrongParameterException
	{
		setBoundsIfNecessary(width, height);
		checkImageResolution();
		if (ham)
		{
			if (numPlanes == 6)
			{
				ham6 = true;
			}
			else
			if (numPlanes == 8)
			{
				ham8 = true;
			}
			else
			{
				throw new UnsupportedTypeException("Cannot handle " +
					"IFF ILBM HAM image file with number of planes " +
					"other than 6 or 8 (got " + numPlanes + ").");
			}
			if (palette == null)
			{
				throw new InvalidFileStructureException("Invalid IFF ILBM " +
					"file: HAM (Hold And Modify) image without a palette.");
			}
			int numPaletteEntries = palette.getNumEntries();
			if (ham6 && numPaletteEntries < 16)
			{
				throw new InvalidFileStructureException("Invalid IFF ILBM " +
					"file: HAM (Hold And Modify) 6 bit image with a " +
					"number of palette entries less than 16 (" +
					numPaletteEntries + ").");
			}
			if (ham8 && numPaletteEntries < 64)
			{
				throw new InvalidFileStructureException("Invalid IFF ILBM " +
					"file: HAM (Hold And Modify) 8 bit image with a " +
					"number of palette entries less than 64 (" +
					numPaletteEntries + ").");
			}
		}
		if (ehb)
		{
			createExtraHalfbritePalette();
		}
		int numBytesPerPlane = (width + 7) / 8;
		PixelImage image = null;
		Paletted8Image palettedImage = null;
		RGB24Image rgbImage = null;
		if (numPlanes == 24 || ham)
		{
			rgbImage = new MemoryRGB24Image(getBoundsWidth(), getBoundsHeight());
			image = rgbImage;
		}
		else
		{
			palettedImage = new MemoryPaletted8Image(getBoundsWidth(), getBoundsHeight(), palette);
			image = palettedImage;
		}
		/* only matters for uncompressed files;
		   will be true if the number of bytes is odd;
		   is computed differently for PBM and ILBM types
		*/
		boolean oddBytesPerRow = (((numBytesPerPlane * numPlanes) % 2) != 0);
		if (type == MAGIC_PBM)
		{
			oddBytesPerRow = ((width % 2) == 1);
		}
		// plane data will have numPlanes planes for ILBM and 1 plane for PBM
		byte[][] planes = null;
		int numChannels = 1;
		
		if (type == MAGIC_ILBM)
		{
			int allocBytes = numBytesPerPlane;
			if ((numBytesPerPlane % 2) == 1)
			{
				allocBytes++;
			}
			// allocate numPlanes byte arrays
			planes = new byte[numPlanes][];
			if (rgb24 || ham)
			{
				numChannels = 3;
			}
			// for each of these byte arrays allocate numBytesPerPlane bytes
			for (int i = 0; i < numPlanes; i++)
			{
				planes[i] = new byte[allocBytes];
			}
		}
		else
		{
			// only one plane, but each plane has width bytes instead of 
			// numBytesPerPlane
			planes = new byte[1][];
			planes[0] = new byte[width];
		}
		byte[][] dest = new byte[numChannels][];
		for (int i = 0; i < numChannels; i++)
		{
			dest[i] = new byte[width];
		}
		for (int y = 0, destY = 0 - getBoundsY1(); y <= getBoundsY2(); y++, destY++)
		{
			// load one row, different approach for PBM and ILBM
			if (type == MAGIC_ILBM)
			{
				// decode all planes for a complete row
				for (int p = 0; p < numPlanes; p++)
				{
					loadBytes(in, planes[p], numBytesPerPlane, y);
				}
			}
			else
			if (type == MAGIC_PBM)
			{
				loadBytes(in, planes[0], numBytesPerPlane, y);
			}
			/* all uncompressed rows must have an even number of bytes
			   so in case the number of bytes per row is odd, one byte
			   is read and dropped */
			if (compression == COMPRESSION_NONE && oddBytesPerRow)
			{
				in.readByte();
			}
			setProgress(y, getBoundsY2() + 1);
			// if we do not need the row we just loaded we continue loading
			// the next row
			if (!isRowRequired(y))
			{
				continue;
			}
			//System.out.println("storing row " + y + " as " + destY + ", numPlanes="+ numPlanes + ",type=" + type);
			// compute offset into pixel data array
			if (type == MAGIC_ILBM)
			{
				convertRow(planes, dest);
				if (rgb24 || ham)
				{
					rgbImage.putByteSamples(RGB24Image.INDEX_RED, 0, destY, 
						getBoundsWidth(), 1, dest[0], getBoundsX1());
					rgbImage.putByteSamples(RGB24Image.INDEX_GREEN, 0, destY, 
						getBoundsWidth(), 1, dest[1], getBoundsX1());
					rgbImage.putByteSamples(RGB24Image.INDEX_BLUE, 0, destY, 
						getBoundsWidth(), 1, dest[2], getBoundsX1());
				}
				else
				{
					palettedImage.putByteSamples(0, 0, destY, 
						getBoundsWidth(), 1, dest[0], getBoundsX1());
				}
			}
			else
			if (type == MAGIC_PBM)
			{
				palettedImage.putByteSamples(0, 0, destY, getBoundsWidth(), 1,
					planes[0], getBoundsX1());
			}
		}
		return image;
	}

	public void process() throws 
		InvalidFileStructureException, 
		MissingParameterException, 
		OperationFailedException,
		UnsupportedTypeException, 
		WrongFileFormatException
	{
		initModeFromIOObjects();
		if (getMode() == CodecMode.LOAD)
		{
			try
			{
				checkAndLoad();
			}
			catch (IOException ioe)
			{
				throw new InvalidFileStructureException("I/O error while loading: " + ioe.toString());
			}
		}
		else
		{
			throw new OperationFailedException("Only loading from IFF is supported.");
		}
	}
}
