package com.blocklogic.gentech.screen.custom;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GenTech.MODID, "textures/gui/generator_gui.png");

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    private static final int TEXTURE_WIDTH = 212;
    private static final int TEXTURE_HEIGHT = 170;

    private static final int WATER_BUFFER_X = 35;
    private static final int WATER_BUFFER_Y = 17;
    private static final int WATER_BUFFER_WIDTH = 16;
    private static final int WATER_BUFFER_HEIGHT = 52;

    private static final int LAVA_BUFFER_X = 161;
    private static final int LAVA_BUFFER_Y = 17;
    private static final int LAVA_BUFFER_WIDTH = 16;
    private static final int LAVA_BUFFER_HEIGHT = 52;

    private static final int PROGRESS_BAR_X = 9;
    private static final int PROGRESS_BAR_Y = 17;
    private static final int PROGRESS_BAR_WIDTH = 15;
    private static final int PROGRESS_BAR_HEIGHT = 52;

    private static final int UPGRADE_SLOT_X = 187;
    private static final int UPGRADE_SLOT_Y_START = 16;
    private static final int UPGRADE_SLOT_SPACING = 18;
    private static final int UPGRADE_SLOT_SIZE = 18;

    private static final int WATER_FILL_SOURCE_X = 228;
    private static final int WATER_FILL_SOURCE_Y = 52;

    private static final int LAVA_FILL_SOURCE_X = 212;
    private static final int LAVA_FILL_SOURCE_Y = 52;

    private static final int PROGRESS_FILL_SOURCE_X = 212;
    private static final int PROGRESS_FILL_SOURCE_Y = 0;

    private static final int UPGRADE_SLOT_BACKGROUND_SOURCE_X = 212;
    private static final int UPGRADE_SLOT_BACKGROUND_SOURCE_Y = 105;

    private static final int INVENTORY_LABEL_X = 26;

    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
        this.inventoryLabelX = INVENTORY_LABEL_X;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        renderUpgradeSlotBackgrounds(guiGraphics, x, y);

        GeneratorBlockEntity blockEntity = this.menu.getBlockEntity();
        FluidStack waterFluid = blockEntity.getWaterTank().getFluid();
        FluidStack lavaFluid = blockEntity.getLavaTank().getFluid();

        renderFluidBuffer(guiGraphics, x, y, waterFluid, this.menu.getWaterLevel(),
                WATER_BUFFER_X, WATER_BUFFER_Y, WATER_BUFFER_WIDTH, WATER_BUFFER_HEIGHT);

        renderFluidBuffer(guiGraphics, x, y, lavaFluid, this.menu.getLavaLevel(),
                LAVA_BUFFER_X, LAVA_BUFFER_Y, LAVA_BUFFER_WIDTH, LAVA_BUFFER_HEIGHT);

        renderProgressBar(guiGraphics, x, y);
    }

    private void renderUpgradeSlotBackgrounds(GuiGraphics guiGraphics, int guiX, int guiY) {
        GeneratorBlockEntity blockEntity = this.menu.getBlockEntity();
        int upgradeSlots = blockEntity.getUpgradeSlots();

        for (int i = 0; i < upgradeSlots; i++) {
            int slotX = guiX + UPGRADE_SLOT_X;
            int slotY = guiY + UPGRADE_SLOT_Y_START + (i * UPGRADE_SLOT_SPACING);

            guiGraphics.blit(TEXTURE,
                    slotX, slotY,
                    UPGRADE_SLOT_BACKGROUND_SOURCE_X, UPGRADE_SLOT_BACKGROUND_SOURCE_Y,
                    UPGRADE_SLOT_SIZE, UPGRADE_SLOT_SIZE
            );
        }
    }

    private void renderFluidBuffer(GuiGraphics guiGraphics, int guiX, int guiY, FluidStack fluidStack, float fillLevel, int bufferX, int bufferY, int bufferWidth, int bufferHeight) {
        if (fillLevel <= 0 || fluidStack.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation fluidTexture = fluidExtensions.getStillTexture(fluidStack);

        if (fluidTexture == null) {
            return;
        }

        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTexture);

        int fillHeight = (int) (bufferHeight * fillLevel);
        int targetX = guiX + bufferX;
        int targetY = guiY + bufferY + (bufferHeight - fillHeight);

        int fluidColor = fluidExtensions.getTintColor(fluidStack);
        float red = ((fluidColor >> 16) & 0xFF) / 255.0f;
        float green = ((fluidColor >> 8) & 0xFF) / 255.0f;
        float blue = (fluidColor & 0xFF) / 255.0f;
        float alpha = ((fluidColor >> 24) & 0xFF) / 255.0f;

        if (alpha == 0) alpha = 1.0f;

        guiGraphics.setColor(red, green, blue, alpha);
        guiGraphics.blit(targetX, targetY, 0, bufferWidth, fillHeight, sprite);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset color
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int guiX, int guiY) {
        float progress = this.menu.getProgressLevel();

        if (progress > 0) {
            int fillHeight = (int) (PROGRESS_BAR_HEIGHT * progress);
            int targetX = guiX + PROGRESS_BAR_X;
            int targetY = guiY + PROGRESS_BAR_Y + (PROGRESS_BAR_HEIGHT - fillHeight);

            int sourceY = PROGRESS_FILL_SOURCE_Y + (PROGRESS_BAR_HEIGHT - fillHeight);

            guiGraphics.blit(TEXTURE,
                    targetX, targetY,
                    PROGRESS_FILL_SOURCE_X, sourceY,
                    PROGRESS_BAR_WIDTH, fillHeight
            );
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        int guiX = (width - imageWidth) / 2;
        int guiY = (height - imageHeight) / 2;

        GeneratorBlockEntity blockEntity = this.menu.getBlockEntity();

        if (x >= guiX + WATER_BUFFER_X && x <= guiX + WATER_BUFFER_X + WATER_BUFFER_WIDTH &&
                y >= guiY + WATER_BUFFER_Y && y <= guiY + WATER_BUFFER_Y + WATER_BUFFER_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            FluidStack waterFluid = blockEntity.getWaterTank().getFluid();
            int waterAmount = this.menu.getWaterAmount();
            int waterCapacity = this.menu.getWaterCapacity();

            if (!waterFluid.isEmpty()) {
                tooltip.add(waterFluid.getDisplayName().copy().withStyle(ChatFormatting.BOLD));
            } else {
                tooltip.add(Component.translatable("tooltip.gentech.water_tank")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            }

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tooltip.add(Component.translatable("tooltip.gentech.fluid_amount",
                            formatter.format(waterAmount),
                            formatter.format(waterCapacity))
                    .withStyle(ChatFormatting.BLUE));

            float percentage = waterCapacity > 0 ? (float) waterAmount / waterCapacity * 100 : 0;
            tooltip.add(Component.translatable("tooltip.gentech.fluid_percentage",
                            String.format("%.1f", percentage))
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.translatable("tooltip.gentech.water_input_side")
                    .withStyle(ChatFormatting.DARK_AQUA));

            guiGraphics.renderComponentTooltip(this.font, tooltip, x, y);
        }

        if (x >= guiX + LAVA_BUFFER_X && x <= guiX + LAVA_BUFFER_X + LAVA_BUFFER_WIDTH &&
                y >= guiY + LAVA_BUFFER_Y && y <= guiY + LAVA_BUFFER_Y + LAVA_BUFFER_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            FluidStack lavaFluid = blockEntity.getLavaTank().getFluid();
            int lavaAmount = this.menu.getLavaAmount();
            int lavaCapacity = this.menu.getLavaCapacity();

            if (!lavaFluid.isEmpty()) {
                tooltip.add(lavaFluid.getDisplayName().copy().withStyle(ChatFormatting.BOLD));
            } else {
                tooltip.add(Component.translatable("tooltip.gentech.lava_tank")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tooltip.add(Component.translatable("tooltip.gentech.fluid_amount",
                            formatter.format(lavaAmount),
                            formatter.format(lavaCapacity))
                    .withStyle(ChatFormatting.RED));

            float percentage = lavaCapacity > 0 ? (float) lavaAmount / lavaCapacity * 100 : 0;
            tooltip.add(Component.translatable("tooltip.gentech.fluid_percentage",
                            String.format("%.1f", percentage))
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.translatable("tooltip.gentech.lava_input_side")
                    .withStyle(ChatFormatting.DARK_RED));

            guiGraphics.renderComponentTooltip(this.font, tooltip, x, y);
        }

        if (x >= guiX + PROGRESS_BAR_X && x <= guiX + PROGRESS_BAR_X + PROGRESS_BAR_WIDTH &&
                y >= guiY + PROGRESS_BAR_Y && y <= guiY + PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            float progress = this.menu.getProgressLevel();

            tooltip.add(Component.translatable("tooltip.gentech.generation_progress")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

            if (progress > 0) {
                tooltip.add(Component.translatable("tooltip.gentech.progress_percentage",
                                String.format("%.1f", progress * 100))
                        .withStyle(ChatFormatting.GREEN));

                GeneratorBlockEntity.BlockCategory targetCategory = this.menu.getTargetCategory();
                if (targetCategory != null) {
                    Component categoryComponent = switch (targetCategory) {
                        case SOFT -> Component.translatable("tooltip.gentech.category.soft")
                                .withStyle(ChatFormatting.GREEN);
                        case MEDIUM -> Component.translatable("tooltip.gentech.category.medium")
                                .withStyle(ChatFormatting.YELLOW);
                        case HARD -> Component.translatable("tooltip.gentech.category.hard")
                                .withStyle(ChatFormatting.RED);
                    };
                    tooltip.add(Component.translatable("tooltip.gentech.generating_category", categoryComponent));
                }
            } else {
                tooltip.add(Component.translatable("tooltip.gentech.status.idle")
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.gentech.place_above_block")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }

            guiGraphics.renderComponentTooltip(this.font, tooltip, x, y);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}