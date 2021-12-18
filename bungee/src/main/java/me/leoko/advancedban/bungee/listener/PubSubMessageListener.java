package me.leoko.advancedban.bungee.listener;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

/**
 * @author Beelzebu
 */
public class PubSubMessageListener extends JedisPubSub {

    private static final MethodInterface mi = Universal.get().getMethods();

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("advancedban:main:v1")) {
            String[] msg = message.split(" ");
            if (message.startsWith("kick ")) {
                if (ProxyServer.getInstance().getPlayer(msg[1]) != null) {
                    ProxyServer.getInstance().getPlayer(msg[1]).disconnect(message.substring((msg[0] + msg[1]).length() + 2));
                }
            } else if (message.startsWith("notification ")) {
                for (ProxiedPlayer pp : ProxyServer.getInstance().getPlayers()) {
                    if (mi.hasPerms(pp, msg[1])) {
                        mi.sendMessage(pp, message.substring((msg[0] + msg[1]).length() + 2));
                    }
                }
            } else if (message.startsWith("message ")) {
                if (ProxyServer.getInstance().getPlayer(msg[1]) != null) {
                    ProxyServer.getInstance().getPlayer(msg[1]).sendMessage(message.substring((msg[0] + msg[1]).length() + 2));
                }
                if (msg[1].equalsIgnoreCase("CONSOLE")) {
                    ProxyServer.getInstance().getConsole().sendMessage(message.substring((msg[0] + msg[1]).length() + 2));
                }
            }
        } else if (channel.equals("advancedban:connection:v1")) {
            String[] msg = message.split(",");
            Universal.get().getIps().remove(msg[0].toLowerCase());
            Universal.get().getIps().put(msg[0].toLowerCase(), msg[1]);
        }
    }
}