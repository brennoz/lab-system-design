package com.lab.notification.infrastructure.preference;

import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.RecipientPreference;
import com.lab.notification.domain.port.PreferencePort;

// Pattern: Repository adapter — upserts preferences; defaults to opted-in when no record exists
public class JpaPreferenceAdapter implements PreferencePort {

    private final SpringDataPreferenceRepository delegate;

    public JpaPreferenceAdapter(SpringDataPreferenceRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isOptedOut(String recipientId, Channel channel) {
        return delegate.findByRecipientIdAndChannel(recipientId, channel)
                .map(PreferenceJpaEntity::isOptedOut)
                .orElse(false);
    }

    @Override
    public void save(RecipientPreference preference) {
        PreferenceJpaEntity entity = delegate
                .findByRecipientIdAndChannel(preference.recipientId(), preference.channel())
                .orElse(new PreferenceJpaEntity(preference.recipientId(), preference.channel(), preference.optedOut()));
        entity.setOptedOut(preference.optedOut());
        delegate.save(entity);
    }
}
