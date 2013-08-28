/*******************************************************************************
 * Source File: BufferedRequestWrapper.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import edu.utah.further.core.api.lang.CoreUtil;

/**
 * See http://forums.sun.com/thread.jspa?threadID=238221.
 * <p>
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2010 FURTHeR Project, Health Sciences IT, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * -----------------------------------------------------------------------------------
 *
 * @author Oren E. Livne {@code <oren.livne@utah.edu>}ersion Aug 19, 2009
 */
public final class BufferedRequestWrapper extends HttpServletRequestWrapper
{
	// ========================= CONSTANTS =================================

	// ========================= FIELDS ====================================

	private ByteArrayInputStream bais;
	private BufferedServletInputStream bsis;
	private final byte[] buffer;

	// ========================= CONSTRUCTORS ==============================

	/**
	 * @param req
	 * @throws IOException
	 */
	public BufferedRequestWrapper(final HttpServletRequest req) throws IOException
	{
		super(req);

		// Read InputStream and store its content in a buffer.
		buffer = CoreUtil.readBytesFromStream(req.getInputStream());
	}

	// ========================= IMPL: ServletWrapper ======================

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.ServletRequestWrapper#getInputStream()
	 */
	@Override
	public ServletInputStream getInputStream()
	{
		try
		{
			// Generate a new InputStream by stored buffer
			bais = new ByteArrayInputStream(buffer);
			// Istantiate a subclass of ServletInputStream
			// (Only ServletInputStream or subclasses of it are accepted by the servlet
			// engine!)
			bsis = new BufferedServletInputStream(bais);
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
		return bsis;
	}

	// ========================= PRIVATE METHODS ===========================


}