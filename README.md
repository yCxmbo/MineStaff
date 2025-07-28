# MineStaff

MineStaff is a powerful and easy-to-use staff mode plugin for Minecraft Paper 1.20+ servers. It equips server moderators with essential tools to manage players efficiently, including vanish, freeze, inspect, teleport, and more — all configurable to fit your server’s needs.

## Features

- **Toggle Staff Mode** with `/staffmode` or `/sm` commands  
- **Vanish**: Become invisible to regular players, toggle on/off easily  
- **Freeze**: Freeze and unfreeze players to stop their movement  
- **Inspect**: View other players’ inventories  
- **Random Teleport**: Teleport to a random online player (excluding other staff)  
- **Staff List**: View a list of all staff currently in staff mode via `/stafflist`  
- **Configurable Tool Slots**: Customize the inventory slots where staff tools appear  
- **Customizable Messages**: Change all notification messages via the config.yml  

## Installation

1. Download the latest MineStaff plugin `.jar` file.  
2. Place the `.jar` file into your server’s `plugins` folder.  
3. Start or restart your Paper 1.20+ server.  
4. A default `config.yml` will be generated in the `plugins/MineStaff` folder.  
5. Customize the config as needed, then reload or restart the server again.  

## Usage

- Use `/staffmode` or `/sm` to toggle staff mode on/off.  
- When in staff mode, you will receive a set of staff tools in your inventory.  
- Right-click a player with the **Inspect** tool to view their inventory.  
- Right-click a player with the **Freeze** tool to freeze or unfreeze them.  
- Use the **Random Teleport** tool to teleport to a random non-staff player.  
- Use the **Toggle Vanish** tool to become invisible/visible to non-staff players.  
- Run `/stafflist` to see who is currently in staff mode.  

## Configuration

Edit the `config.yml` to customize tool slots and messages:

```yaml
tool-slots:
  inspect: 0
  teleport: 1
  freeze: 2
  vanish: 3

messages:
  freeze_notify: "&eYou have been frozen by a staff member."
  unfreeze_notify: "&aYou have been unfrozen."
  teleport_failed: "No players to teleport to."
Reload the plugin or server after making changes.

Permissions
Permission	Description
staffmode.use	Allows toggling staff mode and using staff tools

Make sure to assign this permission to your staff roles.

Contribution and Support
Found a bug or have a feature request? Feel free to open an issue or submit a pull request on the GitHub repository.

For support, contact the plugin maintainer or join the community Discord linked on the GitHub page.

License
This project is licensed under the MIT License — see the LICENSE file for details.
