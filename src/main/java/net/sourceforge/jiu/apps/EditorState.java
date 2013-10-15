/*
 * EditorState
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import java.io.IOException;
import java.util.Locale;
import java.util.Vector;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.ops.Operation;
import net.sourceforge.jiu.ops.ProgressListener;

/**
 * Represents the state of the editor, including image(s), modified flag,
 * current file name and directories and more.
 * This class must not know GUI-specific information like Frame or JFrame objects.
 * These GUI classes (more precisely, the JIU classes that extend them) will have to 
 * know EditorState and update according to the information they retrieve from an 
 * EditorState object associated with them.
 * EditorState is a pure data container.
 * @author Marco Schmidt
 */
public class EditorState implements MenuIndexConstants
{
	/**
	 * The default number of undo steps possible.
	 */
	public static final int DEFAULT_MAX_UNDO_IMAGES = 2;

	/**
	 * The default number of redo steps possible.
	 */
	public static final int DEFAULT_MAX_REDO_IMAGES = DEFAULT_MAX_UNDO_IMAGES;

	/**
	 * All allowed zoom levels, as percentage values in ascending order.
	 */
	public static final int[] ZOOM_LEVELS = {5, 7, 10, 15, 20, 30, 50, 70, 100, 150, 200, 300, 500, 700, 1000, 2000, 3000, 5000};

	/**
	 * The index into the {@link #ZOOM_LEVELS} array that holds the original size zoom level (100 percent).
	 * So, ZOOM_LEVELS[ORIGINAL_SIZE_ZOOM_INDEX] must be equal to 100.
	 */
	public static final int ORIGINAL_SIZE_ZOOM_INDEX = 8;

	/**
	 * Integer constant for <em>nearest neighbor interpolation</em>.
	 * A fast but ugly method.
	 */
	public static final int INTERPOLATION_NEAREST_NEIGHBOR = 0;

	/**
	 * Integer constant for <em>bilinear neighbor interpolation</em>.
	 * A slow but nice method.
	 */
	public static final int INTERPOLATION_BILINEAR = 1;

	/**
	 * Integer constant for <em>bicubic interpolation</em>.
	 * A very slow method, but with the nicest output of the three supported interpolation types.
	 */
	public static final int INTERPOLATION_BICUBIC = 2;

	/**
	 * The default interpolation type, one of the three INTERPOLATION_xyz constants.
	 */
	public static final int DEFAULT_INTERPOLATION = INTERPOLATION_NEAREST_NEIGHBOR;
	private String currentDirectory;
	private String fileName;
	private PixelImage currentImage;
	private int interpolation;
	private Locale locale;
	private int maxRedoImages;
	private int maxUndoImages;
	private boolean modified;
	private Vector progressListeners;
	private Vector redoImages;
	private Vector redoModified;
	private String startupImageName;
	private Strings strings;
	private Vector undoImages;
	private Vector undoModified;
	private int zoomIndex = ORIGINAL_SIZE_ZOOM_INDEX;
	private double zoomFactorX;
	private double zoomFactorY;
	private boolean zoomToFit;

	/**
	 * Create new EditorState object and initialize its private fields
	 * to default values.
	 */
	public EditorState()
	{
		locale = Locale.getDefault();
		setStrings(null);
		progressListeners = new Vector();
		maxRedoImages = DEFAULT_MAX_REDO_IMAGES;
		maxUndoImages = DEFAULT_MAX_UNDO_IMAGES;
		redoImages = new Vector(maxRedoImages);
		redoModified = new Vector(maxRedoImages);
		undoImages = new Vector(maxUndoImages);
		undoModified = new Vector(maxUndoImages);
		zoomFactorX = 1.0;
		zoomFactorY = 1.0;
		zoomToFit = false;
	}

	private void addImageToRedo(PixelImage image, boolean modifiedState)
	{
		if (maxRedoImages < 1)
		{
			return;
		}
		if (redoImages.size() == maxRedoImages)
		{
			redoImages.setElementAt(null, 0);
			redoImages.removeElementAt(0);
			redoModified.removeElementAt(0);
		}
		redoImages.addElement(image);
		redoModified.addElement(new Boolean(modifiedState));
	}

	private void addImageToUndo(PixelImage image, boolean modifiedState)
	{
		if (maxUndoImages < 1)
		{
			return;
		}
		if (undoImages.size() == maxUndoImages)
		{
			undoImages.setElementAt(null, 0);
			undoImages.removeElementAt(0);
			undoModified.removeElementAt(0);
		}
		undoImages.addElement(image);
		undoModified.addElement(new Boolean(modifiedState));
	}

	/**
	 * Adds the argument progress listener to the internal list of progress
	 * listeners to be notified by progress updates.
	 * @param pl object implementing ProgressListener to be added
	 */
	public void addProgressListener(ProgressListener pl)
	{
		progressListeners.addElement(pl);
	}

	/**
	 * Returns if a redo operation is possible right now.
	 */
	public boolean canRedo()
	{
		return (redoImages.size() > 0);
	}

	/**
	 * Returns if an undo operation is possible right now.
	 */
	public boolean canUndo()
	{
		return (undoImages.size() > 0);
	}

	public void clearRedo()
	{
		int index = 0;
		while (index < redoImages.size())
		{
			redoImages.setElementAt(null, index++);
		}
		redoImages.setSize(0);
		redoModified.setSize(0);
	}

	public void clearUndo()
	{
		int index = 0;
		while (index < undoImages.size())
		{
			undoImages.setElementAt(null, index++);
		}
		undoImages.setSize(0);
		undoModified.setSize(0);
	}

	public void ensureStringsAvailable()
	{
		if (getStrings() == null)
		{
			setStrings(Strings.DEFAULT_LANGUAGE_ISO_639_CODE);
		}
	}

	/** 
	 * Returns the current directory.
	 * This directory will be used when file dialogs are opened.
	 */
	public String getCurrentDirectory()
	{
		return currentDirectory;
	}

	/**
	 * Returns the name of the file from which the current image was loaded.
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Returns the image object currently loaded.
	 */
	public PixelImage getImage()
	{
		return currentImage;
	}

	/**
	 * Returns the current interpolation type, one of the INTERPOLATION_xyz constants.
	 */
	public int getInterpolation()
	{
		return interpolation;
	}

	/**
	 * Returns the Locale object currently used.
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * Returns the current modified state (true if image was modified and not saved 
	 * after modification, false otherwise).
	 */
	public boolean getModified()
	{
		return modified;
	}

	/**
	 * Returns the internal list of progress listeners.
	 */
	public Vector getProgressListeners()
	{
		return progressListeners;
	}

	public String getStartupImageName()
	{
		return startupImageName;
	}

	/**
	 * Returns the Strings object currently in use.
	 */
	public Strings getStrings()
	{
		return strings;
	}

	/** 
	 * Returns the current zoom factor in horizontal direction.
	 * The value 1.0 means that the image is displayed at its 
	 * original size.
	 * Anything smaller means that the image is scaled down,
	 * anything larger means that the image is scaled up.
	 * The value must not be smaller than or equal to 0.0.
	 * @return zoom factor in horizontal direction
	 * @see #getZoomFactorY
	 */
	public double getZoomFactorX()
	{
		return zoomFactorX;
	}

	/** 
	 * Returns the current zoom factor in vertical direction.
	 * The value 1.0 means that the image is displayed at its 
	 * original size.
	 * Anything smaller means that the image is scaled down,
	 * anything larger means that the image is scaled up.
	 * The value must not be smaller than or equal to 0.0.
	 * @return zoom factor in vertical direction
	 * @see #getZoomFactorX
	 */
	public double getZoomFactorY()
	{
		return zoomFactorY;
	}

	/**
	 * Returns if image display is currently set to &quot;zoom to fit&quot;
	 * Zoom to fit means that the image is always zoomed to fit exactly into the window.
	 */
	public boolean getZoomToFit()
	{
		return zoomToFit;
	}

	/**
	 * Returns if this state encapsulates an image object.
	 */
	public boolean hasImage()
	{
		return (currentImage != null);
	}

	/**
	 * Adds all ProgressListener objects from the internal list of listeners to
	 * the argument operation.
	 */
	public void installProgressListeners(Operation op)
	{
		if (op == null)
		{
			return;
		}
		// cannot use Iterator because it's 1.2+
		int index = 0;
		while (index < progressListeners.size())
		{
			ProgressListener pl = (ProgressListener)progressListeners.elementAt(index++);
			op.addProgressListener(pl);
		}
	}

	/**
	 * Returns if the image is displayed at maximum zoom level.
	 */
	public boolean isMaximumZoom()
	{
		return zoomIndex == ZOOM_LEVELS.length - 1;
	}

	/**
	 * Returns if the image is displayed at minimum zoom level.
	 */
	public boolean isMinimumZoom()
	{
		return zoomIndex == 0;
	}

	/**
	 * Returns if the current zoom level is set to original size
	 * (each image pixel is displayed as one pixel).
	 */
	public boolean isZoomOriginalSize()
	{
		return zoomIndex == ORIGINAL_SIZE_ZOOM_INDEX;
	}

	/**
	 * Perform a redo operation, restore the state before the last undo operation.
	 * Before that is done, save the current state for an undo.
	 */
	public void redo()
	{
		if (redoImages.size() < 1)
		{
			return;
		}
		addImageToUndo(currentImage, modified);
		int redoIndex = redoImages.size() - 1;
		currentImage = (PixelImage)redoImages.elementAt(redoIndex);
		redoImages.setElementAt(null, redoIndex);
		redoImages.setSize(redoIndex);
		modified = ((Boolean)redoModified.elementAt(redoIndex)).booleanValue();
		redoModified.setSize(redoIndex);
	}

	public void resetZoomFactors()
	{
		setZoomFactors(1.0, 1.0);
	}

	/**
	 * Sets a new current directory.
	 * @param newCurrentDirectory the directory to be used as current directory from now on
	 */
	public void setCurrentDirectory(String newCurrentDirectory)
	{
		currentDirectory = newCurrentDirectory;
	}

	/**
	 * Sets a new file name.
	 * This is used mostly after a new image was loaded from a file or
	 * if the current image is closed (then a null value would be given to this method).
	 * @param newFileName new name of the current file
	 */
	public void setFileName(String newFileName)
	{
		fileName = newFileName;
	}

	/**
	 * Sets image and modified state to argument values.
	 * @param image new current image
	 * @param newModifiedState new state of modified flag
	 */
	public void setImage(PixelImage image, boolean newModifiedState)
	{
		if (hasImage())
		{
			addImageToUndo(currentImage, modified);
		}
		currentImage = image;
		modified = newModifiedState;
		clearRedo();
	}

	public void setStartupImageName(String name)
	{
		startupImageName = name;
	}

	/**
	 * Sets a new interpolation type to be used for display.
	 * @param newInterpolation an int for the interpolation type, must be one of the INTERPOLATION_xyz constants
	 */
	public void setInterpolation(int newInterpolation)
	{
		if (newInterpolation == INTERPOLATION_NEAREST_NEIGHBOR ||
		    newInterpolation == INTERPOLATION_BILINEAR ||
		    newInterpolation == INTERPOLATION_BICUBIC)
		{
			interpolation = newInterpolation;
		}
	}

	/**
	 * Defines a new Locale to be used.
	 * @param newLocale Locale object used from now on
	 * @see #setStrings
	 */
	public void setLocale(Locale newLocale)
	{
		locale = newLocale;
	}

	/*public void setModified(boolean modifiedState)
	{
		modified = modifiedState;
	}*/

	/**
	 * Set new Strings resource.
	 * @param iso639Code language of the new Strings resource
	 */
	public void setStrings(String iso639Code)
	{
		Strings newStrings = null;
		try
		{
			StringLoader loader;
			if (iso639Code == null)
			{
				loader = new StringLoader();
			}
			else
			{
				loader = new StringLoader(iso639Code);
			}
			newStrings = loader.load();
		}
		catch (IOException ioe)
		{
		}
		if (newStrings != null)
		{
			strings = newStrings;
		}
	}

	/**
	 * Sets the zoom factors to the argument values.
	 */
	public void setZoomFactors(double zoomX, double zoomY)
	{
		zoomFactorX = zoomX;
		zoomFactorY = zoomY;
	}

	/**
	 * Perform an undo step - the previous state will be set, the
	 * current state will be saved for a redo operation
	 * @see #redo
	 */
	public void undo()
	{
		if (undoImages.size() < 1)
		{
			return;
		}
		addImageToRedo(currentImage, modified);
		int undoIndex = undoImages.size() - 1;
		currentImage = (PixelImage)undoImages.elementAt(undoIndex);
		undoImages.setElementAt(null, undoIndex);
		undoImages.setSize(undoIndex);
		modified = ((Boolean)undoModified.elementAt(undoIndex)).booleanValue();
		undoModified.setSize(undoIndex);
	}

	/**
	 * Increase the zoom level by one.
	 * @see #zoomOut
	 * @see #zoomSetOriginalSize
	 */
	public void zoomIn()
	{
		if (zoomIndex + 1 == ZOOM_LEVELS.length)
		{
			return;
		}
		zoomIndex++;
		zoomFactorX = 1.0 * ZOOM_LEVELS[zoomIndex] / 100;
		zoomFactorY = zoomFactorX;
	}

	/**
	 * Decrease the zoom level by one.
	 * @see #zoomIn
	 * @see #zoomSetOriginalSize
	 */
	public void zoomOut()
	{
		if (zoomIndex == 0)
		{
			return;
		}
		zoomIndex--;
		zoomFactorX = 1.0 * ZOOM_LEVELS[zoomIndex] / 100;
		zoomFactorY = zoomFactorX;
	}

	/**
	 * Set the zoom level to 100 percent (1:1).
	 * Each image pixel will be displayed as one pixel
	 * @see #zoomIn
	 * @see #zoomOut
	 */
	public void zoomSetOriginalSize()
	{
		zoomIndex = ORIGINAL_SIZE_ZOOM_INDEX;
		zoomFactorX = 1.0;
		zoomFactorY = 1.0;
	}
}
