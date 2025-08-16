package me.ycxmbo.mineStaff.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class InfractionsTabCompleter implements TabCompleter {
    private static final List<String> TYPES = Arrays.asList("ban","tempban","kick","warn","mute","tempmute");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) return List.of("add");
        if (args.length == 3 && "add".equalsIgnoreCase(args[1])) return TYPES;
        return List.of();
    }
}
