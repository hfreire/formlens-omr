/*
 * TIFFTag
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

import java.util.Vector;
import net.sourceforge.jiu.codecs.tiff.TIFFConstants;

/**
 * This encapsulates the data stored in a TIFF tag (a single image file directory entry).
 * That includes the following items:
 * <ul>
 * <li>identification number, 254 or higher; see the many TAG_xyz constants in
 *  {@link TIFFConstants} for a list of allowed values</li>
 * <li>type; the allowed types include integer and floating point numbers, Strings etc.;
 *  see the TAG_TYPE_xyz constants in {@link TIFFConstants}</li>
 * <li>count; the number of values of the given type that are stored in this tag;
 *  1 or higher</li>
 * <li>offset; if count is 1 and the type fits into four bytes, this is the complete
 *   value of this tag; otherwise, it is an offset into the file to the position
 *   that will hold the value(s)</li>
 * </ul>
 * See the TIFF specification manual linked in the description of {@link TIFFCodec}
 * for more details.
 *
 * @author Marco Schmidt
 * @see TIFFImageFileDirectory
 */
public class TIFFTag implements TIFFConstants
{
	private int id;
	private int type;
	private int count;
	private int offset;
	private Vector objects;

	/**
	 * Creates a new tag with the given ID, type, number of objects / primitives stored in it
	 * and offset value.
	 */
	public TIFFTag(int id, int type, int count, int offset)
	{
		this.id = id;
		this.type = type;
		this.count = count;
		if (count < 1)
		{
			throw new IllegalArgumentException("Tiff tag count value must " +
				"not be smaller than 1: " + count);
		}
		this.offset = offset;
		objects = null;
	}

	public TIFFTag(int id, int type, int count, int offset, Vector vector)
	{
		this(id, type, count, offset);
		objects = vector;
	}

	/**
	 * Returns the number of items stored in this tag.
	 */
	public int getCount()
	{
		return count;
	}

	/**
	 * Returns an item stored in this tag an <code>int</code> value.
	 * @param index zero-based index of the integer item to be returned
	 */
	public int getElementAsInt(int index)
	{
		Object element = getObject(index);
		if (element == null)
		{
			throw new IllegalArgumentException("Tag does not contain a list of values.");
		}
		if (element instanceof Short)
		{
			return ((Short)element).shortValue() & 0xffff;
		}
		if (element instanceof Integer)
		{
			return ((Integer)element).intValue();
		}
		if (element instanceof Byte)
		{
			return ((Byte)element).byteValue() & 0xff;
		}
		throw new IllegalArgumentException("Element #" + index + " is not an integer value.");
	}

	/**
	 * Returns the ID of this tag, which may be one of the TAG_xyz constants.
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Returns an object from this tag's Vector of items,
	 * or <code>null</code> if no such Vector exists.
	 */
	public Object getObject(int index)
	{
		if (objects == null)
		{
			return null;
		}
		else
		{
			return objects.elementAt(index);
		}
	}

	/**
	 * Returns the offset value stored in this tag.
	 */
	public int getOffset()
	{
		return offset;
	}

	/**
	 * If this tag has a Vector of items and if the first item
	 * is a String, that String is returned, <code>null</code>
	 * otherwise.
	 */
	public String getString()
	{
		if (objects != null && objects.size() > 0)
		{
			Object o = objects.elementAt(0);
			if (o != null && o instanceof String)
			{
				return (String)o;
			}
		}
		return null;
	}

	/**
	 * Returns the type of this tag's content as a TAG_TYPE_xyz constant.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Returns the Vector encapsulating the items stored in this tag.
	 * @see #setVector
	 */
	public Vector getVector()
	{
		return objects;
	}

	/**
	 * Returns if the value(s) stored in this tag are of type BYTE, SHORT or
	 * LONG.
	 * Note that BYTE and SHORT have the same meaning as in Java (one and two bytes 
	 * large) while LONG is a 32-bit-value, just like <code>int</code> in Java.
	 * @return if this tag's contains integer values &lt;= 32 bits
	 */
	public boolean isInt()
	{
		return (type == TAG_TYPE_BYTE || type == TAG_TYPE_SHORT || type == TAG_TYPE_LONG);
	}

	/**
	 * If this tag encapsulates more than one item or a single
	 * item that does not fit into four bytes, this Vector
	 * will store all elements in it.
	 * The size() method called on that Vector object returns 
	 * the same value as getCount().
	 * @param vector the Vector with the items to be encapsulated by this tag
	 * @see #getVector
	 */
	public void setVector(Vector vector)
	{
		objects = vector;
	}
}
