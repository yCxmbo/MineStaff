package me.ycxmbo.mineStaff.channels;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a private staff channel
 */
public class StaffChannel {
    private final String id;
    private final String name;
    private final String displayName;
    private final String format;
    private final String permission;
    private final boolean crossServer;
    private final Set<UUID> members = new HashSet<>();

    public StaffChannel(String id, String name, String displayName, String format, String permission, boolean crossServer) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.format = format;
        this.permission = permission;
        this.crossServer = crossServer;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFormat() {
        return format;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isCrossServer() {
        return crossServer;
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public void addMember(UUID player) {
        members.add(player);
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public boolean hasMember(UUID player) {
        return members.contains(player);
    }
}
