# MineStaff

**MineStaff** is an advanced, fully-featured **Staff Mode plugin** for Minecraft Paper servers (`1.20.x+` and tested on `1.21.x`).  
It provides staff members with tools for moderation, player inspection, alerts, and integrated punishments — all while protecting gameplay balance.

---

## ✨ Features

- **Staff Mode Toggle**  
  Enable/disable staff mode with one command.  
  - Automatically switches to **Creative Mode** when enabled  
  - Restores **previous gamemode** when disabled  
  - Gives configurable staff tools in configured hotbar slots  
  - Blocks item spawning from Creative inventory  
  - Prevents moving or removing staff tools  

- **Staff Tools**
  - **Teleport Tool** – Teleport to blocks you look at, with cooldowns and range limits  
  - **Freeze Tool** – Freeze/unfreeze players for investigation  
  - **Inspect Tool** – View target’s inventory, ender chest, potion effects, health, and more  
  - **Vanish Tool** – Toggle vanish mode to hide from other players

- **Staff Chat**  
  Private chat channel for staff members.

- **Player Freeze System**  
  Prevents movement, block breaking, chat, and commands while frozen.

- **Alerts System**
  - Manual staff alerts  
  - Auto alerts for **LiteBans punishments** and **Vulcan Anti-Cheat** flags  
  - Fully formatted with MiniMessage, hover text, and click-to-teleport

- **Action Logger**  
  Logs all staff tool uses and commands for accountability.

- **Configurable Everything**
  - Tool slots  
  - Alert templates & sounds  
  - Teleport range & cooldown  
  - Staff login requirement  

---

## 📥 Installation

1. Download the latest MineStaff JAR from [Releases](../../releases).
2. Place it in your server's `plugins/` folder.
3. Start the server to generate the default `config.yml`.
4. Edit `plugins/MineStaff/config.yml` to your preferences.
5. Reload or restart the server.

---

## ⚙ Configuration

**Default `config.yml`:**
```yaml
messages:
  prefix: "&8[&aMineStaff&8]&r "
  no_permission: "&cYou don't have permission."
  only_players: "&cOnly players can use this."
  staffmode_enabled: "&aStaff Mode enabled."
  staffmode_disabled: "&cStaff Mode disabled."
  login_required: "&eYou must /stafflogin before using staff tools."
  login_success: "&aLogin successful."
  login_failure: "&cIncorrect password."
  password_set: "&aPassword set."
  vanish_on: "&dVanish enabled."
  vanish_off: "&dVanish disabled."
  teleport_cooldown: "&cTeleport cooldown: {seconds}s"
  teleport_blocked: "&cBlocked destination."
  teleport_no_spot: "&cNo safe spot in sight."

options:
  require_login: true
  staffchat_prefix: "@"
  teleport_max_range: 60
  teleport_max_range_sneak: 120
  teleport_cooldown_ms: 1500

tools:
  slots:
    teleport: 0
    freeze: 1
    inspect: 2
    vanish: 8

alerts:
  use_minimessage: true
  template: "<gradient:#19E68C:#13B5FF>[StaffAlert]</gradient> <gray>{content}</gray>"
  hover_template: "<green>Click to teleport to <yellow>{target}</yellow>"
  click_tp: true
  sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
