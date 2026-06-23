package me.ycxmbo.mineStaff.commands;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.managers.ConfigManager;
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

public class StaffTicketCommand implements CommandExecutor {
    private final MineStaff plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm");

    public StaffTicketCommand(MineStaff plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(cfg.getMessage("only_players", "Only players can use this."));
            return true;
        }
        if (!player.hasPermission("staffmode.tickets")) {
            player.sendMessage(cfg.getMessage("no_permission", "No permission."));
            return true;
        }
        if (args.length == 0) { showHelp(player, cfg); return true; }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args, cfg);
            case "view" -> handleView(player, args, cfg);
            case "list" -> handleList(player, args, cfg);
            case "claim" -> handleClaim(player, args, cfg);
            case "unclaim" -> handleUnclaim(player, args, cfg);
            case "comment" -> handleComment(player, args, cfg);
            case "resolve" -> handleResolve(player, args, cfg);
            case "close" -> handleClose(player, args, cfg);
            case "reopen" -> handleReopen(player, args, cfg);
            case "stats" -> handleStats(player, cfg);
            case "gui" -> handleGUI(player, cfg);
            default -> showHelp(player, cfg);
        }
        return true;
    }

    private void showHelp(Player player, ConfigManager cfg) {
        player.sendMessage("§6§l=== Staff Ticket Commands ===");
        player.sendMessage("§e/ticket create <subject> §8— §7" + cfg.getMessage("ticket_help_create_tip", "Create a new ticket"));
        player.sendMessage("§e/ticket list [mine|claimed|open|all] §8— §7" + cfg.getMessage("ticket_help_list_tip", "List tickets"));
        player.sendMessage("§e/ticket view <id> §8— §7" + cfg.getMessage("ticket_help_view_tip", "View ticket details"));
        player.sendMessage("§e/ticket claim <id> §8— §7" + cfg.getMessage("ticket_help_claim_tip", "Claim a ticket"));
        player.sendMessage("§e/ticket unclaim <id> §8— §7" + cfg.getMessage("ticket_help_unclaim_tip", "Unclaim a ticket"));
        player.sendMessage("§e/ticket comment <id> <message> §8— §7" + cfg.getMessage("ticket_help_comment_tip", "Add comment"));
        player.sendMessage("§e/ticket resolve <id> §8— §7" + cfg.getMessage("ticket_help_resolve_tip", "Mark as resolved"));
        player.sendMessage("§e/ticket close <id> §8— §7" + cfg.getMessage("ticket_help_close_tip", "Close ticket"));
        player.sendMessage("§e/ticket reopen <id> §8— §7" + cfg.getMessage("ticket_help_reopen_tip", "Reopen ticket"));
        player.sendMessage("§e/ticket stats §8— §7" + cfg.getMessage("ticket_help_stats_tip", "View statistics"));
        player.sendMessage("§e/ticket gui §8— §7" + cfg.getMessage("ticket_help_gui_tip", "Open ticket GUI"));
    }

    private void handleCreate(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 2) { player.sendMessage(cfg.getMessage("ticket_create_usage", "Usage: /ticket create <subject>")); return; }
        String subject = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        UUID ticketId = plugin.getStaffTicketManager().createTicket(player, subject, "No description provided", "QUESTION", "MEDIUM");
        String shortId = ticketId.toString().substring(0, 8);
        player.sendMessage(cfg.getMessage("ticket_created", "Ticket created!"));
        player.sendMessage(cfg.getMessage("ticket_created_id", "Ticket ID: {id}").replace("{id}", shortId + "..."));
        player.sendMessage(cfg.getMessage("ticket_created_subject", "Subject: {subject}").replace("{subject}", subject));
        player.sendMessage(cfg.getMessage("ticket_created_tip", "Use /ticket view {id} to view it.").replace("{id}", shortId));
        plugin.getSoundManager().playSound(player, "report.filed");
    }

    private void handleView(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 2) { player.sendMessage(cfg.getMessage("ticket_view_usage", "Usage: /ticket view <id>")); return; }
        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
        StaffTicket ticket = plugin.getStaffTicketManager().getTicket(ticketId);
        if (ticket == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
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
                player.sendMessage("§7[" + dateFormat.format(new Date(comment.timestamp)) + "] §f" + comment.authorName + "§7: §f" + comment.message);
            }
            if (ticket.comments.size() > 3) {
                player.sendMessage("§7... and " + (ticket.comments.size() - 3) + " more comments.");
            }
        }
    }

    private void handleList(Player player, String[] args, ConfigManager cfg) {
        String filter = args.length > 1 ? args[1].toLowerCase() : "open";
        List<StaffTicket> tickets;
        switch (filter) {
            case "mine" -> tickets = plugin.getStaffTicketManager().getTicketsByCreator(player.getUniqueId());
            case "claimed" -> tickets = plugin.getStaffTicketManager().getTicketsByClaimed(player.getUniqueId());
            case "open" -> tickets = plugin.getStaffTicketManager().getOpenTickets();
            case "all" -> tickets = plugin.getStaffTicketManager().getAllTickets();
            case "resolved" -> tickets = plugin.getStaffTicketManager().getTicketsByStatus("RESOLVED");
            case "closed" -> tickets = plugin.getStaffTicketManager().getTicketsByStatus("CLOSED");
            default -> { player.sendMessage(cfg.getMessage("ticket_list_filter_invalid", "Invalid filter!")); return; }
        }
        if (tickets.isEmpty()) { player.sendMessage(cfg.getMessage("ticket_no_tickets", "No tickets found.")); return; }
        player.sendMessage("§6§l=== Tickets (" + filter.toUpperCase() + ") - " + tickets.size() + " ===");
        for (int i = 0; i < Math.min(10, tickets.size()); i++) {
            StaffTicket ticket = tickets.get(i);
            String shortId = ticket.id.toString().substring(0, 8);
            player.sendMessage(String.format("§7%s §f%s §7by §f%s §7[%s%s§7]",
                    shortId, ticket.subject, ticket.createdByName, getStatusColor(ticket.status), ticket.status));
        }
        if (tickets.size() > 10) {
            player.sendMessage(cfg.getMessage("ticket_list_more", "... and {count} more.")
                    .replace("{count}", String.valueOf(tickets.size() - 10)));
        }
    }

    private void handleClaim(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 2) { player.sendMessage(cfg.getMessage("ticket_claim_usage", "Usage: /ticket claim <id>")); return; }
        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
        if (plugin.getStaffTicketManager().claimTicket(ticketId, player)) {
            player.sendMessage(cfg.getMessage("ticket_claimed_self", "Ticket claimed!"));
            plugin.getSoundManager().playSound(player, "report.claimed");
        } else {
            player.sendMessage(cfg.getMessage("ticket_claim_failed", "Failed to claim ticket."));
        }
    }

    private void handleUnclaim(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 2) { player.sendMessage(cfg.getMessage("ticket_unclaim_usage", "Usage: /ticket unclaim <id>")); return; }
        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
        if (plugin.getStaffTicketManager().unclaimTicket(ticketId, player)) {
            player.sendMessage(cfg.getMessage("ticket_unclaimed", "Ticket unclaimed!"));
        } else {
            player.sendMessage(cfg.getMessage("ticket_unclaim_failed", "Failed to unclaim ticket."));
        }
    }

    private void handleComment(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 3) { player.sendMessage(cfg.getMessage("ticket_comment_usage", "Usage: /ticket comment <id> <message>")); return; }
        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        if (plugin.getStaffTicketManager().addComment(ticketId, player, message)) {
            player.sendMessage(cfg.getMessage("ticket_comment_added", "Comment added!"));
        } else {
            player.sendMessage(cfg.getMessage("ticket_comment_failed", "Failed to add comment."));
        }
    }

    private void handleResolve(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 2) { player.sendMessage(cfg.getMessage("ticket_resolve_usage", "Usage: /ticket resolve <id>")); return; }
        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
        if (plugin.getStaffTicketManager().resolveTicket(ticketId, player)) {
            player.sendMessage(cfg.getMessage("ticket_resolved_self", "Ticket resolved!"));
            plugin.getSoundManager().playSound(player, "report.closed");
        } else {
            player.sendMessage(cfg.getMessage("ticket_resolve_failed", "Failed to resolve ticket."));
        }
    }

    private void handleClose(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 2) { player.sendMessage(cfg.getMessage("ticket_close_usage", "Usage: /ticket close <id>")); return; }
        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
        if (plugin.getStaffTicketManager().closeTicket(ticketId, player)) {
            player.sendMessage(cfg.getMessage("ticket_closed_self", "Ticket closed!"));
            plugin.getSoundManager().playSound(player, "report.closed");
        } else {
            player.sendMessage(cfg.getMessage("ticket_close_failed", "Failed to close ticket."));
        }
    }

    private void handleReopen(Player player, String[] args, ConfigManager cfg) {
        if (args.length < 2) { player.sendMessage(cfg.getMessage("ticket_reopen_usage", "Usage: /ticket reopen <id>")); return; }
        UUID ticketId = findTicketId(args[1]);
        if (ticketId == null) { player.sendMessage(cfg.getMessage("ticket_not_found", "Ticket not found!")); return; }
        if (plugin.getStaffTicketManager().reopenTicket(ticketId)) {
            player.sendMessage(cfg.getMessage("ticket_reopened", "Ticket reopened!"));
        } else {
            player.sendMessage(cfg.getMessage("ticket_reopen_failed", "Failed to reopen ticket."));
        }
    }

    private void handleStats(Player player, ConfigManager cfg) {
        Map<String, Integer> stats = plugin.getStaffTicketManager().getTicketStats();
        player.sendMessage(cfg.getMessage("ticket_stats_header", "=== Staff Ticket Statistics ==="));
        player.sendMessage(cfg.getMessage("ticket_stats_total", "Total Tickets: {count}").replace("{count}", String.valueOf(stats.get("total"))));
        player.sendMessage(cfg.getMessage("ticket_stats_open", "Open: {count}").replace("{count}", String.valueOf(stats.get("open"))));
        player.sendMessage(cfg.getMessage("ticket_stats_claimed", "Claimed: {count}").replace("{count}", String.valueOf(stats.get("claimed"))));
        player.sendMessage(cfg.getMessage("ticket_stats_resolved", "Resolved: {count}").replace("{count}", String.valueOf(stats.get("resolved"))));
        player.sendMessage(cfg.getMessage("ticket_stats_closed", "Closed: {count}").replace("{count}", String.valueOf(stats.get("closed"))));
    }

    private void handleGUI(Player player, ConfigManager cfg) {
        player.sendMessage(cfg.getMessage("ticket_opening_gui", "Opening tickets GUI..."));
        plugin.getStaffTicketsGUI().open(player, 0, "open");
    }

    private UUID findTicketId(String input) {
        for (StaffTicket ticket : plugin.getStaffTicketManager().getAllTickets()) {
            if (ticket.id.toString().startsWith(input)) return ticket.id;
        }
        try { return UUID.fromString(input); } catch (IllegalArgumentException e) { return null; }
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
