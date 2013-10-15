/*
 * Operation
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

import java.util.Vector;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;
import net.sourceforge.jiu.ops.ProgressListener;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * <p>
 * Base class for all operations.
 * </p>
 * <p>
 * It supports progress notification.
 * All classes that want to be notified by a new progress level of the operation
 * (defined as value between 0.0f (nothing has been done so far) to 1.0f
 * (operation finished)) must implement the {@link ProgressListener} interface.
 * </p>
 * <p>
 * An abortion state is stored in each Operation object.
 * It should be queried by a running operation from time to time 
 * (via {@link #getAbort()} - if it returns <code>true</code>,
 * the operation should terminate and return control to the caller.
 * The abort state can be modified using {@link #setAbort(boolean)}.
 * </p>
 * @author Marco Schmidt
 */
public abstract class Operation
{
 /* <p>
 * This class class contains a generic system to add parameters to an operation.
 * Whether an item becomes a parameter is often unclear and must be decided by
 * the operation implementor.
 * As an example: one could create an image rotation class with a numerical parameter
 * for the degrees of rotation.
 * One could also define a class of its own for each 90, 180 and 270 degrees
 * (excluding other values).
 * </p>
 * <p>
 * The generic parameter system is insufficient in some situations.
 * Example: A parameter can be defined to be of class Integer or Long, but it cannot
 * be forced to be in a certain interval.
 * Even if such a case could be solved by a specially-designed class, checking
 * of parameters (and maybe their relations among each other) can be done by
 * overriding the {@link #checkParams()} method.
 * </p>
 */
	private boolean abort;
	private Vector progressListeners;

	/**
	 * This constructor creates two internal empty lists for progress listeners and parameters.
	 */
	public Operation()
	{
		abort = false;
		progressListeners = new Vector();
	}

	/**
	 * Adds the argument progress listener to the internal list of
	 * progress listeners.
	 * Does not check if the argument already exists in that list, so you have
	 * to check for duplicates yourself.
	 *
	 * @param progressListener the progress listener to be added
	 */
	public void addProgressListener(ProgressListener progressListener)
	{
		if (progressListener == null)
		{
			return;
		}
		if (!progressListeners.contains(progressListener))
		{
			progressListeners.addElement(progressListener);
		}
	}

	/**
	 * Adds several progress listeners to this operation object.
	 * @param progressListeners contains zero or more objects implementing ProgressListener; 
	 *  each will be added by calling {@link #addProgressListener} on it
	 */
	public void addProgressListeners(Vector progressListeners)
	{
		if (progressListeners != null)
		{
			int index = 0;
			while (index < progressListeners.size())
			{
				ProgressListener listener = (ProgressListener)progressListeners.elementAt(index++);
				addProgressListener(listener);
			}
		}
	}

	/**
	 * Returns the current abort status.
	 * If <code>true</code>, a running operation should terminate what it is doing
	 * (return from {@link #process()}).
	 * @return abort status
	 * @see #setAbort
	 */
	public boolean getAbort()
	{
		return abort;
	}

	/**
	 * This method does the actual work of the operation.
	 * It must be called after all parameters have been given to the operation object.
	 * @throws WrongParameterException if at least one of the input parameters was 
	 *  not initialized appropriately (values out of the valid interval, etc.)
	 * @throws MissingParameterException if any mandatory parameter was not given to the operation
	 * @throws OperationFailedException 
	 */
	public void process() throws 
		MissingParameterException, 
		OperationFailedException, 
		WrongParameterException
	{
	}

	/**
	 * Removes the argument progress listener from the internal list of
	 * progress listeners.
	 * @param progressListener the progress listener to be removed
	 */
	public void removeProgressListener(ProgressListener progressListener)
	{
		progressListeners.removeElement(progressListener);
	}

	/**
	 * Sets a new abort status.
	 * @param newAbortStatus the new status
	 * @see #getAbort
	 */
	public void setAbort(boolean newAbortStatus)
	{
		abort = newAbortStatus;
	}

	/**
	 * This method will notify all registered progress listeners
	 * about a new progress level.
	 * The argument must be from 0.0f to 1.0f where 0.0f marks the
	 * beginning and 1.0f completion.
	 * The progress value should not be smaller than any value that
	 * was previously set.
	 * @param progress new progress value, from 0.0 to 1.0
	 */
	public void setProgress(float progress)
	{
		if (progress < 0.0f || progress > 1.0f)
		{
			throw new IllegalArgumentException("Progress values must be from" +
				" 0.0f to 1.0f; got " + progress);
		}
		int index = 0;
		while (index < progressListeners.size())
		{
			ProgressListener pl = 
				(ProgressListener)progressListeners.elementAt(index++);
			if (pl != null)
			{
				pl.setProgress(progress);
			}
		}
	}

	/**
	 * This method will notify all registered progress listeners
	 * about a new progress level.
	 * Simply checks the arguments and calls <code>setProgress((float)zeroBasedIndex / (float)totalItems);</code>.
	 * @param zeroBasedIndex the index of the item that was just processed, zero-based
	 * @param totalItems the number of items that will be processed
	 */
	public void setProgress(int zeroBasedIndex, int totalItems)
	{
		if (zeroBasedIndex < 0 || zeroBasedIndex >= totalItems ||
		    totalItems < 1)
		{
			throw new IllegalArgumentException("No valid arguments " +
				" zeroBasedIndex=" + zeroBasedIndex + ", totalItems=" +
				totalItems);
		}
		setProgress((float)zeroBasedIndex / (float)totalItems);
	}
}
