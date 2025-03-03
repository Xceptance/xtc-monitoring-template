package com.xceptance.ordermonitoring;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import com.xceptance.ordermonitoring.model.configuration.CustomPredicate;
import com.xceptance.ordermonitoring.model.configuration.LocalePredicate;
import com.xceptance.ordermonitoring.model.configuration.ParserHelper;
import com.xceptance.ordermonitoring.model.configuration.PaymentPredicate;
import com.xceptance.ordermonitoring.model.configuration.dataobject.Requirement;
import com.xceptance.ordermonitoring.model.configuration.dataobject.TimeBasedRequirement;
import com.xceptance.ordermonitoring.model.query.OrderQuery;
import com.xceptance.ordermonitoring.model.query.OrderQuery.OrderQueryBuilder;
import com.xceptance.ordermonitoring.model.query.filter.DateTimeFilter;
import com.xceptance.ordermonitoring.model.query.filter.OrderStatusFilter;
import com.xceptance.ordermonitoring.model.query.filter.TermFilter.Operator;
import com.xceptance.ordermonitoring.model.requests.OrderMonitoringUtils;

public class OMAssertion
{
    public static void assertRequirementsMet(final TimeBasedRequirement timebasedRequirement)
    {
        checkRequirementsMet(ZonedDateTime.now(ZoneId.of("UTC")), timebasedRequirement);
    }

    public static void assertRequirementsMet(final ZonedDateTime now, final List<TimeBasedRequirement> timebasedRequirements)
    {
        boolean oneOfCriterionsFailed = false;
        String errorMessage = "";
        for (TimeBasedRequirement timebasedRequirement : timebasedRequirements)
        {
            System.out.println(now.withZoneSameInstant(ZoneId.of(timebasedRequirement.getTimeZone())).format(ParserHelper.humanDateTimeFormat));

            Optional<String> criterionErrorMessage = OMAssertion.checkRequirementsMet(now, timebasedRequirement);
            if (!criterionErrorMessage.isEmpty())
            {
                oneOfCriterionsFailed = true;
                errorMessage += criterionErrorMessage.get();
            }
        }
        Assert.assertFalse(errorMessage, oneOfCriterionsFailed);
    }

    public static Optional<String> checkRequirementsMet(final ZonedDateTime now, final TimeBasedRequirement timebasedRequirement)
    {
        final long consideredMinutes = timebasedRequirement.getConsideredPeriod();
        final Requirement requirement = timebasedRequirement.getRequirement();

        final OrderQueryBuilder builder = new OrderQueryBuilder(timebasedRequirement.getSite());
        builder.addFilter(new OrderStatusFilter(Operator.ONE_OF, timebasedRequirement.getOrderStatusesAsList()));
        var dateTimeFilter = new DateTimeFilter(now.minusMinutes(consideredMinutes), now);
        builder.addFilter(dateTimeFilter);
        final JsonObject response = OrderMonitoringUtils.getOrders(builder.build());
        JsonObject responseAddition = response;
        while (responseAddition.getAsJsonObject().get("next") != null)
        {
            int nextStart = response.getAsJsonObject().get("next").getAsJsonObject().get("start").getAsInt();
            responseAddition = OrderMonitoringUtils.getOrders(builder.setStart(nextStart).build());
            if (responseAddition.getAsJsonObject().get("next") == null)
            {
                response.remove("next");
            }
            else
            {
                response.add("next", responseAddition.getAsJsonObject().get("next"));
            }
            response.getAsJsonObject().get("hits").getAsJsonArray().addAll(responseAddition.getAsJsonObject().get("hits").getAsJsonArray());
        }
        long total = getTotal(response, timebasedRequirement.getLocale(), timebasedRequirement.getPaymentMethodsAsList(),
                              timebasedRequirement.getCustomConditions(), timebasedRequirement.getPathsToUniqueAttributes());
        System.out.println("Actual total " + total);
        if (requirement.getMinimalOrderAmount() != null)
        {
            if (requirement.getMinimalOrderAmount() > total)
            {
                String errorMessage = timebasedRequirement.getSite() + "/" + timebasedRequirement.getLocale()
                                      + ": only " + total
                                      + " of " + timebasedRequirement.getOrderStatusesAsList() + " orders "
                                      + (timebasedRequirement.getPaymentMethodsAsList().isEmpty() ? ""
                                                                                                  : " with payment methods "
                                                                                                    + timebasedRequirement.getPaymentMethodsAsList() + " ")
                                      + "in timeframe " + dateTimeFilter.getZonedString(timebasedRequirement.getTimeZone()) + " (last " + consideredMinutes
                                      + " minutes) were placed. Expected minimum: " + requirement.getMinimalOrderAmount() + ";";
                return Optional.of(errorMessage + StringUtils.repeat(" ", 190 - (errorMessage.length() - (errorMessage.length() / 190) * 190)));
            }
        }
        if (requirement.getMaximalOrderAmount() != null)
        {
            if (requirement.getMaximalOrderAmount() < total)
            {
                String errorMessage = timebasedRequirement.getSite() + "/" + timebasedRequirement.getLocale()
                                      + ": " + total
                                      + " of " + timebasedRequirement.getOrderStatusesAsList() + " orders "
                                      + (timebasedRequirement.getPaymentMethodsAsList().isEmpty() ? ""
                                                                                                  : " with payment methods "
                                                                                                    + timebasedRequirement.getPaymentMethodsAsList() + " ")
                                      + "in timeframe " + dateTimeFilter.getZonedString(timebasedRequirement.getTimeZone()) + " (last " + consideredMinutes
                                      + " minutes) were placed. Expected maximum: " + requirement.getMaximalOrderAmount() + ";";
                return Optional.of(errorMessage + StringUtils.repeat(" ", 190 - (errorMessage.length() - (errorMessage.length() / 190) * 190)));

            }
        }
        if (requirement.getMinimalOrderPercentageWithFeature() != null
            || requirement.getMaximalOrderPercentageWithFeature() != null)
        {
            final OrderQuery queryAllNewOrdersForConsideredPeriod = new OrderQueryBuilder(timebasedRequirement.getSite())
                                                                                                                         .addFilter(new DateTimeFilter(now.minusMinutes(consideredMinutes), now))
                                                                                                                         .build();
            final JsonObject allOrdersForConsideredPeriod = OrderMonitoringUtils
                                                                                .getOrders(queryAllNewOrdersForConsideredPeriod);
            final long amountOfAllOrders = getTotal(allOrdersForConsideredPeriod, timebasedRequirement.getLocale(), new ArrayList<String>(),
                                                    new HashMap<String, Map<String, String>>(), timebasedRequirement.getPathsToUniqueAttributes());

            // Assert.assertNotEquals("No order was placed in last " + consideredMinutes + " minutes", 0,
            // amountOfAllOrders);
            if (requirement.getMinimalOrderPercentageWithFeature() != null  && amountOfAllOrders > timebasedRequirement.getMaxTotalOrderNumberToIgnoreConditon())
            {
                if (requirement.getMinimalOrderPercentageWithFeature() > (total * 100 / amountOfAllOrders))
                {
                    String errorMessage = timebasedRequirement.getSite() + "/" + timebasedRequirement.getLocale()
                                          + ": only " + (total * 100 / amountOfAllOrders)
                                          + "% of " + timebasedRequirement.getOrderStatusesAsList() + " orders "
                                          + (timebasedRequirement.getPaymentMethodsAsList().isEmpty() ? ""
                                                                                                      : " with payment methods "
                                                                                                        + timebasedRequirement.getPaymentMethodsAsList() + " ")
                                          + "in timeframe " + dateTimeFilter.getZonedString(timebasedRequirement.getTimeZone()) + " (last " + consideredMinutes
                                          + " minutes) is reached. Expected minimum: " + requirement.getMinimalOrderPercentageWithFeature() + ";";
                    return Optional.of(errorMessage + StringUtils.repeat(" ", 190 - (errorMessage.length() - (errorMessage.length() / 190) * 190)));
                }
            }
            if (requirement.getMaximalOrderPercentageWithFeature() != null && amountOfAllOrders > timebasedRequirement.getMaxTotalOrderNumberToIgnoreConditon())
            {
                if (requirement.getMaximalOrderPercentageWithFeature() < (total * 100 / amountOfAllOrders))
                {
                    String errorMessage = timebasedRequirement.getSite() + "/" + timebasedRequirement.getLocale()
                                          + ": " + (total * 100 / amountOfAllOrders)
                                          + "% of " + timebasedRequirement.getOrderStatusesAsList() + " orders "
                                          + (timebasedRequirement.getPaymentMethodsAsList().isEmpty() ? ""
                                                                                                      : " with payment methods "
                                                                                                        + timebasedRequirement.getPaymentMethodsAsList() + " ")
                                          + "in timeframe " + dateTimeFilter.getZonedString(timebasedRequirement.getTimeZone()) + " (last " + consideredMinutes
                                          + " minutes) is reached. Expected maximum: " + requirement.getMaximalOrderPercentageWithFeature() + ";";
                    return Optional.of(errorMessage + StringUtils.repeat(" ", 190 - (errorMessage.length() - (errorMessage.length() / 190) * 190)));
                }
            }
        }
        return Optional.empty();
    }

    public static long getTotal(final JsonObject response, String locale, List<String> paymentMethods, Map<String, Map<String, String>> customConditions,
                                List<String> pathsToUniqueAttributes)
    {
        if (response.get("hits") == null)
        {
            return 0;
        }
        String res = JsonPath.parse(response.toString()).read("$.hits.*.data[?]", new LocalePredicate(locale)).toString();
        if (!paymentMethods.isEmpty())
        {
            res = JsonPath.parse(response.toString()).read("$.hits.*.data[?]", new PaymentPredicate(paymentMethods))
                          .toString();
        }

        if (!customConditions.keySet().isEmpty())
        {
            res = JsonPath.parse(response.toString())
                          .read("$.hits.*.data[?]",
                                new CustomPredicate(customConditions))
                          .toString();
        }
        //
        // if (!customConditions.keySet().isEmpty() && !paymentMethods.isEmpty())
        // {
        // res = JsonPath.parse(response.toString())
        // .read("$.hits.*.data[?]", new PaymentPredicate(paymentMethods), new LocalePredicate(locale),
        // new CustomPredicate(customConditions))
        // .toString();
        // }
        // else if (customConditions.keySet().isEmpty() && !paymentMethods.isEmpty())
        // {
        // res = JsonPath.parse(response.toString()).read("$.hits.*.data[?]", new PaymentPredicate(paymentMethods), new
        // LocalePredicate(locale))
        // .toString();
        // }
        // else if (paymentMethods.isEmpty() && !customConditions.keySet().isEmpty())
        // {
        // res = JsonPath.parse(response.toString()).read("$.hits.*.data[?]", new LocalePredicate(locale), new
        // CustomPredicate(customConditions))
        // .toString();
        // }
        // else
        // {
        // res = JsonPath.parse(response.toString()).read("$.hits.*.data[?]", new LocalePredicate(locale)).toString();
        // }
        // }
        if (pathsToUniqueAttributes != null && !pathsToUniqueAttributes.isEmpty())
        {
            List<String> pathsCombination = JsonParser.parseString(JsonPath.parse(res).read(pathsToUniqueAttributes.get(0)).toString())
                                                      .getAsJsonArray().asList().stream().map(je -> je.getAsString()).collect(Collectors.toList());
            if (pathsCombination.size() > 0)
            {
                for (int i = 1; i < pathsToUniqueAttributes.size(); i++)
                {
                    List<JsonElement> pathsPartCombination = JsonParser.parseString(JsonPath.parse(res.toString()).read(pathsToUniqueAttributes.get(i))
                                                                                            .toString())
                                                                       .getAsJsonArray().asList();
                    for (int j = 0; j < pathsPartCombination.size(); j++)
                    {
                        pathsCombination.set(j, pathsCombination.get(j).concat("_").concat(pathsPartCombination.get(j).getAsString()));
                    }

                }
            }
            return pathsCombination.stream()
                                   .distinct()
                                   .count();
        }
        return JsonParser.parseString(res).getAsJsonArray().size();
    }
}
