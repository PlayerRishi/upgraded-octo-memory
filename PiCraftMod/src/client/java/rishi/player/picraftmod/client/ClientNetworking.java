package rishi.player.picraftmod.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import rishi.player.picraftmod.network.ModNetworking;

public class ClientNetworking {
    public static void registerClientPackets() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Send complete mod list to server
            ClientPlayNetworking.send(ModNetworking.ModListPayload.create());
        });
    }
}