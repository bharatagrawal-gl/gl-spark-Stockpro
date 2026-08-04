package com.stockpro.product.service;

import com.stockpro.product.dto.ProductRequest;
import com.stockpro.product.dto.ProductResponse;
import com.stockpro.product.entity.Product;
import com.stockpro.product.exception.DuplicateResourceException;
import com.stockpro.product.exception.ResourceNotFoundException;
import com.stockpro.product.repository.ProductRepository;
import com.stockpro.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .price(request.getPrice())
                .reorderLevel(request.getReorderLevel())
                .barcode(request.getBarcode())
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public ProductResponse getBySku(String sku) {
        return toResponse(productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku)));
    }

    @Override
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllActive() {
        return productRepository.findByIsActive(true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);

        if (!product.getSku().equals(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists: " + request.getSku());
        }

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setReorderLevel(request.getReorderLevel());
        product.setBarcode(request.getBarcode());
        product.setUpdatedAt(LocalDateTime.now());

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse activate(Long id) {
        Product product = findById(id);
        product.setActive(true);
        product.setUpdatedAt(LocalDateTime.now());
        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse deactivate(Long id) {
        Product product = findById(id);
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        return toResponse(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getSku(),
                p.getPrice(),
                p.getReorderLevel(),
                p.getBarcode(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}