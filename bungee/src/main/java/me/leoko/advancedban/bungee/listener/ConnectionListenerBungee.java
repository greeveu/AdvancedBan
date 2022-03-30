package me.leoko.advancedban.bungee.listener;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.BungeeMain;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Created by Leoko @ dev.skamps.eu on 24.07.2016.
 */
public class ConnectionListenerBungee implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW)
    public void onConnection(LoginEvent event) {
        if (event.isCancelled())
            return;

        UUIDManager.get().supplyInternUUID(event.getConnection().getName(), event.getConnection().getUniqueId());
        event.registerIntent((BungeeMain) Universal.get().getMethods().getPlugin());
        Universal.get().getMethods().runAsync(() -> {
            String result = Universal.get().callConnection(event.getConnection().getName(), event.getConnection().getAddress().getAddress().getHostAddress());

            if (result != null) {
                if (BungeeMain.getCloudSupport() == null) {
                    event.setCancelled(true);
                    event.setCancelReason(result);
                } else {
                    BungeeMain.getCloudSupport().kick(event.getConnection().getUniqueId(), result);
                }
            }

            if (Universal.isRedis()) {
                Universal.get().getMethods().runAsync(() -> {
                    RedisBungee.getApi().sendChannelMessage("advancedban:connection:v1", event.getConnection().getName() + "," + event.getConnection().getAddress().getAddress().getHostAddress());
                });
            }
            event.completeIntent((BungeeMain) Universal.get().getMethods().getPlugin());
        });
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        Universal.get().getMethods().runAsync(() -> {
            if (event.getPlayer() != null) {
                PunishmentManager.get().discard(event.getPlayer().getName());
            }
        });
    }
}