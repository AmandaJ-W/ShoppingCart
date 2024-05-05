package com.techelevator.controller;

import com.techelevator.dao.JdbcProductDao;
import com.techelevator.exception.DaoException;
import com.techelevator.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/products") // removed /api/
public class ProductController {

    private final JdbcProductDao productDao;

    @Autowired
    public ProductController(JdbcProductDao productDao) {
        this.productDao = productDao;
    }


    // Req 1 & 2: See a list of all products or search for product by name and/or sku:
    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<Product> getProducts(@RequestParam(required = false) String name, @RequestParam(required = false) String productSku) {
            try {
                if (name != null) {
                    List<Product> productsByName = productDao.getProductByName(name);
                    return productsByName;
                } else if (productSku != null) {
                    List<Product> productsBySku = productDao.getProductBySku(productSku);
                    return productsBySku;
                } else {
                    return productDao.getProducts();
                }
            } catch (DaoException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Product not found.");
            }
    }


    // Req 3 search for product by id:
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Product getProductById(@PathVariable("id") int id) {
        Product product = productDao.getProductDetailsById(id);
        try {
            if (product != null) {
                return product;
            } else {
                throw new DaoException("Product not found.");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Product not found.");
        }
    }


}
