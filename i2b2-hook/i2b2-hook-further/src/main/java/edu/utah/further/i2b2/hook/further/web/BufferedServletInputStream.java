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

import java.io.ByteArrayInputStream;

import javax.servlet.ServletInputStream;

/**
 * Subclass of ServletInputStream needed by the servlet engine. All inputStream methods
 * are wrapped and are delegated to the ByteArrayInputStream (obtained as constructor
 * parameter).
 * <p>
 * See http://forums.sun.com/thread.jspa?threadID=238221
 * <p>
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * -----------------------------------------------------------------------------------
 *
 * @author Oren E. Livne {@code <oren.livne@utah.edu>}
 * @version Aug 10, 2010
 */
public final class BufferedServletInputStream extends ServletInputStream
{
	private ByteArrayInputStream bais;

	public BufferedServletInputStream(final ByteArrayInputStream bais)
	{
		this.bais = bais;
	}

	@Override
	public int available()
	{
		return bais.available();
	}

	@Override
	public int read()
	{
		return bais.read();
	}

	@Override
	public int read(final byte[] buf, final int off, final int len)
	{
		return bais.read(buf, off, len);
	}

}