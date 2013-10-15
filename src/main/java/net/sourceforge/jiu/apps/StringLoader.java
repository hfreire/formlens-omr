/*
 * StringLoader
 * 
 * Copyright (c) 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * This class loads a {@link Strings} resource from a text file.
 * The text file must contain one  
 * @author Marco Schmidt
 * @since 0.12.0
 */
public class StringLoader
{
	private BufferedReader in;
	private Integer langCode;
	/**
	 * The directory of language resource files, default:
	 * <code>/resources/lang/</code>.
	 */
	public static String resourceDirectory = "/resources/lang/";

	public StringLoader()
	{
		this(Strings.determineSuitableIsoCode());
	}

	public StringLoader(String iso639Code)
	{
		InputStream input = getClass().getResourceAsStream(resourceDirectory + iso639Code + ".txt");
		if (input == null && !"en".equals(iso639Code))
		{
			input = getClass().getResourceAsStream(resourceDirectory + "en.txt");
		}
		if (input == null)
		{
			return;
		}
		in = new BufferedReader(new InputStreamReader(input));
		langCode = Strings.findLanguageCode(iso639Code);
	}

	public Strings load() throws IOException
	{
		if (in == null)
		{
			return null;
		}
		Vector list = new Vector();
		String line;
		while ((line = in.readLine()) != null)
		{
			list.addElement(line);
		}
		if (list.size() < 1)
		{
			return null;
		}
		String[] data = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			data[i] = (String)list.elementAt(i);
		}
		in.close();
		return new Strings(langCode, data);
	}
}
