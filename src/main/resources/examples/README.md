# MineStaff Example Configurations

This directory contains pre-configured example configuration files optimized for different server sizes and use cases.

## Available Configurations

### 1. `config-small-server.yml`
**Best for:** Servers with < 50 players

**Features:**
- YAML storage (no database setup required)
- Staff login disabled (optional for trusted teams)
- Shorter teleport ranges
- Reduced snapshot storage
- Single-server setup
- Minimal resource usage

**Use this if:** You run a small community server with a trusted staff team and want a simple, easy-to-setup configuration.

---

### 2. `config-medium-server.yml`
**Best for:** Servers with 50-200 players

**Features:**
- MySQL storage for better performance
- Staff login enabled
- Discord webhooks integration
- Standard moderation tools
- Longer data retention
- Single or small multi-server setup

**Use this if:** You run a growing server with multiple staff members and need better performance and accountability.

---

### 3. `config-large-network.yml`
**Best for:** Networks with 200+ players across multiple servers

**Features:**
- MySQL storage (required)
- Redis for cross-server communication
- 2FA authentication enabled
- Cross-server reports and staff chat
- Enhanced security features
- Maximum data retention
- Discord integration
- Network-wide synchronization

**Use this if:** You run a large network with multiple servers and need enterprise-grade staff management with cross-server features.

---

## How to Use

1. Choose the configuration that best matches your server size
2. Copy the example file to your `plugins/MineStaff/` directory as `config.yml`
3. Edit the configuration to match your setup:
   - For MySQL: Update database credentials
   - For Redis: Update Redis connection details
   - For Discord: Add your webhook URLs
4. Restart your server
5. Enjoy!

## Need Help?

- Small servers: Most options can be left as default
- Medium servers: Focus on configuring MySQL and Discord webhooks
- Large networks: Ensure MySQL and Redis are properly configured for optimal performance

## Customization Tips

- **Teleport ranges:** Adjust based on your world size
- **Cooldowns:** Increase for larger player counts to prevent spam
- **History days:** Balance between disk space and auditing needs
- **2FA:** Enable for any server handling sensitive data
- **Cross-server:** Only enable if you have multiple servers in your network

---

**Need more help?** Check the main plugin documentation or open an issue on GitHub!
