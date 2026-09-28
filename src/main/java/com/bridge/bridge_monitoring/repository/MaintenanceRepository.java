package com.bridge.bridge_monitoring.repository;

import com.bridge.bridge_monitoring.entity.Maintenance;
import com.bridge.bridge_monitoring.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    List<Maintenance> findByStatus(MaintenanceStatus status);
    List<Maintenance> findAllByOrderByCreatedAtDesc();
    List<Maintenance> findByBridgeIdOrderByCreatedAtDesc(Long bridgeId);
}
