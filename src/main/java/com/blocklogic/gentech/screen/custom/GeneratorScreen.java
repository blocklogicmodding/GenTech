package com.blocklogic.gentech.screen.custom;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

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

    // Texture size
    private static final int TEXTURE_WIDTH = 212;
    private static final int TEXTURE_HEIGHT = 170;

    // Fluid buffer positions (target positions on screen)
    private static final int WATER_BUFFER_X = 35;
    private static final int WATER_BUFFER_Y = 17;
    private static final int WATER_BUFFER_WIDTH = 16;
    private static final int WATER_BUFFER_HEIGHT = 52;

    private static final int LAVA_BUFFER_X = 161;
    private static final int LAVA_BUFFER_Y = 17;
    private static final int LAVA_BUFFER_WIDTH = 16;
    private static final int LAVA_BUFFER_HEIGHT = 52;

    // Progress bar position (target position on screen)
    private static final int PROGRESS_BAR_X = 9;
    private static final int PROGRESS_BAR_Y = 17;
    private static final int PROGRESS_BAR_WIDTH = 15;
    private static final int PROGRESS_BAR_HEIGHT = 52;

    // Upgrade slot positions (target positions on screen)
    private static final int UPGRADE_SLOT_X = 187;
    private static final int UPGRADE_SLOT_Y_START = 16;
    private static final int UPGRADE_SLOT_SPACING = 18;
    private static final int UPGRADE_SLOT_SIZE = 18;

    // Baked fill coordinates (source coordinates from texture)
    private static final int WATER_FILL_SOURCE_X = 228;
    private static final int WATER_FILL_SOURCE_Y = 52;

    private static final int LAVA_FILL_SOURCE_X = 212;
    private static final int LAVA_FILL_SOURCE_Y = 52;

    private static final int PROGRESS_FILL_SOURCE_X = 212;
    private static final int PROGRESS_FILL_SOURCE_Y = 0;

    private static final int UPGRADE_SLOT_BACKGROUND_SOURCE_X = 228;
    private static final int UPGRADE_SLOT_BACKGROUND_SOURCE_Y = 0;

    // Custom label position
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

        // Render main GUI texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render upgrade slot backgrounds based on tier
        renderUpgradeSlotBackgrounds(guiGraphics, x, y);

        // Render fluid buffers (fill from bottom) - now using real values
        renderWaterBuffer(guiGraphics, x, y);
        renderLavaBuffer(guiGraphics, x, y);

        // Render progress bar (fill from bottom)
        renderProgressBar(guiGraphics, x, y);
    }

    private void renderUpgradeSlotBackgrounds(GuiGraphics guiGraphics, int guiX, int guiY) {
        GeneratorBlockEntity blockEntity = this.menu.getBlockEntity();
        int upgradeSlots = blockEntity.getUpgradeSlots();

        // Render upgrade slot backgrounds for available slots
        for (int i = 0; i < upgradeSlots; i++) {
            int slotX = guiX + UPGRADE_SLOT_X;
            int slotY = guiY + UPGRADE_SLOT_Y_START + (i * UPGRADE_SLOT_SPACING);

            guiGraphics.blit(TEXTURE,
                    slotX, slotY,  // Target position
                    UPGRADE_SLOT_BACKGROUND_SOURCE_X, UPGRADE_SLOT_BACKGROUND_SOURCE_Y,  // Source position
                    UPGRADE_SLOT_SIZE, UPGRADE_SLOT_SIZE  // Size
            );
        }
    }

    private void renderWaterBuffer(GuiGraphics guiGraphics, int guiX, int guiY) {
        // Get water level from synced menu data
        float waterLevel = this.menu.getWaterLevel();

        if (waterLevel > 0) {
            int fillHeight = (int) (WATER_BUFFER_HEIGHT * waterLevel);
            int targetX = guiX + WATER_BUFFER_X;
            int targetY = guiY + WATER_BUFFER_Y + (WATER_BUFFER_HEIGHT - fillHeight); // Fill from bottom

            // Source coordinates adjusted for fill height (fill from bottom of source texture too)
            int sourceY = WATER_FILL_SOURCE_Y + (WATER_BUFFER_HEIGHT - fillHeight);

            guiGraphics.blit(TEXTURE,
                    targetX, targetY,  // Target position (bottom-aligned)
                    WATER_FILL_SOURCE_X, sourceY,  // Source position (bottom portion)
                    WATER_BUFFER_WIDTH, fillHeight  // Size
            );
        }
    }

    private void renderLavaBuffer(GuiGraphics guiGraphics, int guiX, int guiY) {
        // Get lava level from synced menu data
        float lavaLevel = this.menu.getLavaLevel();

        if (lavaLevel > 0) {
            int fillHeight = (int) (LAVA_BUFFER_HEIGHT * lavaLevel);
            int targetX = guiX + LAVA_BUFFER_X;
            int targetY = guiY + LAVA_BUFFER_Y + (LAVA_BUFFER_HEIGHT - fillHeight); // Fill from bottom

            // Source coordinates adjusted for fill height (fill from bottom of source texture too)
            int sourceY = LAVA_FILL_SOURCE_Y + (LAVA_BUFFER_HEIGHT - fillHeight);

            guiGraphics.blit(TEXTURE,
                    targetX, targetY,  // Target position (bottom-aligned)
                    LAVA_FILL_SOURCE_X, sourceY,  // Source position (bottom portion)
                    LAVA_BUFFER_WIDTH, fillHeight  // Size
            );
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int guiX, int guiY) {
        // TODO: Get actual progress from block entity
        float progress = 0.3f; // Placeholder - 30% progress

        if (progress > 0) {
            int fillHeight = (int) (PROGRESS_BAR_HEIGHT * progress);
            int targetX = guiX + PROGRESS_BAR_X;
            int targetY = guiY + PROGRESS_BAR_Y + (PROGRESS_BAR_HEIGHT - fillHeight); // Fill from bottom

            // Source coordinates adjusted for fill height (fill from bottom of source texture too)
            int sourceY = PROGRESS_FILL_SOURCE_Y + (PROGRESS_BAR_HEIGHT - fillHeight);

            guiGraphics.blit(TEXTURE,
                    targetX, targetY,  // Target position (bottom-aligned)
                    PROGRESS_FILL_SOURCE_X, sourceY,  // Source position (bottom portion)
                    PROGRESS_BAR_WIDTH, fillHeight  // Size
            );
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);

        int guiX = (width - imageWidth) / 2;
        int guiY = (height - imageHeight) / 2;

        GeneratorBlockEntity blockEntity = this.menu.getBlockEntity();

        // Water tank tooltip
        if (x >= guiX + WATER_BUFFER_X && x <= guiX + WATER_BUFFER_X + WATER_BUFFER_WIDTH &&
                y >= guiY + WATER_BUFFER_Y && y <= guiY + WATER_BUFFER_Y + WATER_BUFFER_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            int waterAmount = this.menu.getWaterAmount();
            int waterCapacity = this.menu.getWaterCapacity();

            tooltip.add(Component.translatable("tooltip.gentech.water_tank"));

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tooltip.add(Component.literal(String.format("%s / %s mB",
                            formatter.format(waterAmount),
                            formatter.format(waterCapacity)))
                    .withStyle(ChatFormatting.BLUE));

            guiGraphics.renderComponentTooltip(this.font, tooltip, x, y);
        }

        // Lava tank tooltip
        if (x >= guiX + LAVA_BUFFER_X && x <= guiX + LAVA_BUFFER_X + LAVA_BUFFER_WIDTH &&
                y >= guiY + LAVA_BUFFER_Y && y <= guiY + LAVA_BUFFER_Y + LAVA_BUFFER_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            int lavaAmount = this.menu.getLavaAmount();
            int lavaCapacity = this.menu.getLavaCapacity();

            tooltip.add(Component.translatable("tooltip.gentech.lava_tank"));

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tooltip.add(Component.literal(String.format("%s / %s mB",
                            formatter.format(lavaAmount),
                            formatter.format(lavaCapacity)))
                    .withStyle(ChatFormatting.RED));

            guiGraphics.renderComponentTooltip(this.font, tooltip, x, y);
        }

        // Progress bar tooltip
        if (x >= guiX + PROGRESS_BAR_X && x <= guiX + PROGRESS_BAR_X + PROGRESS_BAR_WIDTH &&
                y >= guiY + PROGRESS_BAR_Y && y <= guiY + PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            // TODO: Get actual progress from block entity
            float progress = 0.3f; // Placeholder

            tooltip.add(Component.translatable("tooltip.gentech.generation_progress"));
            tooltip.add(Component.literal(String.format("%.1f%%", progress * 100))
                    .withStyle(ChatFormatting.GREEN));

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