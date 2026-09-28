package com.bridge.bridge_monitoring.service;

import com.bridge.bridge_monitoring.entity.Alert;
import com.bridge.bridge_monitoring.entity.AlertStatus;
import com.bridge.bridge_monitoring.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Alert> getAlertById(Long id) {
        return alertRepository.findById(id);
    }

    public Alert saveAlert(Alert alert) {
        if (alert.getMessage() == null || alert.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Alert message is required");
        }
        if (alert.getSeverity() == null) {
            throw new IllegalArgumentException("Alert severity is required");
        }
        return alertRepository.save(alert);
    }

    public Alert updateAlert(Long id, Alert updatedAlert) {
        Alert existingAlert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with id: " + id));

        existingAlert.setMessage(updatedAlert.getMessage());
        existingAlert.setSeverity(updatedAlert.getSeverity());
        existingAlert.setStatus(updatedAlert.getStatus());
        return alertRepository.save(existingAlert);
    }

    public Alert resolveAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with id: " + id));
        alert.setStatus(AlertStatus.RESOLVED);
        return alertRepository.save(alert);
    }

    public void deleteAlert(Long id) {
        if (!alertRepository.existsById(id)) {
            throw new IllegalArgumentException("Alert not found with id: " + id);
        }
        alertRepository.deleteById(id);
    }

    public long countOpenAlerts() {
        return alertRepository.findByStatus(AlertStatus.OPEN).size();
    }
}
