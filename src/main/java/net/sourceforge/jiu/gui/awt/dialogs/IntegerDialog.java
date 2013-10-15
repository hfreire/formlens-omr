/*
 * IntegerDialog
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * An AWT dialog to select an <code>int</code> value from a given interval.
 *
 * @author Marco Schmidt
 */
public class IntegerDialog extends Dialog implements ActionListener, AdjustmentListener, KeyListener
{
	private Button cancel;
	private int maxValue;
	private int minValue;
	private Button ok;
	private Integer result;
	private Scrollbar scrollbar;
	private TextComponent valueTextField;

	/**
	 * Creates an IntegerDialog, a modal dialog that lets the user select one int
	 * value from a given interval.
	 *
	 * @param owner the {@link java.awt.Frame} this dialog will belong to
	 * @param title the text that will be displayed in the title bar of the dialog
	 * @param message the message text that will be displayed in the upper part of the dialog
	 * @param minValue the minimum allowed int value to be selected by the user
	 * @param initialValue the int value that is selected when the dialog first pops up
	 * @param maxValue the maximum allowed int value to be selected by the user
	 * @param okText the value for OK (just <code>&quot;OK&quot;</code> in English
	 *  programs, may be different for other natural languages
	 * @param cancelText the value for Cancel (just <code>&quot;Cancel&quot;</code> in English
	 *  programs, may be different for other natural languages
	 */
	public IntegerDialog(Frame owner, String title, String message,
		int minValue, int initialValue, int maxValue,
		String okText, String cancelText) 
	{
		super(owner, title, true);
		this.minValue = minValue;
		this.maxValue = maxValue;
		add(new Label(message), BorderLayout.NORTH);

		Panel panel = new Panel(new BorderLayout());
		scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, initialValue, 1, minValue, maxValue + 1);
		scrollbar.addAdjustmentListener(this);
		panel.add(scrollbar, BorderLayout.CENTER);

		valueTextField = new TextField(Integer.toString(initialValue), 6);
		valueTextField.addKeyListener(this);
		panel.add(valueTextField, BorderLayout.EAST);
		add(panel, BorderLayout.CENTER);

		panel = new Panel();
		ok = new Button(okText);
		ok.addActionListener(this);
		cancel = new Button(cancelText);
		cancel.addActionListener(this);
		panel.add(ok);
		panel.add(cancel);
		add(panel, BorderLayout.SOUTH);

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
			result = new Integer(scrollbar.getValue());
			setVisible(false);
		}
		else
		if (e.getSource() == cancel)
		{
			setVisible(false);
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		valueTextField.setText(Integer.toString(scrollbar.getValue()));
	}

	public Integer getValue()
	{
		return result;
	}

	public void handleKeys(KeyEvent e)
	{
		setScrollbarFromTextField();
	}

	public void keyPressed(KeyEvent e)
	{
		handleKeys(e);
	}

	public void keyReleased(KeyEvent e)
	{
		handleKeys(e);
	}

	public void keyTyped(KeyEvent e)
	{
		handleKeys(e);
	}

	private void setScrollbarFromTextField()
	{
		String text = valueTextField.getText().trim();
		int number;
		try
		{
			number = Integer.parseInt(text);
		}
		catch (NumberFormatException nfe)
		{
			return;
		}
		
		if (number < minValue || number > maxValue)
		{
			return;
		}
		scrollbar.setValue(number);
	}
}
