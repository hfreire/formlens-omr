/*
 * ToolkitLoader
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Vector;
import net.sourceforge.jiu.codecs.ImageLoader;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.gui.awt.ImageCreator;
import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * This class loads an instance of {@link java.awt.Image} using
 * {@link java.awt.Toolkit}'s built-in loading capabilities and 
 * converts it to {@link net.sourceforge.jiu.data.RGB24Image} using
 * {@link net.sourceforge.jiu.gui.awt.ImageCreator}.
 * <p>
 * Supported file formats are JPEG and GIF.
 * PNG is supported since Java 1.3.
 * I have heard that XBM are supposedly loaded as well.
 * I don't know that format and haven't tested this functionality.
 * <p>
 * In addition, this class can also use JIU's built-in codecs from
 * this class.
 * <h3>Usage examples</h3>
 * Load an image using Java's own {@link java.awt.Toolkit} class:
 * <pre>
 * RGB24Image rgbImage = ToolkitLoader.loadAsRgb24Image("flower.jpg");
 * </pre>
 * This will only load images from files in formats that are supported
 * by Toolkit - normally, that only includes JPEG, GIF and since Java 1.3 PNG.
 * A potential problem of this approach is that Toolkit always delivers RGB
 * data, even if the image file only contains a black and white image.
 * In order to get an image object of the &quot;real&quot; type, try
 * JIU's {@link net.sourceforge.jiu.color.reduction.AutoDetectColorType} with
 * <code>rgbImage</code> (if you follow the link you will get a usage example
 * for that class as well).
 *
 * <h3>Known issues</h3>
 * If you are using this class to load JPEGs, GIFs or PNGs,
 * an AWT background thread is started (as for a normal AWT GUI application).
 * Before Java 1.4 there was a bug that kept the thread running although
 * an application had reached the end of its execution (by getting to the
 * end of the main(String[]) method).
 * If you experience this problem, either update to a 1.4+ JDK or
 * follow the advice given at <a href="http://www.jguru.com/faq/view.jsp?EID=467061" target="_top">jguru.com</a>
 * and call <code>System.exit(0);</code>.
 * @author Marco Schmidt
 */
public class ToolkitLoader
{
	private static Frame frame = null;

	/**
	 * This class has only static methods and fields, so there is no need to instantiate it.
	 * That's why the empty constructor is hidden here.
	 */
	private ToolkitLoader()
	{
	}

	/**
	 * Loads an image from a file using the AWT's built-in loader.
	 * Returns that image as an AWT {@link java.awt.Image} object.
	 * This method does nothing more than call {@link java.awt.Toolkit#getImage(String)},
	 * wait for it using a {@link java.awt.MediaTracker} and return
	 * the resulting image.
	 *
	 * @param fileName name of the image file
	 * @return the image as AWT image object
	 */
	public static Image load(String fileName)
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.getImage(fileName);
		if (frame == null)
		{
			frame = new Frame();
		}
		MediaTracker mt = new MediaTracker(frame);
		mt.addImage(image, 0);
		try
		{
			mt.waitForID(0);
		}
		catch (InterruptedException e)
		{
			return null;
		}
		return image;
	}

	/**
	 * Loads an image from a file using the AWT's built-in loader and
	 * converts the image to a {@link net.sourceforge.jiu.data.RGB24Image}
	 * object.
	 * First calls {@link #load} with the filename, then converts 
	 * the loaded image using {@link ImageCreator#convertImageToRGB24Image}.
	 * @param fileName name of the file from which the image is to be loaded
	 * @return loaded image as {@link net.sourceforge.jiu.data.RGB24Image}
	 */
	public static RGB24Image loadAsRgb24Image(String fileName)
	{
		return ImageCreator.convertImageToRGB24Image(load(fileName));
	}

	/**
	 * Attempts to load an image from a file given by its name,
	 * using both the JIU codecs and the image loading functionality in
	 * java.awt.Toolkit.
	 * First tries JIU's codecs, then java.awt.Toolkit.
	 * Simply calls <code>loadViaToolkitOrCodecs(fileName, false);</code>.
	 * @param fileName name of the image file
	 * @return image object or <code>null</code> on failure
	 */
	public static PixelImage loadViaToolkitOrCodecs(String fileName)
	{
		return loadViaToolkitOrCodecs(fileName, false, null);
	}

	/**
	 * Attempts to load an image from a file given by its name,
	 * using both the JIU codecs and the image loading functionality in
	 * java.awt.Toolkit.
	 * The second argument determines which method is tried first,
	 * Toolkit (true) or the JIU codecs (false).
	 * Uses {@link #loadAsRgb24Image} from this class for Toolkit loading
	 * and {@link net.sourceforge.jiu.codecs.ImageLoader} for JIU's codecs.
	 * @param fileName name of the image file
	 * @return image object or <code>null</code> on failure
	 */
	public static PixelImage loadViaToolkitOrCodecs(String fileName, boolean preferToolkit, Vector progressListeners)
	{
		PixelImage result = null;
		try
		{
			if (preferToolkit)
			{
				result = loadAsRgb24Image(fileName);
				if (result == null)
				{
					result = ImageLoader.load(fileName, progressListeners);
				}
			}
			else
			{
				result = ImageLoader.load(fileName, progressListeners);
				if (result == null)
				{
					result = loadAsRgb24Image(fileName);
				}
			}
		}
		catch (OperationFailedException ofe)
		{
		}
		catch (IOException ioe)
		{
		}
		return result;
	}
}
