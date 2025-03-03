package com.xceptance.ordermonitoring.model.configuration;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.Predicate;

public class LocalePredicate implements Predicate
{
    private String locale;

    public LocalePredicate(String locale)
    {
        this.locale = locale;
    }

    @Override
    public boolean apply(PredicateContext ctx)
    {
        JsonObject paymentJson = JsonParser.parseString((ctx.item(Map.class).get("billing_address").toString())).getAsJsonObject();
        return locale.toLowerCase().trim().equals(paymentJson.get("country_code").getAsString().toLowerCase());
    }
}
