# MineStaff

**The ultimate staff management plugin for Minecraft servers.**  
MineStaff provides a powerful, all-in-one toolkit for staff teams. It combines moderation tools, GUIs, alerts, and integrations into a single lightweight plugin designed for performance and extensibility.

> ✅ Compatible with Paper, Purpur, and Spigot servers running Minecraft **1.20.x – 1.21.x**.

---

## ✨ Features

- **Staff Mode**
    - Toggle with `/staffmode` (alias: `/staff`)
    - Customizable staff tools: Teleport, Inspect, Freeze, Vanish, CPS Check (Clock)
    - Prevents moving/removing staff tools (guarded)

- **Staff Chat**
    - Private staff-only chat via `/staffchat` or `/sc`
    - Toggle mode or use one-off messages

- **Reports & Infractions**
    - `/report <player> <reason>` to file player reports
    - GUI-based report review
    - Infractions system with punishments (ban, warn, mute, kick, etc.)
    - Integration with **LiteBans** to show punishments in GUI

- **CPS Checker**
    - `/cpscheck <player>` to measure clicks per second over 10s, or right-click players with the CPS Check staff tool
    - Staff alert events when checks finish

- **Staff List GUI**
    - `/stafflist` or `/stafflistgui`
    - Displays online staff with ping and world info
    - Fully read-only (no item stealing)

- **Inventory Inspector**
    - Inspect player inventory, ender chest, health, potion effects
    - Inventory Rollback GUI for restoring deaths

- **Silent Chest Opening**
    - Open player chests silently in staff mode
    - View-only (items cannot be taken)

- **Staff Login System**
    - Staff with permission must log in via `/stafflogin` before moving or using commands
    - Password configurable in `config.yml`

- **Alerts System**
    - Suspicious activity alerts
    - Name/skin change alerts
    - Plugin-triggered alerts from LiteBans

- **Custom Sounds & Effects**
    - Sounds and particles for staff tool usage
    - Configurable in `config.yml`

- **PlaceholderAPI Support**
    - Expose staff placeholders for other plugins

- **Persistence**
    - Vanish state saved across server restarts
    - Staff inventories restored when leaving staff mode

---

## 📜 Commands

| Command              | Description                                | Permission                |
|----------------------|--------------------------------------------|---------------------------|
| `/staffmode` `/staff`| Toggle staff mode                          | `staffmode.use`           |
| `/stafflogin`        | Log in as staff                            | `staffmode.login`         |
| `/staffchat` `/sc`   | Staff-only chat                            | `staffmode.chat`          |
| `/report`            | Report a player                            | `staffmode.report`        |
| `/infractions`       | View/issue infractions                     | `staffmode.infractions`   |
| `/rollback`          | Open rollback GUI                          | `staffmode.rollback`      |
| `/cpscheck`          | Run a CPS check on a player                | `staffmode.cpscheck`      |
| `/stafflist(gui)`    | Open staff list GUI                        | `staffmode.stafflist`     |

---

## 🔑 Permissions

- `staffmode.use` – Enter staff mode
- `staffmode.teleport` – Teleport tool
- `staffmode.inspect` – Inspect tool
- `staffmode.freeze` – Freeze players
- `staffmode.vanish` – Vanish tool
- `staffmode.chat` – Use staff chat
- `staffmode.report` – File/view reports
- `staffmode.infractions` – Manage infractions
- `staffmode.rollback` – Rollback inventories
- `staffmode.cpscheck` – Run CPS checks
- `staffmode.stafflist` – View staff list GUI

---

## ⚙️ Configuration

The `config.yml` lets you customize:

- Staff tools (slots, materials, names)
- Messages (supports MiniMessage & legacy color codes)
- Login system (passwords, max attempts)
- Sounds & particle effects
- Alert formatting (with hover/click to teleport)
- Tool cooldowns

---

## 🔌 Dependencies

- [PaperMC / Purpur / Spigot 1.20.x – 1.21.x](https://papermc.io/)
- [LiteBans (optional)](https://www.spigotmc.org/resources/litebans.3715/) for punishment integration
- [PlaceholderAPI (optional)](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholders

---

## 🧩 API

MineStaff exposes a simple API for other plugins:

```java
import me.ycxmbo.mineStaff.api.MineStaffAPI;

MineStaffAPI.get().ifPresent(api -> {
    if (api.isStaffMode(player)) {
        // Do something if the player is in staff mode
    }
});
