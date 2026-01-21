package com.Rajat.product_service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Cacheable(value = "products") // if a product is requested multiple times DB is only hit a single time
    public Product getProductById(Long id) {
        System.out.println("Fetching from database for ID: " + id);// to verify cache working
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // update cache when data changes to prevent stale data
    @CacheEvict(value = "products")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

}
