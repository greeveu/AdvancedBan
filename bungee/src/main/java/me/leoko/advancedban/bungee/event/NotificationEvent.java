package me.leoko.advancedban.bungee.event;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event fired when a notification is sent
 */
public class NotificationEvent extends Event {
    private final ProxiedPlayer receivingPlayer;
    private final String message;
    private boolean cancelled;

    public NotificationEvent(ProxiedPlayer receivingPlayer, String message) {
        this.receivingPlayer = receivingPlayer;
        this.message = message;
    }

    public ProxiedPlayer getReceivingPlayer() {
        return receivingPlayer;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}