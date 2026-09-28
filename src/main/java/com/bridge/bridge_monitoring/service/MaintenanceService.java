package com.bridge.bridge_monitoring.service;

import com.bridge.bridge_monitoring.entity.*;
import com.bridge.bridge_monitoring.repository.BridgeRepository;
import com.bridge.bridge_monitoring.repository.MaintenanceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final BridgeRepository bridgeRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, BridgeRepository bridgeRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.bridgeRepository = bridgeRepository;
    }

    public List<Maintenance> getAllMaintenance() {
        return maintenanceRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Maintenance> getMaintenanceById(Long id) {
        return maintenanceRepository.findById(id);
    }

    public Maintenance saveMaintenance(Maintenance maintenance, Long bridgeId) {
        Bridge bridge = bridgeRepository.findById(bridgeId)
                .orElseThrow(() -> new IllegalArgumentException("Bridge not found with id: " + bridgeId));

        if (maintenance.getIssue() == null || maintenance.getIssue().trim().isEmpty()) {
            throw new IllegalArgumentException("Maintenance issue is required");
        }
        if (maintenance.getPriority() == null) {
            throw new IllegalArgumentException("Maintenance priority is required");
        }
        if (maintenance.getAssignedTeam() == null || maintenance.getAssignedTeam().trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned team is required");
        }

        maintenance.setBridge(bridge);
        if (maintenance.getCreatedAt() == null) {
            maintenance.setCreatedAt(LocalDateTime.now());
        }
        if (maintenance.getStatus() == null) {
            maintenance.setStatus(MaintenanceStatus.PENDING);
        }

        return maintenanceRepository.save(maintenance);
    }

    public Maintenance updateMaintenance(Long id, Maintenance updatedMaintenance) {
        Maintenance existingMaintenance = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found with id: " + id));

        existingMaintenance.setIssue(updatedMaintenance.getIssue());
        existingMaintenance.setPriority(updatedMaintenance.getPriority());
        existingMaintenance.setAssignedTeam(updatedMaintenance.getAssignedTeam());
        if (updatedMaintenance.getStatus() != null) {
            existingMaintenance.setStatus(updatedMaintenance.getStatus());
        }
        if (updatedMaintenance.getStatus() == MaintenanceStatus.COMPLETED) {
            existingMaintenance.setCompletedAt(LocalDateTime.now());
        }
        return maintenanceRepository.save(existingMaintenance);
    }

    public void deleteMaintenance(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new IllegalArgumentException("Maintenance record not found with id: " + id);
        }
        maintenanceRepository.deleteById(id);
    }

    public long countPendingMaintenance() {
        return maintenanceRepository.findByStatus(MaintenanceStatus.PENDING).size();
    }
}
