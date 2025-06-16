package com.blocklogic.gentech.compat.jei;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.GTBlocks;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
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

        recipes.addAll(generateCategoryRecipes(GeneratorBlockEntity.BlockCategory.SOFT));
        recipes.addAll(generateCategoryRecipes(GeneratorBlockEntity.BlockCategory.MEDIUM));
        recipes.addAll(generateCategoryRecipes(GeneratorBlockEntity.BlockCategory.HARD));

        LogUtils.getLogger().info("Generated {} total generation recipes for JEI", recipes.size());
        return recipes;
    }

    private List<GenerationRecipe> generateCategoryRecipes(GeneratorBlockEntity.BlockCategory category) {
        List<GenerationRecipe> recipes = new ArrayList<>();
        List<String> blockList = getBlockListForCategory(category);

        for (String blockId : blockList) {
            try {
                GenerationRecipe recipe = GenerationRecipe.create(blockId, category);
                if (recipe != null) {
                    recipes.add(recipe);
                }
            } catch (Exception e) {
                LogUtils.getLogger().error("Error creating generation recipe for block {}: {}",
                        blockId, e.getMessage(), e);
            }
        }

        LogUtils.getLogger().info("Generated {} {} block generation recipes for JEI",
                recipes.size(), category.name().toLowerCase());
        return recipes;
    }

    private List<String> getBlockListForCategory(GeneratorBlockEntity.BlockCategory category) {
        return switch (category) {
            case SOFT -> Config.getValidatedSoftGeneratableBlocks();
            case MEDIUM -> Config.getValidatedMediumGeneratableBlocks();
            case HARD -> Config.getValidatedHardGeneratableBlocks();
        };
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