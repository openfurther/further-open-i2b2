/*******************************************************************************
 * Source File: FurtherInterceptionFilter.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further;

import static edu.utah.further.core.api.xml.XmlUtil.fullTag;
import static edu.utah.further.core.api.xml.XmlUtil.openTag;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;

import edu.utah.further.core.api.constant.Strings;
import edu.utah.further.core.api.lang.CoreUtil;
import edu.utah.further.core.api.text.StringUtil;
import edu.utah.further.fqe.ds.api.domain.QueryState;
import edu.utah.further.fqe.ds.api.to.QueryContextIdentifier;
import edu.utah.further.fqe.ds.api.to.QueryContextIdentifierTo;
import edu.utah.further.fqe.ds.api.to.QueryContextTo;
import edu.utah.further.i2b2.hook.further.service.FurtherServices;
import edu.utah.further.i2b2.hook.further.service.FurtherServicesFactory;
import edu.utah.further.i2b2.hook.further.web.BufferedRequestWrapper;
import edu.utah.further.i2b2.hook.further.web.CapturedResponseWrapper;
import edu.utah.further.i2b2.hook.further.web.ServletUtil;

/**
 * A web filter that intercepts requests to the i2b2 query web service and sends them also
 * to the FURTHeR FQE web services for processing. This class can be extended for other
 * inter-operability requirements between i2b2 and FURTHeR or any other system.
 * <p>
 * Note: We add some extra XML elements in the FURTHeR-tweaked i2b2 web client to the i2b2
 * XML request received by this filter, to control the FURTHeR hook processing chain. It
 * seems that these extra elements does not interfere with any subsequent i2b2 processing,
 * so they are left there. In case there's a problem. simply remove that part of the
 * request string before forwarding it to the i2b2 filter chain.
 * <p>
 * Add this Servlet to the i2b2 Axis2 webapp (preferably packaged in a jar generated from
 * this maven module and placed in their webapp's WEB-INF/lib directory) and modify the
 * webapp's web.xml to active the Servlet. See the etc directory for the updated web.xml
 * to replace with the one that ships with i2b2 1.3.
 * <p>
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2010 FURTHeR Project, AVP Health Sciences IT Office, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * -----------------------------------------------------------------------------------
 * 
 * @author Oren E. Livne {@code <oren.livne@utah.edu>}
 * @version Dec 27, 2008
 */
public final class FurtherInterceptionFilter implements Filter
{
	// ========================= CONSTANTS =================================

	/**
	 * @serial Serializable version identifier.
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	/**
	 * A logger that helps identify this class' printouts.
	 */
	private static final Logger log = getLogger(FurtherInterceptionFilter.class);

	/**
	 * A boolean flag that controls whether to send the query to the FURTHeR FQE for
	 * processing.
	 */
	private static final String FQE_PROCESS = "fqe-process";

	/**
	 * Unique key attribute for the {@link QueryContextIdentifier}
	 */
	private static final String QUERY_ID = "edu.utah.further.fqe.id";

	/**
	 * The tag name of a query result block
	 */
	private static final String QUERY_RESULT_INSTANCE = "query_result_instance";

	/**
	 * Matches FQE processing flag in an XML string.
	 */
	private static final String FQE_PROCESSING_FLAG_STRING = fullTag(FQE_PROCESS, "true")
			.toString();

	/**
	 * Matches FQE processing flag in an XML string.
	 */
	private static final String QUERY_RESULT_STRING = openTag(QUERY_RESULT_INSTANCE)
			.toString();

	// ========================= FIELDS ====================================

	// ========================= DEPENDENCIES ==============================

	/**
	 * FURTHeR services.
	 */
	private final FurtherServices furtherServices = FurtherServicesFactory
			.newFurtherServicesInstance();

	// ========================= CONSTRUCTORS ==============================

	/**
	 * No-arg c-tor.
	 */
	public FurtherInterceptionFilter()
	{
		super();
		if (log.isDebugEnabled())
		{
			log.debug("Instantiating");
		}
	}

	// ========================= IMPLEMENTATION: Filter ====================

	/**
	 * @param filterConfig
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(final FilterConfig filterConfig) throws ServletException
	{
		if (log.isDebugEnabled())
		{
			log.debug("Initializing");
		}
		
		final Properties properties = new Properties();
		try {
			properties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("further.properties"));
		} catch (IOException e) {
			throw new ServletException(
					"Unable to initialize FURTHeR i2b2 hook", e);
		}

		final String queryRequestUrl = properties.getProperty("query.request.url");
		final String queryStateUrl = properties.getProperty("query.state.url");
		final String queryResultsUrl = properties.getProperty("query.result.url");
		final String resultMaskBoundary = properties.getProperty("result.mask.boundary");
		final String maxQueryTime = properties.getProperty("query.max.time");

		if (log.isDebugEnabled())
		{
			log.debug(properties.toString());
		}

		furtherServices.setQueryRequestUrl(queryRequestUrl);
		furtherServices.setQueryStateUrl(queryStateUrl);
		furtherServices.setQueryResultsUrl(queryResultsUrl);
		furtherServices.setResultMaskBoundary(Long.valueOf(resultMaskBoundary));
		furtherServices.setMaxQueryTime(Long.valueOf(maxQueryTime).longValue());
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy()
	{
		if (log.isDebugEnabled())
		{
			log.debug("Shutting down");
		}
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(final ServletRequest req, final ServletResponse resp,
			final FilterChain chain) throws IOException, ServletException
	{
		// Cast to friendler versions
		if (!HttpServletRequest.class.isAssignableFrom(req.getClass()))
		{
			throw new ServletException(
					"ServletRequest must be an instance of HttpServletRequest");
		}
		if (!HttpServletResponse.class.isAssignableFrom(resp.getClass()))
		{
			throw new ServletException(
					"ServletResponse must be an instance of HttpServletResponse");
		}
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) resp;

		// Decorate the i2b2 processing chain. Decorate the HTTP request so that input
		// stream can be read twice - once by this filter's methods, and one by the
		// i2b2 processing chain call chain.doFilter().
		final HttpServletRequest bufferedRequest = new BufferedRequestWrapper(request);
		final CapturedResponseWrapper capturedResponse = new CapturedResponseWrapper(
				response);
		beforeRequest(bufferedRequest);
		chain.doFilter(bufferedRequest, capturedResponse);
		afterRequest(bufferedRequest, response, capturedResponse);
	}

	// ========================= HOOKS =====================================

	/**
	 * Pre-processes the request before the i2b2 processing chain is triggered. A hook for
	 * sub-classes. <i>Must catch all {@link Throwable}s</i>.
	 * 
	 * @param request
	 *            intercepted HTTP request from i2b2
	 * @param response
	 *            HTTP response
	 */
	protected void beforeRequest(final HttpServletRequest request)
	{
		// if (log.isDebugEnabled())
		// {
		// log.debug("Before i2b2 processing");
		// }
	}

	/**
	 * Post-processes the request after the i2b2 processing chain is complete. A hook for
	 * sub-classes. <i>Must catch all {@link Throwable}s</i>.
	 * 
	 * @param request
	 *            intercepted HTTP request from i2b2
	 * @param response
	 *            HTTP response
	 */
	protected void afterRequest(final HttpServletRequest request,
			final HttpServletResponse originalResponse,
			final CapturedResponseWrapper capturedResponse)
	{
		if (log.isDebugEnabled())
		{
			log.debug("After i2b2 processing");
		}

		// Extract i2b2 query ID from the response and send it to FURTHeR
		final byte[] originalResponseBytes = capturedResponse.toByteArray();
		final long i2b2QueryId = furtherServices
				.extractI2b2QueryId(originalResponseBytes);
		spawnFurtherRequest(request, i2b2QueryId);

		// Determine if the response to this i2b2 request needs to be modified here, and
		// modify if so. Depends on the response from polling the FURTHeR WS result
		final byte[] requestBytes = getRequestBytes(request);
		byte[] responseBytes = originalResponseBytes;
		final String responseString = new String(originalResponseBytes);
		if (isProcessRequest(requestBytes) && isResultResponse(originalResponseBytes))
		{
			responseBytes = modifyI2b2Response(request, responseString);
		}
		try
		{
			originalResponse.getOutputStream().write(responseBytes);
		}
		catch (final IOException writeEx)
		{
			log.error("Unable to write response", writeEx);
		}
	}

	// ========================= PROTECTED METHODS ===========================

	/**
	 * Decide whether to fork an FQE processing chain or not.
	 * 
	 * @param requestBytes
	 *            request XML as byte array
	 * @return <code>true</code> if and only if this request requires FURTHeR processing
	 *         (pun intended...)
	 */
	protected boolean isProcessRequest(final byte[] requestBytes)
	{
		return (requestBytes == null) ? false : new String(requestBytes).replaceAll(
				Strings.SPACE_STRING, Strings.EMPTY_STRING).contains(
				FQE_PROCESSING_FLAG_STRING);
	}

	/**
	 * Determines if the response is a result response
	 * 
	 * @param response
	 *            the captured response as a String
	 * @return true if it is, false otherwise
	 */
	protected boolean isResultResponse(final byte[] responseBytes)
	{
		return (responseBytes == null) ? false : new String(responseBytes).replaceAll(
				Strings.SPACE_STRING, Strings.EMPTY_STRING).contains(QUERY_RESULT_STRING);
	}

	// ========================= PRIVATE METHODS ===========================

	/**
	 * Send the i2b2 query request message to the FURTHeR FQE. Must be run after the i2b2
	 * processing chain, because it depends on the i2b2 query ID generated by the i2b2
	 * server.
	 * 
	 * @param request
	 * @param i2b2QueryId
	 *            i2b2 query ID, obtained from the i2b2 response
	 */
	private void spawnFurtherRequest(final HttpServletRequest request,
			final long i2b2QueryId)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Read i2b2QueryId from request: " + i2b2QueryId);
		}
		try
		{
			// Need to read create from request.getInputStream() multiple times
			// in this method ==> save a copy in a buffer first
			// inputStream is already at the end of the file.
			final InputStream inputStream = request.getInputStream();
			final byte[] buffer = CoreUtil.readBytesFromStream(inputStream);
			inputStream.close();

			// Decide whether to fork or not
			if (StringUtil.isValidLong(i2b2QueryId) && isProcessRequest(buffer))
			{
				// Read the FURTHeR section of the i2b2 request body
				final String requestXml = new String(buffer);
				// Request contains an FQE processing flag, send to FURTHeR
				if (log.isDebugEnabled())
				{
					ServletUtil.printRequestHeaders(request);
					ServletUtil.printRequestParameters(request);
					ServletUtil.printRequestAttributes(request);
				}

				// TODO: read query instance id from i2b2 response and pass to the
				// following call
				final QueryContextIdentifier id = furtherServices.i2b2QueryRequest(
						requestXml, i2b2QueryId);

				// Make available to response through the request, ensures thread safety
				// instead of using instance var
				request.setAttribute(QUERY_ID, id);

				QueryState state = furtherServices.getQueryState(id).getState();

				final StopWatch stopWatch = new StopWatch();

				final int interval = 10;
				int i = 0;
				stopWatch.start();
				// Poll state every sec
				final long maxQueryTimeMillis = furtherServices.getMaxQueryTime() * 1000;
				while (state != QueryState.COMPLETED && state != QueryState.STOPPED
						&& state != QueryState.FAILED && state != QueryState.INVALID
						&& state != null && stopWatch.getTime() < maxQueryTimeMillis)
				{
					Thread.yield();
					state = furtherServices.getQueryState(id).getState();

					if (log.isDebugEnabled() && ((i % interval) == 0))
					{
						log.debug("QueryState for query " + id.getId() + ": " + state);
					}

					i++;
				}
				stopWatch.stop();
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.info("Ignoring unrecognized/irrelvant requestXml");
				}
			}
		}
		catch (final Throwable throwable)
		{
			if (log.isDebugEnabled())
			{
				log.error("Caught " + throwable + ", ignoring", throwable);
			}
		}
	}

	/**
	 * @param request
	 * @return
	 */
	private byte[] getRequestBytes(final HttpServletRequest request)
	{
		byte[] requestBytes = new byte[0];
		try
		{
			requestBytes = CoreUtil.readBytesFromStream(request.getInputStream());
		}
		catch (final IOException readEx)
		{
			log.error("Error reading request", readEx);
		}
		return requestBytes;
	}

	/**
	 * @param request
	 * @param originalResponseBytes
	 * @return
	 */
	private byte[] modifyI2b2Response(final HttpServletRequest request,
			final String i2b2Response)
	{
		String originalResponse = i2b2Response;
		if (log.isDebugEnabled())
		{
			log.debug("Response: " + originalResponse);
		}

		final QueryContextIdentifierTo id = (QueryContextIdentifierTo) request
				.getAttribute(QUERY_ID);
		if (id == null)
		{
			throw new IllegalStateException(
					"Did not obtain an acknowledgment from the FURTHeR FQE; check "
							+ "that the FQE WS init-param is set to the correct value in web.xml and in the code");
		}
		// Clean up
		request.removeAttribute(QUERY_ID);

		final QueryContextTo queryContextTo = furtherServices.getQuery(id);

		originalResponse = originalResponse.replaceAll("<set_size>[0-9]+</set_size>",
				furtherServices.buildQueryResultXml(queryContextTo));

		return originalResponse.getBytes();
	}
}
