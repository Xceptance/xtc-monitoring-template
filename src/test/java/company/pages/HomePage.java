package company.pages;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

import java.time.Duration;

import com.xceptance.xlt.engine.scripting.TestContext;

/**
 * Interactions and validations available on the homepage.
 */
public class HomePage extends AbstractCatalogPage
{
    /**
     * Validates this page.
     */
    @Override
    public void validate()
    {
        super.validate();
        $("#carousel-sale").shouldBe(visible, Duration.ofMillis(TestContext.getCurrent().getTimeout()));
    }
}
