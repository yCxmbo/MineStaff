# MineStaff API Documentation

## Overview

MineStaff provides a comprehensive API for third-party plugins to integrate with staff management features. The API includes methods for managing staff mode, vanish, freeze, reports, infractions, notes, and tickets, along with custom events for listening to key plugin actions.

## Getting Started

### Accessing the API

```java
import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import org.bukkit.plugin.Plugin;

public class MyPlugin extends JavaPlugin {
    
    private MineStaffAPI mineStaffAPI;
    
    @Override
    public void onEnable() {
        Plugin mineStaffPlugin = getServer().getPluginManager().getPlugin("MineStaff");
        
        if (mineStaffPlugin == null || !(mineStaffPlugin instanceof MineStaff)) {
            getLogger().warning("MineStaff not found!");
            return;
        }
        
        this.mineStaffAPI = ((MineStaff) mineStaffPlugin).getAPI();
        getLogger().info("Successfully hooked into MineStaff API v" + mineStaffAPI.getVersion());
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
boolean isStaffMode = api.isInStaffMode(player);

// Enable staff mode
boolean success = api.enableStaffMode(player);

// Disable staff mode
boolean success = api.disableStaffMode(player);

// Get all players in staff mode
List<Player> staffPlayers = api.getStaffModePlayers();
```

### Vanish Management

```java
// Check if player is vanished
boolean isVanished = api.isVanished(player);

// Set vanish state
api.setVanished(player, true);  // vanish
api.setVanished(player, false); // unvanish

// Get all vanished players
List<Player> vanished = api.getVanishedPlayers();
```

### Freeze Management

```java
// Check if player is frozen
boolean isFrozen = api.isFrozen(player);

// Freeze a player
boolean success = api.freezePlayer(player, staffMember);

// Unfreeze a player
boolean success = api.unfreezePlayer(player);

// Get all frozen players
List<UUID> frozenPlayers = api.getFrozenPlayers();
```

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

### Utility Methods

```java
// Get plugin version
String version = api.getVersion();

// Check if feature is enabled
boolean enabled = api.isFeatureEnabled("staff-mode.enabled");

// Play configured sound
api.playSound(player, "alert.normal");

// Get plugin instance (use sparingly - prefer API methods)
MineStaff plugin = api.getPlugin();
```

## Events

MineStaff fires custom events that your plugin can listen to. All events are cancellable.

### StaffModeToggleEvent

Fired when a player toggles staff mode.

```java
import me.ycxmbo.mineStaff.api.events.StaffModeToggleEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MyListener implements Listener {
    
    @EventHandler
    public void onStaffModeToggle(StaffModeToggleEvent event) {
        Player player = event.getPlayer();
        
        if (event.isEntering()) {
            // Player is entering staff mode
            player.sendMessage("Welcome to staff mode!");
        } else {
            // Player is leaving staff mode
            player.sendMessage("You left staff mode");
        }
        
        // Cancel the event to prevent staff mode toggle
        // event.setCancelled(true);
    }
}
```

### PlayerVanishEvent

Fired when a player's vanish state changes.

```java
import me.ycxmbo.mineStaff.api.events.PlayerVanishEvent;

@EventHandler
public void onVanish(PlayerVanishEvent event) {
    Player player = event.getPlayer();
    
    if (event.isVanishing()) {
        // Player is vanishing
    } else {
        // Player is unvanishing
    }
    
    // Cancel to prevent vanish state change
    // event.setCancelled(true);
}
```

### PlayerFreezeEvent

Fired when a player is frozen or unfrozen.

```java
import me.ycxmbo.mineStaff.api.events.PlayerFreezeEvent;

@EventHandler
public void onFreeze(PlayerFreezeEvent event) {
    Player player = event.getPlayer();
    Player staff = event.getStaff(); // May be null if done via API
    
    if (event.isFreezing()) {
        // Player is being frozen
        getLogger().info(staff.getName() + " froze " + player.getName());
    } else {
        // Player is being unfrozen
    }
    
    // Cancel to prevent freeze action
    // event.setCancelled(true);
}
```

### PlayerReportEvent

Fired when a player is reported.

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

Fired when an infraction is added to a player.

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

Fired when a staff ticket is created.

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

## Example Integration

Here's a complete example plugin that integrates with MineStaff:

```java
package com.example.minestaff.integration;

import me.ycxmbo.mineStaff.MineStaff;
import me.ycxmbo.mineStaff.api.MineStaffAPI;
import me.ycxmbo.mineStaff.api.events.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MineStaffIntegrationPlugin extends JavaPlugin implements Listener {
    
    private MineStaffAPI api;
    
    @Override
    public void onEnable() {
        // Hook into MineStaff API
        Plugin mineStaffPlugin = getServer().getPluginManager().getPlugin("MineStaff");
        
        if (mineStaffPlugin == null || !(mineStaffPlugin instanceof MineStaff)) {
            getLogger().severe("MineStaff not found! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        this.api = ((MineStaff) mineStaffPlugin).getAPI();
        getLogger().info("Hooked into MineStaff API v" + api.getVersion());
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onStaffModeToggle(StaffModeToggleEvent event) {
        if (event.isEntering()) {
            getLogger().info(event.getPlayer().getName() + " entered staff mode");
            
            // Automatically vanish staff when they enter staff mode
            api.setVanished(event.getPlayer(), true);
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

1. **Always check for null**: Some API methods may return null if the requested data doesn't exist.

2. **Handle exceptions**: Wrap API calls in try-catch blocks to handle potential exceptions gracefully.

3. **Check return values**: Many methods return boolean to indicate success/failure.

4. **Use soft dependencies**: Use `softdepend` instead of `depend` if MineStaff is optional.

5. **Respect event cancellation**: If your plugin cancels events, document this behavior clearly.

6. **Prefer API methods**: Use API methods instead of accessing the plugin instance directly when possible.

7. **Check feature availability**: Use `isFeatureEnabled()` to check if features are enabled before using them.

## Thread Safety

Most API methods should be called from the main server thread. For async operations, use:

```java
Bukkit.getScheduler().runTask(plugin, () -> {
    // API calls here
});
```

## Support

For issues or questions about the API:
- GitHub Issues: https://github.com/ycxmbo/MineStaff/issues
- Documentation: https://github.com/ycxmbo/MineStaff/wiki

## Changelog

### API v1.0
- Initial API release
- Staff mode management
- Vanish management
- Freeze management
- Report management
- Infraction management
- Note management
- Ticket management
- Custom events for all major actions
