# 🌐 PiCraft Plugin

A lightweight, zero-bloat Paper/Spigot plugin for PiCraft Season 2 that adds a clean player report system, simple staff tools, and a /rules command that reads from a separate rules.txt file. Plays nice with LuckPerms, CoreProtect, and DiscordSRV.

**Vibe: Vanilla-first. Tools when you need 'em. No nonsense.**

## ✨ Features

- **Player Reports**: `/report` with anonymous mode (`-anon`)
- **Staff Tools**: Complete report management system
- **Rules System**: `/rules` reads from `rules.txt` file
- **Discord Integration**: Optional DiscordSRV integration
- **SQLite Storage**: Lightweight database with no external dependencies
- **Cooldown System**: Prevents report spam
- **Permission-based**: Works seamlessly with LuckPerms

## 🧩 Commands

### Players

- `/report [-anon] [category] <message>` - Creates a report with optional anonymous mode
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
- `picraft.*` - All permissions (default: op)

## 📦 Installation

1. **Download**: Place `PiCraftPlugin.jar` in your `plugins/` folder
2. **Start Server**: Start the server once to generate config files
3. **Configure**: Edit `plugins/PiCraftPlugin/config.yml` as needed
4. **Rules**: Edit `plugins/PiCraftPlugin/rules.txt` with your server rules
5. **Optional**: Install DiscordSRV for Discord integration
6. **Permissions**: Set up permissions with LuckPerms or your permission plugin
7. **Reload**: Use `/reload confirm` or restart the server

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
- `id` - Auto-incrementing report ID
- `created_at` - Timestamp when report was created
- `reporter_uuid` - UUID of the reporter
- `reporter_name` - Name of the reporter
- `anonymous` - Whether the report is anonymous
- `category` - Optional category (griefing, hacking, etc.)
- `message` - The report message
- `world`, `x`, `y`, `z` - Location where report was filed
- `status` - OPEN, READ, or CLOSED
- `assigned_to` - Staff member assigned to the report

### Report Flow
1. **Player reports**: `/report griefing Someone broke my chest`
2. **System saves**: Report stored in database with location
3. **Staff notified**: Online staff get notification
4. **Discord ping**: Optional Discord message sent
5. **Staff manages**: View, read, assign, and close reports
6. **Teleport**: Staff can teleport to report location

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
/report griefing Someone destroyed my house at spawn
```

### Anonymous Report
```
/report -anon hacking Player123 is using fly hacks in PvP
```

### Categorized Report
```
/report toxicity PlayerX was being very rude in chat
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
1. Install DiscordSRV
2. Set `discord.enabled: true` in config
3. Add your Discord channel ID
4. Optionally add role ID for mentions

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

---

**Remember**: This plugin is designed to be lightweight and vanilla-friendly. Keep it simple, keep it fast! 🚀