package com.example.usageservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;


public interface DatabaseRepository extends JpaRepository<EnergyDataEntity, LocalDateTime> {
    EnergyDataEntity findByHour(Date hour);
}
