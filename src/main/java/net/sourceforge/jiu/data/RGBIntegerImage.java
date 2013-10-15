/*
 * RGBIntegerImage
 *
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

import net.sourceforge.jiu.data.IntegerImage;
import net.sourceforge.jiu.data.RGBImage;

/**
 * An interface for RGB truecolor images that have integer samples.
 * A combination of {@link net.sourceforge.jiu.data.IntegerImage} and
 * {@link net.sourceforge.jiu.data.RGBImage}.
 *
 * @author Marco Schmidt
 * @since 0.9.0
 */
public interface RGBIntegerImage extends IntegerImage, RGBImage
{
}
