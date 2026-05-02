package com.lab.notification.infrastructure.inapp;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataInAppRepository extends JpaRepository<InAppNotificationJpaEntity, UUID> {

    List<InAppNotificationJpaEntity> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
}
