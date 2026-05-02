package com.lab.notification.api;

import com.lab.notification.api.dto.InAppNotificationResponse;
import com.lab.notification.api.dto.SendNotificationRequest;
import com.lab.notification.application.usecase.SendNotificationUseCase;
import com.lab.notification.domain.port.InboxPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final SendNotificationUseCase sendUseCase;
    private final InboxPort inboxPort;

    public NotificationController(SendNotificationUseCase sendUseCase, InboxPort inboxPort) {
        this.sendUseCase = sendUseCase;
        this.inboxPort = inboxPort;
    }

    // 202 Accepted — fan-out is async; tasks are enqueued, not yet delivered
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@RequestBody SendNotificationRequest req) {
        sendUseCase.send(req.type(), req.recipients(), req.channel(), req.priority(), req.payload());
    }

    @GetMapping("/inbox/{recipientId}")
    public List<InAppNotificationResponse> inbox(@PathVariable String recipientId) {
        return inboxPort.findInbox(recipientId)
                .stream()
                .map(item -> new InAppNotificationResponse(item.id(), item.payload(), item.createdAt()))
                .toList();
    }
}
