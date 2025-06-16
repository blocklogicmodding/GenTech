package com.blocklogic.gentech.compat.jei;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.mojang.logging.LogUtils;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class GenerationRecipe implements IRecipeCategoryExtension {
    private final Block targetBlock;
    private final GeneratorBlockEntity.BlockCategory category;
    private final int waterAmount;
    private final int lavaAmount;
    private final int generationTime;
    private final String blockId;

    public GenerationRecipe(Block targetBlock, GeneratorBlockEntity.BlockCategory category,
                            int waterAmount, int lavaAmount, int generationTime, String blockId) {
        this.targetBlock = targetBlock;
        this.category = category;
        this.waterAmount = waterAmount;
        this.lavaAmount = lavaAmount;
        this.generationTime = generationTime;
        this.blockId = blockId;
    }

    public Block getTargetBlock() {
        return targetBlock;
    }

    public GeneratorBlockEntity.BlockCategory getCategory() {
        return category;
    }

    public FluidStack getWaterStack() {
        return new FluidStack(Fluids.WATER, waterAmount);
    }

    public FluidStack getLavaStack() {
        return new FluidStack(Fluids.LAVA, lavaAmount);
    }

    public int getGenerationTime() {
        return generationTime;
    }

    public ItemStack getOutput() {
        return new ItemStack(targetBlock.asItem());
    }

    public String getBlockId() {
        return blockId;
    }

    public static GenerationRecipe create(String blockId, GeneratorBlockEntity.BlockCategory category) {
        try {
            ResourceLocation blockLocation = ResourceLocation.parse(blockId);
            Block block = BuiltInRegistries.BLOCK.get(blockLocation);

            if (block == null) {
                LogUtils.getLogger().error("Failed to create generation recipe: Block not found for ID: {}", blockId);
                throw new IllegalArgumentException("Block not found for ID: " + blockId);
            }

            // Get base values for copper generator (most basic tier)
            int waterAmount = getWaterConsumption(category);
            int lavaAmount = getLavaConsumption(category);
            int generationTime = getGenerationTime(category);

            return new GenerationRecipe(block, category, waterAmount, lavaAmount, generationTime, blockId);
        } catch (Exception e) {
            LogUtils.getLogger().error("Error creating generation recipe for block {}: {}", blockId, e.getMessage());
            return null;
        }
    }

    private static int getWaterConsumption(GeneratorBlockEntity.BlockCategory category) {
        return switch (category) {
            case SOFT -> Config.getCopperGeneratorSoftConsumption();
            case MEDIUM -> Config.getCopperGeneratorMediumConsumption();
            case HARD -> Config.getCopperGeneratorHardConsumption();
        };
    }

    private static int getLavaConsumption(GeneratorBlockEntity.BlockCategory category) {
        return switch (category) {
            case SOFT -> Config.getCopperGeneratorSoftConsumption();
            case MEDIUM -> Config.getCopperGeneratorMediumConsumption();
            case HARD -> Config.getCopperGeneratorHardConsumption();
        };
    }

    private static int getGenerationTime(GeneratorBlockEntity.BlockCategory category) {
        return switch (category) {
            case SOFT -> Config.getCopperGeneratorSoftSpeed();
            case MEDIUM -> Config.getCopperGeneratorMediumSpeed();
            case HARD -> Config.getCopperGeneratorHardSpeed();
        };
    }
}