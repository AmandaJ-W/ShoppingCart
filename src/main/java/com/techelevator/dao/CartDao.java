package com.techelevator.dao;

import com.techelevator.model.CartItem;

import java.util.List;

public interface CartDao {
    public CartItem getCartItemById(int cartItemId); // Get cart item by cartItemId
    public List<CartItem> getUserCart(int userId); // Get and display a user's shopping cart
    public CartItem addItemToCart(CartItem cartItem); // Add an item to shopping cart
    public int removeCartItem(int cartItemId); // Delete an item from shopping cart
    public int clearUserCart(int userId); // Empty shopping cart


}
