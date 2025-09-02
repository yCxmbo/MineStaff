package me.ycxmbo.mineStaff.spy;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpyManager {
    private final Set<UUID> cmd = new HashSet<>();
    private final Set<UUID> social = new HashSet<>();

    public boolean isCommandSpy(UUID id) { return cmd.contains(id); }
    public void setCommandSpy(UUID id, boolean v) { if (v) cmd.add(id); else cmd.remove(id); }

    public boolean isSocialSpy(UUID id) { return social.contains(id); }
    public void setSocialSpy(UUID id, boolean v) { if (v) social.add(id); else social.remove(id); }
}

