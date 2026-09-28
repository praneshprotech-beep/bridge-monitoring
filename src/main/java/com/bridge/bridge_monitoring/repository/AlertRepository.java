package com.bridge.bridge_monitoring.repository;

import com.bridge.bridge_monitoring.entity.Alert;
import com.bridge.bridge_monitoring.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(AlertStatus status);
    List<Alert> findAllByOrderByCreatedAtDesc();
    List<Alert> findByBridgeIdOrderByCreatedAtDesc(Long bridgeId);
}
