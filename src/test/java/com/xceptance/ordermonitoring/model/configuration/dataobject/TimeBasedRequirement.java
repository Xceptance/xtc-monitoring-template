package com.xceptance.ordermonitoring.model.configuration.dataobject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.ordermonitoring.model.query.filter.OrderStatusFilter.OrderStatus;

public class TimeBasedRequirement implements Comparable<TimeBasedRequirement>
{
    private final Timeframe timeframe;

    private final Requirement requirement;

    private final long consideredPeriod;

    private final String site;

    private final String targetLocale;

    private final String orderStatuses;

    private final String paymentMethods;

    private final Map<String, Map<String, String>> customConditions;

    private final List<String> pathsToUniqueAttributes;
    
    private final int maxTotalOrderNumberToIgnoreConditon;

    private final String timeZone;

	public TimeBasedRequirement(TimeBasedRequirementBuilder builder)
    {
        this.timeframe = builder.timeframe;
        this.requirement = builder.requirement;
        this.consideredPeriod = builder.consideredPeriod;
        this.orderStatuses = builder.orderStatuses;
        this.paymentMethods = builder.paymentMethods;
        this.customConditions = builder.customConditions;
        this.site = builder.site;
        this.pathsToUniqueAttributes = builder.pathsToUniqueAttributes;
        this.maxTotalOrderNumberToIgnoreConditon = builder.maxTotalOrderNumberToIgnoreConditon;
        this.targetLocale = builder.targetLocale;
        this.timeZone = builder.timeZone;
    }

    public Timeframe getTimeframe()
    {
        return timeframe;
    }

    public Requirement getRequirement()
    {
        return requirement;
    }

    public String getSite()
    {
        return site;
    }

    public String getLocale()
    {
        return targetLocale;
    }

    public boolean isActiveNow(final LocalTime now)
    {
        return getTimeframe().isActiveNow(now);
    }

    public boolean isOverlapping(final Timeframe now)
    {
        return getTimeframe().isOverlapping(now);
    }

    @Override
    public int compareTo(final TimeBasedRequirement o)
    {
        return getTimeframe().compareTo(o.getTimeframe());
    }

    public TimeBasedRequirement merge(final TimeBasedRequirement otherRequirement)
    {

        final Requirement copy = requirement.copy();

        copy.setMinimalOrderAmountIfAbsent(otherRequirement.getRequirement().getMinimalOrderAmount());
        copy.setMaximalOrderAmountIfAbsent(otherRequirement.getRequirement().getMaximalOrderAmount());
        copy.setMinimalOrderPercentageWithFeatureIfAbsent(
                                                          otherRequirement.getRequirement().getMinimalOrderPercentageWithFeature());
        copy.setMaximalOrderPercentageWithFeatureIfAbsent(
                                                          otherRequirement.getRequirement().getMaximalOrderPercentageWithFeature());
        TimeBasedRequirementBuilder builder= new TimeBasedRequirementBuilder();
        
        return builder
        		.setTimeframe(timeframe)
        		.setRequirement(copy)
        		.setConsideredPeriod(consideredPeriod)
        		.setOrderStatuses(orderStatuses)
        		.setPaymentMethods(paymentMethods)
        		.setCustomConditions(customConditions)
        		.setPathToUniqueAttribute(pathsToUniqueAttributes)
        		.setMaxTotalOrderNumberToIgnoreConditon(maxTotalOrderNumberToIgnoreConditon)
        		.setSite(site)
        		.setTargetLocale(targetLocale)
        		.setTimeZone(timeZone)
        		.build();
    }

    public long getConsideredPeriod()
    {
        return consideredPeriod;
    }

    public String getOrderStatuses()
    {
        return orderStatuses;
    }

    public List<OrderStatus> getOrderStatusesAsList()
    {
        if (StringUtils.isNotBlank(getOrderStatuses()))
        {
            return List.of(getOrderStatuses().split(",")).stream()
                       .map(orderStatus -> OrderStatus.valueOf(orderStatus.trim().toUpperCase())).toList();
        }
        return List.of(OrderStatus.NEW, OrderStatus.CREATED);
    }

    public String getPaymentMethods()
    {
        return paymentMethods;
    }

    public List<String> getPaymentMethodsAsList()
    {
        if (StringUtils.isNotBlank(getPaymentMethods()))
        {
            return List.of(getPaymentMethods().split(",")).stream().map(payment -> payment.trim())
                       .collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }

    public String getTimeZone()
    {
        return timeZone;
    }

    public Map<String, Map<String, String>> getCustomConditions()
    {
        return customConditions;
    }

    public List<String> getPathsToUniqueAttributes()
    {
        return pathsToUniqueAttributes;
    }

    public int getMaxTotalOrderNumberToIgnoreConditon() {
		return maxTotalOrderNumberToIgnoreConditon;
	}

	@Override
	public String toString() {
		return "TimeBasedRequirement [timeframe=" + timeframe + ", requirement=" + requirement + ", consideredPeriod="
				+ consideredPeriod + ", site=" + site + ", targetLocale=" + targetLocale + ", orderStatuses="
				+ orderStatuses + ", paymentMethods=" + paymentMethods + ", customConditions=" + customConditions
				+ ", pathsToUniqueAttributes=" + pathsToUniqueAttributes + ", maxTotalOrderNumberToIgnoreConditon="
				+ maxTotalOrderNumberToIgnoreConditon + ", timeZone=" + timeZone + "]";
	}
    
    public static class TimeBasedRequirementBuilder
    {
        private Timeframe timeframe;

        private Requirement requirement;

        private long consideredPeriod;

        private String site;

        private String targetLocale;

        private String orderStatuses;

        private String paymentMethods;

        private List<String> pathsToUniqueAttributes;
        
        private int maxTotalOrderNumberToIgnoreConditon;

        private Map<String, Map<String, String>> customConditions;

        private String timeZone;

        public TimeBasedRequirementBuilder setTimeframe(Timeframe timeframe)
        {
            this.timeframe = timeframe;
            return this;
        }

        public TimeBasedRequirementBuilder setRequirement(Requirement requirement)
        {
            this.requirement = requirement;
            return this;
        }

        public TimeBasedRequirementBuilder setConsideredPeriod(long consideredPeriod)
        {
            this.consideredPeriod = consideredPeriod;
            return this;
        }

        public TimeBasedRequirementBuilder setSite(String site)
        {
            this.site = site;
            return this;
        }

        public TimeBasedRequirementBuilder setTargetLocale(String targetLocale)
        {
            this.targetLocale = targetLocale;
            return this;
        }

        public TimeBasedRequirementBuilder setOrderStatuses(String orderStatuses)
        {
            this.orderStatuses = orderStatuses;
            return this;
        }

        public TimeBasedRequirementBuilder setPaymentMethods(String paymentMethods)
        {
            this.paymentMethods = paymentMethods;
            return this;
        }

        public TimeBasedRequirementBuilder setTimeZone(String timeZone)
        {
            this.timeZone = timeZone;
            return this;
        }

        public TimeBasedRequirementBuilder setCustomConditions(Map<String, Map<String, String>> customConditions)
        {
            this.customConditions = customConditions;
            return this;
        }

        public TimeBasedRequirementBuilder setPathToUniqueAttribute(List<String> pathsToUniqueAttributes)
        {
            this.pathsToUniqueAttributes = pathsToUniqueAttributes;
            return this;
        }
        
        public TimeBasedRequirementBuilder setMaxTotalOrderNumberToIgnoreConditon(int maxTotalOrderNumberToIgnoreConditon)
        {
            this.maxTotalOrderNumberToIgnoreConditon = maxTotalOrderNumberToIgnoreConditon;
            return this;
        }
        
        public TimeBasedRequirement build()
        {
            return new TimeBasedRequirement(this);
        }
    }
}
