package com.blocklogic.gentech.datagen;

import com.blocklogic.gentech.block.GTBlocks;
import com.blocklogic.gentech.item.GTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class GTRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public GTRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        // Generator Block Recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.COPPER_GENERATOR.get())
                .pattern("CCC")
                .pattern("BGB")
                .pattern("CCC")
                .define('C', Items.COPPER_BLOCK)
                .define('B', Items.BUCKET)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_copper_blocks", has(Items.COPPER_BLOCK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.IRON_GENERATOR.get())
                .pattern("III")
                .pattern("BCB")
                .pattern("III")
                .define('I', Items.IRON_BLOCK)
                .define('B', Items.BUCKET)
                .define('C', GTBlocks.COPPER_GENERATOR.get())
                .unlockedBy("has_copper_generator", has(GTBlocks.COPPER_GENERATOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.DIAMOND_GENERATOR.get())
                .pattern("DDD")
                .pattern("BIB")
                .pattern("DDD")
                .define('D', Items.DIAMOND_BLOCK)
                .define('B', Items.BUCKET)
                .define('I', GTBlocks.IRON_GENERATOR.get())
                .unlockedBy("has_iron_generator", has(GTBlocks.IRON_GENERATOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.NETHERITE_GENERATOR.get())
                .pattern("NNN")
                .pattern("BDB")
                .pattern("NNN")
                .define('N', Items.NETHERITE_INGOT)
                .define('B', Items.BUCKET)
                .define('D', GTBlocks.DIAMOND_GENERATOR.get())
                .unlockedBy("has_diamond_generator", has(GTBlocks.DIAMOND_GENERATOR.get()))
                .save(recipeOutput);

        // Tanks
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.COPPER_TANK.get())
                .pattern("CCC")
                .pattern("CGC")
                .pattern("CCC")
                .define('C', Items.COPPER_BLOCK)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_copper_blocks", has(Items.COPPER_BLOCK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.IRON_TANK.get())
                .pattern("III")
                .pattern("TCT")
                .pattern("III")
                .define('I', Items.IRON_BLOCK)
                .define('T', GTBlocks.COPPER_TANK.get())
                .define('C', Items.COPPER_BLOCK)
                .unlockedBy("has_copper_tank", has(GTBlocks.COPPER_TANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.DIAMOND_TANK.get())
                .pattern("DDD")
                .pattern("ITI")
                .pattern("DDD")
                .define('D', Items.DIAMOND_BLOCK)
                .define('I', Items.IRON_BLOCK)
                .define('T', GTBlocks.IRON_TANK.get())
                .unlockedBy("has_iron_tank", has(GTBlocks.IRON_TANK.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.NETHERITE_TANK.get())
                .pattern("NNN")
                .pattern("DTD")
                .pattern("NNN")
                .define('N', Items.NETHERITE_INGOT)
                .define('D', Items.DIAMOND_BLOCK)
                .define('T', GTBlocks.DIAMOND_TANK.get())
                .unlockedBy("has_diamond_tank", has(GTBlocks.DIAMOND_TANK.get()))
                .save(recipeOutput);

        // Hydro Collector
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.HYDRO_COLLECTOR.get())
                .pattern("IGI")
                .pattern("GBG")
                .pattern("IGI")
                .define('I', Items.IRON_BLOCK)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('B', Items.BUCKET)
                .unlockedBy("has_iron_blocks", has(Items.IRON_BLOCK))
                .save(recipeOutput);

        // Magma Collector
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTBlocks.MAGMA_COLLECTOR.get())
                .pattern("INI")
                .pattern("NLN")
                .pattern("INI")
                .define('I', Items.IRON_BLOCK)
                .define('N', Items.NETHERRACK)
                .define('L', Items.LAVA_BUCKET)
                .unlockedBy("has_iron_blocks", has(Items.IRON_BLOCK))
                .save(recipeOutput);

        // Generator Tier Upgrade Items (same recipes as the blocks they upgrade to)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.COPPER_TO_IRON_GENERATOR_UPGRADE.get())
                .pattern("III")
                .pattern("BCB")
                .pattern("III")
                .define('I', Items.IRON_BLOCK)
                .define('B', Items.BUCKET)
                .define('C', GTBlocks.COPPER_GENERATOR.get())
                .unlockedBy("has_copper_generator", has(GTBlocks.COPPER_GENERATOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.IRON_TO_DIAMOND_GENERATOR_UPGRADE.get())
                .pattern("DDD")
                .pattern("BIB")
                .pattern("DDD")
                .define('D', Items.DIAMOND_BLOCK)
                .define('B', Items.BUCKET)
                .define('I', GTBlocks.IRON_GENERATOR.get())
                .unlockedBy("has_iron_generator", has(GTBlocks.IRON_GENERATOR.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.DIAMOND_TO_NETHERITE_GENERATOR_UPGRADE.get())
                .pattern("NNN")
                .pattern("BDB")
                .pattern("NNN")
                .define('N', Items.NETHERITE_INGOT)
                .define('B', Items.BUCKET)
                .define('D', GTBlocks.DIAMOND_GENERATOR.get())
                .unlockedBy("has_diamond_generator", has(GTBlocks.DIAMOND_GENERATOR.get()))
                .save(recipeOutput);

        // Speed Upgrade Recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.BASIC_SPEED_UPGRADE.get())
                .pattern("IRI")
                .pattern("RSR")
                .pattern("IRI")
                .define('I', Items.IRON_BLOCK)
                .define('R', Items.REDSTONE)
                .define('S', Items.SUGAR)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.ADVANCED_SPEED_UPGRADE.get())
                .pattern("DRD")
                .pattern("RSR")
                .pattern("DRD")
                .define('D', Items.DIAMOND_BLOCK)
                .define('R', Items.REDSTONE)
                .define('S', GTItems.BASIC_SPEED_UPGRADE.get())
                .unlockedBy("has_speed_upgrade_mk1", has(GTItems.BASIC_SPEED_UPGRADE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.ULTIMATE_SPEED_UPGRADE.get())
                .pattern("NRN")
                .pattern("RSR")
                .pattern("NRN")
                .define('N', Items.NETHERITE_INGOT)
                .define('R', Items.REDSTONE)
                .define('S', GTItems.ADVANCED_SPEED_UPGRADE.get())
                .unlockedBy("has_speed_upgrade_mk2", has(GTItems.ADVANCED_SPEED_UPGRADE.get()))
                .save(recipeOutput);

        // Efficiency Upgrade Recipes
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.BASIC_EFFICIENCY_UPGRADE.get())
                .pattern("ICI")
                .pattern("CQC")
                .pattern("ICI")
                .define('I', Items.IRON_BLOCK)
                .define('C', Items.COPPER_INGOT)
                .define('Q', Items.QUARTZ)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.ADVANCED_EFFICIENCY_UPGRADE.get())
                .pattern("DCD")
                .pattern("CEC")
                .pattern("DCD")
                .define('C', Items.COPPER_INGOT)
                .define('D', Items.DIAMOND_BLOCK)
                .define('E', GTItems.BASIC_EFFICIENCY_UPGRADE.get())
                .unlockedBy("has_efficiency_upgrade_mk1", has(GTItems.BASIC_EFFICIENCY_UPGRADE.get()))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, GTItems.ULTIMATE_EFFICIENCY_UPGRADE.get())
                .pattern("NCN")
                .pattern("CEC")
                .pattern("NCN")
                .define('C', Items.COPPER_INGOT)
                .define('N', Items.NETHERITE_INGOT)
                .define('E', GTItems.ADVANCED_EFFICIENCY_UPGRADE.get())
                .unlockedBy("has_efficiency_upgrade_mk2", has(GTItems.ADVANCED_EFFICIENCY_UPGRADE.get()))
                .save(recipeOutput);
    }
}