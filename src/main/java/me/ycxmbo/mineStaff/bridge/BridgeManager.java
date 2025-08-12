package me.ycxmbo.mineStaff.bridge;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.util.AlertFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BridgeManager {

    public static void initialize(MineStaff plugin) {
        tryVulcan(plugin);
        tryLiteBans(plugin);
    }

    @SuppressWarnings("unchecked")
    private static void tryVulcan(MineStaff plugin) {
        Plugin p = Bukkit.getPluginManager().getPlugin("Vulcan");
        if (p == null || !p.isEnabled()) return;

        // Try a few common/legacy event class names
        List<String> eventCandidates = Arrays.asList(
                "me.frep.vulcan.api.event.VulcanFlagEvent",
                "me.frep.vulcan.api.event.FlagEvent",
                "com.octavials.vulcan.api.event.FlagEvent"
        );

        for (String name : eventCandidates) {
            try {
                Class<? extends Event> clazz = (Class<? extends Event>) Class.forName(name);
                Bukkit.getPluginManager().registerEvent(
                        clazz,
                        new Listener() {},
                        EventPriority.MONITOR,
                        (EventExecutor) (listener, event) -> {
                            try {
                                handleVulcanFlag(plugin, event);
                            } catch (Throwable ignored) {}
                        },
                        plugin,
                        true
                );
                plugin.getLogger().info("[Bridge] Hooked Vulcan via " + name);
                return;
            } catch (Throwable ignored) {
                // try next candidate
            }
        }
        plugin.getLogger().warning("[Bridge] Vulcan present but no compatible event class found.");
    }

    private static void handleVulcanFlag(MineStaff plugin, Object event) {
        try {
            Method getPlayer = event.getClass().getMethod("getPlayer");
            Object playerObj = getPlayer.invoke(event);
            if (!(playerObj instanceof Player player)) return;

            String check = tryGet(event, "getCheckName", obj -> (String) obj);
            if (check == null) {
                Object c = tryGetObj(event, "getCheck");
                if (c != null) {
                    String byName = tryGet(c, "getName", obj -> (String) obj);
                    if (byName != null) check = byName;
                }
            }
            String type = tryGet(event, "getType", String::valueOf);
            Number vl = tryNum(event, "getVl");
            if (vl == null) vl = tryNum(event, "getViolations");

            String msg = "Vulcan flag: " + player.getName()
                    + (check != null ? (" [" + check + "]") : "")
                    + (type != null ? (" type=" + type) : "")
                    + (vl != null ? (" VL=" + vl) : "");

            broadcastToStaff(plugin, msg, player.getName());
        } catch (Throwable ignored) {
            // swallow reflection hiccups
        }
    }

    @SuppressWarnings("unchecked")
    private static void tryLiteBans(MineStaff plugin) {
        Plugin p = Bukkit.getPluginManager().getPlugin("LiteBans");
        if (p == null || !p.isEnabled()) return;

        List<String> events = Arrays.asList(
                "litebans.api.event.PunishmentExecuteEvent",
                "litebans.api.event.PunishmentEvent"
        );

        boolean hooked = false;
        for (String name : events) {
            try {
                Class<? extends Event> clazz = (Class<? extends Event>) Class.forName(name);
                Bukkit.getPluginManager().registerEvent(
                        clazz,
                        new Listener() {},
                        EventPriority.MONITOR,
                        (EventExecutor) (listener, event) -> {
                            try {
                                handleLiteBans(plugin, event);
                            } catch (Throwable ignored) {}
                        },
                        plugin,
                        true
                );
                plugin.getLogger().info("[Bridge] Hooked LiteBans via " + name);
                hooked = true;
                break;
            } catch (Throwable ignored) {
                // try next
            }
        }
        if (!hooked) {
            plugin.getLogger().warning("[Bridge] LiteBans present but API events not found; fallback to command sniffing remains active.");
        }
    }

    private static void handleLiteBans(MineStaff plugin, Object event) {
        try {
            String type = tryGet(event, "getType", String::valueOf);
            String reason = tryGet(event, "getReason", obj -> (String) obj);
            String actor = tryGet(event, "getExecutorName", obj -> (String) obj);
            String target = tryGet(event, "getVictimName", obj -> (String) obj);

            String msg = "LiteBans " + (type != null ? type : "Punishment") + ": "
                    + (target != null ? target : "unknown")
                    + " by " + (actor != null ? actor : "console")
                    + (reason != null && !reason.isEmpty() ? (" | " + reason) : "");

            broadcastToStaff(plugin, msg, target);
        } catch (Throwable ignored) {
            // swallow reflection hiccups
        }
    }

    // ---------- helpers ----------

    private static <T> T tryGet(Object obj, String method, Function<Object, T> cast) {
        try {
            Method m = obj.getClass().getMethod(method);
            Object r = m.invoke(obj);
            return cast.apply(r);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Object tryGetObj(Object obj, String method) {
        try {
            Method m = obj.getClass().getMethod(method);
            return m.invoke(obj);
        } catch (Throwable e) {
            return null;
        }
    }

    private static Number tryNum(Object obj, String method) {
        try {
            Method m = obj.getClass().getMethod(method);
            Object r = m.invoke(obj);
            if (r instanceof Number n) return n;
            return null;
        } catch (Throwable e) {
            return null;
        }
    }

    private static void broadcastToStaff(MineStaff plugin, String message, String tpTarget) {
        AlertFormatter.broadcast(plugin, message, tpTarget);
    }
}
