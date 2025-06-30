
# GenTech

![GenTech](https://deonjonker.com/blm/gentech/gt_banner2.png)

A Minecraft mod that adds tiered block generators powered by fluid combinations. Generate blocks by placing generators above them and supplying the required fluids.

## Features

**Four Generator Tiers:**

-   **Copper Generator** - Basic tier with no upgrade slots
-   **Iron Generator** - 1 upgrade slot, improved speed and efficiency
-   **Diamond Generator** - 2 upgrade slots, faster generation
-   **Netherite Generator** - 3 upgrade slots, ultimate performance

**Upgrade System:**

-   **Speed Upgrades** - Reduce generation time (Basic, Advanced, Ultimate)
-   **Efficiency Upgrades** - Lower fluid consumption (Basic, Advanced, Ultimate)
-   **Tier Upgrades** - Convert generators to higher tiers while preserving contents

**Block Generation:**

-   Place any generator above a valid block to generate it
-   Requires water and lava by default (configurable via custom recipes)
-   Three generation categories: Soft, Medium, and Hard blocks
-   Different speeds and fluid costs based on block category

**Automation:**

-   Dual fluid tanks with directional input (East/West sides)
-   Automatic output to chests placed above generators
-   Item extraction via logistics from North/South sides
-   Persistent fluid storage when breaking/placing generators

**Customization:**

-   Extensive configuration file for speeds, costs, and fluid buffers (`config/gentech-common.toml`)
-   Custom recipe system via TOML files (`config/gentech/custom-generator-recipes.toml`)
-   Support for modded fluids and blocks
-   Hot config reloading with `/gentech reload` command

**Integration:**

-   JEI support showing all generation recipes
-   Compatible with fluid pipes and item automation
-   Works with any mod that adds blocks or fluids

## Configuration

The mod creates configuration files in `config/gentech/`:

-   `gentech-common.toml` - Main settings for speeds, costs, and block categories
-   `custom_generator_recipes.toml` - Define custom fluid combinations and blocks

Use `/gentech reload` to apply configuration changes without restarting.

## License

All rights reserved. This mod is protected by copyright and may not be redistributed or modified without explicit permission.

**Permitted Uses:**

-   Inclusion in modpacks (public or private)
-   Content creation (videos, streams, reviews, etc.)

----------

[**Wiki**](https://github.com/blocklogicmodding/GenTech/wiki) | [**Issue Tracker**](https://github.com/blocklogicmodding/GenTech/issues) | [**BLM Discord**](https://discord.gg/YtdA3AMqsXe)