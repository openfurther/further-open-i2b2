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
package edu.utah.further.i2b2.hook.further.domain;

import static edu.utah.further.core.api.text.ToStringCustomStyles.SHORT_WITH_SPACES_STYLE;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.utah.further.core.api.collections.CollectionUtil;

/**
 * Transfer object containing the results of a FURTHeR query for display in the i2b2 web
 * client.
 * -----------------------------------------------------------------------------------<br>
 * (c) 2008-2013 FURTHeR Project, Health Sciences IT, University of Utah<br>
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
@XmlRootElement(name = "furtherQueryResult", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
public final class I2b2FurtherQueryResultTo
{
	// ========================= CONSTANTS =================================

	/**
	 * Default XML namespace for all entities in this package.
	 */
	public static final String NAMESPACE = ""; // "http://www.i2b2.org/xsd/hive/msg/1.1/";

	/**
	 * Indicates that a small count was scrubbed due to policy.
	 */
	public final static int COUNT_SCRUBBED = 0xDEADBEEF;

	// ========================= FIELDS ====================================

	/**
	 * Results from individual data sources.
	 */
	@XmlElementWrapper(name = "dataSources", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
	@XmlElement(name = "dataSource", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
	private List<I2b2FurtherDataSourceResult> dataSources = CollectionUtil.newList();

	/**
	 * List of federated join results.
	 */
	@XmlElementWrapper(name = "joins", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
	@XmlElement(name = "join", namespace = I2b2FurtherQueryResultTo.NAMESPACE)
	private List<I2b2FurtherJoinResult> joins = CollectionUtil.newList();

	// ========================= IMPL: Object ==============================

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, SHORT_WITH_SPACES_STYLE)
				.append("dataSources", dataSources)
				.append("joins", joins)
				.toString();
	}

	// ========================= GET & SET =================================

	/**
	 * Return the dataSources property.
	 * 
	 * @return the dataSources
	 */
	public List<I2b2FurtherDataSourceResult> getDataSources()
	{
		return dataSources;
	}

	/**
	 * Set a new value for the dataSources property.
	 * 
	 * @param dataSources
	 *            the dataSources to set
	 */
	public void setDataSources(final List<I2b2FurtherDataSourceResult> dataSources)
	{
		this.dataSources = dataSources;
	}

	/**
	 * Return the joins property.
	 * 
	 * @return the joins
	 */
	public List<I2b2FurtherJoinResult> getJoins()
	{
		return joins;
	}

	/**
	 * Set a new value for the joins property.
	 * 
	 * @param joins
	 *            the joins to set
	 */
	public void setJoins(final List<I2b2FurtherJoinResult> joins)
	{
		this.joins = joins;
	}

	// ========================= GET & SET =================================

}
