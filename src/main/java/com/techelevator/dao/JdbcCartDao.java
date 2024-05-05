package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.CartItem;
import com.techelevator.model.Product;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Requirements 4-7:
@Component
public class JdbcCartDao implements CartDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCartDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CartItem getCartItemById(int cartItemId) {
        CartItem cartItem = null;
        String sql = "SELECT * FROM cart_item WHERE cart_item_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, cartItemId);
            if (results.next()) {
                cartItem = mapRowToCartItemOnly(results);
            }
        } catch (DaoException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return cartItem;
    }

    // Req 4 Get user cart:
    @Override
    public List<CartItem> getUserCart(int userId) {
        List<CartItem> userCart = new ArrayList<>();
        String sql = "SELECT cart_item_id, user_id, cart_item.product_id, quantity, " +
                "product.product_id, product_sku, name, description, price, image_name " +
                "FROM cart_item " +
                "JOIN product ON product.product_id = cart_item.product_id " +
                "WHERE user_id = ?";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId);
            while (results.next()) {
                CartItem cartItem = mapRowToCartItem(results);
                userCart.add(cartItem);
            }
        } catch (DaoException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return userCart;
    }

    // Req 5 Add cart item to cart:
    @Override
    public CartItem addItemToCart(CartItem cartItem) {
        try {
            String sql = "INSERT INTO cart_item (user_id, quantity, product_id) VALUES (?, ?, ?) RETURNING cart_item_id";
            int cartItemId = jdbcTemplate.queryForObject(sql, Integer.class, cartItem.getUserId(), cartItem.getQuantity(), cartItem.getProductId());
            cartItem.setCartItemId(cartItemId);
            return cartItem;
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Error creating cart item. Data integrity violation.", e);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    // Update quantity helper method (Req 5):
    public void updateCartItemQuantity(CartItem cartItem, int newQuantity) {
        try {
            String sql = "UPDATE cart_item SET quantity = ? WHERE cart_item_id = ?";

            jdbcTemplate.update(sql, newQuantity, cartItem.getCartItemId());
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Error updating item. Data integrity violation.", e);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    }

    // Req 6 remove cart item from cart:
    @Override
    public int removeCartItem(int cartItemId) {
        int numberOfRows = 0;
        String sql = "DELETE FROM cart_item WHERE cart_item_id = ?";
        try {
            numberOfRows = jdbcTemplate.update(sql, cartItemId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return numberOfRows;
    }

    // Req 7 Clear cart associated with a userId:
    @Override
    public int clearUserCart(int userId) {
        int numberOfRows = 0;
        try {
            String sql = "DELETE FROM cart_item WHERE user_id = ?";
            numberOfRows = jdbcTemplate.update(sql, userId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return numberOfRows;
    }


    private CartItem mapRowToCartItem(SqlRowSet rs) {
        CartItem cartItem = new CartItem();
        cartItem.setCartItemId(rs.getInt("cart_item_id"));
        cartItem.setUserId(rs.getInt("user_id"));
        cartItem.setQuantity(rs.getInt("quantity"));
        cartItem.setProductId(rs.getInt(("product_id")));

        Product product = new Product();
        product.setId(rs.getInt("product_id"));
        product.setProductSku(rs.getString("product_sku"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setImageName(rs.getString("image_name"));

        cartItem.setProduct(product);

        return cartItem;
    }

    private CartItem mapRowToCartItemOnly(SqlRowSet rs) {
        CartItem cartItem = new CartItem();
        cartItem.setCartItemId(rs.getInt("cart_item_id"));
        cartItem.setUserId(rs.getInt("user_id"));
        cartItem.setQuantity(rs.getInt("quantity"));
        cartItem.setProductId(rs.getInt(("product_id")));

        return cartItem;
    }


}
