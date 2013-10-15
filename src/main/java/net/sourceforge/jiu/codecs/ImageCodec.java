/*
 * ImageCodec
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Vector;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.WrongParameterException;
import net.sourceforge.jiu.data.PixelImage;

/**
 * The base class for <em>image codecs</em>, operations to read images from or write them to streams.
 * A codec should support one file format only.
 * The word codec is derived from <em>enCOder DECoder</em>.
 * 
 * <h3>Usage</h3>
 * The codecs differ quite a bit in what they support.
 * But here are two code snippets that demonstrate how to do loading and saving in general.
 *
 * <h4>Load image</h4>
 * <pre>
 * ImageCodec codec = new BMPCodec(); // BMPCodec is just an example
 * codec.setFile("image.bmp", CodecMode.LOAD);
 * codec.process();
 * PixelImage image = codec.getImage();
 * </pre>
 *
 * <h4>Save image</h4>
 * <pre>
 * PixelImage image = ...; // the image to be saved
 * ImageCodec codec = new BMPCodec(); // BMPCodec is just an example
 * codec.setFile("image.bmp", CodecMode.SAVE);
 * codec.setImage(image);
 * codec.process();
 * </pre>
 *
 * <h3>I/O objects</h3>
 * There are several set and get methods for I/O objects, including
 * DataInput, DataOutput, InputStream, OutputStream and RandomAccessFile.
 * If you are just using the codec (and not developing one) make it easier
 * for yourself and use {@link #setFile(String, CodecMode)}.
 * That way the picking of the right type of I/O class and the creation of a 
 * buffered stream wrapper is done automatically.
 * <p>
 * Codecs have different requirements concerning I/O objects.
 * If an image is to be loaded, it's enough for some formats to linearly read
 * from an {@link java.io.InputStream} to load the image.
 * However, some formats (like TIFF) require random access.
 * <p>
 * When implementing a codec, take care that as many I/O classes as possible can be used.
 * If possible, call {@link #getInputAsDataInput} when loading and {@link #getOutputAsDataOutput}
 * when saving.
 * That way, input / output streams, RandomAccessFiles and arbitrary DataInput / DataOutput objects
 * can be used.
 * <p>
 * <h3>Mode</h3>
 * Codecs can be used to save images or load them, or both.
 * As was g; by default, no mode (of enumeration type {@link CodecMode}) 
 * is specified and {@link #getMode()} returns <code>null</code>.
 * Mode only has two possible values, {@link CodecMode#LOAD} and
 * {@link CodecMode#SAVE}.
 * In some cases, the codec can find out whether to load or save from the I/O objects
 * that were given to it; if it has an input stream, something must be loaded,
 * if it has an output stream, something is to be saved.
 * If a codec demands a {@link RandomAccessFile}, there is no way to find out
 * the mode automatically, that is why {@link #setRandomAccessFile} also has an 
 * argument of type {@link CodecMode}.
 * <p>
 * <strong>Bounds</strong>; to load or save only part of an image.
 * Defining bounds is optional; by default, the complete image is loaded
 * or saved (no bounds).
 * Using {@link #setBounds(int, int, int, int)}, one can specify the 
 * rectangle which will be loaded or saved.
 * <p>
 * <strong>PixelImage object</strong>; get and set methods for the image which is to be 
 * loaded or saved.
 * If an image is to be loaded, a PixelImage object can optionally be specified so that the image will
 * be written to that object; image type and resolution must of course match the image
 * from input.
 * Normally, the codec will create the appropriate image object
 * itself.
 * If an image is to be saved, an image object <em>must</em> be provided, otherwise there
 * is nothing to do.
 * <p>
 * <strong>Image index</strong>; the index of the image that is to be loaded (int value, default
 * is 0). For image formats that support more than one image in one stream, the index of the
 * image to be loaded (zero-based) can be specified using {@link #setImageIndex(int)}.
 *
 * <h3>Textual comments</h3>
 * Some file formats allow for the inclusion of textual comments, to
 * store a description, creator, copyright owner or anything else within the image
 * file without actually drawing that text on the image itself.
 * Some codecs support reading and writing of comments.
 *
 * <h3>Other methods</h3>
 * <p>
 * Each file format must be able to return its name ({@link #getFormatName()}) and
 * file extensions that are typical for it ({@link #getFileExtensions()}).
 * <p>
 * A related method suggests a file extension for a given PixelImage object ({@link #suggestFileExtension(PixelImage)}).
 * That method need not be implemented, the default version returns simply <code>null</code>.
 * However, it is encouraged that codec implementors provide this method as well.
 * Most file formats only have one typical extension (e. g. <code>.bmp</code>).
 * However, for a file format like PNM, the extension depends on the image type (a grayscale
 * image would end in <code>.pgm</code>, a color image in <code>.ppm</code>).
 * <p>
 * @author Marco Schmidt
 */
public abstract class ImageCodec extends Operation
{
	private int boundsX1;
	private int boundsY1;
	private int boundsX2;
	private int boundsY2;
	private boolean boundsAvail;
	private int boundsWidth;
	private int boundsHeight;
	private Vector comments;
	private int dpiX;
	private int dpiY;
	private DataInput din;
	private DataOutput dout;
	private PixelImage image;
	private int imageIndex;
	private InputStream in;
	private CodecMode mode;
	private OutputStream out;
	private RandomAccessFile raf;

	/**
	 * This constructor will be called by descendants.
	 * The bounds state is initialized to <em>no bounds</em>.
	 */
	public ImageCodec()
	{
		super();
		comments = new Vector();
		removeBounds();
	}

	/**
	 * Appends a comment to the internal list of comments.
	 * If the argument comment is non-null, it will be added to the internal
	 * list of comment strings.
	 * @param comment the comment to be added
	 */
	public void appendComment(String comment)
	{
		if (comment != null)
		{
			comments.addElement(comment);
		}
	}

	/**
	 * If bounds were defined for this codec, this method tests if the 
	 * bounds rectangle fits into the rectangle <code>(0, 0) / (width - 1, height - 1)</code>.
	 * If the bounds are incorrect, a {@link WrongParameterException} 
	 * is thrown, otherwise nothing happens.
	 * To be used within codecs that support the bounds concept.
	 */
	public void checkBounds(int width, int height) throws WrongParameterException
	{
		if (!hasBounds())
		{
			return;
		}
		int x1 = getBoundsX1();
		if (x1 >= width)
		{
			throw new WrongParameterException("Codec bounds x1 (" + x1 +
				") must be smaller than image width (" + width + ").");
		}
		int x2 = getBoundsX2();
		if (x2 >= width)
		{
			throw new WrongParameterException("Codec bounds x2 (" + x2 +
				") must be smaller than image width (" + width + ").");
		}
		int y1 = getBoundsY1();
		if (y1 >= height)
		{
			throw new WrongParameterException("Codec bounds y1 (" + y1 +
				") must be smaller than image height (" + height + ").");
		}
		int y2 = getBoundsY2();
		if (y2 >= height)
		{
			throw new WrongParameterException("Codec bounds y2 (" + y2 +
				") must be smaller than image height (" + height + ").");
		}
	}

	/**
	 * If an image object was provided to be used for loading via {@link #setImage},
	 * this method checks if its resolution is the same as the bounds' resolution.
	 * If the two differ, a {@link net.sourceforge.jiu.ops.WrongParameterException} is thrown.
	 * @throws WrongParameterException if image resolution and bounds dimension differ
	 */
	public void checkImageResolution() throws WrongParameterException
	{
		PixelImage image = getImage();
		if (image != null)
		{
			if (image.getWidth() != getBoundsWidth())
			{
				throw new WrongParameterException("Specified input image must have width equal to getBoundsWidth().");
			}
			if (image.getHeight() != getBoundsHeight())
			{
				throw new WrongParameterException("Specified input image must have height equal to getBoundsHeight().");
			}
		}
	}

	/**
	 * Calls the close method of all input and output I/O objects
	 * that were given to this object.
	 * Catches and ignores any IOException objects that may be
	 * thrown in the process.
	 * Note that not all I/O objects have a close method (e.g. {@link java.io.DataInput}
	 * and {@link java.io.DataOutput} have not).
	 */
	public void close()
	{
		try
		{
			if (in != null)
			{
				in.close();
			}
			if (out != null)
			{
				out.close();
			}
			if (raf != null)
			{
				raf.close();
			}
		}
		catch (IOException ioe)
		{
		}
	}

	/**
	 * Returns x coordinate of the upper left corner of the bounds.
	 * Bounds must have been specified using {@link #setBounds(int, int, int, int)},
	 * otherwise the return value is undefined.
	 * @return x coordinate of the upper left corner of the bounds
	 */
	public int getBoundsX1()
	{
		return boundsX1;
	}

	/**
	 * Returns x coordinate of the lower right corner of the bounds.
	 * Bounds must have been specified using {@link #setBounds(int, int, int, int)},
	 * otherwise the return value is undefined.
	 * @return x coordinate of the lower right corner of the bounds
	 */
	public int getBoundsX2()
	{
		return boundsX2;
	}

	/**
	 * Returns y coordinate of the upper left corner of the bounds.
	 * Bounds must have been specified using {@link #setBounds(int, int, int, int)},
	 * otherwise the return value is undefined.
	 * @return y coordinate of the upper left corner of the bounds
	 */
	public int getBoundsY1()
	{
		return boundsY1;
	}

	/**
	 * Returns y coordinate of the lower right corner of the bounds.
	 * Bounds must have been specified using {@link #setBounds(int, int, int, int)},
	 * otherwise the return value is undefined.
	 * @return y coordinate of the lower right corner of the bounds
	 */
	public int getBoundsY2()
	{
		return boundsY2;
	}

	/**
	 * Returns the height of the rectangle specified by bounds.
	 * Bounds must have been specified using {@link #setBounds(int, int, int, int)},
	 * otherwise the return value is undefined.
	 * This equals {@link #getBoundsY2()} - {@link #getBoundsY1()} + 1.
	 * @return height of bounds rectangle
	 */
	public int getBoundsHeight()
	{
		return boundsHeight;
	}

	/**
	 * Returns the width of the rectangle specified by bounds.
	 * Bounds must have been specified using {@link #setBounds(int, int, int, int)},
	 * otherwise the return value is undefined.
	 * This equals {@link #getBoundsX2()} - {@link #getBoundsX1()} + 1.
	 * @return width of bounds rectangle
	 */
	public int getBoundsWidth()
	{
		return boundsWidth;
	}

	/**
	 * Returns a comment from the internal list of comments.
	 * @param index the index of the comment to be returned, must be from 
	 *  <code>0</code> to {@link #getNumComments()}<code> - 1</code>; if this is not
	 *  the case, <code>null</code> will be returned
	 * @see #getNumComments
	 * @see #appendComment
	 * @see #removeAllComments
	 */
	public String getComment(int index)
	{
		if (index >= 0 && index < comments.size())
		{
			return (String)comments.elementAt(index);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns a {@link java.io.DataInput} object if one was provided 
	 * via {@link #setDataInput(DataInput)} or <code>null</code> otherwise.
	 * @return the DataInput object
	 */
	public DataInput getDataInput()
	{
		return din;
	}

	/**
	 * Returns a {@link java.io.DataOutput} object if one was provided 
	 * via {@link #setDataOutput(DataOutput)} or <code>null</code> otherwise.
	 * @return the DataInput object
	 */
	public DataOutput getDataOutput()
	{
		return dout;
	}

	/**
	 * Returns the horizontal physical resolution of the image associated
	 * with this codec.
	 * This resolution value was either retrieved from an image file or 
	 * set via {@link #setDpi(int, int)}.
	 * @return horizontal physical resolution in dpi
	 * @see #getDpiY
	 */
	public int getDpiX()
	{
		return dpiX;
	}

	/**
	 * Returns the vertical physical resolution of the image associated
	 * with this codec.
	 * This resolution value was either retrieved from an image file or 
	 * set via {@link #setDpi(int, int)}.
	 * @return horizontal physical resolution in dpi
	 * @see #getDpiX
	 */
	public int getDpiY()
	{
		return dpiY;
	}

	/** 
	 * Returns all file extensions that are typical for this file format.
	 * The default implementation in ImageCodec returns <code>null</code>.
	 * The file extension strings should include a leading dot
	 * and are supposed to be lower case (if that is allowed for
	 * the given file format).
	 * Example: <code>{".jpg", ".jpeg"}</code> for the JPEG file format.
	 * @return String array with typical file extensions
	 */
	public String[] getFileExtensions()
	{
		return null;
	}

	/**
	 * Returns the name of the file format supported by this codec.
	 * All classes extending {@link ImageCodec} must override this method.
	 * When overriding, leave out any words in a particular language so
	 * that this format name can be understood by everyone.
	 * Usually it is enough to return the format creator plus a typical 
	 * abbreviation, e.g. <code>Microsoft BMP</code> or <code>Portable Anymap (PNM)</code>.
	 * @return name of the file format supported by this codec
	 */
	public abstract String getFormatName();

	/**
	 * Returns the image object stored in this codec.
	 * This is either an image given to this object via
	 * {@link #setImage(PixelImage)} or it was created by the codec
	 * itself during a loading operation.
	 * @return PixelImage object stored in this codec
	 */
	public PixelImage getImage()
	{
		return image;
	}

	/**
	 * Returns the zero-based index of the image to be loaded.
	 * Default is zero.
	 * @return zero-based image index value
	 */
	public int getImageIndex()
	{
		return imageIndex;
	}

	/**
	 * Returns a {@link java.io.DataInput} object if one was specified
	 * using {@link #setDataInput(DataInput)}, 
	 * or creates a {@link java.io.DataInputStream} if an 
	 * {@link java.io.InputStream} was specified,
	 * or returns a {@link java.io.RandomAccessFile} if one was specified
	 * (RandomAccessFile implements DataInput).
	 * If neither of those has been given to this object, <code>null</code> is returned.
	 * @return DataInput object or <code>null</code>
	 */
	public DataInput getInputAsDataInput()
	{
		DataInput din = getDataInput();
		if (din != null)
		{
			return din;
		}
		RandomAccessFile raf = getRandomAccessFile();
		if (getMode() == CodecMode.LOAD && raf != null)
		{
			return raf;
		}
		InputStream in = getInputStream();
		if (in != null)
		{
			if (in instanceof DataInput)
			{
				return (DataInput)in;
			}
			else
			{
				return new DataInputStream(in);
			}
		}
		return null;
	}

	/**
	 * Returns an {@link java.io.InputStream} object that was given to 
	 * this codec via {@link #setInputStream(InputStream)} 
	 * (or <code>null</code> otherwise).
	 * @return InputStream object
	 */
	public InputStream getInputStream()
	{
		return in;
	}

	/**
	 * Return the <a target="_top" href="http://www.faqs.org/rfcs/rfc2045.html">MIME</a> 
	 * (Multipurpose Internet Mail Extensions) type strings for this format, or <code>null</code>
	 * if none are available.
	 * @return MIME type strings or null
	 */
	public abstract String[] getMimeTypes();

	/** 
	 * Returns the mode this codec is in.
	 * Can be <code>null</code>, so that the codec will have to find out
	 * itself what to do.
	 * @return codec mode (load or save)
	 */
	public CodecMode getMode()
	{
		return mode;
	}

	/**
	 * Returns the current number of comments in the internal comment list.
	 * @return number of comments in the internal comment list
	 */
	public int getNumComments()
	{
		return comments.size();
	}

	/**
	 * Attempts to return an output object as a {@link java.io.DataOutput} object.
	 * @return a DataOutput object or null if that was not possible
	 */
	public DataOutput getOutputAsDataOutput()
	{
		DataOutput dout = getDataOutput();
		if (dout != null)
		{
			return dout;
		}
		OutputStream out = getOutputStream();
		if (out != null)
		{
			if (out instanceof DataOutput)
			{
				return (DataOutput)out;
			}
			else
			{
				return new DataOutputStream(out);
			}
		}
		RandomAccessFile raf = getRandomAccessFile();
		if (raf != null && getMode() == CodecMode.SAVE)
		{
			return raf;
		}
		return null;
	}

	/**
	 * Returns an {@link java.io.OutputStream} object that was given to 
	 * this codec via {@link #setOutputStream(OutputStream)} 
	 * (or <code>null</code> otherwise).
	 * @return OutputStream object
	 */
	public OutputStream getOutputStream()
	{
		return out;
	}

	/**
	 * Returns a {@link java.io.RandomAccessFile} object that was given to 
	 * this codec via {@link #setRandomAccessFile(RandomAccessFile, CodecMode)} 
	 * (or <code>null</code> otherwise).
	 * @return RandomAccessFile object
	 */
	public RandomAccessFile getRandomAccessFile()
	{
		return raf;
	}

	/**
	 * Returns if bounds have been specified.
	 * @return if bounds have been specified
	 * @see #removeBounds()
	 * @see #setBounds(int, int, int, int)
	 */
	public boolean hasBounds()
	{
		return boundsAvail;
	}

	protected void initModeFromIOObjects() throws MissingParameterException
	{
		if (getMode() != null)
		{
			return;
		}
		if (getInputStream() != null || getDataInput() != null)
		{
			mode = CodecMode.LOAD;
		}
		else
		if (getOutputStream() != null || getDataOutput() != null)
		{
			mode = CodecMode.SAVE;
		}
		else
		{
			throw new MissingParameterException("No streams or files available.");
		}
	}

	/**
	 * Returns if this codec is able to load images in the file format supported by this codec.
	 * If <code>true</code> is returned this does not necessarily mean that all files in this
	 * format can be read, but at least some.
	 * @return if loading is supported
	 */
	public abstract boolean isLoadingSupported();

	/**
	 * Returns if this codec is able to save images in the file format supported by this codec.
	 * If <code>true</code> is returned this does not necessarily mean that all types files in this
	 * format can be written, but at least some.
	 * @return if saving is supported
	 */
	public abstract boolean isSavingSupported();

	/**
	 * Returns if an image row given by its number (zero-based) must be loaded
	 * in the context of the current bounds.
	 * <p>
	 * Example: if vertical bounds have been set to 34 and 37, image rows 34 to
	 * 37 as arguments to this method would result in <code>true</code>, anything
	 * else (e.g. 12 or 45) would result in <code>false</code>.
	 *
	 * @param row the number of the row to be checked
	 * @return if row must be loaded, regarding the current bounds
	 */
	public boolean isRowRequired(int row)
	{
		if (hasBounds())
		{
			return (row >= boundsY1 && row <= boundsY2);
		}
		else
		{
			return (row >= 0 && row < getImage().getHeight());
		}
	}

	/** 
	 * Returns if the tile formed by the argument coordinates 
	 * form a rectangle that overlaps with the bounds.
	 * If no bounds were defined, returns <code>true</code>.
	 * @param x1 
	 * @param y1 
	 * @param x2 
	 * @param y2 
	 * @return if the argument tile is required
	 */
	public boolean isTileRequired(int x1, int y1, int x2, int y2)
	{
		if (hasBounds())
		{
			return !
				(getBoundsY2() < y1 ||
				 getBoundsY1() > y2 ||
				 getBoundsX2() < x1 ||
				 getBoundsX1() > x2);
		}
		else
		{
			return true;
		}
	}

	/**
	 * Removes all entries from the internal list of comments.
	 */
	public void removeAllComments()
	{
		comments.removeAllElements();
	}

	/**
	 * If bounds were set using {@link #setBounds(int, int, int, int)}, these
	 * bounds are no longer regarded after the call to this method.
	 */
	public void removeBounds()
	{
		boundsAvail = false;
	}

	/**
	 * Sets the bounds of a rectangular part of the image that
	 * is to be loaded or saved, instead of the complete image.
	 */
	public void setBounds(int x1, int y1, int x2, int y2)
	{
		if (x1 < 0 || y1 < 0 || x2 < x1 || y2 < y1)
		{
			throw new IllegalArgumentException("Not a valid bounds rectangle: " +
				"x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2);
		}
		boundsX1 = x1;
		boundsY1 = y1;
		boundsX2 = x2;
		boundsY2 = y2;
		boundsAvail = true;
		boundsWidth = x2 - x1 + 1;
		boundsHeight = y2 - y1 + 1;
	}

	/**
	 * If no bounds have been set ({@link #hasBounds()} returns <code>false</code>),
	 * this method will set the bounds to <code>0, 0, width - 1, height - 1</code>.
	 * By calling this method somewhere in the codec, no distinction has to
	 * be made for the two cases <em>bounds have been defined</em> and 
	 * <em>bounds have not been defined</em>.
	 * @param width width of the image to be loaded or saved
	 * @param height height of the image to be loaded or saved
	 */
	public void setBoundsIfNecessary(int width, int height)
	{
		if (!hasBounds())
		{
			setBounds(0, 0, width - 1, height - 1);
		}
	}

	/**
	 * Specifies a DataInput object to be used for loading.
	 * @param dataInput DataInput object to be used for loading an image
	 */
	public void setDataInput(DataInput dataInput)
	{
		din = dataInput;
	}

	/**
	 * Sets a {@link java.io.DataOutput} object to be used for saving
	 * an image.
	 * @param dataOutput the object to be used for output
	 */
	public void setDataOutput(DataOutput dataOutput)
	{
		dout = dataOutput;
	}

	/**
	 * Sets the DPI values to be stored in the file to the argument values.
	 * @param horizontalDpi horizontal physical resolution in DPI (dots per inch)
	 * @param verticalDpi vertical physical resolution in DPI (dots per inch)
	 * @see #getDpiX
	 * @see #getDpiY
	 */
	public void setDpi(int horizontalDpi, int verticalDpi)
	{
		dpiX = horizontalDpi;
		dpiY = verticalDpi;
	}

	/**
	 * Gives a File object and a codec mode to this codec and attempts
	 * to initialize the appropriate I/O objects.
	 * Simply calls {@link #setFile(String, CodecMode)} with the absolute
	 * path of the File object.
	 * @param file File object for the file to be used
	 * @param codecMode defines whether an image is to be loaded from or saved to the file
	 */
	public void setFile(File file, CodecMode codecMode) throws 
		IOException, 
		UnsupportedCodecModeException
	{
		setFile(file.getAbsolutePath(), codecMode);
	}

	/**
	 * Gives a file name and codec mode to the codec which will then
	 * try to create the corresponding I/O object.
	 * The default implementation in ImageCodec creates a DataInputStream object
	 * wrapped around a BufferedInputStream wrapped around a FileInputStream for
	 * CodecMode.LOAD.
	 * Similar for CodecMode.SAVE: a DataOutputStream around a BufferedOutputStream
	 * object around a FileOutputStream object.
	 * Codecs that need different I/O objects must override this method
	 * (some codecs may need random access and thus require a RandomAccessFile object).
	 * @param fileName name of the file to be used for loading or saving
	 * @param codecMode defines whether file is to be used for loading or saving
	 */
	public void setFile(String fileName, CodecMode codecMode) throws 
		IOException, 
		UnsupportedCodecModeException
	{
		if (codecMode == CodecMode.LOAD)
		{
			if (isLoadingSupported())
			{
				setInputStream(new BufferedInputStream(new FileInputStream(fileName)));
			}
			else
			{
				throw new UnsupportedCodecModeException("Loading is not supported for this codec (" + getFormatName() + ").");
			}
		}
		else
		{
			if (isSavingSupported())
			{
				setOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
			}
			else
			{
				throw new UnsupportedCodecModeException("Saving is not supported for this codec (" + getFormatName() + ").");
			}
		}
	}

	/**
	 * Give an image to this codec to be used for loading an image into it
	 * or saving the image.
	 * @param img image object to save or to load data into
	 */
	public void setImage(PixelImage img)
	{
		image = img;
	}

	/**
	 * Sets the index of the image to be loaded to the argument value
	 * (which must be zero or larger).
	 * @param index int index value (zero-based) of the image to be loaded
	 * @throws IllegalArgumentException if the argument is negative
	 */
	public void setImageIndex(int index)
	{
		if (index < 0)
		{
			throw new IllegalArgumentException("The index must be 0 or larger.");
		}
		imageIndex = index;
	}

	/**
	 * An {@link java.io.InputStream} can be given to this codec using this method.
	 * @param inputStream InputStream object to read from
	 */
	public void setInputStream(InputStream inputStream)
	{
		in = inputStream;
	}

	/**
	 * A method to give an {@link java.io.OutputStream} to this codec to be used 
	 * for saving an image.
	 * @param outputStream the output stream to be used by this codec
	 */
	public void setOutputStream(OutputStream outputStream)
	{
		out = outputStream;
	}

	/**
	 * A method to give a {@link java.io.RandomAccessFile} to this codec to be used 
	 * for loading or saving an image.
	 * It is not possible to determine from a RandomAccessFile object whether it
	 * was opened in read-only or read-and-write mode.
	 * To let the codec know whether the object is to be used for loading or saving
	 * the second argument is of type CodecMode.
	 * @param randomAccessFile the file to be used for loading or saving
	 * @param codecMode tells the codec whether the file is to be used for loading or saving
	 */
	public void setRandomAccessFile(RandomAccessFile randomAccessFile, CodecMode codecMode)
	{
		if (randomAccessFile == null)
		{
			throw new IllegalArgumentException("Argument RandomAccessFile must be non-null.");
		}
		if (codecMode == null)
		{
			throw new IllegalArgumentException("Argument codec mode must be non-null.");
		}
		raf = randomAccessFile;
		mode = codecMode;
	}

	/**
	 * Attempts to suggest a filename extension.
	 * The type of the argument image will be taken into consideration,
	 * although this will be necessary for some file formats only (as an
	 * example, PNM has different extensions for different image types, see
	 * {@link PNMCodec}). 
	 * This default implementation always returns <code>null</code>.
	 * @param image the image that is to be written to a file
	 * @return the file extension, including a leading dot, or <code>null</code> if no file extension can be recommended
	 */
	public String suggestFileExtension(PixelImage image)
	{
		return null;
	}
}
