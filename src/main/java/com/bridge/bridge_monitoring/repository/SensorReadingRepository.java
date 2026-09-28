package com.bridge.bridge_monitoring.repository;

import com.bridge.bridge_monitoring.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findBySensorIdOrderByReadingTimeDesc(Long sensorId);
    List<SensorReading> findAllByOrderByReadingTimeDesc();
}
