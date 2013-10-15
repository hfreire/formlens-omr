/*
 * WrongFileFormatException
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * This exception is thrown during image loading.
 * If a codec is sure that the file or input stream that was given to
 * it is not in the format supported by that codec, an instance of
 * this exception class is thrown.
 * This is usually the case if the magic byte sequence of that format
 * is not found at the beginning of the stream.
 * <p>
 * If there were errors during loading because of file corruption, an
 * {@link InvalidFileStructureException} should be thrown.
 * <p>
 * If the format is recognized but cannot be loaded because the codec
 * does not fully support the file format, a {@link UnsupportedTypeException}
 * should be thrown.
 *
 * @author Marco Schmidt
 * @see InvalidFileStructureException
 * @see UnsupportedTypeException
 */
public class WrongFileFormatException extends OperationFailedException
{
	public WrongFileFormatException(String message)
	{
		super(message);
	}
}
