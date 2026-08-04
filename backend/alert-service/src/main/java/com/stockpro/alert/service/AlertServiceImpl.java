package com.stockpro.alert.service;

import com.stockpro.alert.dto.AlertResponse;
import com.stockpro.alert.entity.Alert;
import com.stockpro.alert.exception.ResourceNotFoundException;
import com.stockpro.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    @Override
    @Transactional
    public AlertResponse createAlert(String alertType, Long referenceId,
                                     String referenceType, String message) {
        Alert alert = Alert.builder()
                .alertType(alertType)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .message(message)
                .isRead(false)
                .build();
        return mapToResponse(alertRepository.save(alert));
    }

    @Override
    public AlertResponse getById(Long id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    public List<AlertResponse> getAll() {
        return alertRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AlertResponse> getUnread() {
        return alertRepository.findByIsRead(false)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AlertResponse> getByType(String alertType) {
        return alertRepository.findByAlertType(alertType)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AlertResponse markAsRead(Long id) {
        Alert alert = findOrThrow(id);
        alert.setIsRead(true);
        return mapToResponse(alertRepository.save(alert));
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        List<Alert> unread = alertRepository.findByIsRead(false);
        unread.forEach(a -> a.setIsRead(true));
        alertRepository.saveAll(unread);
    }

    private Alert findOrThrow(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with id: " + id));
    }

    private AlertResponse mapToResponse(Alert a) {
        return AlertResponse.builder()
                .id(a.getId())
                .alertType(a.getAlertType())
                .referenceId(a.getReferenceId())
                .referenceType(a.getReferenceType())
                .message(a.getMessage())
                .isRead(a.getIsRead())
                .createdAt(a.getCreatedAt())
                .build();
    }
}