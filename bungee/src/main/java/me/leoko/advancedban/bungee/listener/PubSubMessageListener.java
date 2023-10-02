package me.leoko.advancedban.bungee.listener;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import lombok.Getter;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * @author Beelzebu
 */
public class PubSubMessageListener implements Listener {

    private static final MethodInterface mi = Universal.get().getMethods();
    @Getter
    private static final HashMap<String, Consumer<Boolean>> findFoundMap = new HashMap<>();

    @EventHandler
    public void onMessageReceive(PubSubMessageEvent event) {
        String channel = event.getChannel();
        String message = event.getMessage();
        switch (channel) {
            case "advancedban:main:v1": {
                String[] msg = message.split(" ");
                if (message.startsWith("kick ")) {
                    if (ProxyServer.getInstance().getPlayer(msg[1]) != null) {
                        ProxyServer.getInstance().getPlayer(msg[1]).disconnect(new ComponentBuilder(message.substring((msg[0] + msg[1]).length() + 2)).create());
                    }
                } else if (message.startsWith("notification ")) {
                    for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers()) {
                        if (mi.hasPerms(pp, msg[1])) {
                            mi.sendMessage(pp, message.substring((msg[0] + msg[1]).length() + 2));
                        }
                    }
                } else if (message.startsWith("message ")) {
                    if (ProxyServer.getInstance().getPlayer(msg[1]) != null) {
                        ProxyServer.getInstance().getPlayer(msg[1]).sendMessage(new ComponentBuilder(message.substring((msg[0] + msg[1]).length() + 2)).create());
                    }
                    if (msg[1].equalsIgnoreCase("CONSOLE")) {
                        ProxyServer.getInstance().getConsole().sendMessage(new ComponentBuilder(message.substring((msg[0] + msg[1]).length() + 2)).create());
                    }
                } else if (message.startsWith("refresh ")) {
                    ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(UUID.fromString(addUUIDDashes(msg[1])));
                    if (proxiedPlayer != null) {
                        Universal.get().refreshUserData(proxiedPlayer.getName(), proxiedPlayer.getUniqueId().toString(), proxiedPlayer.getPendingConnection().getAddress().getAddress().getHostAddress());
                    }
                }
                break;
            }
            case "advancedban:connection:v1": {
                String[] msg = message.split(",");
                Universal.get().getIps().remove(msg[0].toLowerCase());
                Universal.get().getIps().put(msg[0].toLowerCase(), msg[1]);
                break;
            }
            case "advancedban:findplayer:v1": {
                String[] msg = message.split(" ");
                String command = msg[0];
                String username = msg[1];
                String id = msg[2];
                if (command.equals("find") && ProxyServer.getInstance().getPlayer(username) != null) {
                    RedisBungee.getApi().sendChannelMessage("advancedban:findplayer:v1", String.format("found %s %s", username, id));
                } else if (command.equals("found") && findFoundMap.containsKey(id)) {
                    findFoundMap.get(id).accept(true);
                    findFoundMap.remove(id);
                }
                break;
            }
            default:
                ProxyServer.getInstance().getLogger().log(Level.FINE, "Received message on unknown channel: {0}", channel);
                break;
        }
    }

    private String addUUIDDashes(String idNoDashes) {
        StringBuffer idBuff = new StringBuffer(idNoDashes);
        idBuff.insert(20, '-');
        idBuff.insert(16, '-');
        idBuff.insert(12, '-');
        idBuff.insert(8, '-');
        return idBuff.toString();
    }
}
