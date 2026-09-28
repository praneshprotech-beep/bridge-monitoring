package com.bridge.bridge_monitoring.repository;

import com.bridge.bridge_monitoring.entity.Sensor;
import com.bridge.bridge_monitoring.entity.SensorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findBySensorCode(String sensorCode);
    List<Sensor> findByBridgeId(Long bridgeId);
    List<Sensor> findByStatus(SensorStatus status);
}
