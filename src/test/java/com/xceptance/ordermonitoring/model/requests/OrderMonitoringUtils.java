package com.xceptance.ordermonitoring.model.requests;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xceptance.ordermonitoring.model.query.OrderQuery;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderMonitoringUtils
{
    private static final Map<Thread, Map<OrderQuery, JsonObject>> REQUESTS_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private static Map<OrderQuery, JsonObject> getRequestsCache()
    {
        return REQUESTS_CACHE.computeIfAbsent(Thread.currentThread(), key -> {
            return new HashMap<OrderQuery, JsonObject>();
        });
    }

    public static JsonObject getOrders(final OrderQuery orderQuery)
    {
        Optional<OrderQuery> cachedKey = getRequestsCache().keySet().stream().filter(key -> key.equals(orderQuery)).findFirst();
        if (cachedKey.isPresent())
        {
            System.out.println("Return from cache");
            System.out.println(orderQuery.getJsonQuery());
            JsonObject responseJson = getRequestsCache().get(cachedKey.get());
            System.out.println(responseJson);
            return responseJson;
        }
        final OkHttpClient client = new OkHttpClient();
        final String accessToken = OcapiTokenFlow.getAccessToken(client);
        final RequestBody body = RequestBody.create(MediaType.get("application/json"),
                                                    orderQuery.getJsonQuery().toString());
        System.out.println(orderQuery.getSite());
        System.out.println(orderQuery.getJsonQuery());
        final Request request = new Request.Builder()
                                                     .url(HttpUrl.parse(OcapiTokenFlow.getShopUrlForSite(orderQuery.getSite()) + "/order_search")).post(body)
                                                     .addHeader("Accept", "application/json").addHeader("Authorization", "Bearer " + accessToken)
                                                     .addHeader("Origin", OcapiTokenFlow.getConfiguration().orginUrl())
                                                     .addHeader("Content-Type", "application/json").build();

        try
        {
            Response response = client.newCall(request).execute();
            final String responseBody = response.body().string();
            final JsonElement responseJson = JsonParser.parseString(responseBody);
            if (!responseJson.isJsonObject())
            {
                throw new RuntimeException("Unexpected response:\n" + responseJson);
            }
            System.out.println(responseJson);
            getRequestsCache().put(orderQuery, responseJson.getAsJsonObject());
            return responseJson.getAsJsonObject();

        }
        catch (final IOException e)
        {
            throw new RuntimeException("Could not process request for order query " + orderQuery, e);
        }
    }

    // not needed as timezone is read from props
    public static int getTimezoneOffsetInSecondsForSite(final String site)
    {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                                                     .url(HttpUrl.parse(OcapiTokenFlow.getShopUrlForSite(site) + "/site")).get()
                                                     .addHeader("x-dw-client-id", OcapiTokenFlow.getConfiguration().clientId()).build();

        try (Response response = client.newCall(request).execute())
        {
            final String responseBody = response.body().string();
            System.out.println(responseBody);
            final JsonElement responseJson = JsonParser.parseString(responseBody);
            if (!responseJson.isJsonObject() && responseJson.getAsJsonObject().get("timezone_offset") != null)
            {
                throw new RuntimeException("Unexpected response:\n" + responseJson);
            }
            final long offsetInMilis = responseJson.getAsJsonObject().get("timezone_offset").getAsLong();
            return Long.valueOf(offsetInMilis / 1000).intValue();

        }
        catch (final IOException e)
        {
            throw new RuntimeException("Could not process request " + request, e);
        }
    }
}
