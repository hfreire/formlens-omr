/*
 * OperationProcessor
 * 
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import net.sourceforge.jiu.data.BilevelImage;
import net.sourceforge.jiu.data.Gray16Image;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.Paletted8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.data.RGB48Image;

/**
 * Abstract base class for performing JIU operations in combination
 * with an {@link EditorState}.
 * @author Marco Schmidt
 */
public abstract class OperationProcessor implements MenuIndexConstants
{
	private EditorState state;

	/**
	 * Create an object of this class, storing the state argument for later use.
	 * @param editorState EditorState object to be used for processing
	 */
	public OperationProcessor(EditorState editorState)
	{
		state = editorState;
	}

	/**
	 * Adjust the brightness of the current image.
	 */
	public abstract void colorAdjustBrightness();

	/**
	 * Adjust the contrast of the current image.
	 */
	public abstract void colorAdjustContrast();

	/**
	 * Adjust the gamma value of the current image.
	 */
	public abstract void colorAdjustGamma();

	/**
	 * Adjust hue, saturation and value of the current image.
	 */
	public abstract void colorAdjustHueSaturationValue();

	/**
	 * Count the number of colors used in the current image.
	 */
	public abstract void colorHistogramCountColorsUsed();

	public abstract void colorHistogramEqualize();

	public abstract void colorHistogramNormalize();

	public abstract void colorHistogramTextureProperties();

	public abstract void colorHistogramSaveHistogramAs();

	public abstract void colorHistogramSaveCoOccurrenceMatrixAs();

	public abstract void colorHistogramSaveCoOccurrenceFrequencyMatrixAs();

	public abstract void colorPaletteSaveAs();

	public abstract void colorPromotePromoteToPaletted();

	public abstract void colorPromotePromoteToGray8();

	public abstract void colorPromotePromoteToGray16();

	public abstract void colorPromotePromoteToRgb24();

	public abstract void colorPromotePromoteToRgb48();

	public abstract void colorReduceReduceNumberOfShadesOfGray();

	public abstract void colorReduceConvertToGrayscale();

	public abstract void colorReduceMedianCut();

	public abstract void colorReduceOctree();

	public abstract void colorReduceReduceToBilevelThreshold();

	public abstract void colorReduceUniformPalette();

	public abstract void colorReduceMapToArbitraryPalette();

	public abstract void colorConvertToMinimumColorType();

	public abstract void colorInvert();

	public abstract void editRedo();

	public abstract void editUndo();

	public abstract void filtersBlur();

	public abstract void filtersSharpen();

	public abstract void filtersEdgeDetection();

	public abstract void filtersEmboss();

	public abstract void filtersPsychedelicDistillation();

	public abstract void filtersLithograph();

	public abstract void filtersHorizontalSobel();

	public abstract void filtersVerticalSobel();

	public abstract void filtersHorizontalPrewitt();

	public abstract void filtersVerticalPrewitt();

	public abstract void filtersMaximum();

	public abstract void filtersMedian();

	public abstract void filtersMean();

	public abstract void filtersMinimum();

	public abstract void filtersOil();

	public abstract void transformationsFlip();

	public abstract void transformationsMirror();

	public abstract void transformationsRotate90Left();

	public abstract void transformationsRotate90Right();

	public abstract void transformationsRotate180();

	public abstract void transformationsCrop();

	public abstract void transformationsShear();

	public abstract void transformationsScale();

	public abstract void viewInterpolationTypeBicubic();

	public abstract void viewInterpolationTypeBilinear();

	public abstract void viewInterpolationTypeNearestNeighbor();

	public abstract void viewZoomIn();

	public abstract void viewZoomOut();

	public abstract void viewSetOriginalSize();


	/**
	 * If there is an image loaded in the application, remove the image.
	 */
	public abstract void fileClose();

	/**
	 * Terminate the application.
	 * If changes were not saved, the user should be asked whether these changes 
	 * should be discarded.
	 */
	public abstract void fileExit();

	/**
	 * Load an image in the application.
	 */
	public abstract void fileOpen(String uri);

	/**
	 * Save the current image as a Windows BMP file.
	 */
	public abstract void fileSaveAsBmp();

	/**
	 * Save the current image as a GIF file.
	 */
	public abstract void fileSaveAsGif();

	/**
	 * Save the current image as a Palm image file.
	 */
	public abstract void fileSaveAsPalm();

	/**
	 * Save the current image as a Portable Bitmap file.
	 */
	public abstract void fileSaveAsPbm();

	/**
	 * Save the current image as a Portable Graymap file.
	 */
	public abstract void fileSaveAsPgm();

	/**
	 * Save the current image as a Portable Network Graphics file.
	 */
	public abstract void fileSaveAsPng();

	/**
	 * Save the current image as a Portable Pixmap file.
	 */
	public abstract void fileSaveAsPpm();

	/**
	 * Save the current image as a Sun Raster file.
	 */
	public abstract void fileSaveAsRas();

	/**
	 * Returns the EditorState object given to this object's constructor.
	 * @return EditorState object used by this processor
	 */
	public EditorState getEditorState()
	{
		return state;
	}

	/**
	 * Display information about the application:
	 * name, version, feedback email address, website.
	 */
	public abstract void helpAbout();

	/**
	 * Display information on the system this application
	 * is currently running on.
	 */
	public abstract void helpSystemInformation();

	/**
	 * Returns if the operation given by the menu index (from {@link MenuIndexConstants}
	 * is available regarding the current editor state.
	 * This method is used to update the enabled status of menu items so that
	 * they reflect what can be done in the current state of an application.
	 * Thus, things that cannot be done cannot be chosen in the menu
	 * because they are disabled.
	 * Example: the File | Save as... items are disabled as long as there is no image loaded,
	 * simply because there is nothing to save.
	 * @param menuIndex index of menu item to be checked
	 * @return whether the operation is available (if true, menu item
	 *  should be enabled)
	 */
	public boolean isAvailable(int menuIndex)
	{
		boolean hasImage = state.hasImage();
		PixelImage image = state.getImage();
		boolean bilevel = hasImage && image instanceof BilevelImage;
		boolean gray8 = hasImage && image instanceof Gray8Image;
		boolean gray16 = hasImage && image instanceof Gray16Image;
		//boolean gray = gray8 || gray16;
		boolean pal8 = hasImage && image instanceof Paletted8Image;
		boolean rgb24 = hasImage && image instanceof RGB24Image;
		boolean rgb48 = hasImage && image instanceof RGB48Image;
		boolean rgb = rgb24 || rgb48;
		switch(menuIndex)
		{
			case(FILE):
			{
				return true;
			}
			case(FILE_OPEN):
			{
				return true;
			}
			case(FILE_CLOSE):
			{
				return hasImage;
			}
			case(FILE_SAVEAS):
			{
				return hasImage;
			}
			case(FILE_SAVEAS_GIF):
			{
				return bilevel || gray8 || pal8;
			}
			case(FILE_SAVEAS_PALM):
			{
				return bilevel || gray8 || pal8 || rgb24;
			}
			case(FILE_SAVEAS_PBM):
			{
				return bilevel;
			}
			case(FILE_SAVEAS_PGM):
			{
				return gray8 || gray16;
			}
			case(FILE_SAVEAS_PNG):
			{
				return hasImage;
			}
			case(FILE_SAVEAS_PPM):
			{
				return rgb24 || rgb48;
			}
			case(FILE_SAVEAS_SUNRASTER):
			{
				return pal8;
			}
			case(FILE_SAVEAS_WINDOWSBMP):
			{
				return bilevel || gray8 || pal8 || rgb24;
			}
			case(FILE_IMAGE_1):
			{
				return true;
			}
			case(FILE_EXIT):
			{
				return true;
			}
			case(EDIT):
			{
				return state.canUndo() || state.canRedo();
			}
			case(EDIT_UNDO):
			{
				return state.canUndo();
			}
			case(EDIT_REDO):
			{
				return state.canRedo();
			}
			case(COLOR):
			{
				return hasImage;
			}
			case(COLOR_ADJUST):
			{
				return !bilevel;
			}
			case(COLOR_ADJUST_BRIGHTNESS):
			{
				return gray8 || gray16 || pal8 || rgb24 || rgb48;
			}
			case(COLOR_ADJUST_CONTRAST):
			{
				return gray8 || gray16 || pal8 || rgb24 || rgb48;
			}
			case(COLOR_ADJUST_GAMMA):
			{
				return gray8 || gray16 || pal8 || rgb24 || rgb48;
			}
			case(COLOR_HISTOGRAM):
			{
				return hasImage;
			}
			case(COLOR_HISTOGRAM_COUNTCOLORSUSED):
			{
				return hasImage;
			}
			case(COLOR_HISTOGRAM_EQUALIZE):
			{
				return gray8 || pal8 || rgb24 || rgb48;
			}
			case(COLOR_HISTOGRAM_NORMALIZE):
			{
				return gray8 || pal8 || rgb24 || rgb48;
			}
			case(COLOR_HISTOGRAM_TEXTUREPROPERTIES):
			{
				return gray8;
			}
			case(COLOR_HISTOGRAM_SAVEHISTOGRAMAS):
			{
				return hasImage;
			}
			case(COLOR_HISTOGRAM_SAVECOOCCURRENCEMATRIXAS):
			{
				return pal8 || gray8 || bilevel; 
			}
			case(COLOR_HISTOGRAM_SAVECOOCCURRENCEFREQUENCYMATRIXAS):
			{
				return pal8 || gray8 || bilevel;
			}
			case(COLOR_PALETTE):
			{
				return pal8;
			}
			case(COLOR_PALETTE_SAVEAS):
			{
				return pal8;
			}
			case(COLOR_PROMOTE):
			{
				return !rgb48;
			}
			case(COLOR_PROMOTE_PROMOTETOGRAY8):
			{
				return bilevel;
			}
			case(COLOR_PROMOTE_PROMOTETOGRAY16):
			{
				return bilevel || gray8;
			}
			case(COLOR_PROMOTE_PROMOTETORGB24):
			{
				return bilevel || gray8 || pal8;
			}
			case(COLOR_PROMOTE_PROMOTETORGB48):
			{
				return bilevel || gray8 || gray16 || pal8 || rgb24;
			}
			case(COLOR_PROMOTE_PROMOTETOPALETTED):
			{
				return bilevel || gray8;
			}
			case(COLOR_ADJUST_HUESATURATIONVALUE):
			{
				return pal8 || rgb24;
			}
			case(COLOR_REDUCE):
			{
				return !bilevel;
			}
			case(COLOR_REDUCE_CONVERTTOGRAYSCALE):
			{
				return pal8 || rgb;
			}
			case(COLOR_REDUCE_REDUCENUMBEROFSHADESOFGRAY):
			{
				return gray8 || gray16;
			}
			case(COLOR_REDUCE_REDUCETOBILEVELTHRESHOLD):
			{
				return gray8 || gray16;
			}
			case(COLOR_REDUCE_MEDIANCUT):
			{
				return rgb24;
			}
			case(COLOR_REDUCE_OCTREE):
			{
				return rgb24;
			}
			case(COLOR_REDUCE_UNIFORMPALETTE):
			{
				return rgb24;
			}
			case(COLOR_REDUCE_MAPTOARBITRARYPALETTE):
			{
				return rgb24;
			}
			case(COLOR_INVERT):
			{
				return hasImage;
			}
			case(COLOR_CONVERTTOMINIMUMCOLORTYPE):
			{
				return !bilevel;
			}
			case(TRANSFORMATIONS):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_FLIP):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_MIRROR):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_ROTATELEFT90):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_ROTATERIGHT90):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_ROTATE180):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_SHEAR):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_SCALE):
			{
				return hasImage;
			}
			case(TRANSFORMATIONS_CROP):
			{
				return hasImage;
			}
			case(FILTERS):
			{
				return hasImage && !pal8;
			}
			case(FILTERS_BLUR):
			case(FILTERS_SHARPEN):
			case(FILTERS_EDGEDETECTION):
			case(FILTERS_EMBOSS):
			case(FILTERS_PSYCHEDELICDISTILLATION):
			case(FILTERS_LITHOGRAPH):
			case(FILTERS_HORIZONTALSOBEL):
			case(FILTERS_VERTICALSOBEL):
			case(FILTERS_HORIZONTALPREWITT):
			case(FILTERS_VERTICALPREWITT):
			case(FILTERS_MEAN):
			case(FILTERS_OIL):
			{
				return gray16 || gray8 || rgb24 || rgb48;
			}
			case(FILTERS_MAXIMUM):
			case(FILTERS_MINIMUM):
			case(FILTERS_MEDIAN):
			{
				return gray16 || gray8 || rgb24 || bilevel || rgb48;
			}
			case(VIEW):
			case(VIEW_INTERPOLATIONTYPE):
			case(VIEW_INTERPOLATIONTYPE_NEARESTNEIGHBOR):
			case(VIEW_INTERPOLATIONTYPE_BILINEAR):
			case(VIEW_INTERPOLATIONTYPE_BICUBIC):
			{
				return hasImage;
			}
			case(VIEW_SETORIGINALSIZE):
			{
				return hasImage && !state.isZoomOriginalSize();
			}
			case(VIEW_ZOOMIN):
			{
				return hasImage && !state.isMaximumZoom();
			}
			case(VIEW_ZOOMOUT):
			{
				return hasImage && !state.isMinimumZoom();
			}
			case(HELP):
			{
				return true;
			}
			case(HELP_ABOUT):
			{
				return true;
			}
			case(HELP_SYSTEMINFORMATION):
			{
				return true;
			}
			default:
			{
				throw new IllegalArgumentException("Not a valid menu index: " + menuIndex);
			}
		}
	}

	public void process(int menuIndex)
	{
		switch (menuIndex)
		{
			case(FILE_OPEN):
			{
				fileOpen(null);
				break;
			}
			case(FILE_SAVEAS_GIF):
			{
				fileSaveAsGif();
				break;
			}
			case(FILE_SAVEAS_PALM):
			{
				fileSaveAsPalm();
				break;
			}
			case(FILE_SAVEAS_PBM):
			{
				fileSaveAsPbm();
				break;
			}
			case(FILE_SAVEAS_PGM):
			{
				fileSaveAsPgm();
				break;
			}
			case(FILE_SAVEAS_PNG):
			{
				fileSaveAsPng();
				break;
			}
			case(FILE_SAVEAS_PPM):
			{
				fileSaveAsPpm();
				break;
			}
			case(FILE_SAVEAS_SUNRASTER):
			{
				fileSaveAsRas();
				break;
			}
			case(FILE_SAVEAS_WINDOWSBMP):
			{
				fileSaveAsBmp();
				break;
			}
			case(FILE_IMAGE_1):
			{
				fileOpen("/resources/images/image1.jpg");
				break;
			}
			case(FILE_CLOSE):
			{
				fileClose();
				break;
			}
			case(FILE_EXIT):
			{
				fileExit();
				break;
			}
			case(EDIT_UNDO):
			{
				editUndo();
				break;
			}
			case(EDIT_REDO):
			{
				editRedo();
				break;
			}
			case(COLOR_ADJUST_BRIGHTNESS):
			{
				colorAdjustBrightness();
				break;
			}
			case(COLOR_ADJUST_CONTRAST):
			{
				colorAdjustContrast();
				break;
			}
			case(COLOR_ADJUST_GAMMA):
			{
				colorAdjustGamma();
				break;
			}
			case(COLOR_ADJUST_HUESATURATIONVALUE):
			{
				colorAdjustHueSaturationValue();
				break;
			}
			case(COLOR_HISTOGRAM_COUNTCOLORSUSED):
			{
				colorHistogramCountColorsUsed();
				break;
			}
			case(COLOR_HISTOGRAM_EQUALIZE):
			{
				colorHistogramEqualize();
				break;
			}
			case(COLOR_HISTOGRAM_NORMALIZE):
			{
				colorHistogramNormalize();
				break;
			}
			case(COLOR_HISTOGRAM_TEXTUREPROPERTIES):
			{
				colorHistogramTextureProperties();
				break;
			}
			case(COLOR_HISTOGRAM_SAVEHISTOGRAMAS):
			{
				colorHistogramSaveHistogramAs();
				break;
			}
			case(COLOR_HISTOGRAM_SAVECOOCCURRENCEMATRIXAS):
			{
				colorHistogramSaveCoOccurrenceMatrixAs();
				break;
			}
			case(COLOR_HISTOGRAM_SAVECOOCCURRENCEFREQUENCYMATRIXAS):
			{
				colorHistogramSaveCoOccurrenceFrequencyMatrixAs();
				break;
			}
			case(COLOR_PALETTE_SAVEAS):
			{
				colorPaletteSaveAs();
				break;
			}
			case(COLOR_PROMOTE_PROMOTETOPALETTED):
			{
				colorPromotePromoteToPaletted();
				break;
			}
			case(COLOR_PROMOTE_PROMOTETOGRAY8):
			{
				colorPromotePromoteToGray8();
				break;
			}
			case(COLOR_PROMOTE_PROMOTETOGRAY16):
			{
				colorPromotePromoteToGray16();
				break;
			}
			case(COLOR_PROMOTE_PROMOTETORGB24):
			{
				colorPromotePromoteToRgb24();
				break;
			}
			case(COLOR_PROMOTE_PROMOTETORGB48):
			{
				colorPromotePromoteToRgb48();
				break;
			}
			case(COLOR_REDUCE_REDUCETOBILEVELTHRESHOLD):
			{
				colorReduceReduceToBilevelThreshold();
				break;
			}
			case(COLOR_REDUCE_REDUCENUMBEROFSHADESOFGRAY):
			{
				colorReduceReduceNumberOfShadesOfGray();
				break;
			}
			case(COLOR_REDUCE_CONVERTTOGRAYSCALE):
			{
				colorReduceConvertToGrayscale();
				break;
			}
			case(COLOR_REDUCE_MEDIANCUT):
			{
				colorReduceMedianCut();
				break;
			}
			case(COLOR_REDUCE_OCTREE):
			{
				colorReduceOctree();
				break;
			}
			case(COLOR_REDUCE_UNIFORMPALETTE):
			{
				colorReduceUniformPalette();
				break;
			}
			case(COLOR_REDUCE_MAPTOARBITRARYPALETTE):
			{
				colorReduceMapToArbitraryPalette();
				break;
			}
			case(COLOR_INVERT):
			{
				colorInvert();
				break;
			}
			case(COLOR_CONVERTTOMINIMUMCOLORTYPE):
			{
				colorConvertToMinimumColorType();
				break;
			}
			case(TRANSFORMATIONS_FLIP):
			{
				transformationsFlip();
				break;
			}
			case(TRANSFORMATIONS_MIRROR):
			{
				transformationsMirror();
				break;
			}
			case(TRANSFORMATIONS_ROTATELEFT90):
			{
				transformationsRotate90Left();
				break;
			}
			case(TRANSFORMATIONS_ROTATERIGHT90):
			{
				transformationsRotate90Right();
				break;
			}
			case(TRANSFORMATIONS_ROTATE180):
			{
				transformationsRotate180();
				break;
			}
			case(TRANSFORMATIONS_CROP):
			{
				transformationsCrop();
				break;
			}
			case(TRANSFORMATIONS_SCALE):
			{
				transformationsScale();
				break;
			}
			case(TRANSFORMATIONS_SHEAR):
			{
				transformationsShear();
				break;
			}
			case(FILTERS_BLUR):
			{
				filtersBlur();
				break;
			}
			case(FILTERS_SHARPEN):
			{
				filtersSharpen();
				break;
			}
			case(FILTERS_EDGEDETECTION):
			{
				filtersEdgeDetection();
				break;
			}
			case(FILTERS_EMBOSS):
			{
				filtersEmboss();
				break;
			}
			case(FILTERS_PSYCHEDELICDISTILLATION):
			{
				filtersPsychedelicDistillation();
				break;
			}
			case(FILTERS_LITHOGRAPH):
			{
				filtersLithograph();
				break;
			}
			case(FILTERS_HORIZONTALSOBEL):
			{
				filtersHorizontalSobel();
				break;
			}
			case(FILTERS_VERTICALSOBEL):
			{
				filtersVerticalSobel();
				break;
			}
			case(FILTERS_HORIZONTALPREWITT):
			{
				filtersHorizontalPrewitt();
				break;
			}
			case(FILTERS_VERTICALPREWITT):
			{
				filtersVerticalPrewitt();
				break;
			}
			case(FILTERS_MINIMUM):
			{
				filtersMinimum();
				break;
			}
			case(FILTERS_MAXIMUM):
			{
				filtersMaximum();
				break;
			}
			case(FILTERS_MEDIAN):
			{
				filtersMedian();
				break;
			}
			case(FILTERS_MEAN):
			{
				filtersMean();
				break;
			}
			case(FILTERS_OIL):
			{
				filtersOil();
				break;
			}
			case(VIEW_ZOOMIN):
			{
				viewZoomIn();
				break;
			}
			case(VIEW_ZOOMOUT):
			{
				viewZoomOut();
				break;
			}
			case(VIEW_SETORIGINALSIZE):
			{
				viewSetOriginalSize();
				break;
			}
			case(VIEW_INTERPOLATIONTYPE_NEARESTNEIGHBOR):
			{
				viewInterpolationTypeNearestNeighbor();
				break;
			}
			case(VIEW_INTERPOLATIONTYPE_BILINEAR):
			{
				viewInterpolationTypeBilinear();
				break;
			}
			case(VIEW_INTERPOLATIONTYPE_BICUBIC):
			{
				viewInterpolationTypeBicubic();
				break;
			}
			case(HELP_ABOUT):
			{
				helpAbout();
				break;
			}
			case(HELP_SYSTEMINFORMATION):
			{
				helpSystemInformation();
				break;
			}
			default:
			{
				// error?
				break;
			}
		}
	}
}
