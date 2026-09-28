package com.bridge.bridge_monitoring.controller;

import com.bridge.bridge_monitoring.entity.Alert;
import com.bridge.bridge_monitoring.service.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/alerts")
    public List<Alert> getAllAlerts() {
        return alertService.getAllAlerts();
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<Alert> getAlertById(@PathVariable Long id) {
        return alertService.getAlertById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/alerts")
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
        try {
            Alert savedAlert = alertService.saveAlert(alert);
            return new ResponseEntity<>(savedAlert, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/alerts/{id}")
    public ResponseEntity<Alert> updateAlert(@PathVariable Long id, @RequestBody Alert alert) {
        try {
            Alert updatedAlert = alertService.updateAlert(id, alert);
            return ResponseEntity.ok(updatedAlert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/alerts/{id}/resolve")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id) {
        try {
            Alert alert = alertService.resolveAlert(id);
            return ResponseEntity.ok(alert);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<String> deleteAlert(@PathVariable Long id) {
        try {
            alertService.deleteAlert(id);
            return ResponseEntity.ok("Alert deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
