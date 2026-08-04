package com.stockpro.product.service;

import com.stockpro.product.dto.ProductRequest;
import com.stockpro.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse getById(Long id);
    ProductResponse getBySku(String sku);
    List<ProductResponse> getAll();
    List<ProductResponse> getAllActive();
    List<ProductResponse> searchByName(String name);
    ProductResponse update(Long id, ProductRequest request);
    ProductResponse activate(Long id);
    ProductResponse deactivate(Long id);
    void delete(Long id);
}