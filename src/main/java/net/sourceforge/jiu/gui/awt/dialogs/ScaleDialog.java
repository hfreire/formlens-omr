/*
 * ScaleDialog
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import net.sourceforge.jiu.apps.Strings;

/**
 * A dialog to enter the parameters for an image scaling operation.
 * @author Marco Schmidt
 */
public class ScaleDialog extends Dialog implements ActionListener, KeyListener
{
	private Button ok;
	private Button cancel;
	private TextComponent heightTextField;
	private TextComponent widthTextField;
	private Checkbox maintainAspectRatio;
	private Choice types;
	private boolean pressedOk;
	private String oldWidthString;
	private String oldHeightString;
	private int oldWidth;
	private int oldHeight;
	private int type;

	/**
	 * Creates an InfoDialog, a modal dialog to display a text message, centered on the desktop.
	 * @param owner the Frame this dialog will belong to
	 * @param strings the Strings resource used for text messages
	 * @param width the current width of the image
	 * @param height the current height of the image
	 * @param pickType determines whether the will be a Choice box for picking the type of scaling algorithm
	 * @param typeNames names of the image scaling algorithms
	 * @param initialType algorithm type to be initially selected
	 */
	public ScaleDialog(Frame owner, Strings strings, int width, int height, boolean pickType, String[] typeNames, int initialType)
	{
		super(owner, strings.get(Strings.SCALE_IMAGE), true);
		pressedOk = false;
		oldWidth = width;
		oldWidthString = Integer.toString(oldWidth);
		oldHeight = height;
		oldHeightString = Integer.toString(oldHeight);

		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 2));

		Label widthLabel = new Label(strings.get(Strings.NEW_WIDTH));
		widthTextField = new TextField(Integer.toString(width), 6);
		widthTextField.addKeyListener(this);
		Label heightLabel = new Label(strings.get(Strings.NEW_HEIGHT));
		heightTextField = new TextField(Integer.toString(height), 6);
		heightTextField.addKeyListener(this);

		panel.add(widthLabel);
		panel.add(widthTextField);

		panel.add(heightLabel);
		panel.add(heightTextField);

		panel.add(new Label(""));
		maintainAspectRatio = new Checkbox(strings.get(Strings.MAINTAIN_ASPECT_RATIO), true);
		panel.add(maintainAspectRatio);

		type = initialType;
		if (pickType)
		{
			panel.add(new Label(strings.get(Strings.METHOD)));
			types = new Choice();
			for (int i = 0; i < typeNames.length; i++)
			{
				types.add(typeNames[i]);
			}
			types.select(initialType);
			panel.add(types);
		}
		add(panel, BorderLayout.CENTER);

		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);

		panel = new Panel();
		panel.add(ok);
		panel.add(cancel);
		add(panel, BorderLayout.SOUTH);

		pack();
		center();
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

	/**
	 * Centers the dialog on screen.
	 */
	public void center()
	{
		Rectangle rect = getBounds();
		int width = rect.width;
		int height = rect.height;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width / 2) - (width / 2),
			(screenSize.height / 2) - (height / 2));
	}

	public int getHeightValue()
	{
		return getValue(heightTextField);
	}

	public int getType()
	{
		if (types == null)
		{
			return type;
		}
		else
		{
			return types.getSelectedIndex();
		}
	}

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

	public int getWidthValue()
	{
		return getValue(widthTextField);
	}
			
	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	public void handleKeys(KeyEvent e)
	{
		if (e.getSource() == widthTextField)
		{
			String text = widthTextField.getText();
			if (maintainAspectRatio.getState() && (!text.equals(oldWidthString)))
			{
				// compute height from current width
				int w = getValue(widthTextField);
				if (w > 0)
				{
					oldHeightString = Integer.toString((int)(w * (float)oldHeight / (float)oldWidth));
					heightTextField.setText(oldHeightString);
				}
			}
		}
		else
		if (e.getSource() == heightTextField)
		{
			String text = heightTextField.getText();
			if (maintainAspectRatio.getState() && (!text.equals(oldHeightString)))
			{
				// compute width from current height
				int h = getValue(heightTextField);
				if (h > 0)
				{
					oldWidthString = Integer.toString((int)(h * (float)oldWidth / (float)oldHeight));
					widthTextField.setText(oldWidthString);
				}
			}
		}
		oldWidthString = widthTextField.getText();
		oldHeightString = heightTextField.getText();
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
