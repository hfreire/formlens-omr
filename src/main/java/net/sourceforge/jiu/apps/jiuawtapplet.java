/*
 * jiuawtapplet
 * 
 * Copyright (c) 2001, 2002, 2003, 2004, 2005, 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import java.applet.Applet;
import net.sourceforge.jiu.gui.awt.JiuAwtFrame;

/**
 * Applet version of jiuawt.
 * Not really useful because no images can be loaded.
 * @author Marco Schmidt
 */
public class jiuawtapplet extends Applet implements JiuInfo
{
	private static final long serialVersionUID = 93423883004L;

	public String getAppletInfo()
	{
		return "jiuawtapplet" +
			"; demo applet for the Java Imaging Utilities" +
			"; feedback address: " + JIU_FEEDBACK_ADDRESS +
			"; homepage: " + JIU_HOMEPAGE;
	}

	public void init()
	{
		EditorState state = new EditorState();
		state.setStrings(null);
		new JiuAwtFrame(state);
	}

	public void start()
	{
	}

	public void stop()
	{
		System.exit(0);
	}
}
