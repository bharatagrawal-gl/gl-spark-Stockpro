package com.stockpro.supplier;

import com.stockpro.supplier.dto.SupplierRequest;
import com.stockpro.supplier.dto.SupplierResponse;
import com.stockpro.supplier.entity.Supplier;
import com.stockpro.supplier.exception.DuplicateResourceException;
import com.stockpro.supplier.exception.ResourceNotFoundException;
import com.stockpro.supplier.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierServiceImpl Tests")
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier sampleSupplier;
    private SupplierRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleSupplier = Supplier.builder()
                .id(1L)
                .name("Acme Corp")
                .contactName("John Doe")
                .email("acme@example.com")
                .phone("+91-9000000001")
                .address("123 Industrial Road")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new SupplierRequest();
        sampleRequest.setName("Acme Corp");
        sampleRequest.setContactName("John Doe");
        sampleRequest.setEmail("acme@example.com");
        sampleRequest.setPhone("+91-9000000001");
        sampleRequest.setAddress("123 Industrial Road");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: saves supplier and returns response")
    void create_success() {
        when(supplierRepository.existsByEmail("acme@example.com")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(sampleSupplier);

        SupplierResponse response = supplierService.create(sampleRequest);

        assertThat(response.getName()).isEqualTo("Acme Corp");
        assertThat(response.getEmail()).isEqualTo("acme@example.com");
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("create: throws DuplicateResourceException when email already exists")
    void create_duplicateEmail() {
        when(supplierRepository.existsByEmail("acme@example.com")).thenReturn(true);

        assertThatThrownBy(() -> supplierService.create(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("acme@example.com");

        verify(supplierRepository, never()).save(any());
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: returns supplier when found")
    void getById_found() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));

        SupplierResponse response = supplierService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Acme Corp");
    }

    @Test
    @DisplayName("getById: throws ResourceNotFoundException when not found")
    void getById_notFound() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll: returns all suppliers")
    void getAll_returnsList() {
        Supplier s2 = Supplier.builder().id(2L).name("Beta Ltd").email("beta@example.com")
                .isActive(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(supplierRepository.findAll()).thenReturn(List.of(sampleSupplier, s2));

        List<SupplierResponse> result = supplierService.getAll();

        assertThat(result).hasSize(2);
    }

    // ── getAllActive ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllActive: returns only active suppliers")
    void getAllActive_onlyActive() {
        when(supplierRepository.findByIsActive(true)).thenReturn(List.of(sampleSupplier));

        List<SupplierResponse> result = supplierService.getAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: updates supplier fields and saves")
    void update_success() {
        SupplierRequest updateReq = new SupplierRequest();
        updateReq.setName("Acme Corp Updated");
        updateReq.setContactName("Jane Doe");
        updateReq.setEmail("acme@example.com");  // same email — no dup check needed
        updateReq.setPhone("+91-9999999999");
        updateReq.setAddress("456 New Road");

        Supplier updated = Supplier.builder().id(1L).name("Acme Corp Updated")
                .email("acme@example.com").contactName("Jane Doe").isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(updated);

        SupplierResponse response = supplierService.update(1L, updateReq);

        assertThat(response.getName()).isEqualTo("Acme Corp Updated");
    }

    @Test
    @DisplayName("update: throws DuplicateResourceException when new email already taken")
    void update_duplicateEmail() {
        SupplierRequest req = new SupplierRequest();
        req.setName("Acme Corp");
        req.setEmail("other@example.com");  // different from current

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> supplierService.update(1L, req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("other@example.com");
    }

    // ── activate / deactivate ─────────────────────────────────────────────────

    @Test
    @DisplayName("activate: sets supplier active and saves")
    void activate_success() {
        sampleSupplier.setActive(false);
        Supplier activated = Supplier.builder().id(1L).name("Acme Corp")
                .email("acme@example.com").isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(activated);

        SupplierResponse response = supplierService.activate(1L);

        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("deactivate: sets supplier inactive and saves")
    void deactivate_success() {
        Supplier deactivated = Supplier.builder().id(1L).name("Acme Corp")
                .email("acme@example.com").isActive(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(deactivated);

        SupplierResponse response = supplierService.deactivate(1L);

        assertThat(response.isActive()).isFalse();
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: deletes supplier when found")
    void delete_success() {
        when(supplierRepository.existsById(1L)).thenReturn(true);

        supplierService.delete(1L);

        verify(supplierRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: throws ResourceNotFoundException when not found")
    void delete_notFound() {
        when(supplierRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> supplierService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(supplierRepository, never()).deleteById(any());
    }
}
