# Extraction contract

## Approved authority

TotemEnchanting owns the weighted Chiseled Bookshelf power rules, Enchanting
Table particle behavior and the Enchantment Menu quality-selection Mixins. It
preserves Minecraft's existing enchantment IDs and has no custom registry or
data-resource surface.

## Boundary and rollback

The behavior is one unit: `EnchantingPowerHelper`, `EnchantmentHelperMixin`,
`EnchantmentMenuMixin` and `EnchantingTableBlockMixin`. DeadRecall must gate
all three legacy Mixins together when the external module is present, leaving
the root implementation available as the observation-window rollback path.

TotemEnchanting depends only on TotemCore and Fabric API. It must not import
DeadRecall, TotemRemnant, TotemNexus or another feature implementation.

## Validation

Java 25 dedicated-server GameTests verify weighted normal/enchant-book power
and the 64-power cap. Cutover requires a standalone server startup and an
exact lockstep compatibility-bundle run with one set of enchanting Mixins.
