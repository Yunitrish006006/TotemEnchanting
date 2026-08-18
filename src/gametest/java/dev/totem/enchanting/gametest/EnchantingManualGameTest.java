package dev.totem.enchanting.gametest;

import dev.totem.core.api.v1.manual.TotemManualAssembler;
import dev.totem.core.api.v1.manual.TotemManualRegistry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Integration coverage for the recovered Enchanting section in the shared Totem Manual. */
public final class EnchantingManualGameTest {
    private static final Identifier SECTION_ID =
            Identifier.fromNamespaceAndPath("totem", "enchanting/manual");

    @GameTest(maxTicks = 20)
    public void enchantingSectionIsRegisteredAndAssemblable(GameTestHelper helper) {
        var section = TotemManualRegistry.global().section(SECTION_ID).orElse(null);
        if (section == null) {
            helper.fail("Enchanting manual section was not registered");
            return;
        }
        if (section.order() != 400 || section.pageKeys().size() != 2) {
            helper.fail("Enchanting manual section metadata did not match the recovered 0.1.5 contract");
            return;
        }
        var manual = TotemManualAssembler.create(List.of(section));
        if (!TotemManualAssembler.isCanonical(manual)
                || TotemManualAssembler.sections(manual).stream().noneMatch(value -> value.id().equals(SECTION_ID))) {
            helper.fail("Enchanting manual section did not assemble into a canonical Totem Manual");
            return;
        }
        helper.succeed();
    }
}
