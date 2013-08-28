/*******************************************************************************
 * Source File: FurtherServices.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further.service;

import javax.xml.bind.JAXBException;

import edu.utah.further.fqe.ds.api.domain.QueryContext;
import edu.utah.further.fqe.ds.api.domain.QueryState;
import edu.utah.further.fqe.ds.api.to.QueryContextIdentifier;
import edu.utah.further.fqe.ds.api.to.QueryContextState;
import edu.utah.further.fqe.ds.api.to.QueryContextTo;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherQueryResultTo;

/**
 * Service methods to further infrastructure.
 * <p>
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2010 FURTHeR Project, Health Sciences IT, University of Utah<br>
 * Contact: {@code <further@utah.edu>}<br>
 * Biomedical Informatics, 26 South 2000 East<br>
 * Room 5775 HSEB, Salt Lake City, UT 84112<br>
 * Day Phone: 1-801-581-4080<br>
 * -----------------------------------------------------------------------------------
 *
 * @author N. Dustin Schultz {@code <dustin.schultz@utah.edu>}
 * @version Dec 16, 2010
 */
public interface FurtherServices
{
	// ========================= METHODS ===================================

	/**
	 * Request a new federated query using the i2b2 query xml.
	 *
	 * @param i2b2RequestXml
	 *            raw i2b2 request message
	 * @param i2b2QueryId
	 *            i2b2 query ID
	 * @return FQE QC identifier
	 */
	QueryContextIdentifier i2b2QueryRequest(String i2b2RequestXml, long i2b2QueryId);

	/**
	 * Get the {@link QueryState} of a requested query. This method may be called
	 * repeatedly to poll for query state.
	 *
	 * @param id
	 *            the federated query contexts identifier
	 * @return the current state of the query.
	 */
	QueryContextState getQueryState(QueryContextIdentifier id);

	/**
	 * Get the full {@link QueryContext}
	 *
	 * @param id
	 *            the federated query contexts identifier
	 * @return the query context in it's current state
	 */
	QueryContextTo getQuery(QueryContextIdentifier id);

	/**
	 * Return the queryRequestUrl property.
	 *
	 * @return the queryRequestUrl
	 */
	String getQueryRequestUrl();

	/**
	 * Set a new value for the queryRequestUrl property.
	 *
	 * @param queryRequestUrl
	 *            the queryRequestUrl to set
	 */
	void setQueryRequestUrl(String queryRequestUrl);

	/**
	 * Return the queryStateUrl property.
	 *
	 * @return the queryStateUrl
	 */
	String getQueryStateUrl();

	/**
	 * Set a new value for the queryStateUrl property.
	 *
	 * @param queryStateUrl
	 *            the queryStateUrl to set
	 */
	void setQueryStateUrl(String queryStateUrl);

	/**
	 * Return the queryResultsUrl property.
	 *
	 * @return the queryResultsUrl
	 */
	String getQueryResultsUrl();

	/**
	 * Set a new value for the queryResultsUrl property.
	 *
	 * @param queryResultsUrl
	 *            the queryResultsUrl to set
	 */
	void setQueryResultsUrl(String queryResultsUrl);

	/**
	 * Return the resultMaskBoundary property.
	 *
	 * @return the resultMaskBoundary
	 */
	Long getResultMaskBoundary();

	/**
	 * Set a new value for the resultMaskBoundary property.
	 *
	 * @param resultMaskBoundary
	 *            the resultMaskBoundary to set
	 */
	void setResultMaskBoundary(Long resultMaskBoundary);

	/**
	 * Create an XML snippet to be placed in the i2b2 XML response that contains
	 * information on a result of a FURTHeR query. This includes a break down into
	 * individual data source person counts as well as join counts.
	 *
	 * @param queryContextTo
	 *            federated query contexts obtained from the FQE
	 * @return XML representation of the query results
	 */
	String buildQueryResultXml(QueryContextTo queryContextTo);

	/**
	 * Marshal an I2b2 FURTHeR query result object graph to XML.
	 *
	 * @param queryResult
	 *            I2b2 FURTHeR query result
	 * @return XML string
	 * @throws JAXBException
	 */
	String marshalI2b2FurtherQueryResult(I2b2FurtherQueryResultTo queryResult);

	/**
	 * Parse i2b2 raw response bytes and extract the I2b2 query ID (query instance ID).
	 *
	 * @param bytes
	 *            response bytes
	 * @return i2b2 query ID
	 */
	long extractI2b2QueryId(byte[] bytes);

	/**
	 * @param maxQueryTime
	 */
	void setMaxQueryTime(final long maxQueryTime);

	/**
	 * @return
	 */
	long getMaxQueryTime();
}
