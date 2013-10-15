/*
 * JPEGFrame
 *
 * Copyright (c) 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */
package net.sourceforge.jiu.codecs.jpeg;

/**
 * Data class to store information on a JPEG frame.
 * A frame here is JPEG terminology for a complete image
 * and has nothing to do with GUI components like JFrame objects
 * in Swing. 
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGFrame
{
	private JPEGFrameComponent[] components; 
	private int numComponents;
	private int height;
	private int samplePrecision;
	private int width;

	public JPEGFrameComponent[] getComponents()
	{
		return components;
	}

	public int getHeight()
	{
		return height;
	}

	public int getNumComponents()
	{
		return numComponents;
	}

	public int getSamplePrecision()
	{
		return samplePrecision;
	}

	public int getWidth()
	{
		return width;
	}

	public void setComponents(JPEGFrameComponent[] components)
	{
		this.components = components;
	}

	public void setHeight(int i)
	{
		height = i;
	}

	public void setNumComponents(int i)
	{
		numComponents = i;
	}

	public void setSamplePrecision(int i)
	{
		samplePrecision = i;
	}

	public void setWidth(int i)
	{
		width = i;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("#components=");
		sb.append(numComponents);
		sb.append("/precision=");
		sb.append(samplePrecision);
		sb.append("/width=");
		sb.append(width);
		sb.append("/height=");
		sb.append(height);
		if (components != null)
		{
			for (int i = 0; i < components.length; i++)
			{
				JPEGFrameComponent comp = components[i];
				if (comp != null)
				{ 
					sb.append("/");
					sb.append(comp.toString());
				}
			}
		}
		return sb.toString();
	}
}
