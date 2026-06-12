# Easy Anvils

A 1.12.2 backport of [Easy Anvils](https://www.curseforge.com/minecraft/mc-mods/easy-anvils) by Fuzs.

Overhauled anvils: input items stay inside when you close the GUI and render on top of the block, "Too Expensive!" can be raised or removed, renames can be free, and almost every cost is configurable. You can also repair anvils with iron blocks and edit name tags without an anvil at all.

Requires 1.12.2, Forge 14.23.5.2860+, and [MixinBooter](https://www.curseforge.com/minecraft/mc-mods/mixin-booter) 9.1+.

## Features

- **Items stay in the anvil** — input items persist when the GUI is closed and render on top of the block. Hopper- and comparator-aware.
- **Configurable repair costs** — prior-work-penalty mode (fixed / vanilla / disabled), per-rarity enchantment multipliers, halved book costs, and adjustable repair-material costs.
- **No more "Too Expensive!"** — set a custom level limit or remove it entirely.
- **Free renames** — for all items, or name tags only.
- **Repair anvils with iron blocks** — by hand or from a dispenser.
- **Edit name tags without an anvil** — sneak + right-click a name tag to open a small naming screen.
- **Formatting codes in names** — use `&` colour/style codes in the anvil and the name tag screen.
- **Configurable anvil break chance**, with optional risk-free renaming.
- **Combine incompatible enchantments** *(off by default)* — optionally allow normally mutually-exclusive enchantments on one item (Sharpness + Smite + Bane of Arthropods, or every Protection on one piece of armor).

Everything is configurable in `config/easyanvils.cfg`, with an in-game config screen from the mod list.

## Mod Compatibility

Built for maximum compatibility. Every gameplay tweak (costs, break chance, repairs, name
tags) runs through Forge events — `AnvilUpdateEvent`, `AnvilRepairEvent`, and interaction
events — so no anvil class is replaced.

The only two vanilla-touching mixins exist for the "items stay in the anvil" feature (giving
the anvil a block entity and backing the container's input slots with it). Both disable
themselves automatically when another mod overhauls the anvil container, so the cost and
name-tag features keep working even alongside a full anvil-replacement mod.

## License

Code is [MPL-2.0](LICENSE.md), matching the original. The name tag screen texture is Fuzs'
original asset and remains © Fuzs — see [LICENSE-ASSETS.md](LICENSE-ASSETS.md).

Original mod by Fuzs.
