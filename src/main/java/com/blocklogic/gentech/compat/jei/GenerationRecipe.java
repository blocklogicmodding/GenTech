package com.blocklogic.gentech.compat.jei;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.blocklogic.gentech.config.CustomGeneratorRecipeConfig;
import com.mojang.logging.LogUtils;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class GenerationRecipe implements IRecipeCategoryExtension {
    private final Block targetBlock;
    private final GeneratorBlockEntity.BlockCategory category;
    private final Fluid fluid1;
    private final Fluid fluid2;
    private final int fluid1Amount;
    private final int fluid2Amount;
    private final int generationTime;
    private final String recipeName;

    public GenerationRecipe(Block targetBlock, GeneratorBlockEntity.BlockCategory category,
                            Fluid fluid1, Fluid fluid2, int fluid1Amount, int fluid2Amount,
                            int generationTime, String recipeName) {
        this.targetBlock = targetBlock;
        this.category = category;
        this.fluid1 = fluid1;
        this.fluid2 = fluid2;
        this.fluid1Amount = fluid1Amount;
        this.fluid2Amount = fluid2Amount;
        this.generationTime = generationTime;
        this.recipeName = recipeName;
    }

    public Block getTargetBlock() {
        return targetBlock;
    }

    public GeneratorBlockEntity.BlockCategory getCategory() {
        return category;
    }

    public FluidStack getFluid1Stack() {
        return new FluidStack(fluid1, fluid1Amount);
    }

    public FluidStack getFluid2Stack() {
        return new FluidStack(fluid2, fluid2Amount);
    }

    public int getGenerationTime() {
        return generationTime;
    }

    public ItemStack getOutput() {
        return new ItemStack(targetBlock.asItem());
    }

    public String getRecipeName() {
        return recipeName;
    }

    public FluidStack getWaterStack() {
        return getFluid1Stack();
    }

    public FluidStack getLavaStack() {
        return getFluid2Stack();
    }

    public static GenerationRecipe createFromCustomRecipe(CustomGeneratorRecipeConfig.CustomGeneratorRecipe customRecipe) {
        try {
            int fluid1Amount = getFluidConsumption(customRecipe.category);
            int fluid2Amount = getFluidConsumption(customRecipe.category);
            int generationTime = getGenerationTime(customRecipe.category);

            return new GenerationRecipe(
                    customRecipe.catalyst,
                    customRecipe.category,
                    customRecipe.fluid1,
                    customRecipe.fluid2,
                    fluid1Amount,
                    fluid2Amount,
                    generationTime,
                    customRecipe.name
            );
        } catch (Exception e) {
            LogUtils.getLogger().error("Error creating JEI recipe for {}: {}", customRecipe.name, e.getMessage());
            return null;
        }
    }

    private static int getFluidConsumption(GeneratorBlockEntity.BlockCategory category) {
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