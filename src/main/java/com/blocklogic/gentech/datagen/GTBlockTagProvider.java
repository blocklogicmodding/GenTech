package com.blocklogic.gentech.datagen;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.GTBlocks;
import com.blocklogic.gentech.util.GTTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GTBlockTagProvider extends BlockTagsProvider {
    public GTBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, GenTech.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(GTBlocks.COPPER_GENERATOR.get())
                .add(GTBlocks.IRON_GENERATOR.get())
                .add(GTBlocks.DIAMOND_GENERATOR.get())
                .add(GTBlocks.NETHERITE_GENERATOR.get());

        tag(GTTags.Blocks.GENTECH_GENERATORS)
                .add(GTBlocks.COPPER_GENERATOR.get())
                .add(GTBlocks.IRON_GENERATOR.get())
                .add(GTBlocks.DIAMOND_GENERATOR.get())
                .add(GTBlocks.NETHERITE_GENERATOR.get());


        tag(GTTags.Blocks.SOFT_GENERATABLE_BLOCKS)
                .add(Blocks.COBBLESTONE)
                .add(Blocks.STONE)
                .add(Blocks.COBBLED_DEEPSLATE)
                .add(Blocks.TUFF)
                .add(Blocks.CALCITE)
                .add(Blocks.NETHERRACK);

        tag(GTTags.Blocks.MEDIUM_GENERATABLE_BLOCKS)
                .add(Blocks.DEEPSLATE)
                .add(Blocks.ANDESITE)
                .add(Blocks.DIORITE)
                .add(Blocks.GRANITE)
                .add(Blocks.BASALT)
                .add(Blocks.END_STONE);

        tag(GTTags.Blocks.HARD_GENERATABLE_BLOCKS)
                .add(Blocks.SMOOTH_BASALT)
                .add(Blocks.OBSIDIAN)
                .add(Blocks.CRYING_OBSIDIAN)
                .add(Blocks.DRIPSTONE_BLOCK)
                .add(Blocks.BLACKSTONE)
                .add(Blocks.SMOOTH_STONE);
    }
}
