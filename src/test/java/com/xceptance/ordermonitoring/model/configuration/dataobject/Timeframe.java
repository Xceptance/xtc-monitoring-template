package com.xceptance.ordermonitoring.model.configuration.dataobject;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import com.xceptance.ordermonitoring.model.configuration.ParserHelper;

public class Timeframe implements Comparable<Timeframe>
{
    private final LocalTime from;

    private final LocalTime to;

    public Timeframe(final LocalTime from, final LocalTime to)
    {
        this.from = from;
        this.to = to;
    }

    public LocalTime getFrom()
    {
        return from;
    }

    public LocalTime getTo()
    {
        return to;
    }

    public boolean isActiveNow(final LocalTime now)
    {
        return now.isAfter(getFrom()) && now.isBefore(getTo()) || now.equals(getFrom()) || now.equals(getTo());
    }

    public boolean isOverlapping(final Timeframe now)
    {
        return now.getFrom().isAfter(getFrom()) && now.getFrom().isBefore(getTo()) || now.getTo().isAfter(getFrom()) && now.getTo().isBefore(getTo())
               || now.getFrom().equals(getFrom()) || now.getFrom().equals(getTo()) || now.getTo().equals(getFrom()) || now.getTo().equals(getTo());
    }

    public long getOverlappingMinutes(final Timeframe now)
    {

        if (isOverlapping(now))
        {
            if (now.getFrom().isAfter(getFrom()))
            {
                now.getFrom().until(getTo(), ChronoUnit.MINUTES);
            }
        }
        return 0;
    }

    public static Timeframe parseString(final String timeframeString)
    {
        if ("default".equals(timeframeString))
        {
            return new Timeframe(LocalTime.MIN, LocalTime.MAX);
        }
        final LocalTime timeframeStart = LocalTime.parse(timeframeString.split(ParserHelper.timeframeSplitter)[0],
                                                         ParserHelper.timeFormat);
        final LocalTime timeframeEnd = LocalTime.parse(timeframeString.split(ParserHelper.timeframeSplitter)[1],
                                                       ParserHelper.timeFormat);
        return new Timeframe(timeframeStart, timeframeEnd);
    }

    @Override
    public int compareTo(final Timeframe o)
    {
        return getFrom().compareTo(o.getFrom());
    }

    @Override
    public String toString()
    {
        return "Timeframe [from=" + from + ", to=" + to + "]";
    }
}
