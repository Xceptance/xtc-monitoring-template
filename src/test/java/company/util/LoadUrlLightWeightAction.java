package company.util;

import org.htmlunit.WebResponse;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractLightWeightPageAction;
import com.xceptance.xlt.api.actions.AbstractWebAction;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;

/**
 * A action for loading URLs directly without a real browser, only retrieving the base HTML. This action performs
 * minimal validation before and after loading the URL.
 */

public class LoadUrlLightWeightAction extends AbstractLightWeightPageAction
{
    private final String url;

    public LoadUrlLightWeightAction(String name, String url)
    {
        super((AbstractWebAction) Session.getCurrent().getValueLog().get("lastAction"), name);
        this.url = url;

        Session.getCurrent().getValueLog().put("lastAction", this);
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
        Assert.assertEquals("Unexpected content type:", "text/html", webResponse.getContentType());

    }

    @Override
    public String toString()
    {
        return getTimerName();
    }
}
