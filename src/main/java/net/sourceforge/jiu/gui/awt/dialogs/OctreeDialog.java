/*
 * OctreeDialog
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
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import net.sourceforge.jiu.apps.Strings;
import net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering;

/**
 * A dialog to enter the parameters for an Octree color quantization operation.
 * It also allows to enter the optional algorithms that can be applied in combination with Octree.
 *
 * @author Marco Schmidt
 * @since 0.6.0
 * @see MedianCutDialog
 */
public class OctreeDialog extends Dialog implements 
	ActionListener, KeyListener
{
	public final int[] DITHERING_STRINGS =
	{
		Strings.DITHERING_NONE,
		Strings.FLOYD_STEINBERG_ERROR_DIFFUSION,
		Strings.STUCKI_ERROR_DIFFUSION,
	 	Strings.BURKES_ERROR_DIFFUSION,
		Strings.SIERRA_ERROR_DIFFUSION,
		Strings.JARVIS_JUDICE_NINKE_ERROR_DIFFUSION,
		Strings.STEVENSON_ARCE_ERROR_DIFFUSION
	};
	public final int[] DITHERING_TYPES =
	{
		0,
		ErrorDiffusionDithering.TYPE_FLOYD_STEINBERG,
		ErrorDiffusionDithering.TYPE_STUCKI,
	 	ErrorDiffusionDithering.TYPE_BURKES,
		ErrorDiffusionDithering.TYPE_SIERRA,
		ErrorDiffusionDithering.TYPE_JARVIS_JUDICE_NINKE,
		ErrorDiffusionDithering.TYPE_STEVENSON_ARCE
	};
	private Button ok;
	private Button cancel;
	private TextField numColorsField;
	private Choice outputColorType;
	private Choice dithering;
	private boolean pressedOk;

	/**
	 * Creates a modal dialog to enter the parameter.
	 * @param owner the parent of this modal dialog
	 * @param strings an object to get String constants in the current language
	 * @param numColors the number of colors in the resulting image
	 * @param paletted if true, the output image will be paletted, otherwise truecolor
	 */
	public OctreeDialog(Frame owner, Strings strings, int numColors, boolean paletted)
	{
		super(owner, strings.get(Strings.OCTREE_COLOR_QUANTIZATION), true);
		pressedOk = false;

		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 2));

		panel.add(new Label(strings.get(Strings.NUM_COLORS)));
		numColorsField = new TextField(Integer.toString(numColors), 6);
		numColorsField.addKeyListener(this);
		panel.add(numColorsField);

		/*panel.add(new Label(strings.get(Strings.OUTPUT_COLOR_TYPE)));
		outputColorType = new Choice();
		outputColorType.add(strings.get(Strings.OUTPUT_COLOR_TYPE_PALETTED));
		outputColorType.add(strings.get(Strings.OUTPUT_COLOR_TYPE_RGB));
		outputColorType.select(paletted ? 0 : 1);
		panel.add(outputColorType);*/

		panel.add(new Label(strings.get(Strings.DITHERING_METHOD)));
		dithering = new Choice();
		for (int i = 0; i < DITHERING_STRINGS.length; i++)
		{
			dithering.add(strings.get(DITHERING_STRINGS[i]));
		}
		dithering.select(1);
		panel.add(dithering);

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

	public int getErrorDiffusion()
	{
		int sel = dithering.getSelectedIndex();
		if (sel > 0)
		{
			return DITHERING_TYPES[sel];
		}
		else
		{
			return -1;
		}
	}

	private int getIntValue(TextField textField)
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

	public int getNumColors()
	{
		return getIntValue(numColorsField);
	}

	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	public boolean isOutputTypePaletted()
	{
		return outputColorType.getSelectedIndex() == 0;
	}

	public void keyPressed(KeyEvent e)
	{
		updateOkButton();
	}

	public void keyReleased(KeyEvent e)
	{
		updateOkButton();
	}

	public void keyTyped(KeyEvent e)
	{
		updateOkButton();
	}

	private void updateOkButton()
	{
		int nc = getNumColors();
		boolean enabled = nc >= 1 && nc <= 256;
		ok.setEnabled(enabled);
	}

	public boolean useErrorDiffusion()
	{
		return dithering.getSelectedIndex() > 0;
	}

	public boolean useNoDithering()
	{
		return dithering.getSelectedIndex() == 0;
	}
}
