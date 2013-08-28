/*******************************************************************************
 * Source File: SuiteWsFurtherHook.java
 ******************************************************************************/
package edu.utah.further.i2b2.hook.further;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Apelon low-level API tests.
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
 * @version May 29, 2009
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{ UTestFurtherInterceptionFilter.class, UTestI2b2FurtherTo.class })
public final class SuiteI2b2WsFurtherHook
{
}
