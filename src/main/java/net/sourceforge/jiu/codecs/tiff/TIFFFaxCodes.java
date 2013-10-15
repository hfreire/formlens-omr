/*
 * TIFFFaxCodes
 * 
 * Copyright (c) 2002, 2003, 2004, 2005 Marco Schmidt
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.tiff;

/**
 * Information to be used to decode and encode TIFF files in one of the
 * bilevel compression types Modified Huffman, CCITT Group 3 or CCITT Group 4.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class TIFFFaxCodes
{
	/**
	 * Index of the code word in the int[] value pairs.
	 */
	public static final int INDEX_CODE_WORD = 0;

	/**
	 * Index of the code value in the int[] value pairs.
	 */
	public static final int INDEX_CODE_VALUE = 1;

	/**
	 * Minimum code length in bits of black codes.
	 */
	public static final int MIN_BLACK_CODE_SIZE = 2;

	/**
	 * Minimum code length in bits of white codes.
	 */
	public static final int MIN_WHITE_CODE_SIZE = 4;

	/**
	 * The code words and their meanings for black codes.
	 * In ascending order, starting at MIN_BLACK_CODE_SIZE bits,
	 * each int[][] object contains all the code word / code value pairs
	 * for one bit length.
	 */
	public static final int[][][] BLACK_CODES =
	{
		{ // 2 bits
			{2, 3},
			{3, 2},
		},
		{ // 3 bits
			{2, 1},
			{3, 4},
		},
		{ // 4 bits
			{2, 6},
			{3, 5},
		},
		{ // 5 bits
			{3, 7},
		},
		{ // 6 bits
			{4, 9},
			{5, 8},
		},
		{ // 7 bits
			{4, 10},
			{5, 11},
			{7, 12},
		},
		{ // 8 bits
			{4, 13},
			{7, 14},
		},
		{ // 9 bits
			{24, 15},
		},
		{ // 10 bits
			{23, 16},
			{24, 17},
			{55, 0},
			{8, 18},
			{15, 64},
		},
		{ // 11 bits
			{23, 24},
			{24, 25},
			{40, 23},
			{55, 22},
			{103, 19},
			{104, 20},
			{108, 21},
			{8, 1792},
			{12, 1856},
			{13, 1920},
		},
		{ // 12 bits
			{18, 1984},
			{19, 2048},
			{20, 2112},
			{21, 2176},
			{22, 2240},
			{23, 2304},
			{28, 2368},
			{29, 2432},
			{30, 2496},
			{31, 2560},
			{36, 52},
			{39, 55},
			{40, 56},
			{43, 59},
			{44, 60},
			{51, 320},
			{52, 384},
			{53, 448},
			{55, 53},
			{56, 54},
			{82, 50},
			{83, 51},
			{84, 44},
			{85, 45},
			{86, 46},
			{87, 47},
			{88, 57},
			{89, 58},
			{90, 61},
			{91, 256},
			{100, 48},
			{101, 49},
			{102, 62},
			{103, 63},
			{104, 30},
			{105, 31},
			{106, 32},
			{107, 33},
			{108, 40},
			{109, 41},
			{200, 128},
			{201, 192},
			{202, 26},
			{203, 27},
			{204, 28},
			{205, 29},
			{210, 34},
			{211, 35},
			{212, 36},
			{213, 37},
			{214, 38},
			{215, 39},
			{218, 42},
			{219, 43},
		},
		{ // 13 bits
			{74, 640},
			{75, 704},
			{76, 768},
			{77, 832},
			{82, 1280},
			{83, 1344},
			{84, 1408},
			{85, 1472},
			{90, 1536},
			{91, 1600},
			{100, 1664},
			{101, 1728},
			{108, 512},
			{109, 576},
			{114, 896},
			{115, 960},
			{116, 1024},
			{117, 1088},
			{118, 1152},
			{119, 1216},
		}
	};

	/**
	 * The code words and their meanings for white codes.
	 * In ascending order, starting at MIN_WHITE_CODE_SIZE bits,
	 * each int[][] object contains all the code word / code value pairs
	 * for one bit length.
	 */
 	public static final int[][][] WHITE_CODES =
	{
		{ // 4 bits
			{7, 2},
			{8, 3},
			{11, 4},
			{12, 5},
			{14, 6},
			{15, 7},
		},
		{ // 5 bits
			{18, 128},
			{19, 8},
			{20, 9},
			{27, 64},
			{7, 10},
			{8, 11},
		},
		{ // 6 bits
			{23, 192},
			{24, 1664},
			{42, 16},
			{43, 17},
			{3, 13},
			{52, 14},
			{53, 15},
			{7, 1},
			{8, 12},
		},
		{ // 7 bits
			{19, 26},
			{23, 21},
			{24, 28},
			{36, 27},
			{39, 18},
			{40, 24},
			{43, 25},
			{3, 22},
			{55, 256},
			{4, 23},
			{8, 20},
			{12, 19},
		},
		{ // 8 bits
			{18, 33},
			{19, 34},
			{20, 35},
			{21, 36},
			{22, 37},
			{23, 38},
			{26, 31},
			{27, 32},
			{2, 29},
			{36, 53},
			{37, 54},
			{40, 39},
			{41, 40},
			{42, 41},
			{43, 42},
			{44, 43},
			{45, 44},
			{3, 30},
			{50, 61},
			{51, 62},
			{52, 63},
			{53, 0},
			{54, 320},
			{55, 384},
			{4, 45},
			{74, 59},
			{75, 60},
			{5, 46},
			{82, 49},
			{83, 50},
			{84, 51},
			{85, 52},
			{88, 55},
			{89, 56},
			{90, 57},
			{91, 58},
			{100, 448},
			{101, 512},
			{103, 640},
			{104, 576},
			{10, 47},
			{11, 48},
		},
		{ // 9 bits
			{152, 1472},
			{153, 1536},
			{154, 1600},
			{155, 1728},
			{204, 704},
			{205, 768},
			{210, 832},
			{211, 896},
			{212, 960},
			{213, 1024},
			{214, 1088},
			{215, 1152},
			{216, 1216},
			{217, 1280},
			{218, 1344},
			{219, 1408},
		},
		{ // 10 bits
		},
		{ // 11 bits
			{8, 1792},
			{12, 1856},
			{13, 1920},
		},
		{ // 12 bits
			{18, 1984},
			{19, 2048},
			{20, 2112},
			{21, 2176},
			{22, 2240},
			{23, 2304},
			{28, 2368},
			{29, 2432},
			{30, 2496},
			{31, 2560},
		}
	};
}

