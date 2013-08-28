/**
 * Copyright (C) [2013] [The FURTHeR Project]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.utah.further.i2b2.hook.further.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A response wrapper which is backed by a {@link ByteArrayOutputStream}. Useful for
 * manipulating the response.
 * <p>
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * -----------------------------------------------------------------------------------
 * 
 * @author N. Dustin Schultz {@code <dustin.schultz@utah.edu>}
 * @version Dec 21, 2010
 */
public final class CapturedResponseWrapper extends HttpServletResponseWrapper
{
	// ========================= CONSTANTS =================================

	/**
	 * A logger that helps identify this class' printouts.
	 */
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(CapturedResponseWrapper.class);

	// ========================= FIELDS ====================================

	/**
	 * The byte array stream for which things will be written and read from.
	 */
	private final ByteArrayOutputStream baos;

	// ========================= CONSTRUCTORS ==============================

	/**
	 * @param response
	 */
	public CapturedResponseWrapper(final HttpServletResponse response)
	{
		super(response);
		baos = new ByteArrayOutputStream();
	}

	// ===================== IMPL: ServletResponseWrapper ===================

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getWriter()
	 */
	@Override
	public PrintWriter getWriter()
	{
		return new PrintWriter(baos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletResponseWrapper#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		return new ServletOutputStream()
		{

			@Override
			public void write(final int b) throws IOException
			{
				baos.write(b);
			}
		};
	}

	// ===================== IMPL: Object ===================

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return baos.toString();
	}

	// ===================== IMPL: CapturedResponseWrapper ===================

	/**
	 * Return the contents of this response as a byte array. This empties the contents and
	 * subsequent calls will fail.
	 * 
	 * @return
	 */
	public byte[] toByteArray()
	{
		return baos.toByteArray();
	}

}
