/*
 * PalettedImage
 *
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.Palette;

/**
 * This interface defines methods for paletted images.
 * The image data of paletted images are usually integer numbers.
 * Those numbers are index values into a list of colors called
 * the <em>palette</em> or <em>color map</em>.
 * This way, for images with few colors relatively small integers
 * can be used as samples. 
 */
public interface PalettedImage
{
	/** 
	 * Gets the palette of this image.
	 * @return palette object
	 */
	Palette getPalette();

	/** 
	 * Sets the palette of this image to the argument palette object.
	 * @param palette the new palette for this image
	 */
	void setPalette(Palette palette);
}
