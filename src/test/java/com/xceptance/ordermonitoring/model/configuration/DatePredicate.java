package com.xceptance.ordermonitoring.model.configuration;

import java.time.LocalDate;
import java.util.Map;

import com.jayway.jsonpath.Predicate;

public class DatePredicate implements Predicate
{
    private final LocalDate today;

    public DatePredicate(final LocalDate today)
    {
        this.today = today;
    }

    @Override
    public boolean apply(final PredicateContext context)
    {
        final String fromString = context.item(Map.class).get(ParserHelper.exclusivePeriodFromKey).toString();
        final String toString = context.item(Map.class).get(ParserHelper.exclusivePeriodToKey).toString();
        final LocalDate from = LocalDate.parse(fromString, ParserHelper.dateFormat);
        final LocalDate to = LocalDate.parse(toString, ParserHelper.dateFormat);
        final boolean isInPeriod = today.isAfter(from) && today.isBefore(to) || today.equals(from) || today.equals(to);
        return isInPeriod;
    }
}
