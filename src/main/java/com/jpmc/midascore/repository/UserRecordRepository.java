// File: UserRecordRepository.java
package com.jpmc.midascore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpmc.midascore.entity.UserRecord;

public interface UserRecordRepository extends JpaRepository<UserRecord, Long> {
    Optional<UserRecord> findByName(String name);
}

