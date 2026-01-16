package me.ycxmbo.mineStaff.listeners;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.gui.enhanced.EnhancedReportsGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles enhanced GUI interactions
 */
public class EnhancedGUIListener implements Listener {
    private final MineStaff plugin;
    private final Map<UUID, PendingInput> pendingInputs = new HashMap<>();
    
    private enum InputType { SEARCH, JUMP_TO_PAGE }
    
    private record PendingInput(InputType type, String guiType) {}
    
    public EnhancedGUIListener(MineStaff plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        
        if (title.startsWith("§4Reports")) {
            event.setCancelled(true);
            
            boolean isShiftClick = event.getClick() == ClickType.SHIFT_LEFT || 
                                  event.getClick() == ClickType.SHIFT_RIGHT;
            boolean isRightClick = event.getClick() == ClickType.RIGHT || 
                                  event.getClick() == ClickType.SHIFT_RIGHT;
            
            EnhancedReportsGUI gui = getEnhancedReportsGUI();
            if (gui != null) {
                gui.handleClick(player, event.getSlot(), isShiftClick, isRightClick);
            }
        }
    }
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PendingInput pending = pendingInputs.get(player.getUniqueId());
        
        if (pending == null) return;
        
        event.setCancelled(true);
        pendingInputs.remove(player.getUniqueId());
        
        String input = event.getMessage().trim();
        
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage("§cCancelled");
            return;
        }
        
        // Run on main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            processChatInput(player, pending, input);
        });
    }
    
    private void processChatInput(Player player, PendingInput pending, String input) {
        if (pending.guiType.equals("reports")) {
            EnhancedReportsGUI gui = getEnhancedReportsGUI();
            if (gui == null) return;
            
            var ctx = gui.getContext(player);
            if (ctx == null) return;
            
            switch (pending.type) {
                case SEARCH -> {
                    ctx.setSearchQuery(input);
                    player.sendMessage("§aSearch set to: §f" + input);
                    gui.open(player);
                }
                case JUMP_TO_PAGE -> {
                    try {
                        int page = Integer.parseInt(input) - 1;
                        if (page < 0) {
                            player.sendMessage("§cPage number must be positive!");
                            return;
                        }
                        ctx.jumpToPage(page);
                        player.sendMessage("§aJumped to page " + (page + 1));
                        gui.open(player);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cInvalid page number!");
                    }
                }
            }
        }
    }
    
    public void requestInput(Player player, InputType type, String guiType) {
        pendingInputs.put(player.getUniqueId(), new PendingInput(type, guiType));
    }
    
    private EnhancedReportsGUI getEnhancedReportsGUI() {
        // This would need to be stored in MineStaff plugin
        // For now, return null - full implementation would need plugin integration
        return null;
    }
}
