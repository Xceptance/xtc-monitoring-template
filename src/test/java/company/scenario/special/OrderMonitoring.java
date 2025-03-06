package company.scenario.special;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;

import com.xceptance.ordermonitoring.OMAssertion;
import com.xceptance.ordermonitoring.model.configuration.PropertiesHelper;
import com.xceptance.ordermonitoring.model.configuration.dataobject.TimeBasedRequirement;
import com.xceptance.xlt.api.tests.AbstractTestCase;

public class OrderMonitoring extends AbstractTestCase
{
    @Test
    public void test()
    {
        final ZonedDateTime now = LocalDateTime.now(ZoneId.of("Z")).atZone(ZoneId.of("UTC"));
        final List<TimeBasedRequirement> timebasedRequirements = PropertiesHelper
                                                                                 .getTheMostRelevantTimebasedRequirementsForTestClass(this.getClass(), now);
        OMAssertion.assertRequirementsMet(now, timebasedRequirements);
    }
}
