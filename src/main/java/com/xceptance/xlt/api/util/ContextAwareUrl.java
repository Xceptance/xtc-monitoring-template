package com.xceptance.xlt.api.util;

public class ContextAwareUrl
{
    public static String localeByTestLocation()
    {
        // determine location based on monitoring locations
        // this is external input and stored in a system variable
        // eu-west3 (DE), eu-west2 (UK), us-west1 (US), more are available of course.
        // This are the region strings (without the zone) of GCP datacenters. Only what
        // you have configured externally will be avaialble of course aka what setup the
        // monitoring has.
        final String location = System.getenv("XTC_MON_LOCATION") == null ? "NONE SET" : System.getenv("XTC_MON_LOCATION");

        String locale = null;

        switch (location)
        {
            case "europe-west3": // Frankfurt
                locale = "de-de";
                break;
            case "europe-west2": // London
                locale = "en-gb";
                break;
            case "us-west3": // Oregon
                locale = "en-us3";
                break;
            case "us-west1": // South Carolina
                locale = "en-us1"; // this is not really a locale of course... just for demo purposes
                break;
            default:
                // Assert.fail("Unknown Location: " + location);
                locale = "unknown"; // just so that local testing runs fine too
        }

        return locale;
    }

    /**
     * The posters store does not react to locale strings, this is here just for demo purposes how to look it up
     * @return a locale based on
     */
    public static String homepageByTestLocation()
    {
        return XltProperties.getInstance().getProperty("startUrl") + "/?locale=" + localeByTestLocation();
    }
}
