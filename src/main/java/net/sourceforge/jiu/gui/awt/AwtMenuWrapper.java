/*
 * AwtMenuWrapper
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import net.sourceforge.jiu.apps.MenuIndexConstants;
import net.sourceforge.jiu.apps.MenuWrapper;
import net.sourceforge.jiu.apps.OperationProcessor;
import net.sourceforge.jiu.apps.StringIndexConstants;
import net.sourceforge.jiu.apps.Strings;

/**
 * A wrapper around an AWT MenuBar object.
 * @author Marco Schmidt
 * @since 0.8.0
 */
public class AwtMenuWrapper extends MenuWrapper
{
	private ActionListener listener;
	private MenuItem[] items;
	private MenuBar menuBar;

	/**
	 * Internally creates a MenuBar object and provides methods to update that
	 * menu bar.
	 * @param strings String resource used to initialize menu items
	 * @param actionListener a listener which will be registered with all menu items
	 */
	public AwtMenuWrapper(Strings strings, ActionListener actionListener)
	{
		items = new MenuItem[MenuIndexConstants.NUM_CONSTANTS];
		listener = actionListener;
		init(strings);
	}

	private Menu createMenu(Strings strings, int stringIndex)
	{
		String labelText = strings.get(stringIndex);
		Menu result = new Menu(labelText);
		return result;
	}

	private MenuShortcut createMenuShortcut(int menuIndex)
	{
		switch(menuIndex)
		{
			case(MenuIndexConstants.FILE_OPEN): return new MenuShortcut(KeyEvent.VK_O);
			case(MenuIndexConstants.FILE_EXIT): return new MenuShortcut(KeyEvent.VK_Q);
			case(MenuIndexConstants.EDIT_UNDO): return new MenuShortcut(KeyEvent.VK_Z);
			case(MenuIndexConstants.EDIT_REDO): return new MenuShortcut(KeyEvent.VK_Y);
			case(MenuIndexConstants.VIEW_ZOOMIN): return new MenuShortcut(KeyEvent.VK_ADD);
			case(MenuIndexConstants.VIEW_ZOOMOUT): return new MenuShortcut(KeyEvent.VK_SUBTRACT);
			case(MenuIndexConstants.VIEW_SETORIGINALSIZE): return new MenuShortcut(KeyEvent.VK_SEPARATER);
			default: return null;
		}
	}

	/**
	 * Attempts to find one of the menu items in the internal list.
	 * Returns its index or -1 if it is not one of the items.
	 */
	public int findIndex(Object o)
	{
		if (o != null && items != null)
		{
			for (int i = 0; i < items.length; i++)
			{
				if (o == items[i])
				{
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the encapsulated MenuBar object.
	 */
	public MenuBar getMenuBar()
	{
		return menuBar;
	}

	/**
	 * Initializes an object of type MenuBar.
	 */
	private void init(Strings strings)
	{
		// by default, create all items as MenuItem objects
		for (int i = 0; i < items.length; i++)
		{
			int stringIndex = getStringIndex(i);
			if (stringIndex == -1)
			{
				continue;
			}
			String labelText = strings.get(stringIndex);
			items[i] = new MenuItem(labelText);
		}
		menuBar = new MenuBar();
		// FILE - SAVE AS
		Menu fileSaveAsMenu = createMenu(strings, StringIndexConstants.SAVEAS);
		items[MenuIndexConstants.FILE_SAVEAS] = fileSaveAsMenu;
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_GIF]);
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_PALM]);
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_PBM]);
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_PGM]);
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_PNG]);
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_PPM]);
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_SUNRASTER]);
		fileSaveAsMenu.add(items[MenuIndexConstants.FILE_SAVEAS_WINDOWSBMP]);
		// FILE
		Menu fileMenu = createMenu(strings, StringIndexConstants.FILE);
		items[MenuIndexConstants.FILE] = fileMenu;
		fileMenu.add(items[MenuIndexConstants.FILE_OPEN]);
		fileMenu.add(fileSaveAsMenu);
		fileMenu.add(items[MenuIndexConstants.FILE_CLOSE]);
		fileMenu.addSeparator();
		fileMenu.add(items[MenuIndexConstants.FILE_IMAGE_1]);
		fileMenu.addSeparator();
		fileMenu.add(items[MenuIndexConstants.FILE_EXIT]);
		menuBar.add(fileMenu);
		// EDIT
		Menu editMenu = createMenu(strings, StringIndexConstants.EDIT);
		items[MenuIndexConstants.EDIT] = editMenu;
		editMenu.add(items[MenuIndexConstants.EDIT_UNDO]);
		editMenu.add(items[MenuIndexConstants.EDIT_REDO]);
		menuBar.add(editMenu);
		// COLOR - ADJUST
		Menu colorAdjustMenu = createMenu(strings, StringIndexConstants.ADJUST);
		items[MenuIndexConstants.COLOR_ADJUST] = colorAdjustMenu;
		colorAdjustMenu.add(items[MenuIndexConstants.COLOR_ADJUST_BRIGHTNESS]);
		colorAdjustMenu.add(items[MenuIndexConstants.COLOR_ADJUST_CONTRAST]);
		colorAdjustMenu.add(items[MenuIndexConstants.COLOR_ADJUST_GAMMA]);
		colorAdjustMenu.add(items[MenuIndexConstants.COLOR_ADJUST_HUESATURATIONVALUE]);
		// COLOR - HISTOGRAM
		Menu colorHistogramMenu = createMenu(strings, StringIndexConstants.HISTOGRAM);
		items[MenuIndexConstants.COLOR_HISTOGRAM] = colorHistogramMenu;
		colorHistogramMenu.add(items[MenuIndexConstants.COLOR_HISTOGRAM_COUNTCOLORSUSED]);
		colorHistogramMenu.add(items[MenuIndexConstants.COLOR_HISTOGRAM_EQUALIZE]);
		colorHistogramMenu.add(items[MenuIndexConstants.COLOR_HISTOGRAM_NORMALIZE]);
		colorHistogramMenu.add(items[MenuIndexConstants.COLOR_HISTOGRAM_TEXTUREPROPERTIES]);
		colorHistogramMenu.add(items[MenuIndexConstants.COLOR_HISTOGRAM_SAVEHISTOGRAMAS]);
		colorHistogramMenu.add(items[MenuIndexConstants.COLOR_HISTOGRAM_SAVECOOCCURRENCEMATRIXAS]);
		colorHistogramMenu.add(items[MenuIndexConstants.COLOR_HISTOGRAM_SAVECOOCCURRENCEFREQUENCYMATRIXAS]);
		// COLOR - PALETTE
		Menu colorPaletteMenu = createMenu(strings, StringIndexConstants.PALETTE_MENU_ITEM);
		items[MenuIndexConstants.COLOR_PALETTE] = colorPaletteMenu;
		colorPaletteMenu.add(items[MenuIndexConstants.COLOR_PALETTE_SAVEAS]);
		// COLOR - PROMOTE
		Menu colorPromoteMenu = createMenu(strings, StringIndexConstants.PROMOTE);
		items[MenuIndexConstants.COLOR_PROMOTE] = colorPromoteMenu;
		colorPromoteMenu.add(items[MenuIndexConstants.COLOR_PROMOTE_PROMOTETOPALETTED]);
		colorPromoteMenu.add(items[MenuIndexConstants.COLOR_PROMOTE_PROMOTETOGRAY8]);
		colorPromoteMenu.add(items[MenuIndexConstants.COLOR_PROMOTE_PROMOTETOGRAY16]);
		colorPromoteMenu.add(items[MenuIndexConstants.COLOR_PROMOTE_PROMOTETORGB24]);
		colorPromoteMenu.add(items[MenuIndexConstants.COLOR_PROMOTE_PROMOTETORGB48]);
		// COLOR - REDUCE
		Menu colorReduceMenu = createMenu(strings, StringIndexConstants.REDUCE);
		items[MenuIndexConstants.COLOR_REDUCE] = colorReduceMenu;
		colorReduceMenu.add(items[MenuIndexConstants.COLOR_REDUCE_REDUCETOBILEVELTHRESHOLD]);
		colorReduceMenu.add(items[MenuIndexConstants.COLOR_REDUCE_REDUCENUMBEROFSHADESOFGRAY]);
		colorReduceMenu.add(items[MenuIndexConstants.COLOR_REDUCE_CONVERTTOGRAYSCALE]);
		colorReduceMenu.add(items[MenuIndexConstants.COLOR_REDUCE_MEDIANCUT]);
		colorReduceMenu.add(items[MenuIndexConstants.COLOR_REDUCE_OCTREE]);
		colorReduceMenu.add(items[MenuIndexConstants.COLOR_REDUCE_UNIFORMPALETTE]);
		colorReduceMenu.add(items[MenuIndexConstants.COLOR_REDUCE_MAPTOARBITRARYPALETTE]);
		// COLOR
		Menu colorMenu = createMenu(strings, StringIndexConstants.COLOR);
		items[MenuIndexConstants.COLOR] = colorMenu;
		colorMenu.add(colorAdjustMenu);
		colorMenu.add(colorHistogramMenu);
		colorMenu.add(colorPaletteMenu);
		colorMenu.add(colorPromoteMenu);
		colorMenu.add(colorReduceMenu);
		colorMenu.add(items[MenuIndexConstants.COLOR_INVERT]);
		colorMenu.add(items[MenuIndexConstants.COLOR_CONVERTTOMINIMUMCOLORTYPE]);
		menuBar.add(colorMenu);
		// TRANSFORMATIONS
		Menu transformationsMenu = createMenu(strings, StringIndexConstants.TRANSFORMATIONS);
		items[MenuIndexConstants.TRANSFORMATIONS] = transformationsMenu;
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_MIRROR]);
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_FLIP]);
		transformationsMenu.addSeparator();
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_ROTATELEFT90]);
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_ROTATERIGHT90]);
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_ROTATE180]);
		transformationsMenu.addSeparator();
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_CROP]);
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_SCALE]);
		transformationsMenu.add(items[MenuIndexConstants.TRANSFORMATIONS_SHEAR]);
		menuBar.add(transformationsMenu);
		// FILTERS
		Menu filtersMenu = createMenu(strings, StringIndexConstants.FILTERS);
		items[MenuIndexConstants.FILTERS] = filtersMenu;
		menuBar.add(filtersMenu);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_BLUR]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_SHARPEN]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_EDGEDETECTION]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_EMBOSS]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_PSYCHEDELICDISTILLATION]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_LITHOGRAPH]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_HORIZONTALSOBEL]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_VERTICALSOBEL]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_HORIZONTALPREWITT]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_VERTICALPREWITT]);
		filtersMenu.addSeparator();
		filtersMenu.add(items[MenuIndexConstants.FILTERS_MINIMUM]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_MAXIMUM]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_MEDIAN]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_MEAN]);
		filtersMenu.add(items[MenuIndexConstants.FILTERS_OIL]);
		// VIEW
		Menu viewMenu = createMenu(strings, StringIndexConstants.VIEW);
		items[MenuIndexConstants.VIEW] = viewMenu;
		menuBar.add(viewMenu);
		viewMenu.add(items[MenuIndexConstants.VIEW_ZOOMIN]);
		viewMenu.add(items[MenuIndexConstants.VIEW_ZOOMOUT]);
		viewMenu.add(items[MenuIndexConstants.VIEW_SETORIGINALSIZE]);
		// VIEW - INTERPOLATION TYPE
		Menu viewInterpolationMenu = createMenu(strings, StringIndexConstants.VIEW_INTERPOLATIONTYPE);
		items[MenuIndexConstants.VIEW_INTERPOLATIONTYPE] = viewInterpolationMenu;
		//viewMenu.add(viewInterpolationMenu);

		/*CheckboxGroup checkboxGroup = new CheckboxGroup();
		int stringIndex = getStringIndex(MenuIndexConstants.VIEW_INTERPOLATIONTYPE_NEARESTNEIGHBOR);
		items[MenuIndexConstants.VIEW_INTERPOLATIONTYPE_NEARESTNEIGHBOR] = new CheckboxMenuItem(strings.get(stringIndex), true);
		stringIndex = getStringIndex(MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BILINEAR);
		items[MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BILINEAR] = new CheckboxMenuItem(strings.get(stringIndex), false);
		stringIndex = getStringIndex(MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BICUBIC);
		items[MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BICUBIC] = new CheckboxMenuItem(strings.get(stringIndex), false);*/

		viewInterpolationMenu.add(items[MenuIndexConstants.VIEW_INTERPOLATIONTYPE_NEARESTNEIGHBOR]);
		viewInterpolationMenu.add(items[MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BILINEAR]);
		viewInterpolationMenu.add(items[MenuIndexConstants.VIEW_INTERPOLATIONTYPE_BICUBIC]);
		// HELP
		Menu helpMenu = createMenu(strings, StringIndexConstants.HELP);
		items[MenuIndexConstants.HELP] = helpMenu;
		menuBar.add(helpMenu);
		helpMenu.add(items[MenuIndexConstants.HELP_ABOUT]);
		helpMenu.add(items[MenuIndexConstants.HELP_SYSTEMINFORMATION]);
		// add the listener to all items
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] != null)
			{
				MenuShortcut shortcut = createMenuShortcut(i);
				if (shortcut != null)
				{
					items[i].setShortcut(shortcut);
				}
				items[i].addActionListener(listener);
			}
		}
	}

	/**
	 * Changes the enabled status of one of the MenuItem objects,
	 * given by its index.
	 */
	public void setEnabled(int index, boolean enabled)
	{
		if (index >= 0 && index < items.length && items[index] != null)
		{
			items[index].setEnabled(enabled);
		}
	}

	/**
	 * Changes the label text of one of the MenuItem objects,
	 * given by its index.
	 */
	public void setLabel(int index, String text)
	{
		if (index >= 0 && index < items.length && items[index] != null)
		{
			items[index].setLabel(text);
		}
	}

	/**
	 * Changes the enabled status of all MenuItem objects
	 * using the argument OperationProcessor object (more
	 * precisely, its isAvailable(int) method).
	 */
	public void updateEnabled(OperationProcessor op)
	{
		for (int i = 0; i < items.length; i++)
		{
			setEnabled(i, op.isAvailable(i));
		}
	}

	/**
	 * Sets the label text of all MenuItem objects to
	 * new values using the argument Strings information.
	 */
	public void updateLabels(Strings strings)
	{
		for (int i = 0; i < items.length; i++)
		{
			int stringIndex = getStringIndex(i);
			String text = strings.get(stringIndex);
			setLabel(i, text);
		}
	}
}
