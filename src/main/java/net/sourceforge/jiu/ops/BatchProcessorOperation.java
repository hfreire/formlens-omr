/*
 * BatchProcessorOperation
 * 
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

import java.io.File;
import java.util.Vector;

/**
 * Small data class for names of directories that are to be
 * processed.
 * @author Marco Schmidt
 */
class DirectoryTree
{
	/**
	 * Input directory name, as found in the file system.
	 */
	String input;

	/**
	 * Corresponding output directory name, may not yet be in the file system.
	 */
	String output;
}

/**
 * Abstract base class to do batch processing on files and complete directory trees.
 * For a non-abstract extension of this operation, you must implement {@link #processFile}.
 * @author Marco Schmidt
 * @since 0.11.0
 */
public abstract class BatchProcessorOperation extends Operation
{
	private boolean collectErrors;
	private Vector directoryTrees = new Vector();
	private Vector errorMessages = new Vector();
	private Vector inputFileNames = new Vector();
	private String outputDirectory;
	private boolean overwrite;

	/**
	 * Adds the argument to the list of directories to be completely 
	 * processed.
	 * @param rootDirectoryName name of the root of the directory tree, can be any valid directory name
	 */
	public void addDirectoryTree(String rootDirectoryName)
	{
		addDirectoryTree(rootDirectoryName, null);
	}

	/**
	 * Adds the first argument to the list of directories to be completely 
	 * processed, writes all output files to the directory tree specified by
	 * the second argument.
	 * @param rootDirectoryName name of the root of the directory tree, can be any valid directory name
	 * @param outputRootDirectoryName name of the root of the directory tree, can be any valid directory name
	 */
	public void addDirectoryTree(String rootDirectoryName, String outputRootDirectoryName)
	{
		DirectoryTree tree = new DirectoryTree();
		tree.input = rootDirectoryName;
		tree.output = outputRootDirectoryName;
		directoryTrees.addElement(tree);
	}

	/**
	 * Adds a single name to the list of file names to be processed.
	 * @param fileName name to be added to list
	 */
	public void addInputFileName(String fileName)
	{
		inputFileNames.addElement(fileName);
	}

	/**
	 * Adds a number of file names to the internal list of file names to be processed.
	 * @param fileNameList list of file names, each object in the list must be a String
	 */
	public void addInputFileNames(Vector fileNameList)
	{
		int index = 0;
		while (index < fileNameList.size())
		{
			String fileName = (String)fileNameList.elementAt(index++);
			inputFileNames.addElement(fileName);
		}
	}

	/**
	 * Returns a list of error messages collected during the execution of {@link #process}.
	 * @return list of error messages, each object is a String
	 */
	public Vector getErrorMessages()
	{
		return errorMessages;
	}

	/**
	 * Returns the current overwrite setting.
	 * @return whether existing files are to be overwritten
	 */
	public boolean getOverwrite()
	{
		return overwrite;
	}

	/**
	 * Processes all directory trees and files given to this operation,
	 * calling {@link #processFile} on each file name.
	 */
	public void process()
	{
		// process directory trees
		int index = 0;
		while (index < directoryTrees.size())
		{
			DirectoryTree tree = (DirectoryTree)directoryTrees.elementAt(index++);
			String output = tree.output;
			if (output == null)
			{
				output = outputDirectory;
			}
			processDirectoryTree(tree.input, output);
		}
		// process single files
		index = 0;
		while (index < inputFileNames.size())
		{
			String fileName = (String)inputFileNames.elementAt(index++);
			File file = new File(fileName);
			if (!file.isFile())
			{
				if (collectErrors)
				{
					errorMessages.addElement("Cannot process \"" + fileName + "\" (not a file).");
				}
			}
			String inDir = file.getParent();
			String outDir = outputDirectory;
			if (outDir == null)
			{
				outDir = inDir;
			}
			processFile(inDir, file.getName(), outDir);
		}
	}

	private void processDirectoryTree(String fromDir, String toDir)
	{
		File fromDirFile = new File(fromDir);
		String[] entries = fromDirFile.list();
		for (int i = 0; i < entries.length; i++)
		{
			String name = entries[i];
			File entry = new File(fromDir, name);
			if (entry.isFile())
			{
				processFile(fromDir, name, toDir);
			}
			else
			if (entry.isDirectory())
			{
				File inSubDir = new File(fromDir, name);
				File outSubDir = new File(toDir, name);
				if (outSubDir.exists())
				{
					if (outSubDir.isFile())
					{
						if (collectErrors)
						{
							errorMessages.addElement("Cannot create output directory \"" + 
								outSubDir.getAbsolutePath() + "\" because a file of that name already exists.");
						}
						continue;
					}
				}
				else
				{
					if (!outSubDir.mkdir())
					{
						if (collectErrors)
						{
							errorMessages.addElement("Could not create output directory \"" + 
								outSubDir.getAbsolutePath() + "\".");
						}
						continue;
					}
				}
				processDirectoryTree(inSubDir.getAbsolutePath(), outSubDir.getAbsolutePath());
			}
		}
	}

	/**
	 * Method to be called on each file given to this operation.
	 * Non-abstract heirs of this class must implement this method to add functionality.
	 * @param inputDirectory name of directory where the file to be processed resides
	 * @param inputFileName name of file to be processed
	 * @param outputDirectory output directory for that file, need not necessarily be used
	 */
	public abstract void processFile(String inputDirectory, String inputFileName, String outputDirectory);

	/**
	 * Specifies whether error messages are supposed to be collected
	 * during the execution of {@link #process}.
	 * @param collectErrorMessages if true, error messages will be collected, otherwise not
	 * @see #getErrorMessages
	 */
	public void setCollectErrorMessages(boolean collectErrorMessages)
	{
		collectErrors = collectErrorMessages;
	}

	/**
	 * Specifies the output directory for all single files.
	 * Note that you can specify different output directories when dealing
	 * with directory trees.
	 * @param outputDirectoryName name of output directory
	 */
	public void setOutputDirectory(String outputDirectoryName)
	{
		outputDirectory = outputDirectoryName;
	}

	/**
	 * Specify whether existing files are to be overwritten.
	 * @param newValue if true, files are overwritten, otherwise not
	 * @see #getOverwrite
	 */
	public void setOverwrite(boolean newValue)
	{
		overwrite = newValue;
	}
}
