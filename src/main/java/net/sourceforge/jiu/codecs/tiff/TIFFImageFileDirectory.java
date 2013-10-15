/*
 * TIFFImageFileDirectory
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.tiff.TIFFConstants;
import net.sourceforge.jiu.codecs.tiff.TIFFTag;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.RGBIndex;

/**
 * This class encapsulates all data of a TIFF image file directory (IFD).
 * @author Marco Schmidt
 */
public class TIFFImageFileDirectory implements TIFFConstants
{
	public static final int TYPE_BILEVEL_PACKED = 0;
	public static final int TYPE_GRAY4 = 1;
	public static final int TYPE_GRAY8 = 2;
	public static final int TYPE_GRAY16 = 3;
	public static final int TYPE_PALETTED4 = 4;
	public static final int TYPE_PALETTED8 = 5;
	public static final int TYPE_RGB24_INTERLEAVED = 6;
	public static final int TYPE_RGB48_INTERLEAVED = 7;
	public static final int TYPE_BILEVEL_BYTE = 8;
	public static final int TYPE_CMYK32_INTERLEAVED = 9;
	public static final int TYPE_CMYK32_PLANAR = 10;
	public static final int TYPE_LOGLUV32_INTERLEAVED = 11;
	public static final int TYPE_LOGL = 12;

	private String artist;
	private int[] bitsPerSample;
	private int bitsPerPixel;
	private int bitsPerRow;
	private int bytesBetweenSamples;
	private int[] bytesPerSample;
	private int bytesPerRow;
	private int compression;
	private String copyright;
	private Date date;
	private String dateTime;
	private int dpiX;
	private int dpiY;
	private int[] extraSamples;
	private int height;
	private int horizontalTiles;
	private String hostComputer;
	private String imageDescription;
	private int imageType;
	private boolean invertGraySamples;
	private String make;
	private String model;
	private int numStrips;
	private int numTiles;
	private int orientation;
	private Palette palette;
	private int pixelsPerRow;
	private int planarConfiguration;
	private int photometricInterpretation;
	private int predictor;
	private int[] sampleTypes;
	private int resolutionUnit;
	private double resolutionX;
	private double resolutionY;
	private int rowsPerStrip;
	private int samplesPerPixel;
	private String software;
	private Vector stripByteCounts;
	private Vector stripOffsets;
	private int t4Options;
	private int t6Options;
	private Vector tags;
	private Vector tileByteCounts;
	private Vector tileOffsets;
	private TimeZone timeZone;
	private int tileWidth;
	private int tileHeight;
	private int verticalTiles;
	private int width;

	/**
	 * Initializes all members to null or -1 and creates an internal list for
	 * the tags that will be make up this directory.
	 */
	public TIFFImageFileDirectory()
	{
		initMembers();
		tags = new Vector();
	}

	/**
	 * Adds a tag to the end of the internal list of tags.
	 * @param tag the TIFFTag instance to be appended
	 */
	public void append(TIFFTag tag)
	{
		tags.addElement(tag);
	}

	private void checkContent() throws
		InvalidFileStructureException,
		UnsupportedTypeException
	{
		if (width < 1)
		{
			throw new InvalidFileStructureException("No valid width available.");
		}
		if (height < 1)
		{
			throw new InvalidFileStructureException("No valid width available.");
		}
		if (stripOffsets != null)
		{
			pixelsPerRow = width;
		}
		else
		if (tileOffsets != null)
		{
			pixelsPerRow = tileWidth;
		}
		if (rowsPerStrip == -1 && stripOffsets != null && stripOffsets.size() == 1)
		{
			rowsPerStrip = height;
		}
		// do more checks based on color type
		switch (photometricInterpretation)
		{
			case(PHOTOMETRIC_BLACK_IS_ZERO):
			case(PHOTOMETRIC_WHITE_IS_ZERO):
			{
				if (bitsPerSample[0] == 1)
				{
					imageType = TYPE_BILEVEL_PACKED;
				}
				else
				{
					if (bitsPerSample[0] == 4)
					{
						imageType = TYPE_GRAY4;
					}
					else
					if (bitsPerSample[0] == 8)
					{
						imageType = TYPE_GRAY8;
					}
					else
					{
						throw new UnsupportedTypeException("Only bit depths 1, 4 and 8 are supported for bilevel and grayscale images.");
					}
				}
				break;
			}
			case(PHOTOMETRIC_PALETTED):
			{
				if (getPalette() == null)
				{
					throw new InvalidFileStructureException("No palette found in paletted image.");
				}
				break;
			}
			case(PHOTOMETRIC_TRUECOLOR_RGB):
			{
				if (planarConfiguration != PLANAR_CONFIGURATION_CHUNKY)
				{
					throw new UnsupportedTypeException("Cannot handle planar configuration other than chunky for RGB images.");
				}
				if (bitsPerSample.length != 3)
				{
					throw new UnsupportedTypeException("Found RGB truecolor image, but instead of three " + bitsPerSample.length + " component(s).");
				}
				if (bitsPerPixel == 24)
				{
					imageType = TYPE_RGB24_INTERLEAVED;
				}
				else
				if (bitsPerPixel == 48)
				{
					imageType = TYPE_RGB48_INTERLEAVED;
				}
				else
				{
					throw new UnsupportedTypeException("Unsupported RGB truecolor image color depth: " + bitsPerPixel + ".");
				}
				break;
			}
			case(PHOTOMETRIC_TRUECOLOR_LOGLUV):
			{
				if (planarConfiguration == PLANAR_CONFIGURATION_CHUNKY)
				{
					imageType = TYPE_LOGLUV32_INTERLEAVED;
				}
				else
				{
					throw new UnsupportedTypeException("Cannot handle planar configuration other than chunky for RGB images.");
				}
				break;
			}
			case(PHOTOMETRIC_LOGL):
			{
				imageType = TYPE_LOGL;
				break;
			}
			case(PHOTOMETRIC_TRUECOLOR_CMYK):
			{
				if (planarConfiguration == PLANAR_CONFIGURATION_CHUNKY)
				{
					imageType = TYPE_CMYK32_INTERLEAVED;
				}
				/*else
				if (planarConfiguration == PLANAR_CONFIGURATION_PLANAR)
				{
					imageType = TYPE_CMYK32_PLANAR;
				}*/
				else
				{
					throw new UnsupportedTypeException("Cannot handle planar configuration other than chunky for CMYK images.");
				}
				break;
			}
			default:
			{
				throw new UnsupportedTypeException("Unsupported color type: "  + photometricInterpretation + ".");
			}
		}
		if (compression == COMPRESSION_CCITT_GROUP3_1D_MODIFIED_HUFFMAN)
		{
			if (bitsPerPixel != 1)
			{
				throw new UnsupportedTypeException("Number of bits per pixel must be 1 for " +
					"compression type: " + getCompressionName(compression) + ".");
			}
			imageType = TYPE_BILEVEL_BYTE;
		}
		// TODO more validity checks
	}

	/**
	 * TODO: regard extra samples
	 */
	public int computeNumBytes(int numPixels)
	{
		if (bitsPerPixel == 1)
		{
			if (imageType == TYPE_BILEVEL_BYTE)
			{
				return numPixels;
			}
			else
			{
				return (numPixels + 7) / 8;
			}
		}
		else
		if (bitsPerPixel <= 4)
		{
			return (numPixels + 1) / 2;
		}
		else
		if (bitsPerPixel <= 8)
		{
			return numPixels;
		}
		else
		if (bitsPerPixel == 16)
		{
			return numPixels * 2;
		}
		else
		if (bitsPerPixel == 24)
		{
			return numPixels * 3;
		}
		else
		if (bitsPerPixel == 32)
		{
			return numPixels * 4;
		}
		else
		if (bitsPerPixel == 48)
		{
			return numPixels * 6;
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Returns information on the person who created the image
	 * (as stored in tag {@link TIFFConstants#TAG_ARTIST}).
	 */
	public String getArtist()
	{
		return artist;
	}

	/**
	 * Returns the number of bits per pixel (not including transparency information).
	 */
	public int getBitsPerPixel()
	{
		return bitsPerPixel;
	}

	/**
	 * Returns the number of compressed byte for a given tile.
	 * Tile index must not be negative and must be smaller than the number of tiles.
	 * @param tileIndex zero-based index of tile or strip for which the number of compressed bytes is to be returned
	 */
	public int getByteCount(int tileIndex)
	{
		if (stripByteCounts != null)
		{
			return ((Number)stripByteCounts.elementAt(tileIndex)).intValue();
		}
		else
		if (tileByteCounts != null)
		{
			return ((Number)tileByteCounts.elementAt(tileIndex)).intValue();
		}
		else
		{
			return 0;
		}
	}

	public int getBytesPerRow()
	{
		return computeNumBytes(getTileWidth());
	}

	/**
	 * Returns the compression method, encoded as a number as found in
	 * {@link TIFFConstants} (more specifically, the COMPRESSION_xyz constants).
	 * Use {@link #getCompressionName(int)} to get the English name
	 * of this compression method.
	 * @return compression method 
	 */
	public int getCompression()
	{
		return compression;
	}

	/**
	 * Returns the name of a TIFF compression method.
	 * If the name is unknown, <em>Unknown method</em> plus
	 * the method number is returned.
	 * This static method can be used in combination with the value from
	 * {@link #getCompression}.
	 * @param method the compression method number
	 * @return the compression method name
	 */
	public static String getCompressionName(int method)
	{
		switch(method)
		{
			case(COMPRESSION_CCITT_GROUP3_1D_MODIFIED_HUFFMAN): return "CCITT Group 3 1D Modified Huffman";
			case(COMPRESSION_CCITT_T4): return "CCITT T.4";
			case(COMPRESSION_CCITT_T6): return "CCITT T.6";
			case(COMPRESSION_DEFLATED_INOFFICIAL): return "Deflated (inofficial, 32496)";
			case(COMPRESSION_DEFLATED_OFFICIAL): return "Deflated (official, 8)";
			case(COMPRESSION_LZW): return "LZW";
			case(COMPRESSION_NONE): return "Uncompressed";
			case(COMPRESSION_PACKBITS): return "Packbits";
			case(6): return "JPEG (old style)";
			case(7):return "JPEG (new style)";
			case(103): return "Pegasus IMJ";
			case(32766): return "NeXT 2-bit RLE";
			case(32771): return "Uncompressed, word-aligned";
			case(32809): return "Thunderscan RLE";
			case(32895): return "IT8 CT with padding";
			case(32896): return "IT8 Linework RLE";
			case(32897): return "IT8 Monochrome picture";
			case(32898): return "IT8 Binary line art";
			case(32908): return "Pixar 10 bit LZW";
			case(32909): return "Pixar 11 bit ZIP";
			case(32947): return "Kodak DCS";
			case(34661): return "ISO JBIG";
			case(34676): return "SGI Log Luminance RLE";
			case(34677): return "SGI Log 24-bit packed";
			default: return "Unknown method (" + method + ")";
		}
	}

	public String getCopyright()
	{
		return copyright;
	}

	/**
	 * If a date / time tag was found in this image file directory and
	 * {@link #initFromTags} was called already, it was attempted to
	 * create a {@link java.util.Date} object from it.
	 * This object (or <code>null</code>) is returned.
	 * Use {@link #setTimeZone} to provide a time zone before the date 
	 * parsing is done.
	 * @see #getDateTimeString
	 */
	public Date getDateTime()
	{
		return date;
	}

	/**
	 * If there was a date / time tag in this IFD, its String value
	 * is returned.
	 * @see #getDateTime
	 */
	public String getDateTimeString()
	{
		return dateTime;
	}

	public int getDpiX()
	{
		return dpiX;
	}

	public int getDpiY()
	{
		return dpiY;
	}

	public int getHeight()
	{
		return height;
	}

	public String getHostComputer()
	{
		return hostComputer;
	}

	public String getImageDescription()
	{
		return imageDescription;
	}

	public int getImageType()
	{
		return imageType;
	}

	public String getModel()
	{
		return model;
	}

	public int getNumHorizontalTiles()
	{
		return horizontalTiles;
	}

	public int getNumStrips()
	{
		return numStrips;
	}

	public int getNumTiles()
	{
		return numTiles;
	}

	public int getNumVerticalTiles()
	{
		return verticalTiles;
	}

	public Palette getPalette()
	{
		return palette;
	}

	public int getPhotometricInterpretation()
	{
		return photometricInterpretation;
	}

	public int getPredictor()
	{
		return predictor;
	}

	public int getRowsPerStrip()
	{
		return rowsPerStrip;
	}

	public int getSamplesPerPixel()
	{
		return samplesPerPixel;
	}

	public String getSoftware()
	{
		return software;
	}

	public Vector getStripOffsets()
	{
		return stripOffsets;
	}

	public int getT4Options()
	{
		return t4Options;
	}

	public int getT6Options()
	{
		return t6Options;
	}

	public int getTileHeight()
	{
		return tileHeight;
	}

	public long getTileOffset(int tileIndex)
	{
		if (stripOffsets != null)
		{
			Number number = (Number)stripOffsets.elementAt(tileIndex);
			return number.longValue();
		}
		else
		if (tileOffsets != null)
		{
			Number number = (Number)tileOffsets.elementAt(tileIndex);
			return number.longValue();
		}
		else
		{
			throw new IllegalArgumentException("Tile index invalid: " + tileIndex);
		}
	}

	public int getTileWidth()
	{
		return tileWidth;
	}

	public int getTileX1(int tileIndex)
	{
		if (tileIndex < 0 || tileIndex >= getNumTiles())
		{
			throw new IllegalArgumentException("Not a valid tile index: "  + tileIndex);
		}
		else
		{
			return (tileIndex % getNumHorizontalTiles()) * getTileWidth();
		}
	}

	public int getTileX2(int tileIndex)
	{
		if (tileIndex < 0 || tileIndex >= getNumTiles())
		{
			throw new IllegalArgumentException("Not a valid tile index: "  + tileIndex);
		}
		else
		{
			return ((tileIndex % getNumHorizontalTiles()) + 1) * getTileWidth() - 1;
		}
	}

	public int getTileY1(int tileIndex)
	{
		if (tileIndex < 0 || tileIndex >= getNumTiles())
		{
			throw new IllegalArgumentException("Not a valid tile index: "  + tileIndex);
		}
		else
		{
			return (tileIndex % getNumVerticalTiles()) * getTileHeight();
		}
	}

	public int getTileY2(int tileIndex)
	{
		if (tileIndex < 0 || tileIndex >= getNumTiles())
		{
			throw new IllegalArgumentException("Not a valid tile index: "  + tileIndex);
		}
		else
		{
			int result = ((tileIndex % getNumVerticalTiles()) + 1) * getTileHeight() - 1;
			if (result >= height)
			{
				result = height - 1;
			}
			return result;
		}
	}

	public int getWidth()
	{
		return width;
	}

	public void initMembers()
	{
		bitsPerPixel = -1;
		bitsPerSample = null;
		compression = -1;
		height = -1;
		horizontalTiles = -1;
		invertGraySamples = false;
		numStrips = -1;
		numTiles = -1;
		orientation = 1;
		photometricInterpretation = -1;
		planarConfiguration = -1;
		resolutionUnit = 2;
		resolutionX = -1.0;
		resolutionY = -1.0;
		rowsPerStrip = -1;
		stripOffsets = null;
		tags = null;
		tileOffsets = null;
		tileWidth = -1;
		tileHeight = -1;
		verticalTiles = -1;
		width = -1;
	}

	public void initFromTags(boolean check) throws
		InvalidFileStructureException,
		UnsupportedTypeException
	{
		int index = 0;
		while (index < tags.size())
		{
			TIFFTag tag = (TIFFTag)tags.elementAt(index++);
			int id = tag.getId();
			int count = tag.getCount();
			int type = tag.getType();
			boolean isNotInt = !tag.isInt();
			switch(id)
			{
				case(TAG_ARTIST):
				{
					artist = tag.getString();
					break;
				}
				case(TAG_BITS_PER_SAMPLE):
				{
					if (isNotInt)
					{
						throw new InvalidFileStructureException("Bits per " +
							"sample value(s) must be byte/short/long; type=" +
							type);
					}
					if (count == 1)
					{
						bitsPerSample = new int[1];
						bitsPerSample[0] = tag.getOffset();
						bitsPerPixel = bitsPerSample[0];
					}
					else
					{
						bitsPerPixel = 0;
						bitsPerSample = new int[count];
						for (int i = 0; i < count; i++)
						{
							bitsPerSample[i] = tag.getElementAsInt(i);
							if (bitsPerSample[i] < 1)
							{
								throw new InvalidFileStructureException("Bits per " +
									"sample value #" + i + " is smaller than 1.");
							}
							bitsPerPixel += bitsPerSample[i];
						}
					}
					break;
				}
				case(TAG_COLOR_MAP):
				{
					if ((count % 3) != 0)
					{
						throw new InvalidFileStructureException("Number of palette entries must be divideable by three without rest; " + count);
					}
					if (count < 3 || count > 768)
					{
						throw new UnsupportedTypeException("Unsupported number of palette entries: " + count + ".");
					}
					if (type != TAG_TYPE_SHORT)
					{
						throw new UnsupportedTypeException("Unsupported number type for palette entries: " + type);
					}
					int numEntries = count / 3;
					palette = new Palette(numEntries, 255);
					int vectorIndex = 0;
					for (int paletteIndex = 0; paletteIndex < numEntries; paletteIndex++)
					{
						palette.putSample(RGBIndex.INDEX_RED, paletteIndex, tag.getElementAsInt(vectorIndex++) >> 8);
					}
					for (int paletteIndex = 0; paletteIndex < numEntries; paletteIndex++)
					{
						palette.putSample(RGBIndex.INDEX_GREEN, paletteIndex, tag.getElementAsInt(vectorIndex++) >> 8);
					}
					for (int paletteIndex = 0; paletteIndex < numEntries; paletteIndex++)
					{
						palette.putSample(RGBIndex.INDEX_BLUE, paletteIndex, tag.getElementAsInt(vectorIndex++) >> 8);
					}
					break;
				}
				case(TAG_COMPRESSION):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							" single byte/short/long value for compression " +
							"(count=" + count + ", type=" + type + ").");
					}
					compression = tag.getOffset();
					break;
				}
				case(TAG_DATE_TIME):
				{
					dateTime = tag.getString();
					if (dateTime != null)
					{
						dateTime = dateTime.trim();
					}
					if (dateTime != null)
					{
						SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
						if (timeZone != null)
						{
							format.setCalendar(new GregorianCalendar(timeZone));
						}
						try
						{
							date = format.parse(dateTime);
						}
						catch (ParseException pe)
						{
							date = null;
						}
					}
					break;
				}
				case(TAG_HOST_COMPUTER):
				{
					hostComputer = tag.getString();
					break;
				}
				case(TAG_IMAGE_DESCRIPTION):
				{
					imageDescription = tag.getString();
					break;
				}
				case(TAG_IMAGE_WIDTH):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for image width " +
							"(count=" + count + ", type=" + type + ").");
					}
					width = tag.getOffset();
					break;
				}
				case(TAG_IMAGE_LENGTH):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for image height " +
							"(count=" + count + ", type=" + type + ").");
					}
					height = tag.getOffset();
					break;
				}
				case(TAG_MAKE):
				{
					make = tag.getString();
					break;
				}
				case(TAG_MODEL):
				{
					model = tag.getString();
					break;
				}
				case(TAG_ORIENTATION):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for image height " +
							"(count=" + count + ", type=" + type + ").");
					}
					orientation = tag.getOffset();
					break;
				}
				case(TAG_PHOTOMETRIC_INTERPRETATION):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for photometric interpretation.");
					}
					photometricInterpretation = tag.getOffset();
					break;
				}
				case(TAG_RESOLUTION_UNIT):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for planar configuration.");
					}
					resolutionUnit = tag.getOffset();
					break;
				}
				case(TAG_RESOLUTION_X):
				{
					if (count != 1 || type != TAG_TYPE_RATIONAL)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for planar configuration.");
					}
					Object o = tag.getObject(0);
					if (o != null && o instanceof TIFFRational)
					{
						TIFFRational rational = (TIFFRational)o;
						resolutionX = rational.getAsDouble();
					}
					break;
				}
				case(TAG_RESOLUTION_Y):
				{
					if (count != 1 || type != TAG_TYPE_RATIONAL)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for planar configuration.");
					}
					Object o = tag.getObject(0);
					if (o != null && o instanceof TIFFRational)
					{
						TIFFRational rational = (TIFFRational)o;
						resolutionY = rational.getAsDouble();
					}
					break;
				}
				case(TAG_PLANAR_CONFIGURATION):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for planar configuration.");
					}
					planarConfiguration = tag.getOffset();
					break;
				}
				case(TAG_ROWS_PER_STRIP):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for image height.");
					}
					rowsPerStrip = tag.getOffset();
					break;
				}
				case(TAG_SAMPLES_PER_PIXEL):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for samples per pixel.");
					}
					samplesPerPixel = tag.getOffset();
					break;
				}
				case(TAG_SOFTWARE):
				{
					software = tag.getString();
					break;
				}
				case(TAG_STRIP_BYTE_COUNTS):
				{
					if (count < 1)
					{
						throw new InvalidFileStructureException("Need at least one strip offset.");
					}
					if (count == 1)
					{
						if (isNotInt)
						{
							throw new InvalidFileStructureException("There is " +
								"only one strip offset, but its type is not integer.");
						}
						stripByteCounts = new Vector();
						stripByteCounts.addElement(new Long(tag.getOffset()));
					}
					else
					{
						stripByteCounts = tag.getVector();
					}
					break;
				}
				case(TAG_STRIP_OFFSETS):
				{
					if (count < 1)
					{
						throw new InvalidFileStructureException("Need at least one strip offset.");
					}
					if (count == 1)
					{
						if (isNotInt)
						{
							throw new InvalidFileStructureException("There is " +
								"only one strip offset, but its type is not integer.");
						}
						stripOffsets = new Vector();
						stripOffsets.addElement(new Long(tag.getOffset()));
					}
					else
					{
						stripOffsets = tag.getVector();
					}
					numStrips = count;
					numTiles = count;
					horizontalTiles = 1;
					verticalTiles = count;
					break;
				}
				case(TAG_T4_OPTIONS):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for T4 Options.");
					}
					t4Options = tag.getOffset();
					break;
				}
				case(TAG_T6_OPTIONS):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for T6 Options.");
					}
					t6Options = tag.getOffset();
					break;
				}
				case(TAG_TILE_HEIGHT):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for image height " +
							"(count=" + count + ", type=" + type + ").");
					}
					tileHeight = tag.getOffset();
					if (tileHeight < 1)
					{
						throw new InvalidFileStructureException("Tile height must be one or larger.");
					}
					verticalTiles = height / tileHeight;
					if ((height % tileHeight) != 0)
					{
						verticalTiles++;
					}
					break;
				}
				case(TAG_TILE_OFFSETS):
				{
					if (count < 1)
					{
						throw new InvalidFileStructureException("Need at least one tile offset.");
					}
					if (count == 1)
					{
						if (isNotInt)
						{
							throw new InvalidFileStructureException("There is " +
								"only one tile offset, but its type is not integer.");
						}
						tileOffsets = new Vector();
						tileOffsets.addElement(new Long(tag.getOffset()));
					}
					else
					{
						tileOffsets = tag.getVector();
					}
					numStrips = count;
					numTiles = count;
					horizontalTiles = 1;
					verticalTiles = count;
					break;
				}
				case(TAG_TILE_WIDTH):
				{
					if (count != 1 || isNotInt)
					{
						throw new InvalidFileStructureException("Expected " +
							"single byte/short/long value for image height " +
							"(count=" + count + ", type=" + type + ").");
					}
					tileWidth = tag.getOffset();
					if (tileWidth < 1)
					{
						throw new InvalidFileStructureException("Tile width must be one or larger.");
					}
					horizontalTiles = width / tileWidth;
					if ((width % tileWidth) != 0)
					{
						horizontalTiles++;
					}
					break;
				}
			}
		}
		if (planarConfiguration == -1)
		{
			planarConfiguration = PLANAR_CONFIGURATION_CHUNKY;
		}
		if (photometricInterpretation == TIFFConstants.PHOTOMETRIC_PALETTED)
		{
			if (bitsPerPixel == 4)
			{
				imageType = TYPE_PALETTED4;
			}
			else
			if (bitsPerPixel == 8)
			{
				imageType = TYPE_PALETTED8;
			}
			else
			{
				throw new UnsupportedTypeException("Only paletted images with 4 or 8 bits per sample are supported.");
			}
		}
		if (resolutionUnit == 2 && resolutionX > 0.0 && resolutionY > 0.0)
		{
			dpiX = (int)resolutionX;
			dpiY = (int)resolutionY;
		}
		if (isStriped())
		{
			tileWidth = width;
			if (numStrips == 1 && rowsPerStrip == -1)
			{
				rowsPerStrip = height;
			}
			tileHeight = rowsPerStrip;
		}
		if (check)
		{
			checkContent();
		}
	}

	public boolean isGrayscale()
	{
		return getBitsPerPixel() > 1 && 
			(photometricInterpretation == PHOTOMETRIC_BLACK_IS_ZERO || 
			 photometricInterpretation == PHOTOMETRIC_WHITE_IS_ZERO);
	}

	public boolean isPaletted()
	{
		return (photometricInterpretation == PHOTOMETRIC_PALETTED);
	}

	/**
	 * Returns <code>true</code> if the image belonging to this IFD
	 * is stored as strips, <code>false</code> otherwise.
	 * @see #isTiled
	 */
	public boolean isStriped()
	{
		return (stripOffsets != null);
	}

	/**
	 * Returns <code>true</code> if the image belonging to this IFD
	 * is stored as tiles, <code>false</code> otherwise.
	 * @see #isStriped
	 */
	public boolean isTiled()
	{
		return (tileOffsets != null);
	}

	/**
	 * Sets the time zone to be used when trying to interpret dates
	 * found in a {@link #TAG_DATE_TIME} tag.
	 * Example call: 
	 * <code>setTimeZone(TimeZone.getTimeZone("America/New_York");</code>.
	 * @param tz TimeZone object
	 */
	public void setTimeZone(TimeZone tz)
	{
		timeZone = tz;
	}
}
