package com.xceptance.ordermonitoring.model.query;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SortingRule
{
    protected String field;

    protected String sort_order;

    public SortingRule(final String field, final SortingOrder sortingOrder)
    {
        this.field = field;
        sort_order = sortingOrder.toString();
    }

    public SortingRule()
    {
        field = "creation_date";
        sort_order = SortingOrder.DESC.toString();
    }

    public enum SortingOrder
    {
        ASC, DESC;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }

    public JsonObject convertToJson()
    {
        return JsonParser.parseString(new Gson().toJson(this)).getAsJsonObject();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof SortingRule)
        {
            SortingRule sortingRule = (SortingRule) obj;
            return this.field.equals(sortingRule.field) && this.sort_order.equals(sortingRule.sort_order);
        }
        return false;
    }
}
