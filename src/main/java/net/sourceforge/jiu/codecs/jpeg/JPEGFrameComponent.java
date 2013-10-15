/*
 * JPEGFrameComponent
 *
 * Copyright (c) 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.jpeg;

/**
 * Data class for information on a JPEG frame component.
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGFrameComponent
{
	private int componentId;
	private int horizontalSamplingFactor;
	private int verticalSamplingFactor;
	private int quantizationTableId;

	public int getComponentId()
	{
		return componentId;
	}

	public int getHorizontalSamplingFactor()
	{
		return horizontalSamplingFactor;
	}

	public int getQuantizationTableId()
	{
		return quantizationTableId;
	}

	public int getVerticalSamplingFactor()
	{
		return verticalSamplingFactor;
	}

	public void setComponentId(int i)
	{
		componentId = i;
	}

	public void setHorizontalSamplingFactor(int i)
	{
		horizontalSamplingFactor = i;
	}

	public void setQuantizationTableId(int i)
	{
		quantizationTableId = i;
	}

	public void setVerticalSamplingFactor(int i)
	{
		verticalSamplingFactor = i;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("component id=");
		sb.append(componentId);
		sb.append(", horiz. sampling=");
		sb.append(horizontalSamplingFactor);
		sb.append(", vert. sampling=");
		sb.append(verticalSamplingFactor);
		sb.append(", quantization table=");
		sb.append(quantizationTableId);
		return sb.toString();
	}
}
