package com.xceptance.ordermonitoring.model.query.filter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xceptance.ordermonitoring.model.configuration.ParserHelper;

public class DateTimeFilter implements Filter
{
    protected final String field = "creation_date";

    protected final String from;

    protected final String to;

    public DateTimeFilter(final ZonedDateTime from, final ZonedDateTime to)
    {
        this.from = from.format(ParserHelper.dateTimeFormat);
        this.to = to.format(ParserHelper.dateTimeFormat);
    }

    public DateTimeFilter(final ZonedDateTime from)
    {
        this(from, ZonedDateTime.now().plusDays(1));
    }

    public DateTimeFilter()
    {
        this(ZonedDateTime.now().minusYears(1), ZonedDateTime.now().plusDays(1));
    }

    @Override
    public JsonObject convertToJsonFilter()
    {
        final JsonObject filter = new JsonObject();
        filter.add("range_filter", JsonParser.parseString(new Gson().toJson(this)).getAsJsonObject());
        return filter;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DateTimeFilter)
        {
            DateTimeFilter dateTimeFilter = (DateTimeFilter) obj;
            return this.field.equals(dateTimeFilter.field) && this.to.equals(dateTimeFilter.to) && this.from.equals(dateTimeFilter.from);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return from + " - " + to;
    }

    public String getZonedString(String zone)
    {
        return LocalDateTime.parse(from, ParserHelper.dateTimeFormat).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(zone))
                            .format(ParserHelper.humanDateTimeFormat)
               + " - "
               + LocalDateTime.parse(to, ParserHelper.dateTimeFormat).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of(zone))
                              .format(ParserHelper.humanDateTimeFormat);
    }
}
