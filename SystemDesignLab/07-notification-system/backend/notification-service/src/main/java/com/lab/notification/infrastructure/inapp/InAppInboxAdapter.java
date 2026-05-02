package com.lab.notification.infrastructure.inapp;

import com.lab.notification.domain.model.InAppItem;
import com.lab.notification.domain.port.InboxPort;

import java.util.List;

// Pattern: Repository adapter — maps JPA entities to domain InAppItem for inbox queries
public class InAppInboxAdapter implements InboxPort {

    private final SpringDataInAppRepository delegate;

    public InAppInboxAdapter(SpringDataInAppRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<InAppItem> findInbox(String recipientId) {
        return delegate.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(e -> new InAppItem(e.getId(), e.getPayload(), e.getCreatedAt()))
                .toList();
    }
}
