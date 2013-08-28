/*******************************************************************************
 * Source File: I2b2FurtherJoinResult.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further.domain;

import static edu.utah.further.core.api.text.ToStringCustomStyles.SHORT_WITH_SPACES_STYLE;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Transfer object containing a federated join result.
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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dataSource", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
public final class I2b2FurtherJoinResult
{
	// ========================= FIELDS ====================================

	// Fields are currently identical to I2b2FurtherDataSourceResult, but may differ in
	// the future

	/**
	 * Display name of this join.
	 */
	@XmlElement(name = "name", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
	private String name;

	/**
	 * #records obtained from the data source.
	 */
	@XmlElement(name = "count", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
	private long count;

	// ========================= CONSTRUCTORS ==============================

	/**
	 * JAXB-required.
	 */
	public I2b2FurtherJoinResult()
	{
		super();
	}

	/**
	 * Initialize all fields.
	 * 
	 * @param name
	 *            name
	 * @param count
	 *            count
	 */
	public I2b2FurtherJoinResult(final String name, final long count)
	{
		super();
		this.name = name;
		this.count = count;
	}

	// ========================= IMPL: Object ==============================

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, SHORT_WITH_SPACES_STYLE)
				.append("name", name)
				.append("count", count)
				.toString();
	}

	// ========================= GET & SET =================================

	/**
	 * Return the name property.
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set a new value for the name property.
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * Return the count property.
	 * 
	 * @return the count
	 */
	public long getCount()
	{
		return count;
	}

	/**
	 * Set a new value for the count property.
	 * 
	 * @param count
	 *            the count to set
	 */
	public void setCount(final long count)
	{
		this.count = count;
	}
}
