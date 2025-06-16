package com.blocklogic.gentech.util;

import com.blocklogic.gentech.GenTech;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class GTTags {
    public static class Blocks {
        public static final TagKey<Block> GENTECH_GENERATORS = createTag("gentech_generators");
        public static final TagKey<Block> SOFT_GENERATABLE_BLOCKS = createTag("soft_generatable_blocks");
        public static final TagKey<Block> MEDIUM_GENERATABLE_BLOCKS = createTag("dense_generatable_blocks");
        public static final TagKey<Block> HARD_GENERATABLE_BLOCKS = createTag("hard_generatable_blocks");

        private static TagKey<Block> createTag (String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(GenTech.MODID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> GENTECH_GENERATOR_UPGRADES = createTag("gentech_generator_upgrades");
        public static final TagKey<Item> GENERATOR_TIER_UPGRADES = createTag("generator_tier_upgrades");
        public static final TagKey<Item> SOFT_GENERATABLE_BLOCK_ITEMS = createTag("soft_generatable_block_items");
        public static final TagKey<Item> MEDIUM_GENERATABLE_BLOCK_ITEMS = createTag("medium_generatable_block_items");
        public static final TagKey<Item> HARD_GENERATABLE_BLOCK_ITEMS = createTag("hard_generatable_block_items");

        private static TagKey<Item> createTag (String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(GenTech.MODID, name));
        }
    }
}
