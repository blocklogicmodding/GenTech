package com.blocklogic.gentech.client.renderer;

import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntityRenderer implements BlockEntityRenderer<GeneratorBlockEntity> {

    public GeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(GeneratorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = blockEntity.getLevel();
        if (level == null) return;

        Block targetBlock = blockEntity.getTargetBlock();
        if (targetBlock == null) return;

        BlockState targetState = targetBlock.defaultBlockState();

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        long gameTime = level.getGameTime();
        float rotation = (gameTime + partialTick) * 2.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        float progress = blockEntity.getProgressLevel();
        if (progress > 0) {
            float bobOffset = (float) Math.sin((gameTime + partialTick) * 0.2) * 0.05f;
            poseStack.translate(0, bobOffset, 0);

            float progressScale = 0.8f + (progress * 0.4f);
            poseStack.scale(progressScale, progressScale, progressScale);
        }

        poseStack.translate(-0.5, -0.5, -0.5);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                targetState,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRenderOffScreen(GeneratorBlockEntity blockEntity) {
        return false;
    }
}