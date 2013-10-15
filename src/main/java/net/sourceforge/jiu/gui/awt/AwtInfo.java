/*
 * AwtInfo
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import net.sourceforge.jiu.apps.StringIndexConstants;
import net.sourceforge.jiu.apps.Strings;

/**
 * Retrieve some information on the current graphical environment.
 * @author Marco Schmidt
 * @since 0.8.0
 */
public class AwtInfo
{
	private AwtInfo()
	{
	}

	/**
	 * Returns information on the current AWT settings, regarding the current
	 * language by using a {@link Strings} resource.
	 * Right now, only returns the screen resolution.
	 * All textual information is taken from the strings argument.
	 * @param strings String resources
	 * @return AWT information
	 */
	public static String getAwtInfo(Strings strings)
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screen = toolkit.getScreenSize();
		StringBuffer result = new StringBuffer();
		result.append(strings.get(StringIndexConstants.SCREEN_RESOLUTION) + "=" + screen.width + " x " + screen.height + "\n");
		ColorModel model = toolkit.getColorModel();
		if (model != null)
		{
			/* only in Java 1.2+
			   result.append("# components=" + model.getNumComponents() + "\n"); */
			result.append("# bits per pixel=" + model.getPixelSize() + "\n");
		}
		return result.toString();
	}
}
