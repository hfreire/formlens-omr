/*
 * MissingParameterException
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * Exception class to indicate that an operation's parameter is missing
 * (has not been specified by caller and there was no default value that
 * could be used).
 *
 * @author Marco Schmidt
 */
public class MissingParameterException extends OperationFailedException
{
	public MissingParameterException(String message)
	{
		super(message);
	}
}
