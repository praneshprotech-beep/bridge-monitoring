package com.bridge.bridge_monitoring.repository;

import com.bridge.bridge_monitoring.entity.Bridge;
import com.bridge.bridge_monitoring.entity.BridgeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BridgeRepository extends JpaRepository<Bridge, Long> {
    Optional<Bridge> findByBridgeCode(String bridgeCode);
    List<Bridge> findByStatus(BridgeStatus status);
}
