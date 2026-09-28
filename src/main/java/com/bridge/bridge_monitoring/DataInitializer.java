package com.bridge.bridge_monitoring;

import com.bridge.bridge_monitoring.entity.*;
import com.bridge.bridge_monitoring.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {

    private final BridgeRepository bridgeRepository;
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final AlertRepository alertRepository;
    private final MaintenanceRepository maintenanceRepository;

    public DataInitializer(BridgeRepository bridgeRepository,
                          SensorRepository sensorRepository,
                          SensorReadingRepository sensorReadingRepository,
                          AlertRepository alertRepository,
                          MaintenanceRepository maintenanceRepository) {
        this.bridgeRepository = bridgeRepository;
        this.sensorRepository = sensorRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.alertRepository = alertRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @PostConstruct
    public void init() {
        if (bridgeRepository.count() > 0) {
            return;
        }

        Bridge bridge1 = bridgeRepository.save(new Bridge("BR001", "City River Bridge", "Coimbatore", "Cable Stayed", 850, 2018, 24, BridgeStatus.SAFE));
        Bridge bridge2 = bridgeRepository.save(new Bridge("BR002", "Green Valley Crossing", "Erode", "Suspension", 620, 2012, 18, BridgeStatus.WARNING));
        Bridge bridge3 = bridgeRepository.save(new Bridge("BR003", "North Industrial Link", "Madurai", "Beam", 420, 2009, 12, BridgeStatus.CRITICAL));

        Sensor sensor1 = sensorRepository.save(new Sensor("S-001", SensorType.CABLE_STRAIN, "Main Span North", 12.5, "mm", SensorStatus.ACTIVE, bridge1));
        Sensor sensor2 = sensorRepository.save(new Sensor("S-002", SensorType.VIBRATION, "Mid Span", 9.0, "Hz", SensorStatus.ACTIVE, bridge1));
        Sensor sensor3 = sensorRepository.save(new Sensor("S-003", SensorType.TILT, "West Tower", 4.8, "degrees", SensorStatus.ACTIVE, bridge2));
        Sensor sensor4 = sensorRepository.save(new Sensor("S-004", SensorType.LOAD, "Traffic Lane A", 18.0, "kN", SensorStatus.ACTIVE, bridge2));
        Sensor sensor5 = sensorRepository.save(new Sensor("S-005", SensorType.TEMPERATURE, "Deck Support", 32.0, "C", SensorStatus.ACTIVE, bridge3));
        Sensor sensor6 = sensorRepository.save(new Sensor("S-006", SensorType.DISPLACEMENT, "Expansion Joint", 7.5, "mm", SensorStatus.INACTIVE, bridge3));

        sensorReadingRepository.save(new SensorReading(sensor1, 10.2, LocalDateTime.now().minusHours(2), ReadingStatus.NORMAL));
        sensorReadingRepository.save(new SensorReading(sensor1, 16.8, LocalDateTime.now().minusHours(1), ReadingStatus.CRITICAL));
        sensorReadingRepository.save(new SensorReading(sensor2, 6.5, LocalDateTime.now().minusHours(3), ReadingStatus.NORMAL));
        sensorReadingRepository.save(new SensorReading(sensor2, 9.4, LocalDateTime.now().minusMinutes(30), ReadingStatus.WARNING));
        sensorReadingRepository.save(new SensorReading(sensor3, 2.9, LocalDateTime.now().minusHours(4), ReadingStatus.NORMAL));
        sensorReadingRepository.save(new SensorReading(sensor4, 17.1, LocalDateTime.now().minusHours(2), ReadingStatus.WARNING));
        sensorReadingRepository.save(new SensorReading(sensor5, 34.4, LocalDateTime.now().minusHours(5), ReadingStatus.WARNING));
        sensorReadingRepository.save(new SensorReading(sensor5, 41.2, LocalDateTime.now().minusMinutes(50), ReadingStatus.CRITICAL));
        sensorReadingRepository.save(new SensorReading(sensor6, 1.8, LocalDateTime.now().minusHours(6), ReadingStatus.NORMAL));

        alertRepository.save(new Alert(bridge1, sensor1, "Critical cable strain detected on Bridge BR001", AlertSeverity.CRITICAL, LocalDateTime.now().minusMinutes(45), AlertStatus.OPEN));
        alertRepository.save(new Alert(bridge3, sensor5, "Critical temperature detected on Bridge BR003", AlertSeverity.CRITICAL, LocalDateTime.now().minusMinutes(20), AlertStatus.OPEN));

        maintenanceRepository.save(new Maintenance(bridge1, "Inspect cable anchorage joints", MaintenancePriority.HIGH, "Bridge Team Alpha", MaintenanceStatus.PENDING, LocalDateTime.now().minusDays(2), null));
        maintenanceRepository.save(new Maintenance(bridge2, "Lubricate expansion joint bearings", MaintenancePriority.MEDIUM, "Structural Team 3", MaintenanceStatus.IN_PROGRESS, LocalDateTime.now().minusDays(1), null));
        maintenanceRepository.save(new Maintenance(bridge3, "Replace cracked deck support sensor", MaintenancePriority.HIGH, "Emergency Works Crew", MaintenanceStatus.PENDING, LocalDateTime.now().minusHours(5), null));

        List<Sensor> sensors = List.of(sensor1, sensor2, sensor3, sensor4, sensor5, sensor6);
        for (Sensor sensor : sensors) {
            sensor.getBridge().getSensors().add(sensor);
        }
        bridgeRepository.saveAll(List.of(bridge1, bridge2, bridge3));
    }
}
