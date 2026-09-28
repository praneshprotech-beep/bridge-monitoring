package com.bridge.bridge_monitoring.service;

import com.bridge.bridge_monitoring.entity.*;
import com.bridge.bridge_monitoring.repository.AlertRepository;
import com.bridge.bridge_monitoring.repository.BridgeRepository;
import com.bridge.bridge_monitoring.repository.SensorReadingRepository;
import com.bridge.bridge_monitoring.repository.SensorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorRepository sensorRepository;
    private final AlertRepository alertRepository;
    private final BridgeRepository bridgeRepository;

    public SensorReadingService(SensorReadingRepository sensorReadingRepository,
                               SensorRepository sensorRepository,
                               AlertRepository alertRepository,
                               BridgeRepository bridgeRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.sensorRepository = sensorRepository;
        this.alertRepository = alertRepository;
        this.bridgeRepository = bridgeRepository;
    }

    public List<SensorReading> getAllReadings() {
        return sensorReadingRepository.findAllByOrderByReadingTimeDesc();
    }

    public Optional<SensorReading> getReadingById(Long id) {
        return sensorReadingRepository.findById(id);
    }

    public SensorReading saveReading(Long sensorId, double value) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found with id: " + sensorId));

        if (sensor.getStatus() != SensorStatus.ACTIVE) {
            throw new IllegalArgumentException("Sensor is inactive and cannot record readings");
        }

        ReadingStatus readingStatus = determineReadingStatus(sensor, value);
        SensorReading sensorReading = new SensorReading(sensor, value, LocalDateTime.now(), readingStatus);
        SensorReading savedReading = sensorReadingRepository.save(sensorReading);

        if (readingStatus == ReadingStatus.CRITICAL) {
            createAlertForCriticalReading(sensor, value);
        }

        updateBridgeStatus(sensor.getBridge());
        return savedReading;
    }

    public SensorReading updateReading(Long id, SensorReading updatedReading) {
        SensorReading existingReading = sensorReadingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reading not found with id: " + id));

        existingReading.setValue(updatedReading.getValue());
        existingReading.setReadingTime(updatedReading.getReadingTime());
        existingReading.setStatus(determineReadingStatus(existingReading.getSensor(), updatedReading.getValue()));

        return sensorReadingRepository.save(existingReading);
    }

    public void deleteReading(Long id) {
        if (!sensorReadingRepository.existsById(id)) {
            throw new IllegalArgumentException("Reading not found with id: " + id);
        }
        sensorReadingRepository.deleteById(id);
    }

    public ReadingStatus determineReadingStatus(Sensor sensor, double value) {
        if (value > sensor.getThreshold()) {
            return ReadingStatus.CRITICAL;
        }
        if (value >= sensor.getThreshold() * 0.8) {
            return ReadingStatus.WARNING;
        }
        return ReadingStatus.NORMAL;
    }

    private void createAlertForCriticalReading(Sensor sensor, double value) {
        Bridge bridge = sensor.getBridge();
        String message = "Critical " + sensor.getSensorType() + " detected on Bridge " + bridge.getBridgeCode();

        boolean alertExists = alertRepository.findByBridgeIdOrderByCreatedAtDesc(bridge.getId()).stream()
                .anyMatch(alert -> alert.getSensor().getId().equals(sensor.getId()) && alert.getStatus() == AlertStatus.OPEN);

        if (!alertExists) {
            Alert alert = new Alert(
                    bridge,
                    sensor,
                    message,
                    AlertSeverity.CRITICAL,
                    LocalDateTime.now(),
                    AlertStatus.OPEN
            );
            alertRepository.save(alert);
        }
    }

    private void updateBridgeStatus(Bridge bridge) {
        List<Sensor> sensors = sensorRepository.findByBridgeId(bridge.getId());
        if (sensors.isEmpty()) {
            bridge.setStatus(BridgeStatus.SAFE);
            bridgeRepository.save(bridge);
            return;
        }

        boolean hasCritical = false;
        boolean hasWarning = false;

        for (Sensor sensor : sensors) {
            List<SensorReading> readings = sensorReadingRepository.findBySensorIdOrderByReadingTimeDesc(sensor.getId());
            if (!readings.isEmpty()) {
                ReadingStatus latestStatus = readings.get(0).getStatus();
                if (latestStatus == ReadingStatus.CRITICAL) {
                    hasCritical = true;
                } else if (latestStatus == ReadingStatus.WARNING) {
                    hasWarning = true;
                }
            }
        }

        if (hasCritical) {
            bridge.setStatus(BridgeStatus.CRITICAL);
        } else if (hasWarning) {
            bridge.setStatus(BridgeStatus.WARNING);
        } else {
            bridge.setStatus(BridgeStatus.SAFE);
        }

        bridgeRepository.save(bridge);
    }
}
