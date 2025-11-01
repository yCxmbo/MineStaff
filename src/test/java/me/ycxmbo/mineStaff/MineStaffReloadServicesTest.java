package me.ycxmbo.mineStaff;

import me.ycxmbo.mineStaff.managers.ConfigManager;
import me.ycxmbo.mineStaff.messaging.ProxyMessenger;
import me.ycxmbo.mineStaff.messaging.RedisBridge;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.messaging.Messenger;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class MineStaffReloadServicesTest {

    @Test
    void reloadServicesReflectConfigToggles() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.set("discord.enabled", true);
        config.set("discord.alerts_webhook", "https://example.com/alerts");
        config.set("discord.staffchat_webhook", "https://example.com/staff");
        config.set("redis.enabled", true);
        config.set("redis.host", "localhost");
        config.set("redis.port", 6379);
        config.set("redis.channels.staffchat", "test:sc");
        config.set("redis.channels.reports", "test:reports");
        config.set("redis.channels.alerts", "test:alerts");

        ConfigManager configManager = Mockito.mock(ConfigManager.class);
        Mockito.when(configManager.getConfig()).thenReturn(config);

        MineStaff plugin = Mockito.mock(MineStaff.class, Answers.CALLS_REAL_METHODS);
        Mockito.when(plugin.getConfigManager()).thenReturn(configManager);
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("MineStaffTest"));

        ProxyMessenger oldProxy = Mockito.mock(ProxyMessenger.class);
        RedisBridge oldRedis = Mockito.mock(RedisBridge.class);
        setField(plugin, "proxyMessenger", oldProxy);
        setField(plugin, "redisBridge", oldRedis);
        setField(plugin, "discordBridge", Mockito.mock(me.ycxmbo.mineStaff.messaging.DiscordBridge.class));

        Messenger messenger = Mockito.mock(Messenger.class);

        try (MockedStatic<Bukkit> mockedBukkit = Mockito.mockStatic(Bukkit.class)) {
            mockedBukkit.when(Bukkit::getMessenger).thenReturn(messenger);

            plugin.reloadConfigDrivenServices();

            Mockito.verify(oldProxy).close();
            Mockito.verify(oldRedis).close();

            me.ycxmbo.mineStaff.messaging.DiscordBridge discordBridge = plugin.getDiscordBridge();
            RedisBridge redisBridge = plugin.getRedisBridge();
            ProxyMessenger proxyMessenger = plugin.getProxyMessenger();

            assertNotNull(discordBridge);
            assertNotNull(redisBridge);
            assertNotNull(proxyMessenger);

            assertTrue(discordBridge.isEnabled());
            assertTrue(redisBridge.isInitialized());

            Mockito.verify(messenger).registerOutgoingPluginChannel(plugin, "BungeeCord");
            Mockito.verify(messenger).registerOutgoingPluginChannel(plugin, "minestaff:staff");
            Mockito.verify(messenger).registerIncomingPluginChannel(plugin, "minestaff:staff", proxyMessenger);

            config.set("discord.enabled", false);
            config.set("redis.enabled", false);

            ProxyMessenger previousProxy = plugin.getProxyMessenger();
            RedisBridge previousRedis = plugin.getRedisBridge();

            Mockito.reset(messenger);

            plugin.reloadConfigDrivenServices();

            Mockito.verify(messenger).unregisterIncomingPluginChannel(plugin, "minestaff:staff", previousProxy);
            Mockito.verify(messenger).unregisterOutgoingPluginChannel(plugin, "BungeeCord");
            Mockito.verify(messenger).unregisterOutgoingPluginChannel(plugin, "minestaff:staff");
            Mockito.verify(messenger).registerOutgoingPluginChannel(plugin, "BungeeCord");
            Mockito.verify(messenger).registerOutgoingPluginChannel(plugin, "minestaff:staff");
            Mockito.verify(messenger).registerIncomingPluginChannel(plugin, "minestaff:staff", plugin.getProxyMessenger());

            assertFalse(plugin.getDiscordBridge().isEnabled());
            assertFalse(plugin.getRedisBridge().isInitialized());
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        var field = MineStaff.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
