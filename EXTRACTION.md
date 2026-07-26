# Extraction contract

TotemEnchanting will own `EnchantingPowerHelper` and the Enchanting Table,
Enchantment Helper and Enchantment Menu Mixins, together with their rules,
resources and tests. These classes move as one server/client behavioral unit;
leaving any matching legacy Mixin active during cutover is prohibited.

Before code moves, inventory every registration, resource, GameTest and
compatibility identifier. Existing visible behavior and `deadrecall:*`
identifiers stay compatible through the lockstep observation window.
