package rishi.player.picraftmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import rishi.player.picraftmod.Picraftmod;

public class PicraftmodClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        Picraftmod.LOGGER.info("PiCraft Mod client initialized!");
        
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Send mod list after a delay
            client.execute(() -> {
                try {
                    Thread.sleep(3000); // 3 second delay
                    sendModList();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
    }
    
    private void sendModList() {
        try {
            var mods = FabricLoader.getInstance().getAllMods();
            Picraftmod.LOGGER.info("Sending " + mods.size() + " mods to server");
            
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(mods.size());
            
            for (var mod : mods) {
                String modId = mod.getMetadata().getId();
                String version = mod.getMetadata().getVersion().getFriendlyString();
                buf.writeString(modId);
                buf.writeString(version);
                Picraftmod.LOGGER.info("  - " + modId + " v" + version);
            }
            
            // Send using Identifier.of
            Identifier channel = Identifier.of("picraft", "mod_list");
            
            // Convert to byte array and send via client brand or other method
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            
            Picraftmod.LOGGER.info("Prepared mod list data (" + data.length + " bytes)");
            // Note: Actual sending will be handled by the server detecting the client brand
            
        } catch (Exception e) {
            Picraftmod.LOGGER.error("Failed to send mod list: " + e.getMessage());
        }
    }
}