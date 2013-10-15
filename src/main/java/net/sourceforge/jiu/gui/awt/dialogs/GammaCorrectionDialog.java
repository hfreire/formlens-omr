/*
 * GammaCorrectionDialog
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
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import net.sourceforge.jiu.apps.Strings;

/**
 * A dialog to enter the parameters for a gamma correction operation.
 * @author Marco Schmidt
 */
public class GammaCorrectionDialog extends Dialog implements ActionListener, KeyListener
{
	private Button ok;
	private Button cancel;
	private TextField gammaTextField;
	private boolean pressedOk;
	private Double result;
	private final double MAX_GAMMA;

	/**
	 * Creates a GammaCorrectionDialog.
	 * @param owner the Frame this dialog will belong to
	 * @param strings Strings resource used for text messages
	 * @param initialValue the value to be set when the dialog pops up
	 * @param maxGamma the maximum allowed gamma value
	 */
	public GammaCorrectionDialog(Frame owner, Strings strings, double initialValue, double maxGamma)
	{
		super(owner, strings.get(Strings.ADJUST_GAMMA), true);
		MAX_GAMMA = maxGamma;

		Panel panel = new Panel(new BorderLayout());
		panel.add(new Label(strings.get(Strings.ENTER_GAMMA_VALUE)), BorderLayout.CENTER);
		gammaTextField = new TextField(Double.toString(initialValue));
		gammaTextField.addKeyListener(this);
		panel.add(gammaTextField, BorderLayout.EAST);
		add(panel, BorderLayout.CENTER);

		panel = new Panel();
		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
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
			pressedOk = true;
			result = getValue(gammaTextField);
			setVisible(false);
		}
		else
		if (e.getSource() == cancel)
		{
			setVisible(false);
		}
	}

	private Double getValue(TextField tf)
	{
		if (tf == null)
		{
			return null;
		}
		double d;
		try
		{
			d = (Double.valueOf(tf.getText())).doubleValue();
		}
		catch(NumberFormatException nfe)
		{
			return null;
		}
		if (d <= 0.0 || d > MAX_GAMMA)
		{
			return null;
		}
		return new Double(d);
	}

	public Double getValue()
	{
		return result;
	}

	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	public void handleKeys(KeyEvent e)
	{
		ok.setEnabled(getValue(gammaTextField) != null);
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
}
