/*
 * ConvolutionKernelData
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

/**
 * This class encapsulates the information for a specific convolution kernel filter.
 * An object of this class is used in combination with {@link ConvolutionKernelFilter}.
 * Several kernel data objects are predefined in that class.
 *
 * @author Marco Schmidt
 * @see ConvolutionKernelFilter
 */
public class ConvolutionKernelData
{
	private int[] data;
	private int width;
	private int height;
	private int div;
	private int bias;
	private String name;

	/**
	 * Creates a new kernel from the arguments.
	 * Calls the various set methods to actually store these arguments.
	 */
	public ConvolutionKernelData(String name, int[] data, int width, int height, int div, int bias)
	{
		setName(name);
		setData(data);
		setWidth(width);
		setHeight(height);
		setDiv(div);
		setBias(bias);
		check();
	}

	/**
	 * Checks if this kernel's data is valid and throws an IllegalArgumentException if anything
	 * is wrong.
	 * Otherwise, does nothing.
	 */
	public void check()
	{
		if (data.length < width * height)
		{
			throw new IllegalArgumentException("Kernel data array must have at least width * height elements.");
		}
	}

	/**
	 * Returns this kernel's bias value.
	 * See {@link ConvolutionKernelFilter} for an explanation of this and other kernel properties.
	 * @see #setBias
	 */
	public int getBias()
	{
		return bias;
	}

	/**
	 * Returns this kernel's div value.
	 * Must not be <code>0</code>.
	 * See {@link ConvolutionKernelFilter} for an explanation of this and other kernel properties.
	 * @see #setDiv
	 */
	public int getDiv()
	{
		return div;
	}

	/**
	 * Returns the kernel data.
	 * See {@link ConvolutionKernelFilter} for an explanation of this and other kernel properties.
	 * @see #setData
	 */
	public int[] getData()
	{
		return data;
	}

	/**
	 * Returns this kernel's height, an odd positive number.
	 * See {@link ConvolutionKernelFilter} for an explanation of this and other kernel properties.
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * Returns this kernel's name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns this kernel's width, an odd positive number.
	 * See {@link ConvolutionKernelFilter} for an explanation of this and other kernel properties.
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * Set new bias value.
	 * See {@link ConvolutionKernelFilter} for an explanation of this and other kernel properties.
	 */
	public void setBias(int newBias)
	{
		bias = newBias;
	}

	/**
	 * Sets the data array to be used in this kernel.
	 * Must have at least getWidth() times getHeight() elements - however,
	 * this constraint is not checked in this method (setting
	 * width and height may happen later).
	 * Call {@link #check} 
	 * @param newData
	 */
	public void setData(int[] newData)
	{
		if (newData == null)
		{
			throw new IllegalArgumentException("The data array must not be null.");
		}
		if (newData.length < 1)
		{
			throw new IllegalArgumentException("The data array must have a length of at least 1.");
		}
		data = newData;
	}

	public void setDiv(int newDiv)
	{
		if (newDiv == 0)
		{
			throw new IllegalArgumentException("Div value must not be 0.");
		}
		div = newDiv;
	}

	public void setHeight(int newHeight)
	{
		if (newHeight < 1)
		{
			throw new IllegalArgumentException("Height must be 1 or larger.");
		}
		if ((newHeight % 2) == 0)
		{
			throw new IllegalArgumentException("Height must not be an even number.");
		}
		height = newHeight;
	}

	public void setName(String newName)
	{
		name = newName;
	}

	public void setWidth(int newWidth)
	{
		if (newWidth < 1)
		{
			throw new IllegalArgumentException("Width must be 1 or larger.");
		}
		if ((newWidth % 2) == 0)
		{
			throw new IllegalArgumentException("Width must not be an even number.");
		}
		width = newWidth;
	}
}
