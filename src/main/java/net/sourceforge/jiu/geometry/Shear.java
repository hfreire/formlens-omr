/*
 * Shear
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Shears an image by a given angle.
 * The angle must be larger than -90 and smaller than 90 degrees.
 * Shearing works with all image types that implement {@link net.sourceforge.jiu.data.IntegerImage}.
 * <h3>Usage example</h3>
 * <pre>
 * Shear shear = new Shear();
 * shear.setInputImage(image); // some IntegerImage
 * shear.setAngle(5.0);
 * shear.process();
 * PixelImage shearedImage = shear.getOutputImage();
 * </pre>
 * <p>
 * This is an adjusted version of Jef Poskanzer's shearing code from his ACME
 * package; see the
 * <a target="_top" href="http://www.acme.com/java/software/Acme.JPM.Filters.Shear.html">API
 * documentation page</a> of ACME's Shear class.
 *
 * @author Jef Poskanzer
 * @author Marco Schmidt
 */
public class Shear extends ImageToImageOperation
{
	private double angle;

	/**
	 * For a given image width and shearing angle this method
	 * computes the width of the resulting image.
	 * This method is static so that it can be called easily from a GUI dialog
	 * or other objects that want to present the width of a sheared image.
	 * @param oldImageWidth horizontal resolution of the image to be sheared
	 * @param height height of the image to be sheared
	 * @param angle the angle to be used in the shearing operation
	 * @return width of the sheared image
	 */
	public static int computeNewImageWidth(int oldImageWidth, int height, double angle)
	{
		double shearfac = Math.tan(angle * Math.PI / 180.0);
		if (shearfac < 0.0)
		{
			shearfac = -shearfac;
		}
		return (int)(height * shearfac + oldImageWidth + 0.999999);
	}

	/**
	 * Returns the angle associated with this shearing operation object.
	 * @return shearing angle, between -90 and 90
	 * @see #setAngle
	 */
	public double getAngle()
	{
		return angle;
	}

	private void process(IntegerImage in, IntegerImage out)
	{
		final int WIDTH = in.getWidth();
		final int HEIGHT = in.getHeight();
		int totalItems = in.getNumChannels() * HEIGHT;
		int processedItems = 0;
		double angle = getAngle() * Math.PI / 180.0;
		double shearfac = Math.tan(angle);
		if (shearfac < 0.0)
		{
			shearfac = -shearfac;
		}
		int NEW_WIDTH = (int)(HEIGHT * shearfac + WIDTH + 0.999999);
		if (out == null)
		{
			out = (IntegerImage)in.createCompatibleImage(NEW_WIDTH, HEIGHT);
			setOutputImage(out);
		}
		for (int c = 0; c < in.getNumChannels(); c++)
		{
			for (int y = 0; y < HEIGHT; y++)
			{
				double new0;
				if (angle > 0.0)
				{
					new0 = y * shearfac;
				}
				else
				{
					new0 = (HEIGHT - y) * shearfac;
				}
				int intnew0 = (int)new0;
				double fracnew0 = new0 - intnew0;
				double omfracnew0 = 1.0 - fracnew0;
				int prev = 0;
				for (int x = 0; x < WIDTH; x++)
				{
					int value = in.getSample(c, x, y);
					out.putSample(c, intnew0 + x, y, (int)(fracnew0 * prev + omfracnew0 * value));
					prev = value;
				}
				out.putSample(c, intnew0 + WIDTH, y, (int)(fracnew0 * prev));
				setProgress(processedItems++, totalItems);
			}
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		PixelImage in = getInputImage();
		ensureOutputImageResolution(computeNewImageWidth(in.getWidth(), in.getHeight(), getAngle()), in.getHeight());
		if (in instanceof IntegerImage)
		{
			process((IntegerImage)in, (IntegerImage)getOutputImage());
		}
		else
		{
			throw new WrongParameterException("Input image must implement IntegerImage.");
		}
	}

	/**
	 * Sets the angle to be used in the shearing operation to the argument value.
	 * The angle must be larger than -90.0 and smaller than 90.0.
	 * @param newAngle the angle to be used in this operation
	 * @throws IllegalArgumentException if the argument is not in the above mentioned interval
	 * @see #getAngle
	 */
	public void setAngle(double newAngle)
	{
		if (newAngle <= -90.0 || newAngle >= 90.0)
		{
			throw new IllegalArgumentException("Angle must be > -90 and < 90; got " + newAngle);
		}
		else
		{
			angle = newAngle;
		}
	}
}
