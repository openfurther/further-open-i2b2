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
package edu.utah.further.i2b2.hook.further;

import static edu.utah.further.core.api.collections.CollectionUtil.newList;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import com.sun.xml.bind.api.JAXBRIContext;

import edu.utah.further.core.api.constant.Strings;
import edu.utah.further.core.api.time.TimeService;
import edu.utah.further.core.api.xml.XmlService;
import edu.utah.further.core.test.annotation.UnitTest;
import edu.utah.further.core.util.io.IoUtil;
import edu.utah.further.core.xml.jaxb.JaxbConfigurationFactoryBean;
import edu.utah.further.core.xml.jaxb.XmlServiceImpl;
import edu.utah.further.fqe.ds.api.domain.QueryState;
import edu.utah.further.fqe.ds.api.service.results.ResultType;
import edu.utah.further.fqe.ds.api.to.QueryContextTo;
import edu.utah.further.fqe.ds.api.to.QueryContextToImpl;
import edu.utah.further.fqe.ds.api.util.FqeDsQueryContextUtil;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherDataSourceResult;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherJoinResult;
import edu.utah.further.i2b2.hook.further.domain.I2b2FurtherQueryResultTo;
import edu.utah.further.i2b2.hook.further.service.FurtherServices;
import edu.utah.further.i2b2.hook.further.service.FurtherServicesFactory;

/**
 * Unit tests of I2b2 request and response FURTHeR transfer objects.
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
 * @author Oren E. Livne {@code <oren.livne@utah.edu>}
 * @version Jan 6, 2011
 */
@UnitTest
public final class UTestI2b2FurtherTo {
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
	private static final Long resultMaskBoundary = 5l;

	// ========================= SETUP METHODS =============================

	@Before
	public void setup() {
		
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setNormalizeWhitespace(true);
		
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
	public void marshalFurtherQueryResult() throws Exception {
		marshallingTest(FURTHER_QUERY_RESULT_XML, newFurtherQueryResult());
	}

	// ========================= TEST METHODS ==============================

	/**
	 * Marshal an i2b2 message FURTHeR query result section to XML.
	 */
	@Test
	public void marshalFurtherQueryContextToResult() throws Exception {
		marshallingMaskedQueryContextTest(FURTHER_QUERY_RESULT_MASKED_XML);
	}

	// ========================= PRIVATE METHODS ===========================

	/**
	 * @param fileName
	 * @param entity
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXException
	 */
	private void marshallingTest(final String fileName,
			final I2b2FurtherQueryResultTo entity) throws JAXBException,
			IOException, SAXException {
		final String marshalled = furtherServices
				.marshalI2b2FurtherQueryResult(entity);
		if (log.isDebugEnabled()) {
			log.debug("Marshalled:" + Strings.NEW_LINE_STRING + marshalled);
		}
		assertNotNull("Marshalling failed", marshalled);
		final String expected = IoUtil.getResourceAsString(fileName);

		final DetailedDiff diff = new DetailedDiff(new Diff(expected,
				marshalled));
		assertTrue("XML is different" + diff.getAllDifferences(),
				diff.similar());
	}

	/**
	 * @param fileName
	 * @param entity
	 * @throws IOException
	 * @throws JAXBException
	 * @throws SAXException
	 */
	private void marshallingMaskedQueryContextTest(final String i2b2FileName)
			throws JAXBException, IOException, SAXException {
		final QueryContextTo entity = newQueryContextToBasicWithResultViews();
		final String marshalled = furtherServices.buildQueryResultXml(entity);
		if (log.isDebugEnabled()) {
			log.debug("Marshalled:" + Strings.NEW_LINE_STRING + marshalled);
		}
		assertNotNull("Marshalling failed", marshalled);
		final String expected = IoUtil.getResourceAsString(i2b2FileName);
		final DetailedDiff diff = new DetailedDiff(new Diff(expected,
				marshalled));
		assertTrue("XML is different" + diff.getAllDifferences(),
				diff.similar());
	}

	/**
	 * @return
	 */
	private I2b2FurtherQueryResultTo newFurtherQueryResult() {
		final I2b2FurtherQueryResultTo queryResult = new I2b2FurtherQueryResultTo();

		queryResult.getDataSources().add(
				new I2b2FurtherDataSourceResult("@DSCUSTOM-26@", 123));
		queryResult.getDataSources().add(
				new I2b2FurtherDataSourceResult("@DSCUSTOM-28@", 456));

		queryResult.getJoins().add(new I2b2FurtherJoinResult("Sum", 789));
		queryResult.getJoins().add(new I2b2FurtherJoinResult("Union", 500));
		queryResult.getJoins().add(
				new I2b2FurtherJoinResult("Intersection", 100));

		return queryResult;
	}

	/**
	 * @param query
	 * @return
	 */
	private QueryContextTo newQueryContextToBasic(final boolean withExecutionId) {
		final QueryContextTo queryContextTo = QueryContextToImpl.newInstance();
		// Set this fixed for asserting the XML
		if (withExecutionId) {
			queryContextTo
					.setExecutionId("3c0c8360-09f7-11e0-81e0-0800200c9a66");
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
	private QueryContextTo newQueryContextToBasicWithResultViews() {
		final QueryContextTo queryContextTo = newQueryContextToBasic(false);
		final List<QueryContextTo> children = newList();
		children.add(newChildQueryContextToWithResults("@DSCUSTOM-26@", 8l,
				queryContextTo));
		children.add(newChildQueryContextToWithResults("@DSCUSTOM-28@", 3l,
				queryContextTo));
		queryContextTo.addChildren(children);
		FqeDsQueryContextUtil.addResultViewTo(queryContextTo, ResultType.SUM, 11l);
		FqeDsQueryContextUtil.addResultViewTo(queryContextTo, ResultType.UNION, 9l);
		FqeDsQueryContextUtil.addResultViewTo(queryContextTo, ResultType.INTERSECTION, 1l);
		queryContextTo.setStaleDateTime(staleDateTime);
		return queryContextTo;
	}

	/**
	 * @param query
	 * @return
	 */
	@SuppressWarnings("boxing")
	private QueryContextTo newChildQueryContextToWithResults(
			String dataSourceId, Long numRecords, QueryContextTo parent) {
		final QueryContextTo queryContextTo = QueryContextToImpl.newInstance();
		queryContextTo.setNumRecords(numRecords);
		queryContextTo.setState(QueryState.COMPLETED);
		queryContextTo.setDataSourceId(dataSourceId);
		queryContextTo.setStaleDateTime(staleDateTime);
		queryContextTo.setParent(parent);
		return queryContextTo;
	}

}
