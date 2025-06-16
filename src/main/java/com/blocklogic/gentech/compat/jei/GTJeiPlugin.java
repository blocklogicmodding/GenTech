package com.blocklogic.gentech.compat.jei;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.GTBlocks;
import com.blocklogic.gentech.config.CustomGeneratorRecipeConfig;
import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class GTJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(GenTech.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new GenerationRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<GenerationRecipe> generationRecipes = generateGenerationRecipes();
        registration.addRecipes(GenerationRecipeCategory.GENERATION_RECIPE_TYPE, generationRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(GTBlocks.COPPER_GENERATOR.get()),
                GenerationRecipeCategory.GENERATION_RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(GTBlocks.IRON_GENERATOR.get()),
                GenerationRecipeCategory.GENERATION_RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(GTBlocks.DIAMOND_GENERATOR.get()),
                GenerationRecipeCategory.GENERATION_RECIPE_TYPE
        );

        registration.addRecipeCatalyst(
                new ItemStack(GTBlocks.NETHERITE_GENERATOR.get()),
                GenerationRecipeCategory.GENERATION_RECIPE_TYPE
        );
    }

    private List<GenerationRecipe> generateGenerationRecipes() {
        List<GenerationRecipe> recipes = new ArrayList<>();

        if (CustomGeneratorRecipeConfig.getAllRecipes().isEmpty()) {
            CustomGeneratorRecipeConfig.loadRecipes();
        }

        for (CustomGeneratorRecipeConfig.CustomGeneratorRecipe customRecipe : CustomGeneratorRecipeConfig.getAllRecipes()) {
            try {
                GenerationRecipe jeiRecipe = GenerationRecipe.createFromCustomRecipe(customRecipe);
                if (jeiRecipe != null) {
                    recipes.add(jeiRecipe);
                }
            } catch (Exception e) {
                LogUtils.getLogger().error("Error creating JEI recipe for {}: {}", customRecipe.name, e.getMessage());
            }
        }

        LogUtils.getLogger().info("Generated {} JEI generation recipes", recipes.size());
        return recipes;
    }

    private static IJeiRuntime jeiRuntime;

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        GTJeiPlugin.jeiRuntime = jeiRuntime;
    }

    public static IJeiRuntime getJeiRuntime() {
        return jeiRuntime;
    }
}