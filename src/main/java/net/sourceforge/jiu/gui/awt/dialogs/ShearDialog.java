/*
 * ShearDialog
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
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import net.sourceforge.jiu.apps.Strings;
import net.sourceforge.jiu.geometry.Shear;

/**
 * An AWT dialog to enter the angle for a shearing operation.
 * @author Marco Schmidt
 */
public class ShearDialog extends Dialog implements ActionListener, KeyListener
{
	private Button ok;
	private Button cancel;
	private TextField angleTextField;
	private boolean pressedOk;
	private Double result;
	private Label newWidthLabel;
	private int imageWidth;
	private int imageHeight;

	/**
	 * Creates a ShearDialog.
	 * @param owner the Frame this dialog will belong to
	 */
	public ShearDialog(Frame owner, Strings strings, double initialValue, int imageWidth, int imageHeight)
	{
		super(owner, strings.get(Strings.SHEAR_IMAGE) + " (" + imageWidth + " x " + imageHeight + ")", true);
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;

		Panel panel = new Panel(new GridLayout(0, 2));

		panel.add(new Label(strings.get(Strings.SHEAR_ENTER_ANGLE)));
		angleTextField = new TextField(Double.toString(initialValue));
		angleTextField.addKeyListener(this);
		panel.add(angleTextField);

		panel.add(new Label(strings.get(Strings.NEW_WIDTH)));
		newWidthLabel = new Label("");
		panel.add(newWidthLabel);

		add(panel, BorderLayout.CENTER);

		panel = new Panel();
		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);
		panel.add(ok);
		panel.add(cancel);
		add(panel, BorderLayout.SOUTH);

		handleKeys(null);
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
			result = getValue(angleTextField);
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
		if (d <= -90.0 || d >= 90.0)
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
		Double d = getValue(angleTextField);
		double angle = -90.0;
		if (d != null)
		{
			angle = d.doubleValue();
		}
		String labelText;
		if (angle > -90.0 && angle < 90.0)
		{
			ok.setEnabled(true);
			int newWidth = Shear.computeNewImageWidth(imageWidth, imageHeight, angle);
			labelText = Integer.toString(newWidth);
		}
		else
		{
			ok.setEnabled(false);
			labelText = "-";
		}
		newWidthLabel.setText(labelText);
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
