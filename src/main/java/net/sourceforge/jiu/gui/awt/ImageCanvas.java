/*
 * ImageCanvas
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
//import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
//import java.awt.RenderingHints;
import java.awt.ScrollPane;
import net.sourceforge.jiu.apps.EditorState;

/**
 * An AWT canvas that displays an {@link java.awt.Image} object.
 * Capable to display at arbitrary zooming levels.
 * Does not use rendering hints because they require Java 1.2 or higher
 * (although bilinear and bicubic interpolation usually improve display quality
 * when zooming at the cost of slowing down image drawing).
 *
 * @author Marco Schmidt
 */
public class ImageCanvas extends Canvas
{
	private Image image;
	private int width;
	private int height;
	private int scaledWidth;
	private int scaledHeight;
	private double zoomFactorX = 1.0;
	private double zoomFactorY = 1.0;
	private boolean zoomToFit;
	private ScrollPane myScrollPane;

	public ImageCanvas(ScrollPane scrollPane)
	{
		myScrollPane = scrollPane;
		//interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
	}

	public void computeZoomToFitSize()
	{
		if (!zoomToFit || myScrollPane == null)
		{
			return;
		}
		Dimension scrollPaneSize = myScrollPane.getSize();
		int maxWidth = scrollPaneSize.width;
		int maxHeight = scrollPaneSize.height;
		double paneRatio = (double)maxWidth / (double)maxHeight;
		double imageRatio = (double)width / (double)height;
		if (paneRatio < imageRatio)
		{
			scaledWidth = maxWidth;
			scaledHeight = (int)(scaledWidth * imageRatio);
		}
		else
		{
			scaledHeight = maxHeight;
			scaledWidth = (int)(scaledHeight * imageRatio);
		}
		scaledHeight--;
		scaledWidth--;
		zoomFactorX = (double)scaledWidth / (double)width;
		zoomFactorY = zoomFactorX;
	}

	public int getZoomPercentageX()
	{
		return (int)(zoomFactorX * 100.0);
	}

	public int getZoomPercentageY()
	{
		return (int)(zoomFactorY * 100.0);
	}

	public Dimension getPreferredSize()
	{
		return new Dimension(scaledWidth, scaledHeight);
	}

	/**
	 * Draws image to upper left corner.
	 */
	public void paint(Graphics g)
	{
		if (image == null)
		{
			super.paint(g);
		}
		else
		{
			Rectangle rect = getBounds();
			int canvasWidth = rect.width;
			int canvasHeight = rect.height;
			int x1 = 0;
			int y1 = 0;
			if (canvasWidth > scaledWidth)
			{
				x1 = (canvasWidth - scaledWidth) / 2;
			}
			if (canvasHeight > scaledHeight)
			{
				y1 = (canvasHeight - scaledHeight) / 2;
			}
			if (canvasHeight > canvasWidth || canvasHeight > scaledHeight)
			{
				super.paint(g);
			}
			/* commented because Graphics2D requires Java 1.2+
			if (g instanceof Graphics2D)
			{
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
			}
			*/
			g.drawImage(image, x1, y1, scaledWidth, scaledHeight, this);
		}
	}

	/**
	 * Specifies a new Image object to be displayed in this canvas.
	 * @param newImage the new Image object, potentially null
	 */
	public void setImage(Image newImage)
	{
		image = newImage;
		width = image.getWidth(this);
		height = image.getHeight(this);
		scaledWidth = (int)(width * zoomFactorX);
		scaledHeight = (int)(height * zoomFactorY);
		/*zoomFactorX = 1.0;
		zoomFactorY = 1.0;*/
		setSize(scaledWidth, scaledHeight);
		validate();
	}

	/**
	 * Sets both zoom factors to <code>1.0</code>.
	 */
	public void setOriginalSize()
	{
		setZoomFactor(1.0);
	}

	public double getZoomFactorX()
	{
		return zoomFactorX;
	}

	public double getZoomFactorY()
	{
		return zoomFactorY;
	}

	/**
	 * Sets the interpolation type used for drawing to the argument
	 * (must be one of the
	 * INTERPOLATION_xyz constants of EditorState), but does not
	 * do a redraw.
	 */
	public void setInterpolation(int newType)
	{
		switch(newType)
		{
			case(EditorState.INTERPOLATION_BICUBIC):
			{
				//interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
				break;
			}
			case(EditorState.INTERPOLATION_BILINEAR):
			{
				//interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
				break;
			}
			case(EditorState.INTERPOLATION_NEAREST_NEIGHBOR):
			{
				//interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
				break;
			}
		}
	}

	public void setZoomFactor(double newZoomFactor)
	{
		setZoomFactors(newZoomFactor, newZoomFactor);
	}

	public void setZoomFactors(double newZoomFactorX, double newZoomFactorY)
	{
		if (newZoomFactorX <= 0.0 || newZoomFactorY <= 0.0)
		{
			throw new IllegalArgumentException("Zoom factors must be larger than 0.0.");
		}
		zoomFactorX = newZoomFactorX;
		zoomFactorY = newZoomFactorY;
		scaledWidth = (int)(width * zoomFactorX);
		scaledHeight = (int)(height * zoomFactorY);
		setSize(scaledWidth, scaledHeight);
		myScrollPane.validate();
	}

	public void setZoomToFit(boolean newValue)
	{
		zoomToFit = newValue;
		validate();
	}

	/**
	 * Simply calls {@link #paint(Graphics)} with the argument.
	 * @param g Graphics context
	 */
	public void update(Graphics g)
	{
		paint(g);
	}
}
