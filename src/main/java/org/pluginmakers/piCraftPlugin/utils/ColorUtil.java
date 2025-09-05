package org.pluginmakers.piCraftPlugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ColorUtil {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    
    @NotNull
    public static Component colorize(@NotNull String text) {
        return SERIALIZER.deserialize(text);
    }
    
    @NotNull
    public static String colorizeToString(@NotNull String text) {
        return SERIALIZER.serialize(SERIALIZER.deserialize(text));
    }
}