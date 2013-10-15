/*
 * JiuAwtFrame
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.gui.awt;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Label;
import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import net.sourceforge.jiu.apps.EditorState;
import net.sourceforge.jiu.apps.ImageDescriptionCreator;
import net.sourceforge.jiu.apps.JiuInfo;
import net.sourceforge.jiu.apps.StringIndexConstants;
import net.sourceforge.jiu.apps.Strings;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.gui.awt.AwtMenuWrapper;
import net.sourceforge.jiu.gui.awt.ImageCanvas;
import net.sourceforge.jiu.gui.awt.dialogs.InfoDialog;
import net.sourceforge.jiu.ops.ProgressListener;

/**
 * The frame class for the AWT demo program {@link net.sourceforge.jiu.apps.jiuawt}.
 * @author Marco Schmidt
 * @since 0.8.0
 */
public class JiuAwtFrame extends Frame implements ActionListener, ComponentListener, JiuInfo, ProgressListener
{
	/**
	 * The name of this application, jiuawt, plus the version number taken
	 * from {@link JiuInfo}.
	 * Example: <code>jiuawt 0.8.0</code>.
	 * Will be displayed in the title bar of this frame.
	 */
	public static final String APP_NAME = "jiuawt " + JiuInfo.JIU_VERSION;
	static final long serialVersionUID = 2592450425245L;
	private EditorState editor;
	private AwtMenuWrapper menuWrapper;
	private AwtOperationProcessor processor;
	private Label statusBar;
	private ScrollPane scrollPane;
	private ImageCanvas canvas;

	/**
	 * Create an object of this class, using the argument editor
	 * state.
	 * String resources to initialize the menu etc. will be taken
	 * from the EditorState object's Strings variable
	 * @param editorState EditorState object used by this frame
	 */
	public JiuAwtFrame(EditorState editorState)
	{
		super(APP_NAME);
		processor = new AwtOperationProcessor(editorState, this);
		editor = editorState;
		editor.addProgressListener(this);
		addComponentListener(this);
		addWindowListener(new WindowAdapter()
		{
      		public void windowClosing(WindowEvent e)
      		{
        		processor.fileExit();
      		}
    	});
		// MENU
		menuWrapper = new AwtMenuWrapper(editor.getStrings(), this);
		setMenuBar(menuWrapper.getMenuBar());
		menuWrapper.updateEnabled(processor);
		// IMAGE CANVAS
		// STATUS BAR
		statusBar = new Label("");
		add(statusBar, BorderLayout.SOUTH);
		maximize();
		//pack();
		repaint();
		setVisible(true);
		if (editor.getStartupImageName() != null)
		{
			processor.fileOpen(null);
		}
	}

	/**
	 * Processes event objects that get created when menu items are
	 * picked.
	 * Determines the {@link net.sourceforge.jiu.apps.MenuIndexConstants} value for a given
	 * event object and calls the internal {@link AwtOperationProcessor}
	 * object's process method with the menu value.
	 * The operation will then be performed.
	 * @param e the ActionEvent object
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		int index = menuWrapper.findIndex(source);
		if (index != -1)
		{
			processor.process(index);
		}
	}

	public void componentHidden(ComponentEvent e)
	{
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentResized(ComponentEvent e)
	{
		if (scrollPane != null)
		{
			canvas.computeZoomToFitSize();
			scrollPane.doLayout();
		}
	}

	public void componentShown(ComponentEvent e)
	{
	}

	/**
	 * Maximize the frame on the desktop.
	 * There is no such function in the 1.1 AWT (was added in 1.4), so
	 * this class determines the screen size and sets the frame to be 
	 * a little smaller than that (to make up for task bars etc.).
	 * So this is just a heuristical approach.
	 */
	public void maximize()
	{
		/*
		The following line:
		setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
		does a nice maximization, but works only with Java 1.4+
		*/
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit == null)
		{
			return;
		}
		Dimension screenSize = toolkit.getScreenSize();
		if (screenSize == null)
		{
			return;
		}
		int w = screenSize.width;
		int h = screenSize.height;
		int x = 20;
		int y = 80;
		setLocation(x / 2, y / 2);
		setSize(w - x, h - y);
	}

	/**
	 * Displays the argument text in a message box with
	 * error in the title bar.
	 * @param text the error message to be displayed
	 */
	public void showError(String text)
	{
		Strings strings = editor.getStrings();
		showInfo(strings.get(StringIndexConstants.ERROR_MESSAGE), text);
	}

	/**
	 * Sets the current cursor to be {@link java.awt.Cursor#DEFAULT_CURSOR}.
	 */
	public void setDefaultCursor()
	{
		Cursor cursor = new Cursor(Cursor.DEFAULT_CURSOR);
		setCursor(cursor);
	}

	/**
	 * If an image is currently loaded,
	 */
	public void setOriginalSize()
	{
		if (canvas != null && !editor.isZoomOriginalSize())
		{
			editor.zoomSetOriginalSize();
			canvas.setZoomFactors(editor.getZoomFactorX(), editor.getZoomFactorY());
			updateTitle();
			menuWrapper.updateEnabled(processor);
		}
	}

	public void setProgress(int zeroBasedIndex, int totalItems)
	{
		if (totalItems < 1)
		{
			throw new IllegalArgumentException("Total number of items (second parameter) must be larger than zero.");
		}
		if (zeroBasedIndex < 0)
		{
			throw new IllegalArgumentException("Zero-based index must be at least zero.");
		}
		if (zeroBasedIndex >= totalItems)
		{
			throw new IllegalArgumentException("Zero-based index must be smaller than total " +
				"number of items; zeroBasedIndex=" + zeroBasedIndex + ", totalItems=" +
				totalItems);
		}
		setProgress((float)(zeroBasedIndex + 1) / (float)totalItems);
	}

	/**
	 * Set a new progress status.
	 * @param progress float from 0.0f to 1.0f, indicating the progress between 0 and 100 percent 
	 */
	public void setProgress(float progress)
	{
		if (progress >= 0.0f && progress <= 1.0f)
		{
			setStatusBar(" " + Math.round(progress * 100.0f) + "%");
		}
	}

	public void setStatusBar(String text)
	{
		statusBar.setText(text);
	}

	public void setWaitCursor()
	{
		Cursor cursor = new Cursor(Cursor.WAIT_CURSOR);
		setCursor(cursor);
	}

	/**
	 * Shows a modal dialog with given title bar and message text.
	 * @param title will be displayed in the dialog's title bar
	 * @param text will be displayed in the dialog's center part
	 */
	public void showInfo(String title, String text)
	{
		InfoDialog d = new InfoDialog(this, title, text);
		d.setVisible(true);
	}

	/**
	 * If there is an image loaded, forces a canvas redraw by 
	 * calling repaint.
	 */
	public void updateCanvas()
	{
		if (canvas != null)
		{
			canvas.setInterpolation(editor.getInterpolation());
			//canvas.revalidate();
			canvas.repaint();
		}
	}

	/**
	 * Removes the current canvas from the frame (if there
	 * is an image loaded) and creates a new canvas for the
	 * current image.
	 */
	public void updateImage()
	{
		PixelImage image = editor.getImage();
		if (scrollPane != null)
		{
			remove(scrollPane);
		}
		if (image != null)
		{
			//editor.zoomSetOriginalSize();
			Image awtImage = ImageCreator.convertToAwtImage(image, RGBA.DEFAULT_ALPHA);
			scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
			canvas = new ImageCanvas(scrollPane);
			canvas.setInterpolation(editor.getInterpolation());
			canvas.setZoomToFit(editor.getZoomToFit());
			canvas.setImage(awtImage);
			canvas.setZoomFactors(editor.getZoomFactorX(), editor.getZoomFactorY());
			//canvas.computeZoomToFitSize();
			scrollPane.add(canvas);
			add(scrollPane);
		}
		updateStatusBar();
		updateTitle();
		validate();
		menuWrapper.updateEnabled(processor);
	}

	/**
	 * Creates a description string for the current image and sets the
	 * status bar to that text.
	 */
	public void updateStatusBar()
	{
		PixelImage image = editor.getImage();
		String statusBarText;
		if (image == null)
		{
			statusBarText = "";
		}
		else
		{
			statusBarText = ImageDescriptionCreator.getDescription(image, editor.getLocale(), editor.getStrings());
		}
		setStatusBar(statusBarText);
	}

	/**
	 * Sets the frame's title bar to the application name, plus the file name of
	 * the currently loaded image file, plus the current zoom factor, plus an
	 * optional asterisk in case the image was modified but not yet saved.
	 */
	public void updateTitle()
	{
		StringBuffer sb = new StringBuffer(APP_NAME);
		String fileName = editor.getFileName();
		if (fileName != null && fileName.length() > 0)
		{
			sb.append(" [");
			sb.append(fileName);
			if (editor.getModified())
			{
				sb.append('*');
			}
			sb.append(']');
		}
		if (editor.getImage() != null)
		{
			double zoom = editor.getZoomFactorX();
			int percent = (int)(zoom * 100.0);
			sb.append(' ');
			sb.append(Integer.toString(percent));
			sb.append('%');
		}
		setTitle(sb.toString());
	}

	/**
	 * If an image is currently displayed, zoom in one level.
	 */
	public void zoomIn()
	{
		if (canvas != null && !editor.isMaximumZoom())
		{
			editor.zoomIn();
			canvas.setZoomFactors(editor.getZoomFactorX(), editor.getZoomFactorY());
			updateTitle();
			menuWrapper.updateEnabled(processor);
		}
	}

	/**
	 * If an image is currently displayed, zoom out one level.
	 */
	public void zoomOut()
	{
		if (canvas != null && !editor.isMinimumZoom())
		{
			editor.zoomOut();
			canvas.setZoomFactors(editor.getZoomFactorX(), editor.getZoomFactorY());
			updateTitle();
			menuWrapper.updateEnabled(processor);
		}
	}
}
