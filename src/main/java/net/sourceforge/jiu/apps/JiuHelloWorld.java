/*
 * JiuHelloWorld
 * 
 * Copyright (c) 2007 Marco Schmidt.
 * All rights reserved.
 */
package net.sourceforge.jiu.apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.PNGCodec;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.gui.awt.ImageCreator;

/**
 * Small example command line program which creates a new image,
 * prints the text <em>Hello World!</em> into it and saves it as
 * a PNG file.
 * @author Marco Schmidt
 * @since 0.14.2
 */
public class JiuHelloWorld
{
	public static void main(String[] args) throws Exception
	{
		// AWT image creation
		BufferedImage awtImage = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = awtImage.getGraphics();
		// fill image with red
		graphics.setColor(Color.RED);
		graphics.fillRect(0, 0, awtImage.getWidth(), awtImage.getHeight());
		// draw on it in white
		graphics.setColor(Color.WHITE);
		graphics.drawString("Hello World!", 5, 15);
		// conversion AWT image to JIU image
		RGB24Image jiuImage = ImageCreator.convertImageToRGB24Image(awtImage);
		// saving JIU image to file
		PNGCodec codec = new PNGCodec();
		codec.setImage(jiuImage);
		codec.appendComment("Hello World! as a text comment; " + 
			"see http://schmidt.devlib.org/jiu/introduction.html");
		codec.setFile("jiu-hello-world.png", CodecMode.SAVE);
		codec.process();
	}
}
