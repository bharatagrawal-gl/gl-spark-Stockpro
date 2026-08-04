package com.stockpro.alert.service;

import com.stockpro.alert.dto.AlertResponse;
import java.util.List;

public interface AlertService {
    AlertResponse createAlert(String alertType, Long referenceId,
                              String referenceType, String message);
    AlertResponse getById(Long id);
    List<AlertResponse> getAll();
    List<AlertResponse> getUnread();
    List<AlertResponse> getByType(String alertType);
    AlertResponse markAsRead(Long id);
    void markAllAsRead();
}