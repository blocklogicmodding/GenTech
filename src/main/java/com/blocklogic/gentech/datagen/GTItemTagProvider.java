package com.blocklogic.gentech.datagen;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.item.GTItems;
import com.blocklogic.gentech.util.GTTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GTItemTagProvider extends ItemTagsProvider {
    public GTItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, GenTech.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(GTTags.Items.GENERATOR_TIER_UPGRADES)
                .add(GTItems.COPPER_TO_IRON_GENERATOR_UPGRADE.get())
                .add(GTItems.IRON_TO_DIAMOND_GENERATOR_UPGRADE.get())
                .add(GTItems.DIAMOND_TO_NETHERITE_GENERATOR_UPGRADE.get());

        tag(GTTags.Items.GENTECH_GENERATOR_UPGRADES)
                .add(GTItems.BASIC_EFFICIENCY_UPGRADE.get())
                .add(GTItems.ADVANCED_EFFICIENCY_UPGRADE.get())
                .add(GTItems.ULTIMATE_EFFICIENCY_UPGRADE.get())
                .add(GTItems.BASIC_SPEED_UPGRADE.get())
                .add(GTItems.ADVANCED_SPEED_UPGRADE.get())
                .add(GTItems.ULTIMATE_SPEED_UPGRADE.get());

        copy(GTTags.Blocks.SOFT_GENERATABLE_BLOCKS, GTTags.Items.SOFT_GENERATABLE_BLOCK_ITEMS);
        copy(GTTags.Blocks.MEDIUM_GENERATABLE_BLOCKS, GTTags.Items.MEDIUM_GENERATABLE_BLOCK_ITEMS);
        copy(GTTags.Blocks.SOFT_GENERATABLE_BLOCKS, GTTags.Items.HARD_GENERATABLE_BLOCK_ITEMS);
    }
}
