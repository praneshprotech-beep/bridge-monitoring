package com.bridge.bridge_monitoring.service;

import com.bridge.bridge_monitoring.entity.*;
import com.bridge.bridge_monitoring.repository.BridgeRepository;
import com.bridge.bridge_monitoring.repository.SensorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final BridgeRepository bridgeRepository;

    public SensorService(SensorRepository sensorRepository, BridgeRepository bridgeRepository) {
        this.sensorRepository = sensorRepository;
        this.bridgeRepository = bridgeRepository;
    }

    public List<Sensor> getAllSensors() {
        return sensorRepository.findAll();
    }

    public List<Sensor> getSensorsByBridge(Long bridgeId) {
        return sensorRepository.findByBridgeId(bridgeId);
    }

    public Optional<Sensor> getSensorById(Long id) {
        return sensorRepository.findById(id);
    }

    public Sensor saveSensor(Sensor sensor, Long bridgeId) {
        Bridge bridge = bridgeRepository.findById(bridgeId)
                .orElseThrow(() -> new IllegalArgumentException("Bridge not found with id: " + bridgeId));

        if (sensor.getSensorCode() == null || sensor.getSensorCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor code is required");
        }
        if (sensor.getSensorType() == null) {
            throw new IllegalArgumentException("Sensor type is required");
        }
        if (sensor.getLocation() == null || sensor.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor location is required");
        }
        if (sensor.getThreshold() <= 0) {
            throw new IllegalArgumentException("Threshold must be greater than zero");
        }
        if (sensor.getUnit() == null || sensor.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("Unit is required");
        }

        sensor.setBridge(bridge);
        if (sensor.getStatus() == null) {
            sensor.setStatus(SensorStatus.ACTIVE);
        }

        return sensorRepository.save(sensor);
    }

    public Sensor updateSensor(Long id, Sensor updatedSensor) {
        Sensor existingSensor = sensorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found with id: " + id));

        existingSensor.setSensorCode(updatedSensor.getSensorCode());
        existingSensor.setSensorType(updatedSensor.getSensorType());
        existingSensor.setLocation(updatedSensor.getLocation());
        existingSensor.setThreshold(updatedSensor.getThreshold());
        existingSensor.setUnit(updatedSensor.getUnit());
        if (updatedSensor.getStatus() != null) {
            existingSensor.setStatus(updatedSensor.getStatus());
        }

        return sensorRepository.save(existingSensor);
    }

    public void deleteSensor(Long id) {
        if (!sensorRepository.existsById(id)) {
            throw new IllegalArgumentException("Sensor not found with id: " + id);
        }
        sensorRepository.deleteById(id);
    }

    public long countSensors() {
        return sensorRepository.count();
    }
}
