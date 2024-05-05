package com.techelevator.model;

// Cart item should also have a price, product_id, name, description, price, image_name
// This info is in the product table in the database
// I added a Product product variable to this class
public class CartItem {
    private int cartItemId;
    private int userId;
    private int quantity;
    private Product product;

    private int productId;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public CartItem() {
    }

    public CartItem(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public CartItem(int cartItemId, int userId, int quantity, Product product, int productId) {
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.quantity = quantity;
        this.product = product;
        this.productId = productId;
    }

    public CartItem(int cartItemId, int userId, int quantity, int productId) {
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.quantity = quantity;
        this.productId = productId;
    }

    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

}
