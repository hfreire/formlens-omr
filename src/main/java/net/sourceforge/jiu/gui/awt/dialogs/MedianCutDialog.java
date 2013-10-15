/*
 * MedianCutDialog
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import net.sourceforge.jiu.apps.Strings;
import net.sourceforge.jiu.color.dithering.ErrorDiffusionDithering;
import net.sourceforge.jiu.color.quantization.MedianCutQuantizer;

/**
 * A dialog to enter the parameters for a Median Cut color quantization operation.
 * It also allows to enter the optional algorithms that can be applied in combination with Median Cut.
 * @author Marco Schmidt
 */
public class MedianCutDialog extends Dialog implements 
	ActionListener, ItemListener, KeyListener
{
	public final int[][] METHODS =
	{
		{
		 MedianCutQuantizer.METHOD_REPR_COLOR_WEIGHTED_AVERAGE,
		 MedianCutQuantizer.METHOD_REPR_COLOR_AVERAGE,
		 MedianCutQuantizer.METHOD_REPR_COLOR_MEDIAN
		},
		{
		 Strings.METHOD_REPR_COLOR_WEIGHTED_AVERAGE,
		 Strings.METHOD_REPR_COLOR_AVERAGE,
		 Strings.METHOD_REPR_COLOR_MEDIAN
		}
	};
	public final int[] ERROR_DIFFUSION_STRINGS =
	{
		Strings.FLOYD_STEINBERG_ERROR_DIFFUSION,
		Strings.STUCKI_ERROR_DIFFUSION,
	 	Strings.BURKES_ERROR_DIFFUSION,
		Strings.SIERRA_ERROR_DIFFUSION,
		Strings.JARVIS_JUDICE_NINKE_ERROR_DIFFUSION,
		Strings.STEVENSON_ARCE_ERROR_DIFFUSION
	};
	public final int[] ERROR_DIFFUSION_TYPES =
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
	private TextField numColorsField;
	private Choice outputColorType;
	private Choice reprColorMethod;
	private Choice algorithms;
	private Choice errorDiffusion;
	private TextField numPassesField;
	private TextField tauField;
	private boolean pressedOk;

	/**
	 * Creates a modal dialog to enter the parameter.
	 * @param owner the parent of this modal dialog
	 * @param strings an object to get String constants in the current language
	 * @param numColors the number of colors in the resulting image
	 * @param representativeColorMethod the method to determine the representative color from a set of colors
	 * @param paletted if true, the output image will be paletted, otherwise truecolor
	 * @param numPasses number of contour removal iterations
	 * @param initialTau maximum distance for two colors to be considered similar in contour removal
	 */
	public MedianCutDialog(Frame owner, Strings strings, int numColors, int representativeColorMethod, boolean paletted, int numPasses, double initialTau)
	{
		super(owner, strings.get(Strings.MEDIAN_CUT_COLOR_QUANTIZATION), true);
		pressedOk = false;

		Panel panel = new Panel();
		panel.setLayout(new GridLayout(0, 2));

		panel.add(new Label(strings.get(Strings.NUM_COLORS)));
		numColorsField = new TextField(Integer.toString(numColors), 6);
		numColorsField.addKeyListener(this);
		panel.add(numColorsField);

		panel.add(new Label(strings.get(Strings.OUTPUT_COLOR_TYPE)));
		outputColorType = new Choice();
		outputColorType.add(strings.get(Strings.OUTPUT_COLOR_TYPE_PALETTED));
		outputColorType.add(strings.get(Strings.OUTPUT_COLOR_TYPE_RGB));
		outputColorType.select(paletted ? 0 : 1);
		panel.add(outputColorType);

		panel.add(new Label(strings.get(Strings.METHOD_REPR_COLOR)));
		reprColorMethod = new Choice();
		for (int i = 0; i < METHODS[0].length; i++)
		{
			reprColorMethod.add(strings.get(METHODS[1][i]));
			if (representativeColorMethod == METHODS[0][i])
			{
				reprColorMethod.select(i);
			}
		}
		panel.add(reprColorMethod);

		panel.add(new Label(strings.get(Strings.OUTPUT_QUALITY_IMPROVEMENT_ALGORITHM)));
		algorithms = new Choice();
		algorithms.add(strings.get(Strings.ALGORITHMS_NONE));
		algorithms.add(strings.get(Strings.ERROR_DIFFUSION));
		algorithms.add(strings.get(Strings.CONTOUR_REMOVAL));
		algorithms.select(1);
		algorithms.addItemListener(this);
		panel.add(algorithms);

		panel.add(new Label(strings.get(Strings.ERROR_DIFFUSION)));
		errorDiffusion = new Choice();
		for (int i = 0; i < ERROR_DIFFUSION_STRINGS.length; i++)
		{
			errorDiffusion.add(strings.get(ERROR_DIFFUSION_STRINGS[i]));
		}
		errorDiffusion.select(0);
		panel.add(errorDiffusion);

		panel.add(new Label(strings.get(Strings.CONTOUR_REMOVAL_NUM_PASSES)));
		numPassesField = new TextField(Integer.toString(numPasses));
		numPassesField.addKeyListener(this);
		panel.add(numPassesField);

		panel.add(new Label(strings.get(Strings.CONTOUR_REMOVAL_TAU)));
		tauField = new TextField(Double.toString(initialTau));
		tauField.addKeyListener(this);
		panel.add(tauField);

		add(panel, BorderLayout.CENTER);

		ok = new Button(strings.get(Strings.OK));
		ok.addActionListener(this);
		cancel = new Button(strings.get(Strings.CANCEL));
		cancel.addActionListener(this);

		updateStates();

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
		if (algorithms.getSelectedIndex() == 1)
		{
			return ERROR_DIFFUSION_TYPES[errorDiffusion.getSelectedIndex()];
		}
		else
		{
			return -1;
		}
	}

	private double getDoubleValue(TextField textField)
	{
		try
		{
			Double d = new Double(textField.getText());
			return d.doubleValue();
		}
		catch (NumberFormatException nfe)
		{
			return Double.NaN;
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

	public int getNumPasses()
	{
		return getIntValue(numPassesField);
	}

	public int getReprColorMethod()
	{
		return METHODS[0][reprColorMethod.getSelectedIndex()];
	}

	public double getTau()
	{
		return getDoubleValue(tauField);
	}

	public boolean hasPressedOk()
	{
		return pressedOk;
	}

	public boolean isOutputTypePaletted()
	{
		return outputColorType.getSelectedIndex() == 0;
	}

	public void itemStateChanged(ItemEvent event)
	{
		if (event.getSource() == algorithms)
		{
			updateStates();
		}
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
		if (enabled && algorithms.getSelectedIndex() == 2)
		{
			enabled = getTau() >= 0.0 && getNumPasses() >= 1;
		}
		ok.setEnabled(enabled);
	}

	private void updateStates()
	{
		int algorithmSelection = algorithms.getSelectedIndex();
		boolean ed = algorithmSelection == 1;
		errorDiffusion.setEnabled(ed);
		ed = algorithmSelection == 2;
		tauField.setEnabled(ed);
		numPassesField.setEnabled(ed);
	}

	public boolean useContourRemoval()
	{
		return algorithms.getSelectedIndex() == 2;
	}

	public boolean useErrorDiffusion()
	{
		return algorithms.getSelectedIndex() == 1;
	}
}
