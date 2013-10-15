/*
 * ReduceGrayscaleDialog
 * 
 * Copyright (c) 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import net.sourceforge.jiu.apps.Strings;

/**
 * A dialog to enter the parameters for a grayscale reduction operation.
 * @author Marco Schmidt
 */
public class ReduceGrayscaleDialog extends Dialog implements ActionListener, AdjustmentListener
{
	public static final int TYPE_DITHERING_NONE = 0;
	public static final int TYPE_ORDERED_DITHERING = 1;
	public static final int TYPE_FLOYD_STEINBERG_ERROR_DIFFUSION = 2;
	public static final int TYPE_STUCKI_ERROR_DIFFUSION = 3;
	public static final int TYPE_BURKES_ERROR_DIFFUSION = 4;
	public static final int TYPE_SIERRA_ERROR_DIFFUSION = 5;
	public static final int TYPE_JARVIS_JUDICE_NINKE_ERROR_DIFFUSION = 6;
	public static final int TYPE_STEVENSON_ARCE_ERROR_DIFFUSION = 7;
	public final int[][] DITHERING_METHODS =
	{
		{
		 TYPE_DITHERING_NONE,
		 TYPE_ORDERED_DITHERING,
		 TYPE_FLOYD_STEINBERG_ERROR_DIFFUSION,
		 TYPE_STUCKI_ERROR_DIFFUSION,
		 TYPE_BURKES_ERROR_DIFFUSION,
		 TYPE_SIERRA_ERROR_DIFFUSION,
		 TYPE_JARVIS_JUDICE_NINKE_ERROR_DIFFUSION,
		 TYPE_STEVENSON_ARCE_ERROR_DIFFUSION
		},
		{
		 Strings.DITHERING_NONE,
		 Strings.ORDERED_DITHERING,
		 Strings.FLOYD_STEINBERG_ERROR_DIFFUSION,
		 Strings.STUCKI_ERROR_DIFFUSION,
		 Strings.BURKES_ERROR_DIFFUSION,
		 Strings.SIERRA_ERROR_DIFFUSION,
		 Strings.JARVIS_JUDICE_NINKE_ERROR_DIFFUSION,
		 Strings.STEVENSON_ARCE_ERROR_DIFFUSION
		}
	};
	private Strings strings;
	private Button ok;
	private Button cancel;
	private Scrollbar scrollbar;
	private Choice ditheringMethod;
	private Label bitLabel;
	private Label shadesLabel;
	private boolean pressedOk;

	/**
	 * Creates a modal dialog to enter the parameters.
	 * @param owner the parent of this modal dialog
	 * @param strings an object to get String constants in the current language
	 * @param bits initial number of bits to be shown in the dialog
	 * @param maxBits maximum allowed number of bits
	 * @param ditheringMethodSelection initial selection of dithering method
	 */
	public ReduceGrayscaleDialog(Frame owner, Strings strings, int bits, int  maxBits,
		int ditheringMethodSelection)
	{
		super(owner, strings.get(Strings.REDUCE_NUMBER_OF_SHADES_OF_GRAY), true);
		pressedOk = false;
		this.strings = strings;
		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 2));

		bitLabel = new Label();
		panel.add(bitLabel);
		scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, bits, 1, 1, maxBits + 1);
		scrollbar.addAdjustmentListener(this);
		panel.add(scrollbar);

		panel.add(new Label(strings.get(Strings.NUMBER_OF_SHADES_OF_GRAY) + ": "));
		shadesLabel = new Label();
		panel.add(shadesLabel);

		panel.add(new Label(strings.get(Strings.DITHERING_METHOD)));
		ditheringMethod = new Choice();
		for (int i = 0; i < DITHERING_METHODS[0].length; i++)
		{
			ditheringMethod.add(strings.get(DITHERING_METHODS[1][i]));
			if (ditheringMethodSelection == i)
			{
				ditheringMethod.select(i);
			}
		}
		panel.add(ditheringMethod);

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

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		updateLabels();
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

	public int getDitheringMethod()
	{
		return DITHERING_METHODS[0][ditheringMethod.getSelectedIndex()];
	}

	public int getNumBits()
	{
		return scrollbar.getValue();
	}

	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	private void updateLabels()
	{
		int numBits = getNumBits();
		bitLabel.setText(strings.get(Strings.NUMBER_OF_BITS) + ": " + numBits);
		shadesLabel.setText(Integer.toString(1 << numBits));
	}
}
