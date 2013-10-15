/*
 * MenuWrapper
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

/**
 * Abstract menu wrapper.
 * A menu consists of a number of menu elements, each of which have a text,
 * an enabled status and an int constant from MenuIndexConstants associated with it.
 * @author Marco Schmidt
 */
public abstract class MenuWrapper
{
	/**
	 * Attempts to find the index of a given object that represents a menu element.
	 * @param o some object representing part of the menu
	 * @return corresponding index value from {@link MenuIndexConstants} on success
	 *  or -1 on failure
	 */
	public abstract int findIndex(Object o);

	/**
	 * For one of the values in {@link MenuIndexConstants}, returns the corresponding 
	 * constant in {@link StringIndexConstants}.
	 * @param menuIndex int value from the MenuIndexConstants interface
	 * @return int value from the StringIndexConstants interface
	 */
	public int getStringIndex(int menuIndex)
	{
		switch(menuIndex)
		{
			case(MenuIndexConstants.FILE): return StringIndexConstants.FILE;
			case(MenuIndexConstants.FILE_OPEN): return StringIndexConstants.OPEN;
			case(MenuIndexConstants.FILE_SAVEAS): return StringIndexConstants.SAVE_AS;
			case(MenuIndexConstants.FILE_SAVEAS_GIF): return StringIndexConstants.GIF;
			case(MenuIndexConstants.FILE_SAVEAS_PALM): return StringIndexConstants.PALM;
			case(MenuIndexConstants.FILE_SAVEAS_PBM): return StringIndexConstants.PORTABLE_BITMAP;
			case(MenuIndexConstants.FILE_SAVEAS_PGM): return StringIndexConstants.PORTABLE_GRAYMAP;
			case(MenuIndexConstants.FILE_SAVEAS_PNG): return StringIndexConstants.PORTABLE_NETWORK_GRAPHICS;
			case(MenuIndexConstants.FILE_SAVEAS_PPM): return StringIndexConstants.PORTABLE_PIXMAP;
			case(MenuIndexConstants.FILE_SAVEAS_SUNRASTER): return StringIndexConstants.SUN_RASTER;
			case(MenuIndexConstants.FILE_SAVEAS_WINDOWSBMP): return StringIndexConstants.WINDOWS_BITMAP;
			case(MenuIndexConstants.FILE_IMAGE_1): return StringIndexConstants.IMAGE_1;
			case(MenuIndexConstants.FILE_CLOSE): return StringIndexConstants.CLOSE;
			case(MenuIndexConstants.FILE_EXIT): return StringIndexConstants.EXIT;
			case(MenuIndexConstants.EDIT): return StringIndexConstants.EDIT;
			case(MenuIndexConstants.EDIT_UNDO): return StringIndexConstants.EDIT_UNDO;
			case(MenuIndexConstants.EDIT_REDO): return StringIndexConstants.EDIT_REDO;
			case(MenuIndexConstants.COLOR): return StringIndexConstants.COLOR;
			case(MenuIndexConstants.COLOR_ADJUST): return StringIndexConstants.ADJUST;
			case(MenuIndexConstants.COLOR_ADJUST_BRIGHTNESS): return StringIndexConstants.BRIGHTNESS_MENU_ITEM;
			case(MenuIndexConstants.COLOR_ADJUST_CONTRAST): return StringIndexConstants.CONTRAST_MENU_ITEM;
			case(MenuIndexConstants.COLOR_ADJUST_GAMMA): return StringIndexConstants.GAMMA_MENU_ITEM;
			case(MenuIndexConstants.COLOR_ADJUST_HUESATURATIONVALUE): return StringIndexConstants.HUE_SATURATION_VALUE_MENU_ITEM;
			case(MenuIndexConstants.COLOR_HISTOGRAM): return StringIndexConstants.HISTOGRAM;
			case(MenuIndexConstants.COLOR_HISTOGRAM_COUNTCOLORSUSED): return StringIndexConstants.COUNT_COLORS_USED;
			case(MenuIndexConstants.COLOR_HISTOGRAM_EQUALIZE): return StringIndexConstants.EQUALIZE_HISTOGRAM_MENU_ITEM;
			case(MenuIndexConstants.COLOR_HISTOGRAM_NORMALIZE): return StringIndexConstants.NORMALIZE_HISTOGRAM_MENU_ITEM;
			case(MenuIndexConstants.COLOR_HISTOGRAM_TEXTUREPROPERTIES): return StringIndexConstants.TEXTURE_PROPERTIES_MENU_ITEM;
			case(MenuIndexConstants.COLOR_HISTOGRAM_SAVEHISTOGRAMAS): return StringIndexConstants.SAVE_HISTOGRAM_AS_MENU_ITEM;
			case(MenuIndexConstants.COLOR_HISTOGRAM_SAVECOOCCURRENCEMATRIXAS): return StringIndexConstants.SAVE_COOCCURRENCE_MATRIX_MENU_ITEM;
			case(MenuIndexConstants.COLOR_HISTOGRAM_SAVECOOCCURRENCEFREQUENCYMATRIXAS): return StringIndexConstants.SAVE_COOCCURRENCE_FREQUENCY_MATRIX_MENU_ITEM;
			case(MenuIndexConstants.COLOR_PALETTE): return StringIndexConstants.PALETTE_MENU_ITEM;
			case(MenuIndexConstants.COLOR_PALETTE_SAVEAS): return StringIndexConstants.PALETTE_SAVE_AS_MENU_ITEM;
			case(MenuIndexConstants.COLOR_PROMOTE): return StringIndexConstants.PROMOTE;
			case(MenuIndexConstants.COLOR_PROMOTE_PROMOTETOPALETTED): return StringIndexConstants.PROMOTE_TO_PALETTED;
			case(MenuIndexConstants.COLOR_PROMOTE_PROMOTETOGRAY8): return StringIndexConstants.PROMOTE_TO_GRAY8;
			case(MenuIndexConstants.COLOR_PROMOTE_PROMOTETOGRAY16): return StringIndexConstants.PROMOTE_TO_GRAY16;
			case(MenuIndexConstants.COLOR_PROMOTE_PROMOTETORGB24): return StringIndexConstants.PROMOTE_TO_RGB24;
			case(MenuIndexConstants.COLOR_PROMOTE_PROMOTETORGB48): return StringIndexConstants.PROMOTE_TO_RGB48;
			case(MenuIndexConstants.COLOR_REDUCE): return StringIndexConstants.REDUCE;
			case(MenuIndexConstants.COLOR_REDUCE_REDUCETOBILEVELTHRESHOLD): return StringIndexConstants.REDUCE_TO_BILEVEL_THRESHOLD_MENU_ITEM;
			case(MenuIndexConstants.COLOR_REDUCE_REDUCENUMBEROFSHADESOFGRAY): return StringIndexConstants.REDUCE_NUMBER_OF_SHADES_OF_GRAY_MENU_ITEM;
			case(MenuIndexConstants.COLOR_REDUCE_CONVERTTOGRAYSCALE): return StringIndexConstants.CONVERT_TO_GRAYSCALE;
			case(MenuIndexConstants.COLOR_REDUCE_MEDIANCUT): return StringIndexConstants.MEDIAN_CUT;
			case(MenuIndexConstants.COLOR_REDUCE_OCTREE): return StringIndexConstants.OCTREE_COLOR_QUANTIZATION_MENU_ITEM;
			case(MenuIndexConstants.COLOR_REDUCE_UNIFORMPALETTE): return StringIndexConstants.UNIFORM_PALETTE_COLOR_QUANTIZATION_MENU_ITEM;
			case(MenuIndexConstants.COLOR_REDUCE_MAPTOARBITRARYPALETTE): return StringIndexConstants.MAP_TO_ARBITRARY_PALETTE_MENU_ITEM;
			case(MenuIndexConstants.COLOR_INVERT): return StringIndexConstants.INVERT;
			case(MenuIndexConstants.COLOR_CONVERTTOMINIMUMCOLORTYPE): return StringIndexConstants.CONVERT_TO_MINIMUM_COLOR_TYPE_MENU_ITEM;
			case(MenuIndexConstants.TRANSFORMATIONS): return StringIndexConstants.TRANSFORMATIONS;
			case(MenuIndexConstants.TRANSFORMATIONS_FLIP): return StringIndexConstants.FLIP;
			case(MenuIndexConstants.TRANSFORMATIONS_MIRROR): return StringIndexConstants.MIRROR;
			case(MenuIndexConstants.TRANSFORMATIONS_ROTATELEFT90): return StringIndexConstants.ROTATE_90_LEFT;
			case(MenuIndexConstants.TRANSFORMATIONS_ROTATERIGHT90): return StringIndexConstants.ROTATE_90_RIGHT;
			case(MenuIndexConstants.TRANSFORMATIONS_ROTATE180): return StringIndexConstants.ROTATE_180;
			case(MenuIndexConstants.TRANSFORMATIONS_CROP): return StringIndexConstants.CROP_MENU_ITEM;
			case(MenuIndexConstants.TRANSFORMATIONS_SHEAR): return StringIndexConstants.SHEAR_MENU_ITEM;
			case(MenuIndexConstants.TRANSFORMATIONS_SCALE): return StringIndexConstants.SCALE;
			case(MenuIndexConstants.FILTERS): return StringIndexConstants.FILTERS;
			case(MenuIndexConstants.FILTERS_BLUR): return StringIndexConstants.BLUR;
			case(MenuIndexConstants.FILTERS_SHARPEN): return StringIndexConstants.SHARPEN;
			case(MenuIndexConstants.FILTERS_EDGEDETECTION): return StringIndexConstants.EDGE_DETECTION;
			case(MenuIndexConstants.FILTERS_EMBOSS): return StringIndexConstants.EMBOSS;
			case(MenuIndexConstants.FILTERS_PSYCHEDELICDISTILLATION): return StringIndexConstants.PSYCHEDELIC_DISTILLATION;
			case(MenuIndexConstants.FILTERS_LITHOGRAPH): return StringIndexConstants.LITHOGRAPH;
			case(MenuIndexConstants.FILTERS_HORIZONTALSOBEL): return StringIndexConstants.HORIZONTAL_SOBEL;
			case(MenuIndexConstants.FILTERS_VERTICALSOBEL): return StringIndexConstants.VERTICAL_SOBEL;
			case(MenuIndexConstants.FILTERS_HORIZONTALPREWITT): return StringIndexConstants.HORIZONTAL_PREWITT;
			case(MenuIndexConstants.FILTERS_VERTICALPREWITT): return StringIndexConstants.VERTICAL_PREWITT;
			case(MenuIndexConstants.FILTERS_MINIMUM): return StringIndexConstants.MINIMUM_FILTER_MENU_ITEM;
			case(MenuIndexConstants.FILTERS_MAXIMUM): return StringIndexConstants.MAXIMUM_FILTER_MENU_ITEM;
			case(MenuIndexConstants.FILTERS_MEDIAN): return StringIndexConstants.MEDIAN_FILTER_MENU_ITEM;
			case(MenuIndexConstants.FILTERS_MEAN): return StringIndexConstants.MEAN_FILTER_MENU_ITEM;
			case(MenuIndexConstants.FILTERS_OIL): return StringIndexConstants.OIL_FILTER_MENU_ITEM;
			case(MenuIndexConstants.VIEW): return StringIndexConstants.VIEW;
			case(MenuIndexConstants.VIEW_ZOOMIN): return StringIndexConstants.VIEW_ZOOMIN;
			case(MenuIndexConstants.VIEW_ZOOMOUT): return StringIndexConstants.VIEW_ZOOMOUT;
			case(MenuIndexConstants.VIEW_SETORIGINALSIZE): return StringIndexConstants.VIEW_SETORIGINALSIZE;
			case(MenuIndexConstants.VIEW_INTERPOLATIONTYPE): return StringIndexConstants.VIEW_INTERPOLATIONTYPE;
			case(MenuIndexConstants.VIEW_INTERPOLATIONTYPE_NEARESTNEIGHBOR): return StringIndexConstants.VIEW_INTERPOLATIONTYPE_NEARESTNEIGHBOR;
			case(MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BILINEAR): return StringIndexConstants.VIEW_INTERPOLATIONTYPE_BILINEAR;
			case(MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BICUBIC): return StringIndexConstants.VIEW_INTERPOLATIONTYPE_BICUBIC;
			case(MenuIndexConstants.HELP): return StringIndexConstants.HELP;
			case(MenuIndexConstants.HELP_ABOUT): return StringIndexConstants.ABOUT;
			case(MenuIndexConstants.HELP_SYSTEMINFORMATION): return StringIndexConstants.SYSTEM_INFORMATION;
			default: return -1;
		}
	}

	/**
	 * Sets the enabled status of one of the menu items to either
	 * <code>true</code> or <code>false</code>.
	 * @param index menu index of the component whose status is to be reset
	 * @param enabled boolean with the new value
	 */
	public abstract void setEnabled(int index, boolean enabled);

	/**
	 * Sets the text of one of the menu elements to a new value.
	 * This method is usually called when the language settings have changed and
	 * new words have to be assigned.
	 * @param index integer index of the menu element
	 * @param text new text value to be used for this element
	 */
	public abstract void setLabel(int index, String text);
}
