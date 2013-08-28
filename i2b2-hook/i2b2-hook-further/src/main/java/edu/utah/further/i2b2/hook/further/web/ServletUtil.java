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

import static edu.utah.further.core.api.text.StringUtil.quote;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains basic Servlet API features.
 * <p>
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2013 FURTHeR Project, AVP Health Sciences IT Office, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * -----------------------------------------------------------------------------------
 *
 * @author Oren E. Livne {@code <oren.livne@utah.edu>}
 * @version Oct 13, 2008
 */
public final class ServletUtil
{
	// ========================= CONSTANTS =================================

	/**
	 * A logger that helps identify this class' printouts.
	 */
	private static final Log log = LogFactory.getLog(ServletUtil.class);

	// ========================= CONSTRUCTORS ==============================

	/**
	 * <p>
	 * Hide constructor in utility class.
	 * </p>
	 */
	private ServletUtil()
	{
		throw new IllegalAccessError("A utility class cannot be instantiated");
	}

	// ========================= METHODS ===================================

	/**
	 * Return the IP address of the client. Works when tomcat is the front server, or when
	 * Apache redirects to tomcat using a proxy pass.
	 *
	 * @param request
	 *            HTTP request
	 * @return client's IP address
	 */
	public static String getRemoteAddr(final HttpServletRequest request)
	{
		if (request.getHeader("x-forwarded-for") != null)
		{
			return request.getHeader("x-forwarded-for");
		}
		return request.getRemoteAddr();
	}

	// Depends on a core-http method, but we don't necessarily want to import the
	// httpclient transitive dependency to a module that depends on core-servlet.
	//
	// /**
	// * Creates query String from request body parameters.
	// *
	// * @param request
	// * The request that will supply parameters
	// * @return Query string corresponding to that request parameters
	// */
	// @SuppressWarnings("unchecked")
	// public static String getRequestParameters(final HttpServletRequest request)
	// {
	// final Map<String, ?> m = request.getParameterMap();
	// return createQueryStringFromMap(m, "&").toString();
	// }

	/**
	 * Extracts a base address from URI (that is, part of address before '?')
	 *
	 * @param uri
	 *            An address to extract base address from
	 * @return base address
	 */
	public static String getBaseFromUri(final String uri)
	{
		if ((uri == null) || (uri.trim().length() == 0))
		{
			return "";
		}

		final int qSignPos = uri.indexOf('?');
		if (qSignPos == -1)
		{
			return uri;
		}

		return uri.substring(0, qSignPos);
	}

	/**
	 * Convenience method to set a cookie. The cookie gets max age set to 30 days.
	 *
	 * @param response
	 *            response that will accept a cookie
	 * @param name
	 *            name of the cookie to store
	 * @param value
	 *            value of the cookie
	 * @param path
	 *            path of the cookie
	 */
	public static void setCookie(final HttpServletResponse response, final String name,
			final String value, final String path)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Setting cookie " + quote(name) + " on path " + quote(path));
		}

		final Cookie cookie = new Cookie(name, value);
		cookie.setSecure(false);
		cookie.setPath(path);
		cookie.setMaxAge(3600 * 24 * 30); // 30 days

		response.addCookie(cookie);
	}

	/**
	 * Convenience method to get a cookie by name
	 *
	 * @param request
	 *            the current request
	 * @param name
	 *            the name of the cookie to find
	 * @return the cookie (if found), null if not found
	 */
	public static Cookie getCookie(final HttpServletRequest request, final String name)
	{
		final Cookie[] cookies = request.getCookies();
		Cookie returnCookie = null;

		if (cookies == null)
		{
			return returnCookie;
		}

		for (int i = 0; i < cookies.length; i++)
		{
			final Cookie thisCookie = cookies[i];

			if (thisCookie.getName().equals(name))
			{
				// cookies with no value do me no good!
				if (!thisCookie.getValue().equals(""))
				{
					returnCookie = thisCookie;

					break;
				}
			}
		}

		return returnCookie;
	}

	/**
	 * @param request
	 */
	public static void printRequestHeaders(final HttpServletRequest request)
	{
		log.debug("Request headers:");
		final Enumeration<?> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements())
		{
			final String name = (String) headerNames.nextElement();
			log.debug(name + " = " + request.getHeader(name));
		}
	}

	/**
	 * @param request
	 */
	public static void printRequestParameters(final HttpServletRequest request)
	{
		log.debug("Request parameters:");
		final Enumeration<?> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements())
		{
			final String name = (String) parameterNames.nextElement();
			log.debug(name + " = " + request.getParameter(name));
		}
	}

	/**
	 * @param request
	 */
	public static void printRequestAttributes(final HttpServletRequest request)
	{
		log.debug("Request attributes:");
		final Enumeration<?> attributeNames = request.getAttributeNames();
		while (attributeNames.hasMoreElements())
		{
			final String name = (String) attributeNames.nextElement();
			log.debug(name + " = " + request.getAttribute(name));
		}
	}
}
