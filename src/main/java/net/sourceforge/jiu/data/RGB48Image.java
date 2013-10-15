/*
 * RGB48Image
 *
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.data.ShortChannelImage;

/**
 * An empty interface for RGB truecolor images with integer samples 
 * that are each sixteen bits large (thus, 48 bits per pixel).
 * @author Marco Schmidt
 * @since 0.12.0
 */
public interface RGB48Image extends ShortChannelImage, RGBIntegerImage
{
}
