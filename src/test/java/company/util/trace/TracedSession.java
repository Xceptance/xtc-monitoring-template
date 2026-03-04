package company.util.trace;

import org.openqa.selenium.WebDriver;

import com.codeborne.selenide.proxy.SelenideProxyServer;

public class TracedSession implements AutoCloseable
{
    private final WebDriver driver;

    private final SelenideProxyServer proxy;

    private final AutoCloseable cleanupTask;

    public TracedSession(WebDriver driver, SelenideProxyServer proxy, AutoCloseable cleanupTask)
    {
        this.driver = driver;
        this.proxy = proxy;
        this.cleanupTask = cleanupTask;
    }

    public WebDriver getDriver()
    {
        return driver;
    }

    public SelenideProxyServer getProxy()
    {
        return proxy;
    }

    // This handles the teardown for ALL approaches automatically
    @Override
    public void close() throws Exception
    {
        if (cleanupTask != null)
        {
            cleanupTask.close(); // Cleans up Interceptor or DevTools
        }
        if (proxy != null)
        {
            proxy.shutdown(); // Shuts down the proxy server
        }
    }
}
