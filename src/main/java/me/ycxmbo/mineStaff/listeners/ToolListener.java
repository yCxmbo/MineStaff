package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffDataManager;
import me.ycxmbo.mineStaff.tools.ToolManager;
import me.ycxmbo.mineStaff.util.CooldownManager;
import me.ycxmbo.mineStaff.util.SoundUtil;
import me.ycxmbo.mineStaff.util.VanishUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    // Right-click air/block with tools
    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        ItemStack it = e.getItem();
        if (it == null) return;
        if (!data.isStaffMode(p)) return;

        if (it.getType() == ToolManager.TELEPORT_TOOL && p.hasPermission("staffmode.teleport")) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                useTeleport(p, p.isSneaking() ? 120 : 60);
            }
        } else if (it.getType() == ToolManager.VANISH_TOOL && p.hasPermission("staffmode.vanish")) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                toggleVanish(p);
            }
        }
    }

    // Right-click a player with Freeze/Inspect
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
            p.sendMessage(ChatColor.YELLOW + "Player " + target.getName() + " " + (state ? "frozen." : "unfrozen."));
            if (state) target.sendMessage(ChatColor.RED + "You have been frozen by staff. Do not log out.");
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0, 1, 0), 40, 0.6, 0.8, 0.6);
            SoundUtil.playFreeze(p);
        } else if (it.getType() == ToolManager.INSPECT_TOOL && p.hasPermission("staffmode.inspect")) {
            e.setCancelled(true);
            plugin.getInspectorGUI().open(p, target);
            target.getWorld().spawnParticle(Particle.CLOUD, target.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5);
            SoundUtil.playInspectSound(p);
        }
    }

    private void toggleVanish(Player p) {
        boolean newState = !data.isVanished(p);
        data.setVanished(p, newState);
        MineStaff.getInstance().getVanishStore().setVanished(p.getUniqueId(), newState);
        MineStaff.getInstance().getVanishStore().save();
        VanishUtil.applyVanish(p, newState);
        if (newState) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Vanish enabled.");
            SoundUtil.playVanishOn(p);
            p.getWorld().spawnParticle(Particle.END_ROD, p.getLocation(), 40, 0.6, 0.8, 0.6);
        } else {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Vanish disabled.");
            SoundUtil.playVanishOff(p);
            p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation(), 20, 0.6, 0.6, 0.6);
        }
    }

    private void useTeleport(Player p, int maxRange) {
        String key = "teleport";
        if (!cooldowns.ready(p.getUniqueId(), key)) {
            long ms = cooldowns.remaining(p.getUniqueId(), key);
            p.sendMessage(ChatColor.RED + "Teleport cooldown: " + (ms/1000.0) + "s");
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
        // Basic safety: ensure destination and head are not inside solid blocks
        if (dest.getBlock().getType().isSolid() || dest.clone().add(0,1,0).getBlock().getType().isSolid()) {
            p.sendMessage(ChatColor.RED + "Blocked destination.");
            return;
        }
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), 30, 0.6, 1.0, 0.6);
        p.teleport(dest);
        p.getWorld().spawnParticle(Particle.PORTAL, dest, 30, 0.6, 1.0, 0.6);
        SoundUtil.playTeleport(p);
        cooldowns.set(p.getUniqueId(), key, 1500); // 1.5s cooldown
    }
}
