package com.blocklogic.gentech.util;

import com.blocklogic.gentech.block.custom.CopperGeneratorBlock;
import com.blocklogic.gentech.block.entity.GeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class BucketInteractionHandler {
    private static final int BUCKET_CAPACITY = 1000;

    public static boolean handleBucketInteraction(Level level, BlockPos pos, BlockState state, Player player,
                                                  InteractionHand hand, BlockHitResult hit, ItemStack heldItem) {
        if (level.isClientSide()) {
            return false;
        }

        if (!(heldItem.getItem() instanceof BucketItem)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof GeneratorBlockEntity generatorBE)) {
            return false;
        }

        Direction hitSide = hit.getDirection();
        Direction facing = state.getValue(CopperGeneratorBlock.FACING);
        Direction absoluteSide = getAbsoluteSide(facing, hitSide);

        if (absoluteSide != Direction.WEST && absoluteSide != Direction.EAST) {
            return false;
        }

        FluidTank targetTank = getTargetTank(generatorBE, absoluteSide);
        if (targetTank == null) {
            return false;
        }

        BucketItem bucketItem = (BucketItem) heldItem.getItem();
        Fluid bucketFluid = bucketItem.content;

        if (bucketFluid == Fluids.EMPTY) {
            return handleFluidExtraction(level, pos, player, hand, heldItem, targetTank);
        } else {
            return handleFluidInsertion(level, pos, player, hand, heldItem, targetTank, bucketFluid);
        }
    }

    private static boolean handleFluidExtraction(Level level, BlockPos pos, Player player,
                                                 InteractionHand hand, ItemStack emptyBucket, FluidTank tank) {
        FluidStack tankFluid = tank.getFluid();

        if (tankFluid.isEmpty() || tankFluid.getAmount() < BUCKET_CAPACITY) {
            return false;
        }

        ItemStack filledBucket = getFilledBucket(tankFluid.getFluid());
        if (filledBucket.isEmpty()) {
            return false;
        }

        FluidStack extracted = tank.drain(BUCKET_CAPACITY, IFluidHandler.FluidAction.EXECUTE);
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
                                                FluidTank tank, Fluid bucketFluid) {
        FluidStack fluidToInsert = new FluidStack(bucketFluid, BUCKET_CAPACITY);
        if (!tank.isFluidValid(fluidToInsert)) {
            return false;
        }

        int filled = tank.fill(fluidToInsert, IFluidHandler.FluidAction.SIMULATE);
        if (filled != BUCKET_CAPACITY) {
            return false;
        }

        tank.fill(fluidToInsert, IFluidHandler.FluidAction.EXECUTE);

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

    private static FluidTank getTargetTank(GeneratorBlockEntity generatorBE, Direction absoluteSide) {
        return switch (absoluteSide) {
            case WEST -> generatorBE.getLavaTank();
            case EAST -> generatorBE.getWaterTank();
            default -> null;
        };
    }

    private static Direction getAbsoluteSide(Direction facing, Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return side;
        }

        return switch (facing) {
            case NORTH -> side;
            case SOUTH -> side.getOpposite();
            case EAST -> side.getClockWise();
            case WEST -> side.getCounterClockWise();
            default -> side;
        };
    }

    private static ItemStack getFilledBucket(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return new ItemStack(Items.WATER_BUCKET);
        } else if (fluid == Fluids.LAVA) {
            return new ItemStack(Items.LAVA_BUCKET);
        }
        return ItemStack.EMPTY;
    }
}