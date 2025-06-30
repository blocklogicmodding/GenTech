package com.blocklogic.gentech.screen.custom;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.block.entity.CollectorBlockEntity;
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

public class CollectorScreen extends AbstractContainerScreen<CollectorMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GenTech.MODID, "textures/gui/collector_gui.png");

    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 170;

    private static final int FLUID_BUFFER_X = 53;
    private static final int FLUID_BUFFER_Y = 17;
    private static final int FLUID_BUFFER_WIDTH = 70;
    private static final int FLUID_BUFFER_HEIGHT = 52;

    private static final int PROGRESS_BAR_X = 8;
    private static final int PROGRESS_BAR_Y = 17;
    private static final int PROGRESS_BAR_WIDTH = 15;
    private static final int PROGRESS_BAR_HEIGHT = 52;

    private static final int PROGRESS_FILL_SOURCE_X = 176;
    private static final int PROGRESS_FILL_SOURCE_Y = 0;

    private static final int INVENTORY_LABEL_X = 8;

    public CollectorScreen(CollectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
        this.inventoryLabelX = INVENTORY_LABEL_X;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        CollectorBlockEntity blockEntity = this.menu.getBlockEntity();
        FluidStack fluidStack = blockEntity.getFluidTank().getFluid();
        renderFluidBuffer(guiGraphics, x, y, fluidStack, this.menu.getFluidLevel());

        renderProgressBar(guiGraphics, x, y);
    }

    private void renderFluidBuffer(GuiGraphics guiGraphics, int guiX, int guiY, FluidStack fluidStack, float fillLevel) {
        if (fillLevel <= 0 || fluidStack.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation fluidTexture = fluidExtensions.getStillTexture(fluidStack);

        if (fluidTexture == null) {
            return;
        }

        TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidTexture);

        int fillHeight = (int) (FLUID_BUFFER_HEIGHT * fillLevel);
        int targetX = guiX + FLUID_BUFFER_X;
        int targetY = guiY + FLUID_BUFFER_Y + (FLUID_BUFFER_HEIGHT - fillHeight);

        int fluidColor = fluidExtensions.getTintColor(fluidStack);
        float red = ((fluidColor >> 16) & 0xFF) / 255.0f;
        float green = ((fluidColor >> 8) & 0xFF) / 255.0f;
        float blue = (fluidColor & 0xFF) / 255.0f;
        float alpha = ((fluidColor >> 24) & 0xFF) / 255.0f;

        if (alpha == 0) alpha = 1.0f;

        guiGraphics.setColor(red, green, blue, alpha);
        guiGraphics.blit(targetX, targetY, 0, FLUID_BUFFER_WIDTH, fillHeight, sprite);
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

        CollectorBlockEntity blockEntity = this.menu.getBlockEntity();

        if (x >= guiX + FLUID_BUFFER_X && x <= guiX + FLUID_BUFFER_X + FLUID_BUFFER_WIDTH &&
                y >= guiY + FLUID_BUFFER_Y && y <= guiY + FLUID_BUFFER_Y + FLUID_BUFFER_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            FluidStack fluidStack = blockEntity.getFluidTank().getFluid();
            int fluidAmount = this.menu.getFluidAmount();
            int fluidCapacity = this.menu.getFluidCapacity();

            if (!fluidStack.isEmpty()) {
                tooltip.add(fluidStack.getDisplayName().copy().withStyle(ChatFormatting.BOLD));
            } else {
                CollectorBlockEntity.CollectorType type = blockEntity.getCollectorType();
                String fluidName = type == CollectorBlockEntity.CollectorType.HYDRO ? "Water" : "Lava";
                tooltip.add(Component.literal(fluidName + " Tank")
                        .withStyle(type == CollectorBlockEntity.CollectorType.HYDRO ?
                                ChatFormatting.AQUA : ChatFormatting.RED, ChatFormatting.BOLD));
            }

            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            tooltip.add(Component.translatable("tooltip.gentech.fluid_amount",
                            formatter.format(fluidAmount),
                            formatter.format(fluidCapacity))
                    .withStyle(ChatFormatting.BLUE));

            float percentage = fluidCapacity > 0 ? (float) fluidAmount / fluidCapacity * 100 : 0;
            tooltip.add(Component.translatable("tooltip.gentech.fluid_percentage",
                            String.format("%.1f", percentage))
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.translatable("tooltip.gentech.collector_export")
                    .withStyle(ChatFormatting.DARK_GRAY));

            guiGraphics.renderComponentTooltip(this.font, tooltip, x, y);
        }

        if (x >= guiX + PROGRESS_BAR_X && x <= guiX + PROGRESS_BAR_X + PROGRESS_BAR_WIDTH &&
                y >= guiY + PROGRESS_BAR_Y && y <= guiY + PROGRESS_BAR_Y + PROGRESS_BAR_HEIGHT) {

            List<Component> tooltip = new ArrayList<>();
            float progress = this.menu.getProgressLevel();
            boolean hasValidSources = this.menu.hasValidSources();

            tooltip.add(Component.translatable("tooltip.gentech.collection_progress")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

            if (hasValidSources && progress > 0) {
                tooltip.add(Component.translatable("tooltip.gentech.progress_percentage",
                                String.format("%.1f", progress * 100))
                        .withStyle(ChatFormatting.GREEN));

                CollectorBlockEntity.CollectorType type = blockEntity.getCollectorType();
                String fluidName = type == CollectorBlockEntity.CollectorType.HYDRO ? "water" : "lava";
                tooltip.add(Component.translatable("tooltip.gentech.collecting_fluid", fluidName)
                        .withStyle(ChatFormatting.AQUA));
            } else if (!hasValidSources) {
                tooltip.add(Component.translatable("tooltip.gentech.status.no_sources")
                        .withStyle(ChatFormatting.RED));

                CollectorBlockEntity.CollectorType type = blockEntity.getCollectorType();
                String fluidName = type == CollectorBlockEntity.CollectorType.HYDRO ? "water" : "lava";
                tooltip.add(Component.translatable("tooltip.gentech.requires_sources", fluidName)
                        .withStyle(ChatFormatting.DARK_RED));
            } else {
                tooltip.add(Component.translatable("tooltip.gentech.status.tank_full")
                        .withStyle(ChatFormatting.GOLD));
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