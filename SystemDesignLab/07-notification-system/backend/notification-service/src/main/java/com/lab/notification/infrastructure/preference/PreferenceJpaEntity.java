package com.lab.notification.infrastructure.preference;

import com.lab.notification.domain.model.Channel;
import jakarta.persistence.*;

// Pattern: JPA Entity — maps RecipientPreference to recipient_preferences table
@Entity
@Table(name = "recipient_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"recipientId", "channel"}))
public class PreferenceJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private boolean optedOut;

    protected PreferenceJpaEntity() {}

    public PreferenceJpaEntity(String recipientId, Channel channel, boolean optedOut) {
        this.recipientId = recipientId;
        this.channel = channel;
        this.optedOut = optedOut;
    }

    public String getRecipientId() { return recipientId; }
    public Channel getChannel() { return channel; }
    public boolean isOptedOut() { return optedOut; }
    public void setOptedOut(boolean optedOut) { this.optedOut = optedOut; }
}
