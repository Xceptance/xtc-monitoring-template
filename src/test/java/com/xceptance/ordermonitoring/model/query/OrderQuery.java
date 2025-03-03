package com.xceptance.ordermonitoring.model.query;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xceptance.ordermonitoring.model.query.filter.DateTimeFilter;
import com.xceptance.ordermonitoring.model.query.filter.Filter;

public class OrderQuery
{
    private final String site;

    private final int start;

    private final int count;

    private final List<Filter> filters;

    private final List<SortingRule> sortingRules;

    public OrderQuery(OrderQueryBuilder builder)
    {
        this.site = builder.site;
        this.start = builder.start;
        this.count = builder.count;
        this.filters = builder.filters;
        this.sortingRules = builder.sortingRules;
        if (this.filters.isEmpty())
        {
            this.filters.add(new DateTimeFilter());
        }
        if (this.sortingRules.isEmpty())
        {
            this.sortingRules.add(new SortingRule());
        }
    }

    public String getSite()
    {
        return site;
    }

    public JsonObject getJsonQuery()
    {
        final JsonArray filtersJson = new JsonArray();
        final JsonArray sortingRulesJson = new JsonArray();

        for (final Filter filter : filters)
        {
            filtersJson.add(filter.convertToJsonFilter());
        }
        for (final SortingRule sortingRule : sortingRules)
        {
            sortingRulesJson.add(sortingRule.convertToJson());
        }

        final JsonObject boolFilter = new JsonObject();
        boolFilter.addProperty("operator", "and");
        boolFilter.add("filters", filtersJson);

        final JsonObject filter = new JsonObject();
        filter.add("bool_filter", boolFilter);

        final JsonObject matchAllQueryObject = JsonParser.parseString("{ \"match_all_query\": {} }").getAsJsonObject();
        final JsonObject filteredQuery = new JsonObject();
        filteredQuery.add("query", matchAllQueryObject);
        filteredQuery.add("filter", filter);

        final JsonObject query = new JsonObject();
        query.add("filtered_query", filteredQuery);

        final JsonObject fullQuery = new JsonObject();
        fullQuery.add("query", query);
        fullQuery.addProperty("select", "(**)");
        fullQuery.add("sorts", sortingRulesJson);
        fullQuery.addProperty("start", start);
        fullQuery.addProperty("count", count);
        return fullQuery;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof OrderQuery)
        {
            OrderQuery orderQuery = (OrderQuery) obj;
            return this.site.equals(orderQuery.site) && this.start == orderQuery.start
                   && this.count == orderQuery.count
                   && this.filters.size() == orderQuery.filters.size()
                   && this.filters.containsAll(orderQuery.filters)
                   && this.sortingRules.size() == orderQuery.sortingRules.size()
                   && this.sortingRules.containsAll(orderQuery.sortingRules);
        }
        return false;
    }

    public static class OrderQueryBuilder
    {
        private String site;

        private int start = 0;

        private int count = 200;

        private final List<Filter> filters = new ArrayList<Filter>();

        private final List<SortingRule> sortingRules = new ArrayList<SortingRule>();

        public OrderQueryBuilder(String site)
        {
            this.site = site;
        }

        public OrderQueryBuilder addFilter(final Filter filter)
        {
            filters.add(filter);
            return this;
        }

        public OrderQueryBuilder addSortingRule(final SortingRule sortingRule)
        {
            sortingRules.add(sortingRule);
            return this;
        }

        public OrderQueryBuilder setStart(int start)
        {
            this.start = start;
            return this;
        }

        public OrderQueryBuilder setCount(int count)
        {
            this.count = count;
            return this;
        }

        public OrderQuery build()
        {
            return new OrderQuery(this);
        }
    }
}
