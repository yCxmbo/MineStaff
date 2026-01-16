package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class StaffHelpCommand implements CommandExecutor {
    private final MineStaff plugin;

    public StaffHelpCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendMainHelp(sender);
        } else {
            String category = args[0].toLowerCase();
            sendCategoryHelp(sender, category);
        }
        return true;
    }

    private void sendMainHelp(CommandSender sender) {
        sender.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
        sender.sendMessage(Component.text());
        sender.sendMessage(Component.text("         MineStaff Help", NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text());
        sender.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
        sender.sendMessage(Component.text());

        sendCategoryButton(sender, "basic", "Basic Commands", "Essential staff commands");
        sendCategoryButton(sender, "moderation", "Moderation", "Player management and punishment");
        sendCategoryButton(sender, "inspection", "Inspection", "Player inspection tools");
        sendCategoryButton(sender, "communication", "Communication", "Staff chat and messaging");
        sendCategoryButton(sender, "reports", "Reports & Evidence", "Report management");
        sendCategoryButton(sender, "tools", "Staff Tools", "Special staff mode tools");
        sendCategoryButton(sender, "admin", "Administration", "Configuration and management");

        sender.sendMessage(Component.text());
        sender.sendMessage(Component.text("Click a category or use: ", NamedTextColor.GRAY)
                .append(Component.text("/staffhelp <category>", NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
    }

    private void sendCategoryButton(CommandSender sender, String category, String name, String description) {
        Component button = Component.text()
                .append(Component.text("  [", NamedTextColor.DARK_GRAY))
                .append(Component.text(name, NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                .append(Component.text(description, NamedTextColor.GRAY))
                .clickEvent(ClickEvent.runCommand("/staffhelp " + category))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Click to view ", NamedTextColor.GREEN)
                                .append(Component.text(name, NamedTextColor.YELLOW))
                                .append(Component.text(" commands", NamedTextColor.GREEN))))
                .build();
        sender.sendMessage(button);
    }

    private void sendCategoryHelp(CommandSender sender, String category) {
        Map<String, List<HelpEntry>> categories = getCommandCategories();

        if (!categories.containsKey(category)) {
            sender.sendMessage(Component.text("Unknown category! Use /staffhelp to see all categories.", NamedTextColor.RED));
            return;
        }

        List<HelpEntry> commands = categories.get(category);
        String categoryName = getCategoryName(category);

        sender.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
        sender.sendMessage(Component.text());
        sender.sendMessage(Component.text("     " + categoryName, NamedTextColor.GOLD, TextDecoration.BOLD));
        sender.sendMessage(Component.text());
        sender.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
        sender.sendMessage(Component.text());

        for (HelpEntry entry : commands) {
            if (!hasPermission(sender, entry.permission)) continue;

            Component cmdComponent = Component.text()
                    .append(Component.text("  /", NamedTextColor.DARK_GRAY))
                    .append(Component.text(entry.command, NamedTextColor.YELLOW))
                    .append(Component.text(" " + entry.usage, NamedTextColor.GRAY))
                    .clickEvent(ClickEvent.suggestCommand("/" + entry.command + " "))
                    .hoverEvent(HoverEvent.showText(
                            Component.text(entry.description, NamedTextColor.WHITE)
                                    .append(Component.text("\n\nClick to suggest command", NamedTextColor.GRAY, TextDecoration.ITALIC))))
                    .build();
            sender.sendMessage(cmdComponent);

            if (!entry.aliases.isEmpty()) {
                sender.sendMessage(Component.text("    Aliases: ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(String.join(", ", entry.aliases), NamedTextColor.DARK_GRAY)));
            }
            sender.sendMessage(Component.text());
        }

        sender.sendMessage(Component.text("← Back to categories", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.runCommand("/staffhelp"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to go back", NamedTextColor.GRAY))));
        sender.sendMessage(Component.text("═══════════════════════════════════════", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
    }

    private Map<String, List<HelpEntry>> getCommandCategories() {
        Map<String, List<HelpEntry>> categories = new LinkedHashMap<>();

        // Basic Commands
        categories.put("basic", Arrays.asList(
                new HelpEntry("staffmode", "", "Toggle staff mode on/off", "staffmode.toggle", Arrays.asList("sm", "staff")),
                new HelpEntry("stafflogin", "<password>", "Log in to staff mode", "staffmode.login", Arrays.asList("slogin")),
                new HelpEntry("staffduty", "", "Toggle staff duty (auto staff mode + vanish)", "staffmode.duty", Arrays.asList("duty")),
                new HelpEntry("staffreload", "", "Reload plugin configuration", "staffmode.reload", Arrays.asList("sr"))
        ));

        // Moderation
        categories.put("moderation", Arrays.asList(
                new HelpEntry("freeze", "<player> [seconds]", "Freeze/unfreeze a player", "staffmode.freeze", Arrays.asList("f", "ice")),
                new HelpEntry("infractions", "<player> [add <type> <reason>]", "View or add infractions", "staffmode.infractions", Arrays.asList("inf", "warns")),
                new HelpEntry("notes", "<player> <add|list|remove> [note]", "Manage player notes", "staffmode.notes", Arrays.asList("note")),
                new HelpEntry("cpscheck", "<player>", "Check player's clicks per second", "staffmode.cpscheck", Arrays.asList("cps"))
        ));

        // Inspection
        categories.put("inspection", Arrays.asList(
                new HelpEntry("inspect", "<player>", "Open player inspection GUI", "staffmode.inspect", Arrays.asList("insp", "view")),
                new HelpEntry("profile", "<player>", "View player profile", "staffmode.profile", Arrays.asList("p")),
                new HelpEntry("inspectoffline", "<player> [ec]", "Inspect offline player's inventory", "staffmode.inspect", Arrays.asList("io")),
                new HelpEntry("rollback", "", "View inventory rollback history", "staffmode.rollback", Arrays.asList("rb")),
                new HelpEntry("commandspy", "", "Toggle command spy", "staffmode.spy", Arrays.asList("cmdspy", "cs")),
                new HelpEntry("socialspy", "", "Toggle social spy", "staffmode.spy", Arrays.asList("ss"))
        ));

        // Communication
        categories.put("communication", Arrays.asList(
                new HelpEntry("staffchat", "[message]", "Send message to staff chat or toggle", "staffmode.chat", Arrays.asList("sc")),
                new HelpEntry("stafflist", "", "View online staff members", "staffmode.stafflist", Arrays.asList("sl")),
                new HelpEntry("stafflistgui", "", "Open staff list GUI", "staffmode.stafflist.gui", Arrays.asList("slg")),
                new HelpEntry("backup", "[reason]", "Request backup from other staff", "staffmode.backup", Arrays.asList("requestbackup"))
        ));

        // Reports & Evidence
        categories.put("reports", Arrays.asList(
                new HelpEntry("report", "<player> <reason>", "Report a player", "staffmode.report", Arrays.asList("rp")),
                new HelpEntry("reports", "", "Open reports management GUI", "staffmode.alerts", Arrays.asList("reportsgui")),
                new HelpEntry("evidence", "<reportId> <add|list> [url]", "Manage report evidence", "staffmode.alerts", Arrays.asList("ev", "proof"))
        ));

        // Tools
        categories.put("tools", Arrays.asList(
                new HelpEntry("Staff Tools", "", "In staff mode, you receive special tools:", "", Collections.emptyList()),
                new HelpEntry("  Compass", "", "Teleport to players (right-click)", "staffmode.teleport", Collections.emptyList()),
                new HelpEntry("  Blaze Rod", "", "Freeze/unfreeze players", "staffmode.freeze.use", Collections.emptyList()),
                new HelpEntry("  Book", "", "Inspect player inventory", "staffmode.inspect", Collections.emptyList()),
                new HelpEntry("  Dye", "", "Toggle vanish mode", "staffmode.vanish", Collections.emptyList()),
                new HelpEntry("  Clock", "", "Run CPS check on player", "staffmode.cpscheck", Collections.emptyList()),
                new HelpEntry("  Feather", "", "Random teleport", "staffmode.randomtp", Collections.emptyList())
        ));

        // Admin
        categories.put("admin", Arrays.asList(
                new HelpEntry("staff2fa", "<enable|disable|status>", "Manage two-factor authentication", "staffmode.login", Arrays.asList("2fa")),
                new HelpEntry("staffreload", "", "Reload plugin configuration", "staffmode.reload", Arrays.asList("sreload"))
        ));

        return categories;
    }

    private String getCategoryName(String category) {
        return switch (category) {
            case "basic" -> "Basic Commands";
            case "moderation" -> "Moderation Commands";
            case "inspection" -> "Inspection Commands";
            case "communication" -> "Communication Commands";
            case "reports" -> "Reports & Evidence";
            case "tools" -> "Staff Mode Tools";
            case "admin" -> "Administration";
            default -> "Commands";
        };
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (permission.isEmpty()) return true;
        if (!(sender instanceof Player)) return true;
        return sender.hasPermission(permission);
    }

    private static class HelpEntry {
        final String command;
        final String usage;
        final String description;
        final String permission;
        final List<String> aliases;

        HelpEntry(String command, String usage, String description, String permission, List<String> aliases) {
            this.command = command;
            this.usage = usage;
            this.description = description;
            this.permission = permission;
            this.aliases = aliases;
        }
    }
}
