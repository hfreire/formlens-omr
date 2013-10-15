/*
 * UnsharpMaskKernel
 * 
 * Copyright (c) 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

/**
 * An unsharp mask kernel to be used with {@link ConvolutionKernelFilter}.
 *
 * @author Marco Schmidt
 * @author Niels Donvil
 * @since 0.10.0
 */
public class UnsharpMaskKernel extends ConvolutionKernelData
{
	/**
	 * Creates a new unsharp mask kernel.
	 * @param level adjusts the amount of 'unsharpness', must be from 1 to 50
	 */
	public UnsharpMaskKernel(int level)
	{
		super("Unsharp mask", new int[] {1}, 1, 1, 1, 0);
		if (level < 1 || level > 50)
		{
			throw new IllegalArgumentException("The level argument must be >= 1 and <= 50.");
		}
		level = ((51 - level) * 4 ) + 20;
		setDiv(level);
		int[] data = 
		{
			 0,   0,          -1,   0,  0,
			 0,  -8,         -21,  -8,  0,
			-1, -21, level + 120, -21, -1,
			 0,  -8,         -21,  -8,  0,
			 0,   0,          -1,   0,  0
		};
		setData(data);
		setHeight(5);
		setWidth(5);
		check();
	}
}
