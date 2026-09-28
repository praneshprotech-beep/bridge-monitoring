package com.bridge.bridge_monitoring.controller;

import com.bridge.bridge_monitoring.entity.Sensor;
import com.bridge.bridge_monitoring.service.SensorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping("/sensors")
    public List<Sensor> getAllSensors() {
        return sensorService.getAllSensors();
    }

    @GetMapping("/sensors/bridge/{bridgeId}")
    public List<Sensor> getSensorsByBridge(@PathVariable Long bridgeId) {
        return sensorService.getSensorsByBridge(bridgeId);
    }

    @GetMapping("/sensors/{id}")
    public ResponseEntity<Sensor> getSensorById(@PathVariable Long id) {
        return sensorService.getSensorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bridges/{bridgeId}/sensors")
    public ResponseEntity<Sensor> createSensor(@PathVariable Long bridgeId, @RequestBody Sensor sensor) {
        try {
            Sensor savedSensor = sensorService.saveSensor(sensor, bridgeId);
            return new ResponseEntity<>(savedSensor, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/sensors/{id}")
    public ResponseEntity<Sensor> updateSensor(@PathVariable Long id, @RequestBody Sensor sensor) {
        try {
            Sensor updatedSensor = sensorService.updateSensor(id, sensor);
            return ResponseEntity.ok(updatedSensor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/sensors/{id}")
    public ResponseEntity<String> deleteSensor(@PathVariable Long id) {
        try {
            sensorService.deleteSensor(id);
            return ResponseEntity.ok("Sensor deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
