/*
 * YesNoDialog
 * 
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.sourceforge.jiu.apps.Strings;

/**
 * A dialog that asks a question and offers a Yes and a No button
 * (and optionally a Cancel button).
 * @author Marco Schmidt
 * @since 0.11.0
 */
public class YesNoDialog extends Dialog implements ActionListener
{
	/**
	 * Will be returned in {@link #getResult} if the YES button was chosen.
	 */
	public static final int RESULT_YES = 0;

	/**
	 * Will be returned in {@link #getResult} if the NO button was chosen.
	 */
	public static final int RESULT_NO = 1;

	/**
	 * Will be returned in {@link #getResult} if the CANCEL button was chosen.
	 */
	public static final int RESULT_CANCEL = 2;
	private Button yes;
	private Button no;
	private Button cancel;
	private int result;

	/**
	 * Creates a new YesNoDialog object and shows it centered on the screen.
	 * @param owner the frame that owns this modal dialog
	 * @param strings the String resources
	 * @param titleIndex the index into the String resource of the title text 
	 * @param questionIndex the index into the String resource of the question text
	 * @param includeCancel determines whether a third button 'Cancel' will be included
	 */
	public YesNoDialog(Frame owner, Strings strings, int titleIndex, int questionIndex, boolean includeCancel)
	{
		super(owner, strings.get(titleIndex), true);

		add(new Label(strings.get(questionIndex)), BorderLayout.CENTER);

		yes = new Button(strings.get(Strings.YES));
		yes.addActionListener(this);
		no = new Button(strings.get(Strings.NO));
		no.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);

		Panel panel = new Panel();
		panel.add(yes);
		panel.add(no);
		if (includeCancel)
		{
			panel.add(cancel);
		}
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
		if (e.getSource() == yes)
		{
			result = RESULT_YES;
			setVisible(false);
		}
		else
		if (e.getSource() == no)
		{
			result = RESULT_NO;
			setVisible(false);
		}
		else
		if (e.getSource() == cancel)
		{
			result = RESULT_CANCEL;
			setVisible(false);
		}
	}

	/**
	 * Returns one of the RESULT_xyz constants of this class. 
	 * @return the RESULT constant of the button which the user has chosen
	 */
	public int getResult()
	{
		return result;
	}
}
