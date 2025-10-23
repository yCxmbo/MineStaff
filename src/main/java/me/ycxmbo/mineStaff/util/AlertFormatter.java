package me.ycxmbo.mineStaff.util;

import me.ycxmbo.mineStaff.MineStaff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class AlertFormatter {

    public static void broadcast(MineStaff plugin, String content, String tpTarget) {
        broadcast(plugin, content, tpTarget, true);
    }

    public static void broadcast(MineStaff plugin, String content, String tpTarget, boolean forward) {
        FileConfiguration cfg = plugin.getConfigManager().getConfig();

        boolean useMM = cfg.getBoolean("alerts.use_minimessage", false);
        String template = cfg.getString("alerts.template",
                "<dark_aqua>[StaffAlert]</dark_aqua> <white>{content}</white>");
        String hoverTemplate = cfg.getString("alerts.hover_template",
                "<green>Click to teleport to <yellow>{target}</yellow>");
        boolean clickTp = cfg.getBoolean("alerts.click_tp", true);
        String soundName = cfg.getString("alerts.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");

        Component base;
        if (useMM) {
            String applied = template.replace("{content}", content);
            base = MiniMessage.miniMessage().deserialize(applied);
            if (tpTarget != null && clickTp) {
                String hover = hoverTemplate.replace("{target}", tpTarget);
                base = base.hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize(hover)))
                           .clickEvent(ClickEvent.runCommand("/tp " + tpTarget));
            }
        } else {
            // Legacy path
            String prefixRaw = cfg.getString("alerts.prefix", "&3[StaffAlert]&r ");
            Component prefix = LegacyComponentSerializer.legacyAmpersand().deserialize(prefixRaw);
            base = prefix.append(Component.text(content));
            if (tpTarget != null && clickTp) {
                String hoverRaw = cfg.getString("alerts.hover_text", "&aClick to teleport to &e{target}").replace("{target}", tpTarget);
                Component hoverComp = LegacyComponentSerializer.legacyAmpersand().deserialize(hoverRaw);
                base = base.hoverEvent(HoverEvent.showText(hoverComp))
                           .clickEvent(ClickEvent.runCommand("/tp " + tpTarget));
            }
        }

        Sound s = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
        try { s = Sound.valueOf(soundName); } catch (IllegalArgumentException ignored) {}

        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.hasPermission("staffmode.alerts") || pl.hasPermission("staffmode.chat")) {
                pl.sendMessage(base);
                try { pl.playSound(pl.getLocation(), s, 0.6f, 1.2f); } catch (Throwable ignored) {}
            }
        }
        plugin.getLogger().info("[StaffAlert] " + content);
        try { plugin.getDiscordBridge().sendAlert(content); } catch (Throwable ignored) {}
        if (forward) {
            try { plugin.getProxyMessenger().sendStaffAlert(content, tpTarget); } catch (Throwable ignored) {}
            try { plugin.getRedisBridge().publishStaffAlert(content, tpTarget); } catch (Throwable ignored) {}
        }
    }
}
