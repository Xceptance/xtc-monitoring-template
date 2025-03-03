package company.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.xceptance.xlt.api.util.XltProperties;

public class TestdataHelper
{
    /**
     * Reads location from XTC_MON_LOCATION environmental variable or, if not found, from 'xlt.location' and maps it to
     * site that is preferred for this location
     * 
     * @return preferred site
     */
    public static String getSite()
    {
        final String location = System.getenv("XTC_MON_LOCATION") == null ? XltProperties.getInstance().getProperty("xlt.location")
                                                                          : System.getenv("XTC_MON_LOCATION");

        String site = XltProperties.getInstance().getProperty("xlt.site." + location);

        Assert.assertFalse("No site defined for region: " + location, StringUtils.isBlank(site));

        return site;
    }

    /**
     * Reads base start URL from 'xlt.location' and maps it to specific URL that is preferred for the current site
     * 
     * @return localized start URL
     */
    public static String getStartUrl()
    {
        String siteSpecificStartUrl = XltProperties.getInstance().getProperty("xlt.startUrl." + getSite());

        Assert.assertFalse("No start URL for site " + getSite() + " set.", StringUtils.isBlank(siteSpecificStartUrl));

        return siteSpecificStartUrl;
    }

    /**
     * Reads 'xlt.<site>.<key>' property and returns the value
     * 
     * @param key
     *            property key
     * @return property value
     */
    public static String getLocalizedTestdata(String key)
    {
        return XltProperties.getInstance().getProperty("xlt." + getSite() + "." + key);
    }
}
