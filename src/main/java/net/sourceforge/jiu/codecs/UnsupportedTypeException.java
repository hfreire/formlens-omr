/*
 * UnsupportedTypeException
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * This exception is thrown during image loading.
 * If a codec recognizes the file format but does not support the
 * exact subtype it encounters (the compression type is unknown or
 * the color depth unsupported), an instance of this exception class is 
 * created.
 * <p>
 * If the format is not recognized at all, a {@link WrongFileFormatException}
 * should be thrown.
 * <p>
 * If there were errors during loading because of file corruption, an
 * {@link InvalidFileStructureException} must be thrown.
 *
 * @see InvalidFileStructureException
 * @see WrongFileFormatException
 *
 * @author Marco Schmidt
 */
public class UnsupportedTypeException extends OperationFailedException
{
	public UnsupportedTypeException(String message)
	{
		super(message);
	}
}
