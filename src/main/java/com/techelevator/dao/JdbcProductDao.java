package com.techelevator.dao;

import com.techelevator.exception.DaoException;
import com.techelevator.model.Product;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Requirements 1-3:
@Component
public class JdbcProductDao implements ProductDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcProductDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Req 1 : As an unauthenticated user, I can review a list of products for sale.
    @Override
    public List<Product> getProducts() {
        List<Product> productList = new ArrayList<>();
        String sql = "SELECT * FROM product";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Product product = mapRowToProduct(results);
                productList.add(product);
            }
        }
        catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return productList;
    }

    // Req 2: As an unauthenticated user, I can search for a list of products by name or SKU:
    @Override
    public List<Product> getProductByName(String name) {
        List<Product> productListByName = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE name ILIKE ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, "%" + name + "%");
            while (results.next()) {
                Product product = mapRowToProduct(results);
                productListByName.add(product);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return productListByName;
    }


    @Override
    public List<Product> getProductBySku(String productSku) {
        List<Product> productListBySku = new ArrayList<>();
        String sql = "SELECT * FROM product WHERE product_sku ILIKE ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql,"%" + productSku + "%");
            while (results.next()) {
                Product product = mapRowToProduct(results);
                productListBySku.add(product);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return productListBySku;
    }


    // Req 3: As an unauthenticated user, I can view additional information about a specific product (product detail).
    @Override
    public Product getProductDetailsById(int id) {
        Product product = null;
        String sql = "SELECT * FROM product WHERE product_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next()) {
                product = mapRowToProduct(results);
            }
        }
        catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return product;
    }


    private Product mapRowToProduct(SqlRowSet rs) {
        Product product = new Product();
        product.setId(rs.getInt("product_id"));
        product.setProductSku(rs.getString("product_sku"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setImageName(rs.getString("image_name"));
        return product;
    }

}
