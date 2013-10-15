/*
 * dumpcodecs
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import net.sourceforge.jiu.codecs.*;

/**
 * Command line program that lists all codecs registered with ImageLoader.
 * All program arguments are ignored.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class dumpcodecs
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("Codecs known to net.sourceforge.jiu.codecs.ImageLoader");
		for (int i = 0; i < ImageLoader.getNumCodecs(); i++)
		{
			ImageCodec codec = ImageLoader.createCodec(i);
			System.out.println("(" + (i + 1) + ") " + 
				codec.getClass().getName() + " / " +
				codec.getFormatName() + " / " +
				"Saving supported=" + (codec.isSavingSupported() ? "yes" : "no"));
		}
	}
}
