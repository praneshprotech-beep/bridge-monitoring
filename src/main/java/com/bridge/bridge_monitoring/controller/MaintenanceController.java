package com.bridge.bridge_monitoring.controller;

import com.bridge.bridge_monitoring.entity.Maintenance;
import com.bridge.bridge_monitoring.service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/maintenance")
    public List<Maintenance> getAllMaintenance() {
        return maintenanceService.getAllMaintenance();
    }

    @GetMapping("/maintenance/{id}")
    public ResponseEntity<Maintenance> getMaintenanceById(@PathVariable Long id) {
        return maintenanceService.getMaintenanceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bridges/{bridgeId}/maintenance")
    public ResponseEntity<Maintenance> createMaintenance(@PathVariable Long bridgeId, @RequestBody Maintenance maintenance) {
        try {
            Maintenance savedMaintenance = maintenanceService.saveMaintenance(maintenance, bridgeId);
            return new ResponseEntity<>(savedMaintenance, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/maintenance/{id}")
    public ResponseEntity<Maintenance> updateMaintenance(@PathVariable Long id, @RequestBody Maintenance maintenance) {
        try {
            Maintenance updatedMaintenance = maintenanceService.updateMaintenance(id, maintenance);
            return ResponseEntity.ok(updatedMaintenance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/maintenance/{id}")
    public ResponseEntity<String> deleteMaintenance(@PathVariable Long id) {
        try {
            maintenanceService.deleteMaintenance(id);
            return ResponseEntity.ok("Maintenance record deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
