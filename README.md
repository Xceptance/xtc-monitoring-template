# XTC Monitoring Template
A base monitoring demo and template for [XTC-based synthetic monitoring](https://www.xceptance.com/xtc/#xtc-monitoring). 

This repository should be used as a starting point for a website or API-monitoring. It demonstrates four base scenarios:

- Ping - A simple server ping to check the server health
- Homepage - A real browser based Homepage visit, allowing to check if the homepage is stable and fast with all connected 3rd parties
- Search - a bit more complex scenario demonstrating the use of the search
- ServerCertificateCheck - A check if the server certificate is valid, if someone tempered with it and if it will expire in the near future
- OrderMonitoring - A REST based check if the number of orders is in the expected range. This is directly usable for Salesforce Commerce Cloud customers.

## Usage

For a more detailed introduction to monitoring with XTC as well as a detailed setup guide, please refer to the [official documentation](https://docs.xceptance.com/xtc/monitoring/400-about-monitoring/).

### Basic Setup

Properties are stored in the `project.properties` file.

1. Adjust location mapping to your needs:

```
xlt.site.europe-west3 = <site1>
xlt.site.europe-west2 = <site2>
xlt.site.us-west3 = <site3>
xlt.site.us-west1 = <site4>
```
2. Replace the start url for each planned site (`xlt.startUrl.<site>`) in project.properties with the url you want to monitor
3. If the site is protected with basic authentication, paste the credentials to the following properties:

```
com.xceptance.xlt.auth.userName
com.xceptance.xlt.auth.password 
```

4. Set the value `xlt.location=` to the location you want to monitor (e.g. europe-west3) in the `config/dev.properties` file.
5. Run `company.scenario.special.Ping` as a unit test from your IDE to see if everything is running fine.

### Browsing Scenarios

1. Adjust existing page-objects to match the structure of the target site. Feel free to change scenario flow, if the site requires it. The page-objects can be found in the package `company.pages`.
2. Adjust search terms via `xlt.de.searchTerms` property
3. Use existing scenarios as reference and extend the project with further scenarios, e.g. for guest and registered checkout (avoid placing orders and creating accounts on production instances, if it's not explicitly required)


### Server Certificate Check

Enter host, port and fingerprint of the site certificate in the following properties:

```
xlt.certificate.host
xlt.certificate.port
xlt.certificate.fingerprint
```

Tip: if you don't know the fingerprint for your site, just run the `ServerCertificateCheck` check once and you will see the current fingerprint in the error message


### Order Monitoring

See also the extended documentation for [order monitoring](OrderMonitoringPropertiesDocumentation.md).

In the `config/ocapi.properties` fill in the following properties:

```
host
clientId
clientPassword
orginUrl
```

* Origin URL is the URL from which the OCAPI calls are expected (otherwise, they will be rejected). Usually it matches the pattern `https://<host>/`
* For clientId and clientPassword ask your SFCC Admin to generate ones for you. It's required for the credentials to have at least the following permission:

```json
{
  "resource_id": "/order_search",
  "methods": [
    "post"
  ],
  "read_attributes": "(count, start, total, next.(**), hits.(data.(order_no, status,creation_date,confirmation_status,export_status,customer_info.(email),billing_address.(country_code),payment_instruments.(c_adyenPaymentMethod,payment_method_id,c_paymentType))))",
  "write_attributes": "(**)"
}
```

Adjust the order monitoring scenario metrics in `config/order-schedule.properties`

The template already has most common order monitoring scenarios pre-configured. Feel free to remove ones you don't need for the project. For the scenarios you need, update the following properties:
  1. `consideredPeriod` - time in minutes within which the condition is expected to be met
  2. `site` - Business Manager site for which the condition should be checked
  3. `locale` - country code of the billing address of the orders (used to distinguish among different locales on the same BM site)
  4. `timezone` - time zone used in BM for the site *Important: although OCAPI works with UTC, the orders in BM have time zone location, therefore order monitoring schedules expect timetable to be localized by the BM time zone*
  5. `maximalOrderPercentageWithFeature`, `maximalOrderAmount`, `minimalOrderAmount` properties should be adjusted according to your expectations based on business statistics 

## Scenarios

### Availability - `Ping`

This scenario uses HTMLUnit to send a request to the homepage, verifies that the response has status code 200 and measures the time it took for the server to deliver the response.

After warm up, runs a simple interaction that is not using a full browser to avoid that piece of the stack. This scenario will only pull the initial HTML from the server, to see is the server is healthy and responding as fast as expected.

### Browsing - `Homepage`, `Search`

These tests act like real users, opening pages in a browser and interacting with them, measuring the performance in background. To keep the code for these scenarios structured, it's recommended to use the page object pattern, creating classes for visited pages in `pages` package. The page-object classes should store encapsulated selectors for the page element and public method to interact with the page. Don't hesitate to use inheritance to reuse code mutual for multiple page-objects. Implementing `validate` method and calling it every time opening the page will help to ensure the monitoring lands on the correct page, that every expected part of the page is displayed and prevents unexpected actions to be done during monitoring.

#### Test Data

A lot of scenarios requires test data. Usually test data differs depending on the location, so to have the test data always localized, use `TestdataHelper.getLocalizedTestdata()` a method to get it by the key (see example in `Search` scenario).

Sometimes it's also useful to have fallbacks for some test data, like, e.g. for search terms or SKUs, to make monitoring more robust to data changes on the site. In this template, you can see an example of fallback implementation in the `Seach` scenario. Feel free to reuse the concept in other scenarios if needed.

### TLS-Check - `ServerCertificateCheck`

Scenario for validating SSL/TLS server certificates. This class performs several certificate validation checks, including:

 * Certificate retrieval from a specified host and port
 * Current validity verification
 * Certificate fingerprint validation
 * Future expiration date checking
 
### Order Monitoring - `OrderMonitoring`

Order monitoring allows verifying percentage and/or number of orders with a specific status(es) and or specific payment method(es) on a site per specific period within a defined range. 
You can combine conditions into a single monitoring execution run.

*Note*: This can only be used in this form by Saleforce Commerce Cloud customers. If you need something similar for a different type of system please [contact us](mailto:contact@xceptance.com).

*Important*: The following section provides examples of possible order monitoring configurations. You have the flexibility to create more complex and tailored configurations as needed.

#### Failed Order Rate 

*Goal*
Verifies that the order failure rate is not deviation too much from the known numbers.

*Example*
No more than 30% of orders made within the last hour have status failed. To filter out single user trying to place an order multiple times and producing a lot of failed orders, it's recommended to set `pathToUniqueAttribute` property to `$..data.customer_info.email;$..data.status`. This will make order monitoring filter out duplicates and count multiple failed orders from single user as one order.

If number of orders is too low, even one failed order can cause failed orders rate to be higher than expected. At the same time, it's hard to say, if the failed order is caused by a problem on the site. In this case, `maxTotalOrderNumberToIgnoreConditon` property can be useful. It allows skipping the assertion if number of order during the defined period is lower than needed to make any conclusion.

*Condition*
`maximum_failed_orders_per_hour`

#### Orders and Payment

*Goal*
Verifies that payment systems are avaialble and used.

*Example*
At least one order was paid with PayPal within last 2 hours (ensures payment systems are available of user)

*Conditions*
`minimal_order_number_with_cc`,`minimal_order_number_with_paypal`,`minimal_order_number_with_klarna`,`minimal_order_number_with_apple_pay`

#### Order Count

*Goal*
Checks that the usual order numbers are coming in.

*Example*
There are from 3 to 3000 newly placed orders (with status new or open) within the previous hour

*Condition*
`minimal_order_number`

#### Order Export Status

*Goal*
Ensure that the order export is working.

*Example*
Orders with invalid e-mails are not accepted by the system

*Condition*
`no_orders_with_invalid_email`


