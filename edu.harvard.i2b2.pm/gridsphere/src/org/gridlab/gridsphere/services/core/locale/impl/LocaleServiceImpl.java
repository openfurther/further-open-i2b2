package org.gridlab.gridsphere.services.core.locale.impl;

import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.services.core.locale.LocaleService;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: LocaleServiceImpl.java,v 1.1 2007/08/24 17:24:53 mem61 Exp $
 */
public class LocaleServiceImpl implements PortletServiceProvider, LocaleService {

    private Locale[] locales;

    public void init(PortletServiceConfig config) {
        String displayLocales = config.getInitParameter("display-locale");
        StringTokenizer localeTokenizer = new StringTokenizer(displayLocales, ",");
        locales = new Locale[localeTokenizer.countTokens()];
        int i = 0;
        while (localeTokenizer.hasMoreTokens()) {
            String displayLocaleStr = localeTokenizer.nextToken();
            Locale displayLocale = new Locale(displayLocaleStr, "", "");
            locales[i] = displayLocale;
            i++;
        }
    }

    public Locale[] getSupportedLocales() {
        return locales;
    }

    public void destroy() {
    }


}
