package com.blocklogic.gentech.datagen;

import com.blocklogic.gentech.GenTech;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class GTBlockStateProvider extends BlockStateProvider {
    public GTBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, GenTech.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }

    private void blockWithItem (DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
}
