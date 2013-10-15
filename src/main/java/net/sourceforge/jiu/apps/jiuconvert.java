/*
 * jiuconvert
 *
 * Copyright (c) 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import net.sourceforge.jiu.codecs.BMPCodec;
import net.sourceforge.jiu.codecs.CodecMode;
import net.sourceforge.jiu.codecs.ImageCodec;
import net.sourceforge.jiu.codecs.ImageLoader;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.InvalidImageIndexException;
import net.sourceforge.jiu.codecs.PalmCodec;
import net.sourceforge.jiu.codecs.PNMCodec;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.OperationFailedException;

class JiuConvertSettings
{
	static final int VERBOSITY_QUIET = 0;
	static final int VERBOSITY_NORMAL = 1;
	static final int VERBOSITY_HIGH = 2;
	static final int FORMAT_UNKNOWN = -1;
	static final int FORMAT_BMP = 0;
	static final int FORMAT_PNM = 1;
	static final int FORMAT_PALM = 2;

	Vector inputFileNames = new Vector();
	File destinationDirectory;
	int fileFormat;
	boolean noAwtLoading;
	boolean overwrite;
	boolean testOnly;
	long time1;
	long time2;
	int verbosity;
}

abstract class Switch
{
	void check(JiuConvertSettings settings) { }
	abstract String getDescription();
	int getMinParameters() { return 0; }
	String getParameters() { return ""; }
	abstract String[] getValues();
	abstract int init(String[] args, int index, JiuConvertSettings settings);
	void setDefaults(JiuConvertSettings settings) { }
}


class BMPSwitch extends Switch
{
	void check(JiuConvertSettings settings)
	{
		if (settings.fileFormat == JiuConvertSettings.FORMAT_UNKNOWN && !settings.testOnly)
		{
			System.err.println("ERROR: You must either use test mode or provide an output file format switch.");
			System.exit(1);
		}
	}
	String[] getValues() { return new String[] {"-b", "--bmp"}; }
	String getDescription() { return "write Windows BMP output"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.fileFormat = JiuConvertSettings.FORMAT_BMP;
		return index;
	}
	void setDefaults(JiuConvertSettings settings) { settings.fileFormat = JiuConvertSettings.FORMAT_UNKNOWN; }
}

class DestinationDirectorySwitch extends Switch
{
	int getMinParameters() { return 1; }
	String getDescription() { return "write output files to directory DIR"; }
	String getParameters() { return "DIR"; }
	String[] getValues() { return new String[] {"-d", "--destdir"}; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		String name = args[index++];
		File dir = new File(name);
		if (!dir.exists())
		{
			System.err.println("Directory " + name + " does not exist.");
			System.exit(1);
		}
		if (!dir.isDirectory())
		{
			System.err.println(name + " does not seem to be a directory.");
			System.exit(1);
		}
		settings.destinationDirectory = dir;
		return index;
	}
	void setDefaults(JiuConvertSettings settings) { settings.destinationDirectory = new File("."); }
}

class NoAwtLoadingSwitch extends Switch
{
	String[] getValues() { return new String[] {"--noawtloading"}; }
	String getDescription() { return "never use AWT Toolkit to load images"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.noAwtLoading = true;
		return index;
	}
	void setDefaults(JiuConvertSettings settings) { settings.noAwtLoading = false; }
}

class OverwriteSwitch extends Switch
{
	String[] getValues() { return new String[] {"-o", "--overwrite"}; }
	String getDescription() { return "overwrite existing files"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.overwrite = true;
		return index;
	}
	void setDefaults(JiuConvertSettings settings) { settings.overwrite = false; }
}

class PalmSwitch extends Switch
{
	String[] getValues() { return new String[] {"-P", "--palm"}; }
	String getDescription() { return "write Palm native image output"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.fileFormat = JiuConvertSettings.FORMAT_PALM;
		return index;
	}
}

class PNMSwitch extends Switch
{
	String[] getValues() { return new String[] {"-p", "--pnm"}; }
	String getDescription() { return "write Portable Anymap (PNM/PBM/PGM/PPM) output"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.fileFormat = JiuConvertSettings.FORMAT_PNM;
		return index;
	}
}

class PrintHelpSwitch extends Switch
{
	static Vector switches;
	String[] getValues() { return new String[] {"-H", "--help"}; }
	String getDescription() { return "print help text to stdout and terminate"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		System.out.println("Usage: java jiuconvert [OPTIONS] [FILEs]");
		System.out.println("");
		for (int i = 0; i < switches.size(); i++)
		{
			Switch sw = (Switch)switches.elementAt(i);
			System.out.print("\t");
			String[] values = sw.getValues();
			int chars = 0;
			for (int j = 0; j < values.length; j++)
			{
				if (j > 0)
				{
					System.out.print(", ");
					chars += 2;
				}
				System.out.print(values[j]);
				chars += values[j].length();
			}
			String params = sw.getParameters();
			System.out.print(" " + params);
			chars += params.length() + 1;
			while (chars++ < 24)
			{
				System.out.print(" ");
			}
			System.out.println(sw.getDescription());
		}
		System.exit(0);
		return 0; // compiler doesn't know that System.exit(0) prevents execution ever getting here
	}
}

class PrintVersionSwitch extends Switch
{
	String[] getValues() { return new String[] {"-V", "--version"}; }
	String getDescription() { return "print version to stdout and terminate"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		System.out.println("jiuconvert " + JiuInfo.JIU_VERSION);
		System.out.println("Written by Marco Schmidt.");
		System.out.println("Copyright (C) 2002, 2003, 2004, 2005 Marco Schmidt.");
		System.out.println("This is free software; see the source for copying conditions.  There is NO");
		System.out.println("warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
		System.exit(0);
		return 0; // compiler doesn't know that System.exit(0) prevents execution ever getting here
	}
}

class QuietSwitch extends Switch
{
	String[] getValues() { return new String[] {"-q", "--quiet"}; }
	String getDescription() { return "print only error messages"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.verbosity = JiuConvertSettings.VERBOSITY_QUIET;
		return index;
	}
	void setDefaults(JiuConvertSettings settings) { settings.verbosity = JiuConvertSettings.VERBOSITY_NORMAL; }
}

class TestSwitch extends Switch
{
	String[] getValues() { return new String[] {"-t", "--test"}; }
	String getDescription() { return "test loading, do not write any output files"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.testOnly = true;
		return index;
	}
	void setDefaults(JiuConvertSettings settings) { settings.testOnly = false; }
}

class VerbositySwitch extends Switch
{
	String[] getValues() { return new String[] {"-v", "--verbose"}; }
	String getDescription() { return "print extensive information to stdout"; }
	int init(String[] args, int index, JiuConvertSettings settings)
	{
		settings.verbosity = JiuConvertSettings.VERBOSITY_HIGH;
		return index;
	}
}

/**
 * A command line program to convert between file formats.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class jiuconvert
{
	// to prevent instances of this class
	private jiuconvert()
	{
	}

	private static Vector createSwitches()
	{
		Vector switches = new Vector();
		// note that the order in which the switches are added is the order in which they are displayed
		switches.addElement(new BMPSwitch());
		switches.addElement(new PalmSwitch());
		switches.addElement(new PNMSwitch());
		switches.addElement(new TestSwitch());
		switches.addElement(new NoAwtLoadingSwitch());
		switches.addElement(new OverwriteSwitch());
		switches.addElement(new DestinationDirectorySwitch());
		switches.addElement(new QuietSwitch());
		switches.addElement(new VerbositySwitch());
		switches.addElement(new PrintHelpSwitch());
		switches.addElement(new PrintVersionSwitch());
		return switches;
	}

	private static void exit(JiuConvertSettings settings, int exitCode)
	{
		settings.time2 = System.currentTimeMillis();
		println(JiuConvertSettings.VERBOSITY_NORMAL, settings, "Time: " + ((settings.time2 - settings.time1) / 1000L) + " s.");
		System.exit(exitCode);
	}

	private static JiuConvertSettings initFromArguments(String[] args)
	{
		// create switch objects
		Vector switches = createSwitches();
		PrintHelpSwitch.switches = switches;
		// set defaults
		JiuConvertSettings settings = new JiuConvertSettings();
		settings.time1 = System.currentTimeMillis();
		Hashtable switchHash = new Hashtable();
		for (int i = 0; i < switches.size(); i++)
		{
			Switch sw = (Switch)switches.elementAt(i);
			sw.setDefaults(settings);
			String[] values = sw.getValues();
			int j = 0;
			while (j < values.length)
			{
				String value = values[j++];
				if (switchHash.get(value) != null)
				{
					System.err.println("FATAL INTERNAL ERROR: Switch " + value + " is used more than once.");
					System.exit(1);
				}
				switchHash.put(value, sw);
			}
		}
		// process arguments
		int index = 0;
		while (index < args.length)
		{
			String arg = args[index++];
			Switch sw = (Switch)switchHash.get(arg);
			if (sw == null)
			{
				// maybe a switch that does not exist?
				if (arg.charAt(0) == '-')
				{
					System.err.println("Error: Unknown switch " + arg);
					System.exit(1);
				}
				// there is no switch of that name => must be a file
				File file = new File(arg);
				if (!file.exists() || !file.isFile())
				{
					System.err.println("Error: There is no file \"" + arg + "\".");
					System.exit(1);
				}
				settings.inputFileNames.addElement(arg);
			}
			else
			{
				int minParams = sw.getMinParameters();
				if (index + minParams > args.length)
				{
					System.err.println("Error: switch " + arg + " needs at least " + minParams + " parameter(s).");
					System.exit(1);
				}
				index = sw.init(args, index, settings);
			}
		}
		// now call check() on each switch
		for (int i = 0; i < switches.size(); i++)
		{
			Switch sw = (Switch)switches.elementAt(i);
			sw.check(settings);
		}
		// other checks
		if (settings.inputFileNames.size() < 1)
		{
			System.err.println("Error: You must provide at least one input file name.");
			System.exit(1);
		}
		return settings;
	}

	private static void print(int minVerbosityLevel, JiuConvertSettings settings, String message)
	{
		if (settings.verbosity >= minVerbosityLevel)
		{
			System.out.print(message);
		}
	}

	private static void println(int minVerbosityLevel, JiuConvertSettings settings, String message)
	{
		if (settings.verbosity >= minVerbosityLevel)
		{
			System.out.println(message);
		}
	}

	private static void run(JiuConvertSettings settings, String inputFileName)
	{
		String message = null;
		PixelImage image = null;
		try
		{
			image = ImageLoader.load(inputFileName, (Vector)null);
		}
		catch (InvalidImageIndexException iiie)
		{
			message = "Failed: " + iiie.toString();
		}
		catch (InvalidFileStructureException ifse)
		{
			message = "Failed: " + ifse.toString();
		}
		catch (UnsupportedTypeException ute)
		{
			message = "Failed: " + ute.toString();
		}
		catch (IOException ioe)
		{
			message = "Failed: " + ioe.toString();
		}
		if (message == null && image == null)
		{
			message = "Failed.";
		}
		if (message != null)
		{
			println(JiuConvertSettings.VERBOSITY_QUIET, settings, "\"" + inputFileName + "\" " + message);
			return;
		}
		else
		{
			print(JiuConvertSettings.VERBOSITY_NORMAL, settings, "\"" + inputFileName + "\" ");
			print(JiuConvertSettings.VERBOSITY_NORMAL, settings, "Loaded (" + 
				//ImageDescriptionCreator.getDescription(image, Locale.US, state) + 
				").");
		}
		if (settings.testOnly)
		{
			println(JiuConvertSettings.VERBOSITY_NORMAL, settings, "");
			return;
		}
		String outputFileName = inputFileName;
		String sep = System.getProperty("file.separator");
		int index = outputFileName.lastIndexOf(sep);
		if (index != -1)
		{
			outputFileName = outputFileName.substring(index + sep.length());
		}
		index = outputFileName.lastIndexOf(".");
		if (index != -1)
		{
			outputFileName = outputFileName.substring(0, index);
		}
		ImageCodec codec = null;
		switch(settings.fileFormat)
		{
			case(JiuConvertSettings.FORMAT_BMP):
			{
				codec = new BMPCodec();
				break;
			}
			case(JiuConvertSettings.FORMAT_PALM):
			{
				codec = new PalmCodec();
				break;
			}
			case(JiuConvertSettings.FORMAT_PNM):
			{
				codec = new PNMCodec();
				break;
			}
		}
		String ext = codec.suggestFileExtension(image);
		if (ext != null)
		{
			outputFileName += ext;
		}
		File outputFile = new File(settings.destinationDirectory, outputFileName);
		outputFileName = outputFile.getAbsolutePath();
		if (outputFile.exists() && !settings.overwrite)
		{
			println(JiuConvertSettings.VERBOSITY_NORMAL, settings, " File \"" + outputFileName + "\" already exists, skipping.");
		}
		codec.setImage(image);
		try
		{
			codec.setFile(outputFileName, CodecMode.SAVE);
			codec.process();
			codec.close();
			println(JiuConvertSettings.VERBOSITY_NORMAL, settings, " Wrote \"" + outputFileName + "\".");
		}
		catch (IOException ioe)
		{
			println(JiuConvertSettings.VERBOSITY_HIGH, settings, " I/O error writing \"" + outputFileName + "\": " + ioe.toString());
		}
		catch (OperationFailedException ofe)
		{
			println(JiuConvertSettings.VERBOSITY_HIGH, settings, " Error writing \"" + outputFileName + "\": " + ofe.toString());
		}
	}

	private static void run(JiuConvertSettings settings)
	{
		int index = 0;
		while (index < settings.inputFileNames.size())
		{
			String fileName = (String)settings.inputFileNames.elementAt(index++);
			run(settings, fileName);
		}
		exit(settings, 0);
	}

	public static void main(String[] args)
	{
		JiuConvertSettings settings = initFromArguments(args);
		run(settings);
	}
}
