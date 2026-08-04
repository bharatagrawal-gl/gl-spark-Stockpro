package com.stockpro.alert;

import com.stockpro.alert.dto.AlertResponse;
import com.stockpro.alert.entity.Alert;
import com.stockpro.alert.exception.ResourceNotFoundException;
import com.stockpro.alert.repository.AlertRepository;
import com.stockpro.alert.service.AlertServiceImpl;
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
@DisplayName("AlertServiceImpl Tests")
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertServiceImpl alertService;

    private Alert sampleAlert;

    @BeforeEach
    void setUp() {
        sampleAlert = Alert.builder()
                .id(1L)
                .alertType("LOW_STOCK")
                .referenceId(10L)
                .referenceType("PRODUCT")
                .message("Stock is low for product 10")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── createAlert ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAlert: saves and returns alert response")
    void createAlert_success() {
        when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

        AlertResponse response = alertService.createAlert(
                "LOW_STOCK", 10L, "PRODUCT", "Stock is low for product 10");

        assertThat(response).isNotNull();
        assertThat(response.getAlertType()).isEqualTo("LOW_STOCK");
        assertThat(response.getReferenceId()).isEqualTo(10L);
        assertThat(response.getMessage()).isEqualTo("Stock is low for product 10");
        assertThat(response.getIsRead()).isFalse();
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    @DisplayName("createAlert: new alert is marked unread by default")
    void createAlert_isReadDefaultFalse() {
        when(alertRepository.save(any(Alert.class))).thenReturn(sampleAlert);

        AlertResponse response = alertService.createAlert("LOW_STOCK", 10L, "PRODUCT", "msg");

        assertThat(response.getIsRead()).isFalse();
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: returns alert when found")
    void getById_found() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(sampleAlert));

        AlertResponse response = alertService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAlertType()).isEqualTo("LOW_STOCK");
    }

    @Test
    @DisplayName("getById: throws ResourceNotFoundException when not found")
    void getById_notFound() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll: returns all alerts")
    void getAll_returnsList() {
        Alert second = Alert.builder().id(2L).alertType("PO_OVERDUE")
                .message("PO overdue").isRead(true).createdAt(LocalDateTime.now()).build();
        when(alertRepository.findAll()).thenReturn(List.of(sampleAlert, second));

        List<AlertResponse> result = alertService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AlertResponse::getAlertType)
                .containsExactlyInAnyOrder("LOW_STOCK", "PO_OVERDUE");
    }

    @Test
    @DisplayName("getAll: returns empty list when no alerts exist")
    void getAll_empty() {
        when(alertRepository.findAll()).thenReturn(List.of());

        assertThat(alertService.getAll()).isEmpty();
    }

    // ── getUnread ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUnread: returns only unread alerts")
    void getUnread_returnsUnread() {
        when(alertRepository.findByIsRead(false)).thenReturn(List.of(sampleAlert));

        List<AlertResponse> result = alertService.getUnread();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsRead()).isFalse();
    }

    // ── getByType ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByType: returns alerts matching type")
    void getByType_returnsMatching() {
        when(alertRepository.findByAlertType("LOW_STOCK")).thenReturn(List.of(sampleAlert));

        List<AlertResponse> result = alertService.getByType("LOW_STOCK");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlertType()).isEqualTo("LOW_STOCK");
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead: flips isRead to true and saves")
    void markAsRead_success() {
        Alert updated = Alert.builder().id(1L).alertType("LOW_STOCK")
                .message("msg").isRead(true).createdAt(LocalDateTime.now()).build();
        when(alertRepository.findById(1L)).thenReturn(Optional.of(sampleAlert));
        when(alertRepository.save(any(Alert.class))).thenReturn(updated);

        AlertResponse response = alertService.markAsRead(1L);

        assertThat(response.getIsRead()).isTrue();
        verify(alertRepository).save(sampleAlert);
    }

    @Test
    @DisplayName("markAsRead: throws ResourceNotFoundException when alert not found")
    void markAsRead_notFound() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.markAsRead(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── markAllAsRead ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("markAllAsRead: marks all unread alerts as read")
    void markAllAsRead_success() {
        Alert a1 = Alert.builder().id(1L).alertType("A").message("m").isRead(false).createdAt(LocalDateTime.now()).build();
        Alert a2 = Alert.builder().id(2L).alertType("B").message("m").isRead(false).createdAt(LocalDateTime.now()).build();
        when(alertRepository.findByIsRead(false)).thenReturn(List.of(a1, a2));

        alertService.markAllAsRead();

        assertThat(a1.getIsRead()).isTrue();
        assertThat(a2.getIsRead()).isTrue();
        verify(alertRepository).saveAll(List.of(a1, a2));
    }

    @Test
    @DisplayName("markAllAsRead: does nothing when no unread alerts")
    void markAllAsRead_noneUnread() {
        when(alertRepository.findByIsRead(false)).thenReturn(List.of());

        alertService.markAllAsRead();

        verify(alertRepository).saveAll(List.of());
    }
}
