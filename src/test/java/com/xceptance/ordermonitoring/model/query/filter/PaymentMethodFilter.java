package com.xceptance.ordermonitoring.model.query.filter;

import java.util.List;

public class PaymentMethodFilter extends TermFilter
{

    public PaymentMethodFilter(final Operator operator, final List<String> values)
    {
        super("payment_instruments.payment_method_id", operator, values);
    }

}
