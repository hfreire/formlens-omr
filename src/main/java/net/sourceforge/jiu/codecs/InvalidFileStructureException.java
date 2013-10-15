/*
 * InvalidFileStructureException
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * This exception is thrown during image loading, when the decoding
 * process is made impossible by errors in the image file.
 * If a codec has recognized the file format but finds irregularities in the
 * data and cannot continue loading, it is supposed to throw an instance of this
 * exception class.
 * An unexpected end of the input stream also falls into this category.
 * This typically means that the file is corrupt, but of course it could
 * also be because of an error in the codec implementation.
 * <p>
 * If the format is not recognized at all, a {@link WrongFileFormatException}
 * should be thrown.
 * <p>
 * If the format is recognized but cannot be loaded because the codec
 * does not fully support the file format, a {@link UnsupportedTypeException}
 * should be thrown.
 * @author Marco Schmidt
 * @see UnsupportedTypeException
 * @see WrongFileFormatException
 */
public class InvalidFileStructureException extends OperationFailedException
{
	public InvalidFileStructureException(String message)
	{
		super(message);
	}
}
