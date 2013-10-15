/*
 * Gray8Image
 *
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.ByteChannelImage;
import net.sourceforge.jiu.data.GrayIntegerImage;

/**
 * Interface for grayscale images using integer samples that are eight bits large.
 * Valid sample values must lie in the interval 0 to 255 (including both of those values).
 * Like all grayscale images, implementations of this class are supposed to have one channel only.
 * Simply merges the two interfaces GrayIntegerImage and ByteChannelImage without adding new methods.
 * @author Marco Schmidt
 * @see ByteChannelImage
 * @see GrayIntegerImage
 */
public interface Gray8Image extends GrayIntegerImage, ByteChannelImage
{
}
