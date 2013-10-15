/*
 * GrayIntegerImage
 *
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.data;

/**
 * An empty interface for grayscale images which have integer values
 * of up to 32 bits (<code>int</code> or smaller) as samples.
 * An interface composed of {@link GrayImage} and {@link IntegerImage}.
 * <p>
 * Like all extensions of {@link GrayImage}, this image data class supports
 * only one channel.
 * @author Marco Schmidt
 * @since 0.9.0
 * @see GrayImage
 * @see IntegerImage
 */
public interface GrayIntegerImage extends GrayImage, IntegerImage
{
}
