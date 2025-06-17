package com.blocklogic.gentech.util;

import com.blocklogic.gentech.block.GTBlocks;
import com.blocklogic.gentech.block.custom.CopperGeneratorBlock;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import com.blocklogic.gentech.component.GTDataComponents;
import com.blocklogic.gentech.item.GTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class GeneratorUpgradeHandler {

    public static boolean tryUpgradeGenerator(Level level, BlockPos pos, Player player, ItemStack upgradeItem) {
        if (level.isClientSide()) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof GeneratorBlockEntity generatorBE)) {
            return false;
        }

        Block currentBlock = level.getBlockState(pos).getBlock();
        Block targetBlock = getTargetBlock(currentBlock, upgradeItem.getItem());

        if (targetBlock == null) {
            showUpgradeMessage(player, Component.translatable("message.gentech.generator.upgrade_failed"), false);
            return false;
        }

        if (currentBlock == GTBlocks.NETHERITE_GENERATOR.get()) {
            showUpgradeMessage(player, Component.translatable("message.gentech.generator.max_tier"), false);
            return false;
        }

        if (performUpgrade(level, pos, generatorBE, targetBlock, player)) {
            if (!player.getAbilities().instabuild) {
                upgradeItem.shrink(1);
            }

            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, 1.2f);

            showUpgradeMessage(player, Component.translatable("message.gentech.generator.upgraded"), true);
            return true;
        }

        return false;
    }

    private static Block getTargetBlock(Block currentBlock, Item upgradeItem) {
        if (currentBlock == GTBlocks.COPPER_GENERATOR.get() &&
                upgradeItem == GTItems.COPPER_TO_IRON_GENERATOR_UPGRADE.get()) {
            return GTBlocks.IRON_GENERATOR.get();
        }

        if (currentBlock == GTBlocks.IRON_GENERATOR.get() &&
                upgradeItem == GTItems.IRON_TO_DIAMOND_GENERATOR_UPGRADE.get()) {
            return GTBlocks.DIAMOND_GENERATOR.get();
        }

        if (currentBlock == GTBlocks.DIAMOND_GENERATOR.get() &&
                upgradeItem == GTItems.DIAMOND_TO_NETHERITE_GENERATOR_UPGRADE.get()) {
            return GTBlocks.NETHERITE_GENERATOR.get();
        }

        return null;
    }

    private static boolean performUpgrade(Level level, BlockPos pos, GeneratorBlockEntity oldBE, Block targetBlock, Player player) {
        GeneratorUpgradeData upgradeData = saveGeneratorData(oldBE);

        BlockState oldState = level.getBlockState(pos);

        level.removeBlockEntity(pos);

        BlockState newState = targetBlock.defaultBlockState();
        if (oldState.hasProperty(CopperGeneratorBlock.FACING) &&
                newState.hasProperty(CopperGeneratorBlock.FACING)) {
            newState = newState.setValue(CopperGeneratorBlock.FACING,
                    oldState.getValue(CopperGeneratorBlock.FACING));
        }

        level.setBlock(pos, newState, 3);

        BlockEntity newBlockEntity = level.getBlockEntity(pos);
        if (newBlockEntity instanceof GeneratorBlockEntity newBE) {
            restoreGeneratorData(newBE, upgradeData);
            newBE.setChanged();
            level.sendBlockUpdated(pos, newState, newState, 3);

            newBE.forceRevalidation();

            return true;
        }

        return false;
    }

    private static GeneratorUpgradeData saveGeneratorData(GeneratorBlockEntity blockEntity) {
        GeneratorUpgradeData data = new GeneratorUpgradeData();

        FluidStack waterTankFluid = blockEntity.getWaterTank().getFluid();
        FluidStack lavaTankFluid = blockEntity.getLavaTank().getFluid();

        data.fluid1 = waterTankFluid.isEmpty() ? Fluids.EMPTY : waterTankFluid.getFluid();
        data.fluid1Amount = waterTankFluid.getAmount();
        data.fluid2 = lavaTankFluid.isEmpty() ? Fluids.EMPTY : lavaTankFluid.getFluid();
        data.fluid2Amount = lavaTankFluid.getAmount();

        data.targetBlock = blockEntity.getTargetBlock();
        data.targetCategory = blockEntity.getTargetCategory();
        data.progress = blockEntity.getCurrentProgress();
        data.maxProgress = blockEntity.getMaxProgress();

        ItemStackHandler itemHandler = blockEntity.getItemHandler();
        data.items = new ItemStack[itemHandler.getSlots()];
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            data.items[i] = itemHandler.getStackInSlot(i).copy();
        }

        return data;
    }

    private static void restoreGeneratorData(GeneratorBlockEntity blockEntity, GeneratorUpgradeData data) {
        if (data.fluid1Amount > 0 || data.fluid2Amount > 0) {
            GTDataComponents.FluidData fluidData = GTDataComponents.FluidData.create(
                    data.fluid1, data.fluid1Amount, data.fluid2, data.fluid2Amount);
            blockEntity.restoreFluidData(fluidData);
        }

        ItemStackHandler itemHandler = blockEntity.getItemHandler();
        int slotsToRestore = Math.min(data.items.length, itemHandler.getSlots());

        for (int i = 0; i < slotsToRestore; i++) {
            if (!data.items[i].isEmpty()) {
                itemHandler.setStackInSlot(i, data.items[i]);
            }
        }

        if (data.items.length > itemHandler.getSlots()) {
            Level level = blockEntity.getLevel();
            BlockPos pos = blockEntity.getBlockPos();

            for (int i = itemHandler.getSlots(); i < data.items.length; i++) {
                if (!data.items[i].isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), data.items[i]);
                }
            }
        }

        if (data.targetBlock != null && data.targetCategory != null) {
            blockEntity.restoreGenerationState(data.targetBlock, data.targetCategory, data.progress, data.maxProgress);
        }
    }

    private static void showUpgradeMessage(Player player, Component message, boolean success) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(message, true);
        }
    }

    public static boolean isGeneratorUpgradeItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        return item == GTItems.COPPER_TO_IRON_GENERATOR_UPGRADE.get() ||
                item == GTItems.IRON_TO_DIAMOND_GENERATOR_UPGRADE.get() ||
                item == GTItems.DIAMOND_TO_NETHERITE_GENERATOR_UPGRADE.get();
    }

    private static class GeneratorUpgradeData {
        Fluid fluid1;
        int fluid1Amount;
        Fluid fluid2;
        int fluid2Amount;
        ItemStack[] items;

        Block targetBlock;
        GeneratorBlockEntity.BlockCategory targetCategory;
        int progress;
        int maxProgress;
    }
}