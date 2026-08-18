package dev.totem.enchanting.manual;

import dev.totem.core.api.v1.manual.TotemManualSection;
import dev.totem.core.api.v1.manual.TotemModuleManualSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/** Enchanting-power guide recorded from an enchanting table. */
public final class EnchantingManual {
    private static final TotemManualSection SECTION = new TotemManualSection(
            Identifier.fromNamespaceAndPath("totem", "enchanting/manual"),
            400,
            "book.deadrecall.enchanting_manual.title",
            List.of(
                    "book.deadrecall.enchanting_manual.page.1",
                    "book.deadrecall.enchanting_manual.page.2"
            )
    );

    private EnchantingManual() {
    }

    public static void register() {
        TotemModuleManualSource.register(
                SECTION,
                Identifier.fromNamespaceAndPath("deadrecall", "enchanting_manual"),
                state -> state.is(Blocks.ENCHANTING_TABLE)
        );
    }
}
