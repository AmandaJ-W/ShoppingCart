/*package com.techelevator.services;

import com.techelevator.dao.JdbcCartDao;
import com.techelevator.dao.JdbcProductDao;
import com.techelevator.dao.JdbcUserDao;
import com.techelevator.exception.DaoException;
import com.techelevator.model.Cart;
import com.techelevator.model.CartItem;
import com.techelevator.model.Tax;
import com.techelevator.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public class CartService {

    private final JdbcCartDao cartDao;
    private final JdbcUserDao userDao;
    private final JdbcProductDao productDao;
    private final TaxService taxService;

    private String taxApiUrl = "https://teapi.netlify.app/api/statetax?state=";

    @Autowired
    public CartService(JdbcCartDao cartDao, JdbcUserDao userDao, JdbcProductDao productDao, TaxService taxService) {
        this.cartDao = cartDao;
        this.userDao = userDao;
        this.productDao = productDao;
        this.taxService = taxService;

    }

    public Cart getUserCart(Principal principal) {
        User user = getUser(principal); // Where do I write a getUser method?
        int userId = user.getId();

        try {
            Cart userCart = new Cart();
            userCart.setCartItems(cartDao.getUserCart(userId));

            if (!userCart.getCartItems().isEmpty()) {
                BigDecimal subtotal = calculateSubtotal(userCart.getCartItems());
                userCart.setSubtotal(subtotal);

                String stateCode = getStateCodeFromUser(userId);
                BigDecimal salesTax = getTaxRate(stateCode);
                userCart.setTaxAmount(salesTax);

                BigDecimal calculatedTax = subtotal.multiply(salesTax.divide(BigDecimal.valueOf(100)));
                BigDecimal grandTotal = subtotal.add(calculatedTax);
                userCart.setCartTotal(grandTotal);

                return userCart;
            } else {
                throw new DaoException("No info available");
            }
        } catch (DaoException e) {
            throw new DaoException(e.getMessage());
        }
    }

    // remove item from cart?
    // clear cart?
    private String getStateCodeFromUser(int userId) {
        User user = userDao.getUserById(userId);
        if (user != null) {
            return user.getStateCode();
        } else {
            return null;
        }
    }

    private BigDecimal getTaxRate(String stateCode) {
        try {
            Tax tax = taxService.getTaxRateByState(stateCode);
            if (tax != null) {
                return BigDecimal.valueOf(tax.getSalesTax());
            }
        } catch (DaoException e) {
            throw new DaoException("Error");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateSubtotal(List<CartItem> cartItems) {
        BigDecimal subtotal = BigDecimal.ZERO; // Initially it is set at 0
        // Loop through cart items (use .getProduct() to access the PRICE of the item) And multiply by the quantities of the item
        for (CartItem cartItem : cartItems) {
            subtotal = subtotal.add(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        return subtotal;
    }

    /*
    public User getUser(Principal principal) {
        String userName = principal.getName();
        User user = userDao.getUserByUsername(userName);
        return user;
    }



}
*/
