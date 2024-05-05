package com.techelevator.controller;

import com.techelevator.dao.JdbcCartDao;
import com.techelevator.dao.JdbcProductDao;
import com.techelevator.dao.JdbcUserDao;
import com.techelevator.exception.DaoException;
import com.techelevator.model.*;
import com.techelevator.services.TaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/cart")
@PreAuthorize("isAuthenticated()")
public class CartController {

    private String taxApiUrl = "https://teapi.netlify.app/api/statetax?state=";

    private final JdbcCartDao cartDao;
    private final JdbcUserDao userDao;
    private final JdbcProductDao productDao;
    private final TaxService taxService;

    @Autowired
    public CartController(JdbcCartDao cartDao, JdbcUserDao userDao, JdbcProductDao productDao, TaxService taxService) {
        this.cartDao = cartDao;
        this.userDao = userDao;
        this.productDao = productDao;
        this.taxService = taxService;
    }

    // Req 4 Get user's cart:
    @RequestMapping(path = "", method = RequestMethod.GET)
    public ResponseEntity<?> getUserCart(Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        int userId = user.getId();

        try {
            //Create new cart and fill it with items:
            Cart userCart = new Cart();
            userCart.setCartItems(cartDao.getUserCart(userId));

            // If the user's cart is NOT empty
            if (!userCart.getCartItems().isEmpty()) {

                //Calculate subtotal and add that to the cart:
                BigDecimal subtotal = calculateSubtotal(userCart.getCartItems());
                userCart.setSubtotal(subtotal);

                //Retrieve tax from external API and calculate tax, then add to cart
                String stateCode = getStateCodeFromUser(userId);
                BigDecimal salesTax = getTaxRate(stateCode);
                userCart.setTaxAmount(salesTax);

                // Calculate the tax amount then add it to the subtotal to get the cart total
                BigDecimal calculatedTax = subtotal.multiply(salesTax.divide(BigDecimal.valueOf(100)));
                BigDecimal grandTotal = subtotal.add(calculatedTax);
                userCart.setCartTotal(grandTotal);

                return ResponseEntity.ok(userCart);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Your cart is empty.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error accessing cart.");
        }
    }


    // Req 5 Add cart item to cart and increase quantity of item if needed:
    @RequestMapping(path = "/items", method = RequestMethod.POST)
    public List<CartItem> addCartItemToCart(@RequestBody CartItem cartItem, Principal principal) {
        User user = userDao.getUserByUsername(principal.getName());
        int userId = user.getId();

        try {
            List<CartItem> userCart = cartDao.getUserCart(userId);
            boolean itemExists = false;
            int newQuantity = 0;
            for (CartItem item : userCart) {
                if (cartItem.getProductId() == item.getProductId()) {
                    itemExists = true;
                    int initialQuantity = item.getQuantity();
                    newQuantity = initialQuantity + cartItem.getQuantity();

                    // Update the quantity of the item in the existing cartItem:
                    item.setQuantity(newQuantity);
                    cartDao.updateCartItemQuantity(item, newQuantity);

                    break;
                }
            }
            // If item doesn't exist:
            if (!itemExists) {
                // Get the product details of the item:
                Product product = productDao.getProductDetailsById(cartItem.getProductId());

                // Set the user id, product info:
                cartItem.setUserId(userId);
                cartItem.setProduct(product);

                // Now the item needs to be added to the cart:
                cartDao.addItemToCart(cartItem);
            }
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add item to cart.");
        }
        // return updated cart:
        List<CartItem> updatedUserCart = cartDao.getUserCart(userId);
        return updatedUserCart;
    }

    // Req 6 Remove cart item from cart:
    @RequestMapping(path = "/items/{cartItemId}", method = RequestMethod.DELETE)
    public void deleteCartItem(@PathVariable int cartItemId, Principal principal) {
        try {
            User user = userDao.getUserByUsername(principal.getName());
            int userId = user.getId();

            // Get the cart item by id:
            CartItem cartItem = cartDao.getCartItemById(cartItemId);

            if (cartItem != null && cartItem.getUserId() == userId) {
                cartDao.removeCartItem(cartItemId);
            }

        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to remove item from cart. Check to make sure it exists in your cart.");
        }
    }

    // Req 7 clear cart that corresponds to the user:
    @RequestMapping(path = "", method = RequestMethod.DELETE)
    public ResponseEntity<?> clearCart(Principal principal) {
        try {
            User user = userDao.getUserByUsername(principal.getName());
            int userId = user.getId();
            List<CartItem> userCart = cartDao.getUserCart(userId);

            // If the user's cart is not empty:
            if (!userCart.isEmpty()) {
                cartDao.clearUserCart(userId);
            } else {
                // Otherwise, return a message saying that the cart is already empty:
                Map<String, String> emptyCart = new HashMap<>();
                emptyCart.put("error", "Cart is already empty!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(emptyCart);

            }
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to empty cart.");
        }
        return ResponseEntity.ok().build();
    }

    private String getStateCodeFromUser(int userId) {
        User user = userDao.getUserById(userId);
        if (user != null) {
            return user.getStateCode();
        } else {
            return null;
        }
    }


    private BigDecimal getTaxRate(String stateCode) {
        BigDecimal taxRate = taxService.getTaxRateByState(stateCode);
        return taxRate;
    }




    private BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        BigDecimal subtotal = BigDecimal.ZERO; // Initially it is set at 0
        // Loop through cart items (use .getProduct() to access the PRICE of the item)
        // then multiply by the quantities of the item
        for (CartItem cartItem : cartItems) {
            subtotal = subtotal.add(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        return subtotal;
    }


}
