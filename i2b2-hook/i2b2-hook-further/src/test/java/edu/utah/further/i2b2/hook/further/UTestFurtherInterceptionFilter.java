/*******************************************************************************
 * Source File: UTestRawI2b2Converter.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import edu.utah.further.core.api.lang.CoreUtil;
import edu.utah.further.i2b2.hook.further.service.FurtherServices;
import edu.utah.further.i2b2.hook.further.service.FurtherServicesFactory;

/**
 * Learning how to parse an XML document using the Java XPath API.
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
 * @version Dec 5, 2008
 * @see http://www.ibm.com/developerworks/library/x-javaxpathapi.html#changed
 */
public final class UTestFurtherInterceptionFilter
{
	// ========================= CONSTANTS =================================

	/**
	 * I2b2 query input XML.
	 */
	private static final String RAW_REQUEST_XML = "i2b2-raw-request.xml";

	/**
	 * Unprocessed I2b2 response XML.
	 */
	private static final String RAW_RESPONSE_XML = "i2b2-raw-response.xml";

	// ========================= FIELDS ====================================

	// ========================= DEPENDENCIES ==============================

	/**
	 * FURTHeR services.
	 */
	private final FurtherServices furtherServices = FurtherServicesFactory
			.newFurtherServicesInstance();

	/**
	 * Filter instance to test.
	 */
	private final FurtherInterceptionFilter filter = new FurtherInterceptionFilter();

	// ========================= SETUP METHODS =============================

	// ========================= METHODS ===================================

	/**
	 * Test reading the FURTHeR configuration flags from an i2b2 XML request.
	 */
	@Test
	public void readFurtherConfig() throws Exception
	{
		final byte[] bytes = getXmlResourceAsBytes(RAW_REQUEST_XML);
		assertTrue("Unexpected FQE processing flag value", filter.isProcessRequest(bytes));
	}

	/**
	 * Test reading the i2b2 query ID from an i2b2 response.
	 */
	@SuppressWarnings("boxing")
	@Test
	public void readQueryInstanceId() throws Exception
	{
		final byte[] bytes = getXmlResourceAsBytes(RAW_RESPONSE_XML);
		final Long i2b2QueryId = furtherServices.extractI2b2QueryId(bytes);
		assertThat(i2b2QueryId, is(2416l));
	}

	// ========================= PRIVATE METHODS ===========================

	/**
	 * @param resourceName
	 * @return
	 * @throws IOException
	 */
	private byte[] getXmlResourceAsBytes(final String resourceName) throws IOException
	{
		final InputStream inputStream = CoreUtil.getResourceAsStream(resourceName);
		final byte[] bytes = CoreUtil.readBytesFromStream(inputStream);
		inputStream.close();
		return bytes;
	}
}
