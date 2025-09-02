package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import me.ycxmbo.mineStaff.api.events.FreezeToggleEvent;
import me.ycxmbo.mineStaff.api.events.VanishToggleEvent;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.tools.ToolManager;
import me.ycxmbo.mineStaff.util.CooldownManager;
import me.ycxmbo.mineStaff.util.SoundUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

public class ToolListener implements Listener {
    private final MineStaff plugin;
    private final StaffDataManager data;
    private final CooldownManager cooldowns = new CooldownManager();

    public ToolListener(MineStaff plugin) {
        this.plugin = plugin;
        this.data = plugin.getStaffDataManager();
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        ItemStack it = e.getItem();
        if (it == null) return;
        if (!data.isStaffMode(p)) return;

        boolean right = e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK;

        if (it.getType() == ToolManager.TELEPORT_TOOL && p.hasPermission("staffmode.teleport")) {
            if (right) {
                e.setCancelled(true);
                useTeleport(p,  p.isSneaking() ? 120 : 60);
            }
            return;
        }

        if (it.getType() == ToolManager.RANDOMTP_TOOL && p.hasPermission("staffmode.randomtp")) {
            if (right) {
                e.setCancelled(true);
                useRandomTp(p);
            }
            return;
        }
        if ((it.getType() == Material.LIME_DYE || it.getType() == Material.LIGHT_GRAY_DYE) && p.hasPermission("staffmode.vanish")) {
            if (right) {
                e.setCancelled(true);
                toggleVanish(p);
            }
            return;
        }
    }

    @EventHandler
    public void onUseOnPlayer(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Player target)) return;
        Player p = e.getPlayer();
        if (!data.isStaffMode(p)) return;
        ItemStack it = p.getInventory().getItemInMainHand();
        if (it == null) return;

        if (it.getType() == ToolManager.FREEZE_TOOL && p.hasPermission("staffmode.freeze")) {
            e.setCancelled(true);
            boolean state = !data.isFrozen(target);
            data.setFrozen(target, state);
            // API event
            org.bukkit.Bukkit.getPluginManager().callEvent(new FreezeToggleEvent(target, state, MineStaffAPI.ToggleCause.TOOL));

            p.sendMessage(ChatColor.YELLOW + "Player " + target.getName() + " " + (state ? "frozen." : "unfrozen."));
            if (state) target.sendMessage(ChatColor.RED + "You have been frozen by staff. Do not log out.");
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 40, 0.6, 0.8, 0.6);
            SoundUtil.playFreeze(p);
            MineStaff.getInstance().getAuditLogger().log(java.util.Map.of(
                    "type","freeze","actor",p.getUniqueId().toString(),
                    "target",target.getUniqueId().toString(),
                    "state",String.valueOf(state),
                    "cause","tool"
            ));
            return;
        }

        if (it.getType() == ToolManager.INSPECT_TOOL && p.hasPermission("staffmode.inspect")) {
            e.setCancelled(true);
            plugin.getInspectorGUI().open(p, target);
            target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5);
            SoundUtil.playInspectSound(p);
        }
    }

    private void toggleVanish(Player p) {
        boolean newState = !data.isVanished(p);
        data.setVanished(p, newState);

        // API + effects
        org.bukkit.Bukkit.getPluginManager().callEvent(new VanishToggleEvent(p, newState, MineStaffAPI.ToggleCause.TOOL));
        String key = newState ? "vanish_on" : "vanish_off";
        String raw = plugin.getConfigManager().getConfig().getString("messages." + key, newState ? "&dVanish enabled." : "&dVanish disabled.");
        p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', raw));
    }

    private void useTeleport(Player p, int maxRange) {
        String key = "teleport";
        if (!cooldowns.ready(p.getUniqueId(), key)) {
            long ms = cooldowns.remaining(p.getUniqueId(), key);
            String templ = plugin.getConfigManager().getConfig().getString("messages.teleport_cooldown", "&cTeleport cooldown: {seconds}s");
            String msg = templ.replace("{seconds}", String.format(java.util.Locale.US, "%.1f", ms / 1000.0));
            p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
            return;
        }
        int range = plugin.getConfigManager().getConfig().getInt("options.teleport_max_range", 60);
        int rangeSneak = plugin.getConfigManager().getConfig().getInt("options.teleport_max_range_sneak", 120);
        int usedRange = p.isSneaking() ? rangeSneak : range;
        Location eye = p.getEyeLocation();
        BlockIterator it = new BlockIterator(p.getWorld(), eye.toVector(), eye.getDirection(), 0, usedRange);
        Block last = null;
        while (it.hasNext()) {
            Block b = it.next();
            if (b.getType().isSolid()) { last = b; break; }
            last = b;
        }
        if (last == null) {
            String raw = plugin.getConfigManager().getConfig().getString("messages.teleport_no_spot", "&cNo safe spot in sight.");
            p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', raw));
            return;
        }

        Location dest = last.getLocation().add(0.5, 1, 0.5);
        if (dest.getBlock().getType().isSolid() || dest.clone().add(0,1,0).getBlock().getType().isSolid()) {
            String raw = plugin.getConfigManager().getConfig().getString("messages.teleport_blocked", "&cBlocked destination.");
            p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', raw));
            return;
        }
        p.teleport(dest);
        int cd = plugin.getConfigManager().getConfig().getInt("options.teleport_cooldown_ms", 1500);
        cooldowns.set(p.getUniqueId(), key, cd);
    }

    private void useRandomTp(Player p) {
        int activeSec = plugin.getConfigManager().getConfig().getInt("randomtp.active_seconds", 60);
        long cutoff = System.currentTimeMillis() - (activeSec * 1000L);
        java.util.List<Player> candidates = new java.util.ArrayList<>();
        for (Player t : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (t.equals(p)) continue;
            if (t.hasPermission("staffmode.toggle")) continue; // skip staff
            long last = MineStaff.getInstance().getActivityTracker().lastActive(t.getUniqueId());
            if (last == 0L || last < cutoff) continue; // inactive
            candidates.add(t);
        }
        if (candidates.isEmpty()) { p.sendMessage(org.bukkit.ChatColor.RED + "No active non-staff players found."); return; }
        java.util.Collections.shuffle(candidates);
        Player target = candidates.get(0);
        p.teleport(target.getLocation());
        int cd = plugin.getConfigManager().getConfig().getInt("randomtp.cooldown_ms", 2000);
        cooldowns.set(p.getUniqueId(), "randomtp", cd);
        p.sendMessage(org.bukkit.ChatColor.AQUA + "Teleported to " + target.getName());
    }
}
