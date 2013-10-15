/*
 * InfoDialog
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A modal AWT dialog that displays text in a non-editable text area component
 * (so that it can be selected and easily copied to the system's clipboard).
 * Provides an OK button so that user can remove the dialog.
 * @author Marco Schmidt
 */
public class InfoDialog extends Dialog implements ActionListener
{
	private Button ok;
	private TextArea textArea;

	/**
	 * Creates an InfoDialog, a modal dialog to display a text message, centered on the desktop.
	 * @param owner the Frame this dialog will belong to
	 * @param title the text that will be displayed in the title bar of the dialog
	 * @param text the message text that will be displayed in the main part of the dialog
	 */
	public InfoDialog(Frame owner, String title, String text)
	{
		super(owner, title, true);
		ok = new Button("OK");
		ok.addActionListener(this);
		Panel panel = new Panel();
		panel.add(ok);
		add(panel, BorderLayout.SOUTH);
		textArea = new TextArea(text);
		textArea.setEditable(false);
		textArea.setSize(textArea.getMinimumSize());
		//ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		//scrollPane.add(textArea);
		add(textArea);
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
}
