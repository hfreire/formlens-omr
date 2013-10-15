/*
 * JPEGData
 *
 * Copyright (c) 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */
package net.sourceforge.jiu.codecs.jpeg;

import java.util.Vector;

/**
 * Data for decoding or encoding images from or to 
 * JPEG File Interchange Format (JFIF) files.
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGData
{
	private JPEGFrame frame;
	private Vector huffmanTables = new Vector();
	private Vector quantTables = new Vector();
	private Vector scans = new Vector();

	public void addQuantizationTable(JPEGQuantizationTable table)
	{
		quantTables.add(table);
	}

	public void addHuffmanTable(JPEGHuffmanTable table)
	{
		huffmanTables.add(table);
	}

	public void addScan(JPEGScan scan)
	{
		scans.add(scan);
	}

	public JPEGFrame getFrame()
	{
		return frame;
	}

	/**
	 * Return a quantization table with a given id or
	 * null on failure to find it.
	 * @param id integer id value of table
	 * @return actual table or null on failure
	 */
	public JPEGQuantizationTable getQuantizationTable(int id)
	{
		JPEGQuantizationTable table = null;
		int index = 0;
		while (index < quantTables.size())
		{
			table = (JPEGQuantizationTable)quantTables.elementAt(index++);
			if (table.getId() == id)
			{
				return table;
			}
		}
		return null;
	}

	public void setFrame(JPEGFrame newFrame)
	{
		frame = newFrame;
	}
}
