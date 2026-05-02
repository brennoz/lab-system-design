package com.lab.notification.domain.port;

import com.lab.notification.domain.model.InAppItem;

import java.util.List;

// Port — query side for in-app inbox; decouples API layer from SpringData infra
public interface InboxPort {
    List<InAppItem> findInbox(String recipientId);
}
