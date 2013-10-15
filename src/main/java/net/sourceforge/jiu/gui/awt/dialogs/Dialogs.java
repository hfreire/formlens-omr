/*
 * Dialogs
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * Convenience class that provides a number of static helper methods to deal with dialogs.
 * @author Marco Schmidt
 */
public class Dialogs
{
	private Dialogs()
	{
	}

	/**
	 * Centers the argument window on screen.
	 */
	public static void center(Window window)
	{
		if (window == null)
		{
			return;
		}
		Rectangle rect = window.getBounds();
		int width = rect.width;
		int height = rect.height;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation((screenSize.width / 2) - (width / 2),
			(screenSize.height / 2) - (height / 2));
	}

	/**
	 * Creates a new IntegerDialog, displays it and returns the Integer
	 * value specified by the user (or null if the dialog was canceled).
	 * @param owner frame from which the dialog is spawned
	 * @param title text for the title bar of the dialog
	 * @param message message displayed in the dialog
	 * @param minValue minimal allowed integer value to be entered by the user 
	 * @param initialValue initial integer value shown in the dialog
	 * @param maxValue maximal allowed integer value to be entered by the user
	 * @param okText the text for the OK button
	 * @param cancelText the text for the cancel button
	 * @return the specified integer value or null if the Cancel button was pressed
	 */
	public static Integer getInteger(Frame owner, String title, String message,
		int minValue, int initialValue, int maxValue, String okText,
		String cancelText)
	{
		IntegerDialog dialog = new IntegerDialog(owner, title, message,
			minValue, initialValue, maxValue, okText, cancelText);
		dialog.setVisible(true);
		return dialog.getValue();
	}
}
