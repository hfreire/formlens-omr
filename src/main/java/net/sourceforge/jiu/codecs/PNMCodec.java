/*
 * PNMCodec
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * Copyright (c) 2009 Knut Arild Erstad.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.DataOutput;
import java.io.InputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.GrayImage;
import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray16Image;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.MemoryRGB48Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * A codec to read and write Portable Anymap (PNM) image files.
 * This format includes three file types well-known in the Unix world:
 * <ul>
 * <li>PBM (Portable Bitmap - 1 bit per pixel bilevel image),</li>
 * <li>PGM (Portable Graymap - grayscale image) and</li>
 * <li>PPM (Portable Pixmap - RGB truecolor image).</li>
 * </ul>
 * <p>
 *
 * <h3>Compression</h3>
 * The file format only allows for uncompressed storage.
 *
 * <h3>ASCII mode / binary mode</h3>
 * PNM streams can be stored in binary mode or ASCII mode.
 * ASCII mode files are text files with numbers representing the pixels.
 * They become larger than their binary counterparts, but as they are
 * very redundant they can be compressed well with archive programs.
 * ASCII PGM and PPM files can have all kinds of maximum sample values,
 * thus allowing for arbitrary precision.
 * They are not restricted by byte limits.
 * PBM streams always have two colors, no matter if they are ASCII or binary.
 *
 * <h3>Color depth for PGM / PPM</h3>
 * <p>
 * The header of a PGM and PPM file stores a maximum sample value
 * (such a value is not stored for PBM, where the maximum value is always 1).
 * When in binary mode, PGM and PPM typically have a maximum sample value of 255,
 * which makes PGM 8 bits per pixel and PPM 24 bits per pixel large.
 * One sample will be stored as a single byte.
 * However, there also exist binary PGM files with a maximum sample value larger than
 * 255 and smaller than 65536.
 * These files use two bytes per sample, in network byte order (big endian).
 * I have yet to see PPM files with that property, but they are of course imagineable.
 * 16 bpp
 * </p>
 *
 * <h3>DPI values</h3>
 * PNM files cannot store the physical resolution in DPI.
 *
 * <h3>Number of images</h3>
 * Only one image can be stored in a PNM file.
 *
 * <h3>Usage example - load an image from a PNM file</h3>
 * <pre>
 * PNMCodec codec = new PNMCodec();
 * codec.setFile("test.ppm", CodecMode.LOAD);
 * codec.process();
 * codec.close();
 * PixelImage image = codec.getImage();
 * </pre>
 *
 * <h3>Usage example - save an image to a PNM file</h3>
 * <pre>
 * PNMCodec codec = new PNMCodec();
 * BilevelImage myFax = ...; // initialize
 * codec.setImage(myFax);
 * codec.setFile("out.pbm", CodecMode.SAVE);
 * codec.process();
 * codec.close();
 * </pre>
 *
 * @author Marco Schmidt
 */
public class PNMCodec extends ImageCodec
{
	/**
	 * Image type constant for images of unknown type.
	 */
	public static final int IMAGE_TYPE_UNKNOWN = -1;

	/**
	 * Image type constant for bilevel images, stored in PBM files.
	 */
	public static final int IMAGE_TYPE_BILEVEL = 0;

	/**
	 * Image type constant for grayscale images, stored in PGM files.
	 */
	public static final int IMAGE_TYPE_GRAY = 1;

	/**
	 * Image type constant for RGB truecolor images, stored in PPM files.
	 */
	public static final int IMAGE_TYPE_COLOR = 2;
	private static final String[] IMAGE_TYPE_FILE_EXTENSIONS = 
		{".pbm", ".pgm", ".ppm"};
	private Boolean ascii;
	private int columns;
	private int imageType;
	private PushbackInputStream in;
	private DataOutput out;
	private int height;
	private int maxSample;
	private int width;

	/**
	 * Attempts to find the appropriate image type by looking at a file's name.	
	 * Ignores case when comparing.
	 * Returns {@link #IMAGE_TYPE_BILEVEL} for <code>.pbm</code>,
	 * {@link #IMAGE_TYPE_GRAY} for <code>.pgm</code> and
	 * {@link #IMAGE_TYPE_COLOR} for <code>.ppm</code>.
	 * Otherwise, {@link #IMAGE_TYPE_UNKNOWN} is returned.
	 * To get a file extension given that you have an image type, use
	 * {@link #getTypicalFileExtension}.
	 * 
	 * @param fileName the file name to be examined
	 * @return one of the <code>IMAGE_TYPE_xxx</code> constants of this class
	 */
	public static int determineImageTypeFromFileName(String fileName)
	{
		if (fileName == null || fileName.length() < 4)
		{
			return IMAGE_TYPE_UNKNOWN;
		}
		String ext = fileName.substring(fileName.length() - 3);
		ext = ext.toLowerCase();
		for (int i = 0; i < IMAGE_TYPE_FILE_EXTENSIONS.length; i++)
		{
			if (IMAGE_TYPE_FILE_EXTENSIONS[i].equals(ext))
			{
				return i;
			}
		}
		return IMAGE_TYPE_UNKNOWN;
	}

	/**
	 * Returns if ASCII mode was used for loading an image or will
	 * be used to store an image.
	 * @return true for ASCII mode, false for binary mode, null if that information is not available
	 * @see #setAscii
	 */
	public Boolean getAscii()
	{
		return ascii;
	}

	public String getFormatName()
	{
		return "Portable Anymap (PBM, PGM, PPM)";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/x-ppm", "image/x-pgm", "image/x-pbm", "image/x-pnm", 
			"image/x-portable-pixmap", "image/x-portable-bitmap", "image/x-portable-graymap", 
			"image/x-portable-anymap"};
	}

	/**
	 * Returns the typical file extension (including leading dot) for an
	 * image type.
	 * Returns <code>null</code> for {@link #IMAGE_TYPE_UNKNOWN}.
	 * To get the image type given that you have a file name, use
	 * {@link #determineImageTypeFromFileName}.
	 *
	 * @param imageType the image type for which the extension is required
	 * @return the file extension or null
	 */
	public static String getTypicalFileExtension(int imageType)
	{
		if (imageType >= 0 && imageType < IMAGE_TYPE_FILE_EXTENSIONS.length)
		{
			return IMAGE_TYPE_FILE_EXTENSIONS[imageType];
		}
		else
		{
			return null;
		}
	}

	public boolean isLoadingSupported()
	{
		return true;
	}

	public boolean isSavingSupported()
	{
		return true;
	}

	/**
	 * Loads an image from a PNM input stream.
	 * It is assumed that a stream was given to this codec using {@link #setInputStream(InputStream)}.
	 *
	 * @return the image as an instance of a class that implements {@link IntegerImage}
	 * @throws InvalidFileStructureException if the input stream is not a valid PNM stream (or unsupported)
	 * @throws java.io.IOException if there were problems reading from the input stream
	 */
	private void load() throws 
		InvalidFileStructureException,
		IOException,
		MissingParameterException,
		UnsupportedTypeException,
		WrongFileFormatException,
		WrongParameterException
	{
		InputStream is = getInputStream();
		if (is != null)
		{
			if (is instanceof PushbackInputStream)
			{
				in = (PushbackInputStream)is;
			}
			else
			{
				in = new PushbackInputStream(is);
			}
		}
		else
		{
			throw new MissingParameterException("InputStream object required for loading.");
		}
		loadType();
		String resolutionLine = loadTextLine();
		setResolution(resolutionLine);
		setBoundsIfNecessary(width, height);
		if (imageType == IMAGE_TYPE_BILEVEL)
		{
			maxSample = 1;
		}
		else
		{
			// load maximum value
			String maxSampleLine = loadTextLine();
			setMaximumSample(maxSampleLine);
		}
		if (maxSample > 65535)
		{
			throw new UnsupportedTypeException("Cannot deal with samples larger than 65535.");
		}
		checkImageResolution();
		switch (imageType)
		{
			case(IMAGE_TYPE_BILEVEL):
			{
				loadBilevelImage();
				break;
			}
			case(IMAGE_TYPE_COLOR):
			{
				loadColorImage();
				break;
			}
			case(IMAGE_TYPE_GRAY):
			{
				loadGrayImage();
				break;
			}
			default:
			{
				throw new UnsupportedTypeException("Cannot deal with image type.");
			}
		}
	}

	private int loadAsciiNumber() throws
		InvalidFileStructureException,
		IOException
	{
		boolean hasDigit = false;
		int result = -1;
		do
		{
			int b = in.read();
			if (b >= 48 && b <= 57)
			{
				// decimal digit
				if (hasDigit)
				{
					result = result * 10 + (b - 48);
				}
				else
				{
					hasDigit = true;
					result = b - 48;
				}
			}
			else
			if (b == 32 || b == 10 || b == 13 || b == 9)
			{
				// whitespace
				if (hasDigit)
				{
					if (result > maxSample)
					{
						throw new InvalidFileStructureException("Read number " +
							"from PNM stream that is larger than allowed " +
							"maximum sample value " + maxSample + " (" + result + ").");
					}
					return result;
				}
				// ignore whitespace
			}
			else
			if (b == 35) 
			{
				// the # character, indicating a comment row
				if (hasDigit)
				{
					in.unread(b);
					if (result > maxSample)
					{
						throw new InvalidFileStructureException("Read " +
							"number from PNM stream that is larger than " +
							"allowed maximum sample value " + maxSample + 
							" (" + result + ").");
					}
					return result;
				}
				do
				{
					b = in.read();
				}
				while (b != -1 && b != 10 && b != 13);
				if (b == 13)
				{
				}
				// put it into the comment list
			}
			else
			if (b == -1)
			{
				// the end of file character
				if (hasDigit)
				{
					if (result > maxSample)
					{
						throw new InvalidFileStructureException("Read number from PNM stream that is larger than allowed maximum sample value " +
							maxSample + " (" + result + ")");
					}
					return result;
				}
				throw new InvalidFileStructureException("Unexpected end of file while reading ASCII number from PNM stream.");
			}
			else
			{
				throw new InvalidFileStructureException("Read invalid character from PNM stream: " + b +
					" dec.");
			}
		}
		while(true);
	}

	private void loadBilevelImage() throws
		InvalidFileStructureException,
		IOException,
		WrongParameterException
	{
		PixelImage image = getImage();
		if (image == null)
		{
			setImage(new MemoryBilevelImage(getBoundsWidth(), getBoundsHeight()));
		}
		else
		{
			if (!(image instanceof BilevelImage))
			{
				throw new WrongParameterException("Specified input image must implement BilevelImage for this image type.");
			}
		}
		if (getAscii().booleanValue())
		{
			loadBilevelImageAscii();
		}
		else
		{
			loadBilevelImageBinary();
		}
	}

	private void loadBilevelImageAscii() throws
		InvalidFileStructureException,
		IOException
	{
		BilevelImage image = (BilevelImage)getImage();
		// skip the pixels of the first getBoundsY1() rows
		int pixelsToSkip = width * getBoundsY1();
		for (int i = 0; i < pixelsToSkip; i++)
		{
			loadAsciiNumber();
		}
		final int NUM_ROWS = getBoundsHeight();
		final int COLUMNS = getBoundsWidth();
		final int X1 = getBoundsX1();
		int[] row = new int[width];
		// now read and store getBoundsHeight() rows
		for (int y = 0; y < NUM_ROWS; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int value = loadAsciiNumber();
				if (value == 0)
				{
					row[x] = BilevelImage.WHITE;
				}
				else
				if (value == 1)
				{
					row[x] = BilevelImage.BLACK;
				}
				else
				{
					throw new InvalidFileStructureException("Loaded " +
						"number for position x=" + x + ", y=" + (y + getBoundsY1()) + 
						" is neither 0 nor 1 in PBM stream: " + value);
				}
			}
			image.putSamples(0, 0, y, COLUMNS, 1, row, X1);
			setProgress(y, NUM_ROWS);
		}
	}

	private void loadBilevelImageBinary() throws
		InvalidFileStructureException,
		IOException
	{
		BilevelImage image = (BilevelImage)getImage();
		int bytesPerRow = (width + 7) / 8;
		// skip the first getBoundsY1() rows
		long bytesToSkip = (long)getBoundsY1() * (long)bytesPerRow;
		// Note:
		// removed in.skip(bytesToSkip) because that was only available in Java 1.2
		// instead the following while loop is used
		while (bytesToSkip-- > 0)
		{
			in.read();
		}
		// allocate buffer large enough for a complete row
		byte[] row = new byte[bytesPerRow];
		final int numRows = getBoundsHeight();
		// read and store the next getBoundsHeight() rows
		for (int y = 0; y < numRows; y++)
		{
			// read bytesPerRow bytes into row
			int bytesToRead = bytesPerRow;
			int index = 0;
			while (bytesToRead > 0)
			{
				int result = in.read(row, index, bytesToRead);
				if (result >= 0)
				{
					index += result;
					bytesToRead -= result;
				}
				else
				{
					throw new InvalidFileStructureException("Unexpected end of input stream while reading.");
				}
			}
			// invert values
			for (int x = 0; x < row.length; x++)
			{
				row[x] = (byte)~row[x];
			}
			//image.putPackedBytes(0, y, bytesPerRow, buffer, 0);
			if (isRowRequired(y))
			{
				image.putPackedBytes(0, y - getBoundsY1(), getBoundsWidth(), row, getBoundsX1() >> 3, getBoundsX1() & 7);
			}
			setProgress(y, numRows);
		}
	}

	/**
	 * Read a 16-bit binary value in network (big-endian) order.
	 * @return An integer between 0 and 65535, or -1 for EOF.
	 * @throws IOException If the underlying read operation failed. 
	 */
	private int read16BitBinaryValue() throws IOException {
		int byte1 = in.read();
		if (byte1 < 0)
			return -1;
		int byte2 = in.read();
		if (byte2 < 0)
			return -1;
		return (byte1 << 8) | byte2;
	}

	private void loadColorImage() throws InvalidFileStructureException, IOException
	{
		RGBIntegerImage image = null;
		RGB24Image image24 = null;
		RGB48Image image48 = null;
		if (maxSample <= 255)
		{
			image24 = new MemoryRGB24Image(width, height);
			image = image24;
			setImage(image);
		}
		else
		{
			image48 = new MemoryRGB48Image(width, height);
			image = image48;
			setImage(image);
		}
		for (int y = 0, destY = - getBoundsY1(); y < height; y++, destY++)
		{
			if (getAscii().booleanValue())
			{
				for (int x = 0; x < width; x++)
				{
					int red = loadAsciiNumber();
					if (red < 0 || red > maxSample)
					{
						throw new InvalidFileStructureException("Invalid " +
							"sample value " + red + " for red sample at " +
							"(x=" + x + ", y=" + y + ").");
					}
					image.putSample(RGBIndex.INDEX_RED, x, y, red);

					int green = loadAsciiNumber();
					if (green < 0 || green > maxSample)
					{
						throw new InvalidFileStructureException("Invalid " +
							"sample value " + green + " for green sample at " +
							"(x=" + x + ", y=" + y + ").");
					}
					image.putSample(RGBIndex.INDEX_GREEN, x, y, green);

					int blue = loadAsciiNumber();
					if (blue < 0 || blue > maxSample)
					{
						throw new InvalidFileStructureException("Invalid " +
							"sample value " + blue + " for blue sample at " +
							"(x=" + x + ", y=" + y + ").");
					}
					image.putSample(RGBIndex.INDEX_BLUE, x, y, blue);
				}
			}
			else
			{
				if (image24 != null)
				{
					for (int x = 0; x < width; x++)
					{
						int red = in.read();
						if (red == -1)
						{
							throw new InvalidFileStructureException("Unexpected " +
								"end of file while reading red sample for pixel " +
								"x=" + x + ", y=" + y + ".");
						}
						image24.putByteSample(RGBIndex.INDEX_RED, x, y, (byte)(red & 0xff));
						int green = in.read();
						if (green == -1)
						{
							throw new InvalidFileStructureException("Unexpected " +
								"end of file while reading green sample for pixel " +
								"x=" + x + ", y=" + y + ".");
						}
						image24.putByteSample(RGBIndex.INDEX_GREEN, x, y, (byte)(green & 0xff));
						int blue = in.read();
						if (blue == -1)
						{
							throw new InvalidFileStructureException("Unexpected " +
								"end of file while reading blue sample for pixel " +
								"x=" + x + ", y=" + y + ".");
						}
						image24.putByteSample(RGBIndex.INDEX_BLUE, x, y, (byte)(blue & 0xff));
					}
				}
				else if (image48 != null)
				{
					for (int x = 0; x < width; x++)
					{
						int red = read16BitBinaryValue();
						if (red == -1)
						{
							throw new InvalidFileStructureException("Unexpected " +
								"end of file while reading red sample for pixel " +
								"x=" + x + ", y=" + y + ".");
						}
						image48.putShortSample(RGBIndex.INDEX_RED, x, y, (short)(red & 0xffff));
						int green = read16BitBinaryValue();
						if (green == -1)
						{
							throw new InvalidFileStructureException("Unexpected " +
								"end of file while reading green sample for pixel " +
								"x=" + x + ", y=" + y + ".");
						}
						image48.putShortSample(RGBIndex.INDEX_GREEN, x, y, (short)(green & 0xffff));
						int blue = read16BitBinaryValue();
						if (blue == -1)
						{
							throw new InvalidFileStructureException("Unexpected " +
								"end of file while reading blue sample for pixel " +
								"x=" + x + ", y=" + y + ".");
						}
						image48.putShortSample(RGBIndex.INDEX_BLUE, x, y, (short)(blue & 0xffff));
					}
				}
			}
			setProgress(y, getBoundsHeight());
		}
	}

	private void loadGrayImage() throws InvalidFileStructureException, IOException, UnsupportedTypeException
	{
		final int WIDTH = getBoundsWidth();
		final int HEIGHT = getBoundsHeight();
		PixelImage pimage = getImage();
		if (pimage == null)
		{
			if (maxSample < 256)
			{
				pimage = new MemoryGray8Image(WIDTH, HEIGHT);
			}
			else
			if (maxSample < 65536)
			{
				pimage = new MemoryGray16Image(WIDTH, HEIGHT);
			}
			else
			{
				throw new UnsupportedTypeException("Gray images with more than 16 bits per pixel are not supported.");
			}
			setImage(pimage);
		}
		else
		{
		}
		GrayIntegerImage image = (GrayIntegerImage)pimage;
		int[] buffer = new int[width];
		for (int y = 0, destY = -getBoundsY1(); destY < getBoundsHeight(); y++, destY++)
		{
			if (getAscii().booleanValue())
			{
				for (int x = 0; x < width; x++)
				{
					buffer[x] = loadAsciiNumber();
				}
			}
			else
			{
				if (maxSample < 256)
				{
					for (int x = 0; x < width; x++)
					{
						buffer[x] = in.read();
					}
				}
				else
				{
					for (int x = 0; x < width; x++)
					{
						int msb = in.read();
						int lsb = in.read();
						buffer[x] = (msb << 8) | lsb;
					}
				}
			}
			if (destY >= 0 && destY < getBoundsHeight())
			{
				image.putSamples(0, 0, destY, getBoundsWidth(), 1, buffer, getBoundsX1());
			}
			setProgress(y, getBoundsY2() + 1);
		}
	}

	private String loadTextLine() throws InvalidFileStructureException, IOException
	{
		// load text lines until
		// 1) a normal text line is found
		// 2) an error occurs
		// any comment lines starting with # are added to the
		// comments Vector
		boolean isComment;
		StringBuffer sb;
		do
		{
			sb = new StringBuffer();
			int b;
			boolean crOrLf;
			do
			{
				b = in.read();
				if (b == -1)
				{
					throw new InvalidFileStructureException("Unexpected end of file in PNM stream.");
				}
				crOrLf = (b == 0x0a || b == 0x0d);
				if (!crOrLf)
				{
					sb.append((char)b);
				}
			}
			while (!crOrLf);
			if (b == 0x0d)
			{
				b = in.read();
				if (b != 0x0a)
				{
					throw new InvalidFileStructureException("Unexpected end of file in PNM stream.");
				}
			}
			isComment = (sb.length() > 0 && sb.charAt(0) == '#');
			if (isComment)
			{
				//sb.deleteCharAt(0);
				//sb.delete(0, 1);
				StringBuffer result = new StringBuffer(sb.length() - 1);
				int i = 1;
				while (i < sb.length())
				{
					result.append(sb.charAt(i++));
				}
				appendComment(result.toString());
			}
		}
		while (isComment);
		return sb.toString();
	}

	/**
	 * Loads the first two characters (which are expected to be a capital P
	 * followed by a decimal digit between 1 and 6, inclusively) and skips
	 * following LF and CR characters.
	 * This method not only checks the two bytes, it also initializes internal fields
	 * for storage mode (ASCII or binary) and image type.
	 *
	 * @throws WrongFileFormatException if the input stream is not a PNM stream
	 * @throws InvalidFileStructureException if the format that
	 *  is described above was not encountered
	 * @throws java.io.IOException if there were errors reading data
	 * @throws java.lang.IllegalArgumentException if the input stream was not given to this codec
	 */
	private void loadType() throws InvalidFileStructureException, IOException, WrongFileFormatException 
	{
		// read two bytes
		int v1 = in.read();
		int v2 = in.read();
		// check if first byte is P
		if (v1 != 0x50)
		{
			throw new WrongFileFormatException("Not a PNM stream. First byte " +
				"in PNM stream is expected to be 0x50 ('P'); found: " +
				v1 + " (dec).");
		}
		// check if second byte is ASCII of digit from 1 to 6
		if (v2 < 0x31 || v2 > 0x36)
		{
			throw new WrongFileFormatException("Not a PNM stream. Second byte " +
				"in PNM stream is expected to be the ASCII value of decimal " +
				"digit between 1 and 6 (49 dec to 54 dec); found " +
				v2 + " dec.");
		}
		// determine mode (ASCII or binary) from second byte
		ascii = new Boolean(v2 < 0x34);
		// determine image type from second byte
		v2 = v2 - 0x30;
		imageType = (v2 - 1) % 3;
		// skip LF and CR
		int b;
		do
		{
			b = in.read();
		}
		while (b == 0x0a || b == 0x0d || b == ' ');
		if (b == -1)
		{
			throw new InvalidFileStructureException("Read type (" +
				v2 + "). Unexpected end of file in input PNM stream.");
		}
		in.unread(b);
	}

	public void process() throws
		MissingParameterException,
		OperationFailedException
	{
		initModeFromIOObjects();
		try
		{
			if (getMode() == CodecMode.LOAD)
			{
				load();
			}
			else
			{
				save();
			}
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("I/O error: " + ioe.toString());
		}
	}

	private void save() throws
		IOException, 
		MissingParameterException,
		WrongParameterException
	{
		out = getOutputAsDataOutput();
		if (out == null)
		{
			throw new WrongParameterException("Cannot get a DataOutput object to use for saving.");
		}
		PixelImage pi = getImage();
		if (pi == null)
		{
			throw new MissingParameterException("Input image missing.");
		}
		if (!(pi instanceof IntegerImage))
		{
			throw new WrongParameterException("Input image must implement IntegerImage.");
		}
		IntegerImage image = (IntegerImage)pi;
		width = image.getWidth();
		height = image.getHeight();
		setBoundsIfNecessary(width, height);
		if (image instanceof RGB24Image)
		{
			imageType = IMAGE_TYPE_COLOR;
			maxSample = 255;
			save((RGB24Image)image);
		}
		else
		if (image instanceof RGB48Image)
		{
			imageType = IMAGE_TYPE_COLOR;
			maxSample = 65535;
			save((RGB48Image)image);
		}
		else
		if (image instanceof BilevelImage)
		{
			imageType = IMAGE_TYPE_BILEVEL;
			maxSample = 1;
			save((BilevelImage)image);
		}
		else
		if (image instanceof Gray8Image)
		{
			imageType = IMAGE_TYPE_GRAY;
			maxSample = 255;
			save((Gray8Image)image);
		}
		else
		if (image instanceof Gray16Image)
		{
			imageType = IMAGE_TYPE_GRAY;
			maxSample = 65535;
			save((Gray16Image)image);
		}
		else
		{
			throw new WrongParameterException("Unsupported input image type: " +
				image.getClass().getName());
		}
		close();
	}

	private void save(BilevelImage image) throws IOException
	{
		saveHeader();
		final int WIDTH = getBoundsWidth();
		final int HEIGHT = getBoundsHeight();
		final int BYTES_PER_ROW = (WIDTH + 7) / 8;
		byte[] buffer = new byte[BYTES_PER_ROW];
		for (int y = 0, srcY = getBoundsY1(); y < HEIGHT; y++, srcY++)
		{
			if (getAscii().booleanValue())
			{
				for (int x = 0, srcX = getBoundsX1(); x < WIDTH; x++, srcX++)
				{
					if (image.isBlack(srcX, srcY))
					{
						out.write(49); // 1
					}
					else
					{
						out.write(48); // 0
					}
					columns ++;
					if (columns > 70)
					{
						columns = 0;
						out.write(10);
					}
					else
					{
						out.write(32);
						columns++;
					}
				}
			}
			else
			{
				image.getPackedBytes(getBoundsX1(), srcY, WIDTH, buffer, 0, 0);
				for (int x = 0; x < buffer.length; x++)
				{
					buffer[x] = (byte)(~buffer[x]);
				}
				out.write(buffer);
			}
			setProgress(y, HEIGHT);
		}
	}

	private void save(Gray8Image image) throws IOException
	{
		saveHeader();
		final int HEIGHT = getBoundsHeight();
		final int WIDTH = getBoundsWidth();
		final int X1 = getBoundsX1();
		System.out.println(WIDTH + " " + HEIGHT + " " + X1);
		byte[] buffer = new byte[WIDTH];
		for (int y = 0, srcY = getBoundsY1(); y < HEIGHT; y++, srcY++)
		{
			image.getByteSamples(0, X1, srcY, WIDTH, 1, buffer, 0);
			if (getAscii().booleanValue())
			{
				for (int x = 0; x < WIDTH; x++)
				{
					saveAsciiNumber(buffer[x] & 0xff);
					out.write(32);
					columns += 2;
					if (columns > 70)
					{
						columns = 0;
						out.write(10);
					}
					else
					{
						out.write(32);
						columns++;
					}
				}
			}
			else
			{
				out.write(buffer);
			}
			setProgress(y, HEIGHT);
		}
	}

	private void save(Gray16Image image) throws IOException
	{
		saveHeader();
		final int HEIGHT = getBoundsHeight();
		final int WIDTH = getBoundsWidth();
		final int X1 = getBoundsX1();
		short[] buffer = new short[WIDTH];
		for (int y = 0, srcY = getBoundsY1(); y < HEIGHT; y++, srcY++)
		{
			image.getShortSamples(0, X1, srcY, WIDTH, 1, buffer, 0);
			if (getAscii().booleanValue())
			{
				for (int x = 0; x < WIDTH; x++)
				{
					saveAsciiNumber(buffer[x] & 0xffff);
					out.write(32);
					columns += 4;
					if (columns > 70)
					{
						columns = 0;
						out.write(10);
					}
					else
					{
						out.write(32);
						columns++;
					}
				}
			}
			else
			{
				for (int x = 0; x < WIDTH; x++)
				{
					int sample = buffer[x] & 0xffff;
					out.write((sample >> 8) & 0xff);
					out.write(sample & 0xff);
				}
			}
			setProgress(y, HEIGHT);
		}
	}

	private void save(RGB24Image image) throws IOException
	{
		saveHeader();
		final int WIDTH = getBoundsWidth();
		final int HEIGHT = getBoundsHeight();
		for (int y = 0, srcY = getBoundsY1(); y < HEIGHT; y++, srcY++)
		{
			if (getAscii().booleanValue())
			{
				for (int x = 0, srcX = getBoundsX1(); x < WIDTH; x++, srcX++)
				{
					int red = image.getSample(RGBIndex.INDEX_RED, srcX, srcY);
					int green = image.getSample(RGBIndex.INDEX_GREEN, srcX, srcY);
					int blue = image.getSample(RGBIndex.INDEX_BLUE, srcX, srcY);
					saveAsciiNumber(red);
					out.write(32);
					saveAsciiNumber(green);
					out.write(32);
					saveAsciiNumber(blue);
					columns += 11;
					if (columns > 80)
					{
						columns = 0;
						out.write(10);
					}
					else
					{
						out.write(32);
						columns++;
					}
				}
			}
			else
			{
				for (int x = 0, srcX = getBoundsX1(); x < WIDTH; x++, srcX++)
				{
					out.write(image.getSample(RGBIndex.INDEX_RED, srcX, srcY));
					out.write(image.getSample(RGBIndex.INDEX_GREEN, srcX, srcY));
					out.write(image.getSample(RGBIndex.INDEX_BLUE, srcX, srcY));
				}
			}
			setProgress(y, HEIGHT);
		}
	}

	private void save(RGB48Image image) throws IOException
	{
		saveHeader();
		final int WIDTH = getBoundsWidth();
		final int HEIGHT = getBoundsHeight();
		for (int y = 0, srcY = getBoundsY1(); y < HEIGHT; y++, srcY++)
		{
			if (getAscii().booleanValue())
			{
				for (int x = 0, srcX = getBoundsX1(); x < WIDTH; x++, srcX++)
				{
					int red = image.getSample(RGBIndex.INDEX_RED, srcX, srcY);
					int green = image.getSample(RGBIndex.INDEX_GREEN, srcX, srcY);
					int blue = image.getSample(RGBIndex.INDEX_BLUE, srcX, srcY);
					saveAsciiNumber(red);
					out.write(32);
					saveAsciiNumber(green);
					out.write(32);
					saveAsciiNumber(blue);
					columns += 13;
					if (columns > 80)
					{
						columns = 0;
						out.write(10);
					}
					else
					{
						out.write(32);
						columns++;
					}
				}
			}
			else
			{
				/*
				for (int x = 0, srcX = getBoundsX1(); x < WIDTH; x++, srcX++)
				{
					out.write(image.getSample(RGBIndex.INDEX_RED, srcX, srcY));
					out.write(image.getSample(RGBIndex.INDEX_GREEN, srcX, srcY));
					out.write(image.getSample(RGBIndex.INDEX_BLUE, srcX, srcY));
				}
				*/
			}
			setProgress(y, HEIGHT);
		}
	}

	private void saveAsciiNumber(int number) throws
		IOException
	{
		String s = Integer.toString(number);
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			out.write(c);
		}
		columns += s.length();
	}

	private void saveHeader() throws IOException
	{
		out.write(80); // 'P'
		int pnmType = 49 + imageType;
		if (getAscii() == null)
		{
			setAscii(maxSample > 255);
		}
		if (!getAscii().booleanValue())
		{
			pnmType += 3;
		}
		out.write(pnmType); // '1' .. '6'
		out.write(10); // line feed
		saveAsciiNumber(getBoundsWidth());
		out.write(32); // space
		saveAsciiNumber(getBoundsHeight());
		out.write(10); // line feed
		if (imageType != IMAGE_TYPE_BILEVEL)
		{
			// bilevel max sample is always 1 and MUST NOT be saved
			saveAsciiNumber(maxSample);
			out.write(10);// line feed
		}
	}

	/**
	 * Specify whether ASCII mode is to be used when saving an image.
	 * Default is binary mode.
	 * @param asciiMode if true, ASCII mode is used, binary mode otherwise
	 */
	public void setAscii(boolean asciiMode)
	{
		ascii = new Boolean(asciiMode);
	}

	private void setMaximumSample(String line) throws InvalidFileStructureException
	{
		line = line.trim();
		try
		{
			maxSample = Integer.parseInt(line);
		}
		catch (NumberFormatException nfe)
		{
			throw new InvalidFileStructureException("Not a valid value for the maximum sample: " + line);
		}
		if (maxSample < 0)
		{
			throw new InvalidFileStructureException("The value for the maximum sample must not be negative; found " + maxSample);
		}
	}

	/*
	 * Reads resolution from argument String and sets private variables
	 * width and height.
	 */
	private void setResolution(String line) throws InvalidFileStructureException
	{
		line = line.trim();
		StringTokenizer st = new StringTokenizer(line, " ");
		try
		{
			if (!st.hasMoreTokens())
			{
				throw new InvalidFileStructureException("No width value found in line \"" +
					line + "\".");
			}
			String number = st.nextToken();
			try
			{
				width = Integer.parseInt(number);
			}
			catch (NumberFormatException nfe)
			{
				throw new InvalidFileStructureException("Not a valid int value for width: " +
					number);
			}
			if (width < 1)
			{
				throw new InvalidFileStructureException("The width value must be larger than " +
					"zero; found " + width + ".");
			}
			if (!st.hasMoreTokens())
			{
				throw new InvalidFileStructureException("No height value found in line \"" +
					line + "\".");
			}
			number = st.nextToken();
			try
			{
				height = Integer.parseInt(number);
			}
			catch (NumberFormatException nfe)
			{
				throw new InvalidFileStructureException("Not a valid int value for height: " +
					number);
			}
			if (height < 1)
			{
				throw new InvalidFileStructureException("The height value must be larger than " +
					"zero; found " + width + ".");
			}
		}
		catch (NoSuchElementException nsee)
		{
			// should not happen because we always check if there is a token
		}
	}

	public String suggestFileExtension(PixelImage image)
	{
		if (image == null)
		{
			return null;
		}
		if (image instanceof BilevelImage)
		{
			return IMAGE_TYPE_FILE_EXTENSIONS[IMAGE_TYPE_BILEVEL];
		}
		else
		if (image instanceof GrayImage)
		{
			return IMAGE_TYPE_FILE_EXTENSIONS[IMAGE_TYPE_GRAY];
		}
		else
		if (image instanceof RGB24Image)
		{
			return IMAGE_TYPE_FILE_EXTENSIONS[IMAGE_TYPE_COLOR];
		}
		return null;
	}
}
