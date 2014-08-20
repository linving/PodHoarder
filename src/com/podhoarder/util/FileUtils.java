package com.podhoarder.util;

public class FileUtils
{
	/**
	 * Makes sure that a file name is within the OS rules for naming files. (length, special chars etc.)
	 * @param fileName	The file name you want sanitized.
	 * @return A sanitized version of fileName.
	 */
	public static String sanitizeFileName(String fileName)
	{
		String retString = fileName;
		retString = retString.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
		if (retString.length() > 30) retString = retString.substring(0, 25);
		return retString;
	}
}
