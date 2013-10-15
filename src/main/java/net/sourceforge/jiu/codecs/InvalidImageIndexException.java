/*
 * InvalidImageIndexException
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * This exception is thrown when the caller has defined an image
 * index that specifies the image to be loaded in a multiple-image
 * file and that index is unavailable.
 * Example: user has specified an image index of 5, which is the
 * sixth image in the file (counting starts at 0), 
 * but only three images are available.
 * @author Marco Schmidt
 */
public class InvalidImageIndexException extends OperationFailedException
{
	/**
	 * Creates new exception object with a given error message.
	 * @param message String with text describing the exact problem
	 */
	public InvalidImageIndexException(String message)
	{
		super(message);
	}
}
