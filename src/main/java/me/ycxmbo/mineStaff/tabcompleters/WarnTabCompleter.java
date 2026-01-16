package me.ycxmbo.mineStaff.tabcompleters;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WarnTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Subcommands or player names
            List<String> suggestions = new ArrayList<>(Arrays.asList("list", "remove", "clear", "gui"));
            suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
            return suggestions;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            // For list, clear, and gui - suggest player names
            if (subCommand.equals("list") || subCommand.equals("clear") || subCommand.equals("gui")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        if (args.length >= 2 && !Arrays.asList("list", "remove", "clear", "gui").contains(args[0].toLowerCase())) {
            // Issuing a warning - suggest severity or duration
            return Arrays.asList("LOW", "MEDIUM", "HIGH", "SEVERE", "1h", "3d", "7d", "perm");
        }

        return new ArrayList<>();
    }
}
