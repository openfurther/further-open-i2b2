/*******************************************************************************
 * Source File: CrcPropertiesWatcher.java
 ******************************************************************************/
package edu.harvard.i2b2.crc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Properties file watcher of the entire CRC cell. Register all file listeners here.
 * <p>
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2010 FURTHeR Project, Health Sciences IT, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * -----------------------------------------------------------------------------------
 * 
 * @author Oren E. Livne {@code <oren.livne@utah.edu>}
 * @version Feb 12, 2010
 */
public final class CrcUtil
{
	// ========================= CONSTANTS =================================

	// /**
	// * Logging support.
	// */
	// private static final Log log = LogFactory.getLog(CrcPropertiesWatcher.class);

	// ========================= CONSTRUCTORS ==============================

	/**
	 * Initialize watcher.
	 */
	private CrcUtil()
	{
		super();
	}

	// ========================= METHODS ===================================

	/**
	 * Save a string to a file.
	 * 
	 * @param contents
	 * @param file
	 * @throws IOException
	 */
	public static void saveStringToTextFile(final String contents, final File file)
	{
		try
		{
			final PrintWriter out = new PrintWriter(new FileWriter(file));
			out.print(contents);
			out.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param file
	 *            the name of the file to open. Not sure if it can accept URLs or just
	 *            filenames. Path handling could be better, and buffer sizes are hardcoded
	 */
	public static String readFileAsString(final File file)
	{
		try
		{
			final StringBuffer fileData = new StringBuffer(1000);
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1)
			{
				final String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return fileData.toString();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			return "";
		}
	}
}
