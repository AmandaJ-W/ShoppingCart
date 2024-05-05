package com.techelevator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Tax {
    @JsonProperty("salesTax")
    private BigDecimal salesTax;

    public Tax() {
    }

    public Tax(BigDecimal salesTax) {
        this.salesTax = salesTax;
    }

    public BigDecimal getSalesTax() {
        return salesTax;
    }

    public void setSalesTax(BigDecimal salesTax) {
        this.salesTax = salesTax;
    }
}
