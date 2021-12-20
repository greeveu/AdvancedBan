package me.leoko.advancedban.bungee;

import lombok.Getter;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.bungee.cloud.CloudSupport;
import me.leoko.advancedban.bungee.cloud.CloudSupportHandler;
import me.leoko.advancedban.bungee.listener.ChatListenerBungee;
import me.leoko.advancedban.bungee.listener.ConnectionListenerBungee;
import me.leoko.advancedban.bungee.listener.InternalListener;
import me.leoko.advancedban.bungee.listener.PubSubMessageListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class BungeeMain extends Plugin {

    @Getter
    private static BungeeMain instance;

    @Getter
    private JedisPool jedisPool;

    @Getter
    private static CloudSupport cloudSupport;

    @Override
    public void onEnable() {
        instance = this;
        Universal.get().setup(new BungeeMethods());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ConnectionListenerBungee());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new ChatListenerBungee());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new InternalListener());
        ProxyServer.getInstance().registerChannel("advancedban:main:v1");

        cloudSupport = CloudSupportHandler.getCloudSystem();

        MethodInterface methods = Universal.get().getMethods();

        if (methods.getBoolean(methods.getConfig(), "redis.enabled", false)) {
            jedisPool = new JedisPool(new JedisPoolConfig(),
                    methods.getString(methods.getConfig(), "redis.host", "localhost"),
                    methods.getInteger(methods.getConfig(), "redis.port", 6379),
                    methods.getInteger(methods.getConfig(), "redis.timeout", 5000),
                    methods.getString(methods.getConfig(), "redis.password", null)
            );

            Universal.setRedis(true);

            new Thread(() -> {
                JedisPubSub jedisPubSub = new PubSubMessageListener();
                //I need a new jedis instance here since redis only allows the instance to publish or to listen but not to do both at the same time
                jedisPool.getResource().subscribe(jedisPubSub, "advancedban:main:v1", "advancedban:connection:v1", "advancedban:findplayer:v1");
            }).start();

            Universal.get().log("Redis enabled, hooking into it!");
        }
    }

    @Override
    public void onDisable() {
        Universal.get().shutdown();
    }
}