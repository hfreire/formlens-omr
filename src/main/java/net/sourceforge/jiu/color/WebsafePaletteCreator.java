/*
 * WebsafePaletteCreator
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.color;

import net.sourceforge.jiu.data.Palette;
import net.sourceforge.jiu.data.RGBIndex;

/**
 * This class creates {@link net.sourceforge.jiu.data.Palette} objects that 
 * contain the so-called <em>websafe palette</em>.
 * This palette has 216 entries which are uniformly spread over the RGB color
 * cube.
 * Each component (red / green / blue) takes each of the six values 0, 51, 101,
 * 153, 204 and 255 (note that the difference is almost equal between two consecutive
 * values, between 50 and 52).
 * Therefore, the palette will have 6<sup>3</sup> = 6 * 6 * 6 = 216 entries.
 * <p>
 * This palette was designed with computer systems in mind that can only display
 * 256 colors at a time.
 * With the 216 colors that are uniformly spread over RGB color space, there is
 * at least a somewhat similar match for each possible input color.
 *
 * @author Marco Schmidt
 * @since 0.5.0
 */
public class WebsafePaletteCreator implements RGBIndex
{
	private static final int[] SAMPLES = {0x00, 0x33, 0x66, 0x99, 0xcc, 0xff};
	
	private WebsafePaletteCreator()
	{
		// private so that this class cannot be instantiated
	}

	/**
	 * Creates a new palette with the 216 websafe colors.
	 * @return new palette object
	 */
	public static Palette create()
	{
		Palette result = new Palette(216, 255);
		int index = 0;
		for (int r = 0; r < 6; r++)
		{
			for (int g = 0; g < 6; g++)
			{
				for (int b = 0; b < 6; b++)
				{
					result.put(index++, SAMPLES[r], SAMPLES[g], SAMPLES[b]);
				}
			}
		}
		return result;
	}
}
