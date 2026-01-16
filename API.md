# MineStaff API Documentation

## Overview

MineStaff provides a comprehensive API for third-party plugins to integrate with staff management features. The API uses a service provider pattern and includes methods for managing staff mode, vanish, freeze, reports, infractions, notes, and tickets, along with custom events for listening to key plugin actions.

## Getting Started

### Accessing the API

```java
import me.ycxmbo.mineStaff.api.MineStaffAPI;

public class MyPlugin extends JavaPlugin {
    
    private MineStaffAPI api;
    
    @Override
    public void onEnable() {
        // Get API via service provider pattern
        var apiOpt = MineStaffAPI.get();
        
        if (apiOpt.isEmpty()) {
            getLogger().warning("MineStaff API not available!");
            return;
        }
        
        this.api = apiOpt.get();
        getLogger().info("Successfully hooked into MineStaff API");
    }
}
```

### Adding as a Dependency

Add MineStaff as a dependency in your `plugin.yml`:

```yaml
depend: [MineStaff]
# or
softdepend: [MineStaff]
```

## API Methods

### Staff Mode Management

```java
// Check if player is in staff mode
boolean isStaffMode = api.isStaffMode(player);
boolean isStaffMode = api.isStaffMode(playerUUID);

// Toggle staff mode
boolean newState = api.setStaffMode(player, true, MineStaffAPI.ToggleCause.API);

// Get snapshot of player's staff state
MineStaffAPI.StaffSnapshot snapshot = api.snapshot(player);
```

### Vanish Management

```java
// Check if player is vanished
boolean isVanished = api.isVanished(player);
boolean isVanished = api.isVanished(playerUUID);

// Toggle vanish
boolean newState = api.setVanish(player, true, MineStaffAPI.ToggleCause.API);
```

### Freeze Management

```java
// Check if player is frozen
boolean isFrozen = api.isFrozen(player);
boolean isFrozen = api.isFrozen(playerUUID);

// Toggle freeze
boolean newState = api.setFrozen(player, true, MineStaffAPI.ToggleCause.API);
```

**Toggle Causes:**
- `COMMAND` - Triggered by command
- `TOOL` - Triggered by staff tool
- `API` - Triggered by API call
- `OTHER` - Other sources

### Report Management

```java
// Create a new report
ReportedPlayer report = api.createReport(reporter, reported, "Reason for report");

// Get all active reports
List<ReportedPlayer> activeReports = api.getActiveReports();

// Get reports for a specific player
List<ReportedPlayer> reports = api.getReportsFor(player);

// Close a report
boolean success = api.closeReport(reportId, staffMember);
```

### Infraction Management

```java
// Add an infraction
int infractionId = api.addInfraction(player, staffMember, "WARN", "Breaking server rules");

// Get all infractions for a player
List<InfractionManager.Infraction> infractions = api.getInfractions(player);

// Get infraction count
int count = api.getInfractionCount(player);

// Remove an infraction
boolean success = api.removeInfraction(infractionId);
```

**Infraction Types:**
- `WARN` - Warning
- `MUTE` - Mute
- `KICK` - Kick
- `BAN` - Ban

### Note Management

```java
// Add a staff note
int noteId = api.addNote(player, staffMember, "Player was warned about behavior");

// Get all notes for a player
List<InfractionManager.Note> notes = api.getNotes(player);

// Remove a note
boolean success = api.removeNote(noteId);
```

### Ticket Management

```java
// Create a staff ticket
UUID ticketId = api.createTicket(
    creator,
    "Need help with permissions",
    "Cannot access admin commands",
    "TECHNICAL",
    "HIGH"
);

// Get a specific ticket
StaffTicket ticket = api.getTicket(ticketId);

// Get all open tickets
List<StaffTicket> openTickets = api.getOpenTickets();

// Claim a ticket
boolean success = api.claimTicket(ticketId, staffMember);

// Add comment to ticket
boolean success = api.addTicketComment(ticketId, author, "Working on this now");

// Resolve a ticket
boolean success = api.resolveTicket(ticketId, resolver);
```

**Ticket Categories:**
- `QUESTION` - General question
- `TECHNICAL` - Technical issue
- `PERMISSION` - Permission problem
- `OTHER` - Other issues

**Ticket Priorities:**
- `LOW` - Low priority
- `MEDIUM` - Medium priority
- `HIGH` - High priority
- `URGENT` - Urgent

## Events

MineStaff fires custom events that your plugin can listen to.

### StaffModeToggleEvent

Fired when a player toggles staff mode.

```java
import me.ycxmbo.mineStaff.api.events.StaffModeToggleEvent;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {
    
    @EventHandler
    public void onStaffModeToggle(StaffModeToggleEvent event) {
        Player player = event.getPlayer();
        boolean enabled = event.isEnabled();
        MineStaffAPI.ToggleCause cause = event.getCause();
        
        if (enabled) {
            player.sendMessage("You entered staff mode via " + cause);
        } else {
            player.sendMessage("You left staff mode");
        }
    }
}
```

### VanishToggleEvent

Fired when a player's vanish state changes.

```java
import me.ycxmbo.mineStaff.api.events.VanishToggleEvent;

@EventHandler
public void onVanish(VanishToggleEvent event) {
    Player player = event.getPlayer();
    boolean enabled = event.isEnabled();
    MineStaffAPI.ToggleCause cause = event.getCause();
    
    if (enabled) {
        // Player is now vanished
    } else {
        // Player is now visible
    }
}
```

### FreezeToggleEvent

Fired when a player is frozen or unfrozen.

```java
import me.ycxmbo.mineStaff.api.events.FreezeToggleEvent;

@EventHandler
public void onFreeze(FreezeToggleEvent event) {
    Player player = event.getPlayer();
    boolean enabled = event.isEnabled();
    MineStaffAPI.ToggleCause cause = event.getCause();
    
    if (enabled) {
        // Player is now frozen
    } else {
        // Player is now unfrozen
    }
}
```

### PlayerReportEvent

Fired when a player is reported. **Cancellable**.

```java
import me.ycxmbo.mineStaff.api.events.PlayerReportEvent;

@EventHandler
public void onReport(PlayerReportEvent event) {
    Player reporter = event.getReporter();
    OfflinePlayer reported = event.getReported();
    String reason = event.getReason();
    
    // Log to external system
    logToDatabase(reporter, reported, reason);
    
    // Cancel to prevent report creation
    // event.setCancelled(true);
}
```

### InfractionAddEvent

Fired when an infraction is added to a player. **Cancellable**.

```java
import me.ycxmbo.mineStaff.api.events.InfractionAddEvent;

@EventHandler
public void onInfraction(InfractionAddEvent event) {
    OfflinePlayer player = event.getPlayer();
    Player staff = event.getStaff();
    String type = event.getType(); // WARN, MUTE, KICK, BAN
    String reason = event.getReason();
    
    if (type.equals("BAN")) {
        // Special handling for bans
        notifyAdmins("Ban issued by " + staff.getName());
    }
    
    // Cancel to prevent infraction
    // event.setCancelled(true);
}
```

### StaffTicketCreateEvent

Fired when a staff ticket is created. **Cancellable**.

```java
import me.ycxmbo.mineStaff.api.events.StaffTicketCreateEvent;

@EventHandler
public void onTicketCreate(StaffTicketCreateEvent event) {
    UUID ticketId = event.getTicketId();
    Player creator = event.getCreator();
    String subject = event.getSubject();
    String category = event.getCategory();
    String priority = event.getPriority();
    
    if (priority.equals("URGENT")) {
        // Send notification to senior staff
        notifySeniorStaff("Urgent ticket created: " + subject);
    }
    
    // Cancel to prevent ticket creation
    // event.setCancelled(true);
}
```

### CPSCheckStartEvent & CPSCheckFinishEvent

Fired when CPS checks start and finish.

```java
import me.ycxmbo.mineStaff.api.events.CPSCheckStartEvent;
import me.ycxmbo.mineStaff.api.events.CPSCheckFinishEvent;

@EventHandler
public void onCPSCheckStart(CPSCheckStartEvent event) {
    Player target = event.getTarget();
    Player staff = event.getStaff();
    // CPS check started
}

@EventHandler
public void onCPSCheckFinish(CPSCheckFinishEvent event) {
    Player target = event.getTarget();
    double cps = event.getCPS();
    // CPS check finished
}
```

## Example Integration

Here's a complete example plugin that integrates with MineStaff:

```java
package com.example.minestaff.integration;

import me.ycxmbo.mineStaff.api.MineStaffAPI;
import me.ycxmbo.mineStaff.api.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MineStaffIntegrationPlugin extends JavaPlugin implements Listener {
    
    private MineStaffAPI api;
    
    @Override
    public void onEnable() {
        // Hook into MineStaff API
        var apiOpt = MineStaffAPI.get();
        
        if (apiOpt.isEmpty()) {
            getLogger().severe("MineStaff API not available! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        this.api = apiOpt.get();
        getLogger().info("Hooked into MineStaff API");
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onStaffModeToggle(StaffModeToggleEvent event) {
        if (event.isEnabled()) {
            getLogger().info(event.getPlayer().getName() + " entered staff mode");
            
            // Automatically vanish staff when they enter staff mode
            api.setVanish(event.getPlayer(), true, MineStaffAPI.ToggleCause.API);
        }
    }
    
    @EventHandler
    public void onReport(PlayerReportEvent event) {
        // Track reports in external database
        saveReportToDatabase(
            event.getReporter().getUniqueId(),
            event.getReported().getUniqueId(),
            event.getReason()
        );
    }
    
    @EventHandler
    public void onInfraction(InfractionAddEvent event) {
        if (event.getType().equals("BAN")) {
            // Send webhook notification on bans
            sendDiscordWebhook(
                "Ban issued to " + event.getPlayer().getName() + 
                " by " + event.getStaff().getName() + 
                " for: " + event.getReason()
            );
        }
    }
    
    private void saveReportToDatabase(UUID reporter, UUID reported, String reason) {
        // Implementation here
    }
    
    private void sendDiscordWebhook(String message) {
        // Implementation here
    }
}
```

## Best Practices

1. **Always check for API availability**: Use `MineStaffAPI.get()` which returns an `Optional`.

2. **Handle empty results**: Methods may return empty lists or null if data doesn't exist.

3. **Check return values**: Many methods return boolean to indicate success/failure.

4. **Use soft dependencies**: Use `softdepend` instead of `depend` if MineStaff is optional.

5. **Respect event cancellation**: If your plugin cancels events, document this behavior clearly.

6. **Use appropriate ToggleCause**: When using the API to toggle states, use `ToggleCause.API`.

## Thread Safety

Most API methods should be called from the main server thread. For async operations, use:

```java
Bukkit.getScheduler().runTask(plugin, () -> {
    // API calls here
});
```

## Support

For issues or questions about the API:
- GitHub: https://github.com/ycxmbo/MineStaff

## Changelog

### API v1.0
- Initial API release with service provider pattern
- Staff mode, vanish, and freeze management
- Report management with events
- Infraction and note management with events
- Ticket management with events
- CPS check events
- All core events are cancellable where appropriate
