/*
 * ConvolutionKernelFilter
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.filters;

import net.sourceforge.jiu.data.GrayIntegerImage;
import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.ImageToImageOperation;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * Applies a convolution kernel filter to an image.
 * <h3>Supported image types</h3>
 * Only image types that store intensity samples are supported.
 * Right now, this only includes {@link net.sourceforge.jiu.data.GrayIntegerImage} and
 * {@link net.sourceforge.jiu.data.RGBIntegerImage}.
 * <h3>Usage example</h3>
 * Standard approach (set up everything yourself):
 * <pre>
 * ConvolutionKernelFilter filter = new ConvolutionKernelFilter();
 * filter.setKernel(ConvolutionKernelFilter.TYPE_SHARPEN);
 * filter.setInputImage(image);
 * filter.process();
 * PixelImage sharpenedImage = filter.getOutputImage();</pre>
 * Use static convenience method on image <code>img</code>:
 * <pre>
 * PixelImage filteredImage = ConvolutionKernelFilter.filter(img, ConvolutionKernelFilter.TYPE_BLUR);
 * </pre>
 * <h3>Credits</h3>
 * The implementation of the filter was created by members of the Java newsgroup 
 * <a href="news://de.comp.lang.java">de.comp.lang.java</a> and adapted to the JIU
 * framework by Marco Schmidt.
 * As it was done in a contest style where people improved other people's work, and even
 * more people suggested ideas, tested results and discussed the contest it is (1)
 * hard to tell who won the contest and (2) only fair to list all persons involved.
 * <p>
 * The resulting implementation is significantly faster than the
 * <a href="http://groups.yahoo.com/group/dclj/files/CONTEST/Vorschlag/">reference implementation</a>.
 * The contest was started by the posting <em>[JPEC#3] Vorschl&auml;ge</em> to de.comp.lang.java
 * by Marco Schmidt (2001-02-18) and was ended by the posting <em>[JPEC#3] Ergebnisse</em>
 * (2001-03-07).
 * A Usenet archive like <a href="http://groups.google.com">Google Groups</a> should be 
 * able to provide the postings.
 *
 * @author Bernd Eckenfels
 * @author Carl Rosenberger
 * @author Dietmar M&uuml;nzenberger
 * @author Karsten Schulz
 * @author Marco Kaiser
 * @author Marco Schmidt
 * @author Peter Luschny
 * @author Peter Schneider
 * @author Ramin Sadre
 * @author Roland Dieterich
 * @author Thilo Schwidurski
 */
public class ConvolutionKernelFilter extends ImageToImageOperation
{
	public static final int TYPE_BLUR = 0;
	public static final int TYPE_SHARPEN = 1;
	public static final int TYPE_EDGE_DETECTION = 2;
	public static final int TYPE_EMBOSS = 3;
	public static final int TYPE_PSYCHEDELIC_DISTILLATION = 4;
	public static final int TYPE_LITHOGRAPH = 5;
	public static final int TYPE_HORIZONTAL_SOBEL = 6;
	public static final int TYPE_VERTICAL_SOBEL = 7;
	public static final int TYPE_HORIZONTAL_PREWITT = 8;
	public static final int TYPE_VERTICAL_PREWITT = 9;

	private static final int[] BLUR_DATA = {1, 1, 1, 1, 1, 1, 1, 1, 1};
	private static final int[] SHARPEN_DATA = {0, -1, 0, -1, 5, -1, 0, -1, 0};
	private static final int[] EDGE_DETECTION_DATA = {-1, -1, -1, -1, 8, -1, -1, -1, -1};
	private static final int[] EMBOSS_DATA = {1, 1, 0, 1, 0, -1, 0, -1, -1};
	private static final int[] PSYCHEDELIC_DISTILLATION_DATA = {0, -1, -2, -3, -4, 0, -1,  3,  2,  1, 0, -1, 10,  2,  1, 0, -1,  3,  2,  1, 0, -1, -2, -3, -4};
	private static final int[] LITHOGRAPH_DATA = {-1, -1, -1, -1, -1, -1,-10,-10,-10, -1, -1,-10, 98,-10, -1, -1,-10,-10,-10, -1, -1, -1, -1, -1, -1};
	private static final int[] HORIZONTAL_SOBEL_DATA = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
	private static final int[] VERTICAL_SOBEL_DATA = {-1, -2, -1, 0, 0, 0, 1, 2, 1};
	private static final int[] HORIZONTAL_PREWITT_DATA = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
	private static final int[] VERTICAL_PREWITT_DATA = {-1, -1, -1, 0, 0, 0, 1, 1, 1};
	private static ConvolutionKernelData[] PREDEFINED_KERNELS = 
	{
		new ConvolutionKernelData("Blur", BLUR_DATA, 3, 3, 9, 0),
		new ConvolutionKernelData("Sharpen", SHARPEN_DATA, 3, 3, 1, 0),
		new ConvolutionKernelData("Edge detection", EDGE_DETECTION_DATA, 3, 3, 1, 0),
		new ConvolutionKernelData("Emboss", EMBOSS_DATA, 3, 3, 1, 128),
		new ConvolutionKernelData("Psychedelic Distillation", PSYCHEDELIC_DISTILLATION_DATA, 5, 5, 1, 0),
		new ConvolutionKernelData("Lithograph", LITHOGRAPH_DATA, 5, 5, 1, 0),
		new ConvolutionKernelData("Horizontal Sobel", HORIZONTAL_SOBEL_DATA, 3, 3, 1, 0),
		new ConvolutionKernelData("Vertical Sobel", VERTICAL_SOBEL_DATA, 3, 3, 1, 0),
		new ConvolutionKernelData("Horizontal Prewitt", HORIZONTAL_PREWITT_DATA, 3, 3, 1, 0),
		new ConvolutionKernelData("Vertical Prewitt", VERTICAL_PREWITT_DATA, 3, 3, 1, 0)
	};
	private int kernelBias;
	private int[] kernelData;
	private int kernelDiv;
	private int kernelHeight;
	private int kernelWidth;

	/**
	 * Copies row data from input image to buffer and replicates 
	 * samples at the left and right border.
	 */
	private void copyRow(IntegerImage srcImage, int srcChannelIndex, int rowIndex, int[] dest, int destOffset, int numBorderColumns)
	{
		/*
		row has a width of N + 1 samples at positions 0 to N
		X X 0 1 ... N Y Y
		copy byte at 0 to all X positions
		copy byte at N to all Y positions
		*/
		final int WIDTH = srcImage.getWidth();
		srcImage.getSamples(srcChannelIndex, 0, rowIndex, WIDTH, 1, dest, destOffset + numBorderColumns);
		// copy leftmost sample to X X positions
		int srcOffset = destOffset + numBorderColumns;
		int offset = numBorderColumns - 1;
		while (offset >= 0)
		{
			dest[offset--] = dest[srcOffset];
		}
		// copy rightmost sample to Y Y positions
		srcOffset = destOffset + numBorderColumns + WIDTH - 1;
		offset = srcOffset + 1;
		int n = numBorderColumns;
		while (n-- > 0)
		{
			dest[offset++] = dest[srcOffset];
		}
	}

	/**
	 * Filters argument image with argument kernel type and returns output image.
	 * Static convenience method to do filtering with one line of code:
	 * <pre>PixelImage blurredImage = ConvolutionKernelFilter.filter(in, ConvolutionKernelFilter.TYPE_BLUR);</pre>
	 */
	public static PixelImage filter(PixelImage input, int kernelType)
	{
		return filter(input, PREDEFINED_KERNELS[kernelType]);
	}

	public static PixelImage filter(PixelImage input, ConvolutionKernelData data)
	{
		ConvolutionKernelFilter op = new ConvolutionKernelFilter();
		op.setKernel(data);
		op.setInputImage(input);
		try
		{
			op.process();
			return op.getOutputImage();
		}
		catch (OperationFailedException ofe)
		{
			return null;
		}
	}

	/**
	 * Applies the kernel to one of the channels of an image.
	 * @param channelIndex index of the channel to be filtered, must be from 0 to ByteChannelImage.getNumChannels() - 1
	 */
	private void process(int channelIndex, IntegerImage in, IntegerImage out)
	{
		final int H_DIM = kernelWidth;
		final int H_DIM_2 = (H_DIM / 2);
		final int V_DIM = kernelHeight;
		final int V_DIM_2 = (V_DIM / 2);
		final int HEIGHT = in.getHeight();
		final int WIDTH = in.getWidth();
		final int NEW_WIDTH = WIDTH + 2 * H_DIM_2;
		final int NEW_HEIGHT = HEIGHT + 2 * V_DIM_2;
		final int MAX = in.getMaxSample(channelIndex);
		int processedItems = channelIndex * HEIGHT;
		final int TOTAL_ITEMS = in.getNumChannels() * HEIGHT;
		int[] src = new int[NEW_WIDTH * NEW_HEIGHT];
		// fill src with data
		for (int y = 0, offs = V_DIM_2 * NEW_WIDTH; y < HEIGHT; y++, offs += NEW_WIDTH)
		{
			copyRow(in, channelIndex, y, src, offs, H_DIM_2);
		}
		// copy row H_DIM_2 to 0 .. H_DIM_2 - 1
		int srcOffset = V_DIM_2 * NEW_WIDTH;
		for (int y = 0; y < V_DIM_2; y++)
		{
			System.arraycopy(src, srcOffset, src, y * NEW_WIDTH, NEW_WIDTH);
		}
		// copy row H_DIM_2 + HEIGHT - 1 to H_DIM_2 + HEIGHT .. 2 * H_DIM_2 + HEIGHT - 1 
		srcOffset = (HEIGHT + V_DIM_2 - 1) * NEW_WIDTH;
		for (int y = V_DIM_2 + HEIGHT; y < NEW_HEIGHT; y++)
		{
			System.arraycopy(src, srcOffset, src, y * NEW_WIDTH, NEW_WIDTH);
		}
		// do the filtering
		int count = H_DIM * V_DIM;
		final int[] kernelLine = new int[count];
		final int[] kernelD = new int[count];
		int p;
		count = 0;
		final int j = H_DIM - 1;
		for (int x = H_DIM; x-- > 0;)
		{
			for (int y = V_DIM; y-- > 0;)
			{
				int index = y * H_DIM + x;
				if (kernelData[index] != 0)
				{
					kernelLine[count] = kernelData[index];
					kernelD[count] = y * NEW_WIDTH + x;
					count++;
				}
			}
		}
		// all kernel elements are zero => nothing to do, resulting channel will be full of zeroes
		if (count == 0)
		{
			setProgress(channelIndex, in.getNumChannels());
			return;
		}
		p = (HEIGHT - 1) * NEW_WIDTH + (WIDTH - 1);
		int[] dest = new int[WIDTH];
		for (int y = HEIGHT; y-- > 0;)
		{
			for (int x = WIDTH; x-- > 0;)
			{
				int sum = 0;
				for (int i = count; i-- > 0;)
				{
					sum += (src[p + kernelD[i]] & MAX) * kernelLine[i];
				}
				sum = (sum / kernelDiv) + kernelBias;
				if (sum <= 0)
				{
					dest[x] = 0;
				}
				else
				if (sum >= MAX)
				{
					dest[x] = MAX;
				}
				else
				{
					dest[x] = sum;
				}
				// (byte)(((0xFFFFFF00 & sum) == 0) ? sum : ((sum >>> 31) - 1));
				p--;
			}
			out.putSamples(channelIndex, 0, y, WIDTH, 1, dest, 0);
			p -= j;
			setProgress(processedItems++, TOTAL_ITEMS);
		}
	}

	private void process(IntegerImage in, IntegerImage out)
	{
		for (int channelIndex = 0; channelIndex < in.getNumChannels(); channelIndex++)
		{
			process(channelIndex, in, out);
		}
	}

	public void process() throws
		MissingParameterException,
		WrongParameterException
	{
		ensureInputImageIsAvailable();
		ensureImagesHaveSameResolution();
		PixelImage in = getInputImage();
		if (in instanceof GrayIntegerImage || in instanceof RGBIntegerImage)
		{
			PixelImage out = getOutputImage();
			if (out == null)
			{
				out = (IntegerImage)in.createCompatibleImage(in.getWidth(), in.getHeight());
				setOutputImage(out);
			}
			process((IntegerImage)in, (IntegerImage)out);
		}
		else
		{
			throw new WrongParameterException("Input image must implement GrayIntegerImage or RGBIntegerImage.");
		}
	}

	/**
	 * Sets properties of the kernel to be used in this operation.
	 * @param data the kernel coefficients; this one-dimensional array stores
	 *   them in order top-to-bottom, left-to-right; the length of this
	 *   array must be at least width times height
	 * @param width the width of the kernel; must not be even
	 * @param height the height of the kernel; must not be even
	 * @param div the result is divided by this value after the addition of value
	 *  (so this value must not be zero)
	 * @param bias this value is added to the result before the division
	 */
	public void setKernel(int[] data, int width, int height, int div, int bias)
	{
		if (data == null)
		{
			throw new IllegalArgumentException("Kernel data must be non-null.");
		}
		if (width < 1)
		{
			throw new IllegalArgumentException("Kernel width must be at least 1.");
		}
		if (width % 2 != 1)
		{
			throw new IllegalArgumentException("Kernel width must not be even.");
		}
		if (height < 1)
		{
			throw new IllegalArgumentException("Kernel height must be at least 1.");
		}
		if (height % 2 != 1)
		{
			throw new IllegalArgumentException("Kernel width must not be even.");
		}
		if (data.length < width * height)
		{
			throw new IllegalArgumentException("Kernel data must have a length >= " + 
				(width * height) + " to hold " + width + " times " + height + 
				" elements.");
		}
		if (div == 0)
		{
			throw new IllegalArgumentException("The div parameter must not be zero.");
		}
		kernelData = data;
		kernelWidth = width;
		kernelHeight = height;
		kernelDiv = div;
		kernelBias = bias;
	}

	/**
	 * Sets kernel data to be used for filtering.
	 * @param ckd all information necessary for filtering
	 */
	public void setKernel(ConvolutionKernelData ckd)
	{
		setKernel(ckd.getData(), ckd.getWidth(), ckd.getHeight(), ckd.getDiv(), ckd.getBias());
	}

	/**
	 * Sets one of the predefined kernel types to be used for filtering.
	 * @param type one of the TYPE_xyz constants of this class
	 * @throws IllegalArgumentException if the argument is not a valid TYPE_xyz constant
	 */
	public void setKernel(int type)
	{
		if (type < 0 || type >= PREDEFINED_KERNELS.length)
		{
			throw new IllegalArgumentException("Not a valid type index for predefined kernels: " + type);
		}
		else
		{
			setKernel(PREDEFINED_KERNELS[type]);
		}
	}
}
