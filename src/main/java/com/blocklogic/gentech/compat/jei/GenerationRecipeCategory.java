package com.blocklogic.gentech.compat.jei;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.GTBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GenerationRecipeCategory implements IRecipeCategory<GenerationRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(GenTech.MODID, "generation");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GenTech.MODID, "textures/gui/jei/jei_gen_recipe_gui.png");
    public static final RecipeType<GenerationRecipe> GENERATION_RECIPE_TYPE = new RecipeType<>(UID, GenerationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public GenerationRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 107, 76);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(GTBlocks.DIAMOND_GENERATOR.get()));
        this.arrow = guiHelper.createDrawable(TEXTURE, 160, 0, 11, 8);
    }

    @Override
    public RecipeType<GenerationRecipe> getRecipeType() {
        return GENERATION_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.gentech.category.generation");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 107;
    }

    @Override
    public int getHeight() {
        return 76;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GenerationRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 28, 29)
                .addItemStack(new ItemStack(recipe.getTargetBlock()))
                .addTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable("jei.gentech.generation.catalyst"));
                });

        builder.addSlot(RecipeIngredientRole.INPUT, 6, 29)
                .addFluidStack(recipe.getWaterStack().getFluid(), recipe.getWaterStack().getAmount())
                .setFluidRenderer(1000, false, 16, 16)
                .addTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable("jei.gentech.generation.water",
                            String.format("%,d", recipe.getWaterStack().getAmount())));
                });

        builder.addSlot(RecipeIngredientRole.INPUT, 50, 29)
                .addFluidStack(recipe.getLavaStack().getFluid(), recipe.getLavaStack().getAmount())
                .setFluidRenderer(1000, false, 16, 16)
                .addTooltipCallback((view, tooltip) -> {
                    tooltip.add(Component.translatable("jei.gentech.generation.lava",
                            String.format("%,d", recipe.getLavaStack().getAmount())));
                });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 84, 29)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(GenerationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 69, 33);

        Font font = net.minecraft.client.Minecraft.getInstance().font;
        String timeText = Component.translatable("jei.gentech.generation.time",
                String.format("%.1f", recipe.getGenerationTime() / 20.0f)).getString();

        int textWidth = font.width(timeText);
        int x = (getWidth() - textWidth) / 2;
        guiGraphics.drawString(font, timeText, x, 5, 0x404040, false);

        Component categoryComponent = switch (recipe.getCategory()) {
            case SOFT -> Component.translatable("tooltip.gentech.category.soft");
            case MEDIUM -> Component.translatable("tooltip.gentech.category.medium");
            case HARD -> Component.translatable("tooltip.gentech.category.hard");
        };

        String categoryText = categoryComponent.getString();
        int categoryWidth = font.width(categoryText);
        int categoryX = (getWidth() - categoryWidth) / 2;

        int categoryColor = switch (recipe.getCategory()) {
            case SOFT -> 0x55FF55;
            case MEDIUM -> 0xFFAA00;
            case HARD -> 0xFF5555;
        };

        guiGraphics.drawString(font, categoryText, categoryX, 65, categoryColor, false);
    }
}