package com.xceptance.ordermonitoring.model.configuration.dataobject;

public class Requirement
{
    private Integer minimalOrderAmount;

    private Integer maximalOrderAmount;

    private Integer minimalOrderPercentageWithFeature;

    private Integer maximalOrderPercentageWithFeature;

    public Integer getMinimalOrderAmount()
    {
        return minimalOrderAmount;
    }

    public Integer getMaximalOrderAmount()
    {
        return maximalOrderAmount;
    }

    public Integer getMinimalOrderPercentageWithFeature()
    {
        return minimalOrderPercentageWithFeature;
    }

    public Integer getMaximalOrderPercentageWithFeature()
    {
        return maximalOrderPercentageWithFeature;
    }

    public void setMinimalOrderAmountIfAbsent(final Integer minimalOrderAmount)
    {
        if (this.minimalOrderAmount == null)
        {
            this.minimalOrderAmount = minimalOrderAmount;
        }
    }

    public void setMaximalOrderAmountIfAbsent(final Integer maximalOrderAmount)
    {
        if (this.maximalOrderAmount == null)
        {
            this.maximalOrderAmount = maximalOrderAmount;
        }
    }

    public void setMinimalOrderPercentageWithFeatureIfAbsent(final Integer minimalOrderPercentageWithFeature)
    {
        if (this.minimalOrderPercentageWithFeature == null)
        {
            this.minimalOrderPercentageWithFeature = minimalOrderPercentageWithFeature;
        }
    }

    public void setMaximalOrderPercentageWithFeatureIfAbsent(final Integer maximalOrderPercentageWithFeature)
    {
        if (this.maximalOrderPercentageWithFeature == null)
        {
            this.maximalOrderPercentageWithFeature = maximalOrderPercentageWithFeature;
        }
    }

    public Requirement copy()
    {
        final Requirement copy = new Requirement();
        copy.minimalOrderAmount = minimalOrderAmount;
        copy.maximalOrderAmount = maximalOrderAmount;
        copy.minimalOrderPercentageWithFeature = minimalOrderPercentageWithFeature;
        copy.maximalOrderPercentageWithFeature = maximalOrderPercentageWithFeature;
        return copy;
    }

    @Override
    public String toString()
    {
        return "Requirement [minimalOrderAmount=" + minimalOrderAmount + ", maximalOrderAmount=" + maximalOrderAmount
               + ", minimalOrderPercentageWithFeature=" + minimalOrderPercentageWithFeature
               + ", maximalOrderPercentageWithFeature=" + maximalOrderPercentageWithFeature + "]";
    }
}
