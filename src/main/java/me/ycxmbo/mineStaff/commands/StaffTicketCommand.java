package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.tickets.StaffTicket;
import me.ycxmbo.mineStaff.tickets.StaffTicketManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Command for managing staff support tickets
 */
public class StaffTicketCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm");

    public StaffTicketCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("staffmode.tickets")) {
            player.sendMessage("§cYou don't have permission to use staff tickets.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create" -> handleCreate(player, args);
            case "view" -> handleView(player, args);
            case "list" -> handleList(player, args);
            case "claim" -> handleClaim(player, args);
            case "unclaim" -> handleUnclaim(player, args);
            case "comment" -> handleComment(player, args);
            case "resolve" -> handleResolve(player, args);
            case "close" -> handleClose(player, args);
            case "reopen" -> handleReopen(player, args);
            case "stats" -> handleStats(player);
            case "gui" -> handleGUI(player);
            default -> showHelp(player);
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§6§l=== Staff Ticket Commands ===");
        player.sendMessage("§e/ticket create <subject> - §7Create a new ticket");
        player.sendMessage("§e/ticket list [mine|claimed|open|all] - §7List tickets");
        player.sendMessage("§e/ticket view <id> - §7View ticket details");
        player.sendMessage("§e/ticket claim <id> - §7Claim a ticket");
        player.sendMessage("§e/ticket unclaim <id> - §7Unclaim a ticket");
        player.sendMessage("§e/ticket comment <id> <message> - §7Add comment");
        player.sendMessage("§e/ticket resolve <id> - §7Mark as resolved");
        player.sendMessage("§e/ticket close <id> - §7Close ticket");
        player.sendMessage("§e/ticket reopen <id> - §7Reopen ticket");
        player.sendMessage("§e/ticket stats - §7View ticket statistics");
        player.sendMessage("§e/ticket gui - §7Open ticket GUI");
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ticket create <subject>");
            player.sendMessage("§7You'll be prompted for more details.");
            return;
        }

        String subject = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // For simplicity, we'll ask for category and priority through chat or use defaults
        // In a full implementation, you might use a GUI or chat prompts

        String category = "QUESTION"; // Default
        String priority = "MEDIUM"; // Default
        String description = "No description provided"; // Default

        UUID ticketId = plugin.getStaffTicketManager().createTicket(player, subject, description, category, priority);

        player.sendMessage("§a§l[Staff Ticket] §r§7Ticket created successfully!");
        player.sendMessage("§7Ticket ID: §e" + ticketId.toString().substring(0, 8) + "...");
        player.sendMessage("§7Subject: §f" + subject);
        player.sendMessage("§7Use §e/ticket view " + ticketId.toString().substring(0, 8) + " §7to view details.");
        player.sendMessage("§7Use §e/ticket comment " + ticketId.toString().substring(0, 8) + " <message> §7to add details.");

        plugin.getSoundManager().playSound(player, "report.filed");
    }

    private void handleView(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ticket view <id>");
            return;
        }

        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        StaffTicket ticket = plugin.getStaffTicketManager().getTicket(ticketId);
        if (ticket == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        displayTicket(player, ticket);
    }

    private void displayTicket(Player player, StaffTicket ticket) {
        player.sendMessage("§6§l=== Ticket Details ===");
        player.sendMessage("§7ID: §e" + ticket.id.toString().substring(0, 8) + "...");
        player.sendMessage("§7Subject: §f" + ticket.subject);
        player.sendMessage("§7Created by: §f" + ticket.createdByName + " §7on §f" + dateFormat.format(new Date(ticket.created)));
        player.sendMessage("§7Category: §e" + ticket.category + " §7Priority: " + getPriorityColor(ticket.priority) + ticket.priority);
        player.sendMessage("§7Status: " + getStatusColor(ticket.status) + ticket.status);

        if (ticket.claimedBy != null) {
            player.sendMessage("§7Claimed by: §f" + ticket.claimedByName + " §7on §f" + dateFormat.format(new Date(ticket.claimedAt)));
        }

        if (ticket.resolvedAt > 0) {
            player.sendMessage("§7Resolved: §f" + dateFormat.format(new Date(ticket.resolvedAt)));
        }

        player.sendMessage("§7Description: §f" + ticket.description);

        if (!ticket.comments.isEmpty()) {
            player.sendMessage("§7§l--- Comments (" + ticket.comments.size() + ") ---");
            for (int i = 0; i < Math.min(3, ticket.comments.size()); i++) {
                StaffTicket.TicketComment comment = ticket.comments.get(i);
                player.sendMessage("§7[" + dateFormat.format(new Date(comment.timestamp)) + "] §f" +
                        comment.authorName + "§7: §f" + comment.message);
            }
            if (ticket.comments.size() > 3) {
                player.sendMessage("§7... and " + (ticket.comments.size() - 3) + " more comments.");
            }
        }
    }

    private void handleList(Player player, String[] args) {
        String filter = args.length > 1 ? args[1].toLowerCase() : "open";

        List<StaffTicket> tickets;

        switch (filter) {
            case "mine" -> tickets = plugin.getStaffTicketManager().getTicketsByCreator(player.getUniqueId());
            case "claimed" -> tickets = plugin.getStaffTicketManager().getTicketsByClaimed(player.getUniqueId());
            case "open" -> tickets = plugin.getStaffTicketManager().getOpenTickets();
            case "all" -> tickets = plugin.getStaffTicketManager().getAllTickets();
            case "resolved" -> tickets = plugin.getStaffTicketManager().getTicketsByStatus("RESOLVED");
            case "closed" -> tickets = plugin.getStaffTicketManager().getTicketsByStatus("CLOSED");
            default -> {
                player.sendMessage("§cInvalid filter! Use: mine, claimed, open, all, resolved, closed");
                return;
            }
        }

        if (tickets.isEmpty()) {
            player.sendMessage("§7No tickets found.");
            return;
        }

        player.sendMessage("§6§l=== Tickets (" + filter.toUpperCase() + ") - " + tickets.size() + " ===");
        for (int i = 0; i < Math.min(10, tickets.size()); i++) {
            StaffTicket ticket = tickets.get(i);
            String shortId = ticket.id.toString().substring(0, 8);
            player.sendMessage(String.format("§7%s §f%s §7by §f%s §7[%s%s§7]",
                    shortId, ticket.subject, ticket.createdByName,
                    getStatusColor(ticket.status), ticket.status));
        }

        if (tickets.size() > 10) {
            player.sendMessage("§7... and " + (tickets.size() - 10) + " more. Use §e/ticket gui §7to see all.");
        }
    }

    private void handleClaim(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ticket claim <id>");
            return;
        }

        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        boolean success = plugin.getStaffTicketManager().claimTicket(ticketId, player);
        if (success) {
            player.sendMessage("§a§l[Staff Ticket] §r§7Ticket claimed successfully!");
            plugin.getSoundManager().playSound(player, "report.claimed");
        } else {
            player.sendMessage("§cFailed to claim ticket. It may already be claimed.");
        }
    }

    private void handleUnclaim(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ticket unclaim <id>");
            return;
        }

        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        boolean success = plugin.getStaffTicketManager().unclaimTicket(ticketId, player);
        if (success) {
            player.sendMessage("§a§l[Staff Ticket] §r§7Ticket unclaimed successfully!");
        } else {
            player.sendMessage("§cFailed to unclaim ticket.");
        }
    }

    private void handleComment(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§cUsage: /ticket comment <id> <message>");
            return;
        }

        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        boolean success = plugin.getStaffTicketManager().addComment(ticketId, player, message);
        if (success) {
            player.sendMessage("§a§l[Staff Ticket] §r§7Comment added successfully!");
        } else {
            player.sendMessage("§cFailed to add comment.");
        }
    }

    private void handleResolve(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ticket resolve <id>");
            return;
        }

        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        boolean success = plugin.getStaffTicketManager().resolveTicket(ticketId, player);
        if (success) {
            player.sendMessage("§a§l[Staff Ticket] §r§7Ticket marked as resolved!");
            plugin.getSoundManager().playSound(player, "report.closed");
        } else {
            player.sendMessage("§cFailed to resolve ticket.");
        }
    }

    private void handleClose(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ticket close <id>");
            return;
        }

        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        boolean success = plugin.getStaffTicketManager().closeTicket(ticketId, player);
        if (success) {
            player.sendMessage("§a§l[Staff Ticket] §r§7Ticket closed successfully!");
            plugin.getSoundManager().playSound(player, "report.closed");
        } else {
            player.sendMessage("§cFailed to close ticket.");
        }
    }

    private void handleReopen(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ticket reopen <id>");
            return;
        }

        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) {
            player.sendMessage("§cTicket not found!");
            return;
        }

        boolean success = plugin.getStaffTicketManager().reopenTicket(ticketId);
        if (success) {
            player.sendMessage("§a§l[Staff Ticket] §r§7Ticket reopened!");
        } else {
            player.sendMessage("§cFailed to reopen ticket.");
        }
    }

    private void handleStats(Player player) {
        Map<String, Integer> stats = plugin.getStaffTicketManager().getTicketStats();

        player.sendMessage("§6§l=== Staff Ticket Statistics ===");
        player.sendMessage("§7Total Tickets: §f" + stats.get("total"));
        player.sendMessage("§7Open: §c" + stats.get("open"));
        player.sendMessage("§7Claimed: §e" + stats.get("claimed"));
        player.sendMessage("§7Resolved: §a" + stats.get("resolved"));
        player.sendMessage("§7Closed: §8" + stats.get("closed"));
    }

    private void handleGUI(Player player) {
        // This would open the tickets GUI
        player.sendMessage("§7Opening tickets GUI...");
        plugin.getStaffTicketsGUI().open(player, 0, "open");
    }

    private UUID findTicketId(String input) {
        // Try to find ticket by partial ID match
        for (StaffTicket ticket : plugin.getStaffTicketManager().getAllTickets()) {
            if (ticket.id.toString().startsWith(input)) {
                return ticket.id;
            }
        }

        // Try exact UUID match
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String getStatusColor(String status) {
        return switch (status.toUpperCase()) {
            case "OPEN" -> "§c";
            case "CLAIMED" -> "§e";
            case "RESOLVED" -> "§a";
            case "CLOSED" -> "§8";
            default -> "§7";
        };
    }

    private String getPriorityColor(String priority) {
        return switch (priority.toUpperCase()) {
            case "LOW" -> "§a";
            case "MEDIUM" -> "§e";
            case "HIGH" -> "§6";
            case "URGENT" -> "§c";
            default -> "§7";
        };
    }
}
