package rishi.player.picraftmod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Picraftmod implements ModInitializer {
    public static final String MOD_ID = "picraftmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("PiCraft Mod initialized!");
    }
}