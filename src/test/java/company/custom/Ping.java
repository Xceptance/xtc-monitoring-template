package company.custom;

import org.htmlunit.WebResponse;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractLightWeightPageAction;
import com.xceptance.xlt.api.actions.AbstractWebAction;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;

/**
 * Action object to represent a simple browser-less ping
 */
public class Ping extends AbstractLightWeightPageAction
{
    private final String url;

    private final boolean contentTypeValidation;

    public Ping(final String url)
    {
        super(null, "PingStart");
        this.url = url;
        this.contentTypeValidation = true;
    }

    public Ping(final AbstractWebAction previous, final String url)
    {
        super(previous, "Ping");
        this.url = url;
        this.contentTypeValidation = true;
    }

    @Override
    public void preValidate() throws Exception
    {
    }

    @Override
    protected void execute() throws Exception
    {
        loadPage(url);
    }

    @Override
    protected void postValidate() throws Exception
    {
        final LightWeightPage page = getLightWeightPage();

        // basic checks
        final WebResponse webResponse = page.getWebResponse();
        Assert.assertEquals("Unexpected status code:", 200, webResponse.getStatusCode());

        if (this.contentTypeValidation)
        {
            Assert.assertEquals("Unexpected content type:", "text/html", webResponse.getContentType());
        }
    }
}
