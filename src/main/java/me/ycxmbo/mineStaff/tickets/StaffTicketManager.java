package me.ycxmbo.mineStaff.tickets;

import me.ycxmbo.mineStaff.MineStaff;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages staff support tickets
 */
public class StaffTicketManager {
    private final MineStaff plugin;
    private final File file;
    private FileConfiguration yaml;
    private final Map<UUID, StaffTicket> tickets = new LinkedHashMap<>();

    public StaffTicketManager(MineStaff plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "tickets.yml");
        reload();
    }

    public void reload() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        yaml = YamlConfiguration.loadConfiguration(file);
        loadTickets();
    }

    private void loadTickets() {
        tickets.clear();
        ConfigurationSection ticketsSection = yaml.getConfigurationSection("tickets");
        if (ticketsSection == null) return;

        for (String key : ticketsSection.getKeys(false)) {
            try {
                String base = "tickets." + key;
                UUID id = UUID.fromString(key);
                UUID createdBy = UUID.fromString(yaml.getString(base + ".createdBy"));
                String createdByName = yaml.getString(base + ".createdByName");
                long created = yaml.getLong(base + ".created");
                String subject = yaml.getString(base + ".subject");
                String description = yaml.getString(base + ".description");
                String category = yaml.getString(base + ".category", "OTHER");
                String priority = yaml.getString(base + ".priority", "MEDIUM");

                StaffTicket ticket = new StaffTicket(id, createdBy, createdByName, created,
                        subject, description, category, priority);

                ticket.status = yaml.getString(base + ".status", "OPEN");

                String claimedByStr = yaml.getString(base + ".claimedBy");
                if (claimedByStr != null && !claimedByStr.isEmpty()) {
                    ticket.claimedBy = UUID.fromString(claimedByStr);
                    ticket.claimedByName = yaml.getString(base + ".claimedByName");
                    ticket.claimedAt = yaml.getLong(base + ".claimedAt");
                }

                ticket.resolvedAt = yaml.getLong(base + ".resolvedAt", 0L);

                // Load comments
                List<Map<?, ?>> commentMaps = yaml.getMapList(base + ".comments");
                for (Map<?, ?> commentMap : commentMaps) {
                    UUID author = UUID.fromString((String) commentMap.get("author"));
                    String authorName = (String) commentMap.get("authorName");
                    long timestamp = ((Number) commentMap.get("timestamp")).longValue();
                    String message = (String) commentMap.get("message");
                    ticket.comments.add(new StaffTicket.TicketComment(author, authorName, timestamp, message));
                }

                tickets.put(id, ticket);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load ticket " + key + ": " + e.getMessage());
            }
        }
    }

    public synchronized void save() {
        for (StaffTicket ticket : tickets.values()) {
            String base = "tickets." + ticket.id;
            yaml.set(base + ".createdBy", ticket.createdBy.toString());
            yaml.set(base + ".createdByName", ticket.createdByName);
            yaml.set(base + ".created", ticket.created);
            yaml.set(base + ".subject", ticket.subject);
            yaml.set(base + ".description", ticket.description);
            yaml.set(base + ".category", ticket.category);
            yaml.set(base + ".priority", ticket.priority);
            yaml.set(base + ".status", ticket.status);

            if (ticket.claimedBy != null) {
                yaml.set(base + ".claimedBy", ticket.claimedBy.toString());
                yaml.set(base + ".claimedByName", ticket.claimedByName);
                yaml.set(base + ".claimedAt", ticket.claimedAt);
            } else {
                yaml.set(base + ".claimedBy", null);
                yaml.set(base + ".claimedByName", null);
                yaml.set(base + ".claimedAt", null);
            }

            yaml.set(base + ".resolvedAt", ticket.resolvedAt);

            // Save comments
            List<Map<String, Object>> commentMaps = new ArrayList<>();
            for (StaffTicket.TicketComment comment : ticket.comments) {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("author", comment.author.toString());
                commentMap.put("authorName", comment.authorName);
                commentMap.put("timestamp", comment.timestamp);
                commentMap.put("message", comment.message);
                commentMaps.add(commentMap);
            }
            yaml.set(base + ".comments", commentMaps);
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID createTicket(Player creator, String subject, String description, String category, String priority) {
        UUID id = UUID.randomUUID();
        StaffTicket ticket = new StaffTicket(id, creator.getUniqueId(), creator.getName(),
                System.currentTimeMillis(), subject, description, category, priority);

        tickets.put(id, ticket);
        save();

        // Notify online staff
        notifyStaff(ticket);

        return id;
    }

    private void notifyStaff(StaffTicket ticket) {
        String message = String.format("§e§l[Staff Ticket] §r§f%s §7created ticket: §e%s §7[%s/%s]",
                ticket.createdByName, ticket.subject, ticket.category, ticket.priority);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("staffmode.tickets.notify") &&
                    !staff.getUniqueId().equals(ticket.createdBy)) {
                staff.sendMessage(message);
                plugin.getSoundManager().playSound(staff, "alert.normal");
            }
        }
    }

    public StaffTicket getTicket(UUID id) {
        return tickets.get(id);
    }

    public List<StaffTicket> getAllTickets() {
        return new ArrayList<>(tickets.values());
    }

    public List<StaffTicket> getOpenTickets() {
        return tickets.values().stream()
                .filter(StaffTicket::isOpen)
                .sorted(Comparator.comparingLong(t -> -t.created))
                .collect(Collectors.toList());
    }

    public List<StaffTicket> getTicketsByCreator(UUID creator) {
        return tickets.values().stream()
                .filter(t -> t.createdBy.equals(creator))
                .sorted(Comparator.comparingLong(t -> -t.created))
                .collect(Collectors.toList());
    }

    public List<StaffTicket> getTicketsByClaimed(UUID claimer) {
        return tickets.values().stream()
                .filter(t -> t.claimedBy != null && t.claimedBy.equals(claimer))
                .sorted(Comparator.comparingLong(t -> -t.created))
                .collect(Collectors.toList());
    }

    public List<StaffTicket> getTicketsByStatus(String status) {
        return tickets.values().stream()
                .filter(t -> t.status.equalsIgnoreCase(status))
                .sorted(Comparator.comparingLong(t -> -t.created))
                .collect(Collectors.toList());
    }

    public boolean claimTicket(UUID ticketId, Player staff) {
        StaffTicket ticket = tickets.get(ticketId);
        if (ticket == null || !ticket.isOpen()) return false;

        if (ticket.claimedBy != null && !ticket.claimedBy.equals(staff.getUniqueId())) {
            return false; // Already claimed by someone else
        }

        ticket.claim(staff.getUniqueId(), staff.getName());
        save();

        // Notify ticket creator
        Player creator = Bukkit.getPlayer(ticket.createdBy);
        if (creator != null && creator.isOnline()) {
            creator.sendMessage(String.format("§a§l[Staff Ticket] §r§f%s §7claimed your ticket: §e%s",
                    staff.getName(), ticket.subject));
        }

        return true;
    }

    public boolean unclaimTicket(UUID ticketId, Player staff) {
        StaffTicket ticket = tickets.get(ticketId);
        if (ticket == null) return false;

        if (ticket.claimedBy != null && !ticket.claimedBy.equals(staff.getUniqueId())) {
            return false; // Can't unclaim someone else's ticket
        }

        ticket.unclaim();
        save();
        return true;
    }

    public boolean addComment(UUID ticketId, Player author, String message) {
        StaffTicket ticket = tickets.get(ticketId);
        if (ticket == null) return false;

        ticket.addComment(author.getUniqueId(), author.getName(), message);
        save();

        // Notify involved parties
        Set<UUID> toNotify = new HashSet<>();
        toNotify.add(ticket.createdBy);
        if (ticket.claimedBy != null) toNotify.add(ticket.claimedBy);
        for (StaffTicket.TicketComment comment : ticket.comments) {
            toNotify.add(comment.author);
        }
        toNotify.remove(author.getUniqueId()); // Don't notify the commenter

        for (UUID uuid : toNotify) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(String.format("§b§l[Staff Ticket] §r§f%s §7commented on ticket: §e%s",
                        author.getName(), ticket.subject));
            }
        }

        return true;
    }

    public boolean resolveTicket(UUID ticketId, Player resolver) {
        StaffTicket ticket = tickets.get(ticketId);
        if (ticket == null || ticket.isClosed()) return false;

        ticket.resolve();
        save();

        // Notify ticket creator
        Player creator = Bukkit.getPlayer(ticket.createdBy);
        if (creator != null && creator.isOnline()) {
            creator.sendMessage(String.format("§a§l[Staff Ticket] §r§7Your ticket §e%s §7has been resolved by §f%s",
                    ticket.subject, resolver.getName()));
        }

        return true;
    }

    public boolean closeTicket(UUID ticketId, Player closer) {
        StaffTicket ticket = tickets.get(ticketId);
        if (ticket == null) return false;

        ticket.close();
        save();
        return true;
    }

    public boolean reopenTicket(UUID ticketId) {
        StaffTicket ticket = tickets.get(ticketId);
        if (ticket == null || !ticket.isClosed()) return false;

        ticket.reopen();
        save();
        return true;
    }

    public int getOpenTicketCount() {
        return (int) tickets.values().stream().filter(StaffTicket::isOpen).count();
    }

    public Map<String, Integer> getTicketStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", tickets.size());
        stats.put("open", (int) tickets.values().stream().filter(t -> "OPEN".equals(t.status)).count());
        stats.put("claimed", (int) tickets.values().stream().filter(t -> "CLAIMED".equals(t.status)).count());
        stats.put("resolved", (int) tickets.values().stream().filter(t -> "RESOLVED".equals(t.status)).count());
        stats.put("closed", (int) tickets.values().stream().filter(t -> "CLOSED".equals(t.status)).count());
        return stats;
    }
}
