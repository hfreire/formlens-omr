/*
 * GIFCodec
 *
 * Copyright (c) 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.DataOutput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.RGBIndex;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * A codec to write Compuserve GIF (Graphics Interchange Format) files.
 * <p>
 * Only writing GIF files is supported right now.
 * Reading GIF files with JIU can be done with the {@link net.sourceforge.jiu.gui.awt.ToolkitLoader}
 * class which uses the image reader built into the Java runtime library ({@link java.awt.Toolkit} class).
 * That reader has supported GIF since Java 1.0.
 * <h3>Supported image types</h3>
 * When saving, classes implementing the following image data interfaces
 * are supported: 
 * {@link net.sourceforge.jiu.data.BilevelImage},
 * {@link net.sourceforge.jiu.data.Gray8Image} and
 * {@link net.sourceforge.jiu.data.Paletted8Image}.
 * GIF only supports up to 256 colors in an image, so
 * you will have to use one of the quantization classes to reduce
 * a truecolor image to 256 or less colors before you can save it
 * with this codec.
 * <h3>Supported I/O classes</h3>
 * This codec supports {@link java.io.OutputStream}, {@link java.io.DataOutput} 
 * and  {@link java.io.RandomAccessFile}.
 * <h3>Bounds</h3>
 * {@link net.sourceforge.jiu.codecs.ImageCodec}'s bounds concept is supported.
 * A user of this codec can specify a rectangular part of the input image
 * that will be saved instead of the complete image.
 * <h3>Comments</h3>
 * GIF - at least in its 89a version - allows for the inclusion of textual
 * comments.
 * When saving an image to a GIF file, each comment given to a codec
 * will be stored in a comment extension block of its own.
 * <h3>Usage example</h3>
 * Save an image using this codec:
 * <pre>
 * GIFCodec codec = new GIFCodec();
 * codec.appendComment("Bob and Susan at the Munich airport (2002-06-13).");
 * codec.setImage(image); // BilevelImage, Gray8Image or Paletted8Image
 * codec.setInterlacing(true);
 * codec.setFile("output.gif", CodecMode.SAVE);
 * codec.process();
 * codec.close();
 * </pre>
 * <h3>Interlaced storage</h3>
 * This codec allows creating interlaced and non-interlaced GIF files.
 * The default is non-interlaced storage.
 * Non-interlaced files store the rows top-to-bottom.
 * <p>
 * Interlaced files store the image in four passes, progressively adding 
 * rows until the complete image is stored.
 * When decoding, the progressive display of interlaced files makes it
 * supposedly quicker to find out what's displayed in the image.
 * <p>
 * On the other hand, transmission typically takes longer, because interlacing
 * often leads to slightly larger files.
 * When using interlaced mode, lines that get stored one after another
 * have some room between them in the image, so there are less similarities
 * between consecutive lines, which worsens compression ratio (compression
 * works better with a lot of similarities in the data to be compressed).
 * <h3>GIF versions</h3>
 * There are two versions of GIF, 87a and 89a.
 * In 89a, several things were added to the file format specification.
 * From the 89a features this codec only uses the possibility of storing textual comments
 * in GIF files.
 * Thus, the version used for writing depends on the return value of 
 * {@link #getNumComments()}.
 * If there is at least one comment to be written to the file, version 89a
 * will be used, 87a otherwise.
 * <h3>Licensing of the LZW algorithm</h3>
 * Unisys Corp. had a patent in several countries on the LZW algorithm used within GIF.
 * However, this patent has expired (Japan being the last country 
 * where the patent expired, on July 7th 2004)  so that LZW can be used freely. 
 * <h3>Licensing of the file format</h3>
 * GIF was defined by Compuserve.
 * In a technical document file called <code>Gif89a.txt</code> that I found 
 * somewhere on the Net they grant a royalty-free license for use of the
 * format to anyone - in order to improve the popularity of the format, I guess.
 * I don't think that it should be possible to put a file format under a copyright,
 * but all that Compuserve asks for in exchange for freely using the format
 * is the inclusion of a message.
 * So, here is that message:
 * <blockquote>
 * "The Graphics Interchange Format(c) is the Copyright property of
 *  CompuServe Incorporated. GIF(sm) is a Service Mark property of
 *  CompuServe Incorporated."
 * </blockquote>
 * <h3>Animated GIFs</h3>
 * GIF allows for animations to be stored. This codec only supports storing
 * a single image, though.
 * <h3>File format background</h3>
 * I've compiled a web page with  
 * <a target="_top" href="http://schmidt.devlib.org/file-formats/gif-image-file-format.html">technical
 * information on GIF</a>.
 * @author Marco Schmidt
 */
public class GIFCodec extends ImageCodec
{
	private static final int CODE_ARRAY_LENGTH = 5020;
	private static final int[] INTERLACING_FIRST_ROW = {0, 4, 2, 1};
	private static final int[] INTERLACING_INCREMENT = {8, 8, 4, 2};
	private static final int NUM_INTERLACING_PASSES = 4;
	private static final byte[] MAGIC_GIF87A = {71, 73, 70, 56, 55, (byte)97};
	private static final byte[] MAGIC_GIF89A = {71, 73, 70, 56, 57, (byte)97};
	private int backgroundColor;
	private byte[] block;
	private int bitOffset;
	private int bitsPerPixel;
	private int blockLength;
	private int clearCode;
	private int codeSize;
	private int[] currentCode;
	private int currentColumn;
	private int currentInterlacingPass;
	private int currentRow;
	private int endOfInformationCode;
	private boolean notFinished;
	private int freeCode;
	private IntegerImage imageToBeSaved;
	private int initialCodeSize;
	private boolean interlaced;
	private int height;
	private int maxCode;
	private int[] newCode;
	private int[] oldCode;
	private DataOutput out;
	private int processedRows;
	private int width;

	/**
	 * Returns the index of the background color.
	 * @return int value with the color (index into the palette) of the background color
	 * @see #setBackgroundColor
	 */
	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	public String[] getFileExtensions()
	{
		return new String[] {".gif"};
	}

	public String getFormatName()
	{
		return "Compuserve GIF";
	}

	public String[] getMimeTypes()
	{
		return new String[] {"image/gif"};
	}

	private int getNextSample()
	{
		int result = imageToBeSaved.getSample(currentColumn++, currentRow);
		if (currentColumn > getBoundsX2())
		{
			setProgress(processedRows++, getBoundsHeight());
			currentColumn = getBoundsX1();
			if (isInterlaced())
			{
				currentRow += INTERLACING_INCREMENT[currentInterlacingPass];
				boolean done;
				do
				{
					if (currentRow > getBoundsY2())
					{
						currentInterlacingPass++;
						if (currentInterlacingPass < NUM_INTERLACING_PASSES)
						{
							currentRow = getBoundsY1() + INTERLACING_FIRST_ROW[currentInterlacingPass];
						}
					}
					done = currentRow <= getBoundsY2() || currentInterlacingPass > NUM_INTERLACING_PASSES;
				}
				while (!done);
			}
			else
			{
				currentRow++;
			}
			notFinished = processedRows < getBoundsHeight();
		}
		return result;
	}

	private void initEncoding() throws IOException
	{
		imageToBeSaved = (IntegerImage)getImage();
		currentColumn = getBoundsX1();
		currentRow = getBoundsY1();
		processedRows = 0;
		currentInterlacingPass = 0;
		notFinished = true;
		block = new byte[255];
		currentCode = new int[CODE_ARRAY_LENGTH];
		newCode = new int[CODE_ARRAY_LENGTH];
		oldCode = new int[CODE_ARRAY_LENGTH];
		if (bitsPerPixel == 1)
		{
			initialCodeSize = 2;
		}
		else
		{
			initialCodeSize = bitsPerPixel;
		}
	}

	/**
	 * Returns if the image will be stored in interlaced (<code>true</code>) 
	 * or non-interlaced mode (<code>false</code>).
	 * @return interlacing mode
	 * @see #setInterlacing
	 */
	public boolean isInterlaced()
	{
		return interlaced;
	}

	public boolean isLoadingSupported()
	{
		return false;
	}

	public boolean isSavingSupported()
	{
		return true;
	}

	public void process() throws 
		MissingParameterException, 
		OperationFailedException
	{
		initModeFromIOObjects();
		if (getMode() == CodecMode.LOAD)
		{
			throw new OperationFailedException("Loading is not supported.");
		}
		else
		{
			save();
		}
	}

	private void resetBlock()
	{
		for (int i = 0; i < block.length; i++)
		{
			block[i] = 0;
		}
		blockLength = 0;
		bitOffset = 0;
	}

	private void resetEncoder()
	{
		codeSize = initialCodeSize + 1;
		clearCode = 1 << initialCodeSize;
		endOfInformationCode = clearCode + 1;
		freeCode = endOfInformationCode + 1;
		maxCode = (1 << codeSize) - 1;
		for (int i = 0; i < currentCode.length; i++)
		{
			currentCode[i] = 0;
		}
	}

	private void save() throws
		MissingParameterException, 
		OperationFailedException, 
		UnsupportedTypeException,
		WrongParameterException
	{
		PixelImage image = getImage();
		if (image == null)
		{
			throw new MissingParameterException("No image available for saving.");
		}
		width = image.getWidth();
		height = image.getHeight();
		setBoundsIfNecessary(width, height);
		width = getBoundsWidth();
		height = getBoundsHeight();
		if (image instanceof Paletted8Image)
		{
			Palette palette = ((Paletted8Image)image).getPalette();
			int numEntries = palette.getNumEntries();
			if (numEntries < 1 || numEntries > 256)
			{
				throw new WrongParameterException("Palette of image to be saved must have 1..256 entries.");
			}
			bitsPerPixel = 8;
			// determine minimum number of bits per pixel necessary to store image
			for (int i = 1; i <= 8; i++)
			{
				if ((1 << i) >= numEntries)
				{
					bitsPerPixel = i;
					break;
				}
			}
		}
		else
		if (image instanceof Gray8Image)
		{
			bitsPerPixel = 8;
		}
		else
		if (image instanceof BilevelImage)
		{
			bitsPerPixel = 1;
		}
		else
		{
			throw new UnsupportedTypeException("Unsupported image type: " + image.getClass().getName());
		}
		out = getOutputAsDataOutput();
		if (out == null)
		{
			throw new MissingParameterException("Output stream / random access file parameter missing.");
		}
		// now write the output stream
		try
		{
			writeStream();
		}
		catch (IOException ioe)
		{
			throw new OperationFailedException("I/O failure: " + ioe.toString());
		}
	}

	/**
	 * Specify the value of the background color.
	 * Default is <code>0</code>.
	 * @param colorIndex int value with the color (index into the palette) of the background color
	 * @see #getBackgroundColor
	 */
	public void setBackgroundColor(int colorIndex)
	{
		backgroundColor = colorIndex;
	}

	/**
	 * Specifies whether the image will be stored in interlaced mode
	 * (<code>true</code>) or non-interlaced mode (<code>false</code>).
	 * @param useInterlacing boolean, if true interlaced mode, otherwise non-interlaced mode
	 * @see #isInterlaced()
	 */
	public void setInterlacing(boolean useInterlacing)
	{
		interlaced = useInterlacing;
	}

	private void writeBlock() throws IOException
	{
		if (bitOffset > 0)
		{
			blockLength++;
		}
		if (blockLength == 0)
		{
			return;
		}
		out.write(blockLength);
		out.write(block, 0, blockLength);
		resetBlock();
	}

	private void writeCode(int code) throws IOException
	{
		int remainingBits = codeSize;
		do
		{
			int bitsFree = 8 - bitOffset;
			int bits; 
			/* bits =>  number of bits to be copied from "code" to 
			   "block[blockLength]" in this loop iteration */
			if (bitsFree < remainingBits)
			{
				bits = bitsFree;
			}
			else
			{
				bits = remainingBits;
			}
			int value = block[blockLength] & 0xff;
			value += (code & ((1 << bits) - 1)) << bitOffset;
			block[blockLength] = (byte)value;
			bitOffset += bits;
			if (bitOffset == 8)
			{
				blockLength++;
				bitOffset = 0;
				if (blockLength == 255)
				{
					writeBlock();
				}
			}
			code >>= bits;
			remainingBits -= bits;
		}
		while (remainingBits != 0);
	}

	private void writeComments() throws IOException
	{
		if (getNumComments() < 1)
		{
			return;
		}
		for (int commentIndex = 0; commentIndex < getNumComments(); commentIndex++)
		{
			String comment = getComment(commentIndex);
			byte[] data = comment.getBytes();
			out.write(0x21); // extension introducer
			out.write(0xfe); // comment label
			int offset = 0;
			while (offset < data.length)
			{
				int number = Math.min(data.length - offset, 255);
				out.write(number);
				out.write(data, offset, number);
				offset += number;
			}
			out.write(0); // zero-length block
		}
	}

	/**
	 * Writes a global header, a global palette and
	 * an image descriptor to output.
	 */
	private void writeHeader() throws IOException
	{
		// pick a GIF version; stay with 87a if possible (no comments included
		// which require 89a)
		byte[] magic;
		if (getNumComments() > 0)
		{
			magic = MAGIC_GIF89A;
		}
		else
		{
			magic = MAGIC_GIF87A;
		}
		// global header
		out.write(magic);
		writeShort(width);
		writeShort(height);
		int depth = bitsPerPixel - 1;
		/* meaning of packed byte
		   128 => there is a global palette following
		   (depth << 4) => the number of bits used for encoding
		   depth - 1 => the number of bits in the global palette (same as for encoding) */
		int packed = 128 | (depth << 4) | depth;
		out.write(packed);
		out.write(backgroundColor);
		int pixelAspectRatio = 0;
		out.write(pixelAspectRatio);

		// global palette
		writePalette();

		// write textual comments (if any) to file as extension blocks
		writeComments();

		// image descriptor
		out.write(44); // comma
		writeShort(0); // x1
		writeShort(0); // y1
		writeShort(width); // width
		writeShort(height); // height
		packed = 0;
		if (isInterlaced())
		{
			packed |= 64;
		}
		out.write(packed); // flags
	}

	private void writeImage() throws IOException
	{
		out.write(initialCodeSize);
		resetBlock();
		resetEncoder();
		writeCode(clearCode);
		int suffixChar = getNextSample();
		int prefixCode = suffixChar;
		do
		{
			suffixChar = getNextSample();
			int d = 1;
			int hashIndex = (prefixCode ^ (suffixChar << 5)) % 5003;
			boolean endInnerLoop;
			do
			{
				if (currentCode[hashIndex] == 0)
				{
					writeCode(prefixCode);
					d = freeCode;
					if (freeCode <= 4095)
					{
						oldCode[hashIndex] = prefixCode;
						newCode[hashIndex] = suffixChar;
						currentCode[hashIndex] = freeCode;
						freeCode++;
					}
					if (d > maxCode)
					{
						if (codeSize < 12)
						{
							codeSize++;
							maxCode = (1 << codeSize) - 1;
						}
						else
						{
							writeCode(clearCode);
							resetEncoder();
						}
					}
					prefixCode = suffixChar;
					break;
				}
				if (oldCode[hashIndex] == prefixCode && newCode[hashIndex] == suffixChar)
				{
					prefixCode= currentCode[hashIndex];
					endInnerLoop = true;
				}
				else
				{
					hashIndex += d;
					d += 2;
					if (hashIndex > 5003)
					{
						hashIndex -= 5003;
					}
					endInnerLoop = false;
				}
			}
			while (!endInnerLoop);
		}
		while (notFinished);
		writeCode(prefixCode);
		writeCode(endOfInformationCode);
		writeBlock();
	}

	private void writePalette() throws IOException
	{
		PixelImage image = getImage();
		if (image instanceof Paletted8Image)
		{
			Palette palette = ((Paletted8Image)image).getPalette();
			int numEntries = 1 << bitsPerPixel;
			for (int i = 0; i < numEntries; i++)
			{
				if (i < palette.getNumEntries())
				{
					out.write(palette.getSample(RGBIndex.INDEX_RED, i));
					out.write(palette.getSample(RGBIndex.INDEX_GREEN, i));
					out.write(palette.getSample(RGBIndex.INDEX_BLUE, i));
				}
				else
				{
					out.write(0);
					out.write(0);
					out.write(0);
				}
			}
		}
		else
		if (image instanceof Gray8Image)
		{
			for (int i = 0; i < 256; i++)
			{
				out.write(i);
				out.write(i);
				out.write(i);
			}
		}
		else
		if (image instanceof BilevelImage)
		{
			out.write(0);
			out.write(0);
			out.write(0);
			out.write(255);
			out.write(255);
			out.write(255);
		}
	}

	private void writeShort(int value) throws IOException
	{
		out.write(value & 0xff);
		out.write((value >> 8) & 0xff);
	}

	private void writeStream() throws IOException
	{
		initEncoding();
		writeHeader();
		writeImage();
		writeTrailer();
	}

	private void writeTrailer() throws IOException
	{
		out.write(0); // zero-length block
		out.write(59); // semicolon
	}
}
