package company.util.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;

import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltChromeDriver;

public class BrowserSetup
{
    private static final String PROP_PREFIX_WEB_DRIVER = "xlt.webDriver";

    // It only works for chromium so make sure to use chromium properties (or maybe change it here to chrome)
    private static final String WEBDRIVER_TYPE = "chromium";

    private static final Pattern BROWSER_ARGS_PATTERN = Pattern.compile("\"(.*?)\"|'(.*?)'|\\S+");

    public static XltChromeDriver setupWebDriver(ChromeOptions chromeOptions)
    {
        // Note: always look up the properties freshly to get test-case-specific settings
        final XltProperties props = XltProperties.getInstance();

        // get the web-driver-specific path to the browser
        final String pathToBrowser = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + WEBDRIVER_TYPE + ".pathToBrowser", null);

        // get the web-driver-specific browser command line arguments
        final String browserArgs = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + WEBDRIVER_TYPE + ".browserArgs", null);

        // get the web-driver-specific path to the driver server
        final String pathToDriverServer = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + WEBDRIVER_TYPE + ".pathToDriverServer", null);

        // get the web-driver-specific page load strategy
        final String pageLoadStrategy = props.getProperty(PROP_PREFIX_WEB_DRIVER + "." + WEBDRIVER_TYPE + ".pageLoadStrategy", null);

        // get/create the driver
        final XltChromeDriver webDriver;
        // always create a fresh driver
        webDriver = createWebDriver(chromeOptions, pathToDriverServer, pathToBrowser, browserArgs, pageLoadStrategy);

        // get the configured size of browser window and whether it should be maximized
        final int windowWidth = props.getProperty(PROP_PREFIX_WEB_DRIVER + ".window.width", -1);
        final int windowHeight = props.getProperty(PROP_PREFIX_WEB_DRIVER + ".window.height", -1);
        final boolean maximizeWindow = props.getProperty(PROP_PREFIX_WEB_DRIVER + ".window.maximize", false);

        // maximize window
        if (maximizeWindow)
        {
            webDriver.manage().window().maximize();
        }
        // resize browser window if sizes are defined
        else if (windowWidth > 0 && windowHeight > 0)
        {
            final Dimension windowSize = new Dimension(windowWidth, windowHeight);
            webDriver.manage().window().setSize(windowSize);
        }
        // log actual size of browser window to runtime logger
        logWindowSize(webDriver);

        return webDriver;
    }

    /**
     * Creates a new {@link WebDriver} instance.
     *
     * @param pathToDriverServer
     *     the path to the driver server if the driver requires one (may be blank)
     * @param pathToBrowser
     *     the path to the browser binary
     * @param browserArgs
     *     additional browser command line arguments
     * @param pageLoadStrategy
     *     the page load strategy to use (may be blank)
     * @return the new {@link WebDriver} instance
     */
    public static XltChromeDriver createWebDriver(ChromeOptions chromeOptions, final String pathToDriverServer,
                                            final String pathToBrowser,
                                            final String browserArgs, final String pageLoadStrategy)
    {
        final XltChromeDriver webDriver;

        setPathToDriverServer(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, pathToDriverServer);

        ChromeOptions options = setXltChromeOptions(chromeOptions, pathToBrowser, browserArgs, pageLoadStrategy);
        // options.setBinary("/usr/bin/chromium"); // set the chromium binary path - TODO use config value for this

        webDriver = new XltChromeDriver(options);

        return webDriver;
    }

    /**
     * Logs the actual size of the browser window to the runtime logger at level INFO.
     *
     * @param webDriver
     *     the {@link WebDriver} instance
     */
    private static void logWindowSize(final WebDriver webDriver)
    {
        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            final Dimension windowDim = webDriver.manage().window().getSize();
            XltLogger.runTimeLogger.info("Size of browser window: " + windowDim.getWidth() + " x " + windowDim.getHeight());
        }
    }

    /**
     * Sets the path to the driver server in the system environment, but only if the path is not blank.
     *
     * @param propertyName
     *     the name of the system property
     * @param path
     *     the path
     */
    private static void setPathToDriverServer(final String propertyName, final String path)
    {
        if (StringUtils.isNotBlank(path))
        {
            System.setProperty(propertyName, path);
        }
    }

    /**
     * Creates a {@link ChromeOptions} object and sets the path, but only if the path is not blank.
     *
     * @param pathToBrowser
     *     the path to the browser binary
     * @param browserArgs
     *     additional browser command line arguments
     * @param pageLoadStrategy
     *     the page loading strategy
     * @return the Chrome options
     */
    private static ChromeOptions setXltChromeOptions(ChromeOptions options, final String pathToBrowser, final String browserArgs, String pageLoadStrategy)
    {
        if (StringUtils.isNotBlank(pathToBrowser))
        {
            options.setBinary(pathToBrowser);
        }

        if (StringUtils.isNotBlank(browserArgs))
        {
            final List<String> args = parseBrowserArgs(browserArgs);
            options.addArguments(args);
        }

        if (StringUtils.isNotBlank(pageLoadStrategy))
        {
            options.setPageLoadStrategy(PageLoadStrategy.fromString(pageLoadStrategy));
        }

        return options;
    }

    /**
     * Parses a line of quoted or unquoted browser arguments into a list of separate arguments. Unless quoted, the input is split on whitespace characters. Both
     * single and double quotes may be used as quoting character, but in pairs only. The quotes around quoted arguments are removed.
     * <pre>
     * browserArgs: -a --b c "--d=foo bar" 'baz bum'
     *      result: [-a, --b, c, --d=foo bar, baz bum ]
     * </pre>
     *
     * @param browserArgs
     *     the browser arguments as a single string
     * @return the list of parsed browser arguments
     */
    static List<String> parseBrowserArgs(final String browserArgs)
    {
        final List<String> args = new ArrayList<>();

        final Matcher matcher = BROWSER_ARGS_PATTERN.matcher(browserArgs);
        while (matcher.find())
        {
            // (1) get the content of a double-quoted argument
            String arg = matcher.group(1);
            if (arg == null)
            {
                // (2) get the content of a single-quoted argument
                arg = matcher.group(2);
                if (arg == null)
                {
                    // (3) get the full argument text
                    arg = matcher.group();
                }
            }

            args.add(arg);
        }

        return args;
    }
}
