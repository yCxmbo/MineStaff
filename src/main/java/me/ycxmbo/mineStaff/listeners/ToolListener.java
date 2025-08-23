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
        if (newState) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Vanish enabled.");
        } else {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Vanish disabled.");
        }
    }

    private void useTeleport(Player p, int maxRange) {
        String key = "teleport";
        if (!cooldowns.ready(p.getUniqueId(), key)) {
            long ms = cooldowns.remaining(p.getUniqueId(), key);
            p.sendMessage(ChatColor.RED + "Teleport cooldown: " + String.format(java.util.Locale.US, "%.1f", ms / 1000.0) + "s");
            return;
        }
        Location eye = p.getEyeLocation();
        BlockIterator it = new BlockIterator(p.getWorld(), eye.toVector(), eye.getDirection(), 0, maxRange);
        Block last = null;
        while (it.hasNext()) {
            Block b = it.next();
            if (b.getType().isSolid()) { last = b; break; }
            last = b;
        }
        if (last == null) { p.sendMessage(ChatColor.RED + "No safe spot in sight."); return; }

        Location dest = last.getLocation().add(0.5, 1, 0.5);
        if (dest.getBlock().getType().isSolid() || dest.clone().add(0,1,0).getBlock().getType().isSolid()) {
            p.sendMessage(ChatColor.RED + "Blocked destination.");
            return;
        }
        p.teleport(dest);
        cooldowns.set(p.getUniqueId(), key, 1500);
    }
}
