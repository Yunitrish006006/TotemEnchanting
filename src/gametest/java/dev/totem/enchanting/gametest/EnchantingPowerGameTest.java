package dev.totem.enchanting.gametest;

import dev.totem.enchanting.power.EnchantingPowerHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

/** Dedicated-server coverage for the weighted chiseled-bookshelf power rules. */
public final class EnchantingPowerGameTest {
    private static final BlockPos TABLE_POS = new BlockPos(4, 2, 4);

    @GameTest(maxTicks = 40)
    public void chiseledBookshelfWeightsNormalAndEnchantedBooks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos tablePos = helper.absolutePos(TABLE_POS);
        level.setBlockAndUpdate(tablePos, Blocks.ENCHANTING_TABLE.defaultBlockState());

        ChiseledBookShelfBlockEntity shelf = placeShelf(helper, tablePos, EnchantingTableBlock.BOOKSHELF_OFFSETS.getFirst());
        shelf.setItem(0, new ItemStack(Items.BOOK));
        shelf.setItem(1, enchantedBook(level, 3));
        shelf.setItem(2, new ItemStack(Items.DIRT));

        require(helper, EnchantingPowerHelper.calculateBookPower(level, tablePos) == 4,
                "Normal and enchanted books did not contribute their expected weighted power");
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void chiseledBookshelfPowerCapsAtSixtyFour(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos tablePos = helper.absolutePos(TABLE_POS);
        level.setBlockAndUpdate(tablePos, Blocks.ENCHANTING_TABLE.defaultBlockState());

        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            ChiseledBookShelfBlockEntity shelf = placeShelf(helper, tablePos, offset);
            for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
                shelf.setItem(slot, new ItemStack(Items.BOOK));
            }
        }

        require(helper, EnchantingPowerHelper.calculateBookPower(level, tablePos) == 64,
                "Weighted chiseled-bookshelf power was not capped at 64");
        helper.succeed();
    }

    private static ChiseledBookShelfBlockEntity placeShelf(GameTestHelper helper, BlockPos tablePos, BlockPos offset) {
        ServerLevel level = helper.getLevel();
        BlockPos shelfPos = tablePos.offset(offset);
        level.setBlockAndUpdate(shelfPos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        BlockEntity blockEntity = level.getBlockEntity(shelfPos);
        if (!(blockEntity instanceof ChiseledBookShelfBlockEntity shelf)) {
            throw helper.assertionException("Placed chiseled bookshelf did not create its block entity");
        }
        return shelf;
    }

    private static ItemStack enchantedBook(ServerLevel level, int enchantmentLevel) {
        Holder<Enchantment> sharpness = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SHARPNESS);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.enchant(sharpness, enchantmentLevel);
        return book;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            throw helper.assertionException(message);
        }
    }
}
