/*
 * TransparencyInformation
 *
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.IntegerImage;

/**
 * An interface that represents transparency information which may be
 * available for a pixel image.
 * Transparency information describes how an image is supposed to be 
 * drawn on a pixel background (e.g. another image).
 * That way, irregularly shaped images can easily be handled by excluding
 * those pixels of a rectangular image that are not part of the image.
 * @author Marco Schmidt
 */
public interface TransparencyInformation
{
	/**
	 * Returns an image object that contains an alpha channel.
	 * The first channel of that image is supposed to be the alpha channel.
	 * @return the alpha channel image object
	 * @see #setAlphaChannelImage
	 */
	IntegerImage getAlphaChannelImage();

	/**
	 * If there is a transparency index, this method returns it.
	 * Otherwise, the return value is undefined.
	 * @return transparency index
	 * @see #setTransparencyIndex
	 */
	Integer getTransparencyIndex();

	/**
	 * Set a new alpha channel image object.
	 * @see #getAlphaChannelImage
	 */
	void setAlphaChannelImage(IntegerImage newImage);

	/**
	 * Set a new transparency value.
	 * Can be <code>null</code>.
	 * However, if the value is non-null, it must encapsulate an
	 * integer number which is 0 or larger.
	 * @param newValue new transparency index
	 * @see #getAlphaChannelImage
	 * @throws IllegalArgumentException if the argument is non-null and contains a negative value
	 */
	void setTransparencyIndex(Integer newValue);
}
