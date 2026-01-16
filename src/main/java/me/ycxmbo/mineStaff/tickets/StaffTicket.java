package me.ycxmbo.mineStaff.tickets;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a staff support ticket
 */
public class StaffTicket {
    public final UUID id;
    public final UUID createdBy;
    public final String createdByName;
    public final long created;
    public String subject;
    public String description;
    public String category; // QUESTION, TECHNICAL, PERMISSION, OTHER
    public String priority; // LOW, MEDIUM, HIGH, URGENT
    public String status; // OPEN, CLAIMED, RESOLVED, CLOSED
    public UUID claimedBy;
    public String claimedByName;
    public long claimedAt;
    public long resolvedAt;
    public final List<TicketComment> comments;

    public StaffTicket(UUID id, UUID createdBy, String createdByName, long created,
                      String subject, String description, String category, String priority) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.created = created;
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = "OPEN";
        this.claimedBy = null;
        this.claimedByName = null;
        this.claimedAt = 0L;
        this.resolvedAt = 0L;
        this.comments = new ArrayList<>();
    }

    public static class TicketComment {
        public final UUID author;
        public final String authorName;
        public final long timestamp;
        public final String message;

        public TicketComment(UUID author, String authorName, long timestamp, String message) {
            this.author = author;
            this.authorName = authorName;
            this.timestamp = timestamp;
            this.message = message;
        }
    }

    public void addComment(UUID author, String authorName, String message) {
        comments.add(new TicketComment(author, authorName, System.currentTimeMillis(), message));
    }

    public void claim(UUID staff, String staffName) {
        this.claimedBy = staff;
        this.claimedByName = staffName;
        this.claimedAt = System.currentTimeMillis();
        this.status = "CLAIMED";
    }

    public void unclaim() {
        this.claimedBy = null;
        this.claimedByName = null;
        this.claimedAt = 0L;
        this.status = "OPEN";
    }

    public void resolve() {
        this.status = "RESOLVED";
        this.resolvedAt = System.currentTimeMillis();
    }

    public void close() {
        this.status = "CLOSED";
        if (this.resolvedAt == 0L) {
            this.resolvedAt = System.currentTimeMillis();
        }
    }

    public void reopen() {
        this.status = this.claimedBy != null ? "CLAIMED" : "OPEN";
        this.resolvedAt = 0L;
    }

    public boolean isOpen() {
        return "OPEN".equals(status) || "CLAIMED".equals(status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    public long getAge() {
        return System.currentTimeMillis() - created;
    }

    public long getResponseTime() {
        return claimedAt > 0 ? claimedAt - created : 0L;
    }
}
