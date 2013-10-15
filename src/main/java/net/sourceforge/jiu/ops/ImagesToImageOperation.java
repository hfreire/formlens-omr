/*
 * ImagesToImageOperation
 * 
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

import java.util.Vector;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * An operation that takes several input images and produces one output image.
 *
 * @author Marco Schmidt
 * @since 0.11.0
 */
public abstract class ImagesToImageOperation extends Operation
{
	private Vector inputImages = new Vector();
	private PixelImage outputImage;

	/**
	 * Constructs a new ImagesToImageOperation and initializes
	 * input images and output image to null.
	 */
	public ImagesToImageOperation()
	{
		this(null, null);
	}

	/**
	 * Constructs a new ImagesToImageOperation and initializes
	 * input images and output image to the arguments.
	 */
	public ImagesToImageOperation(Vector in, PixelImage out)
	{
		if (in != null)
		{
			for (int i = 0; i < in.size(); i++)
			{
				addInputImage((PixelImage)in.elementAt(i));
			}
		}
		setOutputImage(out);
	}

	/**
	 * Adds an image to the end of the internal list of 
	 * input images.
	 */
	public void addInputImage(PixelImage in)
	{
		inputImages.addElement(in);
	}

	/**
	 * Checks if all images have the same resolution as given by their
	 * getWidth and getHeight methods.
	 * This method will not complain if input and / or output images are not 
	 * available.
	 * @throws WrongParameterException if input and output images exist and their 
	 *  resolutions differ
	 */
	public void ensureImagesHaveSameResolution() throws WrongParameterException
	{
		if (inputImages == null || inputImages.size() < 1)
		{
			return;
		}
		PixelImage in = getInputImage(0);
		int width = in.getWidth();
		int height = in.getHeight();
		int index = 1;
		while (index < inputImages.size())
		{
			in = getInputImage(index);
			if (in.getWidth() != width)
			{
				throw new WrongParameterException("Width of images #0 and #" + index + " are not equal.");
			}
			if (in.getHeight() != height)
			{
				throw new WrongParameterException("Height of images #0 and #" + index + " are not equal.");
			}
			index++;
		}		
		PixelImage out = getOutputImage();
		if (out != null)
		{
			if (out.getWidth() != width)
			{
				throw new WrongParameterException("Width of input images #0 and output image are not equal.");
			}
			if (out.getHeight() != height)
			{
				throw new WrongParameterException("Height of input images #0 and output image are not equal.");
			}
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
	public PixelImage getInputImage(int index)
	{
		return (PixelImage)inputImages.elementAt(index);
	}

	/**
	 * Return the number of input images currently stored in this operation.
	 * @return number of images
	 */
	public int getNumInputImages()
	{
		return inputImages.size();
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
	 * Sets the output image stored in this object to the argument.
	 * Argument can be <code>null</code>.
	 * @param out the new output image of this object
	 */
	public void setOutputImage(PixelImage out)
	{
		outputImage = out;
	}
}
