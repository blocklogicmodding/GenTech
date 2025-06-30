package com.blocklogic.gentech.block.entity;

import com.blocklogic.gentech.Config;
import com.blocklogic.gentech.block.custom.HydroCollectorBlock;
import com.blocklogic.gentech.block.custom.MagmaCollectorBlock;
import com.blocklogic.gentech.item.GTItems;
import com.blocklogic.gentech.screen.custom.CollectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CollectorBlockEntity extends BlockEntity implements MenuProvider {

    private static final int FLUID_CAPACITY = 10000;
    private static final int BASE_COLLECTION_TIME = 600;
    private static final int FLUID_PER_COLLECTION = 1000;
    private static final int UPGRADE_SLOT = 0;

    private int progress = 0;
    private int maxProgress = BASE_COLLECTION_TIME;
    private boolean hasValidSources = false;
    private int lastValidatedTick = -1;
    private static final int VALIDATION_INTERVAL = 20;

    private final ItemStackHandler itemHandler;
    private final FluidTank fluidTank;
    private CollectorType type;

    private int lastFluidAmount = -1;

    public CollectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(GTBlockEntities.COLLECTOR_BLOCK_ENTITY.get(), pos, blockState);

        this.type = determineType(blockState);

        this.fluidTank = new FluidTank(FLUID_CAPACITY) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid() == getTargetFluid();
            }

            @Override
            protected void onContentsChanged() {
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };

        this.itemHandler = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                if (slot != UPGRADE_SLOT) return false;
                return isValidSpeedUpgrade(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CollectorBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        int currentFluid = blockEntity.fluidTank.getFluidAmount();
        if (currentFluid != blockEntity.lastFluidAmount) {
            blockEntity.lastFluidAmount = currentFluid;
            level.sendBlockUpdated(pos, state, state, 3);
            blockEntity.setChanged();
        }

        if (level.getGameTime() % VALIDATION_INTERVAL == 0 || blockEntity.lastValidatedTick == -1) {
            blockEntity.validateSources();
            blockEntity.lastValidatedTick = (int) level.getGameTime();
        }

        if (!blockEntity.hasValidSources || !blockEntity.canCollect()) {
            if (blockEntity.progress > 0) {
                blockEntity.progress = 0;
                blockEntity.setChanged();
            }
            blockEntity.updateBlockState();
            return;
        }

        if (blockEntity.maxProgress != blockEntity.getActualCollectionTime()) {
            blockEntity.maxProgress = blockEntity.getActualCollectionTime();
        }

        blockEntity.progress++;

        if (blockEntity.progress >= blockEntity.maxProgress) {
            blockEntity.completeCollection();
        }

        blockEntity.updateBlockState();
        blockEntity.setChanged();
    }

    private void validateSources() {
        if (level == null) return;

        int sourceCount = 0;
        Fluid targetFluid = getTargetFluid();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);

            if (adjacentState.getFluidState().getType() == targetFluid &&
                    adjacentState.getFluidState().isSource()) {
                sourceCount++;
            }
        }

        this.hasValidSources = sourceCount >= 2;
    }

    private boolean canCollect() {
        return fluidTank.getSpace() >= FLUID_PER_COLLECTION;
    }

    private void completeCollection() {
        FluidStack fluidToAdd = new FluidStack(getTargetFluid(), FLUID_PER_COLLECTION);
        fluidTank.fill(fluidToAdd, IFluidHandler.FluidAction.EXECUTE);

        this.progress = 0;
        this.maxProgress = getActualCollectionTime();
    }

    private int getActualCollectionTime() {
        double speedMultiplier = getSpeedMultiplier();
        return Math.max(1, (int) (BASE_COLLECTION_TIME / speedMultiplier));
    }

    private double getSpeedMultiplier() {
        ItemStack upgrade = itemHandler.getStackInSlot(UPGRADE_SLOT);
        if (upgrade.isEmpty()) return 1.0;

        Item upgradeItem = upgrade.getItem();
        if (upgradeItem == GTItems.BASIC_SPEED_UPGRADE.get()) {
            return Config.getBasicSpeedUpgradeMultiplier();
        } else if (upgradeItem == GTItems.ADVANCED_SPEED_UPGRADE.get()) {
            return Config.getAdvancedSpeedUpgradeMultiplier();
        } else if (upgradeItem == GTItems.ULTIMATE_SPEED_UPGRADE.get()) {
            return Config.getUltimateSpeedUpgradeMultiplier();
        }

        return 1.0;
    }

    private boolean isValidSpeedUpgrade(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        return item == GTItems.BASIC_SPEED_UPGRADE.get() ||
                item == GTItems.ADVANCED_SPEED_UPGRADE.get() ||
                item == GTItems.ULTIMATE_SPEED_UPGRADE.get();
    }

    private CollectorType determineType(BlockState blockState) {
        if (blockState.getBlock() instanceof HydroCollectorBlock) {
            return CollectorType.HYDRO;
        } else if (blockState.getBlock() instanceof MagmaCollectorBlock) {
            return CollectorType.MAGMA;
        }
        return CollectorType.HYDRO;
    }

    private Fluid getTargetFluid() {
        return switch (type) {
            case HYDRO -> Fluids.WATER;
            case MAGMA -> Fluids.LAVA;
        };
    }

    public float getProgressLevel() {
        if (maxProgress == 0) return 0.0f;
        return (float) progress / (float) maxProgress;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public int getFluidAmount() {
        return fluidTank.getFluidAmount();
    }

    public int getFluidCapacity() {
        return fluidTank.getCapacity();
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public boolean hasValidSources() {
        return hasValidSources;
    }

    public CollectorType getCollectorType() {
        return type;
    }

    public @Nullable IFluidHandler getFluidHandler(Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return new OutputOnlyFluidHandler(fluidTank);
        }
        return null;
    }

    public @Nullable IItemHandler getItemHandler(Direction side) {
        return null;
    }

    private static class OutputOnlyFluidHandler implements IFluidHandler {
        private final FluidTank internal;

        public OutputOnlyFluidHandler(FluidTank internal) {
            this.internal = internal;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return internal.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return internal.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return internal.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return internal.drain(maxDrain, action);
        }
    }

    @Override
    public Component getDisplayName() {
        return switch (type) {
            case HYDRO -> Component.translatable("block.gentech.hydro_collector");
            case MAGMA -> Component.translatable("block.gentech.magma_collector");
        };
    }

    private void updateBlockState() {
        if (level == null || level.isClientSide()) return;

        BlockState currentState = level.getBlockState(worldPosition);
        boolean shouldBeCollecting = hasValidSources && canCollect() && progress > 0;

        if (currentState.getBlock() instanceof HydroCollectorBlock) {
            boolean isCurrentlyCollecting = currentState.getValue(HydroCollectorBlock.COLLECTING);
            if (isCurrentlyCollecting != shouldBeCollecting) {
                BlockState newState = currentState.setValue(HydroCollectorBlock.COLLECTING, shouldBeCollecting);
                level.setBlock(worldPosition, newState, 3);
            }
        } else if (currentState.getBlock() instanceof MagmaCollectorBlock) {
            boolean isCurrentlyCollecting = currentState.getValue(MagmaCollectorBlock.COLLECTING);
            if (isCurrentlyCollecting != shouldBeCollecting) {
                BlockState newState = currentState.setValue(MagmaCollectorBlock.COLLECTING, shouldBeCollecting);
                level.setBlock(worldPosition, newState, 3);
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CollectorMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.put("fluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putString("type", type.name());
        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putBoolean("hasValidSources", hasValidSources);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("fluidTank")) {
            fluidTank.readFromNBT(registries, tag.getCompound("fluidTank"));
        }
        if (tag.contains("type")) {
            try {
                this.type = CollectorType.valueOf(tag.getString("type"));
            } catch (IllegalArgumentException e) {
                this.type = determineType(getBlockState());
            }
        }

        this.progress = tag.getInt("progress");
        this.maxProgress = tag.getInt("maxProgress");
        this.hasValidSources = tag.getBoolean("hasValidSources");
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, GTBlockEntities.COLLECTOR_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof CollectorBlockEntity collectorBlockEntity) {
                        return collectorBlockEntity.getFluidHandler(direction);
                    }
                    return null;
                });

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GTBlockEntities.COLLECTOR_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> {
                    if (blockEntity instanceof CollectorBlockEntity collectorBlockEntity) {
                        return collectorBlockEntity.getItemHandler(direction);
                    }
                    return null;
                });
    }

    public void drops() {
        if (level == null || level.isClientSide()) return;

        ItemStack upgrade = itemHandler.getStackInSlot(UPGRADE_SLOT);
        if (!upgrade.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), upgrade);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(getBlockPos());
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            level.invalidateCapabilities(getBlockPos());
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public enum CollectorType {
        HYDRO, MAGMA
    }
}