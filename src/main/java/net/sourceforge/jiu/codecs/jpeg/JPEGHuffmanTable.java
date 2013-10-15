/*
 * JPEGHuffmanTable
 *
 * Copyright (c) 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.jpeg;

/**
 * Data class that stores a single Huffman table, including class 
 * (AC or DC), ID and codes for the 16 possible bit lengths from 1 to 16.
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGHuffmanTable
{
	public static final int TABLE_CLASS_AC = 1;
	public static final int TABLE_CLASS_DC = 0;

	private int id;
	private int classAcDc;
	private int[][] codes;

	private int[] huffCode;
	private int[] huffSize;
	private int lastK;

	public void createDecoderTables()
	{
		generateSizeTable();
		generateCodeTable();
		// TODO: F.15
	}

	/**
	 * Initialize huffCode from huffSize.
	 * P&M figure C.2, p. 406f.
	 */
	private void generateCodeTable()
	{
		huffCode = new int[257];
		int k = 0;
		int code = 0;
		int si = huffSize[0];
		while (true)
		{
			while (true)
			{
				huffCode[k] = code;
				code++;
				k++;
				if (huffSize[k] != si)
				{
					break;
				}
			}
			if (huffSize[k] == 0)
			{
				break;
			}
			while (true)
			{
				code <<= 1;
				si++;
				if (huffSize[k] == si)
				{
					break;
				}
			}
		}
	}

	/**
	 * Initialize huffSize and lastK from codes.
	 * P&M figure C.1, p. 405f.
	 */
	private void generateSizeTable()
	{
		huffSize = new int[257];
		int i = 1;
		int j = 1;
		int k = 0;
		while (true)
		{
			while (true)
			{
				if (j > codes[i].length)
				{
					break;
				}
				huffSize[k] = i;
				k++;
				j++;
			}
			i++;
			j = 1;
			if (i > JPEGConstants.MAX_HUFFMAN_CODE_LENGTH)
			{
				break;
			}
		}
		huffSize[k] = 0;
		lastK = k;
	}

	public int getClassAcDc()
	{
		return classAcDc;
	}

	public int[][] getCodes()
	{
		return codes;
	}

	public int getId()
	{
		return id;
	}

	public void setClassAcDc(int i)
	{
		classAcDc = i;
	}

	public void setCodes(int[][] is)
	{
		codes = is;
	}

	public void setId(int i)
	{
		id = i;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("id=");
		sb.append(id);
		sb.append("/class=");
		sb.append(classAcDc == TABLE_CLASS_AC ? "AC" : "DC");
		if (codes != null)
		{
			sb.append("/codes(length,number)=");
			for (int i = 0; i < codes.length; i++)
			{
				if (codes[i].length > 0)
				{
					sb.append(" ");
					sb.append((i+1));
					sb.append(":");
					sb.append(codes[i].length);
				}
			}
		}
		return sb.toString();
	}
}
