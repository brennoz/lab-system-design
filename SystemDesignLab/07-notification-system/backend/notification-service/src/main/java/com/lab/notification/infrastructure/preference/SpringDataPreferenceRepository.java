package com.lab.notification.infrastructure.preference;

import com.lab.notification.domain.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPreferenceRepository extends JpaRepository<PreferenceJpaEntity, Long> {
    Optional<PreferenceJpaEntity> findByRecipientIdAndChannel(String recipientId, Channel channel);
}
