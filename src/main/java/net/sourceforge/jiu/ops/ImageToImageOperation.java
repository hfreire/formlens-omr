/*
 * ImageToImageOperation
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * An operation that acesses an input image and produces data for an output image.
 * This abstract class only provides methods to get and set those images.
 * <p>
 * Normally, an operation creates the output image itself.
 * However, an output image can be specified by the user with
 * {@link #setOutputImage}.
 * This could be done when existing image objects are to be reused.
 * <p>
 * An operation extending ImageToImageOperation must check if 
 * (1) a user-defined output image is available and
 * (2) whether that image matches the required criteria.
 * The criteria depend on the operation - example: for an operation that
 * rotates an image by 180 degrees, an output image must have the same resolution
 * as the input image and be of the same type.
 * <p>
 * If an output image is not available (case #1), the operation must create 
 * the matching output image itself.
 * It should know best what is required.
 * Very generic methods (like rotation of images by 90 degrees) must know
 * relatively little about the image.
 * They can make use of PixelImage.createCompatibleImage(int, int) and provide 
 * width and height.
 * That way, the operation works for all kinds of images, like BilevelImage,
 * Paletted8Image, Gray8Image, RGB24Image etc.
 * <p>
 * If a user-provided image does not match the required criteria, an appropriate 
 * exception (most of the time {@link WrongParameterException} will do) with a 
 * descriptive error message must be thrown.
 * In the example of the 90-degree rotation, the width of the output image must
 * be equal to the height of the input image and vice versa.
 * The types of input and output must be equal.
 * <p>
 * However, there are limits to the checks on user-provided output images.
 * As an example, a generic test could not check if a paletted output image
 * has the same palette as the input counterpart because it treats all images
 * based on IntegerImage the same.
 * <p>
 * When performing an image-to-image-operation, the input image can possibly be 
 * used as the output image.
 * This can be done
 * <ul>
 * <li>if input and output are of the same type and resolution and</li>
 * <li>if the operation needs only one input pixel to compute the output pixel
 *  at any given position.</li>
 * </ul>
 * <p>
 * Mirroring the image horizontally is an example of an operation that can be
 * implemented that way - the operation starts at the top left and at the bottom
 * right pixel, swaps them and proceeds one pixel to the right of the top left
 * pixel (and one to the left of the bottom right pixel).
 *
 * @author Marco Schmidt
 * @since 0.6.0
 */
public abstract class ImageToImageOperation extends Operation
{
	private PixelImage inputImage;
	private PixelImage outputImage;
	private boolean canInAndOutBeEqual;

	/**
	 * Creates an object of this class and sets input image 
	 * and output image to the argument values.
	 */
	public ImageToImageOperation(PixelImage in, PixelImage out)
	{
		super();
		setInputImage(in);
		setOutputImage(out);
		canInAndOutBeEqual = false;
	}

	/**
	 * Creates an object of this class and sets the input image 
	 * to the argument value, output image to <code>null</code>.
	 */
	public ImageToImageOperation(PixelImage in)
	{
		this(in, null);
	}

	/**
	 * Creates an object of this class and sets both input image 
	 * and output image to <code>null</code>.
	 */
	public ImageToImageOperation()
	{
		this(null, null);
	}

	/**
	 * Returns if input and output image are allowed to be the same object.
	 * @see #setCanInputAndOutputBeEqual
	 */
	public boolean canInputAndOutputBeEqual()
	{
		return canInAndOutBeEqual;
	}

	/**
	 * If both an input and an output image have been specified (both non-null), 
	 * this method compares their width and height properties and throws
	 * an exception if the two images do not have the same resolution.
	 * @throws WrongParameterException if input and output images exist and their 
	 *  resolutions differ
	 */
	public void ensureImagesHaveSameResolution() throws WrongParameterException
	{
		PixelImage in = getInputImage();
		PixelImage out = getOutputImage();
		if (in != null && out != null)
		{
			if (in.getWidth() != out.getWidth())
			{
				throw new WrongParameterException("Input and output image must have the same width.");
			}
			if (in.getHeight() != out.getHeight())
			{
				throw new WrongParameterException("Input and output image must have the same height.");
			}
		}
	}

	/**
	 * If {@link #getInputImage} returns <code>null</code> this
	 * method throws a {@link net.sourceforge.jiu.ops.MissingParameterException}
	 * complaining that an input image is missing.
	 * @throws MissingParameterException if no input image is available
	 */
	public void ensureInputImageIsAvailable() throws MissingParameterException
	{
		if (getInputImage() == null)
		{
			throw new MissingParameterException("Input image missing.");
		}
	}

	/**
	 * If an output image has been specified this method will compare
	 * its resolution with the argument resolution and throw an exception if the
	 * resolutions differ.
	 * If no output image has been specified nothing happens.
	 * @param width the horizontal pixel resolution that the output image must have
	 * @param height the vertical pixel resolution that the output image must have
	 * @throws WrongParameterException if the resolutions differ
	 */
	public void ensureOutputImageResolution(int width, int height) throws WrongParameterException
	{
		PixelImage out = getOutputImage();
		if (out != null)
		{
			if (out.getWidth() != width)
			{
				throw new WrongParameterException("Output image must have width " + width + " (got: " + out.getWidth() + ").");
			}
			if (out.getHeight() != height)
			{
				throw new WrongParameterException("Output image must have height " + height + " (got: " + out.getHeight() + ").");
			}
		}
	}

	/**
	 * Returns the input image stored in this object.
	 * @return input image, possibly <code>null</code>
	 */
	public PixelImage getInputImage()
	{
		return inputImage;
	}

	/**
	 * Returns the output image stored in this object.
	 * @return output image, possibly <code>null</code>
	 */
	public PixelImage getOutputImage()
	{
		return outputImage;
	}

	/**
	 * Specify if input and output image are allowed to be the same object.
	 * @see #canInputAndOutputBeEqual
	 */
	public void setCanInputAndOutputBeEqual(boolean newValue)
	{
		canInAndOutBeEqual = newValue;
	}

	/**
	 * Sets the input image stored in this object to the argument.
	 * Argument can be <code>null</code>.
	 * @param in the new input image of this object
	 */
	public void setInputImage(PixelImage in)
	{
		inputImage = in;
	}

	/**
	 * Sets the output image stored in this object to the argument.
	 * Argument can be <code>null</code>.
	 * @param out the new output image of this object
	 */
	public void setOutputImage(PixelImage out)
	{
		outputImage = out;
	}
}
