package me.ycxmbo.mineStaff.reports;

import me.ycxmbo.mineStaff.managers.ReportManager;

import java.util.UUID;

/**
 * Backward compatibility alias for ReportManager.Report
 * @deprecated Use {@link ReportManager.Report} instead
 */
@Deprecated
public class ReportedPlayer {
    public final UUID id;
    public final UUID reporter;
    public final UUID target;
    public final String reason;
    public final long created;
    public String status;
    public UUID claimedBy;
    public final String category;
    public final String priority;
    public final long dueBy;
    public final long claimedAt;

    // Legacy field names for backward compatibility
    public final String reportedPlayerName;
    public final long timestamp;

    public ReportedPlayer(ReportManager.Report report) {
        this.id = report.id;
        this.reporter = report.reporter;
        this.target = report.target;
        this.reason = report.reason;
        this.created = report.created;
        this.status = report.status;
        this.claimedBy = report.claimedBy;
        this.category = report.category;
        this.priority = report.priority;
        this.dueBy = report.dueBy;
        this.claimedAt = report.claimedAt;

        // Legacy compatibility
        this.reportedPlayerName = null; // Will be resolved by caller if needed
        this.timestamp = report.created;
    }

    public ReportedPlayer(UUID id, UUID reporter, UUID target, String reason, long created,
                          String status, UUID claimedBy, String category, String priority,
                          long dueBy, long claimedAt) {
        this.id = id;
        this.reporter = reporter;
        this.target = target;
        this.reason = reason;
        this.created = created;
        this.status = status;
        this.claimedBy = claimedBy;
        this.category = category;
        this.priority = priority;
        this.dueBy = dueBy;
        this.claimedAt = claimedAt;

        // Legacy compatibility
        this.reportedPlayerName = null;
        this.timestamp = created;
    }

    public ReportManager.Report toReport() {
        return new ReportManager.Report(id, reporter, target, reason, created,
                                       status, claimedBy, category, priority, dueBy, claimedAt);
    }

    public static ReportedPlayer fromReport(ReportManager.Report report) {
        if (report == null) return null;
        return new ReportedPlayer(report);
    }
}
