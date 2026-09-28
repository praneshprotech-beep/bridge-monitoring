package com.bridge.bridge_monitoring.controller;

import com.bridge.bridge_monitoring.entity.SensorReading;
import com.bridge.bridge_monitoring.service.SensorReadingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SensorReadingController {

    private final SensorReadingService sensorReadingService;

    public SensorReadingController(SensorReadingService sensorReadingService) {
        this.sensorReadingService = sensorReadingService;
    }

    @GetMapping("/readings")
    public List<SensorReading> getAllReadings() {
        return sensorReadingService.getAllReadings();
    }

    @GetMapping("/readings/{id}")
    public ResponseEntity<SensorReading> getReadingById(@PathVariable Long id) {
        return sensorReadingService.getReadingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sensors/{sensorId}/readings")
    public ResponseEntity<SensorReading> createReading(@PathVariable Long sensorId, @RequestParam double value) {
        try {
            SensorReading savedReading = sensorReadingService.saveReading(sensorId, value);
            return new ResponseEntity<>(savedReading, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/readings/{id}")
    public ResponseEntity<SensorReading> updateReading(@PathVariable Long id, @RequestBody SensorReading reading) {
        try {
            SensorReading updatedReading = sensorReadingService.updateReading(id, reading);
            return ResponseEntity.ok(updatedReading);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/readings/{id}")
    public ResponseEntity<String> deleteReading(@PathVariable Long id) {
        try {
            sensorReadingService.deleteReading(id);
            return ResponseEntity.ok("Reading deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
