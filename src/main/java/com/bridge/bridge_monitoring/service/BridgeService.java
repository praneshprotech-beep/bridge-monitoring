package com.bridge.bridge_monitoring.service;

import com.bridge.bridge_monitoring.entity.Bridge;
import com.bridge.bridge_monitoring.entity.BridgeStatus;
import com.bridge.bridge_monitoring.repository.BridgeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BridgeService {

    private final BridgeRepository bridgeRepository;

    public BridgeService(BridgeRepository bridgeRepository) {
        this.bridgeRepository = bridgeRepository;
    }

    public List<Bridge> getAllBridges() {
        return bridgeRepository.findAll();
    }

    public Optional<Bridge> getBridgeById(Long id) {
        return bridgeRepository.findById(id);
    }

    public Bridge saveBridge(Bridge bridge) {
        if (bridge.getBridgeCode() == null || bridge.getBridgeCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Bridge code is required");
        }
        if (bridge.getBridgeName() == null || bridge.getBridgeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Bridge name is required");
        }
        if (bridge.getLocation() == null || bridge.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (bridge.getStatus() == null) {
            bridge.setStatus(BridgeStatus.SAFE);
        }
        return bridgeRepository.save(bridge);
    }

    public Bridge updateBridge(Long id, Bridge updatedBridge) {
        Bridge existingBridge = bridgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bridge not found with id: " + id));

        existingBridge.setBridgeCode(updatedBridge.getBridgeCode());
        existingBridge.setBridgeName(updatedBridge.getBridgeName());
        existingBridge.setLocation(updatedBridge.getLocation());
        existingBridge.setBridgeType(updatedBridge.getBridgeType());
        existingBridge.setLength(updatedBridge.getLength());
        existingBridge.setConstructionYear(updatedBridge.getConstructionYear());
        existingBridge.setNumberOfCables(updatedBridge.getNumberOfCables());
        if (updatedBridge.getStatus() != null) {
            existingBridge.setStatus(updatedBridge.getStatus());
        }

        return bridgeRepository.save(existingBridge);
    }

    public void deleteBridge(Long id) {
        if (!bridgeRepository.existsById(id)) {
            throw new IllegalArgumentException("Bridge not found with id: " + id);
        }
        bridgeRepository.deleteById(id);
    }

    public long countBridges() {
        return bridgeRepository.count();
    }

    public long countSafeBridges() {
        return bridgeRepository.findByStatus(BridgeStatus.SAFE).size();
    }

    public long countWarningBridges() {
        return bridgeRepository.findByStatus(BridgeStatus.WARNING).size();
    }

    public long countCriticalBridges() {
        return bridgeRepository.findByStatus(BridgeStatus.CRITICAL).size();
    }
}
