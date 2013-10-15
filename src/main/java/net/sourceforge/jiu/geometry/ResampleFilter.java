/*
 * ResampleFilter
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.geometry;

/**
 * Abstract base class for filters to be used with
 * the {@link Resample} operation.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public abstract class ResampleFilter
{
	private float samplingRadius;

	/**
	 * This empty constructor sets the sampling radius to the
	 * recommended sampling radius as provided by 
	 * {@link #getRecommendedSamplingRadius()}.
	 */
	public ResampleFilter()
	{
		setSamplingRadius(getRecommendedSamplingRadius());
	}

	/**
	 * Returns the weight of the sample at the distance given
	 * by the argument value.
	 */
	public abstract float apply(float value);

	/**
	 * Return the name of this filter.
	 * Should avoid natural language words if possible.
	 * @return String with filter name
	 */
	public abstract String getName();

	/**
	 * Returns a recommendation for the sampling radius to
	 * be used with this filter.
	 * This recommendation value will be the default value 
	 * for the sampling radius of objects of this class.
	 * You can modify it with a call to {@link #setSamplingRadius}.
	 * @return the recommended sampling radius to be used with this filter
	 */
	public abstract float getRecommendedSamplingRadius();

	/**
	 * Returns the sampling radius of this object.
	 * @see #getRecommendedSamplingRadius
	 * @see #setSamplingRadius
	 */
	public float getSamplingRadius()
	{
		return samplingRadius;
	}

	/**
	 * Sets the sampling radius to a new value.
	 * Call this method if you do not want to use the default
	 * radius as provided by {@link #getRecommendedSamplingRadius}.
	 * @param newValue new sampling radius to be used with this object
	 */
	public void setSamplingRadius(float newValue)
	{
		if (newValue <= 0.0f)
		{
			throw new IllegalArgumentException("Sampling radius must be larger than 0.0f.");
		}
		samplingRadius = newValue;
	}
}
