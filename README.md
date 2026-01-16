# MineStaff

**The ultimate staff management plugin for Minecraft servers.**
MineStaff provides a comprehensive, all-in-one toolkit for staff teams with advanced features including cross-server coordination, enhanced GUIs, developer API, and extensive customization options.

> ✅ Compatible with Paper, Purpur, and Spigot servers running Minecraft **1.20.x – 1.21.x**.

---

## ✨ Core Features

### 🛡️ Staff Mode
- Toggle with `/staffmode` (aliases: `/staff`, `/sm`)
- Customizable staff tools: Teleport, Inspect, Freeze, Vanish, CPS Check
- Tool protection (prevents moving/removing staff tools)
- Inventory saving/restoration on mode toggle
- Gamemode switching with configurable mode
- Sound and particle effects

### 💬 Communication Systems

**Staff Chat**
- Private staff-only chat via `/staffchat` or `/sc`
- Toggle mode or use one-off messages
- Cross-server support via Redis

**Private Staff Channels**
- Isolated communication channels for different roles
- `/channel` to manage channel memberships
- Pre-configured channels: admin, mod, staff
- Cross-server channel support
- Toggle channel mode for automatic routing

### 📋 Report & Moderation Systems

**Advanced Reporting**
- `/report <player> <reason>` to file player reports
- Interactive GUI-based report review with filtering
- Report history viewer per player (`/reporthistory`)
- **Network-wide report synchronization** (cross-server)
- Real-time notifications to all staff
- Evidence attachment system
- SLA tracking with overdue indicators

**Infractions & Warnings**
- Comprehensive infractions system (ban, warn, mute, kick)
- Warning system with severity levels and durations
- Player notes system for internal documentation
- Integration with **LiteBans** for punishment history
- GUI-based management interfaces

**Staff Support Tickets**
- Internal help desk for staff coordination
- `/ticket` command with full lifecycle management
- Categories: QUESTION, TECHNICAL, PERMISSION, OTHER
- Priority levels: LOW, MEDIUM, HIGH, URGENT
- Comment threads on tickets
- Interactive GUI with filtering and statistics

### 🔍 Investigation Tools

**Player Inspection**
- `/inspect <player>` for comprehensive player data
- View inventory, ender chest, health, potion effects
- Offline player inspection support
- Profile GUI with all player information

**Inventory Rollback**
- Death inventory restoration system
- GUI-based rollback with timestamp tracking
- View and restore previous inventories

**CoreProtect Integration**
- `/co` command for block logging lookups
- Block, player, and nearby change queries
- Rollback and restore operations
- Time and radius filtering
- Audit logging for all operations

**CPS Checker**
- `/cpscheck <player>` to measure clicks per second
- 10-second measurement window
- Staff alert events when checks finish
- Right-click players with CPS Check tool

### 🎮 Staff Utilities

**Follow Mode**
- `/follow <player>` to continuously teleport to a player
- Auto-teleport on player movement
- Toggle on/off for different targets

**Freeze System**
- `/freeze <player> [seconds]` to immobilize players
- Prevent movement, combat, and commands
- Optional timed freezes
- Screen effects for frozen players

**Vanish System**
- Invisible to non-staff players
- Tab list hiding
- Interaction prevention
- Cross-server vanish support
- Permission-based vanish seeing

**Command & Social Spy**
- Monitor all player commands (`/commandspy`)
- View private messages (`/socialspy`)
- Real-time tracking of player activity

### 📊 Enhanced GUIs

All GUIs now feature:
- **🔎 Search functionality** - Real-time content filtering
- **📄 Improved pagination** - Page numbers, jump-to-page
- **🔄 Sort options** - 8+ sort modes (date, priority, name, status)
- **🎯 Multi-filter support** - Simultaneous filters with visual indicators
- **✅ Bulk actions** - Shift-click selection for batch operations
- **💡 Hover tooltips** - Context-sensitive action hints
- Interactive controls with click-based cycling

### 🌐 Cross-Server Features (Redis)

**Cross-Server Teleportation**
- `/csteleport <player>` to teleport to players on any server
- Automatic server switching via BungeeCord/Velocity
- Network-wide player location queries

**Global Staff List**
- `/globalstafflist` to view all staff across the network
- Real-time staff tracking with status indicators
- Shows staff mode and vanish status per server
- Auto-updates every 10 seconds

**Unified Report System**
- Network-wide report synchronization
- Reports visible across all servers
- Real-time updates for claims and closures
- Origin server tracking

### 🔐 Security Features

**Staff Login System**
- `/stafflogin <password>` required before staff actions
- Configurable password and max attempts
- Movement and command blocking until authenticated
- Optional 2FA/TOTP support (`/staff2fa`)
- BCrypt password hashing

**Permission System**
- Granular permission nodes for all features
- Bypass permissions for special cases
- Role-based channel access control

### 💾 Data Management

**Storage Options**
- YAML (default) or SQL (MySQL/SQLite) storage
- HikariCP connection pooling for performance

**Data Migration**
- `/migrate` to convert between YAML and SQL
- Bidirectional migration support
- Automatic backups before migration
- Progress tracking with detailed statistics

**Backup System**
- `/backupdata` for manual data backups
- Automated backup scheduling
- Backup request system (`/backup`) for staff coordination
- Restore from previous backups

### 🔧 Customization

**Sound System**
- Fully customizable sound effects
- Per-action sound configuration
- Volume and pitch control
- Particle effect integration

**Message System**
- Supports MiniMessage & legacy color codes
- Hover tooltips and click actions
- Configurable alert formatting
- Multi-line message support

### 🔌 Developer API

Comprehensive API for third-party integrations:

**API Methods**
- Staff mode, vanish, freeze management
- Report creation and management
- Infraction and note operations
- Ticket system integration
- Network staff queries

**Custom Events**
- `StaffModeToggleEvent` - Staff mode state changes
- `VanishToggleEvent` - Vanish state changes
- `FreezeToggleEvent` - Freeze state changes
- `PlayerReportEvent` - New reports (cancellable)
- `InfractionAddEvent` - New infractions (cancellable)
- `StaffTicketCreateEvent` - New tickets (cancellable)
- `CPSCheckStartEvent` / `CPSCheckFinishEvent` - CPS checks

See [API.md](API.md) for complete documentation.

---

## 📜 Commands

### Core Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/staffmode`, `/staff`, `/sm` | Toggle staff mode | `staffmode.toggle` |
| `/stafflogin <password>` | Log in as staff | `staffmode.login` |
| `/staff2fa enable\|confirm\|disable\|status` | Manage 2FA | `staffmode.login` |
| `/staffreload`, `/sr` | Reload configuration | `staffmode.reload` |
| `/staffduty`, `/duty` | Toggle staff duty | `staffmode.duty` |

### Communication
| Command | Description | Permission |
|---------|-------------|------------|
| `/staffchat <message>`, `/sc` | Staff-only chat | `staffmode.chat` |
| `/channel [list\|join\|toggle\|info]` | Manage private channels | `staffmode.channel` |
| `/staffhelp [category]`, `/shelp` | Categorized help system | - |

### Reports & Moderation
| Command | Description | Permission |
|---------|-------------|------------|
| `/report <player> <reason>` | Report a player | `staffmode.report` |
| `/reports`, `/reportsgui` | Open reports GUI | `staffmode.alerts` |
| `/reporthistory <player>`, `/rh` | View report history | `staffmode.reports.history` |
| `/evidence <reportId> add\|list` | Manage report evidence | `staffmode.alerts` |

### Infractions & Warnings
| Command | Description | Permission |
|---------|-------------|------------|
| `/infractions <player>` | View/issue infractions | `staffmode.infractions` |
| `/warn <player> <reason>` | Issue warnings | `staffmode.warn` |
| `/notes <player> add\|list\|remove` | Manage player notes | `staffmode.notes` |

### Investigation Tools
| Command | Description | Permission |
|---------|-------------|------------|
| `/inspect <player>`, `/insp` | Inspect player | `staffmode.inspect` |
| `/profile <player>` | Open player profile | `staffmode.inspect` |
| `/inspectoffline <player> [ec]` | Inspect offline inventory | `staffmode.inspect` |
| `/rollback` | Open rollback GUI | `staffmode.rollback` |
| `/cpscheck <player>` | Run CPS check | `staffmode.cpscheck` |
| `/co <block\|player\|nearby\|rollback\|restore>` | CoreProtect integration | `staffmode.coreprotect` |

### Staff Utilities
| Command | Description | Permission |
|---------|-------------|------------|
| `/freeze <player> [seconds]` | Freeze/unfreeze player | `staffmode.freeze` |
| `/follow <player>` | Follow a player | `staffmode.follow` |
| `/commandspy`, `/cmdspy` | Toggle command spy | `staffmode.spy` |
| `/socialspy`, `/sspy` | Toggle social spy | `staffmode.spy` |

### Staff Lists
| Command | Description | Permission |
|---------|-------------|------------|
| `/stafflistgui`, `/slg` | Open staff list GUI | `staffmode.stafflist.gui` |
| `/stafflist`, `/sl` | Show staff list in chat | `staffmode.stafflist.text` |
| `/globalstafflist`, `/gsl` | View network-wide staff | `staffmode.stafflist.global` |

### Tickets & Coordination
| Command | Description | Permission |
|---------|-------------|------------|
| `/ticket [create\|list\|view\|claim]` | Manage staff tickets | `staffmode.tickets` |
| `/backup [reason]` | Request backup from staff | `staffmode.backup` |

### Cross-Server
| Command | Description | Permission |
|---------|-------------|------------|
| `/csteleport <player>`, `/cstp` | Cross-server teleport | `staffmode.teleport.crossserver` |

### Data Management
| Command | Description | Permission |
|---------|-------------|------------|
| `/backupdata [create\|list\|restore]` | Manage data backups | `staffmode.backup.manage` |
| `/migrate <yaml-to-sql\|sql-to-yaml>` | Migrate storage format | `staffmode.admin` |

---

## 🔑 Key Permissions

### Core
- `staffmode.toggle` - Enter staff mode
- `staffmode.use` - Use staff mode features
- `staffmode.login` - Access to staff login
- `staffmode.reload` - Reload configuration

### Tools
- `staffmode.teleport` - Teleport tool
- `staffmode.inspect` - Inspect tool
- `staffmode.freeze` - Freeze players
- `staffmode.vanish` - Vanish tool
- `staffmode.see` - See vanished staff

### Moderation
- `staffmode.report` - File reports
- `staffmode.alerts` - View reports GUI
- `staffmode.infractions` - Manage infractions
- `staffmode.warn` - Issue warnings
- `staffmode.notes` - Player notes

### Advanced
- `staffmode.coreprotect` - CoreProtect lookups
- `staffmode.coreprotect.rollback` - Perform rollbacks
- `staffmode.teleport.crossserver` - Cross-server teleport
- `staffmode.stafflist.global` - View global staff list
- `staffmode.network.reports` - Network-wide reports

### Channels
- `staffmode.channel` - Use private channels
- `staffmode.channel.admin` - Access admin channel
- `staffmode.channel.mod` - Access moderator channel
- `staffmode.channel.staff` - Access staff channel

### Tickets & Support
- `staffmode.tickets` - Use ticket system
- `staffmode.tickets.notify` - Receive ticket notifications
- `staffmode.backup` - Request backup
- `staffmode.backup.receive` - Receive backup requests

### Administration
- `staffmode.admin` - Administrative commands
- `staffmode.backup.manage` - Data backup management

---

## ⚙️ Configuration

### config.yml
The main configuration file allows customization of:
- Staff tools (slots, materials, names, lore)
- Messages (MiniMessage & legacy color codes)
- Login system (passwords, 2FA, max attempts)
- Staff mode settings (gamemode, spawn teleport)
- Tool cooldowns and limits
- Feature toggles

### sounds.yml
Configure sound effects for:
- Staff mode toggle
- Tool usage
- Alerts and notifications
- Report actions
- Each sound has volume, pitch, and particle options

### Redis Configuration (Cross-Server)
Required for cross-server features:
```yaml
redis:
  enabled: true
  host: localhost
  port: 6379
  password: ""
  ssl: false
  channels:
    staffchat: "minestaff:sc"
    reports: "minestaff:reports"
    alerts: "minestaff:alerts"
    staffchannels: "minestaff:channels"
```

Also requires:
```yaml
server-name: "hub"  # Unique identifier for this server
```

### Storage Configuration
Choose between YAML and SQL:
```yaml
storage:
  mode: "yaml"  # or "mysql", "sqlite"
  # MySQL settings
  mysql:
    host: "localhost"
    port: 3306
    database: "minestaff"
    username: "root"
    password: "password"
```

---

## 🔌 Dependencies

### Required
- [PaperMC / Purpur / Spigot 1.20.x – 1.21.x](https://papermc.io/)

### Optional Integrations
- [Redis](https://redis.io/) - For cross-server features
- [BungeeCord](https://www.spigotmc.org/wiki/bungeecord/) or [Velocity](https://velocitypowered.com/) - For network functionality
- [LiteBans](https://www.spigotmc.org/resources/litebans.3715/) - Punishment history integration
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) - Placeholder support
- [Essentials](https://essentialsx.net/) - Spawn teleport integration
- [Vulcan](https://www.spigotmc.org/resources/vulcan-anti-cheat.83626/) - Anti-cheat alerts
- [CoreProtect](https://www.spigotmc.org/resources/coreprotect.8631/) - Block logging integration

---

## 🧩 API

MineStaff provides a comprehensive API for third-party plugin developers:

### Quick Start
```java
import me.ycxmbo.mineStaff.api.MineStaffAPI;

// Get API instance
MineStaffAPI.get().ifPresent(api -> {
    // Check staff status
    if (api.isStaffMode(player)) {
        // Player is in staff mode
    }

    // Create a report
    api.createReport(reporter, reported, "Reason");

    // Add an infraction
    int id = api.addInfraction(player, staff, "WARN", "Reason");

    // Create a ticket
    UUID ticketId = api.createTicket(
        creator,
        "Need help",
        "Description",
        "TECHNICAL",
        "HIGH"
    );
});
```

### Event Handling
```java
import me.ycxmbo.mineStaff.api.events.*;

@EventHandler
public void onStaffModeToggle(StaffModeToggleEvent event) {
    Player player = event.getPlayer();
    boolean enabled = event.isEnabled();
    MineStaffAPI.ToggleCause cause = event.getCause();

    // Do something when staff mode is toggled
}

@EventHandler
public void onReport(PlayerReportEvent event) {
    if (someCondition) {
        event.setCancelled(true); // Cancel the report
    }
}
```

### Available Events
- `StaffModeToggleEvent` - Staff mode changes
- `VanishToggleEvent` - Vanish state changes
- `FreezeToggleEvent` - Freeze state changes
- `PlayerReportEvent` - New reports (cancellable)
- `InfractionAddEvent` - New infractions (cancellable)
- `StaffTicketCreateEvent` - New tickets (cancellable)
- `CPSCheckStartEvent` - CPS check started
- `CPSCheckFinishEvent` - CPS check completed

For complete API documentation, see [API.md](API.md).

---

## 📦 Installation

1. Download the latest `MineStaff.jar` from releases
2. Place in your server's `plugins/` folder
3. Start the server to generate default configuration
4. Edit `plugins/MineStaff/config.yml` to your preferences
5. Reload with `/staffreload` or restart the server

### Network Setup (Optional)
For cross-server features:
1. Install Redis on your network
2. Configure Redis connection in each server's `config.yml`
3. Set unique `server-name` for each server
4. Install BungeeCord or Velocity on your proxy
5. Restart all servers

---

## 🎯 Feature Highlights

### What Makes MineStaff Different?

✅ **Complete Solution** - Everything staff needs in one plugin
✅ **Enhanced GUIs** - Search, sort, filter, pagination, bulk actions
✅ **Cross-Server Ready** - Network-wide coordination via Redis
✅ **Developer Friendly** - Comprehensive API with custom events
✅ **Performance Focused** - Async operations, connection pooling
✅ **Highly Customizable** - Every message, sound, and feature configurable
✅ **Modern Tech Stack** - Adventure API, MiniMessage, BCrypt, HikariCP
✅ **Security First** - BCrypt passwords, 2FA support, audit logging
✅ **Active Development** - Regular updates and feature additions

---

## 🤝 Support

- **Issues**: [GitHub Issues](https://github.com/ycxmbo/MineStaff/issues)
- **Documentation**: See `API.md` for developer documentation
- **Configuration**: Check `config.yml` comments for all options

---

## 📄 License

Copyright © 2024 ycxmbo. All rights reserved.

---

## 🌟 Credits

Developed by **ycxmbo**

Special thanks to:
- The Paper/Spigot community
- All contributors and testers
- Library developers (Kyori, HikariCP, Jedis, BCrypt)
