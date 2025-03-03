package com.xceptance.ordermonitoring.model.configuration;

import java.util.Map;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;

public class CustomPredicate implements Predicate
{
    private Map<String, Map<String, String>> customConditions;

    public CustomPredicate(Map<String, Map<String, String>> customConditions)
    {
        this.customConditions = customConditions;
    }

    @Override
    public boolean apply(PredicateContext ctx)
    {
        boolean matches = true;
        Gson gson = new Gson();
        for (String key : customConditions.keySet())
        {
            matches = JsonPath.parse(gson.toJson(ctx.item(Map.class)).toString()).read(customConditions.get(key).get("property")).toString()
                              .matches(customConditions.get(key).get("pattern"));
        }
        return matches;
    }

}
