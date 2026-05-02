package com.lab.notification.api;

import com.lab.notification.api.dto.PreferenceRequest;
import com.lab.notification.application.usecase.SendNotificationUseCase;
import com.lab.notification.domain.model.RecipientPreference;
import com.lab.notification.domain.port.PreferencePort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
public class PreferenceController {

    private final PreferencePort preferencePort;

    public PreferenceController(PreferencePort preferencePort) {
        this.preferencePort = preferencePort;
    }

    @PutMapping("/{recipientId}")
    public void update(@PathVariable String recipientId, @RequestBody PreferenceRequest req) {
        preferencePort.save(new RecipientPreference(recipientId, req.channel(), req.optedOut()));
    }
}
