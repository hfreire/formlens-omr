/*
 * ImageLoadTester
 * 
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import java.io.File;
import net.sourceforge.jiu.codecs.ImageLoader;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.gui.awt.ToolkitLoader;
import net.sourceforge.jiu.ops.BatchProcessorOperation;

/**
 * Command line program that tries to load images from files,
 * thus testing the built-in and / or Toolkit's codecs.
 * Start the program with file names or directory names as arguments.
 * Directory names will lead to the inclusion of that complete directory tree
 * in the list of files to test.
 * Add the argument <code>--notoolkit</code> in order to keep this 
 * program from trying to load images via java.awt.Toolkit.
 * The program will process all files and print one line per file
 * that tells the result of the loading process.
 * At the end, three lines with statistics (failed / successful / total
 * loading attempts) are printed.
 * Note that you may want to give the JVM more memory if large images
 * are stored in those files you want to test.
 * Example: 
 * <pre>java -mx300m net.sourceforge.jiu.apps.ImageLoadTester *.jpg</pre>
 * This gives 300 MB to the JVM.
 * @author Marco Schmidt
 * @since 0.11.0
 */
public class ImageLoadTester extends BatchProcessorOperation
{
	private boolean useToolkit;
	private int numFailed;
	private int numSuccess;

	/**
	 * Main method of this command line program.
	 * @param args program arguments, provided by the Java Virtual Machine, must be file or directory names
	 */
	public static void main(String[] args) throws Exception
	{
		ImageLoadTester tester = new ImageLoadTester();
		boolean useToolkit = true;
		for (int i = 0; i < args.length; i++)
		{
			String name = args[i];
			if ("--notoolkit".equals(name))
			{
				useToolkit = false;
			}
			else
			{
				File file = new File(name);
				if (file.isFile())
				{
					tester.addInputFileName(name);
				}
				else
				if (file.isDirectory())
				{
					tester.addDirectoryTree(name);
				}
			}
		}
		tester.setUseToolkit(useToolkit);
		tester.process();
		int total = (tester.numFailed + tester.numSuccess);
		System.out.println("OK:     " + tester.numSuccess + " (" + tester.numSuccess * 100.0 / total + " %)");
		System.out.println("Failed: " + tester.numFailed + " (" + tester.numFailed * 100.0 / total + " %)");
		System.out.println("Total:  " + total + " (100.0 %)");
	}

	/**
	 * Tries to load an image from a file.
	 * Prints a message to standard output and increases certain internal counters
	 * for statistics.
	 * @param inputDirectory directory where the file resides
	 * @param inputFileName name of file
	 * @param outputDirectory not used, this argument is demanded by the parent class+
	 */
	public void processFile(String inputDirectory, String inputFileName, String outputDirectory)
	{
		File file = new File(inputDirectory, inputFileName);
		String name = file.getAbsolutePath();
		System.out.print(name);
		PixelImage image;
		try
		{
			if (useToolkit)
			{
				image = ToolkitLoader.loadViaToolkitOrCodecs(name, true, null);
			}
			else
			{
				image = ImageLoader.load(name);
			}
		}
		catch (Exception e)
		{
			image = null;
		}
		if (image == null)
		{
			numFailed++;
			System.out.println(" Failed.");
		}
		else
		{
			numSuccess++;
			System.out.println(" OK. Width=" + image.getWidth() + ", height=" + image.getHeight() + " pixels.");
		}
	}

	/**
	 * Specifies whether java.awt.Toolkit is supposed to be used when 
	 * trying to load an image.
	 * @param newValue boolean, true if Toolkit is to be used, false otherwise
	 */
	public void setUseToolkit(boolean newValue)
	{
		useToolkit = newValue;
	}
}
