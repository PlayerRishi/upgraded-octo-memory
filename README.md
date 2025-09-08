# 🌐 PiCraft Plugin

A comprehensive Paper/Spigot plugin for PiCraft Season 2 featuring an advanced report system, automated rule enforcement, smart Discord integration, and web dashboard. Includes combat log detection, seed abuse monitoring, dragon egg tracking, weakness potion prevention, and base radius enforcement.

## ✨ Features

- **Player Reports**: `/report` with required categories and anonymous mode (`-anon`)
- **Staff Tools**: Complete report management system with auto-renumbering
- **Rules System**: `/rules` reads from `rules.txt` file
- **Discord Integration**: Optional DiscordSRV integration with smart notifications
- **Base Tracking**: Automatic coordinate logging and spawn radius monitoring
- **Auto-Detection**: Combat logging, seed abuse, and base radius enforcement
- **Evidence System**: Attach screenshots/videos to reports with `/evidence`
- **Web Dashboard**: Simple HTTP interface for viewing reports
- **SQLite Storage**: Lightweight database with no external dependencies
- **Cooldown System**: Prevents report spam
- **Permission-based**: Works seamlessly with LuckPerms
- **Modern API**: Uses Adventure API instead of deprecated ChatColor

## 🧩 Commands

### Players

- `/report [-anon] <category> <message>` - Creates a report (category required, tab completion available)
- `/rules [page]` - Displays server rules (with optional pagination)

### Staff

- `/reports [page]` - Shows unread + open reports (quick inbox)
- `/reportlist [all|open|closed|unread] [page]` - Full inbox with filters
- `/reportview <id>` - View full report details
- `/reportread <id>` - Mark report as read
- `/reportclear <id>` - Close a report
- `/reportassign <id> <staff>` - Assign report to staff member
- `/reportnotify [on|off|toggle]` - Toggle report notifications
- `/reporttp <id>` - Teleport to report location
- `/evidence <id> <url>` - Attach evidence (screenshots/videos) to reports

## 🔒 Permissions

### Player Permissions
- `picraft.rules` - Use /rules command (default: true)
- `picraft.report.use` - File reports (default: true)
- `picraft.report.use.anon` - Use anonymous reporting (default: true)
- `picraft.report.cooldown.bypass` - Bypass report cooldown (default: op)

### Staff Permissions
- `picraft.report.view` - View reports (default: op)
- `picraft.report.list` - List all reports with filters (default: op)
- `picraft.report.read` - Mark reports as read (default: op)
- `picraft.report.clear` - Close reports (default: op)
- `picraft.report.assign` - Assign reports (default: op)
- `picraft.report.notify` - Toggle notifications (default: op)
- `picraft.report.tp` - Teleport to report locations (default: op)
- `picraft.report.view.realname` - View real reporter behind anonymous reports (default: op)
- `picraft.report.evidence` - Add evidence to reports (default: op)
- `picraft.*` - All permissions (default: op)

## 📦 Quick Installation

1. **Download**: Place `PiCraftPlugin.jar` in your `plugins/` folder
2. **Start Server**: Start the server once to generate config files
3. **Configure**: Edit `plugins/PiCraftPlugin/config.yml` as needed
4. **Rules**: Edit `plugins/PiCraftPlugin/rules.txt` with your server rules
5. **Optional**: Install DiscordSRV for Discord integration
6. **Permissions**: Set up permissions with LuckPerms or your permission plugin
7. **Reload**: Use `/reload confirm` or restart the server

## 🚀 Complete Setup Guide

### **Step 1: Installation**
```bash
# 1. Download PiCraftPlugin-1.0-SNAPSHOT.jar
# 2. Place in plugins/ folder
# 3. Start server (generates config files)
# 4. Check console for "PiCraft Plugin has been enabled!"
```

### **Step 2: Basic Configuration**
```yaml
# Edit plugins/PiCraftPlugin/config.yml
cooldowns:
  report: 30              # Seconds between reports

reports:
  categories:             # Customize report categories
    - "grief"
    - "steal"
    - "hack"
    - "toxic"
    - "combat"
    - "exploit"
    - "spam"
    - "other"
```

### **Step 3: PiCraft Season 2 Auto-Detection**
```yaml
reports:
  auto_detection:
    combat_logging:
      enabled: true
      combat_time: 15       # Combat log detection
    dragon_egg:
      enabled: true         # Dragon egg ender chest prevention
    weakness_potions:
      enabled: true         # Weakness potion blocking
    seed_abuse:
      enabled: true
      structure_limit: 5    # Structure finding limit
      diamond_limit: 128    # Diamond mining limit
    replay_mod:
      enabled: true
      chunk_reload_limit: 5 # F3+A detection
  base_tracking:
    enforce_radius: true
    radius_limit: 2500      # Base radius enforcement
```

### **Step 4: Permissions Setup**
```bash
# Player permissions
/lp group default permission set picraft.rules true
/lp group default permission set picraft.report.use true

# Staff permissions
/lp group mod permission set picraft.report.view true
/lp group mod permission set picraft.report.clear true
/lp group admin permission set picraft.* true
```

### **Step 5: Discord Integration (Optional)**
```yaml
discord:
  enabled: true
  smart_notifications:
    channels:
      grief: "123456789012345678"    # Channel IDs
      hack: "234567890123456789"
      combat: "345678901234567890"
    roles:
      grief: "567890123456789012"    # Role IDs for pings
      hack: "678901234567890123"
      urgent: "890123456789012345"   # Auto-detection alerts
```

### **Step 6: Web Dashboard (Optional)**
```yaml
web_dashboard:
  enabled: true
  port: 8080
  auth_key: "PiCraft2024-StaffOnly"  # CHANGE THIS!
```
**Access:** `http://localhost:8080/?key=PiCraft2024-StaffOnly`

## ⚙️ Configuration

The plugin generates a `config.yml` with these sections:

### Storage
```yaml
storage:
  type: sqlite      # Currently only SQLite supported
```

### Cooldowns
```yaml
cooldowns:
  report: 30        # Seconds between reports
```

### Notifications
```yaml
notifications:
  on_staff_join: true    # Notify staff of unread reports on join
  unread_count: true     # Show unread count
```

### Discord Integration
```yaml
discord:
  enabled: false                    # Requires DiscordSRV
  channel_id: ""                   # Discord channel ID
  ping_role_id: ""                 # Optional role to mention
  message: "**New report #{id}**..." # Message template
```

### Report Categories & Base Tracking
```yaml
reports:
  categories:
    - "grief"
    - "steal" 
    - "hack"
    - "toxic"
    - "combat"
    - "exploit"
    - "spam"
    - "other"
  base_tracking:
    enabled: true
    radius_limit: 2500        # Alert if report is outside spawn radius
    log_coordinates: true     # Log all report coordinates for base tracking
```

### Rules System
```yaml
rules:
  file: "rules.txt"               # Rules file name
  pagination:
    enabled: false                # Enable pagination
    lines_per_page: 12           # Lines per page
```

### Messages
All messages are customizable with color codes (`&` format).

## 🗃️ How It Works

### Database Structure
The plugin uses SQLite to store reports with these fields:
- `id` - Sequential report ID (auto-renumbers when reports are closed)
- `created_at` - Timestamp when report was created
- `reporter_uuid` - UUID of the reporter
- `reporter_name` - Name of the reporter
- `anonymous` - Whether the report is anonymous
- `category` - Required category (configurable: grief, steal, hack, toxic, combat, exploit, spam, other)
- `message` - The report message
- `world`, `x`, `y`, `z` - Location where report was filed
- `status` - OPEN, READ, or CLOSED
- `assigned_to` - Staff member assigned to the report

### Report Flow
1. **Player reports**: `/report grief Someone broke my chest`
2. **System saves**: Report stored in database with location
3. **Coordinates logged**: Location saved to `base_coordinates.log` for tracking
4. **Staff notified**: Online staff get notification
5. **Discord ping**: Optional Discord message sent
6. **Staff manages**: View, read, assign, and close reports
7. **Auto-renumber**: When closed, remaining reports shift down (no gaps)
8. **Teleport**: Staff can teleport to report location

### Anonymous Reports
- Anonymous reports hide the reporter's name from normal staff
- Only staff with `picraft.report.view.realname` can see the real reporter
- Useful for sensitive reports where players fear retaliation

## 🔧 Code Structure Explained

### Main Components

1. **PiCraftPlugin.java** - Main plugin class that initializes everything
2. **Report.java** - Data model representing a report
3. **DatabaseManager.java** - Handles SQLite database operations
4. **ConfigManager.java** - Manages configuration settings
5. **ReportManager.java** - Handles notifications and Discord integration
6. **Command Classes** - Handle all command execution
7. **PlayerJoinListener.java** - Notifies staff on join

### Key Design Decisions

- **SQLite over MySQL**: No external database required, perfect for small servers
- **Permission-based**: Integrates with existing permission systems
- **Minimal dependencies**: Only requires Paper/Spigot API and SQLite
- **Modular design**: Each component has a specific responsibility
- **Configuration-driven**: Most behavior can be customized via config

### Database Operations

The `DatabaseManager` class handles all database operations:
- **Connection management**: Opens/closes SQLite connection
- **Table creation**: Creates tables on first run
- **CRUD operations**: Create, read, update reports
- **Filtering**: Get reports by status, pagination support
- **Statistics**: Count unread reports for notifications

### Command System

Commands are organized into logical groups:
- **ReportCommand**: Handles player report submissions
- **RulesCommand**: Displays rules from file
- **StaffCommands**: All staff management commands in one class

Each command:
1. Checks permissions
2. Validates arguments
3. Performs database operations
4. Sends formatted responses
5. Handles errors gracefully

## 🚀 Usage Examples

### Basic Report
```
/report grief Someone destroyed my house at spawn
```

### Anonymous Report
```
/report -anon hack Player123 is using fly hacks in PvP
```

### All Categories
```
/report grief <message>     # Griefing/destruction
/report steal <message>     # Theft/stealing
/report hack <message>      # Hacking/cheating
/report toxic <message>     # Toxic behavior
/report combat <message>    # Combat logging/PvP issues
/report exploit <message>   # Bug exploitation
/report spam <message>      # Chat spam
/report other <message>     # Everything else
```

### Staff Workflow
```
/reports                    # Check unread reports
/reportview 5              # View details of report #5
/reporttp 5                # Teleport to report location
/reportread 5              # Mark as read
/reportassign 5 ModName    # Assign to staff member
/reportclear 5             # Close the report
```

## 🔗 Integration

### LuckPerms Setup
```
/lp group mod permission set picraft.report.view true
/lp group mod permission set picraft.report.read true
/lp group admin permission set picraft.* true
```

### DiscordSRV Setup
1. Install DiscordSRV (optional - plugin works without it)
2. Set `discord.enabled: true` in config
3. Add your Discord channel ID
4. **Important**: Give bot permissions in the channel:
   - View Channel
   - Send Messages
   - Read Message History
   - Mention Everyone (for role pings)
5. Optionally add role ID for mentions

### CoreProtect Integration
This plugin works alongside CoreProtect:
- Use reports to identify issues
- Use CoreProtect to investigate and rollback
- Perfect combination for server moderation

## 🛠️ Development Notes

### Building from Source
```bash
git clone <repository>
cd PiCraftPlugin
./gradlew build
```

**Output JARs:**
- `PiCraftPlugin-1.0-SNAPSHOT.jar` - Main plugin (use this)
- `PiCraftPlugin-1.0-SNAPSHOT-sources.jar` - Source code for decompiling

### Key Features Added

**Required Categories:**
- Categories are now mandatory for all reports
- Tab completion shows available categories
- Configurable category list in config.yml

**Base Coordinate Tracking:**
- All report locations logged to `base_coordinates.log`
- Spawn radius checking (alerts if outside 2500 blocks)
- Useful for tracking grief patterns and base locations

**Auto-Renumbering:**
- When a report is closed, higher-numbered reports shift down
- No gaps in report numbering (always sequential)
- Makes it easier for staff to reference reports

**No Dependencies:**
- Plugin works standalone (no required dependencies)
- DiscordSRV, LuckPerms, CoreProtect are all optional
- Graceful fallback when integrations aren't available

### Adding Features
The modular design makes it easy to add features:
- New commands: Add to appropriate command class
- New data: Extend the Report model and database schema
- New integrations: Add new manager classes

### Testing
- Use `/reload confirm` for quick testing
- Check console for any errors
- Test all permission levels
- Verify database operations work correctly

## 📝 License

This plugin is open source. Feel free to modify and distribute according to your needs.

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 🎯 **PiCraft Season 2 Features**

### **Automatic Rule Enforcement**
- 🚫 **Dragon Egg Protection**: Blocks ender chest placement (auto-reports)
- 🚫 **Weakness Potion Prevention**: Blocks usage (auto-reports)
- 📊 **Combat Log Detection**: 15-second PvP timer (auto-reports)
- 📊 **Seed Abuse Monitoring**: Diamond/structure limits (auto-reports)
- 📊 **Replay Mod Detection**: F3+A usage monitoring (auto-reports)
- 📊 **Base Radius Enforcement**: 2500-block limit (auto-reports)

### **Staff Tools**
- 📱 **Mobile Dashboard**: Secure web interface with authentication
- 🔗 **Evidence System**: Attach screenshots/videos to reports
- 🎯 **Smart Discord**: Category-specific channels and role pings
- 📍 **Location Tracking**: Auto-log coordinates for pattern analysis
- 🔄 **Auto-Renumbering**: Sequential report IDs with no gaps

## 🛠️ **Troubleshooting**

### **Common Issues**
```bash
# Plugin not loading
- Check Minecraft version (1.21+ required)
- Verify JAR file integrity

# Discord not working
- Install DiscordSRV first
- Check bot permissions in channels
- Verify channel/role IDs (18 digits)

# Auto-detection too sensitive
- Increase limits in config.yml
- Or disable: enabled: false

# Web dashboard not accessible
- Check auth key in URL
- Verify port 8080 not blocked
```

---

## 📊 **Plugin Stats**
- **Commands**: 12
- **Permissions**: 15  
- **Auto-Detection Systems**: 6
- **Zero Dependencies**: All integrations optional
- **Modern API**: Adventure-based, future-proof

**Built specifically for PiCraft Season 2** - Complete rule enforcement with zero bloat! 🚀
