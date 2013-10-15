/*
 * ImageDescriptionCreator
 *
 * Copyright (c) 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import java.text.NumberFormat;
import java.util.Locale;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.GrayImage;
import net.sourceforge.jiu.data.PalettedImage;
import net.sourceforge.jiu.data.RGBImage;

/**
 * Returns textual descriptions of the properties of JIU image objects.
 * @author Marco Schmidt
 * @since 0.9.0
 */
public class ImageDescriptionCreator
{
	/*private static final int TYPE = 0;
	private static final int PIXELS = 1;
	private static final int IMAGE_TYPE_BILEVEL = 2;
	private static final int IMAGE_TYPE_GRAYSCALE = 3;
	private static final int IMAGE_TYPE_PALETTED = 4;
	private static final int IMAGE_TYPE_RGB_TRUECOLOR = 5;
	private static final int IMAGE_TYPE_UNKNOWN = 6;
	private static final int BITS_PER_PIXEL = 7;
	private static final int MEMORY = 8;
	private static final int DISK_SPACE = 9;*/

	private ImageDescriptionCreator()
	{
	}

	private static String formatNumber(long value, Locale locale)
	{
		if (locale == null)
		{
			return Long.toString(value);
		}
		else
		{
			return NumberFormat.getInstance(locale).format(value);
		}
	}

	/**
	 * Returns a description of the argument image using the default locale.
	 */
	/*public static String getDescription(PixelImage image)
	{
		return getDescription(image, Locale.getDefault());
	}*/

	/**
	 * Returns a description of the argument image using the language
	 * as specified by the argument locale's two-letter language code.
	 * @param image the image for which a textual description is to be returned
	 * @param locale the Locale storing the natural language to be used for formatting
	 * @return a textual description of the image
	 */
	public static String getDescription(PixelImage image, Locale locale, Strings strings)
	{
		StringBuffer result = new StringBuffer();
		result.append(strings.get(StringIndexConstants.IMAGE_TYPE));
		result.append(": ");
		result.append(strings.get(getImageType(image)));
		result.append(", ");
		result.append(strings.get(StringIndexConstants.PIXELS));
		result.append(": ");
		int width = image.getWidth();
		int height = image.getHeight();
		result.append(formatNumber(width, locale));
		result.append(" x ");
		result.append(formatNumber(height, locale));
		result.append(" (");
		result.append(formatNumber(width * height, locale));
		result.append("), ");
		result.append(strings.get(StringIndexConstants.BITS_PER_PIXEL));
		result.append(": ");
		result.append(formatNumber(image.getBitsPerPixel(), locale));
		return result.toString();
	}

	private static int getImageType(PixelImage image)
	{
		int stringIndex = StringIndexConstants.IMAGE_TYPE_UNKNOWN;
		if (image instanceof BilevelImage)
		{
			return stringIndex = StringIndexConstants.BILEVEL;
		}
		else
		if (image instanceof GrayImage)
		{
			return stringIndex = StringIndexConstants.GRAYSCALE;
		}
		else
		if (image instanceof PalettedImage)
		{
			return stringIndex = StringIndexConstants.PALETTED;
		}
		else
		if (image instanceof RGBImage)
		{
			return stringIndex = StringIndexConstants.RGB_TRUECOLOR;
		}
		return stringIndex;
	}
}
