package org.pluginmakers.piCraftPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.pluginmakers.piCraftPlugin.PiCraftPlugin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModListListener implements PluginMessageListener, Listener {
    private final PiCraftPlugin plugin;
    private final Map<UUID, List<String>> playerMods = new HashMap<>();
    private final Map<UUID, List<String>> playerResourcePacks = new HashMap<>();
    
    public ModListListener(PiCraftPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("picraft:mod_list")) return;
        
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            int modCount = in.readInt();
            
            List<String> mods = new ArrayList<>();
            for (int i = 0; i < modCount; i++) {
                String modInfo = in.readUTF();
                mods.add(modInfo);
            }
            
            int packCount = in.readInt();
            List<String> resourcePacks = new ArrayList<>();
            for (int i = 0; i < packCount; i++) {
                String packInfo = in.readUTF();
                resourcePacks.add(packInfo);
            }
            
            playerMods.put(player.getUniqueId(), mods);
            playerResourcePacks.put(player.getUniqueId(), resourcePacks);
            plugin.getLogger().info("Received data from " + player.getName() + ": " + mods.size() + " mods, " + resourcePacks.size() + " resource packs");
            
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to read mod list from " + player.getName() + ": " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Clear old data
        playerMods.remove(event.getPlayer().getUniqueId());
        playerResourcePacks.remove(event.getPlayer().getUniqueId());
    }
    
    public List<String> getPlayerMods(Player player) {
        return playerMods.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
    
    public boolean hasReceivedModList(Player player) {
        return playerMods.containsKey(player.getUniqueId());
    }
    
    public List<String> getPlayerResourcePacks(Player player) {
        return playerResourcePacks.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
}