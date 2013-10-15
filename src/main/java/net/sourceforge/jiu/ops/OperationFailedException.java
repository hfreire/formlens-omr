/*
 * OperationFailedException
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

/**
 * Exception class to indicate that an operation failed during 
 * the execution of the method {@link Operation#process}.
 * Note that a failure due to missing or wrong parameters must
 * lead to the dedicated exception classes
 * {@link WrongParameterException} or {@link MissingParameterException}
 * being thrown.
 *
 * @since 0.7.0
 * @author Marco Schmidt
 */
public class OperationFailedException extends Exception
{
	public OperationFailedException(String message)
	{
		super(message);
	}
}
