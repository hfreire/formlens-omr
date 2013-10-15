/*
 * MapToArbitraryPaletteDialog
 * 
 * Copyright (c) 2001, 2002 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt.dialogs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.sourceforge.jiu.apps.Strings;
import net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering;

/**
 * A dialog to enter the parameters for an operation to map an RGB truecolor
 * image to any given palette.
 *
 * @since 0.5.0
 * @author Marco Schmidt
 * @see net.sourceforge.jiu.color.quantization.ArbitraryPaletteQuantizer
 */
public class MapToArbitraryPaletteDialog extends Dialog implements
	ActionListener
{
	public static final int PALETTE_FILE = 0;
	public static final int PALETTE_WEBSAFE = 1;
	public static final int PALETTE_PALM_256_COLORS = 2;
	public static final int PALETTE_PALM_16_COLORS = 3;
	public static final int PALETTE_PALM_16_GRAY = 4;
	public static final int PALETTE_PALM_4_GRAY = 5;
	public static final int NUM_PALETTE_TYPES = 6;
	private static final int[] PALETTE_STRING_CONSTANTS =
	{
		Strings.PALETTE_FROM_FILE,
		Strings.WEBSAFE_PALETTE,
		Strings.PALETTE_PALM_256_COLORS,
		Strings.PALETTE_PALM_16_COLORS,
		Strings.PALETTE_PALM_16_GRAY,
		Strings.PALETTE_PALM_4_GRAY,
	};

	private static final int[] DITHERING_STRING_CONSTANTS =
	{
		Strings.DITHERING_NONE,
		Strings.FLOYD_STEINBERG_ERROR_DIFFUSION,
		Strings.STUCKI_ERROR_DIFFUSION,
		Strings.BURKES_ERROR_DIFFUSION,
		Strings.SIERRA_ERROR_DIFFUSION,
		Strings.JARVIS_JUDICE_NINKE_ERROR_DIFFUSION,
		Strings.STEVENSON_ARCE_ERROR_DIFFUSION
	};
	private static final int[] ERROR_DIFFUSION_TYPES =
	{
		ErrorDiffusionDithering.TYPE_FLOYD_STEINBERG,
		ErrorDiffusionDithering.TYPE_STUCKI,
		ErrorDiffusionDithering.TYPE_BURKES,
		ErrorDiffusionDithering.TYPE_SIERRA,
		ErrorDiffusionDithering.TYPE_JARVIS_JUDICE_NINKE,
		ErrorDiffusionDithering.TYPE_STEVENSON_ARCE
	};
	private Button ok;
	private Button cancel;

	private Checkbox[] checkboxes;

	private CheckboxGroup paletteType;

	private Choice dithering;
	private boolean pressedOk;

	/**
	 * @param owner the Frame this dialog will belong to
	 */
	public MapToArbitraryPaletteDialog(Frame owner, Strings strings)
	{
		super(owner, strings.get(Strings.MAP_TO_ARBITRARY_PALETTE), true);
		pressedOk = false;

		// 1 (CENTER) main panel with components for the various options
		Panel mainPanel = new Panel(new GridLayout(0, 1));

		// 1.1 Label with message text
		Panel panel = new Panel(new GridLayout(0, 1));
		panel.add(new Label(strings.get(Strings.CHOOSE_PALETTE_TYPE)));
		mainPanel.add(panel);

		// 1.2 radio buttons (CheckboxGroup) with palette type
		panel = new Panel(new GridLayout(0, 1));
		paletteType = new CheckboxGroup();
		checkboxes = new Checkbox[PALETTE_STRING_CONSTANTS.length];
		boolean selected = true;
		for (int i = 0; i < NUM_PALETTE_TYPES; i++)
		{
			checkboxes[i] = new Checkbox(strings.get(PALETTE_STRING_CONSTANTS[i]), paletteType, selected);
			selected = false;
			panel.add(checkboxes[i]);
		}
		mainPanel.add(panel);

		// 1.3 Choice with dithering types
		panel = new Panel();
		panel.add(new Label(strings.get(Strings.DITHERING_METHOD)));
		dithering = new Choice();
		for (int i = 0; i < DITHERING_STRING_CONSTANTS.length; i++)
		{
			dithering.add(strings.get(DITHERING_STRING_CONSTANTS[i]));
		}
		dithering.select(1);
		panel.add(dithering);
		mainPanel.add(panel);

		add(mainPanel, BorderLayout.CENTER);

		// 2 (SOUTH) buttons OK and Cancel
		panel = new Panel();

		// 2.1 OK button
		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		panel.add(ok);
		// 2.2 Cancel button
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);
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
			setVisible(false);
		}
		else
		if (e.getSource() == cancel)
		{
			setVisible(false);
		}
	}

	/**
	 * If the use of error diffusion was selected, this method
	 * returns on of the ErrorDiffusionDithering TYPE constants
	 */
	public int getErrorDiffusionType()
	{
		int sel = dithering.getSelectedIndex();
		if (sel > 0 && sel <= ERROR_DIFFUSION_TYPES.length)
		{
			return ERROR_DIFFUSION_TYPES[sel - 1];
		}
		else
		{
			return -1;
		}
	}

	/**
	 * Return the palette type (one of the PALETTE_xyz constants of this class)
	 * that is currently selected in the dialog.
	 */
	public int getPaletteType()
	{
		for (int i = 0; i < checkboxes.length; i++)
		{
			if (checkboxes[i].getState())
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns true if the OK button was pressed, false if 
	 * it was the Cancel button.
	 */
	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	/**
	 * Returns whether the use of one of the error diffusion 
	 * algorithms is selected in the dialog.
	 */
	public boolean useErrorDiffusion()
	{
		return dithering.getSelectedIndex() > 0;
	}
}
