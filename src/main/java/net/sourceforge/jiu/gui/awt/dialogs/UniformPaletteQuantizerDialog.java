/*
 * UniformPaletteQuantizerDialog
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import net.sourceforge.jiu.apps.Strings;

/**
 * An AWT dialog to enter the parameters for a uniform palette color quantization operation.
 * @author Marco Schmidt
 */
public class UniformPaletteQuantizerDialog extends Dialog implements ActionListener, AdjustmentListener, ItemListener
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
	private Scrollbar redScrollbar;
	private Scrollbar greenScrollbar;
	private Scrollbar blueScrollbar;
	private Choice ditheringMethod;
	private Label infoLabel1;
	private Label infoLabel2;
	private Label redLabel;
	private Label greenLabel;
	private Label blueLabel;
	private boolean pressedOk;

	/**
	 * Creates a modal dialog to enter the parameter.
	 * @param owner the parent of this modal dialog
	 * @param strings an object to get String constants in the current language
	 * @param redBits the initial selection of the number of bits for the red channel
	 * @param greenBits the initial selection of the number of bits for the green channel
	 * @param blueBits the initial selection of the number of bits for the blue channel
	 * @param ditheringMethodSelection initial selection for dithering method
	 */
	public UniformPaletteQuantizerDialog(Frame owner, Strings strings, int redBits, 
		int greenBits, int blueBits, int ditheringMethodSelection)
	{
		super(owner, strings.get(Strings.UNIFORM_PALETTE_COLOR_QUANTIZATION), true);
		pressedOk = false;
		this.strings = strings;
		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 2));

		redLabel = new Label(strings.get(Strings.NUMBER_OF_BITS_RED));
		panel.add(redLabel);
		redScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, redBits, 1, 1, 7);
		redScrollbar.addAdjustmentListener(this);
		panel.add(redScrollbar);

		greenLabel = new Label(strings.get(Strings.NUMBER_OF_BITS_GREEN));
		panel.add(greenLabel);
		greenScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, greenBits, 1, 1, 7);
		greenScrollbar.addAdjustmentListener(this);
		panel.add(greenScrollbar);

		blueLabel = new Label(strings.get(Strings.NUMBER_OF_BITS_BLUE));
		panel.add(blueLabel);
		blueScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, blueBits, 1, 1, 7);
		blueScrollbar.addAdjustmentListener(this);
		panel.add(blueScrollbar);

		infoLabel1 = new Label();
		infoLabel2 = new Label();
		panel.add(infoLabel1);
		panel.add(infoLabel2);

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
		ditheringMethod.addItemListener(this);
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

	/*public int getNumColors()
	{
		return getValue(numColorsField);
	}*/

	public int getDitheringMethod()
	{
		return DITHERING_METHODS[0][ditheringMethod.getSelectedIndex()];
	}

	public int getBlueBits()
	{
		return blueScrollbar.getValue();
	}

	public int getGreenBits()
	{
		return greenScrollbar.getValue();
	}

	public int getRedBits()
	{
		return redScrollbar.getValue();
	}

	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	public boolean isSelectionValid()
	{
		int r = getRedBits();
		int g = getGreenBits();
		int b = getBlueBits();
		int sum = r + g + b;
		if (getDitheringMethod() == TYPE_DITHERING_NONE)
		{
			return (sum < 9);
		}
		else
		{
			return (sum < 24);
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
		if (e.getSource() == ditheringMethod)
		{
			updateLabels();
		}
	}

	private void updateLabels()
	{
		redLabel.setText(strings.get(Strings.NUMBER_OF_BITS_RED) + " (" + getRedBits() + ")");
		greenLabel.setText(strings.get(Strings.NUMBER_OF_BITS_GREEN) + " (" + getGreenBits() + ")");
		blueLabel.setText(strings.get(Strings.NUMBER_OF_BITS_BLUE) + " (" + getBlueBits() + ")");
		boolean valid = isSelectionValid();
		ok.setEnabled(valid);
		if (valid)
		{
			int totalBits = getRedBits() + getGreenBits() + getBlueBits();
			int totalColors = 1 << totalBits;
			infoLabel1.setText(strings.get(Strings.TOTAL_NUMBER_OF_BITS_AND_COLORS));
			infoLabel2.setText(Integer.toString(totalBits) + ", " + Integer.toString(totalColors));
		}
		else
		{
			infoLabel1.setText(strings.get(Strings.ERROR_NO_MORE_THAN_8_BITS));
			infoLabel2.setText("");
		}
	}
}
