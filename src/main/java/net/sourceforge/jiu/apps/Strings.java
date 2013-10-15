/*
 * Strings
 *
 * Copyright (c) 2001, 2002, 2003, 2004 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.apps;

import java.util.Hashtable;
import java.util.Locale;

/**
 * String resource for the various apps.
 * Each index value from {@link StringIndexConstants} has a corresponding String value
 * for all supported natural languages.
 * @author Marco Schmidt
 */
public class Strings implements StringIndexConstants
{
	/**
	 * Constant int value for the natural language <em>English</em>.
	 * */
	public static final Integer LANG_ENGLISH = new Integer(0);

	/**
	 * Constant int value for the natural language <em>German</em>.
	 */
	public static final Integer LANG_GERMAN = new Integer(1);

	/**
	 * Constant int value for the natural language <em>Spanish</em>.
	 */
	public static final Integer LANG_SPANISH = new Integer(2);

	/**
	 * Constant int value for the natural language <em>French</em>.
	 */
	public static final Integer LANG_FRENCH = new Integer(3);

	/**
	 * Constant of the default language, {@link #LANG_ENGLISH}.
	 */
	public static final Integer DEFAULT_LANGUAGE = LANG_ENGLISH;

	/**
	 * ISO 639 two-letter country codes for the supported languages, lower case.
	 */
	private static final String[] ISO_639_LANGUAGE_CODES =
	{
		"en",
		"de",
		"es",
		"fr",
	};

	private static final Integer[] LANGUAGE_CONSTANTS =
	{
		LANG_ENGLISH, 
		LANG_GERMAN, 
		LANG_SPANISH,
		LANG_FRENCH,
	};

	/**
	 * The ISO 639 code for the default language {@link #DEFAULT_LANGUAGE}.
	 */
	public static final String DEFAULT_LANGUAGE_ISO_639_CODE = ISO_639_LANGUAGE_CODES[DEFAULT_LANGUAGE.intValue()];

	/**
	 * A hashtable that maps from ISO 639 country codes to Integer
	 * objects with the corresponding LANG_xyz constant for that language.
	 */
	private static Hashtable isoToConstant;

	static
	{
		isoToConstant = new Hashtable(ISO_639_LANGUAGE_CODES.length);
		for (int i = 0; i < ISO_639_LANGUAGE_CODES.length; i++)
		{
			isoToConstant.put(ISO_639_LANGUAGE_CODES[i], LANGUAGE_CONSTANTS[i]);
		}
	}

	private String[] data;
	private Integer language;

	/**
	 * Create a new String object for the given language and fill it
	 * with the String array. 
	 */
	public Strings(Integer languageConstant, String[] stringValues)
	{
		set(languageConstant, stringValues);
	}

	/**
	 * Determines an ISO 639 code of a language suitable for the environment
	 * in which the JVM is currently running.
	 * First calls {@link #determineIsoCodeFromDefaultLocale()}.
	 * If that yields null, the ISO code for {@link #DEFAULT_LANGUAGE} is returned.
	 * So different from {@link #determineIsoCodeFromDefaultLocale()}
	 * this method always returns a non-null value.
	 * @return String with ISO 639 code of a language that fits the JVM environment,
	 *  or the default language as fallback solution 
	 */
	public static String determineSuitableIsoCode()
	{
		String code = determineIsoCodeFromDefaultLocale();
		if (code != null && findLanguageCode(code) != null)
		{
			return code;
		}
		else
		{
			return ISO_639_LANGUAGE_CODES[DEFAULT_LANGUAGE.intValue()];
		}
	}

	public static String determineIsoCodeFromDefaultLocale()
	{
		Locale locale = Locale.getDefault();
		if (locale == null)
		{
			return null;
		}
		return locale.getLanguage();
	}

	public static Integer findLanguageCode(String iso639LanguageCode)
	{
		if (iso639LanguageCode == null)
		{
			return null;
		}
		String code = iso639LanguageCode.toLowerCase();
		return (Integer)isoToConstant.get(code);
	}

	/**
	 * Gets the String denoted by the argument index.
	 * This index must be one of the int constants defined in {@link StringIndexConstants}.
	 * @return String with given index in the current language
	 * @throws IllegalArgumentException is not a valid index from {@link StringIndexConstants}
	 */
	public String get(int index)
	{
		try
		{
			return data[index];
		}
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			throw new IllegalArgumentException("Not a valid String index: " + index);
		}
	}

	/**
	 * Returns the language of this object as one of the LANG_xyz 
	 * constants of this class.
	 */
	public Integer getLanguage()
	{
		return language;
	}

	public static String getFileName(int languageCode)
	{
		if (languageCode >= 0 && languageCode < ISO_639_LANGUAGE_CODES.length)
		{
			return ISO_639_LANGUAGE_CODES[languageCode] + ".txt";
		}
		else
		{
			return null;
		}
	}

	public void set(Integer languageConstant, String[] values)
	{
		if (languageConstant == null || 
		    languageConstant.intValue() < 0 || 
		    languageConstant.intValue() >= ISO_639_LANGUAGE_CODES.length)
		{
			throw new IllegalArgumentException("Not a valid language constant: " + languageConstant);
		}
		if (values == null || values.length < 1)
		{
			throw new IllegalArgumentException("The values array argument must be non-null and have at least one element.");
		}
		language = languageConstant;
		data = values;
	}
}
