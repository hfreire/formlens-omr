/*
 * ImageLoader
 *
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import java.awt.Frame;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.InvalidImageIndexException;
import net.sourceforge.jiu.codecs.WrongFileFormatException;
import net.sourceforge.jiu.codecs.BMPCodec;
import net.sourceforge.jiu.codecs.IFFCodec;
import net.sourceforge.jiu.codecs.PCDCodec;
import net.sourceforge.jiu.codecs.PNGCodec;
import net.sourceforge.jiu.codecs.PNMCodec;
import net.sourceforge.jiu.codecs.PSDCodec;
import net.sourceforge.jiu.codecs.RASCodec;
import net.sourceforge.jiu.codecs.tiff.TIFFCodec;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.gui.awt.ImageCreator;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * A convenience class with static methods to load images from files using JIU codecs.
 * The load methods of this class try to load an image with all codecs  registered with this class.
 * This includes almost every codec that resides in the <code>net.sourceforge.jiu.codecs</code> package.
 * You can register additional codecs with {@link #registerCodecClass} or remove the usage
 * of codecs with {@link #removeCodecClass}.
 * <p>
 * A Codec that cannot safely identify a file to be in the format that it supports must not be used with ImageLoader.
 * The failure to identify typically comes from the lack of magic byte sequences defined for the format.
 * In order to load such a file, use the codec manually.
 * Example: {@link PalmCodec}.
 * <p>
 * In order to load an image via {@link java.awt.Toolkit} (JPEG, PNG or GIF), use
 * {@link net.sourceforge.jiu.gui.awt.ToolkitLoader}.
 * It combines the loading features of java.awt.Toolkit and JIU's ImageLoader.
 * <h3>Usage example</h3>
 * <pre>
 * PixelImage image = null;
 * try
 * {
 *   image = ImageLoader.load("image.tif");
 * }
 * catch (Exception e)
 * {
 *   // handle exception
 * }
 * </pre>
 * @author Marco Schmidt
 */
public class ImageLoader
{
	// all elements of class String
	private static Vector fileExtensions;
	private static Vector imageCodecClasses;

	static
	{
		imageCodecClasses = new Vector();
		registerCodecClass(new BMPCodec());
		registerCodecClass(new IFFCodec());
		registerCodecClass(new PCDCodec());
		registerCodecClass(new PNGCodec());
		registerCodecClass(new PNMCodec());
		registerCodecClass(new PSDCodec());
		registerCodecClass(new RASCodec());
		registerCodecClass(new TIFFCodec());
	}

	private ImageLoader()
	{
	}

	/**
	 * Creates an instance of one of the codec classes known to ImageLoader.
	 * @param index 0-based index of codec number, maximum value is {@link #getNumCodecs}<code> - 1</code>
	 * @return new codec object or <code>null</code> if no object could be instantiated
	 */
	public static ImageCodec createCodec(int index)
	{
		ImageCodec result = null;
		if (index >= 0 && index < getNumCodecs())
		{
			Class c = (Class)imageCodecClasses.elementAt(index);
			try
			{
				Object obj = c.newInstance();
				if (obj != null && obj instanceof ImageCodec)
				{
					result = (ImageCodec)obj;
				}
			}
			catch (IllegalAccessException iae)
			{
				// ignore
			}
			catch (InstantiationException ie)
			{
				// ignore
			}
		}
		return result;
	}

	/**
	 * Returns a filename filter ({@link java.io.FilenameFilter}) that accepts files
	 * with name extensions typical for the image file formats known to ImageLoader.
	 * The filter could then be used in an file dialog like {@link java.awt.FileDialog}.
	 * <p>
	 * Note that this filter does not include file formats supported by the AWT 
	 * {@link java.awt.Toolkit} (GIF and JPEG, also PNG since Java 1.3).
	 * @return filter for image file names
	 */
	public static FilenameFilter createFilenameFilter()
	{
		return new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				if (name == null)
				{
					return false;
				}
				if (fileExtensions == null)
				{
					updateFileExtensions();
				}
				name = name.toLowerCase();
				int index = 0;
				while (index < fileExtensions.size())
				{
					String ext = (String)fileExtensions.elementAt(index++);
					if (name.endsWith(ext))
					{
						return true;
					}
				}
				return false;
			}
		};
	}

	/**
	 * Returns the number of codec classes currently known to ImageLoader.
	 * This number can be changed by registering additional codecs
	 * ({@link #registerCodecClass})
	 * or by removing codec classes ({@link #removeCodecClass}).
	 * @return number of known codec classes
	 */
	public static int getNumCodecs()
	{
		return imageCodecClasses.size();
	}

	/**
	 * Attempts to load an image from a file.
	 * @param file the file from which an image is to be loaded
	 * @return the image on success or <code>null</code> on failure
	 */
	public static PixelImage load(File file) throws
		IOException, 
		InvalidFileStructureException,
		InvalidImageIndexException,
		UnsupportedTypeException
	{
		return load(file, null);
	}

	/**
	 * Attempts to load an image from a file, notifying the
	 * argument progress listeners.
	 * @param file the file to load an image from
	 * @param listeners a Vector of ProgressListener objects to be notified 
	 * @return an instance of a class implementing {@link PixelImage}
	 */
	public static PixelImage load(File file, Vector listeners) throws 
		IOException, 
		InvalidFileStructureException,
		InvalidImageIndexException,
		UnsupportedTypeException
	{
		for (int i = 0; i < getNumCodecs(); i++)
		{
			PixelImage result = null;
			ImageCodec codec = null;
			try
			{
				codec = createCodec(i);
				System.out.println("here 1");
				codec.setFile(file, CodecMode.LOAD);
				System.out.println("here 2");
				codec.addProgressListeners(listeners);
				System.out.println("here 3");
				codec.process();
				System.out.println("here 4");
				result = codec.getImage();
				System.out.println("here 5");
				if (result != null)
				{
					System.out.println("here 6");
					return result;
				}
			}
			catch (MissingParameterException mpe)
			{
				mpe.printStackTrace();
				// ignore
			}
			catch (WrongFileFormatException wffe)
			{
				wffe.printStackTrace();
				// ignore
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
				// ignore
			}
			catch (OperationFailedException ofe)
			{
				ofe.printStackTrace();
				// ignore
				//System.out.println("codec: " + ofe);
			}
			finally
			{
				if (codec != null)
				{
					codec.close();
				}
			}
		}
		System.out.println("here null");
		return null;
	}

	/**
	 * Load an image from a file given by its name.
	 * Simply calls load(fileName, null).
	 * @param fileName name of the file from which an image is to be loaded
	 * @return the loaded image on success, null on failure
	 */
	public static PixelImage load(String fileName) throws
		IOException,
		InvalidFileStructureException,
		InvalidImageIndexException,
		UnsupportedTypeException
	{
		return load(fileName, null);
	}

	/**
	 * Attempts to load an image from the file with the given name, 
	 * using the given list of progress listeners.
	 * @param fileName name of the file from which an image is to be loaded
	 * @param listeners a list of objects implementing ProgressListener
	 * @return the loaded image
	 */
	public static PixelImage load(String fileName, Vector listeners) throws
		IOException,
		InvalidFileStructureException,
		InvalidImageIndexException,
		UnsupportedTypeException
	{
		return load(new File(fileName), listeners);
	}

	public static PixelImage loadToolkitImageUri(String uri) throws 
	IOException, 
	InvalidFileStructureException,
	InvalidImageIndexException,
	UnsupportedTypeException
	{
		try
		{
			ImageLoader loader = new ImageLoader();
			InputStream in = loader.getClass().getResourceAsStream(uri);
			if (in == null)
			{
				return null;
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int b;
			while ((b = in.read()) != -1)
			{
				out.write(b);
			}
			in.close();
			byte[] data = out.toByteArray();
			java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(data);
			MediaTracker mediaTracker = new MediaTracker(new Frame());
			mediaTracker.addImage(awtImage, 0);
			try
			{
				mediaTracker.waitForID(0);
			}
			catch (InterruptedException ie)
			{
				return null;
			}
			PixelImage image = ImageCreator.convertImageToRGB24Image(awtImage);
			return image;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Registers a codec class with ImageLoader.
	 * The argument is an instance of the class to be registered.
	 * Note that the codec class must have an empty constructor.
	 * <p>
	 * Example: let's say you have written a new codec called ACMEImageCodec.
	 * Your codec supports loading images.
	 * Then you could register it like that:
	 * <pre>
	 * ImageLoader.registerCodecClass(new ACMEImageCodec());
	 * </pre>
	 * <p>
	 * @param codec an instance of the codec class to be registered
	 */
	public static void registerCodecClass(ImageCodec codec)
	{
		if (codec == null)
		{
			return;
		}
		if (imageCodecClasses.contains(codec.getClass()))
		{
			return;
		}
		if (!codec.isLoadingSupported())
		{
			throw new IllegalArgumentException("Codec does not support loading.");
		}
		imageCodecClasses.addElement(codec.getClass());
		updateFileExtensions();
	}

	/**
	 * Removes all codec classes from the internal list of codec classes.
	 * After a call to this method, ImageLoader will not load anything unless
	 * new codecs get registered.
	 */
	public static void removeAllCodecClasses()
	{
		imageCodecClasses = new Vector();
		updateFileExtensions();
	}

	/**
	 * Removes a codec class from the internal list of codec classes.
	 * @param codec an instance of the codec class to be removed
	 */
	public static void removeCodecClass(ImageCodec codec)
	{
		if (codec == null)
		{
			return;
		}
		int index = imageCodecClasses.indexOf(codec.getClass());
		if (index != -1)
		{
			imageCodecClasses.remove(index);
			updateFileExtensions();
		}
		else
		{
			throw new IllegalArgumentException("The argument codec's class " +
				"could not be found in the internal list of codec classes.");
		}
	}

	private static void updateFileExtensions()
	{
		fileExtensions = new Vector();
		int index = 0;
		while (index < getNumCodecs())
		{
			try
			{
				ImageCodec codec = createCodec(index++);
				String[] extArray = codec.getFileExtensions();
				if (extArray != null && extArray.length > 0)
				{
					for (int i = 0; i < extArray.length; i++)
					{
						fileExtensions.addElement(extArray[i].toLowerCase());
					}
				}
			}
			catch (Exception e)
			{
			}
		}
	}
}
