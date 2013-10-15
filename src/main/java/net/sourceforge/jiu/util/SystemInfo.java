/*
 * SystemInfo
 * 
 * Copyright (c) 2000, 2001, 2002, 2003 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.util;

import net.sourceforge.jiu.apps.StringIndexConstants;
import net.sourceforge.jiu.apps.Strings;

/**
 * Class to retrieve system information in a human-readable form.
 * @author Marco Schmidt
 */
public class SystemInfo implements StringIndexConstants
{
	private SystemInfo()
	{
	}

	public static String getMemoryInfo(Strings strings)
	{
		Runtime runtime = Runtime.getRuntime();
		long freeMemory = runtime.freeMemory();
		long totalMemory = runtime.totalMemory();
		long usedMemory = totalMemory - freeMemory;
		return
			strings.get(StringIndexConstants.FREE_MEMORY) + "=" + freeMemory + "\n" +
			strings.get(StringIndexConstants.USED_MEMORY) + "=" + usedMemory + "\n" +
			strings.get(StringIndexConstants.TOTAL_MEMORY) + "=" + totalMemory + "\n";
	}

	/**
	 * Returns a multiple-line text with information on the Java Virtual Machine,
	 * the path settings and the operating system used, regarding the current
	 * language by using a {@link Strings} resource.
	 * @return system information as String
	 * @see java.lang.System#getProperty(String)
	 */
	public static String getSystemInfo(Strings strings)
	{
		final String[] PROPERTIES =
		{"java.version",
		 "java.vendor",
		 "java.vendor.url",
		 "java.home",
		 "java.vm.specification.version",
		 "java.vm.specification.vendor",
		 "java.vm.specification.name",
		 "java.vm.version",
		 "java.vm.vendor",
		 "java.vm.name",
		 "java.specification.version",
		 "java.specification.vendor",
		 "java.specification.name",
		 "java.class.version",
		 "java.class.path",
		 "os.name",
		 "os.arch",
		 "os.version",
		 "sun.cpu.endian",
		 "sun.cpu.isalist",
		};
		final int[] STRING_INDEX_VALUES =
		{
			PROPERTY_JAVA_VERSION,
			PROPERTY_JAVA_VENDOR,
			PROPERTY_JAVA_VENDOR_URL,
			PROPERTY_JAVA_HOME,
			PROPERTY_JAVA_VM_SPECIFICATION_VERSION,
			PROPERTY_JAVA_VM_SPECIFICATION_VENDOR,
			PROPERTY_JAVA_VM_SPECIFICATION_NAME,
			PROPERTY_JAVA_VM_VERSION,
			PROPERTY_JAVA_VM_VENDOR,
			PROPERTY_JAVA_VM_NAME,
			PROPERTY_JAVA_SPECIFICATION_VERSION,
			PROPERTY_JAVA_SPECIFICATION_VENDOR,
			PROPERTY_JAVA_SPECIFICATION_NAME,
			PROPERTY_JAVA_CLASS_VERSION,
			PROPERTY_JAVA_CLASS_PATH,
			PROPERTY_OS_NAME,
			PROPERTY_OS_ARCH,
			PROPERTY_OS_VERSION,
			CPU_ENDIANNESS,
			CPU_ISALIST
		};
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < PROPERTIES.length; i++)
		{
			try
			{
				String result = System.getProperty(PROPERTIES[i]);
				if (result != null)
				{
					if (sb.length() > 0)
					{
						sb.append("\n");
					}
					sb.append(strings.get(STRING_INDEX_VALUES[i]));
					sb.append('=');
					sb.append(result);
				}
			}
			catch (Exception e)
			{
			}
		}
		return sb.toString();
	}
}
