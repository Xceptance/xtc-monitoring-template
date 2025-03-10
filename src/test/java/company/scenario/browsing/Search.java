package company.scenario.browsing;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.engine.Session;

import company.util.OpenPageFlow;
import company.util.TestdataHelper;
import company.util.WarmUpFlow;

/**
 * After browser warm up this test uses the search feature and searches for a phrase of 'xlt.<site>.searchTerms' to get
 * 'xlt.<site>.minResultsCount'. </br>
 * If the search term is not found due to a product being temporarily out of this we take the fall-back search phrases
 * and try try the next until 'xlt.<site>.minResultsCount' results is/are found
 */
public class Search extends AbstractBrowserScenario
{
    @Test
    public void test()
    {
        // standard warmup and first access of the homepage
        WarmUpFlow.warmup();
        var homepage = OpenPageFlow.openHomePage();

        // open the search field
        List<String> searchTerms = List.of(TestdataHelper.getLocalizedTestdata("searchTerms").split(","));
        Assert.assertFalse("Please configure at least one search term", searchTerms.isEmpty());

        // perform a search
        boolean resultsFound = false;
        for (int i = 0; i < searchTerms.size(); i++)
        {
            String searchTerm = searchTerms.get(i);
            var searchResultsPage = homepage.searchFor(searchTerm);
            Session.getCurrent().getValueLog().put("Search term used " + i, searchTerm);
            if (searchResultsPage.hasMinResults(Optional.ofNullable(TestdataHelper.getLocalizedTestdata("minResultsCount")).orElse("1")))
            {
                resultsFound = true;
                break;
            }
        }
        Assert.assertTrue("None of search terms " + searchTerms + " had results", resultsFound);
    }
}
