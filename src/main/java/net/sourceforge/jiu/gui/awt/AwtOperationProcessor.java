/*
 * AwtOperationProcessor
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import java.awt.*;
import java.io.*;
import net.sourceforge.jiu.apps.*;
import net.sourceforge.jiu.color.*;
import net.sourceforge.jiu.color.adjustment.*;
import net.sourceforge.jiu.color.analysis.*;
import net.sourceforge.jiu.color.data.*;
import net.sourceforge.jiu.color.dithering.*;
import net.sourceforge.jiu.color.io.*;
import net.sourceforge.jiu.color.promotion.*;
import net.sourceforge.jiu.color.quantization.*;
import net.sourceforge.jiu.color.reduction.*;
import net.sourceforge.jiu.gui.awt.dialogs.*;
import net.sourceforge.jiu.codecs.*;
import net.sourceforge.jiu.data.*;
import net.sourceforge.jiu.filters.*;
import net.sourceforge.jiu.geometry.*;
import net.sourceforge.jiu.ops.*;
import net.sourceforge.jiu.util.*;

/**
 * Performs operations specified by parent class {@link OperationProcessor},
 * uses various AWT dialogs to get parameters from user in a GUI application.
 * @author Marco Schmidt
 * @since 0.8.0
 */
public class AwtOperationProcessor extends OperationProcessor
{
	private JiuAwtFrame frame;

	public AwtOperationProcessor(EditorState editorState, JiuAwtFrame awtFrame)
	{
		super(editorState);
		frame = awtFrame;
	}

	public void colorAdjustBrightness()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		Integer value = Dialogs.getInteger(frame, 
			strings.get(StringIndexConstants.ADJUST_BRIGHTNESS), 
			strings.get(StringIndexConstants.ENTER_BRIGHTNESS_VALUE), 
			-100, 0, 100, 
			strings.get(StringIndexConstants.OK),
			strings.get(StringIndexConstants.CANCEL));
		if (value == null || value.intValue() == 0)
		{
			return;
		}
		Brightness brightness = new Brightness();
		brightness.setBrightness(value.intValue());
		process(brightness);
	}

	public void colorAdjustContrast()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		Integer value = Dialogs.getInteger(frame, 
			strings.get(StringIndexConstants.ADJUST_CONTRAST), 
			strings.get(StringIndexConstants.ENTER_CONTRAST_VALUE), 
			-100, 0, 100, 
			strings.get(StringIndexConstants.OK),
			strings.get(StringIndexConstants.CANCEL));
		if (value == null || value.intValue() == 0)
		{
			return;
		}
		Contrast contrast = new Contrast();
		contrast.setContrast(value.intValue());
		process(contrast);
	}

	public void colorAdjustGamma()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		GammaCorrectionDialog gcd = new GammaCorrectionDialog(frame, strings, 2.2, GammaCorrection.MAX_GAMMA);
		gcd.setVisible(true);
		Double result = gcd.getValue();
		if (result == null)
		{
			return;
		}
		GammaCorrection gc = new GammaCorrection();
		gc.setGamma(result.doubleValue());
		process(gc);
	}

	public void colorAdjustHueSaturationValue()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		HueSaturationValueDialog hsvDialog = new HueSaturationValueDialog(frame, strings, false, 0, 0, 0);
		hsvDialog.setVisible(true);
		if (!hsvDialog.hasPressedOk())
		{
			return;
		}
		boolean setHue = hsvDialog.isHueSet();
		int hue = hsvDialog.getHue();
		int saturation = hsvDialog.getSaturation();
		int value = hsvDialog.getValue();
		HueSaturationValue hsv = new HueSaturationValue();
		if (setHue)
		{
			hsv.setHueSaturationValue(hue, saturation, value);
		}
		else
		{
			hsv.setSaturationValue(saturation, value);
		}
		process(hsv);
	}

	public void colorHistogramCountColorsUsed()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		PixelImage image = state.getImage();
		int numColors = 0;
		frame.setWaitCursor();
		try
		{
			if (image instanceof RGBIntegerImage)
			{
				Histogram3DCreator hc = new Histogram3DCreator();
				hc.setImage((RGBIntegerImage)image);
				hc.addProgressListeners(state.getProgressListeners());
				hc.process();
				Histogram3D hist = hc.getHistogram();
				numColors = hist.getNumUsedEntries();
			}
			else
			if (image instanceof IntegerImage && image.getNumChannels() == 1)
			{	
				Histogram1DCreator hc = new Histogram1DCreator();
				hc.setImage((IntegerImage)image);
				hc.addProgressListeners(state.getProgressListeners());
				hc.process();
				Histogram1D hist = hc.getHistogram();
				numColors = hist.getNumUsedEntries();
			}
			else
			{
				throw new UnsupportedTypeException("Not a supported image type for counting colors: " +
					image.getImageType().getName());
			}
		}
		catch (OperationFailedException ofe)
		{
			frame.setDefaultCursor();
			frame.updateStatusBar();
			frame.showError(ofe.toString());
			return;
		}
		frame.setDefaultCursor();
		frame.updateStatusBar();
		frame.showInfo(
			strings.get(StringIndexConstants.COUNT_COLORS_USED), 
			strings.get(StringIndexConstants.NUMBER_OF_USED_COLORS) + ": " + numColors);
	}

	public void colorHistogramEqualize()
	{
		EditorState state = getEditorState();
		try
		{
			process(new EqualizeHistogram((IntegerImage)state.getImage()));
		}
		catch (OperationFailedException ofe)
		{
			frame.showError(ofe.toString());
		}
	}

	public void colorHistogramNormalize()
	{
		EditorState state = getEditorState();
		try
		{
			process(new NormalizeHistogram((IntegerImage)state.getImage()));
		}
		catch (OperationFailedException ofe)
		{
			frame.showError(ofe.toString());
		}
	}

	public void colorHistogramTextureProperties()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		PixelImage img = state.getImage();
		if (img == null || (!(img instanceof Gray8Image)))
		{
			return;
		}
		Gray8Image image = (Gray8Image)img;
		frame.setWaitCursor();
		CoOccurrenceMatrix matrix = MatrixCreator.createCoOccurrenceMatrix(image, 0);
		TextureAnalysis ta = new TextureAnalysis();
		ta.setMatrix(matrix);
		ta.addProgressListeners(state.getProgressListeners());
		try
		{
			ta.process();
		}
		catch (MissingParameterException mpe)
		{
		}
		StringBuffer text = new StringBuffer();
		text.append(strings.get(StringIndexConstants.CONTRAST) + "=" + ta.getContrast() + "\n");
		text.append(strings.get(StringIndexConstants.CORRELATION) + "=" + ta.getCorrelation() + "\n");
		text.append(strings.get(StringIndexConstants.DISSIMILARITY) + "=" + ta.getDissimilarity() + "\n");
		text.append(strings.get(StringIndexConstants.ENTROPY) + "=" + ta.getEntropy() + "\n");
		text.append(strings.get(StringIndexConstants.ENERGY) + "=" + ta.getEnergy() + "\n");
		text.append(strings.get(StringIndexConstants.HOMOGENEITY) + "=" + ta.getHomogeneity());
		frame.setDefaultCursor();
		frame.updateStatusBar();
		frame.showInfo(
			strings.get(StringIndexConstants.TEXTURE_PROPERTIES), 
			text.toString());
	}

	public void colorHistogramSaveHistogramAs()
	{
		EditorState state = getEditorState();
		PixelImage pi = state.getImage();
		if (pi == null || !(pi instanceof IntegerImage))
		{
			return;
		}
		String textFileName = getUserSaveAsFileName(".txt", StringIndexConstants.SAVE_HISTOGRAM_AS);
		if (textFileName == null)
		{
			return;
		}
		PrintStream out = null;
		try
		{
			out = new PrintStream(new BufferedOutputStream(new FileOutputStream(textFileName)), false);
		}
		catch (IOException ioe)
		{
			frame.showError(ioe.toString());
			return;
		}
		IntegerImage image = (IntegerImage)pi;
		frame.setWaitCursor();
		int numChannels = image.getNumChannels();
		if (numChannels == 1)
		{
			Histogram1D hist = null;
			try
			{
				Histogram1DCreator hc = new Histogram1DCreator();
				hc.setImage(image);
				hc.addProgressListeners(state.getProgressListeners());
				hc.process();
				hist = hc.getHistogram();
			}
			catch(OperationFailedException ofe)
			{
				frame.showError(ofe.toString());
				frame.updateStatusBar();
				return;
			}
			HistogramSerialization.save(hist, out);
		}
		else
		if (numChannels == 3)
		{
			Histogram3D hist = null;
			try
			{
				Histogram3DCreator hc = new Histogram3DCreator();
				hc.setImage(image, RGBIndex.INDEX_RED, RGBIndex.INDEX_GREEN, RGBIndex.INDEX_BLUE);
				hc.addProgressListeners(state.getProgressListeners());
				hc.process();
				hist = hc.getHistogram();
			}
			catch(OperationFailedException ofe)
			{
				frame.showError(ofe.toString());
				frame.updateStatusBar();
				return;
			}
			HistogramSerialization.save(hist, out);
		}
		out.close();
		frame.setDefaultCursor();
		frame.updateStatusBar();
	}

	public void colorHistogramSaveCoOccurrenceMatrixAs()
	{
		EditorState state = getEditorState();
		PixelImage image = state.getImage();
		String textFileName = getUserSaveAsFileName(".txt", StringIndexConstants.SAVE_COOCCURRENCE_MATRIX);
		if (textFileName == null)
		{
			return;
		}
		CoOccurrenceMatrix matrix = MatrixCreator.createCoOccurrenceMatrix((IntegerImage)image, 0);
		File textFile = new File(textFileName);
		try
		{
			PrintStream out = new PrintStream(new FileOutputStream(textFile));
			MatrixSerialization.save(matrix, out);
			out.close();
		}
		catch(IOException ioe)
		{
			frame.showError(ioe.toString());
		}
	}

	public void colorHistogramSaveCoOccurrenceFrequencyMatrixAs()
	{
		EditorState state = getEditorState();
		PixelImage image = state.getImage();
		String textFileName = getUserSaveAsFileName(".txt", StringIndexConstants.SAVE_COOCCURRENCE_FREQUENCY_MATRIX);
		if (textFileName == null)
		{
			return;
		}
		CoOccurrenceMatrix com = MatrixCreator.createCoOccurrenceMatrix((IntegerImage)image, 0);
		CoOccurrenceFrequencyMatrix matrix = MatrixCreator.createCoOccurrenceFrequencyMatrix(com);
		File textFile = new File(textFileName);
		try
		{
			PrintStream out = new PrintStream(new FileOutputStream(textFile));
			MatrixSerialization.save(matrix, out);
			out.close();
		}
		catch(IOException ioe)
		{
			frame.showError(ioe.toString());
		}
	}

	public void colorPaletteSaveAs()
	{
		EditorState state = getEditorState();
		PalettedImage image = (PalettedImage)state.getImage();
		Palette palette = image.getPalette();
		String paletteFileName = getUserSaveAsFileName(".ppm", StringIndexConstants.SAVE_PALETTE);
		if (paletteFileName == null)
		{
			return;
		}
		File paletteFile = new File(paletteFileName);
		try
		{
			PaletteSerialization.save(palette, paletteFile);
		}
		catch(IOException ioe)
		{
			frame.showError(ioe.toString());
		}
	}

	public void colorPromotePromoteToPaletted()
	{
		process(new PromotionPaletted8());
	}

	public void colorPromotePromoteToGray8()
	{
		process(new PromotionGray8());
	}

	public void colorPromotePromoteToGray16()
	{
		process(new PromotionGray16());
	}

	public void colorPromotePromoteToRgb24()
	{
		process(new PromotionRGB24());
	}

	public void colorPromotePromoteToRgb48()
	{
		process(new PromotionRGB48());
	}

	public void colorReduceReduceNumberOfShadesOfGray()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		PixelImage image = state.getImage();
		int maxBits = image.getBitsPerPixel() - 1;
		ReduceGrayscaleDialog rgd = new ReduceGrayscaleDialog(frame, strings, 1, maxBits, 
			ReduceGrayscaleDialog.TYPE_FLOYD_STEINBERG_ERROR_DIFFUSION);
		rgd.setVisible(true);
		if (!rgd.hasPressedOk())
		{
			return;
		}
		int numBits = rgd.getNumBits();
		ImageToImageOperation op = null;
		switch (rgd.getDitheringMethod())
		{
			case(ReduceGrayscaleDialog.TYPE_DITHERING_NONE):
			{
				ReduceShadesOfGray rsog;
				rsog = new ReduceShadesOfGray();
				rsog.setBits(numBits);
				op = rsog;
				break;
			}
			case(ReduceGrayscaleDialog.TYPE_ORDERED_DITHERING):
			{
				OrderedDither od = new OrderedDither();
				od.setOutputBits(numBits);
				op = od;
				break;
			}
			case(ReduceGrayscaleDialog.TYPE_FLOYD_STEINBERG_ERROR_DIFFUSION):
			{
				ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
				ed.setTemplateType(ErrorDiffusionDithering.TYPE_FLOYD_STEINBERG);
				ed.setGrayscaleOutputBits(numBits);
				op = ed;
				break;
			}
			case(ReduceGrayscaleDialog.TYPE_STUCKI_ERROR_DIFFUSION):
			{
				ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
				ed.setTemplateType(ErrorDiffusionDithering.TYPE_STUCKI);
				ed.setGrayscaleOutputBits(numBits);
				op = ed;
				break;
			}
			case(ReduceGrayscaleDialog.TYPE_BURKES_ERROR_DIFFUSION):
			{
				ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
				ed.setTemplateType(ErrorDiffusionDithering.TYPE_BURKES);
				ed.setGrayscaleOutputBits(numBits);
				op = ed;
				break;
			}
			case(ReduceGrayscaleDialog.TYPE_SIERRA_ERROR_DIFFUSION):
			{
				ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
				ed.setTemplateType(ErrorDiffusionDithering.TYPE_SIERRA);
				ed.setGrayscaleOutputBits(numBits);
				op = ed;
				break;
			}
			case(ReduceGrayscaleDialog.TYPE_JARVIS_JUDICE_NINKE_ERROR_DIFFUSION):
			{
				ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
				ed.setTemplateType(ErrorDiffusionDithering.TYPE_JARVIS_JUDICE_NINKE);
				ed.setGrayscaleOutputBits(numBits);
				op = ed;
				break;
			}
			case(ReduceGrayscaleDialog.TYPE_STEVENSON_ARCE_ERROR_DIFFUSION):
			{
				ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
				ed.setTemplateType(ErrorDiffusionDithering.TYPE_STEVENSON_ARCE);
				ed.setGrayscaleOutputBits(numBits);
				op = ed;
				break;
			}
			default:
			{
				return;
			}
		}
		op.setInputImage(image);
		op.addProgressListeners(state.getProgressListeners());
		process(op);
	}

	public void colorReduceConvertToGrayscale()
	{
		process(new RGBToGrayConversion());
	}

	public void colorReduceMedianCut()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		PixelImage image = state.getImage();
		MedianCutDialog mcd  = new MedianCutDialog(
			frame, 
			strings, 
			256, 
			MedianCutQuantizer.METHOD_REPR_COLOR_WEIGHTED_AVERAGE, 
			true, 
			MedianCutContourRemoval.DEFAULT_NUM_PASSES, 
			MedianCutContourRemoval.DEFAULT_TAU);
		mcd.setVisible(true);
		if (!mcd.hasPressedOk())
		{
			return;
		}
		int numColors = mcd.getNumColors();
		int method = mcd.getReprColorMethod();
		boolean palettedOutput = mcd.isOutputTypePaletted();
		//int numBitsToBeCleared = 0;
		ImageToImageOperation op = null;

		if (mcd.useContourRemoval())
		{
			MedianCutQuantizer quantizer = new MedianCutQuantizer();
			quantizer.setInputImage(image);
			quantizer.setPaletteSize(numColors);
			quantizer.setMethodToDetermineRepresentativeColors(method);
			MedianCutContourRemoval removal = new MedianCutContourRemoval();
			removal.setQuantizer(quantizer);
			removal.setTau(mcd.getTau());
			removal.setNumPasses(mcd.getNumPasses());
			op = removal;
		}
		else
		if (mcd.useErrorDiffusion())
		{
			MedianCutQuantizer medianCut = new MedianCutQuantizer();
			medianCut.setInputImage(image);
			medianCut.setPaletteSize(numColors);
			medianCut.setMethodToDetermineRepresentativeColors(method);
			medianCut.setMapping(false);
			try
			{
				medianCut.process();
			}
			catch (OperationFailedException ofe)
			{
				frame.showError(ofe.toString());
				return;
			}
			ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
			ed.setTemplateType(mcd.getErrorDiffusion());
			ed.setQuantizer(medianCut);
			op = ed;
		}
		else
		{
			MedianCutQuantizer medianCut = new MedianCutQuantizer();
			medianCut.setInputImage(image);
			medianCut.setPaletteSize(numColors);
			medianCut.setTruecolorOutput(!palettedOutput);
			medianCut.setMethodToDetermineRepresentativeColors(method);
			op = medianCut;
		}
		process(op);
	}

	public void colorInvert()
	{
		process(new Invert());
	}

	public void colorConvertToMinimumColorType()
	{
		EditorState state = getEditorState();
		PixelImage image = state.getImage();
		AutoDetectColorType adct = new AutoDetectColorType();
		adct.setInputImage(image);
		adct.addProgressListeners(state.getProgressListeners());
		try
		{
			frame.setWaitCursor();
			adct.process();
		}
		catch (MissingParameterException mpe)
		{
			frame.setDefaultCursor();
			return;
		}
		catch (WrongParameterException mpe)
		{
			frame.setDefaultCursor();
			return;
		}
		if (!adct.isReducible())
		{
			frame.setDefaultCursor();
			return;
		}
		frame.setDefaultCursor();
		setImage(adct.getOutputImage(), true);
		frame.updateImage();
	}

	public void colorReduceOctree()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		PixelImage image = state.getImage();
		OctreeDialog od = new OctreeDialog(frame, strings, 256, true);
		od.setVisible(true);
		if (!(od.hasPressedOk()))
		{
			return;
		}
		OctreeColorQuantizer quantizer = new OctreeColorQuantizer();
		quantizer.setPaletteSize(od.getNumColors());
		quantizer.setInputImage(image);
		if (od.useNoDithering())
		{
			quantizer.addProgressListeners(state.getProgressListeners());
			try
			{
				quantizer.process();
			}
			catch(WrongParameterException wpe)
			{
			}	
			catch(MissingParameterException mpe)
			{
			}
			image = quantizer.getOutputImage();
		}
		else
		if (od.useErrorDiffusion())
		{
			try
			{
				quantizer.init();
			}
			catch(WrongParameterException wpe)
			{
			}	
			catch(MissingParameterException mpe)
			{
			}
			ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
			ed.setTemplateType(od.getErrorDiffusion());
			ed.setQuantizer(quantizer);
			process(ed);
			return;
		}
		setImage(image, true);
		frame.updateImage();
	}

	public void colorReduceReduceToBilevelThreshold()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		IntegerImage image = (IntegerImage)state.getImage();
		final int MAX = image.getMaxSample(0);
		Integer value = Dialogs.getInteger(frame, strings.get(StringIndexConstants.REDUCE_TO_BILEVEL_THRESHOLD), 
			strings.get(Strings.ENTER_THRESHOLD_VALUE), 0, MAX / 2, MAX, 
			strings.get(Strings.OK),
			strings.get(Strings.CANCEL));
		if (value == null)
		{
			return;
		}
		ReduceToBilevelThreshold red = new ReduceToBilevelThreshold();
		red.setThreshold(value.intValue());
		process(red);
	}

	private int convertUniformToErrorDiffusion(int utype)
	{
		switch(utype)
		{
			case(UniformPaletteQuantizerDialog.TYPE_FLOYD_STEINBERG_ERROR_DIFFUSION): return ErrorDiffusionDithering.TYPE_FLOYD_STEINBERG;
			case(UniformPaletteQuantizerDialog.TYPE_BURKES_ERROR_DIFFUSION): return ErrorDiffusionDithering.TYPE_BURKES;
			case(UniformPaletteQuantizerDialog.TYPE_STUCKI_ERROR_DIFFUSION): return ErrorDiffusionDithering.TYPE_STUCKI;
			case(UniformPaletteQuantizerDialog.TYPE_SIERRA_ERROR_DIFFUSION): return ErrorDiffusionDithering.TYPE_SIERRA;
			case(UniformPaletteQuantizerDialog.TYPE_JARVIS_JUDICE_NINKE_ERROR_DIFFUSION): return ErrorDiffusionDithering.TYPE_JARVIS_JUDICE_NINKE;
			case(UniformPaletteQuantizerDialog.TYPE_STEVENSON_ARCE_ERROR_DIFFUSION): return ErrorDiffusionDithering.TYPE_STEVENSON_ARCE;
			default: return -1;
		}
	}

	public void colorReduceUniformPalette()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		UniformPaletteQuantizerDialog upqd = new UniformPaletteQuantizerDialog
			(frame, strings, 3, 3, 2, UniformPaletteQuantizerDialog.TYPE_FLOYD_STEINBERG_ERROR_DIFFUSION);
		upqd.setVisible(true);
		if (!upqd.hasPressedOk())
		{
			return;
		}
		int redBits = upqd.getRedBits();
		int greenBits = upqd.getGreenBits();
		int blueBits = upqd.getBlueBits();
		int sum = redBits + greenBits + blueBits;
		switch (upqd.getDitheringMethod())
		{
			case(UniformPaletteQuantizerDialog.TYPE_DITHERING_NONE):
			{
				UniformPaletteQuantizer upq = new UniformPaletteQuantizer(redBits, greenBits, blueBits);
				process(upq);
				return;
			}
			case(UniformPaletteQuantizerDialog.TYPE_ORDERED_DITHERING):
			{
				OrderedDither od = new OrderedDither();
				od.setRgbBits(redBits, greenBits, blueBits);
				process(od);
				return;
			}
			case(UniformPaletteQuantizerDialog.TYPE_FLOYD_STEINBERG_ERROR_DIFFUSION):
			case(UniformPaletteQuantizerDialog.TYPE_BURKES_ERROR_DIFFUSION):
			case(UniformPaletteQuantizerDialog.TYPE_STUCKI_ERROR_DIFFUSION):
			case(UniformPaletteQuantizerDialog.TYPE_SIERRA_ERROR_DIFFUSION):
			case(UniformPaletteQuantizerDialog.TYPE_JARVIS_JUDICE_NINKE_ERROR_DIFFUSION):
			case(UniformPaletteQuantizerDialog.TYPE_STEVENSON_ARCE_ERROR_DIFFUSION):
			{
				ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
				ed.setTemplateType(convertUniformToErrorDiffusion(upqd.getDitheringMethod()));
				UniformPaletteQuantizer upq = new UniformPaletteQuantizer(redBits, greenBits, blueBits);
				ed.setQuantizer(upq);
				ed.setTruecolorOutput(sum > 8);
				process(ed);
				return;
			}
			default:
			{
				return;
			}
		}
	}

	public void colorReduceMapToArbitraryPalette()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		PixelImage image = state.getImage();
		MapToArbitraryPaletteDialog d = new MapToArbitraryPaletteDialog(frame, strings);
		d.setVisible(true);
		if (!d.hasPressedOk())
		{
			return;
		}
		int paletteType = d.getPaletteType();
		if (paletteType < 0)
		{
			return;
		}
		// load palette from file
		Palette palette;
		switch(paletteType)
		{
			case(MapToArbitraryPaletteDialog.PALETTE_FILE):
			{
				String name = getUserFileName(null, StringIndexConstants.LOAD_PALETTE, FileDialog.LOAD);
				if (name == null)
				{
					return;
				}
				File file = new File(name);
				palette = PaletteSerialization.load(file);
				break;
			}
			case(MapToArbitraryPaletteDialog.PALETTE_WEBSAFE):
			{
				palette = WebsafePaletteCreator.create();
				break;
			}
			case(MapToArbitraryPaletteDialog.PALETTE_PALM_256_COLORS):
			{
				palette = PalmCodec.createSystem8BitPalette();
				break;
			}
			case(MapToArbitraryPaletteDialog.PALETTE_PALM_16_COLORS):
			{
				palette = PalmCodec.createSystem4BitColorPalette();
				break;
			}
			case(MapToArbitraryPaletteDialog.PALETTE_PALM_16_GRAY):
			{
				palette = PalmCodec.createSystem4BitGrayscalePalette();
				break;
			}
			case(MapToArbitraryPaletteDialog.PALETTE_PALM_4_GRAY):
			{
				palette = PalmCodec.createSystem2BitGrayscalePalette();
				break;
			}
			default:
			{
				return;
			}
		}
		ArbitraryPaletteQuantizer apq = new ArbitraryPaletteQuantizer(palette);
		if (palette == null)
		{
			return;
		}
		if (d.useErrorDiffusion())
		{
			// error diffusion dithering
			ErrorDiffusionDithering ed = new ErrorDiffusionDithering();
			ed.setTemplateType(d.getErrorDiffusionType());
			ed.setQuantizer(apq);
			process(ed);
			return;
		}
		else
		{
			// no dithering
			apq.setInputImage(image);
			apq.addProgressListeners(state.getProgressListeners());
			try
			{
				apq.process();
			}
			catch (OperationFailedException ofe)
			{
				return;
			}
			image = apq.getOutputImage();
		}
		setImage(image, true);
		frame.updateImage();
	}

	public void editRedo()
	{
		EditorState state = getEditorState();
		if (!state.canRedo())
		{
			return;
		}
		state.redo();
		frame.updateImage();
	}

	public void editUndo()
	{
		EditorState state = getEditorState();
		if (!state.canUndo())
		{
			return;
		}
		state.undo();
		frame.updateImage();
	}

	public void fileClose()
	{
		EditorState state = getEditorState();
		if (state.getModified())
		{
			YesNoDialog dialog = new YesNoDialog(frame, state.getStrings(), 
				StringIndexConstants.CLOSE_FILE, 
				StringIndexConstants.DO_YOU_REALLY_WANT_TO_CLOSE_WITHOUT_SAVING, 
				false);
			dialog.setVisible(true);
			if (dialog.getResult() == YesNoDialog.RESULT_NO)
			{
				return;
			}
		}
		setImage(null, false);
		state.resetZoomFactors();
		state.setFileName("");
		state.clearRedo();
		state.clearUndo();
		frame.updateImage();
	}

	public void fileExit()
	{
		EditorState state = getEditorState();
		if (state.getModified())
		{
			YesNoDialog dialog = new YesNoDialog(frame, state.getStrings(), 
				StringIndexConstants.QUIT_PROGRAM, 
				StringIndexConstants.DO_YOU_REALLY_WANT_TO_QUIT_WITHOUT_SAVING, 
				false);
			dialog.setVisible(true);
			if (dialog.getResult() == YesNoDialog.RESULT_NO)
			{
				return;
			}
		}
		frame.setVisible(false);
		System.exit(0);
	}

	public void fileOpen(String uri)
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();

		if (state.getModified())
		{
			YesNoDialog dialog = new YesNoDialog(frame, state.getStrings(), 
				StringIndexConstants.CLOSE_FILE, 
				StringIndexConstants.DO_YOU_REALLY_WANT_TO_CLOSE_WITHOUT_SAVING, 
				false);
			dialog.setVisible(true);
			if (dialog.getResult() == YesNoDialog.RESULT_NO)
			{
				return;
			}
		}

		File file = null;
		if (uri != null)
		{
			
		}
		else
		if (state.getStartupImageName() != null)
		{
			file = new File(state.getStartupImageName());
			state.setStartupImageName(null);
		}
		else
		{
			FileDialog fd = new FileDialog(frame, strings.get(StringIndexConstants.LOAD_IMAGE_FILE), FileDialog.LOAD);
			String dir = state.getCurrentDirectory();
			if (dir != null)
			{
				fd.setDirectory(dir);
			}
			//fd.setFilenameFilter(ImageLoader.createFilenameFilter());
			fd.setVisible(true);
			fd.setMode(FileDialog.LOAD);
			String fn = fd.getFile();
			String dn = fd.getDirectory();
			if (fn == null || dn == null)
			{
				return;
			}
			state.setCurrentDirectory(dn);
			file = new File(dn, fn);
		}

		PixelImage image = null;
		String fullName = uri;
		try
		{
			if (uri != null)
			{
				image = ImageLoader.loadToolkitImageUri(uri);
			}
			else
			{
				image = ImageLoader.load(file, state.getProgressListeners());
			}
		}
		catch (Exception e)
		{
			frame.showInfo("Error loading image", e.toString());
			e.printStackTrace();
			return;
		}
		if (file != null)
		{
			fullName = file.getAbsolutePath();
			if (image == null)
			{
				image = ToolkitLoader.loadAsRgb24Image(fullName);
			}
		}
		if (image == null)
		{
			frame.showInfo(strings.get(StringIndexConstants.ERROR_LOADING_IMAGE), 
				strings.get(StringIndexConstants.FILE_FORMAT_UNKNOWN));
			return;
		}
		setImage(image, false);
		state.setFileName(fullName);
		frame.updateImage();
	}

	public void fileSaveAsBmp()
	{
		EditorState editor = getEditorState();
		PixelImage image = editor.getImage();
		if (image == null)
		{
			return;
		}
		BMPCodec codec = new BMPCodec();
		String name = getUserSaveAsFileName(codec.suggestFileExtension(image), StringIndexConstants.SAVE_IMAGE_AS);
		if (name == null)
		{
			return;
		}
		codec.addProgressListeners(editor.getProgressListeners());
		try
		{
			codec.setOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
			codec.setImage(image);
			codec.process();
		}
		catch (Exception e)
		{
			frame.showError(e.toString());
			return;
		}
		editor.setFileName(name);
		setImage(image, false);
		frame.updateImage();
	}

	public void fileSaveAsGif()
	{
		EditorState editor = getEditorState();
		PixelImage image = editor.getImage();
		if (image == null)
		{
			return;
		}
		GIFCodec codec = new GIFCodec();
		String name = getUserSaveAsFileName(codec.suggestFileExtension(image), 
			StringIndexConstants.SAVE_IMAGE_AS);
		if (name == null)
		{
			return;
		}
		codec.addProgressListeners(editor.getProgressListeners());
		try
		{
			codec.setFile(name, CodecMode.SAVE);
			codec.setImage(image);
			codec.process();
		}
		catch (Exception e)
		{
			frame.showError(e.toString());
			return;
		}
		editor.setFileName(name);
		setImage(image, false);
		frame.updateImage();
	}

	public void fileSaveAsPalm()
	{
		EditorState editor = getEditorState();
		PixelImage image = editor.getImage();
		if (image == null)
		{
			return;
		}
		PalmCodec codec = new PalmCodec();
		String name = getUserSaveAsFileName(codec.suggestFileExtension(image), StringIndexConstants.SAVE_IMAGE_AS);
		if (name == null)
		{
			return;
		}
		codec.setCompression(PalmCodec.COMPRESSION_SCANLINE);
		codec.addProgressListeners(editor.getProgressListeners());
		try
		{
			codec.setFile(name, CodecMode.SAVE);
			codec.setImage(image);
			codec.process();
		}
		catch (Exception e)
		{
			frame.showError(e.toString());
			return;
		}
		editor.setFileName(name);
		setImage(image, false);
		frame.updateImage();
	}

	public void fileSaveAsPbm()
	{
		fileSaveAsPnm();
	}

	public void fileSaveAsPgm()
	{
		fileSaveAsPnm();
	}

	public void fileSaveAsPng()
	{
		EditorState editor = getEditorState();
		PixelImage image = editor.getImage();
		if (image == null)
		{
			return;
		}
		PNGCodec codec = new PNGCodec();
		String name = getUserSaveAsFileName(codec.suggestFileExtension(image), StringIndexConstants.SAVE_IMAGE_AS);
		if (name == null)
		{
			return;
		}
		codec.addProgressListeners(editor.getProgressListeners());
		try
		{
			codec.setFile(name, CodecMode.SAVE);
			codec.setImage(image);
			codec.process();
		}
		catch (Exception e)
		{
			frame.showError(e.toString());
			return;
		}
		editor.setFileName(name);
		setImage(image, false);
		frame.updateImage();
	}

	private void fileSaveAsPnm()
	{
		EditorState editor = getEditorState();
		PixelImage image = editor.getImage();
		if (image == null)
		{
			return;
		}
		PNMCodec codec = new PNMCodec();
		String name = getUserSaveAsFileName(codec.suggestFileExtension(image), StringIndexConstants.SAVE_IMAGE_AS);
		if (name == null)
		{
			return;
		}
		codec.addProgressListeners(editor.getProgressListeners());
		try
		{
			codec.setOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
			codec.setImage(image);
			codec.process();
		}
		catch (Exception e)
		{
			frame.showError(e.toString());
			return;
		}
		editor.setFileName(name);
		setImage(image, false);
		frame.updateImage();
	}

	public void fileSaveAsPpm()
	{
		fileSaveAsPnm();
	}

	public void fileSaveAsRas()
	{
		EditorState editor = getEditorState();
		PixelImage image = editor.getImage();
		if (image == null)
		{
			return;
		}
		RASCodec codec = new RASCodec();
		String name = getUserSaveAsFileName(codec.suggestFileExtension(image), StringIndexConstants.SAVE_IMAGE_AS);
		if (name == null)
		{
			return;
		}
		codec.addProgressListeners(editor.getProgressListeners());
		try
		{
			codec.setOutputStream(new BufferedOutputStream(new FileOutputStream(name)));
			codec.setImage(image);
			codec.process();
		}
		catch (Exception e)
		{
			frame.showError(e.toString());
			return;
		}
		editor.setFileName(name);
		setImage(image, false);
		frame.updateImage();
	}

	public void filterConvolutionFilter(int type)
	{
		ConvolutionKernelFilter ckf = new ConvolutionKernelFilter();
		ckf.setKernel(type);
		process(ckf);
	}

	public void filtersBlur()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_BLUR);
	}

	public void filtersSharpen()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_SHARPEN);
	}

	public void filtersEdgeDetection()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_EDGE_DETECTION);
	}

	public void filtersEmboss()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_EMBOSS);
	}

	public void filtersPsychedelicDistillation()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_PSYCHEDELIC_DISTILLATION);
	}

	public void filtersLithograph()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_LITHOGRAPH);
	}

	public void filtersHorizontalSobel()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_HORIZONTAL_SOBEL);
	}

	public void filtersVerticalSobel()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_VERTICAL_SOBEL);
	}

	public void filtersHorizontalPrewitt()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_HORIZONTAL_PREWITT);
	}

	public void filtersVerticalPrewitt()
	{
		filterConvolutionFilter(ConvolutionKernelFilter.TYPE_VERTICAL_PREWITT);
	}

	public void filtersMaximum()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		WindowSizeDialog wsd = new WindowSizeDialog(frame, strings, StringIndexConstants.APPLY_MAXIMUM_FILTER, 3, 3);
		wsd.setVisible(true);
		if (!wsd.hasPressedOk())
		{
			return;
		}
		MaximumFilter mf = new MaximumFilter();
		mf.setArea(wsd.getWidthValue(), wsd.getHeightValue());
		process(mf);
	}

	public void filtersMedian()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		WindowSizeDialog wsd = new WindowSizeDialog(frame, strings, StringIndexConstants.APPLY_MEDIAN_FILTER, 3, 3);
		wsd.setVisible(true);
		if (!wsd.hasPressedOk())
		{
			return;
		}
		MedianFilter mf = new MedianFilter();
		mf.setArea(wsd.getWidthValue(), wsd.getHeightValue());
		process(mf);
	}

	public void filtersMean()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		WindowSizeDialog wsd = new WindowSizeDialog(frame, strings, StringIndexConstants.APPLY_MEAN_FILTER, 3, 3);
		wsd.setVisible(true);
		if (!wsd.hasPressedOk())
		{
			return;
		}
		MeanFilter mf = new MeanFilter();
		mf.setArea(wsd.getWidthValue(), wsd.getHeightValue());
		process(mf);
	}


	public void filtersMinimum()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		WindowSizeDialog wsd = new WindowSizeDialog(frame, strings, StringIndexConstants.APPLY_MINIMUM_FILTER, 3, 3);
		wsd.setVisible(true);
		if (!wsd.hasPressedOk())
		{
			return;
		}
		MinimumFilter mf = new MinimumFilter();
		mf.setArea(wsd.getWidthValue(), wsd.getHeightValue());
		process(mf);
	}

	public void filtersOil()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		WindowSizeDialog wsd = new WindowSizeDialog(frame, strings, StringIndexConstants.APPLY_OIL_FILTER, 3, 3);
		wsd.setVisible(true);
		if (!wsd.hasPressedOk())
		{
			return;
		}
		OilFilter of = new OilFilter();
		of.setArea(wsd.getWidthValue(), wsd.getHeightValue());
		process(of);
	}

	public String getUserFileName(String extension, int titleIndex, int fileDialogType)
	{
		EditorState editor = getEditorState();
		Strings strings = editor.getStrings();
		FileDialog fd = new FileDialog(frame, strings.get(titleIndex), fileDialogType);
		String currentDirectory = editor.getCurrentDirectory();
		if (currentDirectory != null)
		{
			fd.setDirectory(currentDirectory);
		}
		String fileName = editor.getFileName();
		if (fileDialogType == FileDialog.SAVE && 
		    fileName != null && 
		    extension != null)
		{
			File existingFile = new File(fileName);
			String name = existingFile.getName();
			if (name != null)
			{
				int dotIndex = name.lastIndexOf(".");
				if (dotIndex != -1)
				{
					name = name.substring(0, dotIndex);
					name += extension;
				}
			}
			fd.setFile(name);
		}
		fd.setVisible(true);
		String fn = fd.getFile();
		String dn = fd.getDirectory();
		if (fn == null || dn == null)
		{
			return null;
		}
		File file = new File(dn, fn);
		return file.getAbsolutePath();
	}

	public String getUserSaveAsFileName(String extension, int titleIndex)
	{
		return getUserFileName(extension, titleIndex, FileDialog.SAVE);
	}

	public void helpAbout()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		String message = 
			JiuAwtFrame.APP_NAME + "\n" + 
			strings.get(StringIndexConstants.HOMEPAGE) + "=" + JiuInfo.JIU_HOMEPAGE + "\n" +
			strings.get(StringIndexConstants.FEEDBACK) + "=" + JiuInfo.JIU_FEEDBACK_ADDRESS;
		frame.showInfo(strings.get(StringIndexConstants.ABOUT), message);
	}

	public void helpSystemInformation()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		frame.showInfo(strings.get(StringIndexConstants.SYSTEM_INFORMATION), 
			SystemInfo.getSystemInfo(strings) + "\n" + 
			AwtInfo.getAwtInfo(strings) + "\n" + 
			SystemInfo.getMemoryInfo(strings));
	}

	/**
	 * This method can be called for ImageToImageOperation objects.
	 */
	public void process(ImageToImageOperation op)
	{
		EditorState state = getEditorState();
		PixelImage image = state.getImage();
		if (image == null)
		{
			return;
		}
		frame.setWaitCursor();
		op.setInputImage(image);
		op.addProgressListeners(state.getProgressListeners());
		try
		{
			op.process();
			frame.setDefaultCursor();
		}
		catch (OperationFailedException ofe)
		{
			frame.setDefaultCursor();
			frame.showError(ofe.toString());
			return;
		}
		setImage(op.getOutputImage(), true);
		frame.updateImage();
	}

	public void setImage(PixelImage newImage, boolean newModified)
	{
		EditorState state = getEditorState();
		state.setImage(newImage, newModified);
	}

	public void transformationsFlip()
	{
		process(new Flip());
	}

	public void transformationsMirror()
	{
		process(new Mirror());
	}

	public void transformationsRotate90Left()
	{
		process(new Rotate90Left());
	}

	public void transformationsRotate90Right()
	{
		process(new Rotate90Right());
	}

	public void transformationsRotate180()
	{
		Rotate180 rot = new Rotate180();
		EditorState state = getEditorState();
		rot.setInputImage(state.getImage());
		process(rot);
	}

	public void transformationsCrop()
	{
		EditorState state = getEditorState();
		Strings strings = state.getStrings();
		PixelImage image = state.getImage();
		CropDialog cd = new CropDialog(frame, strings, image.getWidth(), image.getHeight());
		cd.setVisible(true);
		if (!cd.hasPressedOk())
		{
			return;
		}
		int x1 = cd.getX1();
		int x2 = cd.getX2();
		int y1 = cd.getY1();
		int y2 = cd.getY2();
		Crop crop = new Crop();
		crop.setBounds(x1, y1, x2, y2);
		process(crop);
	}

	public void transformationsShear()
	{
		EditorState state = getEditorState();
		PixelImage image = state.getImage();
		Strings strings = state.getStrings();
		ShearDialog sd = new ShearDialog(frame, strings, 45.0, image.getWidth(), image.getHeight());
		sd.setVisible(true);
		if (!sd.hasPressedOk())
		{
			return;
		}
		Double angle = sd.getValue();
		if (angle == null || angle.doubleValue() == 0.0)
		{
			return;
		}
		Shear shear = new Shear();
		shear.setAngle(angle.doubleValue());
		process(shear);
	}

	public void transformationsScale()
	{
		EditorState state = getEditorState();
		PixelImage image = state.getImage();
		Strings strings = state.getStrings();
		// a type can be chosen by the user if rgb or gray image
		boolean pickType = image instanceof RGB24Image || image instanceof Gray8Image;
		int initialType;
		if (pickType)
		{
			initialType = Resample.FILTER_TYPE_B_SPLINE;
		}
		else
		{
			initialType = Resample.FILTER_TYPE_BOX;
		}
		ScaleDialog sd = new ScaleDialog(frame, strings, image.getWidth(), 
			image.getHeight(), pickType, Resample.getFilterNames(), initialType);
		sd.setVisible(true);
		if (sd.hasPressedOk())
		{
			int newWidth = sd.getWidthValue();
			int newHeight = sd.getHeightValue();
			if (newWidth < 1 || newHeight < 1 ||
			    (newWidth == image.getWidth() && newHeight == image.getHeight()))
			{
				return;
			}
			if (pickType)
			{
				Resample resample = new Resample();
				resample.setFilter(sd.getType());
				ResampleFilter filter = resample.getFilter();
				filter.setSamplingRadius(filter.getRecommendedSamplingRadius() * 50);
				resample.setSize(newWidth, newHeight);
				process(resample);
			}
			else
			{
				ScaleReplication sc = new ScaleReplication();
				sc.setSize(newWidth, newHeight);
				process(sc);
			}
		}
	}

	public void updateFrame(PixelImage image)
	{
		EditorState state = getEditorState();
		state.setImage(image, true);
		frame.setDefaultCursor();
		frame.updateImage();
	}

	public void viewInterpolationTypeBicubic()
	{
		EditorState state = getEditorState();
		state.setInterpolation(EditorState.INTERPOLATION_BICUBIC);
		frame.updateCanvas();
	}

	public void viewInterpolationTypeBilinear()
	{
		EditorState state = getEditorState();
		state.setInterpolation(EditorState.INTERPOLATION_BILINEAR);
		frame.updateCanvas();
	}

	public void viewInterpolationTypeNearestNeighbor()
	{
		EditorState state = getEditorState();
		state.setInterpolation(EditorState.INTERPOLATION_NEAREST_NEIGHBOR);
		frame.updateCanvas();
	}

	public void viewZoomIn()
	{
		frame.zoomIn();
	}

	public void viewZoomOut()
	{
		frame.zoomOut();
	}

	public void viewSetOriginalSize()
	{
		frame.setOriginalSize();
	}
}
