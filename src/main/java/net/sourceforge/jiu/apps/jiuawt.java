/*
 * jiuawt
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import net.sourceforge.jiu.apps.EditorState;
import net.sourceforge.jiu.gui.awt.JiuAwtFrame;
import net.sourceforge.jiu.util.SystemInfo;

/**
 * Graphical user interface application based on the AWT (Abstract
 * Windowing Toolkit, part of Java's standard runtime library since 1.0) 
 * that demonstrates features of JIU.
 * <p>
 * <h3>Memory shortage</h3>
 * One of the errors experienced most frequently with jiuawt is the 
 * 'out of memory' error.
 * Note that only whoever starts jiuawt can give it more memory by
 * giving more memory to the Java virtual machine.
 * Example:
 * <pre>java -mx300m jiu.jar</pre>
 * starts jiuawt and provides it with 300 MB of memory.
 *
 * <h3>Command line switches</h3>
 * <dl>
 * <dt><code>--dir DIRECTORY</code>
 * <dd>set working directory to <code>DIRECTORY</code>
 * <dt><code>--help</code>
 * <dd>print help screen to standard output and exit
 * <dt><code>--lang LANGUAGE</code>
 * <dd>set language to <code>LANGUAGE</code>, where <code>en</code> is English, <code>de</code> is German and <code>es</code> is Spanish
 * <dt><code>--system</code>
 * <dd>print system information to standard output and exit
 * <dt><code>--version</code>
 * <dd>print version information to standard output and exit
 * </dl>
 * @author Marco Schmidt
 */
public class jiuawt
{
	private jiuawt()
	{
	}

	/**
	 * Creates a {@link JiuAwtFrame} object.
	 * @param args program arguments, call jiuawt with <code>--help</code> as single argument to get a help screen
	 */
	public static void main(String[] args)
	{
		EditorState state = new EditorState();
		int index = 0;
		while (index < args.length)
		{
			String s = args[index++];
			if ("--dir".equals(s))
			{
				if (index == args.length)
				{
					throw new IllegalArgumentException("Directory switch must be followed by a directory name.");
				}
				state.setCurrentDirectory(args[index++]);
			}
			else
			if ("--help".equals(s))
			{
				printVersion();
				System.out.println();
				System.out.println("Usage: jiuawt [OPTIONS] [FILE]");
				System.out.println("\tFILE is the name of an image file to be loaded after start-up");
				System.out.println("\t--dir  DIRECTORY  set working directory to DIRECTORY");
				System.out.println("\t--help            print this help screen and exit");
				System.out.println("\t--lang LANGCODE   set language to LANGCODE (de=German, en=English, es=Spanish)");
				System.out.println("\t--system          print system info and exit");
				System.out.println("\t--version         print version information and exit");
				System.exit(0);
			}
			else
			if ("--lang".equals(s))
			{
				if (index == args.length)
				{
					throw new IllegalArgumentException("Language switch must be followed by language code.");
				}
				state.setStrings(args[index++]);
			}
			else
			if ("--system".equals(s))
			{
				String info = SystemInfo.getSystemInfo(state.getStrings());
				System.out.println(info);
				System.exit(0);
			}
			else
			if ("--version".equals(s))
			{
				printVersion();
				System.exit(0);
			}
			else
			{
				if (s.startsWith("-"))
				{
					throw new IllegalArgumentException("Unknown switch: " + s);
				}
				else
				{
					state.setStartupImageName(s);
				}
			}
		}
		state.ensureStringsAvailable();
		new JiuAwtFrame(state);
	}

	private static void printVersion()
	{
		System.out.println("jiuawt " + JiuInfo.JIU_VERSION);
		System.out.println("An image editing program as a demo for the JIU image processing library.");
		System.out.println("Written by Marco Schmidt.");
		System.out.println("Visit the JIU website at <" + JiuInfo.JIU_HOMEPAGE + ">.");
	}
}
