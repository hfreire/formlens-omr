/*
 * PNGCodec
 * 
 * Copyright (c) 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.zip.CheckedInputStream;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.CRC32;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray16Image;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.MemoryRGB48Image;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.util.ArrayConverter;

/**
 * An input stream that reads from an underlying stream of PNG
 * IDAT chunks and skips all header information.
 * PNG uses one or more IDAT chunks to store image data.
 * The resulting stream looks like that:
 * <code>IDAT [chunk size] [compressed data] [checksum] 
 * IDAT [chunk size] [compressed data] [checksum] ...</code>
 * This stream class expects an input stream where the first IDAT chunk name and chunk
 * size have been read already, the stream is thus pointing to the
 * first byte of the first [compressed data] section.
 * The size of that section is given to the constructor.
 * This class then returns calls to read(), counts the bytes it has given 
 * away and, whenever a compressed data section has been consumed, it reads
 * the IDAT chunk and stores its size, using it to determine when the
 * next compressed data section will end.
 * That way, for the caller the stream appears to be one large compressed
 * section.
 * <p>
 * According to the PNG specs the reason for multiple IDAT chunks is as
 * follows:
 * <blockquote>
 * (Multiple IDAT chunks are allowed so that encoders can work in a fixed 
 * amount of memory; typically the chunk size will correspond to the encoder's 
 * buffer size.)
 * </blockquote>
 * <a target="_top" href="http://www.w3.org/TR/PNG#C.IDAT">4.1.3. IDAT Image data</a>
 * <p>
 * If there is a more elegant approach to read multiple IDAT chunks, please
 * let me know.
 * However, reading everything into memory is not an option. 
 * @author Marco Schmidt
 * @since 0.12.0
 */
class PngIdatInputStream extends InputStream
{
	private static final int IDAT = 0x49444154;
	private DataInputStream in;
	private long bytesLeft;

	public PngIdatInputStream(DataInputStream input, long bytes)
	{
		in = input;
		bytesLeft = bytes;
	}

	public int read() throws IOException
	{
		if (bytesLeft == 0)
		{
			skipHeaders();
		}
		bytesLeft--;
		return in.read();
	}

	private void skipHeaders() throws IOException
	{
		do
		{
			//int crc = in.readInt();
			in.readInt(); // skip CRC
			bytesLeft = in.readInt() & 0xffffffffL;
			int type = in.readInt();
			if (IDAT != type)
			{
				throw new IOException("Expected IDAT chunk type, got " + 
					Integer.toHexString(type));
			}
		}
		while (bytesLeft == 0);
	}
}

/**
 * A codec for the Portable Network Graphics (PNG) format.
 * Supports both loading and saving of images.
 * <h3>Usage examples</h3> 
 * <h4>Load an image</h4>
 * The following example code loads an image from a PNG file.
 * Note that you could also use {@link ImageLoader} or {@link net.sourceforge.jiu.gui.awt.ToolkitLoader}
 * which require only a single line of code and can load all formats
 * supported by JIU, including PNG. 
 * <pre>  PNGCodec codec = new PNGCodec();
 *  codec.setFile("image.png", CodecMode.LOAD);
 *  codec.process();
 *  PixelImage image = codec.getImage();</pre>
 * <h4>Save an image</h4>
 * <pre>  PNGCodec codec = new PNGCodec();
 *  codec.setFile("out.png", CodecMode.SAVE);
 *  codec.setImage(image);
 *  codec.setCompressionLevel(Deflater.BEST_COMPRESSION);
 *  codec.appendComment("Copyright (c) 1992 John Doe");
 *  // sets last modification time to current time
 *  codec.setModification(new GregorianCalendar(
 *   new SimpleTimeZone(0, "UTC")));
 *  codec.process();</pre>
 * <h3>Supported storage order types</h3>
 * <h4>Loading</h4>
 * This codec reads both non-interlaced and Adam7 interlaced PNG files.
 * <h4>Saving</h4>
 * This codec only writes non-interlaced PNG files.
 * <h3>Supported color types</h3>
 * <h4>Loading</h4>
 * <ul>
 * <li>Grayscale 1 bit streams are loaded as {@link net.sourceforge.jiu.data.BilevelImage} objects,
 *  2, 4 and 8 bit streams as {@link net.sourceforge.jiu.data.Gray8Image} and 16 bit as
 *  {@link net.sourceforge.jiu.data.Gray16Image} objects.</li>
 * <li>Indexed 1, 2, 4 and 8 bit streams are all loaded as {@link net.sourceforge.jiu.data.Paletted8Image}.</li>
 * <li>RGB truecolor 24 bit streams are loaded as {@link net.sourceforge.jiu.data.RGB24Image},
 *  48 bit streams as {@link net.sourceforge.jiu.data.RGB48Image} objects.</li>
 * </ul> 
 * <h4>Saving</h4>
 * <ul>
 * <li>{@link net.sourceforge.jiu.data.BilevelImage} objects are stored as grayscale 1 bit PNG streams.</li>
 * <li>{@link net.sourceforge.jiu.data.Paletted8Image} objects are stored as indexed 8 bit PNG streams.
 *  Images will always be stored as 8 bit files, even if the palette has only 16, 4 or 2 entries.
 * </li>
 * <li>{@link net.sourceforge.jiu.data.Gray8Image} objects are stored as 8 bit grayscale PNG streams.</li>
 * <li>{@link net.sourceforge.jiu.data.Gray16Image} objects are stored as 16 bit grayscale PNG streams.</li>
 * <li>{@link net.sourceforge.jiu.data.RGB24Image} objects are stored as 24 bit RGB truecolor PNG streams.</li>
 * <li>{@link net.sourceforge.jiu.data.RGB48Image} objects are stored as 48 bit RGB truecolor PNG streams.</li>
 * </ul> 
 * <h3>Transparency information</h3>
 * PNG allows to store different types of transparency information.
 * Full alpha channels, transparent index values, and more.
 * Right now, this JIU codec does not make use of this information and simply
 * skips over it when encountered.
 * <h3>Bounds</h3>
 * This codec regards the bounds concept.
 * If bounds are specified with {@link #setBounds}, the codec will only load or save
 * part of an image.
 * <h3>Metadata</h3>
 * <h4>Loading</h4>
 * <ul>
 * <li>Physical resolution information is loaded from <code>pHYs</code> chunks.
 *  Use {@link #getDpiX} and {@link #getDpiY} to retrieve that information.
 *  after the call to {@link #process}.</li>
 * <li>Textual comments are read from <code>tEXt</code> chunks and can be retrieved
 *  with {@link #getComment} after the call to {@link #process}.</li>
 * </ul>
 * <h4>Saving</h4>
 * <ul>
 *  <li>Physical resolution information (specified with {@link #setDpi})
 *    is stored in a <code>pHYs</code> chunk.</li>
 *  <li>Textual comments (specified with {@link #appendComment}) are stored as <code>tEXt</code> chunks.
 *   The keyword used is <code>Comment</code>.
 *   Each of the {@link #getNumComments} is stored in a chunk of its own.</li>
 *  <li>Time of modification is stored in a <code>tIME</code> chunk.
 *   Use {@link #setModification(Calendar)} to give a point in time to this codec.</li>
 * </ul>
 * <h3>Implementation details</h3>
 * This class relies heavily on the Java runtime library for decompression and 
 * checksum creation.
 * <h3>Background</h3>
 * To learn more about the PNG file format, visit its 
 * <a target="_top" href="http://www.libpng.org/pub/png/">official homepage</a>.
 * There you can find a detailed specification, 
 * test images and existing PNG libraries and PNG-aware applications.
 * The book <em>PNG - The Definitive Guide</em> by Greg Roelofs, published by O'Reilly, 1999,
 * ISBN 1-56592-542-4 is a valuable source of information on PNG.
 * It is out of print, but it can be viewed online and downloaded for offline reading 
 * in its entirety from the site. 
 * @author Marco Schmidt
 * @since 0.12.0
 */
public class PNGCodec extends ImageCodec
{
	private final int CHUNK_CRC32_IEND = 0xae426082;
	private final int CHUNK_SIZE_IHDR = 0x0000000d;
	private final int CHUNK_TYPE_IDAT = 0x49444154;
	private final int CHUNK_TYPE_IEND = 0x49454e44;
	private final int CHUNK_TYPE_IHDR = 0x49484452;
	private final int CHUNK_TYPE_PHYS = 0x70485973;
	private final int CHUNK_TYPE_PLTE = 0x504c5445;
	private final int CHUNK_TYPE_TEXT = 0x74455874;
	private final int CHUNK_TYPE_TIME = 0x74494d45;
	private final int COLOR_TYPE_GRAY = 0;
	private final int COLOR_TYPE_GRAY_ALPHA = 4;
	private final int COLOR_TYPE_INDEXED = 3;
	private final int COLOR_TYPE_RGB = 2;
	private final int COLOR_TYPE_RGB_ALPHA = 6;
	private final int COLOR_TYPE_ALPHA = 4;
	private final int FILTER_TYPE_NONE = 0;
	private final int FILTER_TYPE_SUB = 1;
	private final int FILTER_TYPE_UP = 2;
	private final int FILTER_TYPE_AVERAGE = 3;
	private final int FILTER_TYPE_PAETH = 4;
	private final int COMPRESSION_DEFLATE = 0;
	private final int INTERLACING_NONE = 0;
	private final int INTERLACING_ADAM7 = 1;
	private final int FILTERING_ADAPTIVE = 0;
	private final int MAX_TEXT_SIZE = 512;
	private final int ADAM7_NUM_PASSES = 7;
	private final int DEFAULT_ENCODING_MIN_IDAT_SIZE = 32 * 1024;
	private final int[] ADAM7_COLUMN_INCREMENT = {8, 8, 4, 4, 2, 2, 1};
	private final int[] ADAM7_FIRST_COLUMN = {0, 4, 0, 2, 0, 1, 0};
	private final int[] ADAM7_FIRST_ROW = {0, 0, 4, 0, 2, 0, 1};
	private final int[] ADAM7_ROW_INCREMENT = {8, 8, 8, 4, 4, 2, 2};
	private final byte[] MAGIC_BYTES =
		{(byte)0x89, (byte)0x50, (byte)0x4e, (byte)0x47,
		 (byte)0x0d, (byte)0x0a, (byte)0x1a, (byte)0x0a};

	private boolean alpha;
	private byte[][] buffers;
	private int bpp;
	private CRC32 checksum;
	private CheckedInputStream checkedIn;
	private int chunkCounter;
	private int colorType;
	private int compressionType;
	private int currentBufferIndex;
	private int deflateLevel = Deflater.DEFAULT_COMPRESSION;
	private int deflateStrategy = Deflater.DEFAULT_STRATEGY;
	private int encodingMinIdatSize = DEFAULT_ENCODING_MIN_IDAT_SIZE;
	private int filterType;
	private boolean hasIhdr;
	private int height;
	private IntegerImage image;
	private DataInputStream in;
	private InflaterInputStream infl;
	private int interlaceType;
	private Calendar modification;
	private int numChannels;
	private DataOutput out;
	private Palette palette;
	private int precision;
	private int previousBufferIndex;
	private int width;

	/**
	 * Allocates the right image to private field <code>image</code>,
	 * taking into consideration the fields width, height, precision and colorType.
	 * Assumes that an IHDR chunk has been read and the above mentioned
	 * fields have been initialized and checked for their validity.
	 */ 
	private void allocateImage() throws InvalidFileStructureException, UnsupportedTypeException
	{
		setBoundsIfNecessary(width, height);
		int w = getBoundsWidth();
		int h = getBoundsHeight();
		if (colorType == COLOR_TYPE_GRAY || colorType == COLOR_TYPE_GRAY_ALPHA)
		{
			if (precision == 1)
			{
				image = new MemoryBilevelImage(w, h);
			}
			else
			if (precision <= 8)
			{
				image = new MemoryGray8Image(w, h);
			}
			else
			if (precision == 16)
			{
				image = new MemoryGray16Image(w, h);
			}
		}
		else
		if (colorType == COLOR_TYPE_INDEXED)
		{
			if (palette == null)
			{
				throw new InvalidFileStructureException("No palette found when trying to load indexed image.");
			}
			image = new MemoryPaletted8Image(w, h, palette);
		}
		else
		if (colorType == COLOR_TYPE_RGB || colorType == COLOR_TYPE_RGB_ALPHA)
		{
			if (precision == 8)
			{
				image = new MemoryRGB24Image(w, h);
			}
			else
			{
				image = new MemoryRGB48Image(w, h);
			}
		}
		else
		{
			throw new UnsupportedTypeException("Unsupported image type encountered");
		}
	}

	/**
	 * Checks values {@link #precision} and {@link #colorType}.
	 * A lot of combinations possibly found in an IHDR chunk
	 * are invalid. 
	 * Also initializes {@link #alpha} and {@link #numChannels}.
	 * @throws UnsupportedTypeException if an invalid combination 
	 *  of precision and colorType is found
	 */
	private void checkColorTypeAndPrecision() throws UnsupportedTypeException
	{
		if (colorType != COLOR_TYPE_GRAY &&
		    colorType != COLOR_TYPE_RGB &&
		    colorType != COLOR_TYPE_INDEXED && 
		    colorType != COLOR_TYPE_GRAY_ALPHA && 
		    colorType != COLOR_TYPE_RGB_ALPHA)
		{
			throw new UnsupportedTypeException("Not a valid color type: " + colorType);
		}
		if (precision != 1 && precision != 2 && precision != 4 && precision != 8 && precision != 16)
		{
			throw new UnsupportedTypeException("Invalid precision value: " + precision);
		}
		if (colorType == COLOR_TYPE_INDEXED && precision > 8)
		{
			throw new UnsupportedTypeException("More than eight bits of precision are not allowed for indexed images.");
		}
		if (colorType == COLOR_TYPE_RGB && precision < 8)
		{
			throw new UnsupportedTypeException("Less than eight bits of precision are not allowed for RGB images.");
		}
		alpha = (colorType & COLOR_TYPE_ALPHA) != 0;
		if (colorType == COLOR_TYPE_RGB ||
		    colorType == COLOR_TYPE_RGB_ALPHA)
		{
			numChannels = 3;
		}
		else
		{
			numChannels = 1;
		}
		bpp = computeBytesPerRow(1);
	}

	/**
	 * Computes a number of bytes for a given number of pixels,
	 * regarding precision and availability of an alpha channel.
	 * @param numPixels the number of pixels for which the number
	 *  of bytes necessary to store them is to be computed
	 * @return number of bytes
	 */
	private int computeBytesPerRow(int numPixels)
	{
		if (precision < 8)
		{
			return (numPixels + ((8 / precision) - 1)) / (8 / precision);
		}
		else
		{
			return (numChannels + (alpha ? 1 : 0)) * (precision / 8) * numPixels;
		}
	}

	private int computeColumnsAdam7(int pass)
	{
		switch(pass)
		{
			case(0): return (width + 7) / 8;
			case(1): return (width + 3) / 8;
			case(2): return (width + 3) / 4;
			case(3): return (width + 1) / 4;
			case(4): return (width + 1) / 2;
			case(5): return width / 2;
			case(6): return width;
			default: throw new IllegalArgumentException("Not a valid pass index: " + pass);
		}
	}

	private void fillRowBuffer(int y, byte[] row, int offs)
	{
		PixelImage image = getImage();
		int x1 = getBoundsX1();
		int w = getBoundsWidth();
		if (image instanceof BilevelImage)
		{
			BilevelImage bilevelImage = (BilevelImage)image;
			bilevelImage.getPackedBytes(x1, y, w, row, offs, 0);
		}
		else
		if (image instanceof Gray16Image)
		{
			Gray16Image grayImage = (Gray16Image)image;
			while (w-- > 0)
			{
				short sample = grayImage.getShortSample(x1++, y);
				ArrayConverter.setShortBE(row, offs, sample);
				offs += 2;
			}
		}
		else
		if (image instanceof Gray8Image)
		{
			Gray8Image grayImage = (Gray8Image)image;
			grayImage.getByteSamples(0, getBoundsX1(), y, getBoundsWidth(), 1, row, offs);
		}
		else
		if (image instanceof Paletted8Image)
		{
			Paletted8Image palImage = (Paletted8Image)image;
			palImage.getByteSamples(0, getBoundsX1(), y, getBoundsWidth(), 1, row, offs);
		}
		else
		if (image instanceof RGB24Image)
		{
			RGB24Image rgbImage = (RGB24Image)image;
			while (w-- > 0)
			{
				row[offs++] = rgbImage.getByteSample(RGBIndex.INDEX_RED, x1, y);
				row[offs++] = rgbImage.getByteSample(RGBIndex.INDEX_GREEN, x1, y);
				row[offs++] = rgbImage.getByteSample(RGBIndex.INDEX_BLUE, x1, y);
				x1++;
			}
		}
		else
		if (image instanceof RGB48Image)
		{
			RGB48Image rgbImage = (RGB48Image)image;
			while (w-- > 0)
			{
				short sample = rgbImage.getShortSample(RGBIndex.INDEX_RED, x1, y);
				ArrayConverter.setShortBE(row, offs, sample);
				offs += 2;

				sample = rgbImage.getShortSample(RGBIndex.INDEX_GREEN, x1, y);
				ArrayConverter.setShortBE(row, offs, sample);
				offs += 2;

				sample = rgbImage.getShortSample(RGBIndex.INDEX_BLUE, x1, y);
				ArrayConverter.setShortBE(row, offs, sample);
				offs += 2;

				x1++;
			}
		}
	}

	/**
	  * Creates a four-letter String from the parameter, an <code>int</code>
	  * value, supposed to be storing a chunk name.
	  * @return the chunk name
	  */
	private static String getChunkName(int chunk)
	{
		StringBuffer result = new StringBuffer(4);
		for (int i = 24; i >= 0; i -= 8)
		{
			result.append((char)((chunk >> i) & 0xff));
		}
		return result.toString();
	}

	public String getFormatName()
	{
		return "Portable Network Graphics (PNG)";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/png"};
	}

	private static int getPaeth(byte l, byte u, byte nw)
	{
		int a = l & 0xff;
		int b = u & 0xff;
		int c = nw & 0xff;
		int p = a + b - c;
		int pa = p - a;
		if (pa < 0)
		{
			pa = -pa;
		}
		int pb = p - b;
		if (pb < 0)
		{
			pb = -pb;
		} 
		int pc = p - c; 
		if (pc < 0)
		{
			pc = -pc;
		} 
		if (pa <= pb && pa <= pc)
		{
			return a;
		}
		if (pb <= pc)
		{
			return b;
		} 
		return c;
	}

	private void inflateBytes(byte[] buffer, int numBytes) throws InvalidFileStructureException, IOException
	{
		int offset = 0;
		do
		{
			try
			{
				int toRead = numBytes - offset;
				int numRead = infl.read(buffer, offset, toRead);
				if (numRead < 0)
				{
					throw new InvalidFileStructureException("Cannot fill buffer");
				}
				offset += numRead;
			}
			catch (IOException ioe)
			{
				throw new InvalidFileStructureException("Stopped decompressing " + ioe.toString());
			}
		}
		while (offset != numBytes);
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
		InvalidFileStructureException,
		IOException,
		UnsupportedTypeException,
		WrongFileFormatException
	{
		byte[] magic = new byte[MAGIC_BYTES.length];
		in.readFully(magic);
		for (int i = 0; i < MAGIC_BYTES.length; i++)
		{
			if (magic[i] != MAGIC_BYTES[i])
			{
				throw new WrongFileFormatException("Not a valid PNG input " +
					"stream, wrong magic byte sequence.");
			}
		}
		chunkCounter = 0;
		do
		{
			loadChunk();
			chunkCounter++;
		}
		while (image == null);
		close();
		setImage(image);
	}

	private void loadChunk() throws InvalidFileStructureException, IOException, UnsupportedTypeException
	{
		/*
		 * read chunk size; according to the PNG specs, the size value must not be larger
		 * than 2^31 - 1; to be safe, we treat the value as an unsigned
		 * 32 bit value anyway 
		 */
		long chunkSize = in.readInt() & 0xffffffffL;
		checksum.reset();
		int chunkName = in.readInt();
		// first chunk must be IHDR
		if (chunkCounter == 0 && chunkName != CHUNK_TYPE_IHDR)
		{
			throw new InvalidFileStructureException("First chunk was not IHDR but " + getChunkName(chunkName));
		}
		switch (chunkName)
		{
			// image data chunk
			case(CHUNK_TYPE_IDAT):
			{
				loadImage(chunkSize);
				break;
			}
			// end of image chunk
			case(CHUNK_TYPE_IEND):
			{
				throw new InvalidFileStructureException("Reached IEND chunk but could not load image.");
			}
			case(CHUNK_TYPE_IHDR):
			{
				if (hasIhdr)
				{
					throw new InvalidFileStructureException("More than one IHDR chunk found.");
				}
				if (chunkCounter != 0)
				{
					throw new InvalidFileStructureException("IHDR chunk must be first; found to be chunk #" + (chunkCounter + 1));
				}
				if (chunkSize != CHUNK_SIZE_IHDR)
				{
					throw new InvalidFileStructureException("Expected PNG " +
						"IHDR chunk length to be " + CHUNK_SIZE_IHDR + ", got " +
						chunkSize + ".");
				}
				hasIhdr = true;
				loadImageHeader();
				break;
			}
			case(CHUNK_TYPE_PHYS):
			{
				if (chunkSize == 9)
				{
					byte[] phys = new byte[9];
					in.readFully(phys);
					int x = ArrayConverter.getIntBE(phys, 0);
					int y = ArrayConverter.getIntBE(phys, 4);
					if (phys[8] == 1)
					{
						// unit is meters
						final double INCHES_PER_METER = 100 / 2.54;
						setDpi((int)(x / INCHES_PER_METER), (int)(y / INCHES_PER_METER));
					}
				}
				else
				{
					skip(chunkSize);
				}
				break;
			}
			case(CHUNK_TYPE_PLTE):
			{
				if ((chunkSize % 3) != 0)
				{
					throw new InvalidFileStructureException("Not a valid palette chunk size: " + chunkSize);
				}
				loadPalette(chunkSize / 3);
				break;
			}
			case(CHUNK_TYPE_TEXT):
			{
				if (chunkSize == 0)
				{
				}
				else
				if (chunkSize > MAX_TEXT_SIZE)
				{
					skip(chunkSize);
				}
				else
				{
					StringBuffer text = new StringBuffer((int)chunkSize);
					int i = 0;
					char c;
					do
					{
						c = (char)in.read();
						if (c == 0)
						{
							skip(chunkSize - i - 1);
							break;
						}
						text.append(c);
						i++;
					}
					while (i < chunkSize);
					//System.out.println("text=\"" + text.toString() + "\"");
				}
				break;
			}
			default:
			{
				skip(chunkSize);
			}
		}
		int createdChecksum = (int)checksum.getValue();
		if (image == null)
		{
			// this code doesn't work anymore if we have just read an image
			int chunkChecksum = in.readInt();
			if (createdChecksum != chunkChecksum)
			{
				throw new InvalidFileStructureException("Checksum created on chunk " +
					getChunkName(chunkName) + " " + Integer.toHexString(createdChecksum) +
					" is not equal to checksum read from stream " + 
					Integer.toHexString(chunkChecksum) + 
					"; file is corrupted.");
			}
		}
	}

	/**
	 * Load an image from the current position in the file.
	 * Assumes the last things read from input are an IDAT chunk type and
	 * its size, which is the sole argument of this method.
	 * @param chunkSize size of the IDAT chunk that was just read
	 * @throws InvalidFileStructureException if there are values in the PNG stream that make it invalid
	 * @throws IOException if there were I/O errors when reading
	 * @throws UnsupportedTypeException if something was encountered in the stream that is valid but not supported by this codec
	 */
	private void loadImage(long chunkSize) throws InvalidFileStructureException, IOException, UnsupportedTypeException
	{
		// allocate two byte buffers for current and previous row
		buffers = new byte[2][];
		int numBytes = computeBytesPerRow(width);
		currentBufferIndex = 0;
		previousBufferIndex = 1;
		buffers[currentBufferIndex] = new byte[numBytes];
		buffers[previousBufferIndex] = new byte[numBytes];
		for (int i = 0; i < buffers[previousBufferIndex].length; i++)
		{
			buffers[previousBufferIndex][i] = (byte)0;
		}
		// allocate the correct type of image object for the image type read in the IHDR chunk 
		allocateImage();
		// create a PngIdatInputStream which will skip header information when
		// multiple IDAT chunks are in the input stream
		infl = new InflaterInputStream(new PngIdatInputStream(in, chunkSize));
		switch(interlaceType)
		{
			case(INTERLACING_NONE):
			{
				loadImageNonInterlaced();
				break;
			}
			case(INTERLACING_ADAM7):
			{
				loadImageInterlacedAdam7();
				break;
			}
		}
	}

	/**
	 * Reads data from an IHDR chunk and initializes private fields with it.
	 * Does a lot of checking if read values are valid and supported by this class.
	 * @throws IOException
	 * @throws InvalidFileStructureException
	 * @throws UnsupportedTypeException
	 */
	private void loadImageHeader() throws IOException, InvalidFileStructureException, UnsupportedTypeException
	{
		// WIDTH -- horizontal resolution
		width = in.readInt();
		if (width < 1)
		{
			throw new InvalidFileStructureException("Width must be larger than 0; got " + width);
		}
		// HEIGHT -- vertical resolution
		height = in.readInt();
		if (height < 1)
		{
			throw new InvalidFileStructureException("Height must be larger than 0; got " + height);
		}
		// PRECISION -- bits per sample
		precision = in.read();
		// COLOR TYPE -- indexed, paletted, grayscale, optionally alpha
		colorType = in.read();
		// check for invalid combinations of color type and precision
		// and initialize alpha and numChannels
		checkColorTypeAndPrecision();
		// COMPRESSION TYPE -- only Deflate is defined
		compressionType = in.read();
		if (compressionType != COMPRESSION_DEFLATE)
		{
			throw new UnsupportedTypeException("Unsupported compression type: " +
				compressionType + ".");
		}
		// FILTER TYPE -- only Adaptive is defined
		filterType = in.read();
		if (filterType != FILTERING_ADAPTIVE)
		{
			throw new UnsupportedTypeException("Only 'adaptive filtering' is supported right now; got " + filterType);
		}
		// INTERLACE TYPE -- order of storage of image data
		interlaceType = in.read();
		if (interlaceType != INTERLACING_NONE &&
		    interlaceType != INTERLACING_ADAM7)
		{
			throw new UnsupportedTypeException("Only 'no interlacing' and 'Adam7 interlacing' are supported; got " + interlaceType);
		}
	}

	private void loadImageInterlacedAdam7() throws InvalidFileStructureException, IOException, UnsupportedTypeException
	{
		final int TOTAL_LINES = ADAM7_NUM_PASSES * height;
		for (int pass = 0; pass < ADAM7_NUM_PASSES; pass++)
		{
			currentBufferIndex = 0;
			previousBufferIndex = 1;
			byte[] previousBuffer = buffers[previousBufferIndex];
			for (int x = 0; x < previousBuffer.length; x++)
			{
				previousBuffer[x] = 0;
			}
			int y = ADAM7_FIRST_ROW[pass];
			int destY = y - getBoundsY1();
			int numColumns = computeColumnsAdam7(pass);
			if (numColumns == 0)
			{
				// this pass contains no data; skip to next pass
				setProgress((pass + 1) * height, TOTAL_LINES);
				continue;
			}
			int numBytes = computeBytesPerRow(numColumns);
			while (y < height)
			{
				previousBuffer = buffers[previousBufferIndex];
				byte[] currentBuffer = buffers[currentBufferIndex];
				int rowFilterType = readFilterType();
				inflateBytes(currentBuffer, numBytes);
				reverseFilter(rowFilterType, currentBuffer, previousBuffer, numBytes);
				if (isRowRequired(y))
				{
					storeInterlacedAdam7(pass, destY, currentBuffer);
				}
				int progressY = y;
				if (pass > 0)
				{
					progressY += pass * height;
				}
				setProgress(progressY, TOTAL_LINES);
				y += ADAM7_ROW_INCREMENT[pass];
				destY += ADAM7_ROW_INCREMENT[pass];
				currentBufferIndex = 1 - currentBufferIndex;
				previousBufferIndex = 1 - previousBufferIndex;
			}
		}
	}

	private void loadImageNonInterlaced() throws InvalidFileStructureException, IOException, UnsupportedTypeException
	{
		int linesToRead = getBoundsY2() + 1;
		int rowLength = computeBytesPerRow(width);
		for (int y = 0, destY = - getBoundsY1(); y <= getBoundsY2(); y++, destY++)
		{
			byte[] currentBuffer = buffers[currentBufferIndex];
			byte[] previousBuffer = buffers[previousBufferIndex];
			int rowFilterType = readFilterType();
			inflateBytes(currentBuffer, rowLength);
			reverseFilter(rowFilterType, currentBuffer, previousBuffer, rowLength);
			if (isRowRequired(y))
			{
				storeNonInterlaced(destY, currentBuffer);
			}
			setProgress(y, linesToRead);
			previousBufferIndex = 1 - previousBufferIndex;
			currentBufferIndex = 1 - currentBufferIndex;
		}
	}

	private void loadPalette(long numEntries) throws InvalidFileStructureException, IOException
	{
		if (palette != null)
		{
			throw new InvalidFileStructureException("More than one palette in input stream.");
		}
		if (numEntries < 1)
		{
			throw new InvalidFileStructureException("Number of palette entries must be at least 1.");
		}
		if (numEntries > 256)
		{
			throw new InvalidFileStructureException("Number of palette entries larger than 256: " + numEntries);
		}
		palette = new Palette((int)numEntries);
		int index = 0;
		do
		{
			palette.putSample(Palette.INDEX_RED, index, in.read() & 0xff);
			palette.putSample(Palette.INDEX_GREEN, index, in.read() & 0xff);
			palette.putSample(Palette.INDEX_BLUE, index, in.read() & 0xff);
			index++;
		}
		while (index != numEntries);
	}

	public static void main(String[] args) throws Exception
	{
		PNGCodec codec = new PNGCodec();
		codec.setFile(args[0], CodecMode.LOAD);
		codec.process();
		codec.close();
		PixelImage image = codec.getImage();
		codec = new PNGCodec();
		codec.setFile(args[1], CodecMode.SAVE);
		codec.setImage(image);
		codec.setDpi(300, 300);
		codec.appendComment("Test comment #1.");
		codec.appendComment("And test comment #2.");
		codec.setModification(new GregorianCalendar(new SimpleTimeZone(0, "UTC")));
		codec.process();
		codec.close();
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
				if (getImageIndex() != 0)
				{
					throw new InvalidImageIndexException("PNG streams can only store one image; " + 
						"index " + getImageIndex() + " is thus not valid.");
				}
				InputStream input = getInputStream();
				if (input == null)
				{
					throw new MissingParameterException("InputStream object missing.");
				}
				checksum = new CRC32();
				checkedIn = new CheckedInputStream(input, checksum);
				in = new DataInputStream(checkedIn);
				load();
			}
			catch (IOException ioe)
			{
				throw new OperationFailedException("I/O failure: " + ioe.toString());
			}
		}
		else
		if (getMode() == CodecMode.SAVE)
		{
			try
			{
				PixelImage image = getImage(); 
				if (image == null)
				{
					throw new MissingParameterException("Need image for saving.");
				}
				out = getOutputAsDataOutput();
				if (out == null)
				{
					throw new MissingParameterException("Could not retrieve non-null DataOutput object for saving.");
				}
				setBoundsIfNecessary(image.getWidth(), image.getHeight());
				save();
			}
			catch (IOException ioe)
			{
				throw new OperationFailedException("I/O failure: " + ioe.toString());
			}
		}
		else
		{
			throw new OperationFailedException("Unknown codec mode: " + getMode());
		}
	}

	private int readFilterType() throws InvalidFileStructureException, IOException
	{
		int filterType = infl.read();
		if (filterType >= 0 && filterType <= 4)
		{
			return filterType;
		}
		else
		{
			throw new InvalidFileStructureException("Valid filter types are from 0 to 4; got " + filterType);
		}
	}

	private void reverseFilter(int rowFilterType, byte[] buffer, byte[] prev, int numBytes) throws UnsupportedTypeException
	{
		switch(rowFilterType)
		{
			case(FILTER_TYPE_NONE):
			{
				break;
			}
			case(FILTER_TYPE_SUB):
			{
				for (int x = 0, px = -bpp; x < numBytes; x++, px++)
				{
					byte currXMinusBpp;
					if (px < 0)
					{
						currXMinusBpp = 0;
					}
					else
					{
						currXMinusBpp = buffer[px];
					}
					buffer[x] = (byte)(buffer[x] + currXMinusBpp);
				}
				break;
			}
			case(FILTER_TYPE_UP):
			{
				for (int x = 0; x < numBytes; x++)
				{
					buffer[x] = (byte)(buffer[x] + prev[x]);
				}
				break;
			}
			case(FILTER_TYPE_AVERAGE):
			{
				for (int x = 0, px = -bpp; x < numBytes; x++, px++)
				{
					int currX = buffer[x] & 0xff;
					int currXMinus1;
					if (px < 0)
					{
						currXMinus1 = 0;
					}
					else
					{
						currXMinus1 = buffer[px] & 0xff;
					}
					int prevX = prev[x] & 0xff;
					int result = currX + ((currXMinus1 + prevX) / 2);
					byte byteResult = (byte)result;
					buffer[x] = byteResult;
				}
				break;
			}
			case(FILTER_TYPE_PAETH):
			{
				for (int x = 0, px = -bpp; x < numBytes; x++, px++)
				{
					byte currXMinusBpp; 
					byte prevXMinusBpp;
					if (px < 0)
					{
						currXMinusBpp = 0;
						prevXMinusBpp = 0;
					}
					else
					{
						currXMinusBpp = buffer[px];
						prevXMinusBpp = prev[px];
					}
					buffer[x] = (byte)(buffer[x] + getPaeth(currXMinusBpp, prev[x], prevXMinusBpp));
				}
				break;
			}
			default:
			{
				throw new UnsupportedTypeException("Unknown filter type: " + rowFilterType);
			}
		}
	}

	private void save() throws IOException
	{
		// write 8 byte PNG signature
		out.write(MAGIC_BYTES);
		// write IHDR (image header) chunk
		saveIhdrChunk();
		// write pHYs chunk (physical resolution) if data is available
		savePhysChunk();
		// write tEXt chunks if comments are available
		saveTextChunks();
		// write tIME chunk if modification time was set
		saveTimeChunk();
		// write PLTE chunk if necessary
		savePlteChunk();
		// write IDAT chunk
		saveImage();
		// write IEND chunk
		saveIendChunk();
		close();		
	}

	private void saveChunk(int chunkType, int chunkSize, byte[] data) throws IOException
	{
		// set up array with chunk size and type
		byte[] intArray = new byte[8];
		ArrayConverter.setIntBE(intArray, 0, chunkSize);
		ArrayConverter.setIntBE(intArray, 4, chunkType);
		// write chunk size, type and data
		out.write(intArray, 0, 8);
		out.write(data, 0, chunkSize);
		// create checksum on type and data
		CRC32 checksum = new CRC32();
		checksum.reset();
		checksum.update(intArray, 4, 4);
		checksum.update(data, 0, chunkSize);
		// put checksum into byte array
		ArrayConverter.setIntBE(intArray, 0, (int)checksum.getValue());
		// and write it to output
		out.write(intArray, 0, 4);
	}

	private void saveIendChunk() throws IOException
	{
		out.writeInt(0);
		out.writeInt(CHUNK_TYPE_IEND);
		out.writeInt(CHUNK_CRC32_IEND);
	}

	private void saveIhdrChunk() throws IOException
	{
		byte[] buffer = new byte[CHUNK_SIZE_IHDR];
		width = getBoundsWidth();
		ArrayConverter.setIntBE(buffer, 0, width);
		height = getBoundsHeight();
		ArrayConverter.setIntBE(buffer, 4, height);
		PixelImage image = getImage();
		alpha = false;
		numChannels = 1;
		if (image instanceof BilevelImage)
		{
			precision = 1;
			colorType = COLOR_TYPE_GRAY;
		}
		else
		if (image instanceof Gray16Image)
		{
			precision = 16;
			colorType = COLOR_TYPE_GRAY;
		}
		else
		if (image instanceof Gray8Image)
		{
			precision = 8;
			colorType = COLOR_TYPE_GRAY;
		}
		else
		if (image instanceof Paletted8Image)
		{
			precision = 8;
			colorType = COLOR_TYPE_INDEXED;
		}
		else
		if (image instanceof RGB24Image)
		{
			numChannels = 3;
			precision = 8;
			colorType = COLOR_TYPE_RGB;
		}
		else
		if (image instanceof RGB48Image)
		{
			numChannels = 3;
			precision = 16;
			colorType = COLOR_TYPE_RGB;
		}
		buffer[8] = (byte)precision;
		buffer[9] = (byte)colorType;
		compressionType = COMPRESSION_DEFLATE;
		buffer[10] = (byte)compressionType;
		filterType = FILTERING_ADAPTIVE;
		buffer[11] = (byte)filterType;
		interlaceType = INTERLACING_NONE;
		buffer[12] = (byte)interlaceType;
		saveChunk(CHUNK_TYPE_IHDR, CHUNK_SIZE_IHDR, buffer);
	}

	private void saveImage() throws IOException
	{
		switch(interlaceType)
		{
			case(INTERLACING_NONE):
			{
				saveImageNonInterlaced();
				break;
			}
		}
	}

	private void saveImageNonInterlaced() throws IOException
	{
		int bytesPerRow = computeBytesPerRow(getBoundsWidth());
		byte[] rowBuffer = new byte[bytesPerRow + 1];
		byte[] outBuffer = new byte[Math.max(encodingMinIdatSize, bytesPerRow + 1)];
		int outOffset = 0;
		int numDeflated;
		Deflater defl = new Deflater(deflateLevel);
		defl.setStrategy(deflateStrategy);
		for (int y = getBoundsY1(); y <= getBoundsY2(); y++)
		{
			// fill row buffer
			rowBuffer[0] = (byte)FILTER_TYPE_NONE;
			fillRowBuffer(y, rowBuffer, 1);
			// give it to compressor 
			defl.setInput(rowBuffer);
			// store compressed data in outBuffer 
			do
			{
				numDeflated = defl.deflate(outBuffer, outOffset, outBuffer.length - outOffset);
				outOffset += numDeflated;
				if (outOffset == outBuffer.length)
				{
					saveChunk(CHUNK_TYPE_IDAT,  outOffset, outBuffer);
					outOffset = 0;
				}
			}
			while (numDeflated > 0);
			setProgress(y - getBoundsY1(), getBoundsHeight());
		}
		// tell Deflater that it got all the input
		defl.finish();
		// retrieve remaining compressed data from defl to outBuffer  
		do
		{
			numDeflated = defl.deflate(outBuffer, outOffset, outBuffer.length - outOffset);
			outOffset += numDeflated;
			if (outOffset == outBuffer.length)
			{
				saveChunk(CHUNK_TYPE_IDAT,  outOffset, outBuffer);
				outOffset = 0;
			}
		}
		while (numDeflated > 0);
		// write final IDAT chunk if necessary
		if (outOffset > 0)
		{
			saveChunk(CHUNK_TYPE_IDAT,  outOffset, outBuffer);
		}
	}

	private void savePhysChunk() throws IOException
	{
		int dpiX = getDpiX();
		int dpiY = getDpiY();
		if (dpiX < 1 || dpiY < 1)
		{
			return;
		}
		byte[] data = new byte[9];
		int ppuX = (int)(dpiX * (100 / 2.54));
		int ppuY = (int)(dpiY * (100 / 2.54));
		ArrayConverter.setIntBE(data, 0, ppuX);
		ArrayConverter.setIntBE(data, 4, ppuY);
		data[8] = 1; // unit is the meter
		saveChunk(CHUNK_TYPE_PHYS, data.length, data);
	}

	private void savePlteChunk() throws IOException
	{
		if (colorType != COLOR_TYPE_INDEXED)
		{
			return;
		}
		Paletted8Image image = (Paletted8Image)getImage();
		Palette pal = image.getPalette();
		int numEntries = pal.getNumEntries();
		byte[] data = new byte[numEntries * 3];
		for (int i = 0, j = 0; i < numEntries; i++, j += 3)
		{
			data[j] = (byte)pal.getSample(RGBIndex.INDEX_RED, i);
			data[j + 1] = (byte)pal.getSample(RGBIndex.INDEX_GREEN, i);
			data[j + 2] = (byte)pal.getSample(RGBIndex.INDEX_BLUE, i);
		}
		saveChunk(CHUNK_TYPE_PLTE, data.length, data);
	}

	private void saveTextChunks() throws IOException
	{
		int index = 0;
		while (index < getNumComments())
		{
			String comment = getComment(index++);
			comment = "Comment\000" + comment;
			byte[] data = comment.getBytes("ISO-8859-1");
			saveChunk(CHUNK_TYPE_TEXT, data.length, data);
		}
	}

	private void saveTimeChunk() throws IOException
	{
		if (modification == null)
		{
			return;
		}
		byte[] data = new byte[7];
		ArrayConverter.setShortBE(data, 0, (short)modification.get(Calendar.YEAR));
		data[2] = (byte)(modification.get(Calendar.MONTH) + 1);
		data[3] = (byte)modification.get(Calendar.DAY_OF_MONTH);
		data[4] = (byte)modification.get(Calendar.HOUR_OF_DAY);
		data[5] = (byte)modification.get(Calendar.MINUTE);
		data[6] = (byte)modification.get(Calendar.SECOND);
		saveChunk(CHUNK_TYPE_TIME, data.length, data);
	}

	/**
	 * Sets the compression level to be used with the underlying
	 * {@link java.util.zip.Deflater} object which does the compression.
	 * If no value is specified, {@link java.util.zip.Deflater#DEFAULT_COMPRESSION}
	 * is used.  
	 * @param newLevel compression level, from 0 to 9, 0 being fastest 
	 *  and compressing worst and 9 offering highest compression and taking
	 *  the most time 
	 */
	public void setCompressionLevel(int newLevel)
	{
		if (newLevel >= 0 && newLevel <= 9)
		{
			deflateLevel = newLevel;
		}
		else
		{
			throw new IllegalArgumentException("Compression level must be from 0..9; got " + newLevel);
		}
	}

	/**
	 * Sets the compression strategy to be used with the underlying
	 * {@link java.util.zip.Deflater} object which does the compression.
	 * If no value is specified, {@link java.util.zip.Deflater#DEFAULT_STRATEGY}
	 * is used.  
	 * @param newStrategy one of Deflater's strategy values: 
	 *  {@link java.util.zip.Deflater#DEFAULT_STRATEGY},
	 *  {@link java.util.zip.Deflater#FILTERED},
	 *  {@link java.util.zip.Deflater#HUFFMAN_ONLY}
	 */
	public void setCompressionStrategy(int newStrategy)
	{
		if (newStrategy == Deflater.FILTERED ||
		    newStrategy == Deflater.DEFAULT_STRATEGY ||
		    newStrategy == Deflater.HUFFMAN_ONLY)
		{
			deflateStrategy = newStrategy;
		}
		else
		{
			throw new IllegalArgumentException("Unknown compression strategy: " + newStrategy);
		}
	}

	/**
	 * Sets the size of IDAT chunks generated when encoding.
	 * If this method is never called, a default value of 32768 bytes (32 KB) is used.
	 * Note that a byte array of the size of the value you specify here is allocated,
	 * so make sure that you keep the value small enough to stay within a
	 * system's memory.
	 * <p>
	 * Compressed image data is spread over several IDAT chunks by this codec.
	 * The length of the compressed data of a complete image is known only after the complete image 
	 * has been encoded.
	 * With PNG, that length value has to be stored before the compressed data as a chunk size value.
	 * This codec is supposed to work with {@link java.io.OutputStream} objects,
	 * so seeking back to adjust the chunk size value of an IDAT chunk is not
	 * possible.
	 * That's why all data of a chunk is compressed into a memory buffer.
	 * Whenever the buffer gets full, it is written to output as an IDAT chunk.
	 * <p>
	 * Note that the last IDAT chunk may be smaller than the size defined here.
	 * @param newSize size of encoding compressed data buffer
	 */
	public void setEncodingIdatSize(int newSize)
	{
		if (newSize < 1)
		{
			throw new IllegalArgumentException("Minimum IDAT chunk size must be 1 or larger.");
		}
		encodingMinIdatSize = newSize;
	}

	public void setFile(String fileName, CodecMode codecMode) throws IOException, UnsupportedCodecModeException
	{
		if (codecMode == CodecMode.LOAD)
		{
			setInputStream(new BufferedInputStream(new FileInputStream(fileName)));
		}
		else
		{
			super.setFile(fileName, codecMode);
		}
	}

	/**
	 * Sets date and time of last modification of the image to be stored in a PNG stream
	 * when saving.
	 * Make sure the argument object has UTC as time zone
	 * (<a target="_top" href="http://www.w3.org/TR/PNG#C.tIME">as
	 * demanded by the PNG specs)</a>.
	 * If you want the current time and date, use 
	 * <code>new GregorianCalendar(new SimpleTimeZone(0, "UTC"))</code>
	 * as parameter for this method.
	 * @param time time of last modification of the image
	 */
	public void setModification(Calendar time)
	{
		modification = time;
	}

	/**
	 * Skips a number of bytes in the input stream.
	 * @param num number of bytes to be skipped
	 * @throws IOException if there were I/O errors
	 */
	private void skip(long num) throws IOException
	{
		while (num > 0)
		{
			long numSkipped = in.skip(num);
			if (numSkipped > 0)
			{
				num -= numSkipped;
			}
		}
	}

	private void storeInterlacedAdam7(int pass, int y, byte[] buffer)
	{
		switch(colorType)
		{
			case(COLOR_TYPE_GRAY):
			{
				storeInterlacedAdam7Gray(pass, y, buffer);
				break;
			}
			case(COLOR_TYPE_RGB):
			{
				storeInterlacedAdam7Rgb(pass, y, buffer);
				break;
			}
			case(COLOR_TYPE_RGB_ALPHA):
			{
				storeInterlacedAdam7RgbAlpha(pass, y, buffer);
				break;
			}
			case(COLOR_TYPE_GRAY_ALPHA):
			{
				storeInterlacedAdam7GrayAlpha(pass, y, buffer);
				break;
			}
			case(COLOR_TYPE_INDEXED):
			{
				storeInterlacedAdam7Indexed(pass, y, buffer);
				break;
			}
		}
	}

	private void storeInterlacedAdam7Gray(int pass, int y, byte[] buffer)
	{
		int x = ADAM7_FIRST_COLUMN[pass];
		final int incr = ADAM7_COLUMN_INCREMENT[pass];
		final int x1 = getBoundsX1();
		final int x2 = getBoundsX2();
		int offset = 0;
		int numColumns = computeColumnsAdam7(pass);
		int numPackedBytes = computeBytesPerRow(numColumns);
		byte[] dest = new byte[numColumns + 7];
		switch(precision)
		{
			case(1):
			{
				BilevelImage bilevelImage = (BilevelImage)image;
				ArrayConverter.decodePacked1Bit(buffer, 0, dest, 0, numPackedBytes);
				while (x <= x2)
				{
					if (x >= x1)
					{
						if (dest[offset] == 0)
						{
							bilevelImage.putBlack(x - x1, y);
						}
						else
						{
							bilevelImage.putWhite(x - x1, y);
						}
					}
					x += incr;
					offset++;
				}
				break;
			}
			case(2):
			{
				Gray8Image grayImage = (Gray8Image)image;
				ArrayConverter.convertPacked2BitIntensityTo8Bit(buffer, 0, dest, 0, numPackedBytes);
				while (x <= x2)
				{
					if (x >= x1)
					{
						grayImage.putByteSample(x - x1, y, dest[offset]);
					}
					x += incr;
					offset++;
				}
				break;
			}
			case(4):
			{
				Gray8Image grayImage = (Gray8Image)image;
				ArrayConverter.convertPacked4BitIntensityTo8Bit(buffer, 0, dest, 0, numPackedBytes);
				while (x <= x2)
				{
					if (x >= x1)
					{
						grayImage.putByteSample(x - x1, y, dest[offset]);
					}
					x += incr;
					offset++;
				}
				break;
			}
			case(8):
			{
				Gray8Image grayImage = (Gray8Image)image;
				while (x <= x2)
				{
					if (x >= x1)
					{
						grayImage.putSample(x - x1, y, buffer[offset]);
					}
					x += incr;
					offset++;
				}
				break;
			}
			case(16):
			{
				Gray16Image grayImage = (Gray16Image)image;
				while (x <= x2)
				{
					if (x >= x1)
					{
						int sample = (buffer[offset] & 0xff) << 8;
						sample |= (buffer[offset + 1] & 0xff);
						grayImage.putSample(x, y, sample);
					}
					x += incr;
					offset += 2;
				}
				break;
			}
		}
	}

	private void storeInterlacedAdam7GrayAlpha(int pass, int y, byte[] buffer)
	{
		int x = ADAM7_FIRST_COLUMN[pass];
		final int incr = ADAM7_COLUMN_INCREMENT[pass];
		final int x1 = getBoundsX1();
		final int x2 = getBoundsX2();
		int offset = 0;
		switch(precision)
		{
			case(8):
			{
				Gray8Image grayImage = (Gray8Image)image;
				while (x <= x2)
				{
					if (x >= x1)
					{
						grayImage.putSample(x - x1, y, buffer[offset]);
						// alpha
					}
					x += incr;
					offset += 2;
				}
				break;
			}
			case(16):
			{
				Gray16Image grayImage = (Gray16Image)image;
				while (x <= x2)
				{
					if (x >= x1)
					{
						int sample = (buffer[offset] & 0xff) << 8;
						sample |= (buffer[offset + 1] & 0xff);
						grayImage.putSample(x, y, sample);
						// store alpha
					}
					x += incr;
					offset += 4;
				}
				break;
			}
		}
	}

	private void storeInterlacedAdam7Indexed(int pass, int y, byte[] buffer)
	{
		Paletted8Image palImage = (Paletted8Image)image;
		int x = ADAM7_FIRST_COLUMN[pass];
		final int incr = ADAM7_COLUMN_INCREMENT[pass];
		final int x1 = getBoundsX1();
		final int x2 = getBoundsX2();
		int offset = 0;
		int numColumns = computeColumnsAdam7(pass);
		int numPackedBytes = computeBytesPerRow(numColumns);
		byte[] dest = new byte[numColumns + 7];
		switch(precision)
		{
			case(1):
			{
				ArrayConverter.decodePacked1Bit(buffer, 0, dest, 0, numPackedBytes);
				while (x <= x2)
				{
					if (x >= x1)
					{
						palImage.putByteSample(x - x1, y, dest[offset]);
					}
					x += incr;
					offset++;
				}
				break;
			}
			case(2):
			{
				ArrayConverter.decodePacked2Bit(buffer, 0, dest, 0, numPackedBytes);
				while (x <= x2)
				{
					if (x >= x1)
					{
						palImage.putByteSample(x - x1, y, dest[offset]);
					}
					x += incr;
					offset++;
				}
				break;
			}
			case(4):
			{
				ArrayConverter.decodePacked4Bit(buffer, 0, dest, 0, numPackedBytes);
				while (x <= x2)
				{
					if (x >= x1)
					{
						palImage.putByteSample(x - x1, y, dest[offset]);
					}
					x += incr;
					offset++;
				}
				break;
			}
			case(8):
			{
				while (x <= x2)
				{
					if (x >= x1)
					{
						palImage.putSample(x - x1, y, buffer[offset]);
					}
					x += incr;
					offset++;
				}
				break;
			}
		}
	}

	private void storeInterlacedAdam7Rgb(int pass, int y, byte[] buffer)
	{
		int x = ADAM7_FIRST_COLUMN[pass];
		final int x1 = getBoundsX1();
		final int x2 = getBoundsX2();
		final int incr = ADAM7_COLUMN_INCREMENT[pass];
		int offset = 0;
		if (precision == 8)
		{
			RGB24Image rgbImage = (RGB24Image)image;
			while (x <= x2)
			{
				if (x >= x1)
				{
					rgbImage.putSample(RGB24Image.INDEX_RED, x, y, buffer[offset]);
					rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, buffer[offset + 1]);
					rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, buffer[offset + 2]);
				}
				x += incr;
				offset += 3;
			}
		}
		else
		if (precision == 16)
		{
			RGB48Image rgbImage = (RGB48Image)image;
			while (x <= x2)
			{
				if (x >= x1)
				{
					int red = (buffer[offset] & 0xff) << 8;
					red |= buffer[offset + 1] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_RED, x, y, red);
	
					int green = (buffer[offset + 2] & 0xff) << 8;
					green |= buffer[offset + 3] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, green);
		
					int blue = (buffer[offset + 4] & 0xff) << 8;
					blue |= buffer[offset + 5] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, blue);
				}
				x += incr;
				offset += 6;
			}
		}
	}

	private void storeInterlacedAdam7RgbAlpha(int pass, int y, byte[] buffer)
	{
		int x = ADAM7_FIRST_COLUMN[pass];
		final int x1 = getBoundsX1();
		final int x2 = getBoundsX2();
		final int incr = ADAM7_COLUMN_INCREMENT[pass];
		int offset = 0;
		if (precision == 8)
		{
			RGB24Image rgbImage = (RGB24Image)image;
			while (x <= x2)
			{
				if (x >= x1)
				{
					rgbImage.putSample(RGB24Image.INDEX_RED, x, y, buffer[offset]);
					rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, buffer[offset + 1]);
					rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, buffer[offset + 2]);
					// store alpha
				}
				x += incr;
				offset += 4;
			}
		}
		else
		if (precision == 16)
		{
			RGB48Image rgbImage = (RGB48Image)image;
			while (x <= x2)
			{
				if (x >= x1)
				{
					int red = (buffer[offset] & 0xff) << 8;
					red |= buffer[offset + 1] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_RED, x, y, red);
	
					int green = (buffer[offset + 2] & 0xff) << 8;
					green |= buffer[offset + 3] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, green);
		
					int blue = (buffer[offset + 4] & 0xff) << 8;
					blue |= buffer[offset + 5] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, blue);
					
					// store alpha
				}
				x += incr;
				offset += 8;
			}
		}
	}


	private void storeNonInterlaced(int y, byte[] buffer)
	{
		switch(colorType)
		{
			case(COLOR_TYPE_GRAY):
			{
				storeNonInterlacedGray(y, buffer);
				break;
			}
			case(COLOR_TYPE_GRAY_ALPHA):
			{
				storeNonInterlacedGrayAlpha(y, buffer);
				break;
			}
			case(COLOR_TYPE_INDEXED):
			{
				storeNonInterlacedIndexed(y, buffer);
				break;
			}
			case(COLOR_TYPE_RGB):
			{
				storeNonInterlacedRgb(y, buffer);
				break;
			}
			case(COLOR_TYPE_RGB_ALPHA):
			{
				storeNonInterlacedRgbAlpha(y, buffer);
				break;
			}
		}
	}

	private void storeNonInterlacedGray(int y, byte[] buffer)
	{
		switch(precision)
		{
			case(1):
			{
				BilevelImage bilevelImage = (BilevelImage)image;
				int x1 = getBoundsX1();
				bilevelImage.putPackedBytes(0, y, getBoundsWidth(), buffer, x1 / 8, x1 % 8);
				break;
			}
			case(2):
			{
				Gray8Image grayImage = (Gray8Image)image;
				byte[] dest = new byte[width + 3];
				ArrayConverter.convertPacked2BitIntensityTo8Bit(buffer, 0, dest, 0, buffer.length);
				grayImage.putByteSamples(0, 0, y, getBoundsWidth(), 1, dest, getBoundsX1());
				break;
			}
			case(4):
			{
				Gray8Image grayImage = (Gray8Image)image;
				byte[] dest = new byte[width + 1];
				ArrayConverter.convertPacked4BitIntensityTo8Bit(buffer, 0, dest, 0, buffer.length);
				grayImage.putByteSamples(0, 0, y, getBoundsWidth(), 1, dest, getBoundsX1());
				break;
			}
			case(8):
			{
				Gray8Image grayImage = (Gray8Image)image;
				int offset = getBoundsX1();
				int x = 0;
				int k = getBoundsWidth();
				while (k > 0)
				{
					grayImage.putSample(0, x++, y, buffer[offset++]);
					k--;
				}
				break;
			}
			case(16):
			{
				Gray16Image grayImage = (Gray16Image)image;
				int offset = getBoundsX1();
				int x = 0;
				int k = getBoundsWidth();
				while (k > 0)
				{
					int sample = (buffer[offset++] & 0xff) << 8;
					sample |= (buffer[offset++] & 0xff);
					grayImage.putSample(x++, y, sample);
					k--;
				}
				break;
			}
		}
	}

	private void storeNonInterlacedGrayAlpha(int y, byte[] buffer)
	{
		switch(precision)
		{
			case(8):
			{
				Gray8Image grayImage = (Gray8Image)image;
				int offset = getBoundsX1();
				int x = 0;
				int k = getBoundsWidth();
				while (k > 0)
				{
					grayImage.putSample(0, x++, y, buffer[offset++]);
					offset++; // skip alpha; should be stored in a TransparencyInformation object
					k--;
				}
				break;
			}
			case(16):
			{
				Gray16Image grayImage = (Gray16Image)image;
				int offset = getBoundsX1();
				int x = 0;
				int k = getBoundsWidth();
				while (k > 0)
				{
					int sample = (buffer[offset++] & 0xff) << 8;
					sample |= (buffer[offset++] & 0xff);
					grayImage.putSample(x++, y, sample);
					offset += 2; // skip alpha;  TODO: store in TransparencyInformation object
					k--;
				}
				break;
			}
		}
	}

	private void storeNonInterlacedIndexed(int y, byte[] buffer)
	{
		Paletted8Image palImage = (Paletted8Image)image;
		switch(precision)
		{
			case(1):
			{
				byte[] dest = new byte[width + 7];
				ArrayConverter.decodePacked1Bit(buffer, 0, dest, 0, buffer.length);
				palImage.putByteSamples(0, 0, y, getBoundsWidth(), 1, dest, getBoundsX1());
				break;
			}
			case(2):
			{
				byte[] dest = new byte[width + 3];
				ArrayConverter.decodePacked2Bit(buffer, 0, dest, 0, buffer.length);
				palImage.putByteSamples(0, 0, y, getBoundsWidth(), 1, dest, getBoundsX1());
				break;
			}
			case(4):
			{
				byte[] dest = new byte[width + 1];
				ArrayConverter.decodePacked4Bit(buffer, 0, dest, 0, buffer.length);
				palImage.putByteSamples(0, 0, y, getBoundsWidth(), 1, dest, getBoundsX1());
				break;
			}
			case(8):
			{
				int offset = getBoundsX1();
				int x = 0;
				int k = getBoundsWidth();
				while (k > 0)
				{
					palImage.putSample(0, x++, y, buffer[offset++]);
					k--;
				}
				break;
			}
		}
	}

	private void storeNonInterlacedRgb(int y, byte[] buffer)
	{
		if (precision == 8)
		{
			RGB24Image rgbImage = (RGB24Image)image;
			int offset = getBoundsX1() * 3;
			int x = 0;
			int k = getBoundsWidth();
			while (k > 0)
			{
				rgbImage.putSample(RGB24Image.INDEX_RED, x, y, buffer[offset++]);
				rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, buffer[offset++]);
				rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, buffer[offset++]);
				x++;
				k--;
			}
		}
		else
		if (precision == 16)
		{
			RGB48Image rgbImage = (RGB48Image)image;
			int offset = getBoundsX1() * 6;
			int x = 0;
			int k = getBoundsWidth();
			while (k > 0)
			{
				int red = (buffer[offset++] & 0xff) << 8;
				red |= buffer[offset++] & 0xff;
				rgbImage.putSample(RGB24Image.INDEX_RED, x, y, red);

				int green = (buffer[offset++] & 0xff) << 8;
				green |= buffer[offset++] & 0xff;
				rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, green);
	
				int blue = (buffer[offset++] & 0xff) << 8;
				blue |= buffer[offset++] & 0xff;
				rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, blue);
	
				x++;
				k--;
			}
		}
	}

	private void storeNonInterlacedRgbAlpha(int y, byte[] buffer)
	{
		switch(precision)
		{
			case(8):
			{
				RGB24Image rgbImage = (RGB24Image)image;
				int offset = getBoundsX1() * 3;
				int x = 0;
				int k = getBoundsWidth();
				while (k > 0)
				{
					rgbImage.putSample(RGB24Image.INDEX_RED, x, y, buffer[offset++]);
					rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, buffer[offset++]);
					rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, buffer[offset++]);
					offset++; // skip alpha; TODO: store in TransparencyInformation object
					x++;
					k--;
				}
				break;
			}
			case(16):
			{
				RGB48Image rgbImage = (RGB48Image)image;
				int offset = getBoundsX1() * 8;
				int x = 0;
				int k = getBoundsWidth();
				while (k > 0)
				{
					int red = (buffer[offset++] & 0xff) << 8;
					red |= buffer[offset++] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_RED, x, y, red);
		
					int green = (buffer[offset++] & 0xff) << 8;
					green |= buffer[offset++] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_GREEN, x, y, green);
		
					int blue = (buffer[offset++] & 0xff) << 8;
					blue |= buffer[offset++] & 0xff;
					rgbImage.putSample(RGB24Image.INDEX_BLUE, x, y, blue);
		
					offset += 2; // skip alpha; TODO: store in TransparencyInformation object
					x++;
					k--;
				}
				break;
			}
		}
	}

	public String suggestFileExtension(PixelImage image)
	{
		return ".png";
	}
}
