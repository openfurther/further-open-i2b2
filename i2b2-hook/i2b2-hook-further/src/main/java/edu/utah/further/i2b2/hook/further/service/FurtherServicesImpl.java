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
package edu.utah.further.i2b2.hook.further.service;

import static edu.utah.further.core.api.constant.Constants.INVALID_VALUE_LONG;
import static edu.utah.further.core.xml.xpath.XPathUtil.getNodeSubTreeAsString;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;

import edu.utah.further.core.api.collections.CollectionUtil;
import edu.utah.further.core.api.exception.WsException;
import edu.utah.further.core.api.xml.JaxbMarshallerProperty;
import edu.utah.further.core.api.xml.XmlService;
import edu.utah.further.core.ws.HttpResponseTo;
import edu.utah.further.core.ws.HttpUtil;
import edu.utah.further.core.xml.jaxb.XmlServiceImpl;
import edu.utah.further.core.xml.xpath.XPathNamespaceContext;
import edu.utah.further.fqe.ds.api.domain.QueryContext;
import edu.utah.further.fqe.ds.api.domain.ResultContext;
import edu.utah.further.fqe.ds.api.service.results.ResultType;
import edu.utah.further.fqe.ds.api.to.QueryContextIdentifier;
import edu.utah.further.fqe.ds.api.to.QueryContextIdentifierTo;
import edu.utah.further.fqe.ds.api.to.QueryContextState;
import edu.utah.further.fqe.ds.api.to.QueryContextStateTo;
import edu.utah.further.fqe.ds.api.to.QueryContextTo;
import edu.utah.further.fqe.ds.api.to.QueryContextToImpl;
import edu.utah.further.i2b2.hook.further.FurtherInterceptionFilter;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherDataSourceResult;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherJoinResult;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherQueryResultTo;

/**
 * FURTHeR i2b2 services implementation.
 * <p>
 * ----------------------------------------------------------------------------
 * -------<br>
 * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * ----------------------------------------------------------------------------
 * -------
 * 
 * @author N. Dustin Schultz {@code <dustin.schultz@utah.edu>}
 * @version Dec 16, 2010
 */
final class FurtherServicesImpl implements FurtherServices {
	// ========================= CONSTANTS =================================

	/**
	 * A logger that helps identify this class' printouts.
	 */
	private static final Logger log = getLogger(FurtherInterceptionFilter.class);

	/**
	 * XPath context to use in all XML manipulations.
	 */
	private static final NamespaceContext NS_CONTEXT = createNamespaceContext();

	// ========================= FIELDS ====================================

	/**
	 * The URL of the FURTHeR infrastructure of where the send the i2b2 request
	 * XML.
	 */
	private String queryRequestUrl;

	/**
	 * The URL of the FURTHeR infrastructure of where to request current state
	 * of a query.
	 */
	private String queryStateUrl;

	/**
	 * The URL of the FURTHeR infrastructure of where to get result information
	 * about the query.
	 */
	private String queryResultsUrl;

	/**
	 * The minimum number of results, otherwise masked
	 */
	private Long resultMaskBoundary;

	/**
	 * The maximum time a query can run before it times out. 10 minute default.
	 */
	private long maxQueryTime = 600L;

	// ========================= DEPENDENCIES ==============================

	/**
	 * Since we don't have spring here, use manual dependency injection.
	 */
	private final XmlService xmlService = new XmlServiceImpl();

	// ========================= CONSTRUCTORS ==============================

	/**
	 * @param queryRequestUrl
	 * @param queryStateUrl
	 */
	public FurtherServicesImpl() {
		super();
	}

	/**
	 * @param queryRequestUrl
	 * @param queryStateUrl
	 */
	public FurtherServicesImpl(final String queryRequestUrl,
			final String queryStateUrl) {
		super();
		this.queryRequestUrl = queryRequestUrl;
		this.queryStateUrl = queryStateUrl;
	}

	// ========================= IMPL: FurtherServices
	// ==============================

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherService#i2b2QueryRequest
	 * (java .lang.String)
	 */
	public QueryContextIdentifier i2b2QueryRequest(final String i2b2RequestXml,
			final long i2b2QueryId) {
		final String queryRequestUrlWithId = queryRequestUrl + "/"
				+ i2b2QueryId;
		if (log.isDebugEnabled()) {
			log.debug("Sending request " + i2b2RequestXml + " to FURTHeR URL "
					+ queryRequestUrlWithId);
		}
		final HttpMethod method = HttpUtil.createPostMethod(
				queryRequestUrlWithId, i2b2RequestXml);
		final HttpResponseTo furtherResponse = HttpUtil.getHttpResponse(
				queryRequestUrlWithId, method);
		if (log.isDebugEnabled()) {
			log.debug("FURTHeR web service response status code = "
					+ furtherResponse.getStatusCode());
		}

		return toQcIdentifier(furtherResponse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherService#getQueryState
	 * (edu. utah.further.fqe.ds.api.to.QueryContextIdentifier)
	 */
	public QueryContextState getQueryState(final QueryContextIdentifier id) {
		final String queryStateUrlWithId = queryStateUrl + "/" + id.getId();

		HttpResponseTo response;
		try {
			response = HttpUtil.getHttpGetResponseBody(queryStateUrlWithId,
					3000);
		} catch (final WsException e) {
			throw new RuntimeException("Error getting query state for id "
					+ id.getId(), e);
		}

		QueryContextState state;
		try {
			final String responseXml = response.getResponseBodyAsString();

			state = xmlService
					.unmarshal(responseXml, QueryContextStateTo.class);
		} catch (final Exception e) {
			throw new RuntimeException(
					"Error unmarshalling query state response", e);
		}

		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherServices#getQuery(edu
	 * .utah .further.fqe.ds.api.to.QueryContextIdentifier)
	 */
	public QueryContextTo getQuery(final QueryContextIdentifier id) {
		final String queryResultsUrlWithId = queryResultsUrl + "/" + id.getId();

		HttpResponseTo response;
		try {
			response = HttpUtil.getHttpGetResponseBody(queryResultsUrlWithId,
					3000);
		} catch (final WsException e) {
			throw new RuntimeException("Error getting query for id "
					+ id.getId(), e);
		}

		QueryContextTo queryContextTo;
		try {
			final String responseXml = response.getResponseBodyAsString();

			queryContextTo = xmlService.unmarshal(responseXml,
					QueryContextToImpl.class);
		} catch (final Exception e) {
			throw new RuntimeException(
					"Error unmarshalling query from response", e);
		}

		return queryContextTo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherService#getQueryRequestUrl
	 * ()
	 */
	public String getQueryRequestUrl() {
		return queryRequestUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherService#setQueryRequestUrl
	 * (java.lang.String)
	 */
	public void setQueryRequestUrl(final String queryRequestUrl) {
		this.queryRequestUrl = queryRequestUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherService#getQueryStateUrl
	 * ()
	 */
	public String getQueryStateUrl() {
		return queryStateUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherService#setQueryStateUrl
	 * (java .lang.String)
	 */
	public void setQueryStateUrl(final String queryStateUrl) {
		this.queryStateUrl = queryStateUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherServices#getQueryResultsUrl
	 * ()
	 */
	public String getQueryResultsUrl() {
		return queryResultsUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.utah.further.i2b2.hook.further.service.FurtherServices#setQueryResultsUrl
	 * (java.lang.String)
	 */
	public void setQueryResultsUrl(final String queryResultsUrl) {
		this.queryResultsUrl = queryResultsUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.utah.further.i2b2.hook.further.service.FurtherServices#
	 * getResultMaskBoundary ()
	 */
	public Long getResultMaskBoundary() {
		return resultMaskBoundary;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.utah.further.i2b2.hook.further.service.FurtherServices#
	 * setResultMaskBoundary (java.lang.Long)
	 */
	public void setResultMaskBoundary(final Long resultMaskBoundary) {
		this.resultMaskBoundary = resultMaskBoundary;
	}

	/**
	 * @param queryContextTo
	 * @return
	 * @see edu.utah.further.i2b2.hook.further.service.FurtherServices#buildQueryResultXml(edu.utah.further.fqe.ds.api.to.QueryContextTo)
	 */
	public String buildQueryResultXml(final QueryContextTo queryContextTo) {
		// Old i2b2 query result section
		// final StringBuilder s = new StringBuilder();
		// s.append("<set_size>" + queryContextTo.getNumRecords() +
		// "</set_size>").append(
		// Strings.NEW_LINE_STRING);

		// Our i2b2 FURTHeR query result new section
		final I2b2FurtherQueryResultTo i2b2FurtherQueryResultTo = toI2b2FurtherQueryResultTo(queryContextTo);
		return marshalI2b2FurtherQueryResult(i2b2FurtherQueryResultTo);
	}

	/**
	 * Return the maxQueryTime property.
	 * 
	 * @return the maxQueryTime
	 * @see edu.utah.further.i2b2.hook.further.service.FurtherServices#getMaxQueryTime()
	 */
	public long getMaxQueryTime() {
		return maxQueryTime;
	}

	/**
	 * Set a new value for the maxQueryTime property.
	 * 
	 * @param maxQueryTime
	 *            the maxQueryTime to set
	 * @see edu.utah.further.i2b2.hook.further.service.FurtherServices#setMaxQueryTime(long)
	 */
	public void setMaxQueryTime(final long maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
	}

	/**
	 * Adapt a query context to an i2b2 TO graph.
	 * 
	 * @param queryContext
	 *            FQC
	 * @return i2b2 TO graph ready to be marshaled and inserted into an I2b2
	 *         query response message
	 */
	@SuppressWarnings("boxing")
	public I2b2FurtherQueryResultTo toI2b2FurtherQueryResultTo(
			final QueryContextTo queryContext) {
		boolean isMasked = false;
		long maskedRecords = 0;
		if (log.isDebugEnabled()) {
			log.debug("Converting query ID " + queryContext.getId()
					+ " to an i2b2 TO");
		}
		final I2b2FurtherQueryResultTo queryResult = new I2b2FurtherQueryResultTo();

		// Copy DQC list to data source result list
		final List<I2b2FurtherDataSourceResult> dataSourceResults = queryResult
				.getDataSources();
		final int numDataSources = queryContext.getNumChildren();
		if (log.isDebugEnabled()) {
			log.debug("# data sources = " + numDataSources);
		}
		for (final QueryContext childQc : queryContext.getChildren()) {
			if (childQc.getNumRecords() > 0
					&& childQc.getNumRecords() <= resultMaskBoundary) {
				isMasked = true;
				maskedRecords += childQc.getNumRecords();

			}
			dataSourceResults
					.add(new I2b2FurtherDataSourceResult(
							childQc.getDataSourceId(),
							(childQc.getNumRecords() > 0
									&& childQc.getNumRecords() <= resultMaskBoundary ? I2b2FurtherQueryResultTo.COUNT_SCRUBBED
									: childQc.getNumRecords())));
			if (log.isDebugEnabled()) {
				log.debug("Added data source result " + childQc);
			}
		}

		// Copy result context list to join result list. Not needed if there's
		// only one
		// data source.
		if (numDataSources > 1) {
			final List<I2b2FurtherJoinResult> joinResults = queryResult
					.getJoins();
			final SortedMap<ResultType, ResultContext> resultViews = CollectionUtil
					.newSortedMap();
			resultViews.putAll(queryContext.getResultViews());
			for (final Map.Entry<ResultType, ResultContext> entry : resultViews
					.entrySet()) {
				final ResultContext resultContext = entry.getValue();
				joinResults
						.add(new I2b2FurtherJoinResult(
								WordUtils.capitalize(entry.getKey().name()
										.toLowerCase()),
								isMasked ? (entry.getKey().equals(
										ResultType.SUM) ? resultContext
										.getNumRecords() - maskedRecords
										: I2b2FurtherQueryResultTo.COUNT_SCRUBBED)
										: resultContext.getNumRecords()));
				if (log.isDebugEnabled()) {
					log.debug("Added join result " + resultContext);
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("I2b2 TO = " + queryResult);
		}
		return queryResult;
	}

	/**
	 * Marshal an I2b2 FURTHeR query result object graph to XML.
	 * 
	 * @param queryResult
	 *            I2b2 FURTHeR query result
	 * @return XML string
	 * @see edu.utah.further.i2b2.hook.further.service.FurtherServices#marshalI2b2FurtherQueryResult(I2b2FurtherQueryResultTo)
	 */
	public String marshalI2b2FurtherQueryResult(
			final I2b2FurtherQueryResultTo queryResult) {
		final Map<JaxbMarshallerProperty, Object> marshallerConfig = CollectionUtil
				.newMap();
		marshallerConfig.put(JaxbMarshallerProperty.FRAGMENT, Boolean.TRUE);
		String marshalled;
		try {
			marshalled = xmlService.marshal(queryResult, xmlService.options()
					.setFormat(true).addClass(I2b2FurtherQueryResultTo.class)
					.buildContext().setMarshallerConfig(marshallerConfig)
					.setRootNamespaceUris(CollectionUtil.<String> newSet()));
			// Remove trailing space that JAXB inserts instead of the top XML
			// declaration
			marshalled = marshalled.trim();
		} catch (final JAXBException e) {
			throw new RuntimeException(
					"Error unmarshalling query result transfer object", e);
		}
		marshalled = marshalled.replace(
				Integer.toString(I2b2FurtherQueryResultTo.COUNT_SCRUBBED), "*");
		marshalled = marshalled.replace(
				Integer.toString(ResultContext.ACCESS_DENIED), "Unauthorized");
		return marshalled;
	}

	/**
	 * @param bytes
	 * @return
	 * @see edu.utah.further.i2b2.hook.further.service.FurtherServices#extractI2b2QueryId(byte[])
	 */
	public long extractI2b2QueryId(final byte[] bytes) {
		if (bytes == null) {
			return INVALID_VALUE_LONG;
		}
		final String text = getNodeSubTreeAsString(new ByteArrayInputStream(
				bytes), "//query_instance_id/text()", NS_CONTEXT);
		return (text == null) ? INVALID_VALUE_LONG : Long
				.parseLong(text.trim());
	}

	/**
	 * @param furtherResponse
	 * @return
	 */
	private QueryContextIdentifierTo toQcIdentifier(
			final HttpResponseTo furtherResponse) {
		QueryContextIdentifierTo qcIdTo;
		try {
			qcIdTo = xmlService.unmarshal(
					furtherResponse.getResponseBodyAsString(),
					QueryContextIdentifierTo.class);
		} catch (final Exception e) {
			throw new RuntimeException(
					"Error unmarshalling query request response", e);
		}

		if (log.isDebugEnabled()) {
			log.debug("Returned QueryContext is " + qcIdTo.getId());
		}
		return qcIdTo;
	}

	/**
	 * @param inputStream
	 * @param xpathExpression
	 * @return
	 */
	private static NamespaceContext createNamespaceContext() {
		final XPathNamespaceContext nsContext = new XPathNamespaceContext();
		// nsContext.addPrefix("ns4",
		// RequestElementNames.REQUEST_XML_NAMESPACE);
		return nsContext;
	}
}
