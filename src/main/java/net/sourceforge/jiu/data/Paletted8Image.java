/*
 * Paletted8Image
 *
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.ByteChannelImage;
import net.sourceforge.jiu.data.PalettedIntegerImage;

/**
 * An interface for classes that store paletted images with 
 * eight bit integers for each pixel.
 * @author Marco Schmidt
 * @see net.sourceforge.jiu.data.ByteChannelImage
 * @see net.sourceforge.jiu.data.PalettedIntegerImage
 */
public interface Paletted8Image extends ByteChannelImage, PalettedIntegerImage
{
}
