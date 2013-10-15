/*
 * LogLuvConversion
 * 
 * Copyright (c) 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color.conversion;

/**
 * Convert from LogLuv color representation to RGB color space and
 * from LogL to grayscale.
 * <p>
 * This implementation is based on the file <code>tif_luv.c</code> which
 * is part of the TIFF library <a target="_top" href="http://www.libtiff.org">libtiff</a>.
 * The original implementation was written by Greg W. Larson.
 * <p>
 * Learn more about the color type and its encoding on Greg's page
 * <a target="_top" href="http://positron.cs.berkeley.edu/~gwlarson/pixformat/tiffluv.html">LogLuv 
 * Encoding for TIFF Images</a>.
 * @author Marco Schmidt
 * @since 0.10.0
 */
public class LogLuvConversion
{
	private static final double M_LN2 = 0.69314718055994530942;
	private static final double UVSCALE = 410.0;

	// constants from libtiff's uv_decode.h
	private static final float UV_SQSIZ = 0.003500f;
	private static final int UV_NDIVS = 16289;
	private static final float UV_VSTART = 0.016940f;
	private static final int UV_NVS = 163;
	private static final double U_NEU = 0.210526316;
	private static final double V_NEU = 0.473684211;
	private static final double[] USTART =
	{
		0.247663,
		0.243779,
		0.241684,
		0.237874,
		0.235906,
		0.232153,
		0.228352,
		0.226259,
		0.222371,
		0.220410,
		0.214710,
		0.212714,
		0.210721,
		0.204976,
		0.202986,
		0.199245,
		0.195525,
		0.193560,
		0.189878,
		0.186216,
		0.186216,
		0.182592,
		0.179003,
		0.175466,
		0.172001,
		0.172001,
		0.168612,
		0.168612,
		0.163575,
		0.158642,
		0.158642,
		0.158642,
		0.153815,
		0.153815,
		0.149097,
		0.149097,
		0.142746,
		0.142746,
		0.142746,
		0.138270,
		0.138270,
		0.138270,
		0.132166,
		0.132166,
		0.126204,
		0.126204,
		0.126204,
		0.120381,
		0.120381,
		0.120381,
		0.120381,
		0.112962,
		0.112962,
		0.112962,
		0.107450,
		0.107450,
		0.107450,
		0.107450,
		0.100343,
		0.100343,
		0.100343,
		0.095126,
		0.095126,
		0.095126,
		0.095126,
		0.088276,
		0.088276,
		0.088276,
		0.088276,
		0.081523,
		0.081523,
		0.081523,
		0.081523,
		0.074861,
		0.074861,
		0.074861,
		0.074861,
		0.068290,
		0.068290,
		0.068290,
		0.068290,
		0.063573,
		0.063573,
		0.063573,
		0.063573,
		0.057219,
		0.057219,
		0.057219,
		0.057219,
		0.050985,
		0.050985,
		0.050985,
		0.050985,
		0.050985,
		0.044859,
		0.044859,
		0.044859,
		0.044859,
		0.040571,
		0.040571,
		0.040571,
		0.040571,
		0.036339,
		0.036339,
		0.036339,
		0.036339,
		0.032139,
		0.032139,
		0.032139,
		0.032139,
		0.027947,
		0.027947,
		0.027947,
		0.023739,
		0.023739,
		0.023739,
		0.023739,
		0.019504,
		0.019504,
		0.019504,
		0.016976,
		0.016976,
		0.016976,
		0.016976,
		0.012639,
		0.012639,
		0.012639,
		0.009991,
		0.009991,
		0.009991,
		0.009016,
		0.009016,
		0.009016,
		0.006217,
		0.006217,
		0.005097,
		0.005097,
		0.005097,
		0.003909,
		0.003909,
		0.002340,
		0.002389,
		0.001068,
		0.001653,
		0.000717,
		0.001614,
		0.000270,
		0.000484,
		0.001103,
		0.001242,
		0.001188,
		0.001011,
		0.000709,
		0.000301,
		0.002416,
		0.003251,
		0.003246,
		0.004141,
		0.005963,
		0.008839,
		0.010490,
		0.016994,
		0.023659,
	};

	private static final short[] NCUM =
	{
		0,
		4,
		10,
		17,
		26,
		36,
		48,
		62,
		77,
		94,
		112,
		133,
		155,
		178,
		204,
		231,
		260,
		291,
		323,
		357,
		393,
		429,
		467,
		507,
		549,
		593,
		637,
		683,
		729,
		778,
		830,
		882,
		934,
		989,
		1044,
		1102,
		1160,
		1222,
		1284,
		1346,
		1411,
		1476,
		1541,
		1610,
		1679,
		1752,
		1825,
		1898,
		1975,
		2052,
		2129,
		2206,
		2288,
		2370,
		2452,
		2538,
		2624,
		2710,
		2796,
		2887,
		2978,
		3069,
		3164,
		3259,
		3354,
		3449,
		3549,
		3649,
		3749,
		3849,
		3954,
		4059,
		4164,
		4269,
		4379,
		4489,
		4599,
		4709,
		4824,
		4939,
		5054,
		5169,
		5288,
		5407,
		5526,
		5645,
		5769,
		5893,
		6017,
		6141,
		6270,
		6399,
		6528,
		6657,
		6786,
		6920,
		7054,
		7188,
		7322,
		7460,
		7598,
		7736,
		7874,
		8016,
		8158,
		8300,
		8442,
		8588,
		8734,
		8880,
		9026,
		9176,
		9326,
		9476,
		9630,
		9784,
		9938,
		10092,
		10250,
		10408,
		10566,
		10727,
		10888,
		11049,
		11210,
		11375,
		11540,
		11705,
		11873,
		12041,
		12209,
		12379,
		12549,
		12719,
		12892,
		13065,
		13240,
		13415,
		13590,
		13767,
		13944,
		14121,
		14291,
		14455,
		14612,
		14762,
		14905,
		15041,
		15170,
		15293,
		15408,
		15517,
		15620,
		15717,
		15806,
		15888,
		15964,
		16033,
		16095,
		16150,
		16197,
		16237,
		16268,
	};

	private LogLuvConversion()
	{
	}

	/**
	 * Converts an unsigned 10 bit value (the argument must lie in the
	 * interval 0 to 1023) to a <code>double</code> luminance
	 * (brightness) value between <code>0.0</code> and <code>1.0</code>.
	 * This conversion is needed by both LogLuv to XYZ and LogL to grayscale.
	 * @param p10 input LogL value
	 * @return double value with luminance, between 0 and 1
	 */
	public static double convertLogL10toY(int p10)
	{
		if (p10 == 0)
		{
			return 0.0;
		}
		else
		{
			return Math.exp(M_LN2 / 64.0 * (p10 + 0.5) - M_LN2 * 12.0);
		}
	}

	/**
	 * Converts a signed 16 bit value (the argument must lie in the
	 * interval -32768 to 32767) to a <code>double</code> luminance
	 * (brightness) value between <code>0.0</code> and <code>1.0</code>.
	 * This conversion is needed by both LogLuv to XYZ and LogL to grayscale.
	 * @param p16 input LogL value
	 * @return double value with luminance, between 0 and 1
	 */
	public static double convertLogL16toY(int p16)
	{
		int Le = p16 & 0x7fff;
		if (Le == 0)
		{
			return 0.0;
		}
		double Y = Math.exp(M_LN2 / 256.0 * (Le + 0.5) - M_LN2 * 64.0);
		if ((p16 & 0x8000) == 0)
		{
			return Y;
		}
		else
		{
			return -Y;
		}
	}

	private static byte convertDoubleToByte(double d)
	{
		if (d <= 0.0)
		{
			return 0;
		}
		else
		if (d >= 1.0)
		{
			return (byte)255;
		}
		else
		{
			double result = 255.0 * Math.sqrt(d);
			return (byte)result;
		}
	}

	/**
	 * Converts a number of 24 bit LogLuv pixels to 24 bit RGB pixels.
	 * Each LogLuv pixel is stored as three consecutive bytes in the <code>logluv</code> byte array.
	 * The first byte and the top two bits of the second are the LogL value, the remaining
	 * 14 bits are an index that encodes u and v.
	 * @param logluv byte array with LogLuv data, must be at least num * 3 bytes large
	 * @param red the byte samples for the red channel will be written to this array
	 * @param green the byte samples for the green channel will be written to this array
	 * @param blue the byte samples for the blue channel will be written to this array
	 * @param num number of pixels to be converted
	 */
	public static void convertLogLuv24InterleavedtoRGB24Planar(byte[] logluv, byte[] red, byte[] green, byte[] blue, int num)
	{
		int srcOffs = 0;
		int destOffs = 0;
		while (num-- != 0)
		{
			// convert from LogLuv24 to XYZ
			float X = 0.0f;
			float Y = 0.0f;
			float Z = 0.0f;
			// first byte and top two bits of second make 10 bit value L10
			int v1 = logluv[srcOffs++] & 0xff;
			int v2 = logluv[srcOffs++] & 0xff;
			int v3 = logluv[srcOffs++] & 0xff;
			double L = convertLogL10toY(v1 << 2 | ((v2 >> 6) & 0x03));
			if (L > 0.0)
			{
				int c = ((v2 & 0x3f) << 8) | v3;
				double u = U_NEU; 
				double v = V_NEU;
				// uv_decode in tif_luv.c
				int	upper = UV_NVS;
				int lower = 0;
				if (c >= 0 && c < UV_NDIVS)
				{
					lower = 0;
					upper = UV_NVS;
					int ui = 0;
					int vi = 0;
					while (upper - lower > 1)
					{
						vi = (lower + upper) >> 1;
						ui = c - NCUM[vi];
						if (ui > 0)
						{
							lower = vi;
						}
						else
						if (ui < 0)
						{
							upper = vi;
						}
						else
						{
							lower = vi;
							break;
						}
					}
					vi = lower;
					ui = c - NCUM[vi];
					u = USTART[vi] + (ui + 0.5) * UV_SQSIZ;
					v = UV_VSTART + (vi + 0.5) * UV_SQSIZ;
				}
				double s = 1.0 / (6.0 * u - 16.0 * v + 12.0);
				double x = 9.0 * u * s;
				double y = 4.0 * v * s;
				X = (float)(x / y * L);
				Y = (float)L;
				Z = (float)((1.0 - x - y) / y * L);
			}
			// convert from XYZ to RGB
			double r =  2.690 * X + -1.276 * Y + -0.414 * Z;
			double g = -1.022 * X +  1.978 * Y +  0.044 * Z;
			double b =  0.061 * X + -0.224 * Y +  1.163 * Z;
			red[destOffs] = convertDoubleToByte(r);
			green[destOffs] = convertDoubleToByte(g);
			blue[destOffs] = convertDoubleToByte(b);
			destOffs++;
		}
	}

	/**
	 * Converts a number of 32 bit LogLuv pixels to 24 bit RGB pixels.
	 * Each LogLuv pixel is stored as four consecutive bytes in the <code>logluv</code> byte array.
	 * The first two bytes represent the LogL value (most significant bytefirst), followed
	 * by the u value and then the v value.
	 * @param logluv byte array with LogLuv data, must be at least num * 4 bytes large
	 * @param red the byte samples for the red channel will be written to this array
	 * @param green the byte samples for the green channel will be written to this array
	 * @param blue the byte samples for the blue channel will be written to this array
	 * @param num number of pixels to be converted
	 */
	public static void convertLogLuv32InterleavedtoRGB24Planar(byte[] logluv, byte[] red, byte[] green, byte[] blue, int num)
	{
		int srcOffs = 0;
		int destOffs = 0;
		while (num-- != 0)
		{
			// convert from LogLuv32 to XYZ
			float X = 0.0f;
			float Y = 0.0f;
			float Z = 0.0f;
			int v1 = logluv[srcOffs++] & 0xff;
			int v2 = logluv[srcOffs++] & 0xff;
			double L = convertLogL16toY((short)((v1 << 8) | v2));
			if (L > 0.0)
			{
				// decode color
				double u = 1.0 / UVSCALE * ((logluv[srcOffs++] & 0xff) + 0.5);
				double v = 1.0 / UVSCALE * ((logluv[srcOffs++] & 0xff) + 0.5);
				double s = 1.0 / (6.0 * u - 16.0 * v + 12.0);
				double x = 9.0 * u * s;
				double y = 4.0 * v * s;
				X = (float)(x / y * L);
				Y = (float)L;
				Z = (float)((1.0 - x - y) / y * L);
			}
			// convert from XYZ to RGB
			double r =  2.690 * X + -1.276 * Y + -0.414 * Z;
			double g = -1.022 * X +  1.978 * Y +  0.044 * Z;
			double b =  0.061 * X + -0.224 * Y +  1.163 * Z;
			red[destOffs] = convertDoubleToByte(r);
			green[destOffs] = convertDoubleToByte(g);
			blue[destOffs] = convertDoubleToByte(b);
			destOffs++;
		}
	}

	/**
	 * Converts a number of 16 bit LogL samples to 8 bit grayscale samples.
	 * @param logl byte array with LogL samples, each 16 bit sample is stored as 
	 *  two consecutive bytes in order most-significant-byte least-significant-byte (network byte order);
	 *  the array must be at least num * 2 entries large
	 * @param gray the byte array to which the converted samples will be written
	 * @param num the number of samples to be converted
	 */
	public static void convertLogL16toGray8(byte[] logl, byte[] gray, int num)
	{
		int srcOffs = 0;
		int destOffs = 0;
		while (num-- != 0)
		{
			int v1 = logl[srcOffs++] & 0xff;
			int v2 = logl[srcOffs++] & 0xff;
			double L = convertLogL16toY((short)((v1 << 8) | v2));
			gray[destOffs++] = convertDoubleToByte(L);
		}
	}
}
