package dev.totem.enchanting.gametest;

import dev.totem.enchanting.power.EnchantingPowerHelper;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;

/** Gameplay layout regressions for chiseled-bookshelf enchanting power. */
public final class EnchantingLayoutGameTest {
    private static final BlockPos TABLE_POS = new BlockPos(4, 2, 4);

    @GameTest(maxTicks = 40)
    public void visibleShelfContributesPower(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos table = helper.absolutePos(TABLE_POS);
        level.setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
        BlockPos offset = EnchantingTableBlock.BOOKSHELF_OFFSETS.getFirst();
        ChiseledBookShelfBlockEntity shelf = placeShelf(helper, table.offset(offset));
        shelf.setItem(0, new ItemStack(Items.BOOK));

        require(helper, EnchantingPowerHelper.calculateBookPower(level, table) == 1,
                "A visible chiseled bookshelf did not contribute enchanting power");
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void blockedShelfDoesNotContributePower(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos table = helper.absolutePos(TABLE_POS);
        level.setBlockAndUpdate(table, Blocks.ENCHANTING_TABLE.defaultBlockState());
        BlockPos offset = EnchantingTableBlock.BOOKSHELF_OFFSETS.getFirst();
        ChiseledBookShelfBlockEntity shelf = placeShelf(helper, table.offset(offset));
        shelf.setItem(0, new ItemStack(Items.BOOK));

        int stepX = Integer.signum(offset.getX());
        int stepZ = Integer.signum(offset.getZ());
        level.setBlockAndUpdate(table.offset(stepX, 0, stepZ), Blocks.STONE.defaultBlockState());

        require(helper, EnchantingPowerHelper.calculateBookPower(level, table) == 0,
                "A blocked chiseled bookshelf still contributed enchanting power");
        helper.succeed();
    }

    private static ChiseledBookShelfBlockEntity placeShelf(GameTestHelper helper, BlockPos pos) {
        ServerLevel level = helper.getLevel();
        level.setBlockAndUpdate(pos, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChiseledBookShelfBlockEntity shelf)) {
            throw helper.assertionException("Placed chiseled bookshelf did not create its block entity");
        }
        return shelf;
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            throw helper.assertionException(message);
        }
    }
}
