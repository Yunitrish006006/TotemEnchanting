# Totem Enchanting Manual Reference Specification

## Goal

The Totem Enchanting section of the shared Totem Manual must act as an in-game enchantment reference, not only as a guide to the custom enchanting-power system.

The reference must cover:

1. Every vanilla Minecraft 26.2 enchantment and its gameplay effect.
2. Totem Enchanting custom enchantments.
3. Totem Enchanting negative/flaw enchantments.
4. Maximum level, applicable equipment, acquisition notes when relevant, and important incompatibilities.

## Presentation requirements

- Use the enchantment's game-localized name instead of duplicating the vanilla name in Totem translations when possible.
- Keep effect descriptions in Totem Enchanting translations (`en_us` and `zh_tw`).
- When an applicable equipment type has a corresponding in-game ItemStack, prefer the actual item icon in the manual UI. This follows the Totem Manual rule used by other modules.
- Group the reference into readable categories instead of placing all enchantments on one page.
- Mark treasure-only and curse enchantments clearly.
- Show mutually exclusive enchantments where the incompatibility matters to player decisions.
- Do not hard-code only today's registered set without validation: GameTests must fail when a vanilla or Totem enchantment is expected to be documented but lacks a manual descriptor.

## Vanilla Minecraft 26.2 reference set

Minecraft 26.2 contains the following 43 vanilla enchantments that must be documented.

### Armor and movement

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:protection` | IV | Reduces most incoming damage. |
| `minecraft:fire_protection` | IV | Reduces fire damage and burn duration. |
| `minecraft:feather_falling` | IV | Reduces fall damage. |
| `minecraft:blast_protection` | IV | Reduces explosion damage and knockback. |
| `minecraft:projectile_protection` | IV | Reduces projectile damage. |
| `minecraft:respiration` | III | Extends underwater breathing time. |
| `minecraft:aqua_affinity` | I | Removes/reduces the underwater mining-speed penalty. |
| `minecraft:thorns` | III | Can damage an attacker when the wearer is hit. |
| `minecraft:depth_strider` | III | Increases underwater movement speed. |
| `minecraft:frost_walker` | II | Freezes nearby water beneath the wearer while walking. |
| `minecraft:binding_curse` | I | Prevents equipped gear from being removed normally. |
| `minecraft:soul_speed` | III | Increases movement speed on soul sand and soul soil. |
| `minecraft:swift_sneak` | III | Increases movement speed while sneaking. |

### Melee weapons

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:sharpness` | V | Increases melee damage. |
| `minecraft:smite` | V | Increases damage against undead mobs. |
| `minecraft:bane_of_arthropods` | V | Increases damage against arthropods and applies Slowness. |
| `minecraft:knockback` | II | Increases knockback dealt on hit. |
| `minecraft:fire_aspect` | II | Sets struck targets on fire. |
| `minecraft:looting` | III | Improves mob drops from kills. |
| `minecraft:sweeping_edge` | III | Increases sweeping-attack damage. |

### Bow

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:power` | V | Increases arrow damage. |
| `minecraft:punch` | II | Increases arrow knockback. |
| `minecraft:flame` | I | Sets targets hit by arrows on fire. |
| `minecraft:infinity` | I | Allows normal arrows to be fired without consuming them when requirements are met. |

### Crossbow

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:multishot` | I | Fires three projectiles for one loaded shot. |
| `minecraft:quick_charge` | III | Reduces crossbow charging time. |
| `minecraft:piercing` | IV | Allows crossbow projectiles to pass through multiple entities. |

### Trident

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:loyalty` | III | Returns a thrown trident to its owner. |
| `minecraft:impaling` | V | Increases trident damage against affected aquatic targets. |
| `minecraft:riptide` | III | Launches the user when the trident is used in water/rain conditions. |
| `minecraft:channeling` | I | Summons lightning on a valid trident hit during a thunderstorm. |

### Tools and gathering

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:efficiency` | V | Increases mining speed. |
| `minecraft:silk_touch` | I | Makes supported blocks drop themselves instead of normal drops. |
| `minecraft:fortune` | III | Increases supported block drops. |
| `minecraft:luck_of_the_sea` | III | Improves fishing treasure odds. |
| `minecraft:lure` | III | Reduces fishing bite wait time. |

### Mace

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:density` | V | Increases mace smash damage based on fall distance. |
| `minecraft:breach` | IV | Reduces the effectiveness of the target's armor for mace hits. |
| `minecraft:wind_burst` | III | Launches the attacker upward after a successful smash attack. |

### Spear

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:lunge` | III | Propels the player horizontally during the spear jab; consumes hunger and has its vanilla usage restrictions. |

### Universal durability / curse

| ID | Max | Effect summary |
| --- | ---: | --- |
| `minecraft:unbreaking` | III | Gives durability-consuming actions a chance not to consume durability. |
| `minecraft:mending` | I | Repairs the item using experience orbs. |
| `minecraft:vanishing_curse` | I | Destroys the item on the holder's death instead of dropping it normally. |

## Planned Totem flaw enchantments

These are negative enchantments owned by Totem Enchanting. They are flaws, not vanilla-style curses unless that decision is explicitly changed later.

| ID | Max | Proposed effect |
| --- | ---: | --- |
| `totem:heaviness` | III | Attack speed -10% / -20% / -30%. Does not reduce mining speed or base damage. |
| `totem:fragility` | III | When durability would actually be consumed, 25% / 50% / 75% chance to consume one additional durability point. Apply after Unbreaking resolution. |
| `totem:dullness` | III | Mining speed -10% / -20% / -30%; melee damage -5% / -10% / -15%. |
| `totem:instability` | III | 10% / 20% / 30% chance that a positive functional enchantment is treated as one level lower for that action. Exclude curses, flaws, Mending, and Instability itself from this degradation rule. |

## Manual structure

Recommended chapter order:

1. Enchanting power / Chiseled Bookshelf mechanics (existing pages).
2. How to read the enchantment reference.
3. Armor and movement enchantments.
4. Melee enchantments.
5. Bow and crossbow enchantments.
6. Trident enchantments.
7. Tool, gathering, and fishing enchantments.
8. Mace and spear enchantments.
9. Universal durability enchantments and curses.
10. Totem custom enchantments.
11. Totem flaw enchantments.

The implementation may split a category across several physical book pages to avoid overflow. It must use the current TotemCore adaptive manual layout rather than shrinking text below readable size.

## Validation requirements

GameTests should validate at least:

- the Enchanting manual section contains the enchantment-reference pages;
- every vanilla enchantment in the expected Minecraft 26.2 reference set has one manual descriptor;
- every registered Totem Enchanting enchantment has one manual descriptor;
- every descriptor provides an effect translation in `en_us` and `zh_tw`;
- item/icon mappings resolve when a corresponding ItemStack exists;
- no manual page exceeds the content area's intended entry count/layout contract.

When Minecraft changes its vanilla enchantment registry in a future supported game version, the expected reference set and manual descriptions must be updated in the same release.