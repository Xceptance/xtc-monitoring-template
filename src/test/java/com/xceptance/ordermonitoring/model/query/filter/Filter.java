package com.xceptance.ordermonitoring.model.query.filter;

import com.google.gson.JsonObject;

public interface Filter
{
    public abstract JsonObject convertToJsonFilter();
}
