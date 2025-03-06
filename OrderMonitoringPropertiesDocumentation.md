# Order Monitoring Properties Documentation

## Site

`<condition_label>.site`

Order monitoring is site-specific. It makes sense because most of the sites have different payment methods and might also have different order statistics (e.g. visitors of your German site tend to order more than visitors of your Polish site)

Please, keep in mind that all the statistics will be validated for the specified site only and based on site-specific time

## Locale

`<condition_label>.locale`

Country code of the billing address of the orders (used to distinguish among different locales on the same BM site)

## Time zone

`<condition_label>.timezone`

Time zone used in BM for the site

**Important: although OCAPI works with UTC, the orders in BM have time zone location, therefore order monitoring schedules expect timetable to be localized by the BM time zone**

## Order Status Property

`<condition_label>.order statuses`

Multiple values are supported. It depends on the implementation of the order processing, but these are the main default order statuses available (there can be more if more are implemented)

`created` - order was created in the system but is not yet placed (e.g. because it has not been paid yet)
`new` - order is placed successfully and shipping and invoice numbers are generated
`open` - the same as new but after being viewed in BM 
`failed` - order is rejected or payment fails
`completed` - order is fully paid, exported, and shipped
`cancelled` - order is canceled, for example, when the shopper requests it

## Payment Method Property

`<condition_label>.paymentMethod`

Multiple values are supported. Checks for the values in `payment_method_id` and `c_adyenPaymentMethod`, considers also `c_paymentType` combining it with previously mentioned properties by the following pattern `<payment_method>_<c_paymentType>`

These attributes are the most common to store payment information, in case though, information about payment method of you wish is stored in other attribute of OCAPI JSON response, use [custom assertion](#custom-assertion) to handle the case.

Here are the most common payment methods and common values for them. Depending on site implementation, the values may differ. Therefore, please, check if monitoring gathers the expected orders.

### Credit card

* `Visa`
* `Mastercard`
* `Amex`
* `Discover`
* `JCB`
* `Diners`
* `Visa Dankort`
* `Maestro`
* `Bancontact`

### PayPal

* `paypal`
* `paypal_express` (for the one with Ayden as payment processor)
* `PAYPAL_EXPRESS` (for the SFCC default payment processor)

### Klarna

* `klarna` - for payment with invoice
* `klarna_account` - for payment in parts
* `klarna_paynow` - for payment with direct payment via bank

### Apple Pay
 
* `Apple Pay`
* `Apple Pay_express`
* `Credit Card` (or credit card in local language)

### Google Pay

* `Google Pay`
* `Google Pay_express`
* `googlepay`
* `googlepay_express`

### Gift Cards

* `GIFT_CERTIFICATE`

### InStore Payment

* `InStorePayment`

### Paycoinq

* `Payconiq by Bancontact`

### Ideal

* `ideal`

### Bancontact

* `bcmc`

### Iyzico

* `Iyzico`

### No Payment (for zero value products)

* `zero_order_total`

### Cash on delivery

* `CASH_ON_DELIVERY`

## Considered Period

`<condition_label>.consideredPeriod` - time in minutes within which the expected range of number/ percentage of orders with expected status and/or payment method should be kept.

The considered period doesn't have to match the execution interval of the check. Moreover, it's recommended that it be bigger than the execution interval. This allows us to smooth out the peaks and verify the statistics for the last e.g. 3 hours every hour, getting information about the last hour but also averaging it with the statistics for the previous 2 hours. This approach gives more flexibility in checks, as we don't know the exact hour of order peak for every day. Besides that, it enables monitoring to catch the trend of order numbers getting up or down, instead of single measurements for specific points in time

## Custom assertion

In case, you want to monitor an order attribute that is not covered by [payment method property](#payment-method-property), use the following:

```
<condition_label>.customCondition.<custom_assertion_name>.property = <path to desired attribute in OCAPI JSON>
<condition_label>.customCondition.<custom_assertion_name>.pattern = <regex for the attribute value to match>
```

### Example:

```
no_orders_with_invalid_email.customCondition.validEmailCondition.property = $.customer_info.email
no_orders_with_invalid_email.customCondition.validEmailCondition.pattern = ^(?![a-zA-Z0-9_.\\-\\+]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-]+).*$
```

## Filtering out duplicates

`<condition_label>.pathToUniqueAttribute`

Sometimes it's required to consider multiple orders as one based on the matching OCAPI JSON property values. For example, it might be useful to filter out multiple failed orders made by the same user to suppress notifications, as the issue is most probably on the user side. 

### Example:

`maximum_failed_orders_per_hour.pathToUniqueAttribute = $..data.customer_info.email;$..data.status`


## Muting alerts on too few data

`<condition_label>.maxTotalOrderNumberToIgnoreConditon`

Sometimes, if there are too few orders, it's hard to make a valid statement. For example, if there are only 2 orders and one of them failed, it gives you high failure rate, but it's actually hard to say, if there is any problem. In these case, you can make monitoring skip the assertion if order number is not reached.

### Example:

`maximum_failed_orders_per_hour.maxTotalOrderNumberToIgnoreConditon = 2`

## Default Expected Metrics Properties

This configuration is a good point to start to always have a fallback for expected metrics schedule gabs or to define a minimum/maximum that should not be overcome at any point of time

### Examples:

 order failure percentage should never be more than n% per t minutes, otherwise, we might have an issue with the order process
 it's never expected to have more than m new orders within t minutes, otherwise we might have a bot attack
Properties

```
<condition_label>.everyday.default.minimalOrderAmount # minimal number of orders with previously specified status and/or payment method received within the previously specified timeframe
<condition_label>.everyday.default.maximalOrderAmount  # maximal number of orders with previously specified status and/or payment method received within the previously specified timeframe
<condition_label>.everyday.default.minimalOrderPercentageWithFeature # minimal percentage of orders with previously specified status and/or payment method from all orders registered during this period 
<condition_label>.everyday.default.maximalOrderPercentageWithFeature # maximal percentage of orders with previously specified status and/or payment method from all orders registered during this period 
```
## Schedules of expected metrics

It's natural that, depending on the business model, there might be more orders placed during certain hours and/or days than during others. To verify that peaks of user activity match your expectations, you can create and overwrite schedules.

### Everyday schedules

It's logical to start with a schedule for every day and then complement it with details for the day of the week or exclusion period. You can create a schedule using the following property:

`<condition_label>.everyday.<time frame>.<metric property: minimalOrderAmount, maximalOrderAmount, minimalOrderPercentageWithFeature, maximalOrderPercentageWithFeature>`

### Example:

```
minimal_order_number_in_180_minutes.everyday.00-00-00_6-00-00.minimalOrderAmount = 0   # not required as there is a matching default value defined but adding this property doesn't break anything

minimal_order_number_in_180_minutes.everyday.06-00-00_13-00-00.minimalOrderAmount = 50

minimal_order_number_in_180_minutes.everyday.13-00-00_18-00-00.minimalOrderAmount = 150

minimal_order_number_in_180_minutes.everyday.18-00-00_20-30-00.minimalOrderAmount = 200

minimal_order_number_in_180_minutes.everyday.20-30-00_22-00-00.minimalOrderAmount = 100

minimal_order_number_in_180_minutes.everyday.22-00-00_23-59-59.minimalOrderAmount = 50
```

## Day of the week schedules
As mentioned before, it may be required to specify special values for different days of the week, e.g. it's expected that on Sunday users place less orders than on other days of the week. To adjust the monitored values to this, you can use the following property:

`<condition_label>.<day of the week>.<time frame>.<metric property: minimalOrderAmount, maximalOrderAmount, minimalOrderPercentageWithFeature, maximalOrderPercentageWithFeature>`

### Example:


```
minimal_order_number_in_180_minutes.sunday.00-00-00_6-00-00.minimalOrderAmount = 0  # not required as there is  a matching default value defined but adding this property doesn't break anything

minimal_order_number_in_180_minutes.sunday.06-00-00_13-00-00.minimalOrderAmount = 50   # not required as there is a matching everyday value for these hours defined but adding this property doesn't break anything

minimal_order_number_in_180_minutes.sunday.13-00-00_20-00-00.minimalOrderAmount = 60

minimal_order_number_in_180_minutes.sunday.22-00-00_23-59-59.minimalOrderAmount = 0
```

## Exclusive periods

It's expected that order statistics can depend on time period, e.g. on holidays or during sales there are more orders expected to be placed.



To verify that the statistics match your expectations, first define an exclusive period of time with properties

```
<condition_label>.exclusivePeriod.<name of the period>.from 

<condition_label>.exclusivePeriod.<name of the period>.to
```


Then declare different statistic values for the period with 

`<condition_label>.<name of the period>.<time frame>.<metric property: minimalOrderAmount, maximalOrderAmount, minimalOrderPercentageWithFeature, maximalOrderPercentageWithFeature>`

### Example

```
minimal_order_number_in_180_minutes.exclusivePeriod.Christmas_Holidays.from = 2025-12-23

minimal_order_number_in_180_minutes.exclusivePeriod.Christmas_Holidays.from = 2025-12-27



minimal_order_number_in_180_minutes.exclusivePeriod.Christmas_Holidays.13-00-00_15-00-00.minimalOrderAmount = 200

minimal_order_number_in_180_minutes.exclusivePeriod.Christmas_Holidays.15-00-00_19-00-00.minimalOrderAmount = 300

minimal_order_number_in_180_minutes.exclusivePeriod.Christmas_Holidays.19-00-00_22-00-00.minimalOrderAmount = 250

minimal_order_number_in_180_minutes.exclusivePeriod.Christmas_Holidays.22-00-00_23-59-59.minimalOrderAmount = 100

```