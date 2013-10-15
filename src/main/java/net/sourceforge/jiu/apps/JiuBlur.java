/*
 * JiuBlur
 * 
 * Copyright (c) 2007 Marco Schmidt.
 * All rights reserved.
 */
package net.sourceforge.jiu.apps;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.sourceforge.jiu.color.adjustment.Contrast;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.filters.ConvolutionKernelFilter;
import net.sourceforge.jiu.gui.awt.ImageCreator;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;

/**
 * Small example command line program which loads a JPEG file,
 * applies a blur filter, increases its contrast and saves it
 * back to another JPEG file.
 * @author Marco Schmidt
 * @since 0.14.2
 */
public class JiuBlur
{
	public static void main(String[] args) throws Exception
	{
		PixelImage image = ToolkitLoader.loadAsRgb24Image("resources/images/image1.jpg");
		image = Contrast.adjust(image, 20);
		image = ConvolutionKernelFilter.filter(image, ConvolutionKernelFilter.TYPE_BLUR);
		BufferedImage awtImage = ImageCreator.convertToAwtBufferedImage(image);
		ImageIO.write(awtImage, "jpg", new File("out-image1.jpg"));
	}
}
