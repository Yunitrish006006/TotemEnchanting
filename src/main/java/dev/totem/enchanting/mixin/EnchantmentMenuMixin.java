package dev.totem.enchanting.mixin;

import dev.totem.enchanting.power.EnchantingPowerHelper;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    private static final int VANILLA_QUALITY_POWER = 30;
    private static final int MAX_DISPLAYED_POWER = 64;
    private static final int MAX_SELECTION_POWER = 50;
    private static final int QUALITY_REROLL_STEP = 12;
    private static final int MAX_QUALITY_REROLLS = 3;
    private static final long QUALITY_SEED_GAMMA = 0x9E3779B97F4A7C15L;
    private static final long CLUE_SEED_SALT = 0xD1B54A32D192ED03L;

    @Shadow @Final
    private ContainerLevelAccess access;

    @Shadow @Final
    private RandomSource random;

    @Shadow @Final
    private DataSlot enchantmentSeed;

    @Shadow @Final
    public int[] costs;

    @Shadow @Final
    public int[] enchantClue;

    @Shadow @Final
    public int[] levelClue;

    @Shadow
    private List<EnchantmentInstance> getEnchantmentList(RegistryAccess access, ItemStack itemStack, int slot, int enchantmentCost) {
        return null;
    }

    @Shadow @Final
    private Container enchantSlots;

    /**
     * Replaces the bookshelf count used by the vanilla menu with TotemEnchanting's weighted book power.
     *
     * @author TotemEnchanting
     * @reason Custom chiseled-bookshelf power system.
     */
    @Overwrite
    public void slotsChanged(Container container) {
        if (container == this.enchantSlots) {
            ItemStack itemStack = container.getItem(0);
            if (itemStack.is(Items.GOLDEN_APPLE)) {
                java.util.Arrays.fill(this.costs, 0);
                java.util.Arrays.fill(this.enchantClue, -1);
                java.util.Arrays.fill(this.levelClue, -1);
                this.access.execute((level, pos) -> {
                    int power = EnchantingPowerHelper.calculateBookPower(level, pos);
                    if (power >= 54 && itemStack.getCount() == 1) this.costs[2] = power;
                    ((EnchantmentMenu) (Object) this).broadcastChanges();
                });
                return;
            }
            if (!itemStack.isEmpty() && itemStack.isEnchantable()) {
                this.access.execute((level, pos) -> {
                    int bookPower = EnchantingPowerHelper.calculateBookPower(level, pos);
                    this.random.setSeed((long) this.enchantmentSeed.get());

                    for (int i = 0; i < 3; ++i) {
                        this.costs[i] = EnchantmentHelper.getEnchantmentCost(this.random, i, bookPower, itemStack);
                        this.enchantClue[i] = -1;
                        this.levelClue[i] = -1;
                        if (this.costs[i] < i + 1) {
                            this.costs[i] = 0;
                        }
                    }

                    for (int i = 0; i < 3; ++i) {
                        if (this.costs[i] > 0) {
                            List<EnchantmentInstance> list = this.getEnchantmentList(
                                    level.registryAccess(),
                                    itemStack,
                                    i,
                                    this.costs[i]
                            );
                            if (!list.isEmpty()) {
                                var holders = level.registryAccess()
                                        .lookupOrThrow(Registries.ENCHANTMENT)
                                        .asHolderIdMap();
                                EnchantmentInstance enchantment = list.get(this.random.nextInt(list.size()));
                                this.enchantClue[i] = holders.getId(enchantment.enchantment());
                                this.levelClue[i] = enchantment.level();
                            }
                        }
                    }

                    ((net.minecraft.world.inventory.AbstractContainerMenu) (Object) this).broadcastChanges();
                });
            } else {
                for (int i = 0; i < 3; ++i) {
                    this.costs[i] = 0;
                    this.enchantClue[i] = -1;
                    this.levelClue[i] = -1;
                }
            }
        }
    }

    /** One apple per transaction; recheck the actual shelves and resources at click time. */
    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void totem$enchantGoldenApple(net.minecraft.world.entity.player.Player player, int button,
                                         CallbackInfoReturnable<Boolean> cir) {
        ItemStack input = this.enchantSlots.getItem(0);
        if (!input.is(Items.GOLDEN_APPLE)) return;
        var menu = (EnchantmentMenu) (Object) this;
        ItemStack lapis = this.enchantSlots.getItem(1);
        if (button != 2 || input.getCount() != 1 || this.costs[2] < 54 || this.costs[2] > 64
                || player.isSpectator() || player.containerMenu != menu || !menu.stillValid(player)
                || (!player.hasInfiniteMaterials() && (player.experienceLevel < this.costs[2]
                || !lapis.is(Items.LAPIS_LAZULI) || lapis.getCount() < 3))) {
            cir.setReturnValue(false);
            return;
        }
        // Vanilla screens call this locally before sending the menu-button packet. Only the server mutates.
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(this.access.evaluate((level, pos) -> {
            int power = EnchantingPowerHelper.calculateBookPower(level, pos);
            if (power < 54 || power != this.costs[2]) {
                this.slotsChanged(this.enchantSlots);
                return false;
            }
            ItemStack result = input.transmuteCopy(Items.ENCHANTED_GOLDEN_APPLE);
            player.onEnchantmentPerformed(input, 3);
            lapis.consume(3, player);
            this.enchantSlots.setItem(0, result);
            if (lapis.isEmpty()) this.enchantSlots.setItem(1, ItemStack.EMPTY);
            this.enchantmentSeed.set(player.getEnchantmentSeed());
            this.enchantSlots.setChanged();
            player.awardStat(net.minecraft.stats.Stats.ENCHANT_ITEM);
            net.minecraft.advancements.triggers.CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, result, 3);
            menu.broadcastChanges();
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            return true;
        }, false));
    }

    /**
     * Vanilla enchantment definitions use bounded power windows. Passing 64 directly into
     * selectEnchantment can exceed the maximum window of otherwise desirable enchantments and
     * make a 64-power option worse than a level-30 option.
     *
     * <p>Power above 30 is therefore converted into a vanilla-compatible selection range of
     * 30..50. Additional deterministic candidates are rolled as the displayed cost increases,
     * and the best compatible result is selected. The original level-30 candidate is always
     * included, so a high-power result cannot score below its level-30 baseline for the same
     * item, slot and enchantment seed.</p>
     */
    @Inject(method = "getEnchantmentList", at = @At("HEAD"), cancellable = true)
    private void totem$improveHighPowerEnchantments(
            RegistryAccess registryAccess,
            ItemStack itemStack,
            int slot,
            int enchantmentCost,
            CallbackInfoReturnable<List<EnchantmentInstance>> cir
    ) {
        var enchantmentTag = registryAccess
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (enchantmentTag.isEmpty()) {
            cir.setReturnValue(List.of());
            return;
        }

        HolderSet.Named<Enchantment> availableEnchantments = enchantmentTag.get();
        int clampedCost = Mth.clamp(enchantmentCost, 1, MAX_DISPLAYED_POWER);
        long baseSeed = (long) this.enchantmentSeed.get() + slot;

        List<EnchantmentInstance> best = totem$selectCandidate(
                availableEnchantments,
                itemStack,
                Math.min(clampedCost, VANILLA_QUALITY_POWER),
                baseSeed
        );
        int bestScore = totem$qualityScore(best);

        if (clampedCost > VANILLA_QUALITY_POWER) {
            int selectionPower = totem$selectionPower(clampedCost);
            int rerolls = Math.min(
                    MAX_QUALITY_REROLLS,
                    1 + (clampedCost - VANILLA_QUALITY_POWER - 1) / QUALITY_REROLL_STEP
            );

            for (int attempt = 0; attempt < rerolls; attempt++) {
                long candidateSeed = baseSeed + QUALITY_SEED_GAMMA * (attempt + 1L);
                List<EnchantmentInstance> candidate = totem$selectCandidate(
                        availableEnchantments,
                        itemStack,
                        selectionPower,
                        candidateSeed
                );
                int candidateScore = totem$qualityScore(candidate);
                if (candidateScore > bestScore) {
                    best = candidate;
                    bestScore = candidateScore;
                }
            }
        }

        // slotsChanged uses this RandomSource after getEnchantmentList to choose the visible clue.
        // Keep that clue deterministic without coupling it to the number of quality rerolls.
        this.random.setSeed(baseSeed ^ CLUE_SEED_SALT);
        cir.setReturnValue(best);
    }

    private static int totem$selectionPower(int displayedCost) {
        int extraCost = displayedCost - VANILLA_QUALITY_POWER;
        int extraRange = MAX_SELECTION_POWER - VANILLA_QUALITY_POWER;
        int displayedRange = MAX_DISPLAYED_POWER - VANILLA_QUALITY_POWER;
        return VANILLA_QUALITY_POWER + Math.round(extraCost * (float) extraRange / displayedRange);
    }

    private static List<EnchantmentInstance> totem$selectCandidate(
            HolderSet.Named<Enchantment> availableEnchantments,
            ItemStack itemStack,
            int selectionPower,
            long seed
    ) {
        RandomSource candidateRandom = RandomSource.create();
        candidateRandom.setSeed(seed);
        List<EnchantmentInstance> candidate = new ArrayList<>(EnchantmentHelper.selectEnchantment(
                candidateRandom,
                itemStack,
                selectionPower,
                availableEnchantments.stream()
        ));

        if (itemStack.is(Items.BOOK) && candidate.size() > 1) {
            candidate.remove(candidateRandom.nextInt(candidate.size()));
        }
        return candidate;
    }

    private static int totem$qualityScore(List<EnchantmentInstance> enchantments) {
        int score = 0;
        for (EnchantmentInstance enchantment : enchantments) {
            int level = enchantment.level();
            score += 100 + level * level * 25;
        }
        return score;
    }
}
