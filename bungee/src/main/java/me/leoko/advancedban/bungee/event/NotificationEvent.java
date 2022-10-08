package me.leoko.advancedban.bungee.event;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event fired when a notification is sent
 */
@Getter
public class NotificationEvent extends Event {
    private final ProxiedPlayer receivingPlayer;
    private final String message;
    @Setter private boolean cancelled;

    public NotificationEvent(ProxiedPlayer receivingPlayer, String message) {
        this.receivingPlayer = receivingPlayer;
        this.message = message;
    }
}
