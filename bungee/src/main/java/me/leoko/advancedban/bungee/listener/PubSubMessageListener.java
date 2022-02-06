package me.leoko.advancedban.bungee.listener;

import lombok.Getter;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.BungeeMain;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author Beelzebu
 */
public class PubSubMessageListener extends JedisPubSub {

    private static final MethodInterface mi = Universal.get().getMethods();
    @Getter
    private static final HashMap<String, Consumer<Boolean>> findFoundMap = new HashMap<>();

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
        } else if (channel.equals("advancedban:findplayer:v1")) {
            String command = message.split(" ")[0];
            String username = message.split(" ")[1];
            String id = message.split(" ")[2];
            if (command.equals("find") && ProxyServer.getInstance().getPlayer(username) != null) {
                try (Jedis jedis = BungeeMain.getInstance().getJedisPool().getResource()) {
                    jedis.publish("advancedban:findplayer:v1", String.format("found %s %s", username, id));
                }
            } else if (command.equals("found") && findFoundMap.containsKey(id)) {
                findFoundMap.get(id).accept(true);
                findFoundMap.remove(id);
            }
        }
    }
}