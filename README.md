# XTC Monitoring Template
A base monitoring demo and template for XTC based synthetic monitoring.

# Steps to utilize template

## Adjusting properties in project.properties file

1. Adjust location mapping to you needs:

```
xlt.site.europe-west3 = <site1>
xlt.site.europe-west2 = <site2>
xlt.site.us-west3 = <site3>
xlt.site.us-west1 = <site4>
```
2. Replace start (`xlt.startUrl.<site>`) urls in project.properties
3. If site is protected with basic authentication, paste the credentials to the following properties:

```
com.xceptance.xlt.auth.userName
com.xceptance.xlt.auth.password 
```
4. Enter host, port and fingerprint of the site certificate in the following properties:

```
xlt.certificate.host
xlt.certificate.port
xlt.certificate.fingerprint
```

Tip: if you don't know the fingerprint for your site, just run the `TServerCertificate` test once and you will see the current fingerprint in the error message

5. Adjust search terms via `xlt.de.searchTerms` property

## Adjusting credentials for order monitoring in ocapi.properties

1. Fill out the following properties:

```
host
clientId
clientPassword
orginUrl
```

* Origin URL is the URL from which the OCAPI calls are expected (otherwise, they will be rejected). Usually it matches the pattern `https://<host>/`

* For clientId and clientPassword ask your SFCC Admin to generate ones for you. It's required for the credentials to have at least the following permission:

```     
        {
          "resource_id": "/order_search",
          "methods": [
            "post"
          ],
          "read_attributes": "(count, start, total, next.(**), hits.(data.(order_no, status,creation_date,confirmation_status,export_status,customer_info.(email),billing_address.(country_code),payment_instruments.(c_adyenPaymentMethod,payment_method_id,c_paymentType))))",
          "write_attributes": "(**)"
        }
```
## Adjusting order monitoring scenario metrics in order-schedule.properties

The template already has most common order monitoring scenarios pre-configured. Feel free to remove ones you don't need for the project. For the scenarios you need, update the following properties:
1. `consideredPeriod` - time in minutes within which the condition is expected to be met
2. `site` - Business Manager site for which the condition should be checked
3. `locale` - country code of the billing address of the orders (used to distinguish among different locales on the same BM site)
4. `timezone` - time zone used in BM for the site *Important: although OCAPI works with UTC, the orders in BM have time zone location, therefore order monitoring schedules expect time table to be localized by the BM time zone*
5. `maximalOrderPercentageWithFeature`, `maximalOrderAmount`, `minimalOrderAmount` properties should be adjusted according to your expectations based on business statistics 

## Adjusting tests and page objects

Adjust existing page-objects to match structure of target site. Feel free to change test flow, if the site requires it.

Use existing tests as reference and extend the project with test for further scenarios, e.g. for guest and registered checkout


# Project structure

Project contains four basic test types:

## Browser-less test: `TPing`

After warm up runs a simple interaction that is not using a full browser to avoid that piece of the stack. This test will only pull the initial HTML from the server, to see is the server is healthy and responding as fast as expected.

This test uses HTMLUnit to send request to the homepage, verifies that the response has status code 200 and measures the time it took for server to deliver the response.

## Browser tests: `THomepage`, `TSearch`

These tests act like a real user, opening pages in browser and interacting with them, measuring the performance in background. To keep the code for these tests structured, it's recommended to use page object pattern, creating classes for visited pages in `pages` package. The page-object classes should store encapsulated selectors for the page element and public method to interact with the page. Don't hesitate to use inheritance to reuse code mutual for multiple page-objects. Implementing `validate` method and calling it every time opening the page will help to ensure monitoring lands on the correct page, preventing unexpected actions to be done during monitoring.

### Test data

A lot of tests need to be fed with test data. Usually test data differs depending on the location, so to have the test data always localized, use `TestdataHelper.getLocalizedTestdata` method to get it by the key (see example in `TSearch` test).

Sometimes it's also useful to have fallbacks for some test data, like, e.g. for search terms or SKUs, to make monitoring more robust to data changes on the site. In this template you can see an example of fallback implementation in the `TSeach` test. Feel free to reuse the concept in other tests if needed.


## Certificate test: `TServerCertificate`

Test class for validating SSL/TLS server certificates. This class performs several certificate validation checks including:
 * Certificate retrieval from a specified host and port
 * Current validity verification
 * Certificate fingerprint validation
 * Future expiration date checking
 
## Order monitoring test: `OrderMonitoringTest`

Order monitoring allows to verify percentage and/or number of orders with a specific status(es) and or specific payment method(es) on a site per specific period within a defined range.

### Example Cases:

#### Failed orders rate 

Ensures order processing is working fine

Example:
No more than 30% of orders made within the last hour have status failed. To filter out single user trying to place an order multiple times and producing a lot of failed orders, it's recommended to set `pathToUniqueAttribute` property to `$..data.customer_info.email;$..data.status`. This will make order monitoring filter out duplicates and count multiple failed orders from single user as one order.

If number of orders is too low, even one failed order can cause failed orders rate to be higher than expected. At the same time, it's hard to say, if the failed order is caused by a problem on the site. In this case, `maxTotalOrderNumberToIgnoreConditon` property can be useful. It allows to skip the assertion if number of order during the defined period is lower than needed to make any conclusion.

Example condition: `maximum_failed_orders_per_hour`

#### Number/Percentage of orders paid with payment method

Ensures payment systems are available of user

Example: At least one order was paid with PayPal within last 2 hours (ensures payment systems are available of user)

Example conditions: `minimal_order_number_with_cc`,`minimal_order_number_with_paypal`,`minimal_order_number_with_klarna`,`minimal_order_number_with_apple_pay`

#### Number of new orders

Ensures nothing hinders users from placing order according to the expected statistics and the site is not overloaded with unexpectedly high numbers of orders

Example: There are from 3 to 3000 newly placed orders (with status new or open) within the previous hour

Example condition: `minimal_order_number`

#### Valid values transfered to order system

Ensures orders with invalid values are not coming through

Example: Orders with invalid e-mails are not accepted by the system

Example condition: `no_orders_with_invalid_email`

### Condition combination

It's possible to make single test to validate multiple conditions at once to reduce number of calls and to reduce number of alerts in case of depending conditions.

#### Example Situation:

PayPal payment processing is broken

Alerts in case of verification of a single criterion per test:

Alert that percentage of new orders with PayPal is not reached
Alert that expected number of new orders is not reached
Alert that percentage of failed order is more than expected
→ 3 notification giving you a part of information

Alerts in case of verification of multiple criterion per test:

→ single alert informing you that:  percentage of new orders with PayPal and expected number of new orders are not reached and percentage of failed order is more than expected → gives better overview of what could be the reason for not reached expectations


