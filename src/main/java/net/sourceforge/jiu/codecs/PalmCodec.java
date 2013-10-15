/*
 * PalmCodec
 *
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.ByteChannelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.MemoryBilevelImage;
import net.sourceforge.jiu.data.MemoryGray8Image;
import net.sourceforge.jiu.data.MemoryPaletted8Image;
import net.sourceforge.jiu.data.MemoryRGB24Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;
import net.sourceforge.jiu.util.ArrayConverter;
import net.sourceforge.jiu.util.SeekableByteArrayOutputStream;

/**
 * A codec to read and write image files in the native image file format of
 * <a target="_top" href="http://www.palmos.com/">Palm OS</a>,
 * an operating system for handheld devices.
 *
 * <h3>Supported file types when loading</h3>
 * This codec reads uncompressed, scan line compressed and RLE compressed Palm files
 * with bit depths of 1, 2, 4, 8 and 16 bits per pixel.
 * Not supported are the Packbits compression algorithm or any color depths other
 * then the aforementioned.
 *
 * <h3>Supported image types when saving</h3>
 * Compression types <em>Uncompressed</em>, <em>Scan line</em> and <em>RLE</em> are written.
 * When saving an image as a Palm, the image data classes will be mapped to file types as follows:
 * <ul>
 * <li>{@link BilevelImage}: will be saved as a 1 bit per pixel, monochrome file.</li>
 * <li>{@link Gray8Image}: will be saved as an 8 bits per pixel file with a custom
 *  palette which will contain the 256 shades of gray from black - (0, 0, 0) - to
 *  white - (255, 255, 255).</li>
 * <li>{@link Paletted8Image}: it is first checked if the image is using the
 *  Palm system 8 bits per pixel palette. If so, an 8 bits per pixel file
 *  with no custom palette is written, otherwise an 8 bits per pixel file
 *  with a custom palette (of the original length) is written.
 * </li>
 * <li>{@link RGB24Image}: will be saved as a 16 bits per pixel, direct color file.
 *   Some information will get lost when converting from 24 to 16 bits per pixel.
 *   Instead of 256 shades for each red, green and blue (and thus, 256<sup>3</sup> = 16,777,216 
 *   possible colors) the resulting file will only
 *   use 32 shades of red and blue and 64 shades of green (65,536 possible colors).</li>
 * </ul>
 *
 * <h3>I/O objects</h3>
 * This codec supports all the I/O classes that are considered in ImageCodec.
 * If you save images and want a correct <em>compressed size</em> field
 * in the resulting Palm file, make sure to give a RandomAccessFile object to
 * the codec.
 * Or simply use {@link #setFile} which does that automatically.
 *
 * <h3>File extension</h3>
 * This codec suggests <code>.palm</code> as file extension for this file format.
 * This is by no means official, but I find it helpful.
 *
 * <h3>Transparency information</h3>
 * The transparency index in a Palm file is saved and loaded, but a loaded index
 * is not stored in the image object as there is no support for transparency information of 
 * any kind in PixelImage yet.
 * The RGB transparency color that is present in a file only in direct color mode
 * is read but not written.
 *
 * <h3>Bounds</h3>
 * The bounds concept of ImageCodec is supported so that you can load or save 
 * only part of an image.
 *
 * <h3>Open questions on the Palm file format</h3>
 * <ul>
 * <li>How does Packbits compression work? Where can I get sample files or a Windows 
 *  converter that writes those?</li>
 * <li>How is FLAG_4_BYTE_FIELDS interpreted? When are four byte fields used?</li>
 * <li>When loading a 4 bpp Palm image file without a custom palette,
 *  how is the decoder supposed to know whether to take the predefined 
 *  color or grayscale palette with 16 entries?</li>
 * </ul>
 *
 * <h3>Known problems</h3>
 * <ul>
 * <li>Unfortunately, the Palm image file format does not include a signature that
 *  makes it easy to identify such a file. Various checks on allowed combinations of
 *  color depth, compression type etc. will prevent the codec from trying to interpret
 *  all files as Palm image files, but there is still a probability of false 
 *  identification.</li>
 * </ul>
 *
 * <h3>Usage examples</h3>
 * Load an image from a Palm image file:
 * <pre>
 * PalmCodec codec = new PalmCodec();
 * codec.setFile("test.palm", CodecMode.LOAD);
 * codec.process();
 * PixelImage image = codec.getImage();
 * codec.close();
 * </pre>
 * Save an image to a Palm file using RLE compression:
 * <pre>
 * PalmCodec codec = new PalmCodec();
 * codec.setImage(image);
 * codec.setCompression(PalmCodec.COMPRESSION_RLE);
 * codec.setFile("out.palm", CodecMode.SAVE);
 * codec.process();
 * codec.close();
 * </pre>
 *
 * <h3>Background</h3>
 * The code is based on:
 * <ul>
 * <li>the specification 
 * <a target="_top" href="http://www.kawt.de/doc/palmimage.html">Palm
 * Native Image Format</a>,</li>
 * <li>the source code of the utilities <code>pnmtopalm</code> and
 * <code>palmtopnm</code> that are part of the
 * <a href="http://netpbm.sourceforge.net" target="_top">Netpbm</a> package,</li>
 * <li><a href="http://oasis.palm.com/dev/kb/papers/1831.cfm" target="_top">Palm OS Compressed Bitmaps</a> by Ken Krugler,
 *  a Palm Developer Knowledge Base article on the scan line compression algorithm and</li>
 * <li><a href="http://oasis.palm.com/dev/kb/manuals/sdk/Bitmap.cfm" target="_top">Palm OS Bitmaps</a>,
 *  also part of the Palm Developer Knowledge Base, contains general information on the 
 *  structure of Palm images.</li>
 * </ul>
 * I also received helpful feedback and test images from Bill Janssen.
 *
 * @author Marco Schmidt
 */
public class PalmCodec extends ImageCodec
{
	/**
	 * Constant for compression type <em>Uncompressed</em>.
	 */
	public static final int COMPRESSION_NONE = 255;

	/**
	 * Constant for compression type <em>Packbits</em>.
	 */
	public static final int COMPRESSION_PACKBITS = 2;

	/**
	 * Constant for compression type <em>RLE (run length encoding)</em>.
	 */
	public static final int COMPRESSION_RLE = 1;

	/**
	 * Constant for compression type <em>Scanline</em>.
	 */
	public static final int COMPRESSION_SCANLINE = 0;

	private static final int FLAG_COMPRESSED = 0x8000;
	private static final int FLAG_COLOR_TABLE = 0x4000;
	private static final int FLAG_TRANSPARENCY = 0x2000;
	//private static final int FLAG_INDIRECT = 0x1000;
	//private static final int FLAG_FOR_SCREEN = 0x0800;
	private static final int FLAG_DIRECT_COLOR = 0x0400;
	//private static final int FLAG_4_BYTE_FIELD = 0x0200;

	// following the Palm OS default palettes
	// instead of short we could use byte but that would require converting
	// all values > 127 to byte representation (-128 .. 128)

	private static final short[][] PALM_SYSTEM_PALETTE_4_GRAY = new short[][]
	{
		{ 255, 255, 255}, { 192, 192, 192}, { 128, 128, 128 }, {   0,   0,   0 }
	};

	private static final short[][] PALM_SYSTEM_PALETTE_16_COLOR = new short[][]
	{
		{ 255, 255, 255}, { 128, 128, 128 }, { 128,   0,   0 }, { 128, 128,   0 },
		{   0, 128,   0}, {   0, 128, 128 }, {   0,   0, 128 }, { 128,   0, 128 },
		{ 255,   0, 255}, { 192, 192, 192 }, { 255,   0,   0 }, { 255, 255,   0 },
		{   0, 255,   0}, {   0, 255, 255 }, {   0,   0, 255 }, {   0,   0,   0 }
	};

	private static final short[][] PALM_SYSTEM_PALETTE_16_GRAY = new short[][]
	{
		{ 255, 255, 255}, { 238, 238, 238 }, { 221, 221, 221 }, { 204, 204, 204 },
		{ 187, 187, 187}, { 170, 170, 170 }, { 153, 153, 153 }, { 136, 136, 136 },
		{ 119, 119, 119}, { 102, 102, 102 }, {  85,  85,  85 }, {  68,  68,  68 },
		{  51,  51,  51}, {  34,  34,  34 }, {  17,  17,  17 }, {   0,   0,   0 }
	};

	private static final short[][] PALM_SYSTEM_PALETTE_256 = new short[][]
	{
		{ 255, 255, 255 }, { 255, 204, 255 }, { 255, 153, 255 }, { 255, 102, 255 }, 
		{ 255,  51, 255 }, { 255,   0, 255 }, { 255, 255, 204 }, { 255, 204, 204 }, 
		{ 255, 153, 204 }, { 255, 102, 204 }, { 255,  51, 204 }, { 255,   0, 204 }, 
		{ 255, 255, 153 }, { 255, 204, 153 }, { 255, 153, 153 }, { 255, 102, 153 }, 
		{ 255,  51, 153 }, { 255,   0, 153 }, { 204, 255, 255 }, { 204, 204, 255 },
		{ 204, 153, 255 }, { 204, 102, 255 }, { 204,  51, 255 }, { 204,   0, 255 },
		{ 204, 255, 204 }, { 204, 204, 204 }, { 204, 153, 204 }, { 204, 102, 204 },
		{ 204,  51, 204 }, { 204,   0, 204 }, { 204, 255, 153 }, { 204, 204, 153 },
		{ 204, 153, 153 }, { 204, 102, 153 }, { 204,  51, 153 }, { 204,   0, 153 },
		{ 153, 255, 255 }, { 153, 204, 255 }, { 153, 153, 255 }, { 153, 102, 255 },
		{ 153,  51, 255 }, { 153,   0, 255 }, { 153, 255, 204 }, { 153, 204, 204 },
		{ 153, 153, 204 }, { 153, 102, 204 }, { 153,  51, 204 }, { 153,   0, 204 },
		{ 153, 255, 153 }, { 153, 204, 153 }, { 153, 153, 153 }, { 153, 102, 153 },
		{ 153,  51, 153 }, { 153,   0, 153 }, { 102, 255, 255 }, { 102, 204, 255 },
		{ 102, 153, 255 }, { 102, 102, 255 }, { 102,  51, 255 }, { 102,   0, 255 },
		{ 102, 255, 204 }, { 102, 204, 204 }, { 102, 153, 204 }, { 102, 102, 204 },
		{ 102,  51, 204 }, { 102,   0, 204 }, { 102, 255, 153 }, { 102, 204, 153 },
		{ 102, 153, 153 }, { 102, 102, 153 }, { 102,  51, 153 }, { 102,   0, 153 },
		{  51, 255, 255 }, {  51, 204, 255 }, {  51, 153, 255 }, {  51, 102, 255 },
		{  51,  51, 255 }, {  51,   0, 255 }, {  51, 255, 204 }, {  51, 204, 204 },
		{  51, 153, 204 }, {  51, 102, 204 }, {  51,  51, 204 }, {  51,   0, 204 },
		{  51, 255, 153 }, {  51, 204, 153 }, {  51, 153, 153 }, {  51, 102, 153 },
		{  51,  51, 153 }, {  51,   0, 153 }, {   0, 255, 255 }, {   0, 204, 255 },
		{   0, 153, 255 }, {   0, 102, 255 }, {   0,  51, 255 }, {   0,   0, 255 },
		{   0, 255, 204 }, {   0, 204, 204 }, {   0, 153, 204 }, {   0, 102, 204 },
		{   0,  51, 204 }, {   0,   0, 204 }, {   0, 255, 153 }, {   0, 204, 153 },
		{   0, 153, 153 }, {   0, 102, 153 }, {   0,  51, 153 }, {   0,   0, 153 },
		{ 255, 255, 102 }, { 255, 204, 102 }, { 255, 153, 102 }, { 255, 102, 102 },
		{ 255,  51, 102 }, { 255,   0, 102 }, { 255, 255,  51 }, { 255, 204,  51 },
		{ 255, 153,  51 }, { 255, 102,  51 }, { 255,  51,  51 }, { 255,   0,  51 },
		{ 255, 255,   0 }, { 255, 204,   0 }, { 255, 153,   0 }, { 255, 102,   0 },
		{ 255,  51,   0 }, { 255,   0,   0 }, { 204, 255, 102 }, { 204, 204, 102 },
		{ 204, 153, 102 }, { 204, 102, 102 }, { 204,  51, 102 }, { 204,   0, 102 },
		{ 204, 255,  51 }, { 204, 204,  51 }, { 204, 153,  51 }, { 204, 102,  51 },
		{ 204,  51,  51 }, { 204,   0,  51 }, { 204, 255,   0 }, { 204, 204,   0 },
		{ 204, 153,   0 }, { 204, 102,   0 }, { 204,  51,   0 }, { 204,   0,   0 },
		{ 153, 255, 102 }, { 153, 204, 102 }, { 153, 153, 102 }, { 153, 102, 102 },
		{ 153,  51, 102 }, { 153,   0, 102 }, { 153, 255,  51 }, { 153, 204,  51 },
		{ 153, 153,  51 }, { 153, 102,  51 }, { 153,  51,  51 }, { 153,   0,  51 },
		{ 153, 255,   0 }, { 153, 204,   0 }, { 153, 153,   0 }, { 153, 102,   0 },
		{ 153,  51,   0 }, { 153,   0,   0 }, { 102, 255, 102 }, { 102, 204, 102 },
		{ 102, 153, 102 }, { 102, 102, 102 }, { 102,  51, 102 }, { 102,   0, 102 },
		{ 102, 255,  51 }, { 102, 204,  51 }, { 102, 153,  51 }, { 102, 102,  51 },
		{ 102,  51,  51 }, { 102,   0,  51 }, { 102, 255,   0 }, { 102, 204,   0 },
		{ 102, 153,   0 }, { 102, 102,   0 }, { 102,  51,   0 }, { 102,   0,   0 },
		{  51, 255, 102 }, {  51, 204, 102 }, {  51, 153, 102 }, {  51, 102, 102 },
		{  51,  51, 102 }, {  51,   0, 102 }, {  51, 255,  51 }, {  51, 204,  51 },
		{  51, 153,  51 }, {  51, 102,  51 }, {  51,  51,  51 }, {  51,   0,  51 },
		{  51, 255,   0 }, {  51, 204,   0 }, {  51, 153,   0 }, {  51, 102,   0 },
		{  51,  51,   0 }, {  51,   0,   0 }, {   0, 255, 102 }, {   0, 204, 102 },
		{   0, 153, 102 }, {   0, 102, 102 }, {   0,  51, 102 }, {   0,   0, 102 },
		{   0, 255,  51 }, {   0, 204,  51 }, {   0, 153,  51 }, {   0, 102,  51 },
		{   0,  51,  51 }, {   0,   0 , 51 }, {   0, 255,   0 }, {   0, 204,   0 },
		{   0, 153,   0 }, {   0, 102,   0 }, {   0,  51,   0 }, {  17,  17,  17 },
		{  34,  34,  34 }, {  68,  68,  68 }, {  85,  85,  85 }, { 119, 119, 119 },
		{ 136, 136, 136 }, { 170, 170, 170 }, { 187, 187, 187 }, { 221, 221, 221 },
		{ 238, 238, 238 }, { 192, 192, 192 }, { 128,   0,   0 }, { 128,   0, 128 },
		{   0, 128,   0 }, {   0, 128, 128 }, {   0,   0,   0 }, {   0,   0,   0 },
		{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
		{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
		{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
		{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
		{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
		{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }
	};

	private int bitsPerPixel;
	private int blueBits;
	private int bytesPerRow;
	private int compression;
	private long compressedDataOffset;
	private int flags;
	private int greenBits;
	private int height;
	private Palette palette;
	//private int nextImageOffset;
	private int redBits;
	private byte[] rgb;
	private byte[] transColor;
	private int transparencyIndex = -1;
	private int version;
	private int width;

	private static Palette createPalette(short[][] data)
	{
		Palette result = new Palette(data.length);
		for (int i = 0; i < data.length; i++)
		{
			result.put(i, data[i][0], data[i][1], data[i][2]);
		}
		return result;
	}

	/**
	 * Creates the 2 bits per pixel Palm system palette with grayscale values.
	 * This palette is used when no custom palette is defined in a 2 bpp image.
	 * @return Palm's default palette for 2 bits per pixel (grayscale), with 4 entries
	 */
	public static Palette createSystem2BitGrayscalePalette()
	{
		return createPalette(PALM_SYSTEM_PALETTE_4_GRAY);
	}

	/**
	 * Creates the 4 bits per pixel Palm system palette with color values.
	 * This palette (or the 4 bpp grayscale palette) is used when no custom palette is defined in a 4 bpp image.
	 * @return Palm's default palette for 4 bits per pixel (color), with 16 entries
	 */
	public static Palette createSystem4BitColorPalette()
	{
		return createPalette(PALM_SYSTEM_PALETTE_16_COLOR);
	}

	/**
	 * Creates the 4 bits per pixel Palm system palette with grayscale values.
	 * This palette (or the 4 bpp color palette) is used when no custom palette is defined in a 4 bpp image.
	 * @return Palm's default palette for 4 bits per pixel (grayscale), with 16 entries
	 */
	public static Palette createSystem4BitGrayscalePalette()
	{
		return createPalette(PALM_SYSTEM_PALETTE_16_GRAY);
	}

	/**
	 * Creates the 8 bits per pixel Palm system palette.
	 * This palette is used when no custom palette is defined in an 8 bpp image.
	 * @return Palm's default palette for 8 bits per pixel, with 256 entries
	 */
	public static Palette createSystem8BitPalette()
	{
		return createPalette(PALM_SYSTEM_PALETTE_256);
	}

	/**
	 * Returns the Palm compression method.
	 * This should be one of the COMPRESSION_xyz constants of this class.
	 * @return integer value with the compression method (found in a file when
	 *  loading or to be used for saving)
	 * @see #setCompression
	 */
	public int getCompression()
	{
		return compression;
	}

	public String getFormatName()
	{
		return "Palm image file format";
	}

	public String[] getMimeTypes()
	{
		return null;
	}

	/**
	 * Returns the transpareny index if one is available ({@link #hasTransparencyIndex}
	 * returns <code>true</code>) or an undefined value otherwise.
	 * @see #hasTransparencyIndex
	 * @see #removeTransparencyIndex
	 * @see #setTransparencyIndex
	 */
	public int getTransparencyIndex()
	{
		return transparencyIndex;
	}

	/**
	 * Returns whether a transpareny index is available and can be
	 * retrieved via {@link #getTransparencyIndex}.
	 * @return transparency index, a positive value that is a valid index into the palette
	 * @see #getTransparencyIndex
	 * @see #removeTransparencyIndex
	 * @see #setTransparencyIndex
	 */
	public boolean hasTransparencyIndex()
	{
		return transparencyIndex >= 0;
	}

	private void invertBilevelData(byte[] row)
	{
		if (row != null)
		{
			for (int i = 0; i < row.length; i++)
			{
				row[i] = (byte)~row[i];
			}
		}
	}

	private static boolean isEqualPalette(Palette palette, short[][] data)
	{
		if (palette == null || data == null)
		{
			return false;
		}
		if (palette.getNumEntries() != data.length)
		{
			return false;
		}
		for (int i = 0; i < data.length; i++)
		{
			int red = palette.getSample(RGBIndex.INDEX_RED, i);
			int green = palette.getSample(RGBIndex.INDEX_GREEN, i);
			int blue = palette.getSample(RGBIndex.INDEX_BLUE, i);
			short[] color = data[i];
			if (color[0] != red || color[1] != green || color[2] != blue)
			{
				return false;
			}
		}
		return true;
	}

	public boolean isLoadingSupported()
	{
		return true;
	}

	/**
	 * Returns if the argument palette is the Palm system grayscale palette
	 * with 4 entries.
	 * @param palette to be checked
	 * @see #createSystem2BitGrayscalePalette
	 */
	public static boolean isPalmSystemPaletteGray4(Palette palette)
	{
		return isEqualPalette(palette, PALM_SYSTEM_PALETTE_4_GRAY);
	}

	/**
	 * Returns if the argument palette is the Palm system grayscale palette
	 * with 16 entries.
	 * @param palette to be checked
	 * @see #createSystem4BitGrayscalePalette
	 */
	public static boolean isPalmSystemPaletteGray16(Palette palette)
	{
		return isEqualPalette(palette, PALM_SYSTEM_PALETTE_16_GRAY);
	}

	/**
	 * Returns if the argument palette is the Palm system color palette
	 * with 16 entries.
	 * @param palette to be checked
	 * @see #createSystem4BitColorPalette
	 */
	public static boolean isPalmSystemPaletteColor16(Palette palette)
	{
		return isEqualPalette(palette, PALM_SYSTEM_PALETTE_16_COLOR);
	}

	/**
	 * Returns if the argument palette is the Palm system palette
	 * with 256 colors.
	 * @param palette to be checked
	 * @see #createSystem8BitPalette
	 * @return if the argument is an 8 bits per pixel Palm system palette
	 */
	public static boolean isPalmSystemPalette256(Palette palette)
	{
		return isEqualPalette(palette, PALM_SYSTEM_PALETTE_256);
	}

	public boolean isSavingSupported()
	{
		return true;
	}

	private void load() throws 
		InvalidFileStructureException, 
		IOException, 
		OperationFailedException,
		UnsupportedTypeException,
		WrongFileFormatException
	{
		DataInput in = getInputAsDataInput();
		loadHeader(in);
		loadPalette(in);
		loadImage(in);
	}

	private void loadHeader(DataInput in) throws 
		InvalidFileStructureException, 
		IOException, 
		UnsupportedTypeException,
		WrongFileFormatException
	{
		width = in.readShort() & 0xffff;
		height = in.readShort() & 0xffff;
		bytesPerRow = in.readShort() & 0xffff;
		flags = in.readShort() & 0xffff;
		bitsPerPixel = in.readUnsignedByte();
		version = in.readUnsignedByte();
		//nextImageOffset = in.readShort() & 0xffff;
		in.readShort();
		transparencyIndex = in.readUnsignedByte() & 0xffff;
		compression = in.readUnsignedByte() & 0xffff;
		in.skipBytes(2); // reserved
		if ((flags & FLAG_COMPRESSED) == 0)
		{
			compression = COMPRESSION_NONE;
		}
		boolean unsupportedDirectColor = false;
		if ((flags & FLAG_DIRECT_COLOR) != 0)
		{
			// read direct color information (8 bytes)
			redBits = in.readUnsignedByte();
			greenBits = in.readUnsignedByte();
			blueBits = in.readUnsignedByte();
			unsupportedDirectColor = redBits != 5 || greenBits != 6 || blueBits != 5;
			in.skipBytes(2);
			transColor = new byte[3];
			in.readFully(transColor);
		}
		if (width < 1 || height < 1 || 
		    unsupportedDirectColor || 
		    (bitsPerPixel != 1 && bitsPerPixel != 2 && bitsPerPixel != 4 && bitsPerPixel != 8 && bitsPerPixel != 16) ||
		    (compression != COMPRESSION_NONE && compression != COMPRESSION_RLE && compression != COMPRESSION_SCANLINE))
		{
			throw new WrongFileFormatException("Not a file in Palm image file format.");
		}
		/*System.out.println("width=" + width + ", height=" + height + ", bytes per row=" +
			bytesPerRow + ", flags=" + flags + ", bpp=" + bitsPerPixel + ", version=" +
			version + ", palette=" + (((flags & FLAG_COLOR_TABLE) != 0) ? "y" : "n") +
			", transparent=" + transparencyIndex + ", compression=" + compression);*/
	}

	private void loadImage(DataInput in) throws 
		InvalidFileStructureException, 
		IOException, 
		UnsupportedTypeException,
		WrongFileFormatException,
		WrongParameterException
	{
		setBoundsIfNecessary(width, height);
		checkBounds(width, height);
		PixelImage image = getImage();
		/* if there is no image to be reused (image == null), create one;
		   otherwise check if the provided image is of the right type 
		   and throw an exception if not */
		if (palette != null)
		{
			// paletted image
			if (image == null)
			{
				image = new MemoryPaletted8Image(getBoundsWidth(), getBoundsHeight(), palette);
			}
			else
			{
				if (!(image instanceof Paletted8Image))
				{
					throw new WrongParameterException("Image to be used for loading must be paletted for this file.");
				}
				((Paletted8Image)image).setPalette(palette);
			}
		}
		else
		{
			switch(bitsPerPixel)
			{
				case(1): // bilevel image (black and white)
				{
					if (image == null)
					{
						image = new MemoryBilevelImage(getBoundsWidth(), getBoundsHeight());
					}
					else
					{
						if (!(image instanceof BilevelImage))
						{
							throw new WrongParameterException("Image to be used for " +
								"loading must implement BilevelImage for this file.");
						}
					}
					break;
				}
				case(16): // RGB direct color
				{
					if (image == null)
					{
						image = new MemoryRGB24Image(getBoundsWidth(), getBoundsHeight());
					}
					else
					{
						if (!(image instanceof RGB24Image))
						{
							throw new WrongParameterException("Image to be used for " +
								"loading must implement RGB24Image.");
						}
					}
					rgb = new byte[width * 3];
					break;
				}
				default: // grayscale, 2, 4 or 8 bits per pixel
				{
					if (image == null)
					{
						image = new MemoryGray8Image(getBoundsWidth(), getBoundsHeight());
					}
					else
					{
						if (!(image instanceof Gray8Image))
						{
							throw new WrongParameterException("Image to be used for " +
								"loading must implement Gray8Image for this file.");
						}
					}
				}
			}
		}
		setImage(image);
		// check if image has the correct pixel resolution
		if (image.getWidth() != getBoundsWidth() || image.getHeight() != getBoundsHeight())
		{
			throw new WrongParameterException("Image to be reused has wrong resolution (must have " +
				getBoundsWidth() + " x " + getBoundsHeight() + " pixels).");
		}
		loadImageData(in);
	}

	private void loadImageData(DataInput in) throws
		InvalidFileStructureException,
		IOException
	{
		PixelImage image = getImage();
		// if compression is used, read a short with the compressed data size
		if (compression != COMPRESSION_NONE)
		{
			//int compressedDataSize = in.readShort() & 0xffff;
			in.readShort();
		}
		byte[] row = new byte[bytesPerRow];
		final int NUM_ROWS = getBoundsY2() + 1;
		for (int y = 0; y < NUM_ROWS; y++)
		{
			switch(compression)
			{
				case(COMPRESSION_NONE):
				{
					in.readFully(row, 0, bytesPerRow);
					break;
				}
				case(COMPRESSION_RLE):
				{
					int index = 0;
					do
					{
						int num = in.readUnsignedByte();
						if (num < 1 || index + num > bytesPerRow)
						{
							String message = "At index=" + index + ", y=" + y + " there is a run length of " + num;
							System.err.println("ERROR decoding RLE: " + message);
							throw new InvalidFileStructureException(message);
						}
						byte value = in.readByte();
						while (num-- > 0)
						{
							row[index++] = value;
						}
					}
					while (index < bytesPerRow);
					break;
				}
				case(COMPRESSION_SCANLINE):
				{
					int index = 0;
					int pixelMask = 0;
					int mask = 0;
					do
					{
						if (mask == 0)
						{
							pixelMask = in.readUnsignedByte();
							mask = 0x80;
						}
						if ((pixelMask & mask) == 0)
						{
							index++;
						}
						else
						{
							row[index++] = in.readByte();
						}
						mask >>= 1;
					}
					while (index < bytesPerRow);
					break;
				}
				case(COMPRESSION_PACKBITS):
				{
					// compression algorithm unknown, thus not implemented
					// this statement cannot be reached, the codec makes
					// sure that an exception gets thrown when the packbits
					// algorithm is actually encountered in a file;
					// if you have a description of the algorithm, please
					// contact the JIU maintainers
					break;
				}
			}
			store(image, y, row);
			setProgress(y, NUM_ROWS);
		}
	}

	private void loadPalette(DataInput in) throws 
		InvalidFileStructureException, 
		IOException, 
		UnsupportedTypeException,
		WrongFileFormatException
	{
		if ((flags & FLAG_COLOR_TABLE) == 0)
		{
			switch(bitsPerPixel)
			{
				case(2):
				{
					palette = createSystem2BitGrayscalePalette();
					break;
				}
				case(4):
				{
					palette = createSystem4BitGrayscalePalette(); // or color?
					break;
				}
				case(8):
				{
					palette = createSystem8BitPalette();
					break;
				}
			}
			return;
		}
		int numEntries = in.readShort() & 0xffff;
		if (numEntries < 1 || numEntries > 256)
		{
			throw new WrongFileFormatException("Not a Palm image file, invalid number of palette entries: "  + numEntries);
		}
		palette = new Palette(numEntries, 255);
		for (int i = 0; i < numEntries; i++)
		{
			//int reserved = in.readUnsignedByte();
			in.readUnsignedByte();
			int red = in.readUnsignedByte();
			int green = in.readUnsignedByte();
			int blue = in.readUnsignedByte();
			palette.putSample(RGBIndex.INDEX_RED, i, red);
			palette.putSample(RGBIndex.INDEX_GREEN, i, green);
			palette.putSample(RGBIndex.INDEX_BLUE, i, blue);
		}
	}

	public void process() throws 
		InvalidFileStructureException,
		MissingParameterException,
		OperationFailedException,
		WrongParameterException
	{
		try
		{
			initModeFromIOObjects();
			if (getMode() == CodecMode.LOAD)
			{
				load();
			}
			else
			if (getMode() == CodecMode.SAVE)
			{
				save();
			}
			else
			{
				throw new WrongParameterException("Could find neither objects for loading nor for saving.");
			}
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("I/O error in Palm codec: " + ioe.toString());
		}
	}

	/**
	 * Removes the transparency index if one has been set.
	 * @see #getTransparencyIndex
	 * @see #hasTransparencyIndex
	 * @see #setTransparencyIndex
	 */
	public void removeTransparencyIndex()
	{
		transparencyIndex = -1;
	}

	private void save() throws 
		IOException, 
		OperationFailedException,
		UnsupportedTypeException
	{
		// get image, set bounds if necessary and check existing bounds
		PixelImage image = getImage();
		if (image == null)
		{
			throw new MissingParameterException("Need image to save.");
		}
		setBoundsIfNecessary(image.getWidth(), image.getHeight());
		checkBounds(image.getWidth(), image.getHeight());
		// get output object
		DataOutput out = getOutputAsDataOutput();
		if (out == null)
		{
			throw new MissingParameterException("Could not get DataOutput object when saving in Palm file format.");
		}
		// initialize fields to be written to the header
		width = getBoundsWidth();
		height = getBoundsHeight();
		flags = 0;
		if (hasTransparencyIndex())
		{
			flags |= FLAG_TRANSPARENCY;
		}
		if (compression != COMPRESSION_NONE)
		{
			flags |= FLAG_COMPRESSED;
		}
		version = 0;
		if (bitsPerPixel > 1)
		{
			version = 1;
		}
		if (hasTransparencyIndex() || compression != COMPRESSION_NONE)
		{
			version = 2;
		}
		//nextImageOffset = 0;
		compressedDataOffset = 0;
		// check image types
		if (image instanceof BilevelImage)
		{
			save(out, (BilevelImage)image);
		}
		else
		if (image instanceof Gray8Image)
		{
			save(out, (Gray8Image)image);
		}
		else
		if (image instanceof Paletted8Image)
		{
			save(out, (Paletted8Image)image);
		}
		else
		if (image instanceof RGB24Image)
		{
			save(out, (RGB24Image)image);
		}
		else
		{
			throw new UnsupportedTypeException("Unsupported image type: " + image.getClass().getName());
		}
	}

	private void save(DataOutput out, BilevelImage image) throws IOException
	{
		bytesPerRow = (width + 7) / 8;
		if ((bytesPerRow % 2) == 1)
		{
			bytesPerRow++;
		}
		bitsPerPixel = 1;
		setCorrectVersion();
		saveHeader(out);
		byte[] row = new byte[bytesPerRow];
		byte[] prev = null;
		if (compression == COMPRESSION_SCANLINE)
		{
			prev = new byte[row.length];
		}
		final int X1 = getBoundsX1();
		final int Y1 = getBoundsY1();
		for (int y = 0; y < height; y++)
		{
			image.getPackedBytes(X1, y + Y1, width, row, 0, 0);
			invertBilevelData(row);
			saveRow(out, y == 0, row, prev);
			if (compression == COMPRESSION_SCANLINE)
			{
				System.arraycopy(row, 0, prev, 0, row.length);
			}
			setProgress(y, height);
		}
		saveFinalCompressedSize(out);
	}

	private void save(DataOutput out, Gray8Image image) throws IOException
	{
		bytesPerRow = width;
		if ((bytesPerRow % 2) == 1)
		{
			bytesPerRow++;
		}
		bitsPerPixel = 8;
		flags |= FLAG_COLOR_TABLE;
		setCorrectVersion();
		saveHeader(out);
		out.writeShort(256); // palette length
		for (int i = 0; i < 256; i++)
		{
			out.writeByte(0); // reserved
			out.writeByte(i); // red
			out.writeByte(i); // green
			out.writeByte(i); // blue
		}
		compressedDataOffset += 2 + 4 * 256;
		saveInitialCompressedSize(out);
		byte[] row = new byte[width];
		byte[] prev = null;
		if (compression == COMPRESSION_SCANLINE)
		{
			prev = new byte[width];
		}
		final int X1 = getBoundsX1();
		final int Y1 = getBoundsY1();
		for (int y = 0; y < height; y++)
		{
			image.getByteSamples(0, X1, y + Y1, width, 1, row, 0);
			saveRow(out, y == 0, row, prev);
			if (compression == COMPRESSION_SCANLINE)
			{
				System.arraycopy(row, 0, prev, 0, row.length);
			}
			setProgress(y, height);
		}
		saveFinalCompressedSize(out);
	}

	private void save(DataOutput out, Paletted8Image image) throws IOException
	{
		Palette palette = image.getPalette();
		boolean system256Palette = isPalmSystemPalette256(palette);
		boolean system16GrayPalette = isPalmSystemPaletteGray16(palette);
		boolean system16ColorPalette = isPalmSystemPaletteColor16(palette);
		boolean system4GrayPalette = isPalmSystemPaletteGray4(palette);
		boolean customPalette = !(system256Palette || system16GrayPalette || system16ColorPalette || system4GrayPalette);
		if (customPalette)
		{
			flags |= FLAG_COLOR_TABLE;
		}
		// determine bits per pixel, bytesPerRow
		if (palette.getNumEntries() <= 4)
		{
			bitsPerPixel = 2;
			bytesPerRow = (width + 3) / 4;
		}
		else
		if (palette.getNumEntries() <= 16)
		{
			bitsPerPixel = 4;
			bytesPerRow = (width + 1) / 2;
		}
		else
		{
			bitsPerPixel = 8;
			bytesPerRow = width;
		}
		//System.out.println("initial bytesPerRow=" + bytesPerRow);
		// make sure number of bytes per row is even
		if ((bytesPerRow % 2) == 1)
		{
			bytesPerRow++;
		}
		setCorrectVersion();
		saveHeader(out);
		// write the custom palette if necessary
		if (customPalette)
		{
			savePalette(out, palette);
		}
		// if compression type != uncompressed write two bytes with compressed size to output
		saveInitialCompressedSize(out);
		// initialize row buffers
		byte[] row = new byte[width];
		byte[] prev = null;
		if (compression == COMPRESSION_SCANLINE)
		{
			prev = new byte[row.length];
		}
		byte[] temp = null;
		if (bitsPerPixel < 8)
		{
			temp = new byte[width];
		}
		// get position of upper left corner of image part to be written
		final int X1 = getBoundsX1();
		final int Y1 = getBoundsY1();
		// write all rows to file, top to bottom
		for (int y = 0; y < height; y++)
		{
			switch(bitsPerPixel)
			{
				case(2):
				{
					image.getByteSamples(0, X1, y + Y1, width, 1, temp, 0);
					ArrayConverter.encodePacked2Bit(temp, 0, row, 0, width);
					break;
				}
				case(4):
				{
					image.getByteSamples(0, X1, y + Y1, width, 1, temp, 0);
					ArrayConverter.encodePacked4Bit(temp, 0, row, 0, width);
					break;
				}
				case(8):
				{
					image.getByteSamples(0, X1, y + Y1, width, 1, row, 0);
					break;
				}
			}
			saveRow(out, y == 0, row, prev);
			if (compression == COMPRESSION_SCANLINE)
			{
				System.arraycopy(row, 0, prev, 0, row.length);
			}
			setProgress(y, height);
		}
		saveFinalCompressedSize(out);
	}

	private void save(DataOutput out, RGB24Image image) throws IOException
	{
		bytesPerRow = width * 2;
		bitsPerPixel = 16;
		flags |= FLAG_DIRECT_COLOR;
		setCorrectVersion();
		saveHeader(out);
		// write 8 bytes for direct color information to file
		out.write(5); // red bits
		out.write(6); // green bits
		out.write(5); // blue bits
		int i = 5;
		while (i-- > 0)
		{
			out.write(0);
		}
		compressedDataOffset += 8;
		// allocate row buffer(s)
		byte[] row = new byte[width * 2];
		byte[] prev = null;
		if (compression == COMPRESSION_SCANLINE)
		{
			prev = new byte[row.length];
		}
		byte[] red = new byte[width];
		byte[] green = new byte[width];
		byte[] blue = new byte[width];
		
		final int X1 = getBoundsX1();
		final int Y1 = getBoundsY1();
		for (int y = 0; y < height; y++)
		{
			// get samples for each channel of the row to be written out
			image.getByteSamples(RGBIndex.INDEX_RED, X1, y + Y1, width, 1, red, 0);
			image.getByteSamples(RGBIndex.INDEX_GREEN, X1, y + Y1, width, 1, green, 0);
			image.getByteSamples(RGBIndex.INDEX_BLUE, X1, y + Y1, width, 1, blue, 0);
			// encode row as 16 bit samples, big endian, 5-6-5
			ArrayConverter.encodeRGB24ToPackedRGB565BigEndian(
				red, 0,
				green, 0,
				blue, 0,
				row, 0,
				width);
			saveRow(out, y == 0, row, prev);
			if (compression == COMPRESSION_SCANLINE)
			{
				System.arraycopy(row, 0, prev, 0, row.length);
			}
			setProgress(y, height);
		}
		saveFinalCompressedSize(out);
	}

	private void saveFinalCompressedSize(DataOutput out) throws IOException
	{
		if ((flags & FLAG_COMPRESSED) == 0)
		{
			return;
		}
		if (!(out instanceof RandomAccessFile || out instanceof SeekableByteArrayOutputStream))
		{
			return;
		}
		long pos = -1;
		if (out instanceof RandomAccessFile)
		{
			RandomAccessFile raf = (RandomAccessFile)out;
			pos = raf.length();
		}
		else
		if (out instanceof SeekableByteArrayOutputStream)
		{
			SeekableByteArrayOutputStream sbaos = (SeekableByteArrayOutputStream)out;
			pos = sbaos.getPosition();
		}
		long compressedSize = pos - compressedDataOffset;
		compressedSize = Math.min(0xffff, compressedSize);
		/*
		System.out.println("compressed data offset=" + compressedDataOffset);
		System.out.println("position after compression=" + pos);
		System.out.println("compressed size=" + compressedSize + " / " + Integer.toHexString((int)compressedSize));
		*/
		if (out instanceof RandomAccessFile)
		{
			RandomAccessFile raf = (RandomAccessFile)out;
			raf.seek(compressedDataOffset);
			raf.writeShort((int)compressedSize);
		}
		else
		if (out instanceof SeekableByteArrayOutputStream)
		{
			SeekableByteArrayOutputStream sbaos = (SeekableByteArrayOutputStream)out;
			sbaos.seek((int)compressedDataOffset);
			sbaos.write((int)(compressedSize >> 8) & 0xff);
			sbaos.write((int)compressedSize & 0xff);
		}
	}

	private void saveHeader(DataOutput out) throws IOException
	{
		out.writeShort(width);
		out.writeShort(height);
		out.writeShort(bytesPerRow);
		out.writeShort(flags);
		out.writeByte(bitsPerPixel);
		out.writeByte(version);
		out.writeShort(0); // next image offset
		out.writeByte(transparencyIndex);
		out.writeByte(compression);
		out.writeShort(0); // reserved
		compressedDataOffset = 16;
	}

	private void saveInitialCompressedSize(DataOutput out) throws IOException
	{
		if ((flags & FLAG_COMPRESSED) == 0)
		{
			return;
		}
		out.writeShort(bytesPerRow * height); // just a guess
	}

	private void savePalette(DataOutput out, Palette palette) throws IOException
	{
		out.writeShort(palette.getNumEntries());
		for (int i = 0; i < palette.getNumEntries(); i++)
		{
			out.writeByte(0); // reserved
			out.writeByte(palette.getSample(RGBIndex.INDEX_RED, i));
			out.writeByte(palette.getSample(RGBIndex.INDEX_GREEN, i));
			out.writeByte(palette.getSample(RGBIndex.INDEX_BLUE, i));
		}
		compressedDataOffset += 2 + 4 * palette.getNumEntries();
	}

	private void saveRow(DataOutput out, boolean firstRow, byte[] row, byte[] prev) throws IOException
	{
		switch(compression)
		{
			case(COMPRESSION_NONE):
			{
				out.write(row, 0, bytesPerRow);
				break;
			}
			case(COMPRESSION_RLE):
			{
				saveRowRLE(out, row);
				break;
			}
			case(COMPRESSION_SCANLINE):
			{
				saveRowScanLine(out, firstRow, row, prev);
				break;
			}
		}
	}

/*		int srcOffset = 0; // points into uncompressed data array "row"
		do
		{
			// determine length of next run, between 1 and 255
			byte value = row[srcOffset];
			int lookAheadOffset = srcOffset + 1;
			int bytesLeft = bytesPerRow - lookAheadOffset;
			if (bytesLeft > 255)
			{
				bytesLeft = 255;
			}
			while (bytesLeft != 0 && value == row[lookAheadOffset])
			{
				lookAheadOffset++;
				bytesLeft--;
			}
			int runLength = lookAheadOffset - srcOffset;
			if (runLength < 1)
			{
				System.err.println("FATAL: RUN LENGTH <0");
				System.exit(1);
			}
			if (srcOffset + runLength > bytesPerRow)
			{
				System.err.println("FATAL: srcOffset=" + srcOffset+ " runLength=" + runLength + " bytesPerRow=" + bytesPerRow);
				System.exit(1);
			}
			if (srcOffset == 13 && runLength == 2)
			{
				System.err.println("FATAL: 13 2 ");
				System.exit(1);
			}
			// write pair (length-of-run, value) to output
			out.writeByte(runLength);
			out.writeByte(value & 0xff);
			// update srcOffset to point to the next byte in row to be encoded
			srcOffset += runLength;
		}
		while (srcOffset < bytesPerRow);*/

	private void saveRowRLE(DataOutput out, byte[] row) throws IOException
	{
		int srcOffset = 0; // points into uncompressed data array "row"
		do
		{
			// determine length of next run, between 1 and 255
			int runLength = 1;
			int bytesLeft = bytesPerRow - srcOffset;
			byte value = row[srcOffset];
			while (bytesLeft != 0 && srcOffset + runLength < row.length && value == row[srcOffset + runLength])
			{
				bytesLeft--;
				runLength++;
				if (runLength == 255)
				{
					bytesLeft = 0;
				}
			}
			srcOffset += runLength;
			out.writeByte(runLength);
			out.writeByte(value & 0xff);
		}
		while (srcOffset < bytesPerRow);
	}

	private void saveRowScanLine(DataOutput out, boolean firstRow, byte[] row, byte[] prev) throws IOException
	{
		int bytesLeft = bytesPerRow;
		int srcOffset = 0;
		byte[] bytes = new byte[8];
		do
		{
			int pixelMask = 0;
			int bitMask = 128;
			int numBytesToCheck = Math.min(8, bytesLeft);
			int numOutputBytes = 0;
			bytesLeft -= numBytesToCheck;
			while (numBytesToCheck-- != 0)
			{
				if (row[srcOffset] != prev[srcOffset])
				{
					pixelMask |= bitMask;
					bytes[numOutputBytes++] = row[srcOffset];
				}
				srcOffset++;
				bitMask >>= 1;
			}
			out.writeByte(pixelMask);
			out.write(bytes, 0, numOutputBytes);
		}
		while (bytesLeft != 0);
	}

	/**
	 * Sets the compression algorithm to be used for saving an image.
	 * @see #getCompression
	 * @param newCompressionType int value that is one of the COMPRESSION_xyz constants of this class
	 * @throws IllegalArgumentException if the compression type is unsupported
	 */
	public void setCompression(int newCompressionType)
	{
		if (newCompressionType != COMPRESSION_NONE && 
		    newCompressionType != COMPRESSION_RLE &&
		    newCompressionType != COMPRESSION_SCANLINE)
		{
			throw new IllegalArgumentException("Unsupported Palm compression type for writing.");
		}
		compression = newCompressionType;
	}

	private void setCorrectVersion()
	{
		version = 0;
		if (bitsPerPixel > 1)
		{
			version = 1;
		}
		if (hasTransparencyIndex() || getCompression() == COMPRESSION_SCANLINE || getCompression() == COMPRESSION_RLE)
		{
			version = 2;
		}
	}

	/**
	 * Reuses super.setFile when used for CodecMode.LOAD, but
	 * creates a RandomAccessFile instead of a FileOutputStream
	 * in write mode so that the compressed size can be written
	 * correcly (requires a seek operation).
	 * @param fileName name of the file to be opened
	 * @param codecMode defines whether this codec object is to be used for loading or saving
	 */
	public void setFile(String fileName, CodecMode codecMode) throws
		IOException,
		UnsupportedCodecModeException
	{
		if (codecMode == CodecMode.LOAD)
		{
			super.setFile(fileName, codecMode);
		}
		else
		{
			setRandomAccessFile(new RandomAccessFile(fileName, "rw"), CodecMode.SAVE);
		}
	}
			
	/**
	 * Sets a new transparency index when saving an image.
	 * If this method is called, the argument value is used as an index
	 * into the palette for a color that is supposed to be transparent.
	 * When the resulting Palm image file is drawn onto some background,
	 * all pixels in the color pointed to by the transparency index are not
	 * supposed to be overdrawn so that the background is visisble at
	 * those places.
	 * @param newIndex the new transparency index, must be smaller than the number of entries in the palette
	 * @see #getTransparencyIndex
	 * @see #hasTransparencyIndex
	 * @see #removeTransparencyIndex
	 */
	public void setTransparencyIndex(int newIndex)
	{
		if (newIndex < 0)
		{
			throw new IllegalArgumentException("Transparency index must be 0 or larger.");
		}
		transparencyIndex = newIndex;
	}

	private void store(PixelImage image, int y, byte[] row)
	{
		if (!isRowRequired(y))
		{
			return;
		}
		y -= getBoundsY1();
		switch(bitsPerPixel)
		{
			case(1):
			{
				BilevelImage bimage = (BilevelImage)image;
				invertBilevelData(row);
				bimage.putPackedBytes(0, y, getBoundsWidth(), row, getBoundsX1() / 8, getBoundsX1() % 8);
				break;
			}
			case(2):
			{
				byte[] dest = new byte[bytesPerRow * 4];
				ArrayConverter.decodePacked2Bit(row, 0, dest, 0, bytesPerRow);
				ByteChannelImage bcimg = (ByteChannelImage)image;
				bcimg.putByteSamples(0, 0, y, getBoundsWidth(), 1, dest, getBoundsX1());
				break;
			}
			case(4):
			{
				byte[] dest = new byte[bytesPerRow * 2];
				ArrayConverter.decodePacked4Bit(row, 0, dest, 0, bytesPerRow);
				ByteChannelImage bcimg = (ByteChannelImage)image;
				bcimg.putByteSamples(0, 0, y, getBoundsWidth(), 1, dest, getBoundsX1());
				break;
			}
			case(8):
			{
				ByteChannelImage bcimg = (ByteChannelImage)image;
				bcimg.putByteSamples(0, 0, y, getBoundsWidth(), 1, row, getBoundsX1());
				break;
			}
			case(16):
			{
				ArrayConverter.decodePackedRGB565BigEndianToRGB24(
					row, getBoundsX1() * 2,
					rgb, 0,
					rgb, width,
					rgb, width * 2,
					getBoundsWidth());
				RGB24Image img = (RGB24Image)image;
				img.putByteSamples(RGBIndex.INDEX_RED, 0, y, getBoundsWidth(), 1, rgb, 0);
				img.putByteSamples(RGBIndex.INDEX_GREEN, 0, y, getBoundsWidth(), 1, rgb, width);
				img.putByteSamples(RGBIndex.INDEX_BLUE, 0, y, getBoundsWidth(), 1, rgb, width * 2);
				break;
			}
		}
	}

	public String suggestFileExtension(PixelImage image)
	{
		return ".palm";
	}
}
