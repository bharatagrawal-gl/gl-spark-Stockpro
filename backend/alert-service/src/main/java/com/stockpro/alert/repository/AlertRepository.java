package com.stockpro.alert.repository;

import com.stockpro.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByAlertType(String alertType);
    List<Alert> findByIsRead(Boolean isRead);
    List<Alert> findByAlertTypeAndIsRead(String alertType, Boolean isRead);
    List<Alert> findByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}