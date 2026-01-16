package me.ycxmbo.mineStaff.tabcompleters;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.channels.StaffChannel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelTabCompleter implements TabCompleter {
    private final MineStaff plugin;

    public ChannelTabCompleter(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Subcommands
            return Arrays.asList("list", "join", "switch", "toggle", "info").stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("switch"))) {
            // Suggest accessible channels
            return plugin.getChannelManager().getAccessibleChannels(player).stream()
                    .map(StaffChannel::getId)
                    .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
