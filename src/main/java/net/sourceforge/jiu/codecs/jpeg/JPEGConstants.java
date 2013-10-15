/*
 * JPEGConstants
 *
 * Copyright (c) 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.jpeg;

/**
 * Constants necessary to encode and decode JPEG streams.
 * @author Marco Schmidt
 * @since 0.13.0
 */
public final class JPEGConstants
{
	/**
	 * Length of sample block edge, in samples (8).
	 */
	public static final int BLOCK_EDGE_LENGTH = 8;

	/**
	 * 16 bit value that denotes the beginning of a JPEG stream (0xffd8).
	 */
	public static final int JFIF_SIGNATURE = 0xffd8;

	/**
	 * DHT (define Huffman table) marker ID value.
	 */
	public static final int MARKER_DHT  = 0xc4;

	/**
	 * DQT (define quantization table) marker ID value.
	 */
	public static final int MARKER_DQT  = 0xdb;

	/**
	 * SOF0 (start of frame, type 0) marker ID value.
	 */
	public static final int MARKER_SOF0 = 0xc0;

	/**
	 * SOF1 (start of frame, type 1) marker ID value.
	 */
	public static final int MARKER_SOF1 = 0xc1;

	/**
	 * SOF2 (start of frame, type 2) marker ID value.
	 */
	public static final int MARKER_SOF2 = 0xc2;

	/**
	 * SOF3 (start of frame, type 3) marker ID value.
	 */
	public static final int MARKER_SOF3 = 0xc3;

	/**
	 * SOF5 (start of frame, type 5) marker ID value.
	 */
	public static final int MARKER_SOF5 = 0xc5;

	/**
	 * SOF6 (start of frame, type 6) marker ID value.
	 */
	public static final int MARKER_SOF6 = 0xc6;

	/**
	 * SOF7 (start of frame, type 7) marker ID value.
	 */
	public static final int MARKER_SOF7 = 0xc7;

	/**
	 * SOF9 (start of frame, type 9) marker ID value.
	 */
	public static final int MARKER_SOF9 = 0xc9;

	/**
	 * SOFa (start of frame, type a) marker ID value.
	 */
	public static final int MARKER_SOFA = 0xca;

	/**
	 * SOFb (start of frame, type b) marker ID value.
	 */
	public static final int MARKER_SOFB = 0xcb;

	/**
	 * SOFd (start of frame, type d) marker ID value.
	 */
	public static final int MARKER_SOFD = 0xcd;

	/**
	 * SOFe (start of frame, type e) marker ID value.
	 */
	public static final int MARKER_SOFE = 0xce;

	/**
	 * SOFf (start of frame, type f) marker ID value.
	 */
	public static final int MARKER_SOFF = 0xcf;

	/**
	 * SOS (start of scan) marker ID value.
	 */
	public static final int MARKER_SOS = 0xda;

	/**
	 * Maximum length of a Huffman code in bit (16).
	 */
	public static final int MAX_HUFFMAN_CODE_LENGTH = 16;
	
	/**
	 * Number of samples in a block of samples (64).
	 */
	public static final int SAMPLES_PER_BLOCK = BLOCK_EDGE_LENGTH * BLOCK_EDGE_LENGTH; 

	/**
	 * Empty private constructor to prevent instantiation of this class.
	 */
	private JPEGConstants()
	{
	}
}
