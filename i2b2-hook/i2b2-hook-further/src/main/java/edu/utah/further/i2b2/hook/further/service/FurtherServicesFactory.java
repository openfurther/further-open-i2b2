/*******************************************************************************
 * Source File: FurtherServicesFactory.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further.service;

/**
 * A factory of FURTHeR service implementations.
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
public abstract class FurtherServicesFactory
{
	// ========================= METHODS ===================================

	/**
	 * A factory method with no construction arguments.
	 *
	 * @return {@link FurtherServices} instances
	 */
	public static FurtherServices newFurtherServicesInstance()
	{
		return new FurtherServicesImpl();
	}
}
