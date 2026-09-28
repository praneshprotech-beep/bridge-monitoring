package com.bridge.bridge_monitoring.controller;

import com.bridge.bridge_monitoring.entity.Bridge;
import com.bridge.bridge_monitoring.service.BridgeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BridgeController {

    private final BridgeService bridgeService;

    public BridgeController(BridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @GetMapping("/bridges")
    public List<Bridge> getAllBridges() {
        return bridgeService.getAllBridges();
    }

    @GetMapping("/bridges/{id}")
    public ResponseEntity<Bridge> getBridgeById(@PathVariable Long id) {
        return bridgeService.getBridgeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bridges")
    public ResponseEntity<Bridge> createBridge(@RequestBody Bridge bridge) {
        try {
            Bridge savedBridge = bridgeService.saveBridge(bridge);
            return new ResponseEntity<>(savedBridge, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/bridges/{id}")
    public ResponseEntity<Bridge> updateBridge(@PathVariable Long id, @RequestBody Bridge bridge) {
        try {
            Bridge updatedBridge = bridgeService.updateBridge(id, bridge);
            return ResponseEntity.ok(updatedBridge);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/bridges/{id}")
    public ResponseEntity<String> deleteBridge(@PathVariable Long id) {
        try {
            bridgeService.deleteBridge(id);
            return ResponseEntity.ok("Bridge deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
