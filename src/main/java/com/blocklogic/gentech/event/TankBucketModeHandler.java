package com.blocklogic.gentech.event;

import com.blocklogic.gentech.GenTech;
import com.blocklogic.gentech.component.GTDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = GenTech.MODID)
public class TankBucketModeHandler {
    private static final int BUCKET_CAPACITY = 1000;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        ItemStack heldItem = event.getItemStack();

        if (!isTankItem(heldItem)) return;

        GTDataComponents.TankMode mode = heldItem.getOrDefault(GTDataComponents.TANK_MODE.get(), GTDataComponents.TankMode.TANK);
        if (mode == GTDataComponents.TankMode.BUCKET) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockUse(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldItem = event.getItemStack();

        if (!isTankItem(heldItem)) return;

        GTDataComponents.TankMode mode = heldItem.getOrDefault(GTDataComponents.TANK_MODE.get(), GTDataComponents.TankMode.TANK);
        if (mode == GTDataComponents.TankMode.BUCKET) {
            Level level = event.getLevel();
            BlockPos clickedPos = event.getPos();
            BlockState clickedState = level.getBlockState(clickedPos);

            BlockPos fluidPos = clickedState.getFluidState().isEmpty()
                    ? clickedPos.relative(event.getFace())
                    : clickedPos;
            BlockState fluidState = level.getBlockState(fluidPos);

            boolean handled = false;

            if (handleFluidPickup(level, fluidPos, fluidState, heldItem, event.getEntity())) {
                handled = true;
            } else if (handleFluidPlacement(level, clickedPos, event.getFace(), heldItem, event.getEntity())) {
                handled = true;
            }

            event.setCancellationResult(handled ? InteractionResult.SUCCESS : InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    private static boolean handleFluidPickup(Level level, BlockPos pos, BlockState blockState, ItemStack tankItem, Player player) {
        if (level.isClientSide()) {
            return false;
        }

        FluidState fluidState = blockState.getFluidState();
        Fluid fluid = fluidState.getType();

        if (fluid == Fluids.EMPTY && !(blockState.getBlock() instanceof BucketPickup)) {
            return false;
        }

        GTDataComponents.FluidData fluidData = tankItem.getOrDefault(GTDataComponents.FLUID_DATA.get(), GTDataComponents.FluidData.empty());

        ItemStack fluidStack = ItemStack.EMPTY;

        if (blockState.getBlock() instanceof BucketPickup bucketPickup) {
            fluidStack = bucketPickup.pickupBlock(player, level, pos, blockState);
        } else if (fluidState.isSource()) {
            boolean removed = level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            fluidStack = new ItemStack(fluid.getBucket());
        } else {
            return false;
        }

        if (fluidStack.isEmpty()) {
            return false;
        }

        Fluid pickedFluid = getFluidFromBucketItem(fluidStack.getItem());
        if (pickedFluid == Fluids.EMPTY) {
            return false;
        }

        boolean canAccept = canAcceptFluid(fluidData, pickedFluid, tankItem);
        if (!canAccept) {
            return false;
        }

        addFluidToTank(tankItem, pickedFluid, BUCKET_CAPACITY);

        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    private static Fluid getFluidFromBucketItem(Item bucketItem) {
        for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid.getBucket() == bucketItem) {
                return fluid;
            }
        }
        return Fluids.EMPTY;
    }

    private static boolean handleFluidPlacement(Level level, BlockPos pos, Direction face, ItemStack tankItem, Player player) {
        if (level.isClientSide()) return false;

        GTDataComponents.FluidData fluidData = tankItem.getOrDefault(GTDataComponents.FLUID_DATA.get(), GTDataComponents.FluidData.empty());
        if (fluidData.isEmpty() || fluidData.fluid1Amount() < BUCKET_CAPACITY) return false;

        Fluid fluid = fluidData.getFluid1();
        if (fluid == Fluids.EMPTY) return false;

        BlockPos targetPos = pos.relative(face);
        BlockState targetState = level.getBlockState(targetPos);
        FluidState existing = targetState.getFluidState();

        if (existing.getType() != Fluids.EMPTY) {
            return false;
        }

        if (!targetState.isAir() && !targetState.canBeReplaced()) {
            return false;
        }

        BlockState placed = fluid.defaultFluidState().createLegacyBlock();
        if (!level.setBlock(targetPos, placed, 3)) {
            return false;
        }

        removeFluidFromTank(tankItem, BUCKET_CAPACITY);
        level.playSound(null, targetPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f);
        return true;
    }

    private static boolean isTankItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem &&
                blockItem.getBlock().toString().contains("tank");
    }


    private static boolean canAcceptFluid(GTDataComponents.FluidData fluidData, Fluid fluid, ItemStack tankItem) {
        if (fluidData.isEmpty()) return true;

        Fluid existingFluid = fluidData.getFluid1();
        if (existingFluid != fluid) return false;

        int capacity = getTankCapacity(tankItem);
        return fluidData.fluid1Amount() + BUCKET_CAPACITY <= capacity;
    }

    private static void addFluidToTank(ItemStack tankItem, Fluid fluid, int amount) {
        GTDataComponents.FluidData fluidData = tankItem.getOrDefault(GTDataComponents.FLUID_DATA.get(), GTDataComponents.FluidData.empty());
        int newAmount = fluidData.fluid1Amount() + amount;
        GTDataComponents.FluidData newFluidData = GTDataComponents.FluidData.create(fluid, newAmount, Fluids.EMPTY, 0);
        tankItem.set(GTDataComponents.FLUID_DATA.get(), newFluidData);
    }

    private static void removeFluidFromTank(ItemStack tankItem, int amount) {
        GTDataComponents.FluidData fluidData = tankItem.getOrDefault(GTDataComponents.FLUID_DATA.get(), GTDataComponents.FluidData.empty());
        int newAmount = Math.max(0, fluidData.fluid1Amount() - amount);
        Fluid fluid = newAmount > 0 ? fluidData.getFluid1() : Fluids.EMPTY;
        GTDataComponents.FluidData newFluidData = GTDataComponents.FluidData.create(fluid, newAmount, Fluids.EMPTY, 0);
        tankItem.set(GTDataComponents.FLUID_DATA.get(), newFluidData);
    }

    private static int getTankCapacity(ItemStack tankItem) {
        String itemName = tankItem.getItem().toString();
        if (itemName.contains("copper_tank")) return 20000;
        if (itemName.contains("iron_tank")) return 50000;
        if (itemName.contains("diamond_tank")) return 100000;
        if (itemName.contains("netherite_tank")) return 250000;
        return 20000;
    }
}