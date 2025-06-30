package com.blocklogic.gentech.datagen;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.GTBlocks;
import com.blocklogic.gentech.util.GTTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
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
                .add(GTBlocks.NETHERITE_GENERATOR.get())
                .add(GTBlocks.COPPER_TANK.get())
                .add(GTBlocks.IRON_TANK.get())
                .add(GTBlocks.DIAMOND_TANK.get())
                .add(GTBlocks.NETHERITE_TANK.get())
                .add(GTBlocks.HYDRO_COLLECTOR.get())
                .add(GTBlocks.MAGMA_COLLECTOR.get());

        tag(GTTags.Blocks.GENTECH_GENERATORS)
                .add(GTBlocks.COPPER_GENERATOR.get())
                .add(GTBlocks.IRON_GENERATOR.get())
                .add(GTBlocks.DIAMOND_GENERATOR.get())
                .add(GTBlocks.NETHERITE_GENERATOR.get());

        tag(GTTags.Blocks.GENTECH_TANKS)
                .add(GTBlocks.COPPER_TANK.get())
                .add(GTBlocks.IRON_TANK.get())
                .add(GTBlocks.DIAMOND_TANK.get())
                .add(GTBlocks.NETHERITE_TANK.get());

        tag(GTTags.Blocks.GENTECH_COLLECTORS)
                .add(GTBlocks.HYDRO_COLLECTOR.get())
                .add(GTBlocks.MAGMA_COLLECTOR.get());
    }
}
