package com.blocklogic.gentech.client.renderer;

import com.blocklogic.gentech.block.entity.TankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class TankBlockEntityRenderer implements BlockEntityRenderer<TankBlockEntity> {

    public TankBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TankBlockEntity tankEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = tankEntity.getLevel();
        if (level == null) return;

        FluidStack fluidStack = tankEntity.getFluidStack();
        if (fluidStack.isEmpty()) return;

        float fillLevel = tankEntity.getFluidLevel();
        if (fillLevel <= 0) return;

        IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation fluidTexture = fluidExtensions.getStillTexture(fluidStack);
        if (fluidTexture == null) return;

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidTexture);

        int fluidColor = fluidExtensions.getTintColor(fluidStack);
        float red = ((fluidColor >> 16) & 0xFF) / 255.0f;
        float green = ((fluidColor >> 8) & 0xFF) / 255.0f;
        float blue = (fluidColor & 0xFF) / 255.0f;
        float alpha = ((fluidColor >> 24) & 0xFF) / 255.0f;
        if (alpha == 0) alpha = 1.0f;

        float minX = 3.0f / 16.0f;
        float maxX = 13.0f / 16.0f;
        float minZ = 3.0f / 16.0f;
        float maxZ = 13.0f / 16.0f;
        float minY = 1.0f / 16.0f;
        float maxY = minY + (12.0f / 16.0f) * fillLevel;

        poseStack.pushPose();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.translucent());

        addQuad(vertexConsumer, matrix,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ,
                minX, maxY, minZ,
                sprite.getU0(), sprite.getV1(), sprite.getU1(), sprite.getV0(),
                red, green, blue, alpha, packedLight, 0, 1, 0);

        if (fillLevel > 0.05f) {
            float sideAlpha = alpha * 0.9f;

            addQuad(vertexConsumer, matrix,
                    minX, maxY, minZ,
                    maxX, maxY, minZ,
                    maxX, minY, minZ,
                    minX, minY, minZ,
                    sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                    red, green, blue, sideAlpha, packedLight, 0, 0, -1);

            addQuad(vertexConsumer, matrix,
                    maxX, maxY, maxZ,
                    minX, maxY, maxZ,
                    minX, minY, maxZ,
                    maxX, minY, maxZ,
                    sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                    red, green, blue, sideAlpha, packedLight, 0, 0, 1);

            addQuad(vertexConsumer, matrix,
                    maxX, maxY, minZ,
                    maxX, maxY, maxZ,
                    maxX, minY, maxZ,
                    maxX, minY, minZ,
                    sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                    red, green, blue, sideAlpha, packedLight, 1, 0, 0);

            addQuad(vertexConsumer, matrix,
                    minX, maxY, maxZ,
                    minX, maxY, minZ,
                    minX, minY, minZ,
                    minX, minY, maxZ,
                    sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                    red, green, blue, sideAlpha, packedLight, -1, 0, 0);
        }

        poseStack.popPose();
    }

    private void addQuad(VertexConsumer vertexConsumer, Matrix4f matrix,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float u1, float v1, float u2, float v2,
                         float red, float green, float blue, float alpha,
                         int packedLight, float normalX, float normalY, float normalZ) {

        vertexConsumer.addVertex(matrix, x1, y1, z1)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v1)
                .setLight(packedLight)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(matrix, x2, y2, z2)
                .setColor(red, green, blue, alpha)
                .setUv(u2, v1)
                .setLight(packedLight)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(matrix, x3, y3, z3)
                .setColor(red, green, blue, alpha)
                .setUv(u2, v2)
                .setLight(packedLight)
                .setNormal(normalX, normalY, normalZ);

        vertexConsumer.addVertex(matrix, x4, y4, z4)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v2)
                .setLight(packedLight)
                .setNormal(normalX, normalY, normalZ);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRenderOffScreen(TankBlockEntity blockEntity) {
        return false;
    }
}