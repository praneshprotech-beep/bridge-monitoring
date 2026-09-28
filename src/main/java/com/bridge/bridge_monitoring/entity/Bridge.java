package com.bridge.bridge_monitoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bridges")
public class Bridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bridgeCode;

    @Column(nullable = false)
    private String bridgeName;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String bridgeType;

    private double length;

    private int constructionYear;

    private int numberOfCables;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BridgeStatus status = BridgeStatus.SAFE;

    @OneToMany(mappedBy = "bridge", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Sensor> sensors = new ArrayList<>();

    public Bridge() {
    }

    public Bridge(String bridgeCode, String bridgeName, String location, String bridgeType,
                  double length, int constructionYear, int numberOfCables, BridgeStatus status) {
        this.bridgeCode = bridgeCode;
        this.bridgeName = bridgeName;
        this.location = location;
        this.bridgeType = bridgeType;
        this.length = length;
        this.constructionYear = constructionYear;
        this.numberOfCables = numberOfCables;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBridgeCode() {
        return bridgeCode;
    }

    public void setBridgeCode(String bridgeCode) {
        this.bridgeCode = bridgeCode;
    }

    public String getBridgeName() {
        return bridgeName;
    }

    public void setBridgeName(String bridgeName) {
        this.bridgeName = bridgeName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBridgeType() {
        return bridgeType;
    }

    public void setBridgeType(String bridgeType) {
        this.bridgeType = bridgeType;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getConstructionYear() {
        return constructionYear;
    }

    public void setConstructionYear(int constructionYear) {
        this.constructionYear = constructionYear;
    }

    public int getNumberOfCables() {
        return numberOfCables;
    }

    public void setNumberOfCables(int numberOfCables) {
        this.numberOfCables = numberOfCables;
    }

    public BridgeStatus getStatus() {
        return status;
    }

    public void setStatus(BridgeStatus status) {
        this.status = status;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }
}
