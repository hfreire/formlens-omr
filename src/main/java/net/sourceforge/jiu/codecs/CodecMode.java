/*
 * CodecMode
 *
 * Copyright (c) 2000, 2001 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

/**
 * This class is an enumeration type for the two modes that an image codec can be used in,
 * {@link #LOAD} and {@link #SAVE}.
 * These values are used as arguments in some of the methods of {@link ImageCodec}.
 *
 * @author Marco Schmidt
 * @since 0.7.0
 */
public final class CodecMode
{
	private CodecMode()
	{
	}

	/**
	 * Codec mode <em>load</em>, one of the two possible values of CodecMode.
	 * To be used with a codec to indicate that an image is to be read from a stream.
	 */
	public static final CodecMode LOAD = new CodecMode();

	/**
	 * Codec mode <em>save</em>, one of the two possible values of CodecMode.
	 * To be used with a codec to indicate that an image is to be written to a stream.
	 */
	public static final CodecMode SAVE = new CodecMode();
}
