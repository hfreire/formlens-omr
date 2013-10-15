/*
 * HueSaturationValueDialog
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import net.sourceforge.jiu.apps.Strings;

/**
 * A dialog to enter the parameters for an hue/saturation/value adjustment operation.
 * Saturation and value are specified as percentage values between -100 and 100,
 * where 0 means no change.
 * Hue can be specified optionally (a Choice component must be checked so
 * that the hue value will be used); it is a value between 0 and 359.
 *
 * @since 0.5.0
 * @author Marco Schmidt
 * @see net.sourceforge.jiu.color.adjustment.HueSaturationValue
 */
public class HueSaturationValueDialog extends Dialog implements
	ActionListener, ItemListener, KeyListener
{
	private Button ok;
	private Button cancel;
	private Panel colorPanel;
	private TextField hue;
	private TextField saturation;
	private TextField value;
	private Checkbox setHue;
	private boolean pressedOk;

	/**
	 * @param owner the Frame this dialog will belong to
	 */
	public HueSaturationValueDialog(Frame owner, Strings strings, boolean initialSetHue, int h, int s, int v)
	{
		super(owner, strings.get(Strings.ADJUST_HUE_SATURATION_AND_VALUE), true);
		pressedOk = false;
		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 2));

		setHue = new Checkbox(strings.get(Strings.SET_HUE), initialSetHue);
		setHue.addItemListener(this);
		panel.add(setHue);
		colorPanel = new Panel();
		panel.add(colorPanel);

		panel.add(new Label(strings.get(Strings.HUE)));
		hue = new TextField(Integer.toString(h));
		hue.addKeyListener(this);
		panel.add(hue);

		panel.add(new Label(strings.get(Strings.SATURATION)));
		saturation = new TextField(Integer.toString(s));
		saturation.addKeyListener(this);
		panel.add(saturation);

		panel.add(new Label(strings.get(Strings.VALUE)));
		value = new TextField(Integer.toString(v));
		value.addKeyListener(this);
		panel.add(value);

		add(panel, BorderLayout.CENTER);

		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);

		panel = new Panel();
		panel.add(ok);
		panel.add(cancel);
		add(panel, BorderLayout.SOUTH);

		updateTextFields();

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

	public int getHue()
	{
		return getValue(hue);
	}

	public int getSaturation()
	{
		return getValue(saturation);
	}

	public int getValue()
	{
		return getValue(value);
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

	public void itemStateChanged(ItemEvent e)
	{
		updateTextFields();
	}

	public boolean isHueSet()
	{
		return setHue.getState();
	}

	/**
	 * Computes width and height of new image and updates the
	 * corresponding labels.
	 * The labels will either display width and height or a single
	 * dash if the data in the text fields is invalid.
	 */
	private void updateTextFields()
	{
		hue.setEnabled(setHue.getState());
		int h = getValue(hue);
		int s = getValue(saturation);
		int v = getValue(value);
		boolean enabled = s >= -100 && s <= 100 && v >= -100 && v <= 100;
		if (setHue.getState())
		{
			enabled = enabled && h >= 0 && h <= 359;
		}
		ok.setEnabled(enabled);
		Color color = new Color(Color.HSBtoRGB(h / 360f, 1.0f, 1.0f));
		colorPanel.setBackground(color);
	}

	public void keyPressed(KeyEvent e)
	{
		updateTextFields();
	}

	public void keyReleased(KeyEvent e)
	{
		updateTextFields();
	}

	public void keyTyped(KeyEvent e)
	{
		updateTextFields();
	}
}
