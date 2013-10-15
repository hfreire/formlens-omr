/*
 * UnsupportedCodecModeException
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs;

import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * This exception is thrown when a codec does not support the
 * codec mode wanted by the user.
 * Example: A user gives an OutputStream to a codec, indicating that an
 * image is to be saved, but the codec only supports loading.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class UnsupportedCodecModeException extends OperationFailedException
{
	public UnsupportedCodecModeException(String message)
	{
		super(message);
	}
}
