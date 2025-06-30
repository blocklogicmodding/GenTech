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
        public static final TagKey<Block> GENTECH_TANKS = createTag("gentech_tanks");
        public static final TagKey<Block> GENTECH_COLLECTORS = createTag("gentech_collectors");

        private static TagKey<Block> createTag (String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(GenTech.MODID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> GENTECH_GENERATOR_UPGRADES = createTag("gentech_generator_upgrades");
        public static final TagKey<Item> GENERATOR_TIER_UPGRADES = createTag("generator_tier_upgrades");

        private static TagKey<Item> createTag (String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(GenTech.MODID, name));
        }
    }
}
