package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.StaffChatManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor {
    private final StaffChatManager scm;

    public StaffChatCommand(MineStaff plugin) {
        this.scm = plugin.getStaffChatManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Only players."); return true; }
        if (!p.hasPermission("staffmode.chat")) { p.sendMessage(ChatColor.RED + "No permission."); return true; }

        if (args.length == 0) {
            boolean now = !scm.isToggled(p);
            scm.setToggled(p, now);
            p.sendMessage(now
                    ? ChatColor.LIGHT_PURPLE + "StaffChat toggled ON. Your chat will go to staff."
                    : ChatColor.GRAY + "StaffChat toggled OFF. Your chat is public again.");
            return true;
        }

        // /sc <message>
        String msg = String.join(" ", args);
        scm.broadcast(p, msg);
        return true;
    }
}
