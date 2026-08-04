package com.stockpro.supplier.service;

import com.stockpro.supplier.dto.SupplierRequest;
import com.stockpro.supplier.dto.SupplierResponse;
import com.stockpro.supplier.entity.Supplier;
import com.stockpro.supplier.exception.DuplicateResourceException;
import com.stockpro.supplier.exception.ResourceNotFoundException;
import com.stockpro.supplier.repository.SupplierRepository;
import com.stockpro.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public SupplierResponse create(SupplierRequest request) {
        if (supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public List<SupplierResponse> getAll() {
        return supplierRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<SupplierResponse> getAllActive() {
        return supplierRepository.findByIsActive(true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = findById(id);

        if (!supplier.getEmail().equals(request.getEmail())
                && supplierRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        supplier.setName(request.getName());
        supplier.setContactName(request.getContactName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setUpdatedAt(LocalDateTime.now());

        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponse activate(Long id) {
        Supplier supplier = findById(id);
        supplier.setActive(true);
        supplier.setUpdatedAt(LocalDateTime.now());
        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponse deactivate(Long id) {
        Supplier supplier = findById(id);
        supplier.setActive(false);
        supplier.setUpdatedAt(LocalDateTime.now());
        return toResponse(supplierRepository.save(supplier));
    }

    @Override
    public void delete(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found: " + id);
        }
        supplierRepository.deleteById(id);
    }

    private Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
    }

    private SupplierResponse toResponse(Supplier s) {
        return new SupplierResponse(
                s.getId(),
                s.getName(),
                s.getContactName(),
                s.getEmail(),
                s.getPhone(),
                s.getAddress(),
                s.isActive(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}

