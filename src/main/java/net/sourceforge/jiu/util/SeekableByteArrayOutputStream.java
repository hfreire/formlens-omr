/*
 * SeekableByteArrayOutputStream
 * 
 * Copyright (c) 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An extension of {@link java.io.OutputStream} that writes data to an internal
 * byte array, resizing it when necessary.
 * Similar to {@link java.io.ByteArrayOutputStream}, but also enables seeking and truncating.
 *
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class SeekableByteArrayOutputStream extends OutputStream
{
	private byte[] buffer;
	private boolean closed;
	private int incrementStep;
	private int offset;
	private int size;

	/**
	 * Creates a new object of this class, setting initial capacity and increment size
	 * to default values.
	 */
	public SeekableByteArrayOutputStream()
	{
		this(1024, 1024);
	}

	/**
	 * Creates a new object of this class, setting initial capacity to the argument 
	 * value.
	 * The increment size is set to the initial capacity as well if that value is larger
	 * than 0.
	 * Otherwise it is set to a default value.
	 * @param initialCapacity the number of bytes that are allocated in this constructor (0 or larger)
	 */
	public SeekableByteArrayOutputStream(int initialCapacity)
	{
		this(initialCapacity, initialCapacity == 0 ? 1024 : initialCapacity);
	}

	/**
	 * Creates a new object of this class, setting initial capacity and increment 
	 * to the argument values.
	 * @param initialCapacity the number of bytes that are allocated in this constructor (0 or larger)
	 * @param increment the number of bytes by which the internal byte array is increased if it is full (1 or larger)
	 */
	public SeekableByteArrayOutputStream(int initialCapacity, int increment)
	{
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Value for initial capacity must not be negative.");
		}
		if (increment < 1)
		{
			throw new IllegalArgumentException("Value for increment must be 1 or larger.");
		}
		buffer = new byte[initialCapacity];
		incrementStep = increment;
		offset = 0;
	}

	/**
	 * Closes this output stream.
	 * After a call to this method, all write attempts will result in an exception.
	 */
	public void close() throws IOException
	{
		closed = true;
	}

	private void ensureSpace(int numBytes) throws IOException
	{
		if (closed)
		{
			throw new IOException("Stream was closed already. Cannot write to closed stream.");
		}
		if (numBytes < 0)
		{
			throw new IllegalArgumentException("Cannot write negative number of bytes (" + numBytes + ").");
		}
		if (buffer.length - offset < numBytes)
		{
			increaseBuffer(Math.max(buffer.length + incrementStep, offset + numBytes));
		}
	}

	/**
	 * Returns the current offset in the output stream.
	 * Larger than or equal to 0 and smaller than or equal to {@link #getSize}.
	 * @return current position in the output stream, 0-based
	 */
	public int getPosition()
	{
		return offset;
	}

	/**
	 * Returns the current size of the output stream.
	 * @return size of the output stream in bytes (0 or larger)
	 */
	public int getSize()
	{
		return size;
	}

	private void increaseBuffer(int newLength)
	{
		if (newLength <= buffer.length)
		{
			return;
		}
		byte[] temp = new byte[newLength];
		System.arraycopy(buffer, 0, temp, 0, offset);
		buffer = temp;
	}

	/**
	 * Sets the current position in the output stream to the argument.
	 * @param newOffset new offset into the file, must be &gt;= 0 and &lt;= {@link #getSize}
	 * @throws IOException if the argument is invalid
	 */
	public void seek(int newOffset) throws IOException
	{
		if (newOffset < 0)
		{
			throw new IOException("Cannot seek to negative offset (" + newOffset + ").");
		}
		if (newOffset > size)
		{
			throw new IOException("Cannot seek to offset " + newOffset + ", stream has only " + size + " byte(s).");
		}
		offset = newOffset;
	}

	/**
	 * Allocates a new <code>byte[]</code> object, copies {@link #getSize} bytes
	 * from the internal byte array to that new array and returns the array.
	 * @return a copy of the byte[] data stored internally
	 */
	public byte[] toByteArray()
	{
		byte[] result = new byte[size];
		System.arraycopy(buffer, 0, result, 0, size);
		return result;
	}

	/**
	 * Removes all bytes after the current position.
	 * After a call to this method, {@link #getSize} is equal to {@link #getPosition}.
	 */
	public void truncate()
	{
		size = offset;
	}

	/**
	 * Writes the least significant eight bits of the argument <code>int</code> to the internal array.
	 * @param b int variable that stores the byte value to be written
	 */
	public void write(int b) throws IOException
	{
		ensureSpace(1);
		buffer[offset++] = (byte)b;
		if (offset > size)
		{
			size = offset;
		}
	}
	
	/**
	 * Write the complete argument array to this stream.
	 * Copies the data to the internal byte array.
	 * Simply calls <code>write(data, 0, data.length);</code>.
	 * @param data array to be copied to this stream
	 */
	public void write(byte[] data) throws IOException
	{
		write(data, 0, data.length);
	}

	/**
	 * Write some bytes from the argument array to this stream.
	 * Copies num bytes starting at src[srcOffset] to this stream.
	 * @param src the array from which data is copied
	 * @param srcOffset int index into that array pointing to the first byte to be copied
	 * @param num number of bytes to be copied
	 */
	public void write(byte[] src, int srcOffset, int num) throws IOException
	{
		ensureSpace(num);
		System.arraycopy(src, srcOffset, buffer, offset, num);
		offset += num;
		if (offset > size)
		{
			size = offset;
		}
	}

	/** 
	 * Writes the bytes in the internal byte array to the argument output stream.
	 * A call to this method has the same effect as 
	 * <pre>
	 * byte[] copy = toByteArray();
	 * out.write(copy, 0, copy.length);
	 * </pre>
	 * However, you with this method you save the allocation of an additional byte array
	 * and the copying to that new array.
	 * @param out the output stream to which this stream's content is copied
	 * @throws IOException if out has a problem writing the bytes
	 */
	public void writeTo(OutputStream out) throws IOException
	{
		out.write(buffer, 0, size);
	}
}
