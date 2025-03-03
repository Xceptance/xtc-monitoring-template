package com.xceptance.ordermonitoring.model.configuration;

import java.time.format.DateTimeFormatter;

public class ParserHelper
{
    public static final DateTimeFormatter humanDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String siteKey = "site";

    public static final String localeKey = "locale";

    public static final String consideredPeriodKey = "consideredPeriod";

    public static final String orderStatuses = "orderStatuses";

    public static final String paymentMethods = "paymentMethods";
    
    public static final String pathToUniqueAttributeKey = "pathToUniqueAttribute";
    
    public static final String pathToUniqueAttributeKeyArraySeparator = ";";
    
    public static final String conditionsSeparator = ",";

    public static final String maxTotalOrderNumberToIgnoreConditonKey = "maxTotalOrderNumberToIgnoreConditon";

    public static final String timezoneKey = "timezone";

    public static final String conditionKey = "condition";

    public static final String exclusivePeriodKey = "exclusivePeriod";

    public static final String customConditionKey = "customCondition";

    public static final String everydayKey = "everyday";

    public static final String exclusivePeriodFromKey = "from";

    public static final String adyenPaymentMethodKey = "c_adyenPaymentMethod";

    public static final String adyenPaymentMethodTypeKey = "c_paymentType";

    public static final String sandboxPaymentMethodSearch = "payment_method_id";

    public static final String exclusivePeriodToKey = "to";

    public static final String timeframeSplitter = "_";

    public static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH-mm-ss");
}
