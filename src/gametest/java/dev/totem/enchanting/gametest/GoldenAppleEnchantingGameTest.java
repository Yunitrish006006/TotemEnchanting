package dev.totem.enchanting.gametest;

import dev.totem.enchanting.power.EnchantingPowerHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

public final class GoldenAppleEnchantingGameTest {
    @GameTest(maxTicks = 40)
    public void conversionBoundariesCostsAndReplay(GameTestHelper h) {
        var level = h.getLevel();
        BlockPos table = h.absolutePos(new BlockPos(4, 2, 4));
        level.setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
        BlockPos shelfPos = table.offset(2, 0, 0);
        level.setBlockAndUpdate(shelfPos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        var shelf = (ChiseledBookShelfBlockEntity) level.getBlockEntity(shelfPos);
        var player = h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(table.getX()+.5, table.getY()+1, table.getZ()+.5);
        var menu = new EnchantmentMenu(1, player.getInventory(), ContainerLevelAccess.create(level, table));
        player.containerMenu = menu;
        try {
            for (int power : new int[]{53,54,64}) {
                var book = new ItemStack(Items.ENCHANTED_BOOK);
                book.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), power);
                shelf.setItem(0, book);
                require(h, EnchantingPowerHelper.calculateBookPower(level,table)==power, "Invalid power fixture");
                player.experienceLevel = 64;
                menu.getSlot(0).set(new ItemStack(Items.GOLDEN_APPLE));
                menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 6));
                require(h, menu.costs[0]==0 && menu.costs[1]==0, "Apple offered in low option");
                require(h, menu.costs[2]==(power<54?0:power), "Wrong apple power boundary");
                require(h, !menu.clickMenuButton(player,0) && !menu.clickMenuButton(player,3), "Invalid option accepted");
                if(power<54) {
                    require(h,!menu.clickMenuButton(player,2) && player.experienceLevel==64,"Weak shelves consumed resources");
                    continue;
                }
                player.experienceLevel=power-1;
                require(h,!menu.clickMenuButton(player,2),"Insufficient XP accepted");
                player.experienceLevel=64;
                menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI,2));
                require(h,!menu.clickMenuButton(player,2),"Insufficient lapis accepted");
                menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI,6));
                require(h,menu.clickMenuButton(player,2),"Valid apple conversion failed");
                require(h,menu.getSlot(0).getItem().is(Items.ENCHANTED_GOLDEN_APPLE)
                        && menu.getSlot(0).getItem().getCount()==1,"Conversion did not produce exactly one enchanted apple");
                require(h,player.experienceLevel==61 && menu.getSlot(1).getItem().getCount()==3,"Wrong conversion cost");
                require(h,!menu.clickMenuButton(player,2),"Replay converted an already enchanted apple");
            }
            menu.getSlot(0).set(new ItemStack(Items.GOLDEN_APPLE));
            shelf.clearContent();
            require(h,!menu.clickMenuButton(player,2) && menu.getSlot(0).getItem().is(Items.GOLDEN_APPLE),"Stale shelf power accepted");
            require(h,player.experienceLevel==61 && menu.getSlot(1).getItem().getCount()==3,"Rejected stale click consumed resources");
        } finally {
            player.containerMenu=player.inventoryMenu;
            player.discard();
        }
        h.succeed();
    }
    private static void require(GameTestHelper h, boolean condition, String message) { if(!condition) h.fail(message); }
}
