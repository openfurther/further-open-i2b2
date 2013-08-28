/*******************************************************************************
 * Source File: UTestI2b2FurtherTo.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further;

import static edu.utah.further.core.api.collections.CollectionUtil.newList;
import static edu.utah.further.core.qunit.runner.XmlAssertion.xmlAssertion;
import static edu.utah.further.fqe.ds.api.results.ResultType.INTERSECTION;
import static edu.utah.further.fqe.ds.api.results.ResultType.SUM;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.sun.xml.bind.api.JAXBRIContext;

import edu.utah.further.core.api.constant.Strings;
import edu.utah.further.core.api.time.TimeService;
import edu.utah.further.core.api.xml.XmlService;
import edu.utah.further.core.qunit.runner.XmlAssertion;
import edu.utah.further.core.util.io.IoUtil;
import edu.utah.further.core.xml.jaxb.JaxbConfigurationFactoryBean;
import edu.utah.further.core.xml.jaxb.XmlServiceImpl;
import edu.utah.further.fqe.ds.api.domain.QueryState;
import edu.utah.further.fqe.ds.api.to.QueryContextTo;
import edu.utah.further.fqe.ds.api.to.QueryContextToImpl;
import edu.utah.further.fqe.ds.api.util.FqeDsQueryContextUtil;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherDataSourceResult;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherJoinResult;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherQueryResultTo;
import edu.utah.further.i2b2.hook.further.service.FurtherServices;
import edu.utah.further.i2b2.hook.further.service.FurtherServicesFactory;
import edu.utah.further.core.test.annotation.UnitTest;

/**
 * Unit tests of I2b2 request and response FURTHeR transfer objects.
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
 * @version Jan 6, 2011
 */
@UnitTest
public final class UTestI2b2FurtherTo
{
	// ========================= CONSTANTS =================================

	/**
	 * A logger that helps identify this class' printouts.
	 */
	private static final Logger log = getLogger(UTestI2b2FurtherTo.class);

	/**
	 * Test file names.
	 */
	private static final String FURTHER_QUERY_RESULT_XML = "further-query-result.xml";
	
	private static final String FURTHER_QUERY_RESULT_MASKED_XML = "further-query-result-masked.xml";

	/**
	 * JAXB Configuration.
	 */
	private static final Map<String, Object> JAXB_CONFIG = JaxbConfigurationFactoryBean.DEFAULT_JAXB_CONFIG;

	// ========================= DEPENDENCIES ==============================

	/**
	 * Since we don't have spring here, use manual dependency injection.
	 */
	private final XmlService xmlService = new XmlServiceImpl();

	/**
	 * FURTHeR services.
	 */
	private final FurtherServices furtherServices = FurtherServicesFactory
			.newFurtherServicesInstance();
	
	/**
	 * Stale date & time for the test QC.
	 */
	private Date staleDateTime;
	
	/**
	 * The minimum number of results, otherwise masked
	 */
	@SuppressWarnings("boxing")
	private static final Long resultMaskBoundary = 5l ;

	// ========================= SETUP METHODS =============================

	
	@Before
	public void setup()
	{
		/* Override the base */
		JAXB_CONFIG.put(JAXBRIContext.DEFAULT_NAMESPACE_REMAP,
				I2b2FurtherQueryResultTo.NAMESPACE);
		xmlService.setDefaultJaxbConfig(JAXB_CONFIG);
		TimeService.fixSystemTime(10000);
		staleDateTime = TimeService.getDate();
		furtherServices.setResultMaskBoundary(resultMaskBoundary);
	}

	// ========================= TEST METHODS ==============================

	/**
	 * Marshal an i2b2 message FURTHeR query result section to XML.
	 */
	@Test
	public void marshalFurtherQueryResult() throws Exception
	{
		marshallingTest(FURTHER_QUERY_RESULT_XML, newFurtherQueryResult());
	}
	
	// ========================= TEST METHODS ==============================

	/**
	 * Marshal an i2b2 message FURTHeR query result section to XML.
	 */
	@Test
	public void marshalFurtherQueryContextToResult() throws Exception
	{
		marshallingMaskedQueryContextTest(FURTHER_QUERY_RESULT_MASKED_XML);
	}

	// ========================= PRIVATE METHODS ===========================

	/**
	 * @param fileName
	 * @param entity
	 * @throws IOException
	 * @throws JAXBException
	 */
	private void marshallingTest(final String fileName,
			final I2b2FurtherQueryResultTo entity) throws JAXBException, IOException
	{
		final String marshalled = furtherServices.marshalI2b2FurtherQueryResult(entity);
		if (log.isDebugEnabled())
		{
			log.debug("Marshalled:" + Strings.NEW_LINE_STRING + marshalled);
		}
		assertNotNull("Marshalling failed", marshalled);
		final String expected = IoUtil.getResourceAsString(fileName);
		xmlAssertion(XmlAssertion.Type.EXACT_MATCH)
				.actualResourceString(marshalled)
				.expectedResourceString(expected)
				.stripNewLinesAndTabs(true)
				.doAssert();
	}
	
	/**
	 * @param fileName
	 * @param entity
	 * @throws IOException
	 * @throws JAXBException
	 */
	private void marshallingMaskedQueryContextTest(final String i2b2FileName) throws JAXBException, IOException
	{
		final QueryContextTo entity = newQueryContextToBasicWithResultViews();
		final String marshalled = furtherServices.buildQueryResultXml(entity);
		if (log.isDebugEnabled())
		{
			log.debug("Marshalled:" + Strings.NEW_LINE_STRING + marshalled);
		}
		assertNotNull("Marshalling failed", marshalled);
		final String expected = IoUtil.getResourceAsString(i2b2FileName);
		xmlAssertion(XmlAssertion.Type.EXACT_MATCH)
				.actualResourceString(marshalled)
				.expectedResourceString(expected)
				.stripNewLinesAndTabs(true)
				.doAssert();
	}

	/**
	 * @return
	 */
	private I2b2FurtherQueryResultTo newFurtherQueryResult()
	{
		final I2b2FurtherQueryResultTo queryResult = new I2b2FurtherQueryResultTo();

		queryResult.getDataSources().add(new I2b2FurtherDataSourceResult("UUEDW", 123));
		queryResult.getDataSources().add(new I2b2FurtherDataSourceResult("UPDBL", 456));

		queryResult.getJoins().add(new I2b2FurtherJoinResult("Sum", 789));
		queryResult.getJoins().add(new I2b2FurtherJoinResult("Union", 500));
		queryResult.getJoins().add(new I2b2FurtherJoinResult("Intersection", 100));

		return queryResult;
	}
	
	/**
	 * @param query
	 * @return
	 */
	private QueryContextTo newQueryContextToBasic(final boolean withExecutionId)
	{
		final QueryContextTo queryContextTo = QueryContextToImpl.newInstance();
		//Set this fixed for asserting the XML
		if (withExecutionId) {
			queryContextTo.setExecutionId("3c0c8360-09f7-11e0-81e0-0800200c9a66");
		}
		queryContextTo.setMinRespondingDataSources(2);
		queryContextTo.setMaxRespondingDataSources(100);
		queryContextTo.setStaleDateTime(staleDateTime);
		return queryContextTo;
	}

	/**
	 * @param query
	 * @return
	 */
	@SuppressWarnings("boxing")
	private QueryContextTo newQueryContextToBasicWithResultViews()
	{
		final QueryContextTo queryContextTo = newQueryContextToBasic(false);
		final List<QueryContextTo> children = newList();
		children.add(newChildQueryContextToWithResults("UUEDW", 8l, queryContextTo));
		children.add(newChildQueryContextToWithResults("UPDBL", 3l, queryContextTo));
		queryContextTo.addChildren(children);
		FqeDsQueryContextUtil.addResultViewTo(queryContextTo, SUM, null, 11l);
		FqeDsQueryContextUtil.addResultViewTo(queryContextTo, INTERSECTION, 1, 9l);
		FqeDsQueryContextUtil.addResultViewTo(queryContextTo, INTERSECTION, 2, 1l);
		queryContextTo.setStaleDateTime(staleDateTime);
		return queryContextTo;
	}

	/**
	 * @param query
	 * @return
	 */
	@SuppressWarnings("boxing")
	private QueryContextTo newChildQueryContextToWithResults(String dataSourceId, Long numRecords, QueryContextTo parent)
	{
		final QueryContextTo queryContextTo = QueryContextToImpl.newInstance();
		queryContextTo.setNumRecords(numRecords);
		queryContextTo.setState(QueryState.COMPLETED);
		queryContextTo.setDataSourceId(dataSourceId);
		queryContextTo.setStaleDateTime(staleDateTime);
		queryContextTo.setParent(parent);
		return queryContextTo;
	}
	
}
