package com.aspcrm.shop.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartLine implements Serializable {
    private final int productId;
    private final String name;
    private final String sku;
    private final BigDecimal unitPrice;
    private int quantity;
    private final int stockQuantity;

    public CartLine(int productId, String name, String sku, BigDecimal unitPrice, int quantity, int stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.stockQuantity = stockQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
