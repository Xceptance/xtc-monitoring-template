package com.xceptance.ordermonitoring.model.query.filter;

import java.util.List;
import java.util.stream.Collectors;

public class OrderStatusFilter extends TermFilter
{
    public OrderStatusFilter(final Operator operator, final List<OrderStatus> values)
    {
        super("status", operator, values.stream().map(status -> status.toString()).collect(Collectors.toList()));
    }

    public enum OrderStatus
    {
        CREATED, NEW, OPEN, COMPLETED, CANCELED, REPLACED, FAILED;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }
}
