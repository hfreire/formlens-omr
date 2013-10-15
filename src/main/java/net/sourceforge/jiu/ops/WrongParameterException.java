/*
 * WrongParameterException
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

import net.sourceforge.jiu.ops.OperationFailedException;

/**
 * Exception class to indicate that an operation's parameter is of the wrong
 * type, does not fall into a valid interval or a similar mistake.
 *
 * @author Marco Schmidt
 * @since 0.6.0
 */
public class WrongParameterException extends OperationFailedException
{
	public WrongParameterException(String message)
	{
		super(message);
	}
}
