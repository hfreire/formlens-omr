/*
 * JiuCountColors
 * 
 * Copyright (c) 2007 Marco Schmidt.
 * All rights reserved.
 */
package net.sourceforge.jiu.apps;

import net.sourceforge.jiu.color.analysis.Histogram3DCreator;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;

/**
 * Small example command line program to count the number of
 * unique colors in image files.
 * @author Marco Schmidt
 * @since 0.14.2
 */
public class JiuCountColors
{
	public static void main(String[] args) throws Exception
	{
		String[] FILE_NAMES = {"jiu-hello-world.png", "resources/images/image1.jpg", "out-image1.jpg"};
		for (int i = 0; i < FILE_NAMES.length; i++)
		{
			RGB24Image image = ToolkitLoader.loadAsRgb24Image(FILE_NAMES[i]);
			System.out.println(FILE_NAMES[i] + "\t" + Histogram3DCreator.count(image));
		}
	}
}
