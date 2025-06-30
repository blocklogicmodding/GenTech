package com.blocklogic.gentech.util;

import com.blocklogic.gentech.block.entity.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;

public class TankBucketInteractionHandler {
    private static final int BUCKET_CAPACITY = 1000;

    public static boolean handleTankBucketInteraction(Level level, BlockPos pos, BlockState state, Player player,
                                                      InteractionHand hand, BlockHitResult hit, ItemStack heldItem) {
        if (level.isClientSide()) {
            return false;
        }

        if (!(heldItem.getItem() instanceof BucketItem)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TankBlockEntity tankBE)) {
            return false;
        }

        BucketItem bucketItem = (BucketItem) heldItem.getItem();
        Fluid bucketFluid = bucketItem.content;

        if (bucketFluid == Fluids.EMPTY) {
            return handleFluidExtraction(level, pos, player, hand, heldItem, tankBE);
        } else {
            return handleFluidInsertion(level, pos, player, hand, heldItem, tankBE, bucketFluid);
        }
    }

    private static boolean handleFluidExtraction(Level level, BlockPos pos, Player player,
                                                 InteractionHand hand, ItemStack emptyBucket, TankBlockEntity tankBE) {
        FluidStack tankFluid = tankBE.getFluidStack();

        if (tankFluid.isEmpty() || tankFluid.getAmount() < BUCKET_CAPACITY) {
            return false;
        }

        ItemStack filledBucket = getFilledBucket(tankFluid.getFluid());
        if (filledBucket.isEmpty()) {
            return false;
        }

        FluidStack extracted = tankBE.extractBucket();
        if (extracted.getAmount() != BUCKET_CAPACITY) {
            return false;
        }

        if (!player.getAbilities().instabuild) {
            emptyBucket.shrink(1);
        }

        if (!player.getInventory().add(filledBucket)) {
            player.drop(filledBucket, false);
        }

        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);

        return true;
    }

    private static boolean handleFluidInsertion(Level level, BlockPos pos, Player player,
                                                InteractionHand hand, ItemStack filledBucket,
                                                TankBlockEntity tankBE, Fluid bucketFluid) {
        FluidStack fluidToInsert = new FluidStack(bucketFluid, BUCKET_CAPACITY);

        FluidStack existingFluid = tankBE.getFluidStack();
        if (!existingFluid.isEmpty() && existingFluid.getFluid() != bucketFluid) {
            return false;
        }

        if (!tankBE.handleBucketInteraction(fluidToInsert, true)) {
            return false;
        }

        boolean success = tankBE.insertBucket(fluidToInsert);
        if (!success) {
            return false;
        }

        if (!player.getAbilities().instabuild) {
            filledBucket.shrink(1);
        }

        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
        if (!player.getInventory().add(emptyBucket)) {
            player.drop(emptyBucket, false);
        }

        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);

        return true;
    }

    private static ItemStack getFilledBucket(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return new ItemStack(Items.WATER_BUCKET);
        } else if (fluid == Fluids.LAVA) {
            return new ItemStack(Items.LAVA_BUCKET);
        }

        if (fluid == Fluids.EMPTY) {
            return ItemStack.EMPTY;
        }

        try {
            FluidStack fluidStack = new FluidStack(fluid, BUCKET_CAPACITY);
            ItemStack bucketStack = fluid.getFluidType().getBucket(fluidStack);
            if (!bucketStack.isEmpty() && bucketStack.getItem() != Items.BUCKET) {
                return bucketStack.copy();
            }
        } catch (Exception e) {
        }

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BucketItem bucketItem && bucketItem.content == fluid) {
                return new ItemStack(item);
            }
        }

        return ItemStack.EMPTY;
    }
}