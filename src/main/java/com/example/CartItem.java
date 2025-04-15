package com.example;

import java.math.BigDecimal;

public class CartItem {
    private int menuId;
    private String name;
    private BigDecimal price;
    private int quantity;

    public CartItem(int menuId, String name, BigDecimal price, int quantity) {
        this.menuId = menuId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getMenuId() { return menuId; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
