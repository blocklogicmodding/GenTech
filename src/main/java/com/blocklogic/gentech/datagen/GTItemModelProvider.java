package com.blocklogic.gentech.datagen;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.item.GTItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GTItemModelProvider extends ItemModelProvider {
    public GTItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, GenTech.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(GTItems.BASIC_EFFICIENCY_UPGRADE.get());
        basicItem(GTItems.ADVANCED_EFFICIENCY_UPGRADE.get());
        basicItem(GTItems.ULTIMATE_EFFICIENCY_UPGRADE.get());
        basicItem(GTItems.BASIC_SPEED_UPGRADE.get());
        basicItem(GTItems.ADVANCED_SPEED_UPGRADE.get());
        basicItem(GTItems.ULTIMATE_SPEED_UPGRADE.get());
        basicItem(GTItems.COPPER_TO_IRON_GENERATOR_UPGRADE.get());
        basicItem(GTItems.IRON_TO_DIAMOND_GENERATOR_UPGRADE.get());
        basicItem(GTItems.DIAMOND_TO_NETHERITE_GENERATOR_UPGRADE.get());
    }
}
