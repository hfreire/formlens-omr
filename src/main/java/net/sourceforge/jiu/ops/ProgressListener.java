/*
 * ProgressListener
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.ops;

/**
 * This interface must be implemented by classes that want to be notified
 * about progress of an image operation.
 *
 * @author Marco Schmidt
 */
public interface ProgressListener
{
	/**
	 * Set the progress level to a new value, which must be between 0.0f and 1.0f
	 * (including both of these values).
	 * You should not call this method with a value lower than any value you've set
	 * before.
	 * However, this is not checked.
	 * @param progress the degree of progress as a value between 0.0f and 1.0f
	 * @throws IllegalArgumentException if the float argument is not in the mentioned interval
	 */
	void setProgress(float progress);

	/**
	 * Sets a new progress level.
	 * If an operation consists of totalItems steps, which are numbered from 0 to
	 * totalItems - 1, this method can be called after the completion of each step.
	 * <p>
	 * Example: if there are three steps and the first one is done, the parameters
	 * must be 0 and 3, which will indicated 33% completion.
	 * Parameters 1 and 3 mean 66%, 2 and 3 100%.
	 * If you use 3 and 3, an IllegalArgumentException will be thrown.
	 * <p>
	 * Computes <code>(float)(zeroBasedIndex + 1) / (float)totalItems</code> and calls
	 * {@link #setProgress(float)} with that value.
	 *
	 * @param zeroBasedIndex the index of the step that was just completed
	 * @param totalItems the number of steps in this operation
	 * @throws IllegalArgumentException if the parameters don't match the above criteria
	 */
	void setProgress(int zeroBasedIndex, int totalItems);
}
