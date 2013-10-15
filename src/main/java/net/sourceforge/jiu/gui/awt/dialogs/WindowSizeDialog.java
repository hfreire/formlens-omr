/*
 * WindowSizeDialog
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import net.sourceforge.jiu.apps.Strings;

/**
 * A dialog to enter values for the width and height of a window (typically
 * for a spatial filter like median or mean.
 *
 * @author Marco Schmidt
 */
public class WindowSizeDialog extends Dialog implements
	ActionListener, KeyListener
{
	private Button ok;
	private Button cancel;
	private TextField width;
	private TextField height;
	private boolean pressedOk;

	/**
	 * @param owner the Frame this dialog will belong to
	 */
	public WindowSizeDialog(Frame owner, Strings strings, int titleIndex, int initialWidth, int initialHeight)
	{
		super(owner, strings.get(titleIndex), true);
		pressedOk = false;
		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 2));

		panel.add(new Label(strings.get(Strings.ENTER_WINDOW_SIZE)));
		panel.add(new Label(""));

		panel.add(new Label(strings.get(Strings.WINDOW_WIDTH)));
		width = new TextField(Integer.toString(initialWidth));
		width.addKeyListener(this);
		panel.add(width);

		panel.add(new Label(strings.get(Strings.WINDOW_HEIGHT)));
		height = new TextField(Integer.toString(initialHeight));
		height.addKeyListener(this);
		panel.add(height);

		add(panel, BorderLayout.CENTER);

		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);

		panel = new Panel();
		panel.add(ok);
		panel.add(cancel);
		add(panel, BorderLayout.SOUTH);

		updateOkButton();

		pack();
		Dialogs.center(this);
	}

	/**
	 * Hides (closes) this dialog if the OK button was source of the action event
	 * (e.g. if the button was pressed).
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == ok)
		{
			pressedOk = true;
			setVisible(false);
		}
		else
		if (e.getSource() == cancel)
		{
			setVisible(false);
		}
	}

	public int getHeightValue()
	{
		return getValue(height);
	}

	public int getWidthValue()
	{
		return getValue(width);
	}

	/**
	 * Attempts to convert the content of the argument text component
	 * to an <code>int</code>; if successful, returns that int, otherwise
	 * -1000 is returned.
	 * @param textField the text component that is supposed to hold an int value
	 * @return int representation of the text component's data
	 */
	private int getValue(TextComponent textField)
	{
		try
		{
			return Integer.parseInt(textField.getText());
		}
		catch (NumberFormatException nfe)
		{
			return -1000;
		}
	}	

	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	private void updateOkButton()
	{
		int w = getWidthValue();
		int h = getHeightValue();
		ok.setEnabled(w >= 1 && h >= 1 && ((w % 2) == 1) && ((h % 2) == 1));
	}

	public void keyPressed(KeyEvent e)
	{
		updateOkButton();
	}

	public void keyReleased(KeyEvent e)
	{
		updateOkButton();
	}

	public void keyTyped(KeyEvent e)
	{
		updateOkButton();
	}
}
