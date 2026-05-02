package com.lab.notification.domain.port;

import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.RecipientPreference;

// Pattern: Repository port — per-recipient, per-channel opt-out preferences
public interface PreferencePort {
    boolean isOptedOut(String recipientId, Channel channel);
    void save(RecipientPreference preference);
}
