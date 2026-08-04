package com.stockpro.product;

import com.stockpro.product.dto.ProductRequest;
import com.stockpro.product.dto.ProductResponse;
import com.stockpro.product.entity.Product;
import com.stockpro.product.exception.DuplicateResourceException;
import com.stockpro.product.exception.ResourceNotFoundException;
import com.stockpro.product.repository.ProductRepository;
import com.stockpro.product.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Widget A")
                .sku("SKU-001")
                .price(new BigDecimal("99.99"))
                .reorderLevel(10)
                .barcode("1234567890")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new ProductRequest();
        sampleRequest.setName("Widget A");
        sampleRequest.setSku("SKU-001");
        sampleRequest.setPrice(new BigDecimal("99.99"));
        sampleRequest.setReorderLevel(10);
        sampleRequest.setBarcode("1234567890");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: saves product and returns response")
    void create_success() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.create(sampleRequest);

        assertThat(response.getName()).isEqualTo("Widget A");
        assertThat(response.getSku()).isEqualTo("SKU-001");
        assertThat(response.getPrice()).isEqualByComparingTo("99.99");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("create: throws DuplicateResourceException when SKU already exists")
    void create_duplicateSku() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU-001");

        verify(productRepository, never()).save(any());
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: returns product when found")
    void getById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Widget A");
    }

    @Test
    @DisplayName("getById: throws ResourceNotFoundException when not found")
    void getById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getBySku ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getBySku: returns product when SKU found")
    void getBySku_found() {
        when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getBySku("SKU-001");

        assertThat(response.getSku()).isEqualTo("SKU-001");
    }

    @Test
    @DisplayName("getBySku: throws ResourceNotFoundException when SKU not found")
    void getBySku_notFound() {
        when(productRepository.findBySku("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getBySku("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll: returns all products")
    void getAll_returnsList() {
        Product p2 = Product.builder().id(2L).name("Gadget B").sku("SKU-002")
                .price(BigDecimal.TEN).reorderLevel(5).isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(productRepository.findAll()).thenReturn(List.of(sampleProduct, p2));

        List<ProductResponse> result = productService.getAll();

        assertThat(result).hasSize(2);
    }

    // ── getAllActive ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllActive: returns only active products")
    void getAllActive_onlyActive() {
        when(productRepository.findByIsActive(true)).thenReturn(List.of(sampleProduct));

        List<ProductResponse> result = productService.getAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    // ── searchByName ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchByName: returns matching products")
    void searchByName_matches() {
        when(productRepository.findByNameContainingIgnoreCase("widget"))
                .thenReturn(List.of(sampleProduct));

        List<ProductResponse> result = productService.searchByName("widget");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("widget");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: updates product fields and saves")
    void update_success() {
        ProductRequest updateReq = new ProductRequest();
        updateReq.setName("Widget A+");
        updateReq.setSku("SKU-001");
        updateReq.setPrice(new BigDecimal("109.99"));
        updateReq.setReorderLevel(15);
        updateReq.setBarcode("1234567890");

        Product updated = Product.builder().id(1L).name("Widget A+").sku("SKU-001")
                .price(new BigDecimal("109.99")).reorderLevel(15).isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        ProductResponse response = productService.update(1L, updateReq);

        assertThat(response.getName()).isEqualTo("Widget A+");
        assertThat(response.getPrice()).isEqualByComparingTo("109.99");
    }

    @Test
    @DisplayName("update: throws DuplicateResourceException when new SKU already taken")
    void update_duplicateSku() {
        ProductRequest req = new ProductRequest();
        req.setName("Widget A");
        req.setSku("SKU-999");  // different from current
        req.setPrice(BigDecimal.TEN);
        req.setReorderLevel(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.existsBySku("SKU-999")).thenReturn(true);

        assertThatThrownBy(() -> productService.update(1L, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SKU-999");
    }

    // ── activate / deactivate ─────────────────────────────────────────────────

    @Test
    @DisplayName("activate: sets product active and saves")
    void activate_success() {
        sampleProduct.setActive(false);
        Product activated = Product.builder().id(1L).name("Widget A").sku("SKU-001")
                .price(BigDecimal.TEN).reorderLevel(5).isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(activated);

        ProductResponse response = productService.activate(1L);

        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("deactivate: sets product inactive and saves")
    void deactivate_success() {
        Product deactivated = Product.builder().id(1L).name("Widget A").sku("SKU-001")
                .price(BigDecimal.TEN).reorderLevel(5).isActive(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(deactivated);

        ProductResponse response = productService.deactivate(1L);

        assertThat(response.isActive()).isFalse();
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: deletes product when found")
    void delete_success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: throws ResourceNotFoundException when product not found")
    void delete_notFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(productRepository, never()).deleteById(any());
    }
}
