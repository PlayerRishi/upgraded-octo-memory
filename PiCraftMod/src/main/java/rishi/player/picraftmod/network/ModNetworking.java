package rishi.player.picraftmod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.util.Identifier;
import rishi.player.picraftmod.Picraftmod;

import java.util.List;
import java.util.stream.Collectors;

public class ModNetworking {
    public static final Identifier MOD_LIST_ID = Identifier.of(Picraftmod.MOD_ID, "mod_list");
    
    public record ModListPayload(List<String> mods, List<String> resourcePacks) implements CustomPayload {
        public static final CustomPayload.Id<ModListPayload> ID = new CustomPayload.Id<>(MOD_LIST_ID);
        public static final PacketCodec<RegistryByteBuf, ModListPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.mods.size());
                for (String mod : value.mods) {
                    buf.writeUtf(mod);
                }
                buf.writeInt(value.resourcePacks.size());
                for (String pack : value.resourcePacks) {
                    buf.writeUtf(pack);
                }
            },
            (buf) -> {
                int modSize = buf.readInt();
                List<String> mods = new java.util.ArrayList<>();
                for (int i = 0; i < modSize; i++) {
                    mods.add(buf.readUtf());
                }
                int packSize = buf.readInt();
                List<String> resourcePacks = new java.util.ArrayList<>();
                for (int i = 0; i < packSize; i++) {
                    resourcePacks.add(buf.readUtf());
                }
                return new ModListPayload(mods, resourcePacks);
            }
        );
        
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
        
        public static ModListPayload create() {
            List<String> modList = FabricLoader.getInstance().getAllMods().stream()
                .map(ModContainer::getMetadata)
                .map(metadata -> metadata.getId() + " v" + metadata.getVersion().getFriendlyString())
                .collect(Collectors.toList());
            
            List<String> resourcePacks = new java.util.ArrayList<>();
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.getResourcePackManager() != null) {
                    ResourcePackManager packManager = client.getResourcePackManager();
                    resourcePacks = packManager.getEnabledProfiles().stream()
                        .map(ResourcePackProfile::getDisplayName)
                        .map(text -> text.getString())
                        .collect(Collectors.toList());
                }
            } catch (Exception e) {
                // Fallback if resource pack detection fails
                resourcePacks.add("Detection failed: " + e.getMessage());
            }
            
            return new ModListPayload(modList, resourcePacks);
        }
    }
    
    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(ModListPayload.ID, ModListPayload.CODEC);
    }
}