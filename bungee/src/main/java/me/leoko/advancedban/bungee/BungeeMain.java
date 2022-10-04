package me.leoko.advancedban.bungee;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
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

public class BungeeMain extends Plugin {

    @Getter
    private static BungeeMain instance;

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

        if (ProxyServer.getInstance().getPluginManager().getPlugin("RedisBungee") != null) {
            Universal.setRedis(true);
            ProxyServer.getInstance().getPluginManager().registerListener(this, new PubSubMessageListener());
            RedisBungee.getApi().registerPubSubChannels("advancedban:main:v1", "advancedban:connection:v1", "advancedban:findplayer:v1");
            Universal.get().log("RedisBungee detected, hooking into it!");
        }
    }

    @Override
    public void onDisable() {
        Universal.get().shutdown();
    }
}
