/*
 * CropDialog
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
 * A dialog to enter the parameters for an image crop operation.
 * @author Marco Schmidt
 */
public class CropDialog extends Dialog implements ActionListener, KeyListener
{
	private Button ok;
	private Button cancel;
	private TextField x1;
	private TextField y1;
	private TextField x2;
	private TextField y2;
	private int width;
	private int height;
	private Label newWidth;
	private Label newHeight;
	private boolean pressedOk;

	/**
	 * @param owner the Frame this dialog will belong to
	 * @param strings Strings resource to be used for text messages
	 * @param width width of the original image, before cropping; this is used to 
	 *  determine the valid values for left and right column, from 0 to width - 1
	 * @param height height of the original image, before cropping; this is used to 
	 *  determine the valid values for top and bottom row, from 0 to height - 1
	 */
	public CropDialog(Frame owner, Strings strings, int width, int height)
	{
		super(owner, strings.get(Strings.CROP_IMAGE) + " (" + width + " x " + height + ")", true);
		pressedOk = false;
		this.width = width;
		this.height = height;
		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 6));

		panel.add(new Label(strings.get(Strings.LEFT_COLUMN)));
		x1 = new TextField("0");
		x1.addKeyListener(this);
		panel.add(x1);

		panel.add(new Label(strings.get(Strings.RIGHT_COLUMN)));
		x2 = new TextField(Integer.toString(width - 1));
		x2.addKeyListener(this);
		panel.add(x2);

		panel.add(new Label(strings.get(Strings.NEW_WIDTH)));
		newWidth = new Label();
		panel.add(newWidth);

		panel.add(new Label(strings.get(Strings.TOP_ROW)));
		y1 = new TextField("0");
		y1.addKeyListener(this);
		panel.add(y1);

		panel.add(new Label(strings.get(Strings.BOTTOM_ROW)));
		y2 = new TextField(Integer.toString(height - 1));
		y2.addKeyListener(this);
		panel.add(y2);

		panel.add(new Label(strings.get(Strings.NEW_HEIGHT)));
		newHeight = new Label();
		panel.add(newHeight);

		add(panel, BorderLayout.CENTER);

		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);

		panel = new Panel();
		panel.add(ok);
		panel.add(cancel);
		add(panel, BorderLayout.SOUTH);

		updateLabels();
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

	public int getHeight()
	{
		int y1 = getY1();
		int y2 = getY2();
		if (y1 != -1 && y2 != -1 && y1 >= 0 && y2 >= y1 && y2 < height && y1 <= y2)
		{
			return y2 - y1 + 1;
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Attempts to convert the content of the argument text component
	 * to an <code>int</code>; if successful, returns that int, otherwise
	 * -1 is returned.
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
			return -1;
		}
	}	

	/**
	 * Returns the width of the to-be-cropped image as given by the 
	 * current values in the text fields for left column and right column.
	 * Computes the width from those values and returns it or returns -1
	 * if the data in the text fields is not valid for some reason.
	 * @return width of cropped image or -1 if information is invalid
	 */
	public int getWidth()
	{
		int x1 = getX1();
		int x2 = getX2();
		if (x1 != -1 && x2 != -1 && x1 >= 0 && x2 >= x1 && x2 < width && x1 <= x2)
		{
			return x2 - x1 + 1;
		}
		else
		{
			return -1;
		}
	}

	public int getX1()
	{
		return getValue(x1);
	}

	public int getX2()
	{
		return getValue(x2);
	}

	public int getY1()
	{
		return getValue(y1);
	}

	public int getY2()
	{
		return getValue(y2);
	}

	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	/**
	 * Computes width and height of new image and updates the
	 * corresponding labels.
	 * The labels will either display width and height or a single
	 * dash if the data in the text fields is invalid.
	 */
	private void updateLabels()
	{
		String text;
		boolean enabled = true;

		int valueWidth = getWidth();
		if (valueWidth == -1)
		{
			text = "-";
			enabled = false;
		}
		else
		{
			text = Integer.toString(valueWidth);
		}
		newWidth.setText(text);

		int valueHeight = getHeight();
		if (valueHeight == -1)
		{
			text = "-";
			enabled = false;
		}
		else
		{
			text = Integer.toString(valueHeight);
		}
		newHeight.setText(text);
		
		ok.setEnabled(enabled);
	}

	public void keyPressed(KeyEvent e)
	{
		updateLabels();
	}

	public void keyReleased(KeyEvent e)
	{
		updateLabels();
	}

	public void keyTyped(KeyEvent e)
	{
		updateLabels();
	}
}
