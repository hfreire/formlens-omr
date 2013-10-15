/*
 * TIFFCodec
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.Vector;
import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedCodecModeException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray16Image;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.MemoryRGB48Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * A codec to read Tagged Image File Format (TIFF) image files.
 *
 * <h3>Usage example</h3>
 * Load an image from a TIFF file.
 * <pre>
 * TIFFCodec codec = new TIFFCodec();
 * codec.setFile("image.tif", CodecMode.LOAD);
 * codec.process();
 * PixelImage loadedImage = codec.getImage();
 * </pre>
 * Saving images is not supported by this codec.
 *
 * <h3>Compression types</h3>
 * <h4>Reading</h4>
 * The TIFF package supports the following compression types when reading:
 * <ul>
 * <li><em>Uncompressed</em>. Compression method number 1. Works with all types of image data. See {@link TIFFDecoderUncompressed}.</li>
 * <li><em>Packbits</em>. Compression method number 32773. Works with all types of image data. See {@link TIFFDecoderPackbits}.</li>
 * <li><em>CCITT Group 3 1-Dimensional Modified Huffman runlength encoding</em>. Compression method number 2. 
 *  Works with bilevel image data only. See {@link TIFFDecoderModifiedHuffman}.</li>
 * <li><em>Deflated</em>. Compression method number 8 or 32946. Works with all types of image data. See {@link TIFFDecoderDeflated}.</li>
 * <li><em>LogLuv RLE</em> and <em>LogLuv 24</em>. Compression method numbers 34676 and 34677. Works only with LogLuv color data. See {@link TIFFDecoderLogLuv}.</li>
 * </ul>
 * <p>
 * Note that you can write your own decoder (extending {@link TIFFDecoder}) for any compression type
 * you want.
 * </p>
 *
 * <h3>Image types</h3>
 * <h4>Reading</h4>
 * The TIFF package supports the following image / color types when reading:
 * <ul>
 * <li><em>Black &amp; white</em>. JIU image data type {@link net.sourceforge.jiu.data.BilevelImage}.</li>
 * <li><em>Grayscale, 4 and 8 bits per pixel</em>. JIU image data type {@link net.sourceforge.jiu.data.Gray8Image}.</li>
 * <li>TODO add other image types</li>
 * </ul>
 * <p>
 * Note that you can write your own decoder (extending {@link TIFFDecoder}) for any compression type
 * you want.
 * </p>
 *
 * <h4>Writing</h4>
 * <p>Writing TIFFs is not supported. 
 * I don't know if or when it will be supported.</p>
 *
 * <h3>Strips and tiles</h3>
 * The early versions of TIFF considered an image to be a sequence of <em>strips</em>.
 * Each strip was a rectangular part of the image, as wide as the complete image, 
 * and with a certain height defined by the <em>rows per strip</em> tag.
 * So with a number of rows per strip of 10, and an image height of 200, you would
 * have to store 20 strips.
 * It was recommended that a strip should not be larger than 8 KB (RAM was tighter
 * in those days).
 * The rule of thumb to define the number of rows per strip was to see how many rows
 * would fit into 8 KB.
 * <p>
 * Later, the concept of <em>tiles</em> was added to the TIFF specs.
 * Tiled TIFFs are separated into rectangles that not only had a defineable
 * height but also a defineable width (tile width and tile height are also stored in
 * corresponding tags).
 * <p>
 * Obviously, strips are just a special case of tiles, with the tile width being equal
 * to image width.
 * That is why JIU internally only deals with tiles.
 * The only difference: No row padding takes place for strips.
 * In a tiled image with a tile height of 10 and an image height of 14, 
 * the image is two tiles high.
 * 
 * <h3>Number of images</h3>
 * TIFF allows for multiple images in a single file.
 * This codec regards the image index, queries {@link #getImageIndex} and skips to the
 * correct image.
 *
 * <h3>Bounds</h3>
 * The bounds concept of JIU is supported by this codec.
 * So you can specify bounds of a rectangular part of an image that you want to load
 * instead of loading the complete image.
 *
 * <h3>Color spaces</h3>
 * The following color spaces are understood when reading truecolor TIFF files.
 * <ul>
 * <li><em>RGB</em> - should cover most truecolor files.</li>
 * <li><em>CMYK</em> - is supported, but colors may not be exactly right.
 *  CMYK data is converted to RGB on the fly, so the codec user never accesses CMYK data.</li>
 * <li><em>LogLuv</em> - is supported, but not all flavors yet.</li>
 * </ul>
 * <h3>Physical resolution</h3>
 * DPI information can be stored in TIFF files.
 * If that information is available, this codec retrieves it so that it
 * can be queried using {@link #getDpiX} and {@link #getDpiY}.
 *
 * <h3>Background information on TIFF</h3>
 * TIFF is an important image file format for DTP (desktop publishing).
 * The advantages of TIFF include its flexibility, availability of libraries to read
 * and write TIFF files and its good support in existing software.
 * The major disadvantage of TIFF is its complexity, which makes it hard for software
 * to support all possible valid TIFF files.
 * <p>
 * TIFF was created by Aldus and now <em>belongs</em> to Adobe, who offer a specification document: 
 * <a target="_top" href="http://partners.adobe.com/asn/developer/PDFS/TN/TIFF6.pdf">TIFF
 * (Tagged Image File Format) 6.0 Specification</a> (updated on Web September, 20 1995, 
 * document dated June, 3 1992) (PDF: 385 KB / 121 pages).
 * <p>
 * Other good references include the <a target="_top" href="http://www.libtiff.org">homepage 
 * of libtiff</a>, a free C library to read and write TIFF files and
 * <a target="_top" href="http://home.earthlink.net/~ritter/tiff/">The Unofficial TIFF
 * homepage</a> by Niles Ritter.
 * Also see <a target="_top" href="http://dmoz.org/Computers/Data_Formats/Graphics/Pixmap/TIFF/">the TIFF section</a>
 * of the <a target="_top" href="http://www.dmoz.org">Open Directory</a>.
 * <p>
 * TIFF is used for various specialized tasks.
 * As an example, see <a target="_top" href="http://www.remotesensing.org/geotiff/geotiff.html">GeoTIFF</a> (geographical
 * data) or <a target="_top" href="http://www.ba.wakwak.com/~tsuruzoh/index-e.html">EXIF</a>
 * (digital camera metadata; this is actually a TIFF directory embedded in a JPEG header).
 * <p>
 * Here's a list of features that make TIFF quite complex:
 * <ul>
 * <li>More than one image can be stored in a TIFF file.</li>
 * <li>Integer values that are larger than one byte can be in either little or big endian byte order.</li>
 * <li>Various color types are supported (bilevel, gray, paletted, all kinds of color spaces (RGB / YCbCr / CMYK).
 *  It's easy to add new color types, so this list can grow.</li>
 * <li>The meta data (information that describes the image and how it is stored) can be distributed all over 
 *  the file.</li>
 * <li>Image data is stored as packed bytes, 4-bit-nibbles, bytes and 16-bit-integers. Other types are possible
 *  as well.</li>
 * <li>Various compression types are supported; not all types can be used on all color types.</li>
 * <li>Image data can be stored in strips or tiles.</li>
 * <li>An arbitrary number of non-image-data samples can stored within the image data.</li>
 * <li>Color types with more than one sample per pixel can store data in an interleaved (<em>chunky</em>)
 *  way or in planes.</li>
 * <li>Different ways of defining black and white are possible with bilevel and grayscale images.</li>
 * </ul>
 * 
 * @author Marco Schmidt
 */
public class TIFFCodec extends ImageCodec implements TIFFConstants
{
	public static final int BYTE_ORDER_MOTOROLA = 0;
	public static final int BYTE_ORDER_INTEL = 1;
	private static final int MAGIC_INTEL = 0x49492a00;
	private static final int MAGIC_MOTOROLA = 0x4d4d002a;

	private int byteOrder;
	private int nextIfdOffset;

	private static Hashtable decoders;
	static
	{
		decoders = new Hashtable();
		registerDecoder(TIFFDecoderDeflated.class);
		registerDecoder(TIFFDecoderModifiedHuffman.class);
		registerDecoder(TIFFDecoderPackbits.class);
		registerDecoder(TIFFDecoderUncompressed.class);
		registerDecoder(TIFFDecoderLogLuv.class);
	}

	/**
	 * If the current byte order is {@link BYTE_ORDER_MOTOROLA} and the type 
	 * argument is {@link TiffConstants.TAG_TYPE_BYTE} or
	 * {@link TiffConstants.TAG_TYPE_SHORT}, the value parameter must
	 * be adjusted by some bitshifting.
	 * If the above mentioned criteria are not met, the value argument is 
	 * returned without any modifications.
	 * <p>
	 * <em>Why this is necessary remains a mystery to me. Marco</em>
	 *
	 * @param value the int value which may have to be adjusted
	 * @return the value parameter which may have been modified
	 */
	private int adjustInt(int value, int type)
	{
		if (getByteOrder() == BYTE_ORDER_MOTOROLA)
		{
			if (type == TAG_TYPE_BYTE)
			{
				return ((value >> 24) & 0xff);
			}
			else
			if (type == TAG_TYPE_SHORT)
			{
				return ((value >> 16) & 0xff) | (((value >> 24) & 0xff) << 8);
			}
			else
			{
				return value;
			}
		}
		else
		{
			return value;
		}
	}

	private static TIFFDecoder createDecoder(TIFFCodec codec, TIFFImageFileDirectory ifd, int tileIndex) throws 
		IOException, 
		UnsupportedTypeException
	{
		Integer compression = new Integer(ifd.getCompression());
		Class decoderClass = (Class)decoders.get(compression);
		if (decoderClass == null)
		{
			throw new UnsupportedTypeException("Could not create decoder for this compression type: " + 
				compression.intValue());
		}
		Object instance;
		try
		{
			instance = decoderClass.newInstance();
		}
		catch (Exception e)
		{
			throw new UnsupportedTypeException("Could not create decoder for this compression type.");
		}
		if (instance instanceof TIFFDecoder)
		{
			TIFFDecoder decoder = (TIFFDecoder)instance;
			decoder.setCodec(codec);
			decoder.setTileIndex(tileIndex);
			decoder.setImageFileDirectory(ifd);
			try
			{
				decoder.initialize();
			}
			catch (MissingParameterException mpe)
			{
				throw new UnsupportedTypeException("Unable to initialize decoder: " + mpe.toString());
			}
			return decoder;
		}
		else
		{
			throw new UnsupportedTypeException("Could not create decoder for this compression type.");
		}
	}

	/**
	 * Returns the current byte order, either 
	 * {@link #BYTE_ORDER_INTEL} or
	 * {@link #BYTE_ORDER_MOTOROLA}.
	 * @return current byte order
	 */
	public int getByteOrder()
	{
		return byteOrder;
	}

	public String getFormatName()
	{
		return "Tagged Image File Format (TIFF)";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/tiff", "image/tif"};
	}

	/**
	 * Returns the name of a tag in English.
	 * @param id of the tag for which a name is to be returned
	 * @return tag name as String or a question mark <code>?</code>
	 */
	public static String getTagName(int id)
	{
		switch(id)
		{
			case(TAG_ARTIST): return "Artist";
			case(TAG_BAD_FAX_LINES): return "Bad fax lines";
			case(TAG_BITS_PER_SAMPLE): return "Bits per sample";
			case(TAG_CELL_LENGTH): return "Cell length";
			case(TAG_CELL_WIDTH): return "Cell width";
			case(TAG_CLEAN_FAX_DATA): return "Clean fax data";
			case(TAG_COLOR_MAP): return "Color map";
			case(TAG_COMPRESSION): return "Compression";
			case(TAG_CONSECUTIVE_BAD_FAX_LINES): return "Consecutive bad fax lines";
			case(TAG_COPYRIGHT): return "Copyright";
			case(TAG_DATE_TIME): return "Date and time";
			case(TAG_DOCUMENT_NAME): return "Document name";
			case(TAG_EXTRA_SAMPLES): return "Extra samples";
			case(TAG_FILL_ORDER): return "Fill order";
			case(TAG_FREE_BYTE_COUNTS): return "Free byte counts";
			case(TAG_FREE_OFFSETS): return "Free offsets";
			case(TAG_GRAY_RESPONSE_CURVE): return "Gray response curve";
			case(TAG_GRAY_RESPONSE_UNIT): return "Gray response unit";
			case(TAG_HOST_COMPUTER): return "Host computer";
			case(TAG_IMAGE_DESCRIPTION): return "Image description";
			case(TAG_IMAGE_LENGTH): return "Image length";
			case(TAG_IMAGE_WIDTH): return "Image width";
			case(TAG_MAKE): return "Make";
			case(TAG_MAX_SAMPLE_VALUE): return "Maximum sample value";
			case(TAG_MIN_SAMPLE_VALUE): return "Minimum sample value";
			case(TAG_MODEL): return "Model";
			case(TAG_NEW_SUBFILE_TYPE): return "New subfile type";
			case(TAG_ORIENTATION): return "Orientation";
			case(TAG_PHOTOMETRIC_INTERPRETATION): return "Photometric interpretation";
			case(TAG_PLANAR_CONFIGURATION): return "Planar configuration";
			case(TAG_PREDICTOR): return "Predictor";
			case(TAG_RESOLUTION_UNIT): return "Resolution unit";
			case(TAG_RESOLUTION_X): return "Resolution X";
			case(TAG_RESOLUTION_Y): return "Resolution Y";
			case(TAG_ROWS_PER_STRIP): return "Rows per strip";
			case(TAG_SAMPLES_PER_PIXEL): return "Samples per pixel";
			case(TAG_SOFTWARE): return "Software";
			case(TAG_STRIP_BYTE_COUNTS): return "Strip byte counts";
			case(TAG_STRIP_OFFSETS): return "Strip offsets";
			case(TAG_TILE_BYTE_COUNTS): return "Byte counts";
			case(TAG_TILE_HEIGHT): return "Tile height";
			case(TAG_TILE_OFFSETS): return "Tile offsets";
			case(TAG_TILE_WIDTH): return "Tile width";
			default: return "?";
		}
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
	 * Attempts to load an image from a file in the TIFF format.
	 * Some options can be given to this codec before the call
	 * to this load method.
	 * <ul>
	 * <li>You must provide a {@link java.io.RandomAccessFile} using
	 *   {@link #setInput(java.io.RandomAccessFile)}.</li>
	 * <li>If there is more than one image in the input file, you
	 *   can make the codec load it by calling {@link #setImageIndex(int)}.
	 *   The argument is the index of the image, the first being <code>0</code>,
	 *   the second <code>1</code> and so on. The default is <code>0</code>.</li>
	 * </ul>
	 *
	 * @return the image if everything was successful
	 * @throws InvalidFileStructureException if the TIFF file was corrupt in some way
	 * @throws IOException if there were errors reading from the input file
	 * @throws UnsupportedTypeException if the flavour of TIFF encountered in the input
	 *  file is not supported yet
	 * @throws WrongFileFormatException
	 */
	private void load() throws 
		InvalidFileStructureException, 
		IOException, 
		UnsupportedTypeException, 
		WrongFileFormatException,
		WrongParameterException
	{
		readHeader();
		skipImageFileDirectories(getImageIndex());
		TIFFImageFileDirectory ifd = readImageFileDirectory();
		ifd.initFromTags(true);
		int dpiX = ifd.getDpiX();
		int dpiY = ifd.getDpiY();
		if (dpiX > 0 && dpiY > 0)
		{
			setDpi(dpiX, dpiY);
		}
		//ifd.dump();
		load(ifd);
	}

	private void load(TIFFImageFileDirectory ifd) throws
		InvalidFileStructureException,
		IOException,
		UnsupportedTypeException,
		WrongFileFormatException,
		WrongParameterException
	{
		setBoundsIfNecessary(ifd.getWidth(), ifd.getHeight());
		checkImageResolution();
		int width = getBoundsWidth();
		int height = getBoundsHeight();
		// create image if necessary
		PixelImage image = getImage();
		if (image == null)
		{
			int imageType = ifd.getImageType();
			switch (imageType)
			{
				case(TIFFImageFileDirectory.TYPE_BILEVEL_BYTE):
				case(TIFFImageFileDirectory.TYPE_BILEVEL_PACKED):
				{
					image = new MemoryBilevelImage(width, height);
					break;
				}
				case(TIFFImageFileDirectory.TYPE_GRAY4):
				case(TIFFImageFileDirectory.TYPE_GRAY8):
				case(TIFFImageFileDirectory.TYPE_LOGL):
				{
					image = new MemoryGray8Image(width, height);
					break;
				}
				case(TIFFImageFileDirectory.TYPE_GRAY16):
				{
					image = new MemoryGray16Image(width, height);
					break;
				}
				case(TIFFImageFileDirectory.TYPE_PALETTED4):
				case(TIFFImageFileDirectory.TYPE_PALETTED8):
				{
					image = new MemoryPaletted8Image(width, height, ifd.getPalette());
					break;
				}
				case(TIFFImageFileDirectory.TYPE_CMYK32_INTERLEAVED):
				case(TIFFImageFileDirectory.TYPE_CMYK32_PLANAR):
				case(TIFFImageFileDirectory.TYPE_RGB24_INTERLEAVED):
				case(TIFFImageFileDirectory.TYPE_LOGLUV32_INTERLEAVED):
				{
					image = new MemoryRGB24Image(width, height);
					break;
				}
				case(TIFFImageFileDirectory.TYPE_RGB48_INTERLEAVED):
				{
					image = new MemoryRGB48Image(width, height);
					break;
				}
				default:
				{
					throw new UnsupportedTypeException("Unsupported image type.");
				}
			}
			setImage(image);
		}
		int tileIndex = 0;
		int numTiles = ifd.getNumTiles();
		while (tileIndex < numTiles && !getAbort())
		{
			int x1 = ifd.getTileX1(tileIndex);
			int y1 = ifd.getTileY1(tileIndex);
			int x2 = ifd.getTileX2(tileIndex);
			int y2 = ifd.getTileY2(tileIndex);
			if (isTileRequired(x1, y1, x2, y2))
			{
				TIFFDecoder decoder = createDecoder(this, ifd, tileIndex);
				decoder.decode();
			}
			tileIndex++;
		}
	}

	public void process() throws
		MissingParameterException,
		OperationFailedException
	{
		initModeFromIOObjects();
		try
		{
			if (getMode() == CodecMode.LOAD && getRandomAccessFile() != null)
			{
				load();
			}
			else
			{
				throw new MissingParameterException("TIFF codec must have RandomAccessFile object opened for reading.");
			}
		}
		catch (IOException ioe)
		{
			close();
			throw new OperationFailedException("I/O error occurred: " + ioe.toString());
		}
	}

	/**
	 * Reads the first eight bytes from the input file, checks if this is a 
	 * valid TIFF file and stores byte order and offset of the first image
	 * file directory.
	 * @throws IOException if there were reading errors
	 * @throws WrongFileFormatException if this is not a valid TIFF file
	 */
	private void readHeader() throws
		IOException, 
		WrongFileFormatException
	{
		RandomAccessFile in = getRandomAccessFile();
		// the argument to in.seek must be changed to a variable in the future for
		// this codec to be used to read EXIF information from JPEGs;
		// for some reason, TIFF was chosen for that
		in.seek(0);
		// note: this is the only place where we use in.readInt()
		// directly; afterwards, the detected byte order
		// is regarded via this class' methods readInt() and readShort()
		// methods
		int magic = in.readInt();
		if (magic == MAGIC_INTEL)
		{
			setByteOrder(BYTE_ORDER_INTEL);
		}
		else
		if (magic == MAGIC_MOTOROLA)
		{
			setByteOrder(BYTE_ORDER_MOTOROLA);
		}
		else
		{
			throw new WrongFileFormatException("Not a TIFF file (does not " +
				"begin with II or MM followed by 42).");
		}
		nextIfdOffset = readInt();
	}

	/**
	 * Reads a complete TIFF image file directory including all data that is
	 * pointed to using the offset components and returns it.
	 * 
	 * @return the image file directory data or <code>null</code> on failure
	 */
	private TIFFImageFileDirectory readImageFileDirectory() throws 
		InvalidFileStructureException, 
		IOException
	{
		TIFFImageFileDirectory result = new TIFFImageFileDirectory();
		RandomAccessFile in = getRandomAccessFile();
		in.seek(nextIfdOffset);
		short numTags = readShort();
		if (numTags < 0)
		{
			throw new InvalidFileStructureException("Number of tags in IFD " +
				"smaller than 1 @" + nextIfdOffset + ": " + numTags);
		}
		for (int i = 0; i < numTags; i++)
		{
			TIFFTag tag = readTag();
			if (tag != null)
			{
				result.append(tag);
			}
		}
		nextIfdOffset = in.readInt();
		return result;
	}

	/**
	 * Reads a 32 bit signed integer value, regarding the current byte order.
	 * @return the loaded value
	 * @see #getByteOrder
	 */
	private int readInt() throws IOException
	{
		RandomAccessFile in = getRandomAccessFile();
		int result = in.readInt();
		if (getByteOrder() == BYTE_ORDER_INTEL)
		{
			int r1 = (result >> 24) & 0xff;
			int r2 = (result >> 16) & 0xff;
			int r3 = (result >> 8) & 0xff;
			int r4 = result & 0xff;
			return r1 | (r2 << 8) | (r3 << 16) | (r4 << 24);
		}
		else
		{
			return result;
		}
	}

	/**
	 * Reads a 16 bit signed integer value, regarding the current byte order.
	 * @return the loaded value
	 */
	private short readShort() throws IOException
	{
		RandomAccessFile in = getRandomAccessFile();
		short result = in.readShort();
		if (getByteOrder() == BYTE_ORDER_INTEL)
		{
			int r1 = (result >> 8) & 0xff;
			int r2 = result & 0xff;
			return (short)((r2 << 8) | r1);
		}
		else
		{
			return result;
		}
	}

	/**
	 * Loads a String of a given length from current position of input file.
	 * Characters are one-byte ASCII.
	 * Non-text characters are dropped.
	 * @param length number of characters in a row to be loaded
	 * @return loaded String
	 * @throws IOException if there were reading errors or an unexpected 
	 *  end of file
	 */
	private String readString(int length) throws IOException
	{
		RandomAccessFile in = getRandomAccessFile();
		StringBuffer sb = new StringBuffer(length - 1);
		while (length-- > 0)
		{
			int value = in.read();
			if (value >= 32 && value < 256)
			{
				sb.append((char)value);
			}
		}
		return sb.toString();
	}

	/**
	 * Reads a TIFF tag and all data belonging to it and returns a 
	 * TIFFTag object.
	 * The additional data is somewhere in the TIFF file.
	 * The current position will be stored, the method will seek to the offset
	 * position and load the data.
	 *
	 * @return TIFFTag containing information on the tag
	 */
	private TIFFTag readTag() throws
		InvalidFileStructureException,
		IOException
	{
		RandomAccessFile in = getRandomAccessFile();
		int id = readShort() & 0xffff;
		int type = readShort() & 0xffff;
		int count = readInt();
		int offset = readInt();
		if (count < 1)
		{
			//throw new InvalidFileStructureException("Invalid count value for tag " + id + " (" + count + ").");
			return null;
		}
		Vector vector = null;
		// perform weird bitshifting magic if necessary
		if (count == 1 &&
		    (type == TAG_TYPE_BYTE || type == TAG_TYPE_SHORT || type == TAG_TYPE_LONG))
		{
			offset = adjustInt(offset, type);
		}
		else
		if (count <= 4 && type == TAG_TYPE_BYTE)
		{
			vector = new Vector();
			for (int i = 0; i < count; i++)
			{
				byte b = (byte)((offset << (i * 8)) & 0xff);
				vector.addElement(new Byte(b));
			}
		}
		else
		if (count >= 1)
		{
			long oldOffset = in.getFilePointer();
			in.seek(offset);
			vector = new Vector();
			if (type == TAG_TYPE_ASCII)
			{
				vector.addElement(readString(count));
			}
			else
			if (type == TAG_TYPE_BYTE)
			{
				for (int i = 0; i < count; i++)
				{
					byte b = in.readByte();
					vector.addElement(new Byte(b));
				}
			}
			else
			if (type == TAG_TYPE_SHORT)
			{
				for (int i = 0; i < count; i++)
				{
					int s = readShort();
					vector.addElement(new Short((short)s));
				}
			}
			else
			if (type == TAG_TYPE_LONG)
			{
				for (int i = 0; i < count; i++)
				{
					int v = adjustInt(readInt(), type);
					vector.addElement(new Integer(v));
				}
			}
			else
			if (type == TAG_TYPE_RATIONAL)
			{
				for (int i = 0; i < count; i++)
				{
					int v1 = adjustInt(readInt(), TAG_TYPE_LONG);
					int v2 = adjustInt(readInt(), TAG_TYPE_LONG);
					vector.addElement(new TIFFRational(v1, v2));
				}
			}
			in.seek(oldOffset);
		}
		TIFFTag result = new TIFFTag(id, type, count, offset);
		result.setVector(vector);
		return result;
	}

	/**
	 * Register a {@link TIFFDecoder} class.
	 * TIFF knows many compression types, and JIU only supports some of them.
	 * To register an external TIFFDecoder class with TIFFCodec, call this method
	 * with the class field of your decoder.
	 * As an example, for your TIFFDecoderMyCompression class,
	 * call <code>TIFFCodec.registerDecoder(TIFFDecoderMyCompression.class)</code>.
	 * It will be checked if 
	 * <code>decoderClass.newInstance() instanceof TIFFDecoder</code>
	 * is true and, if so, the class will be added to an internal list.
	 * Whenever a TIFF file is to be decoded, the correct decoder is determined
	 * (each decoder knows about the compression types it supports via the getCompressionTypes method)
	 * and for each tile or strip such a decoder object will be created.
	 */
	public static void registerDecoder(Class decoderClass)
	{
		if (decoderClass == null)
		{
			return;
		}
		Object instance;
		try
		{
			instance = decoderClass.newInstance();
		}
		catch (Exception e)
		{
			return;
		}
		if (instance instanceof TIFFDecoder)
		{
			TIFFDecoder decoder = (TIFFDecoder)instance;
			Integer[] compressionTypes = decoder.getCompressionTypes();
			if (compressionTypes == null)
			{
				return;
			}
			int index = 0;
			while (index < compressionTypes.length)
			{
				Integer type = compressionTypes[index++];
				if (type != null)
				{
					decoders.put(type, decoderClass);
				}
			}
		}
	}

	/**
	 * Sets the byte order to the argument.
	 * The byte order in a TIFF file is either {@link #BYTE_ORDER_INTEL} or
	 * {@link #BYTE_ORDER_MOTOROLA}.
	 * @param newByteOrder the new byte order to be set
	 * @throws IllegalArgumentException if the argument is not one of the above
	 *  mentioned constants
	 */
	private void setByteOrder(int newByteOrder)
	{
		if (newByteOrder == BYTE_ORDER_INTEL ||
		    newByteOrder == BYTE_ORDER_MOTOROLA)
		{
			byteOrder = newByteOrder;
		}
		else
		{
			throw new IllegalArgumentException("Byte order must be either " +
				"BYTE_ORDER_INTEL or BYTE_ORDER_MOTOROLA.");
		}
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
			throw new UnsupportedCodecModeException("This TIFF codec can only load images.");
		}
	}

	/**
	 * Skips a given number of image file directories in this TIFF files.
	 * Throws an exception if there were errors or not enough image file
	 * directories.
	 * @param numDirectories the number of directories to be skipped,
	 *  should be non-negative
	 * @throws IllegalArgumentException if argument is negative
	 * @throws InvalidFileStructureException if there aren't enough image
	 *  file directories
	 * @throws IOExceptions if there were errors reading or skipping data
	 */
	private void skipImageFileDirectories(int numDirectories) throws
		InvalidFileStructureException,
		IOException
	{
		RandomAccessFile in = getRandomAccessFile();
		if (numDirectories < 0)
		{
			throw new IllegalArgumentException("Cannot skip negative number " +
				"of image file directories: " + numDirectories);
		}
		int skipped = 0;
		while (numDirectories-- > 0)
		{
			in.seek(nextIfdOffset);
			short numTags = readShort();
			in.skipBytes(numTags * 12);
			nextIfdOffset = readInt();
			if (nextIfdOffset == 0)
			{
				throw new InvalidFileStructureException("Could only skip " + 
					skipped + " image file directories, no more images in file.");
			}
			skipped++;
		}
	}
}
