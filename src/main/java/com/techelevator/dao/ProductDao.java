package com.techelevator.dao;

import com.techelevator.model.Product;

import java.util.List;

public interface ProductDao {
    public List<Product> getProducts();

    public List<Product> getProductByName(String name);

    public List<Product> getProductBySku(String productSku);
    public Product getProductDetailsById(int id);
}
