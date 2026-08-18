package dev.totem.enchanting;

import dev.totem.enchanting.manual.EnchantingManual;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Entry point for the independently owned enchanting-power behavior. */
public final class TotemEnchanting implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("TotemEnchanting");

    @Override
    public void onInitialize() {
        EnchantingManual.register();
        LOGGER.info("TotemEnchanting initialized without DeadRecall implementation dependency");
    }
}
