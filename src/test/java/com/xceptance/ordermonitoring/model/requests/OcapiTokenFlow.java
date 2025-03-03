package com.xceptance.ordermonitoring.model.requests;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Assert;

import com.google.gson.JsonParser;
import com.xceptance.ordermonitoring.OcapiSettings;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OcapiTokenFlow
{
    private static final Map<Thread, OcapiSettings> CONFIGURATION = Collections.synchronizedMap(new WeakHashMap<>());

    public static OcapiSettings getConfiguration()
    {
        return CONFIGURATION.computeIfAbsent(Thread.currentThread(), key ->
            {
                return ConfigFactory.create(OcapiSettings.class);
            });
    }

    public static String getShopUrlForSite(final String site)
    {
        return "https://" + getConfiguration().host() + "/s/" + site + "/dw/shop/" + getConfiguration().apiVersion();
    }

    public static String getAccessToken(final OkHttpClient client)
    {
        final HttpUrl url = HttpUrl.parse("https://account.demandware.com/dw/oauth2/access_token");

        final MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        final String params = "grant_type=client_credentials";
        final RequestBody body = RequestBody.create(mediaType, params);

        final String credentials = getConfiguration().clientId() + ":" + getConfiguration().clientPassword();

        final Request request = new Request.Builder().url(url).post(body).addHeader("Accept", "application/json")
                .addHeader("authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()))
                .addHeader("Content-Type", "application/x-www-form-urlencoded").build();

        try (Response response = client.newCall(request).execute())
        {
            Assert.assertTrue("Could not get access token for client id " + getConfiguration().clientId(),
                    response.isSuccessful());

            return JsonParser.parseString(response.body().string()).getAsJsonObject().get("access_token").getAsString();

        } catch (final IOException e)
        {
            throw new RuntimeException("Could not get access token for client id " + getConfiguration().clientId(), e);
        }
    }

}
