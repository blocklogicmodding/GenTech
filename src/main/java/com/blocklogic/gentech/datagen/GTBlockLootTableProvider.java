package com.blocklogic.gentech.datagen;

import com.blocklogic.gentech.block.GTBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public class GTBlockLootTableProvider extends BlockLootSubProvider {
    protected GTBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(GTBlocks.COPPER_GENERATOR.get());
        dropSelf(GTBlocks.IRON_GENERATOR.get());
        dropSelf(GTBlocks.DIAMOND_GENERATOR.get());
        dropSelf(GTBlocks.NETHERITE_GENERATOR.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return GTBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
