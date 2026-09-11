package dev.totem.enchanting.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

/** Exercises the real menu synchronization and native screen at the conversion threshold. */
@SuppressWarnings("UnstableApiUsage")
public final class GoldenAppleVisualGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(1280, 720);
        try (var world = context.worldBuilder().create()) {
            world.getClientLevel().waitForChunksRender();
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.setGameMode(GameType.SURVIVAL);
                player.giveExperienceLevels(64);
                var level = player.level();
                var pos = player.blockPosition();
                level.setBlockAndUpdate(pos, Blocks.ENCHANTING_TABLE.defaultBlockState());
                var shelfPos = pos.offset(2, 0, 0);
                level.setBlockAndUpdate(pos.offset(1, 0, 0), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(shelfPos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
                var book = new ItemStack(Items.ENCHANTED_BOOK);
                book.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SHARPNESS), 54);
                ((ChiseledBookShelfBlockEntity) level.getBlockEntity(shelfPos)).setItem(0, book);
                player.openMenu(new SimpleMenuProvider((id, inventory, owner) ->
                        new EnchantmentMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                        Component.translatable("container.enchant")));
                player.containerMenu.getSlot(0).set(new ItemStack(Items.GOLDEN_APPLE));
                player.containerMenu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
                player.containerMenu.broadcastFullState();
            });
            context.waitForScreen(EnchantmentScreen.class);
            context.waitFor(client -> client.player.containerMenu instanceof EnchantmentMenu menu
                    && menu.costs[2] == 54 && client.player.experienceLevel >= 54);
            context.waitTicks(5);
            context.takeScreenshot("golden-apple-native-enchanting-54");
            context.runOnClient(client -> {
                var menu = (EnchantmentMenu) client.player.containerMenu;
                if (menu.costs[0] != 0 || menu.costs[1] != 0 || !menu.clickMenuButton(client.player, 2)) {
                    throw new AssertionError("Native client rejected the golden apple option");
                }
                client.gameMode.handleInventoryButtonClick(menu.containerId, 2);
            });
            context.waitFor(client -> client.player.containerMenu.getSlot(0).getItem().is(Items.ENCHANTED_GOLDEN_APPLE)
                    && client.player.containerMenu.getSlot(1).getItem().isEmpty()
                    && client.player.experienceLevel == 61);
            context.takeScreenshot("golden-apple-native-enchanting-result");
            context.runOnClient(client -> client.player.closeContainer());
        }
    }
}
