/*
 * RGB24Image
 *
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.ByteChannelImage;
import net.sourceforge.jiu.data.RGBIntegerImage;

/**
 * An empty interface for RGB truecolor images with integer samples 
 * that are each eight bits large (thus, 24 bits per pixel).
 * @author Marco Schmidt
 */
public interface RGB24Image extends ByteChannelImage, RGBIntegerImage
{
}
