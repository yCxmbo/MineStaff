package me.ycxmbo.mineStaff.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {
    public static void playInspectSound(Player p) { p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f); }
    public static void playFailSound(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f); }
    public static void playVanishOn(Player p) { p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f); }
    public static void playVanishOff(Player p) { p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_DEATH, 0.8f, 0.6f); }
    public static void playTeleport(Player p) { p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f); }
    public static void playFreeze(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_GLASS_PLACE, 1f, 0.8f); }
}
