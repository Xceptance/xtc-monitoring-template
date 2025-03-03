package com.xceptance.ordermonitoring;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@LoadPolicy(LoadType.MERGE)
@Sources(
{
  "file:custom.properties", "system:env", "system:properties", "file:config/dev-ocapi.properties", "file:config/ocapi.properties"
})
public interface OcapiSettings extends Mutable
{
    @Key("host")
    public String host();

    @Key("clientId")
    public String clientId();

    @Key("clientPassword")
    public String clientPassword();

    @Key("apiVersion")
    public String apiVersion();

    @Key("siteId")
    public String siteId();

    @Key("orginUrl")
    public String orginUrl();
}
