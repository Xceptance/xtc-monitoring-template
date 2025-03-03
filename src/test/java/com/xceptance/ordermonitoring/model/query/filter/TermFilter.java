package com.xceptance.ordermonitoring.model.query.filter;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class TermFilter implements Filter
{
    protected String field;

    protected String operator;

    protected List<String> values;

    public TermFilter(final String field, final Operator operator, final List<String> values)
    {
        this.field = field;
        this.operator = operator.toString();
        this.values = values;
    }

    public enum Operator
    {
        IS, ONE_OF, IS_NULL, IS_NOT_NULL, LESS, GREATER, NOT_IN, REQ;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }

    @Override
    public JsonObject convertToJsonFilter()
    {
        final JsonObject filter = new JsonObject();
        filter.add("term_filter", JsonParser.parseString(new Gson().toJson(this)).getAsJsonObject());
        return filter;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof TermFilter)
        {
            TermFilter termFilter = (TermFilter) obj;
            return this.field.equals(termFilter.field) && this.operator.equals(termFilter.operator) && this.values.size() == termFilter.values.size()
                   && this.values.containsAll(termFilter.values);
        }
        return false;
    }
}
